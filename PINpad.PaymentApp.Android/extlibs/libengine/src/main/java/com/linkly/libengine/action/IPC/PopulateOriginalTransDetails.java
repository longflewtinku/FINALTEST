package com.linkly.libengine.action.IPC;

import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_PREAUTH_NOT_FOUND;
import static com.linkly.libui.UIScreenDef.PREAUTH_NOT_FOUND;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TCard;

public class PopulateOriginalTransDetails extends IAction {
    @Override
    public String getName() {
        return "PopulateOriginalTransDetails";
    }

    @Override
    public void run() {

        this.populateTrans();
    }

    private void populateTrans( ) {
        if( trans.isPreAuthCancellation() ) {
            TransRec originalTran = null;
            // preauth UID is set by action LookupPreauthByRfn - must be called prior to this
            if( trans.getPreauthUid() != null ) {
                originalTran = TransRecManager.getInstance().getTransRecDao().getByUid(trans.getPreauthUid());
            }

            if( originalTran == null ) {
                ui.showScreen(PREAUTH_NOT_FOUND);
                d.getProtocol().setInternalRejectReason( trans, IProto.RejectReasonType.PREAUTH_NOT_FOUND );
                d.getStatusReporter().reportStatusEvent(STATUS_ERR_PREAUTH_NOT_FOUND , trans.isSuppressPosDialog());
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            } else {
                // get original receipt number
                trans.getAudit().setReversalReceiptNumber(originalTran.getAudit().getReceiptNumber());

                // deep copy the card object from retrieved txn into our current active txn for receipt
                trans.setCard( TCard.copy(originalTran.getCard()) );

                // clear CVM flags
                trans.getCard().setCvmType(TCard.CvmType.NO_CVM);

                // get preauth amount for cancellation receipt
                trans.getAmounts().setAmount( originalTran.getAmounts().getAmount() );
                trans.getProtocol().setAuthCode( originalTran.getProtocol().getAuthCode() );
                trans.getProtocol().setRRN( originalTran.getProtocol().getRRN() );
                trans.getProtocol().setAccountType(originalTran.getProtocol().getAccountType());
            }
        }
    }
}
