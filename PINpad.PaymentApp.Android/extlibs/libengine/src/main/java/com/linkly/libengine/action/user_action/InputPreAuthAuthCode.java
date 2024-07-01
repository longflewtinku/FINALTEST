package com.linkly.libengine.action.user_action;

import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO_AUTO;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_PREAUTH_NOT_FOUND;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.UIScreenDef.ENTER_PREAUTH_AUTHCODE;
import static com.linkly.libui.UIScreenDef.PREAUTH_NOT_FOUND;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.IPC.CompletionCheckDetails;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

import timber.log.Timber;

public class InputPreAuthAuthCode extends IAction {
    @Override
    public String getName() {
        return "InputLastPreAuthCode";
    }

    @Override
    public void run() {
        if(trans.isCompletion() && trans.getCard().isCardholderPresent() ) {
            // skip this state because we'll ask for card to be presented to find original preauth
            Timber.i( "skipping state because cardholder is present, prompt for card presentation");
            return;
        }

        if (trans.getProtocol().getAuthCode() != null && trans.getProtocol().getAuthCode().length() > 0) {
            // already entered
            return;
        }

        int retryLimit = Util.getNumber(d.getPayCfg().getAuthcodeRetryLimit(),3);
        TransRec trans = d.getCurrentTransaction();
        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiTitleId, trans.getTransType().displayId);
        // NOTE: Override titleId with displayId
        ui.showInputScreen(ENTER_PREAUTH_AUTHCODE, map);
        while (retryLimit > 0) {
            IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
            switch (res) {
                case OK:
                    String result = ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1);

                    if (!lookupPreauthByAuthCode(result)) {
                        // couldn't find original preauth, handle error
                        map.put(IUIDisplay.uiError, d.getPrompt(IUIDisplay.String_id.STR_PREAUTH_NOT_FOUND_TRY_AGAIN));
                        ui.showInputScreen(ENTER_PREAUTH_AUTHCODE, map);
                        retryLimit--;
                    } else {
                        // go card-less, skip card presenting
                        d.getWorkflowEngine().setNextAction(CompletionCheckDetails.class);
                        trans.getProtocol().setAuthCode(result);
                        // success
                        return;
                    }
                    if (retryLimit == 0) {
                        ui.showScreen(PREAUTH_NOT_FOUND);
                        d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.PREAUTH_NOT_FOUND);
                        d.getStatusReporter().reportStatusEvent(STATUS_ERR_PREAUTH_NOT_FOUND , trans.isSuppressPosDialog());
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    }
                    break;
                case TIMEOUT:
                    d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.USER_TIMEOUT);
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    return;
                default:
                    d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.CANCELLED);
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    return;
            }
        }
    }

    private boolean lookupPreauthByAuthCode(String authCode) {

        // look up original txn based on RFN
        TransRec originalTxn = TransRecManager.getInstance().getTransRecDao().getByTransTypeAndAuthCode(PREAUTH, PREAUTH_AUTO, PREAUTH_MOTO, PREAUTH_MOTO_AUTO, authCode );
        if( originalTxn == null ) {
            Timber.e("original preauth not found for Auth Code %s", authCode);
            return false;
        }

        // set original preauth UID variable in trans rec for later use
        trans.setPreauthUid(originalTxn.getUid());
        return true;
    }
}
