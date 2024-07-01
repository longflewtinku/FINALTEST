package com.linkly.libengine.action.user_action;

import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_OPERATOR_TIMEOUT;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_CANCELLED;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;

import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.helpers.AudioUtils;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UI;
import com.linkly.libui.UIScreenDef;
import com.linkly.libui.speech.SpeechUtils;

public class UiCancelled extends IAction {

    private static final int TXN_CANCELLED_BEEP_FREQUENCY_HZ = 250;
    private static final int BEEP_DURATION_MS = 400;

    @Override
    public String getName() {
        return "UiCancelled";
    }

    @Override
    public void run() {
        if (trans == null) {
            // Failsafe timeout used in case transaction null, to avoid slipping into potential infinite
            //  loop involving showScreen.
            ui.getResultCode(ACT_INFORMATION, 5 * 60 * 1000);
            return;
        }
        TransRec safeTrans = trans;

        if (safeTrans.getAudit() != null && safeTrans.getAudit().getRejectReasonType() != null
                && safeTrans.getAudit().getRejectReasonType() == IProto.RejectReasonType.USER_TIMEOUT) {
            d.getStatusReporter().reportStatusEvent(STATUS_OPERATOR_TIMEOUT, safeTrans.isSuppressPosDialog());
            ui.showScreen(UIScreenDef.TRANS_TIMEOUT, safeTrans.getTransType().displayId);
        } else {
            d.getStatusReporter().reportStatusEvent(STATUS_TRANS_CANCELLED, safeTrans.isSuppressPosDialog());
            ui.showScreen(UIScreenDef.CANCELLED, safeTrans.getTransType().displayId);
        }

        if (SpeechUtils.getInstance().isSpeaking().equals(Boolean.FALSE) && safeTrans.getAudit().isAccessMode()) {
            String spokenString = UI.getInstance().getPrompt(IUIDisplay.String_id.STR_TRANSACTION_CANCELLED);
            SpeechUtils.getInstance().speak(spokenString);
        }

        if (d.getCurrentTransaction() != null && d.getCurrentTransaction().isFinancialTransaction()) {

            AudioUtils.playAudioResult("Cancelled.mp3", TXN_CANCELLED_BEEP_FREQUENCY_HZ, BEEP_DURATION_MS, d.getPayCfg().isUseCustomAudioForResult(), context, mal.getHardware());
        }

        int timeout = d.getPayCfg().getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.DECISION_SCREEN_TIMEOUT, safeTrans.getAudit().isAccessMode());
        ui.getResultCode(ACT_INFORMATION, timeout);
    }
}
