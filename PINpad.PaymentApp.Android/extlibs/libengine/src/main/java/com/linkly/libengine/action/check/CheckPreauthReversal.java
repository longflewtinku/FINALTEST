package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED_AND_REVERSED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REVERSAL_QUEUED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_REVERSAL_NOT_POSSIBLE;
import static com.linkly.libui.UIScreenDef.PREAUTH_WAS_CANCELLED;
import static com.linkly.libui.UIScreenDef.TRANSACTION_TYPE_CANT_BE_REVERSED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.env.Stan;
import com.linkly.libui.UIScreenDef;

public class CheckPreauthReversal extends IAction {

    @Override
    public String getName() {
        return "CheckReversal";
    }

    @Override
    public void run() {

        this.checkPreauthReversal();
    }

    private void reportErrorAndCancel(UIScreenDef def, IProto.RejectReasonType rejectReasonType) {
        ui.showScreen(def);
        d.getStatusReporter().reportStatusEvent(STATUS_ERR_REVERSAL_NOT_POSSIBLE ,trans.isSuppressPosDialog());
        d.getProtocol().setInternalRejectReason( trans, rejectReasonType );
        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
    }

    private void checkPreauthReversal() {
        TransRec originalTran = TransRecManager.getInstance().getTransRecDao().getByUid(trans.getPreauthUid());

        if (originalTran.getProtocol().getMessageStatus() == REVERSAL_QUEUED || originalTran.getProtocol().getMessageStatus() == FINALISED_AND_REVERSED) {
            reportErrorAndCancel(PREAUTH_WAS_CANCELLED, IProto.RejectReasonType.PREAUTH_ALREADY_CANCELLED );
            return;
        }

        if (!d.getCustomer().supportReversalsForTransType(originalTran.getTransType())) {
            reportErrorAndCancel(TRANSACTION_TYPE_CANT_BE_REVERSED, IProto.RejectReasonType.TRANS_NOT_ALLOWED );
            return;
        }

        if (!originalTran.isReversible() || !originalTran.isApproved()) {
            reportErrorAndCancel(TRANSACTION_TYPE_CANT_BE_REVERSED, IProto.RejectReasonType.TRANS_NOT_ALLOWED );
            return;
        }

        trans.getCard().setOrigTransClass(originalTran.getTransType().getTransClass());
        trans.getProtocol().setStan(Stan.getNewValue());
        d.getDebugReporter().reportDebugEvent( IDebug.DEBUG_EVENT.REVERSAL_STARTED, null );
    }

}
