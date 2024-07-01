package com.linkly.libengine.action.IPC;

import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_PREAUTH_NOT_FOUND;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_RFN_NOT_ENTERED;
import static com.linkly.libui.UIScreenDef.PREAUTH_NOT_FOUND;
import static com.linkly.libui.UIScreenDef.RFN_NOT_ENTERED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.wrappers.TagDataFromPOS;

import timber.log.Timber;

public class LookupPreauthByRfn extends IAction {
    @Override
    public String getName() {
        return "LookupPreauthByRfn";
    }

    @Override
    public void run() {
        TagDataFromPOS tagData = trans.getTagDataFromPos();
        String rfn = tagData.getRFN();
        if (Util.isNullOrEmpty(rfn)) {
            // RFN tag not present, cancel transaction
            Timber.i("RFN tag not present, not retrieving card by RFN, asking user to present card instead");
            ui.showScreen(RFN_NOT_ENTERED);
            d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.RFN_NOT_ENTERED);
            d.getStatusReporter().reportStatusEvent(STATUS_ERR_RFN_NOT_ENTERED, trans.isSuppressPosDialog());
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        } else {
            // lookup card details based on RFN tag
            if (!lookupCardDetailsFromRfnTag(rfn)) {
                // couldn't find original preauth, handle error
                ui.showScreen(PREAUTH_NOT_FOUND);
                d.getProtocol().setInternalRejectReason( trans, IProto.RejectReasonType.PREAUTH_NOT_FOUND );
                d.getStatusReporter().reportStatusEvent(STATUS_ERR_PREAUTH_NOT_FOUND , trans.isSuppressPosDialog());
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            } else {
                // found original preauth, jump the card states and go to CompletionCheckDetails
                Timber.i( "original preauth found" );
            }
        }
    }

    private boolean lookupCardDetailsFromRfnTag(String rfn) {
        final IProto proto = d.getProtocol();
        assert proto != null;

        // look up original txn based on RFN
        TransRec originalTxn = proto.lookupOriginalTransaction( rfn );
        if( originalTxn == null ) {
            Timber.e( "original preauth not found for RFN tag %s", rfn );
            return false;
        }

        // set original preauth UID variable in trans rec for later use
        trans.setPreauthUid(originalTxn.getUid());
        return true;
    }
}
