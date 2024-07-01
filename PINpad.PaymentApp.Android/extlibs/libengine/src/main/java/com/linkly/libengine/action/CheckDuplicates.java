package com.linkly.libengine.action;

import static com.linkly.libui.UIScreenDef.DUPLICATE_SESSION;

import android.content.Intent;

import com.linkly.libengine.action.IPC.PopulateTransaction;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libpositive.wrappers.PositiveTransAck;
import com.linkly.libui.IUIDisplay;

public class CheckDuplicates extends IAction {
    @Override
    public String getName() {
        return "CheckDuplicates";
    }

    @Override
    public void run() {
        if(trans.getTransType().autoTransaction) {
            checkDuplicateSession();
        }
    }

    private void checkDuplicateSession() {
        TransRec trans1 = TransRecManager.getInstance().getTransRecDao().getByUti(trans.getTransEvent().getUti());
        if (trans1 != null) {
            ui.showScreen(DUPLICATE_SESSION);
            ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
            d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.DUPLICATE_SESSION);
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);

            sendTxnAcknowledgeCreationWhenDuplicate();
        }
    }

    /**
     * Usually acknowledge is sent in the {@link PopulateTransaction} step. However, for duplicate session or txnRef issue,
     * the txn is getting cancelled before reaching that step, so need to send the acknowledgement for the txn creation
     */
    private void sendTxnAcknowledgeCreationWhenDuplicate() {
        // send response with UTI in
        Intent intent = new Intent();
        PositiveTransAck ack = new PositiveTransAck();
        ack.setUti(trans.getAudit().getUti());
        intent.putExtra(PositiveTransAck.class.getName(), ack.toJsonString());
        d.getMessages().sendTransactionAcknowledgeCreation(context, intent);
    }
}
