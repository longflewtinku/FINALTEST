package com.linkly.libengine.action.IPC;

import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO_AUTO;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_PREAUTH_NOT_FOUND;
import static com.linkly.libui.UIScreenDef.PREAUTH_NOT_FOUND;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libmal.global.util.Util;

import timber.log.Timber;

public class GetPreauthByAuthCode extends IAction {
    @Override
    public String getName() {
        return "GetPreauthByAuthCode";
    }

    @Override
    public void run() {
        String authCode = trans.getProtocol().getAuthCode();
        if (Util.isNullOrEmpty(authCode)) {
            // RFN tag not present, nothing to do, fall through to prompt user to present card for lookup of preauth that way
            Timber.i("Auth Code not present");
        } else {
            // lookup card details based on RFN tag
            if (!lookupPreauthByAuthCode(authCode)) {
                // couldn't find original preauth, handle error
                ui.showScreen(PREAUTH_NOT_FOUND);
                d.getProtocol().setInternalRejectReason( trans, IProto.RejectReasonType.PREAUTH_NOT_FOUND );
                d.getStatusReporter().reportStatusEvent(STATUS_ERR_PREAUTH_NOT_FOUND , trans.isSuppressPosDialog());
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            } else {
                // found original preauth, jump the card states and go to CompletionCheckDetails
                d.getWorkflowEngine().setNextAction(CompletionCheckDetails.class);

                // set cardholder not present by default for keyed preauth detail txns
                trans.getCard().setCardholderPresent(false);
            }
        }
    }

    private boolean lookupPreauthByAuthCode(String authCode) {
        // look up original txn based on RFN
        TransRec originalTxn = TransRecManager.getInstance().getTransRecDao().getByTransTypeAndAuthCode(PREAUTH, PREAUTH_AUTO, PREAUTH_MOTO, PREAUTH_MOTO_AUTO, authCode );
        if( originalTxn == null ) {
            Timber.e( "original preauth not found for Auth Code %s", authCode );
            return false;
        }

        // set original preauth UID variable in trans rec for later use
        trans.setPreauthUid(originalTxn.getUid());
        return true;
    }
}
