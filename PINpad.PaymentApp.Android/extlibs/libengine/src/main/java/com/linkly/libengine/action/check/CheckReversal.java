package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED_AND_REVERSED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REVERSAL_QUEUED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_REVERSAL_NOT_POSSIBLE;
import static com.linkly.libui.UIScreenDef.ALREADY_REVERSED;
import static com.linkly.libui.UIScreenDef.CANT_REVERSE_OFFLINE_AN_ONLINE_APPROVED_TRANS;
import static com.linkly.libui.UIScreenDef.INVALID_RECEIPT_NUMBER;
import static com.linkly.libui.UIScreenDef.TRANSACTION_TYPE_CANT_BE_REVERSED;
import static com.linkly.libui.UIScreenDef.UNABLE_TO_REVERSE;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.env.Stan;
import com.linkly.libui.UIScreenDef;

import java.util.Date;

import timber.log.Timber;

public class CheckReversal extends IAction {

    @Override
    public String getName() {
        return "CheckReversal";
    }

    @Override
    public void run() {

        this.checkReversal();
    }

    private void invalidReceiptNumber() {
        ui.showScreen(INVALID_RECEIPT_NUMBER);
        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
    }

    private void displayErrorAndCancel(UIScreenDef def) {
        ui.showScreen(def);
        d.getStatusReporter().reportStatusEvent(STATUS_ERR_REVERSAL_NOT_POSSIBLE , trans.isSuppressPosDialog());
        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
    }

    private void checkReversal() {
        TransRec lastRec = TransRecManager.getInstance().getTransRecDao().getLatestByTransType( RECONCILIATION );

        Integer reversalReceiptNumber = trans.getAudit().getReversalReceiptNumber();

        if (reversalReceiptNumber == null) {
            Timber.e( "unable to reverse - reversal receipt number not set" );
            invalidReceiptNumber();
            return;
        }

        TransRec originalTran = TransRecManager.getInstance().getTransRecDao().getByReceiptNumber(reversalReceiptNumber);
        Timber.i( "reversal receipt no %d", reversalReceiptNumber );

        if (originalTran == null) {
            Timber.e( "unable to reverse - couldn't get last trans record" );
            invalidReceiptNumber();
            return;
        }

        // if last reconciliation not found - skip date check
        if (lastRec != null) {
            Date dateReversal = new Date(originalTran.getAudit().getTransDateTime());
            Date dateRec = new Date(lastRec.getAudit().getTransDateTime());

            Timber.i( "DATE ORIG TXN: %s", originalTran.getAudit().getTransDateTimeAsString("dd/MM/yyyy HH:mm:ss"));
            Timber.i( "DATE REC     : %s", lastRec.getAudit().getTransDateTimeAsString("dd/MM/yyyy HH:mm:ss"));

            if (dateReversal.compareTo(dateRec) < 0) {
                Timber.e( "unable to reverse - last txn was in previous batch" );
                d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.TRANSACTION_ALREADY_SETTLED);
                displayErrorAndCancel(UNABLE_TO_REVERSE);
                return;
            }
        }

        // if 1220 advice has already been sent, disallow reversal of it. To know if it was a 1220 advice, we check if originally authorised offline
        if(originalTran.getProtocol().isAuthMethodOfflineApproved() && originalTran.getProtocol().getMessageStatus().isFinalised()){
            Timber.e( "unable to reverse - advice already sent" );
            d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.VOID_NOT_ALLOWED_FOR_ADVICE);
            displayErrorAndCancel(UNABLE_TO_REVERSE);
            return;
        }

        // if doing offline reversal of an online approved transaction, disallow it
        if (trans.isStartedInOfflineMode() && !originalTran.getProtocol().isAuthMethodOfflineApproved()) {
            Timber.e("unable to reverse - offline reversal of online approved txn not allowed");
            d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.OFFLINE_VOID_NOT_ALLOWED_FOR_ONLINE_APPROVED_TRANSACTION);
            displayErrorAndCancel(CANT_REVERSE_OFFLINE_AN_ONLINE_APPROVED_TRANS);
            return;
        }

        // don't allow reversal if last is already reversed
        if (originalTran.getProtocol().getMessageStatus() == REVERSAL_QUEUED || originalTran.getProtocol().getMessageStatus() == FINALISED_AND_REVERSED) {
            Timber.e( "unable to reverse - txn already reversed" );
            d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.ALREADY_VOIDED);
            displayErrorAndCancel(ALREADY_REVERSED);
            return;
        }

        // if customer disallows reversals, reject here
        if (!d.getCustomer().supportReversalsForTransType(originalTran.getTransType())) {
            Timber.e( "unable to reverse - customer disallows reversals for this trans type" );
            displayErrorAndCancel(TRANSACTION_TYPE_CANT_BE_REVERSED);
            return;
        }

        boolean reversible = originalTran.isReversible();
        boolean tranApproved = originalTran.isApproved();

        if (!reversible || !tranApproved) {
            Timber.e( "unable to reverse - reversible value = %b, tran approved value = %b", reversible, tranApproved );
            displayErrorAndCancel(TRANSACTION_TYPE_CANT_BE_REVERSED);
            return;
        }

        trans.getCard().setOrigTransClass(originalTran.getTransType().getTransClass());
        trans.getProtocol().setStan(Stan.getNewValue());
    }

}
