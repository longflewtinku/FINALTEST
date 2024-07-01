package com.linkly.libengine.action.IPC;


import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_PREAUTH_EXISTS;
import static com.linkly.libui.UIScreenDef.PREAUTH_EXISTS;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;

import timber.log.Timber;

public class CheckExistingPreAuth extends IAction {
    @Override
    public String getName() {
        return "CheckExistingPreAuth";
    }

    @Override
    public void run() {
        if( GetPreauthByCardData.getPreauthByCardDetails() != null ) {
            Timber.e( "There is an open Preauth exists with the same card details" );
            ui.showScreen(PREAUTH_EXISTS);
            d.getProtocol().setInternalRejectReason( trans, IProto.RejectReasonType.PREAUTH_EXISTS );
            d.getStatusReporter().reportStatusEvent(STATUS_ERR_PREAUTH_EXISTS , trans.isSuppressPosDialog());
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }
        // else there is no pre-auth for this card, continue transaction
    }
}
