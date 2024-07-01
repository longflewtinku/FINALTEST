package com.linkly.libengine.action.user_action;

import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_APPROVED;
import static com.linkly.libui.IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.IUIDisplay.String_id.STR_BALANCE;
import static com.linkly.libui.UIScreenDef.AUTHORISED_VAR;
import static com.linkly.libui.UIScreenDef.NL_AUTHORISED;
import static com.linkly.libui.UIScreenDef.NL_AUTHORISED_WITH_SIGN;

import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus;
import com.linkly.libengine.helpers.AudioUtils;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UI;
import com.linkly.libui.speech.SpeechUtils;

public class UiApproved extends IAction {

    int TXN_SUCCESS_BEEP_FREQUENCY_HZ = 2700;
    int BEEP_DURATION_MS = 400;

    @Override
    public String getName() {
        return "UiApproved";
    }

    @Override
    public void run() {
        String spokenString = UI.getInstance().getPrompt(IUIDisplay.String_id.STR_TRANS_APPROVED_PROMPT);
        d.getStatusReporter().reportStatusEvent(STATUS_TRANS_APPROVED, trans.isSuppressPosDialog());

        if (!CoreOverrides.get().isAutoFillTrans()) {
            String prompt = buildPrompt();

            if (shouldShowPrompt(prompt)) {
                ui.showScreen(AUTHORISED_VAR, trans.getTransType().displayId, prompt);
            } else if (trans.getProtocol().isSignatureRequired()) {
                ui.showScreen(NL_AUTHORISED_WITH_SIGN, trans.getTransType().displayId);
            } else {
                ui.showScreen(NL_AUTHORISED, trans.getTransType().displayId);
            }

            if (d.getCurrentTransaction() != null && d.getCurrentTransaction().isFinancialTransaction()) {
                AudioUtils.playAudioResult("Approved.mp3", TXN_SUCCESS_BEEP_FREQUENCY_HZ, BEEP_DURATION_MS, d.getPayCfg().isUseCustomAudioForResult(), context, mal.getHardware());
            }

            if( !SpeechUtils.getInstance().isSpeaking() && trans.getAudit().isAccessMode() ) {
                SpeechUtils.getInstance().speak(spokenString);
            }

            showUiForTransaction();
        }
    }

    private String buildPrompt() {
        String prompt = "";

        if (!Util.isNullOrWhitespace(trans.getProtocol().getAdditionalResponseText()) &&
                !Util.containsAuthCode(trans.getProtocol().getAdditionalResponseText()) &&
                !Util.containsDeclined(trans.getProtocol().getAdditionalResponseText()) &&
                !Util.containsApproved(trans.getProtocol().getAdditionalResponseText())) {
            prompt += "\r\n" + trans.getProtocol().getAdditionalResponseText();
        }

        if (!Util.isNullOrWhitespace(trans.getCard().getCtlsBalanceValueAOSA())) {
            String formattedAmount = curr.formatUIAmount(trans.getCard().getCtlsBalanceValueAOSA() + "", FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
            prompt += "\r\n" + d.getPrompt(STR_BALANCE) + ": " + formattedAmount;
        }

        if (trans.getProtocol().getMessageStatus() == MessageStatus.DEFERRED_AUTH) {
            prompt = "";
        }

        return prompt;
    }

    private boolean shouldShowPrompt(String prompt) {
        return !Util.isNullOrWhitespace(prompt) && !prompt.contains("Approved");
    }

    private void showUiForTransaction() {
        int timeout = d.getPayCfg().getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.DECISION_SCREEN_TIMEOUT, trans.getAudit().isAccessMode());
        ui.getResultCode(ACT_INFORMATION, timeout);
    }
}
