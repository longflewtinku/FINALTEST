package com.linkly.libengine.action.user_action;


import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_DECLINED;
import static com.linkly.libui.IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.IUIDisplay.String_id.STR_BALANCE;
import static com.linkly.libui.IUIDisplay.String_id.STR_DECLINED_UPPER;
import static com.linkly.libui.UIScreenDef.DECLINED;
import static com.linkly.libui.UIScreenDef.DECLINED_VAR;

import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.helpers.AudioUtils;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UI;
import com.linkly.libui.speech.SpeechUtils;

public class UiDeclined extends IAction {

    private static final int TXN_DECLINED_BEEP_FREQUENCY_HZ = 250;
    private static final int BEEP_DURATION_MS = 400;

    @Override
    public String getName() {
        return "UiDeclined";
    }

    @Override
    public void run() {
        String prompt = "";
        String spokenString = UI.getInstance().getPrompt(IUIDisplay.String_id.STR_TRANS_DECLINED_PROMPT);

        if (!Util.isNullOrWhitespace(trans.getProtocol().getAdditionalResponseText()) &&
                !Util.containsAuthCode(trans.getProtocol().getAdditionalResponseText()) &&
                !Util.containsDeclined(trans.getProtocol().getAdditionalResponseText()) &&
                !Util.containsApproved(trans.getProtocol().getAdditionalResponseText())) {
            prompt += "\r\n" + trans.getProtocol().getAdditionalResponseText();
        }

        if (trans.getCard().getCtlsBalanceValueAOSA() != null && !trans.getCard().getCtlsBalanceValueAOSA().isEmpty()) {
            String formattedAmount = curr.formatUIAmount(trans.getCard().getCtlsBalanceValueAOSA() + "", FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
            prompt += "\r\n" + d.getPrompt(STR_BALANCE) + ": " + formattedAmount;
        }

        // if POS response text is set, send free-format status display for POS dialog
        if( !Util.isNullOrWhitespace(trans.getProtocol().getPosResponseText())) {
            // build 2 lines of decline text, with "DECLINE" and response code on top line, then specific text on bottom line e.g. "DECLINE\nCONTACT ISSUER"
            String declineText = String.format( "%s\n%s", d.getPrompt(STR_DECLINED_UPPER), trans.getProtocol().getPosResponseText() );
            // send free-format text type status request
            d.getStatusReporter().reportStatusEvent(STATUS_TRANS_DECLINED, declineText ,trans.isSuppressPosDialog());
        } else {
            // send fixed type status request
            d.getStatusReporter().reportStatusEvent(STATUS_TRANS_DECLINED , trans.isSuppressPosDialog());
        }

        if(!Util.isNullOrWhitespace(prompt)){
            ui.showScreen(DECLINED_VAR, trans.getTransType().displayId, prompt);
        }else{
            ui.showScreen(DECLINED, trans.getTransType().displayId);
        }

        if (d.getCurrentTransaction() != null && d.getCurrentTransaction().isFinancialTransaction()) {
            AudioUtils.playAudioResult("Declined.mp3", TXN_DECLINED_BEEP_FREQUENCY_HZ, BEEP_DURATION_MS, d.getPayCfg().isUseCustomAudioForResult(), context, mal.getHardware());
        }

        if( !SpeechUtils.getInstance().isSpeaking() && trans.getAudit().isAccessMode() ) {
            SpeechUtils.getInstance().speak(spokenString);
        }

        int timeout = d.getPayCfg().getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.DECISION_SCREEN_TIMEOUT, trans.getAudit().isAccessMode());
        ui.getResultCode(ACT_INFORMATION, timeout);

        if (trans.getAmounts().getDiscountedAmount() > 0) {
            IProto iproto = d.getProtocol();
            iproto.discountVoucherReverse(trans);
        }
    }
}
