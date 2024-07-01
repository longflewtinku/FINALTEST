package com.linkly.libengine.action.IPC;

import static com.linkly.libengine.engine.EngineManager.TransType.MANUAL_REVERSAL_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_CANCEL_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION_AUTO;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalState.REVERSIBLE;
import static com.linkly.libui.UIScreenDef.NO_TRANSACTION_FOUND;

import android.content.Intent;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.wrappers.PositiveTransAck;
import com.linkly.libpositive.wrappers.TagDataFromPOS;

import timber.log.Timber;

public class PopulateTransaction extends IAction {
    @Override
    public String getName() {
        return "PopulateTransaction";
    }

    @Override
    public void run() {

        this.ipcPopulateTrans();
    }

    private void ipcPopulateTrans( ) {

        trans.setSuppressPosDialog(false);

        if (trans == null || trans.getTransEvent() == null) {
            return;
        }

        /* don't run auto sales with no amount */
        if (trans.getTransType() == EngineManager.TransType.SALE_AUTO && trans.getTransEvent().getAmount() <= 0) {
            trans.setCancelled(true);
            return;
        }

        if (trans.getTransType() == EngineManager.TransType.REFUND_AUTO && trans.getTransEvent().getAmount() <= 0) {
            trans.setCancelled(true);
            return;
        }

        if (trans.getTransEvent().getPadTagJson() != null) {
            trans.setTagDataFromPos(TagDataFromPOS.builder(trans.getTransEvent().getPadTagJson()));
        }

        if (trans.getTagDataFromPos() != null && trans.getTagDataFromPos().getPAT() != null && trans.isPatMode("2")) {
            trans.setSuppressPosDialog(true);
        }
        /* amount must always be passed in */
        trans.getAmounts().setAmountUserEntered( Long.toString( trans.getTransEvent().getAmount() ) );
        trans.getAudit().setVirtualMid(trans.getTransEvent().getVirtualMid());
        trans.getAudit().setVirtualTid(trans.getTransEvent().getVirtualTid());
        trans.getAudit().setVirtualName(trans.getTransEvent().getVirtualName());
        trans.getAudit().setDisablePrinting(trans.getTransEvent().isDisablePrinting());
        trans.getAudit().updateDateTimes(trans.getTransEvent().getDateTimeyyyyMMddhhmmss());
        trans.getAudit().setReference(trans.getTransEvent().getReference());
        trans.getAudit().setIpcUserId(trans.getTransEvent().getUserId());
        trans.getAudit().setIpcUserPwd(trans.getTransEvent().getUserPwd());
        trans.getAudit().setIpcDepartmentId(trans.getTransEvent().getDepartmentId());
        // if UTI is specified from POS, set this as UTI in trans record
        if( !Util.isNullOrEmpty(trans.getTransEvent().getUti()) ) {
            trans.getAudit().setUti(trans.getTransEvent().getUti());
        }

        trans.setPrintOnTerminal(trans.getTransEvent().isUseTerminalPrinter());

        // if trans type is cash advance, ensure cashback amt = total amount and purchase amount = 0
        if( trans.isCash() ) {
            trans.getAmounts().setAmount(0);
            trans.getAmounts().setCashbackAmount(trans.getTransEvent().getAmount());
        }
        else if(trans.getTransType() == PREAUTH_CANCEL_AUTO)
        {
            Timber.e("Amounts already set for pre-auth cancel");
        }
        else{
            trans.getAmounts().setAmount(trans.getTransEvent().getAmount());
            trans.getAmounts().setCashbackAmount((long)trans.getTransEvent().getCbAmount());
        }

        /* reversals either pass in reference number, receipt number, or 0 which means get the last transaction */
        if (trans.getTransType() == MANUAL_REVERSAL_AUTO) {

            if(!Util.isNullOrWhitespace(trans.getTransEvent().getReference())) {
                // reverse by reference/TxnRef
                String transactionReference = trans.getTransEvent().getReference();
                TransRec txnToReverse = TransRecManager.getInstance().getTransRecDao().getByReference(transactionReference);
                trans.copyFromOriginalTxnForReversal(txnToReverse);

            } else if (trans.getTransEvent().getReceiptNumberForReversal() != 0) {
                // reverse by receipt number
                int reversalReceiptNumber = (int) trans.getTransEvent().getReceiptNumberForReversal();
                trans.getAudit().setReversalReceiptNumber(reversalReceiptNumber);

            } else if(trans.getTransEvent().getUti() != null && trans.getTransEvent().getUti().length() > 0 ) {
                // reverse by UTI
                TransRec latest = TransRecManager.getInstance().getTransRecDao().getByUti(trans.getTransEvent().getUti());
                if (latest != null) {
                    trans.getAudit().setDisablePrinting(trans.getTransEvent().isDisablePrinting());
                    trans.getAudit().setReversalReceiptNumber(latest.getAudit().getReceiptNumber());
                    trans.setAmounts(latest.getAmounts());
                } else {
                    ui.showScreen(NO_TRANSACTION_FOUND);
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                }

            } else {
                // find latest txn that can be reversed
                TransRec latest = TransRecManager.getInstance().getTransRecDao().getLatestByReversalState(REVERSIBLE, true);
                if (latest != null) {
                    trans.getAudit().setDisablePrinting(trans.getTransEvent().isDisablePrinting());
                    trans.getAudit().setReversalReceiptNumber(latest.getAudit().getReceiptNumber());
                    trans.setAmounts(latest.getAmounts());
                } else {
                    ui.showScreen(NO_TRANSACTION_FOUND);
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                }
            }
        }

        // Change transaction type to "RECONCILIATION" as Payment uses it for both auto and not auto; and save the original type
        if (trans.getTransType() == RECONCILIATION_AUTO) {
            trans.setReconciliationOriginalTransType(trans.getTransType());
            trans.setTransType(RECONCILIATION);
        }

        // send response with UTI in
        Intent intent = new Intent();
        PositiveTransAck ack = new PositiveTransAck();

        ack.setUti(trans.getAudit().getUti());
        ack.setAmountTrans(trans.getAmounts().getAmount());
        ack.setAmountGratuity(trans.getAmounts().getTip());
        ack.setAmountCashback(trans.getAmounts().getCashbackAmount());
        ack.setTransType(trans.getTransEvent().getTransType());

        intent.putExtra(PositiveTransAck.class.getName(), ack.toJsonString());

        d.getMessages().sendTransactionAcknowledgeCreation(context, intent);
    }
}
