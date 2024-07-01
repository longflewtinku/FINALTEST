package com.linkly.libengine.action.check;

import static com.linkly.libui.UIScreenDef.CANT_REVERSE_A_REFUND;
import static com.linkly.libui.UIScreenDef.NO_TRANSACTION_FOUND;

import android.text.TextUtils;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.IPC.PopulateTransaction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;

public class CheckReferenceForReversal extends IAction {
    @Override
    public String getName() {
        return "CheckReference";
    }

    @Override
    public void run() {
        String transactionReference = trans.getTransEvent().getReference();
        if (!TextUtils.isEmpty(transactionReference)) {
            TransRec txnToReverse = TransRecManager.getInstance().getTransRecDao().getByReference(transactionReference);

            if (txnToReverse != null) {
                if (txnToReverse.isRefund() && !d.getCustomer().supportReversalsForTransType(EngineManager.TransType.REFUND)) {
                    ui.showScreen(CANT_REVERSE_A_REFUND);
                    d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.VOID_NOT_ALLOWED_FOR_REFUND);
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                } else {
                    // continue to next state
                    // don't need to show Reversal chooser UI, skip to next action
                    d.getWorkflowEngine().setNextAction(PopulateTransaction.class);
                }
            } else {
                ui.showScreen(NO_TRANSACTION_FOUND);
                d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.TRANSACTION_NOT_FOUND);
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            }
        }
    }
}
