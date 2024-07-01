package com.linkly.libengine.action.user_action;

import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO_AUTO;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.RRN_ENTERED;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.UIScreenDef.ENTER_PREAUTH_RRN_CODE;
import static com.linkly.libui.UIScreenDef.PREAUTH_NOT_FOUND;

import android.text.TextUtils;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.status.IStatus;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

import timber.log.Timber;

public class InputPreAuthRRN extends IAction {
    @Override
    public String getName() {
        return "InputLastRRN";
    }

    @Override
    public void run() {
        int retryLimit = Util.getNumber(d.getPayCfg().getRrnRetryLimit(),3);
        if (!TextUtils.isEmpty(trans.getProtocol().getRRN())) {
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiTitleId, trans.getTransType().displayId);
        ui.showInputScreen(ENTER_PREAUTH_RRN_CODE, map);

        while (retryLimit > 0) {
            IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
            switch (res) {
                case OK:
                    String result = ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1);
                    /* if a completion doesn't have a card then RRN can be used to indicate to the server what is happening */
                    if (trans.getCard().getCaptureMethod() == TCard.CaptureMethod.NOT_CAPTURED && trans.isCompletion())
                        trans.getCard().setCaptureMethod(RRN_ENTERED);
                    if (!lookupPreauthByRrn(result)) {
                        map.put(IUIDisplay.uiError, d.getPrompt(IUIDisplay.String_id.STR_PREAUTH_NOT_FOUND_TRY_AGAIN));
                        ui.showInputScreen(ENTER_PREAUTH_RRN_CODE, map);
                        retryLimit--;
                    } else {
                        Timber.i("Found original transaction with RRN : %s", trans.getPreauthUid());
                        return;
                    }
                    if (retryLimit == 0) {
                        // couldn't find original preauth by RRN, handle error
                        ui.showScreen(PREAUTH_NOT_FOUND);
                        d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.PREAUTH_NOT_FOUND);
                        d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_PREAUTH_NOT_FOUND , trans.isSuppressPosDialog());
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    }
                    break;
                case TIMEOUT:
                    d.getProtocol().setInternalRejectReason(trans,IProto.RejectReasonType.USER_TIMEOUT);
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    return;
                default:
                    d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.CANCELLED);
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    return;
            }
        }

    }

    private boolean lookupPreauthByRrn(String rrn) {

        // look up original txn based on RRN
        TransRec originalTxn = TransRecManager.getInstance().getTransRecDao().getByTransTypeAndRrn(PREAUTH, PREAUTH_AUTO, PREAUTH_MOTO, PREAUTH_MOTO_AUTO, rrn);
        if (originalTxn == null) {
            Timber.d("original preauth not found for RRN %s", rrn);
            return false;
        }

        // set original preauth UID variable in trans rec for later use
        trans.setPreauthUid(originalTxn.getUid());
        return true;
    }
}
