package com.linkly.libengine.action.user_action;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.ADVICE_QUEUED;
import static com.linkly.libui.IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.IUIDisplay.String_id.STR_AMOUNT_CAPS;
import static com.linkly.libui.IUIDisplay.String_id.STR_AUTHORISED_BY_CARD_CENTRE;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.CALL_CARD_CENTRE_NL_VAR;
import static com.linkly.libui.UIScreenDef.ENTER_AUTH_CODE;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

import timber.log.Timber;

public class InputReferral extends IAction {
    @Override
    public String getName() {
        return "InputReferral";
    }

    @Override
    public void run() {
        if (!trans.canRefer(d.getPayCfg())) {
            trans.getAudit().setRejectReasonType( IProto.RejectReasonType.DECLINED);
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
            Timber.i( "Transaction doesn't support referrals");
            return;
        }

        // check the limits
        int limit = trans.getCard().getCardsConfig(d.getPayCfg()).getLimits().getTelAuthMax();
        if (trans.getCard().isPinVerificationRequired()) {
            limit = trans.getCard().getCardsConfig(d.getPayCfg()).getLimits().getTelPinAuthMax();
        }

        if (limit > 0 && trans.getAmounts().getTotalAmount() > (limit * TAmounts.getAmountCurExMul(d.getPayCfg().getCurrencyNum()))) {
            trans.getAudit().setRejectReasonType( IProto.RejectReasonType.DECLINED);
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
            Timber.i( "Transaction is over the referral limit: " + limit);
            return;
        }

        long total = trans.getAmounts().getTotalAmount();
        String totalFormatted = curr.formatAmount(String.valueOf(total), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());

        // please call card centre
        String msg = "";

        String number = trans.getProtocol().getReferralNumber();
        if (number != null && !number.isEmpty())
            msg += "No. (" + number + ")";

        ui.showScreen(CALL_CARD_CENTRE_NL_VAR, trans.getTransType().displayId, msg);
        ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, IUIDisplay.INFO_TIMEOUT);

        // for debug only
        if (CoreOverrides.get().isApproveReferral()) {
            trans.updateMessageStatus(ADVICE_QUEUED);
            d.getWorkflowEngine().setNextAction(TransactionApproval.class);
            trans.setReferred(true);
            trans.getProtocol().setAuthEntity(TProtocol.AuthEntity.ACQUIRER);
            trans.save();
            return;
        }

        // authorised offline sale ?
        msg = d.getPrompt(STR_AUTHORISED_BY_CARD_CENTRE) + "\n";
        msg += "MID: " + d.getPayCfg().getMid() + "\n";
        msg += "TID: " + trans.getBestTerminalId(d.getPayCfg().getStid()) + "\n";
        msg += d.getPrompt(STR_AMOUNT_CAPS) + ": " + totalFormatted + "\n";
        if (number != null && !number.isEmpty())
            msg += "\n(" + number + ")";

        // enter auth code
        if (UIHelpers.uiYesNoQuestion(d,trans.getTransType().getDisplayName(), msg, IUIDisplay.LONG_TIMEOUT) && uiInputEnterAuthCode(d)) {
            trans.updateMessageStatus(ADVICE_QUEUED);
            d.getWorkflowEngine().setNextAction(TransactionApproval.class);
            trans.setReferred(true);
            trans.getProtocol().setAuthEntity(TProtocol.AuthEntity.ACQUIRER);
        } else {
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }
        trans.save();
    }

    private boolean uiInputEnterAuthCode(IDependency dependencies) {

        if (trans.getProtocol().getAuthCode() != null && trans.getProtocol().getAuthCode().length() > 0) {
            return true;
        }

        return uiInputAuthCode(dependencies);
    }

    private boolean uiInputAuthCode(IDependency dependencies) {
        TransRec trans = dependencies.getCurrentTransaction();

        String authCode = trans.getProtocol().getAuthCode();
        if (authCode != null && authCode.length() > 0) {
            return true;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiTitleId, trans.getTransType().displayId);

        ui.showInputScreen(ENTER_AUTH_CODE, map);
        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            String result = ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1);
            trans.getProtocol().setAuthCode(result);
            return true;
        }

        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        return false;
    }
}
