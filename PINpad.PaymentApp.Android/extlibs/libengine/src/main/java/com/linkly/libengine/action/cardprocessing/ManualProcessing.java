package com.linkly.libengine.action.cardprocessing;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_QUESTION;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.ENTER_HOUSE_NO;
import static com.linkly.libui.UIScreenDef.ENTER_POST_CODE;
import static com.linkly.libui.UIScreenDef.PLEASE_CHOOSE_ACCOUNT_TYPE;
import static com.linkly.libui.UIScreenDef.TRANS_CANCEL;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.status.IStatus;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.display.DisplayQuestion;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class ManualProcessing extends IAction {

    @Override
    public String getName() {
        return "ManualProcessing";
    }

    @Override
    public void run() {

        if(!(trans.getTransType().equals(EngineManager.TransType.SALE_MOTO_AUTO) || trans.getTransType().equals(EngineManager.TransType.REFUND_MOTO_AUTO)
                || trans.getTransType().equals(EngineManager.TransType.SALE_MOTO) || trans.getTransType().equals(EngineManager.TransType.REFUND_MOTO)
                || trans.getTransType().equals(EngineManager.TransType.PREAUTH_MOTO) || trans.getTransType().equals(EngineManager.TransType.PREAUTH_MOTO_AUTO))) {
            Timber.i( "Not a manual transaction, skip task");
            return;
        }

        TCard cardInfo = trans.getCard();

        if (cardInfo.isMailOrder() || cardInfo.isOverTelephone()) {
            Timber.i("Skip if mail order or telephone order is already selected.");
            return;
        }

        d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_PROCESSING , trans.isSuppressPosDialog());
        
        if (d.getCustomer().supportAvs())
        {
            uiInputHouseNumber();
            if (d.getWorkflowEngine().isJumping()) {
                // cancelled by user
                return;
            }
            uiInputPostCode();
            if (d.getWorkflowEngine().isJumping()) {
                // cancelled by user
                return;
            }
        }

        if (d.getCustomer().supportMotoAndTelephone()) {
            uiInputManualType();
        }

    }

    private void uiInputManualType() {
        final String TELEPHONE = "TELEPHONE";
        final String MAIL_ORDER = "MAIL ORDER";

        if (trans == null || trans.getCard().isFaultyMsr() )
            return;

        TCard cardInfo = trans.getCard();

        String result = "";

        boolean telephoneFromCfg = d.getPayCfg().isTelephone();
        boolean mailOrderFromCfg = d.getPayCfg().isMailOrder();
        boolean telephoneFromPos = true; // default to allow
        boolean mailOrderFromPos = true; // default to allow
        boolean telephone;
        boolean mailOrder;

        if(trans.getTransType().equals(EngineManager.TransType.SALE_MOTO_AUTO) || trans.getTransType().equals(EngineManager.TransType.REFUND_MOTO_AUTO)
                || trans.getTransType().equals(EngineManager.TransType.PREAUTH_MOTO_AUTO)) {
            // use flags from POS, if provided
            telephoneFromPos = trans.getTransEvent().isTeleOrder();
            mailOrderFromPos = trans.getTransEvent().isMailOrder();
        }

        // for mode to be valid it has to be set in both config and set from POS
        telephone = telephoneFromCfg && telephoneFromPos;
        mailOrder = mailOrderFromCfg && mailOrderFromPos;

        cardInfo.setFaultyMsr(false);
        cardInfo.setMailOrder(false);
        cardInfo.setOverTelephone(false);
        cardInfo.setCardholderPresent(false);

        if (!telephone && !mailOrder) {
            // neither type allowed - probably config and POS command mismatch
            Timber.e( "Neither telephone nor mail order allowed in terminal configuration or in POS command - config and POS command mismatch" );
            result = "";
        } else if (telephone != mailOrder) {
            // only telephone or mail order set
            result = telephone ? TELEPHONE : MAIL_ORDER;
        } else {
            // else both mail AND telephone set, or none set at all. in both cases, prompt user to
            ArrayList<DisplayQuestion> options = new ArrayList<>();
            // TODO: Use getString here for the second arg instead of an hardcoded string
            options.add(new DisplayQuestion( String_id.STR_MAIL_ORDER, MAIL_ORDER, DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
            options.add(new DisplayQuestion(String_id.STR_TELEPHONE, TELEPHONE, DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
            options.add(new DisplayQuestion(String_id.STR_CANCEL, "CANCEL", DisplayQuestion.EButtonStyle.BTN_STYLE_TRANSPARENT));

            HashMap<String, Object> map = new HashMap<>();
            map.put(IUIDisplay.uiScreenOptionList, options);
            ui.showScreen(PLEASE_CHOOSE_ACCOUNT_TYPE, map);
            IUIDisplay.UIResultCode res = ui.getResultCode(ACT_QUESTION, IUIDisplay.LONG_TIMEOUT);
            if (res == OK) {
                result = ui.getResultText(ACT_QUESTION, IUIDisplay.uiResultText1);
            }
        }

        switch(result) {
            case MAIL_ORDER:
                cardInfo.setMailOrder(true);
                break;
            case TELEPHONE:
                cardInfo.setOverTelephone(true);
                break;
            case "FAULTY CARD":
                cardInfo.setFaultyMsr(true);
                cardInfo.setCardholderPresent(true);
                break;
            case "CANCEL":
                ui.showScreen(TRANS_CANCEL);
                ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                break;
            default:
                super.d.getProtocol().setInternalRejectReason( super.trans, IProto.RejectReasonType.TRANS_NOT_ALLOWED );
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                break;
        }
    }

    private void uiInputHouseNumber() {

        TCard cardInfo = trans.getCard();

        if (!cardInfo.isMailOrder() && !cardInfo.isOverTelephone())
            return;


        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiSkipButtonOn, true);

        ui.showInputScreen(ENTER_HOUSE_NO, map);
        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            cardInfo.setHouseNumber(ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1));
        } else {

            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }
    }

    private void  uiInputPostCode() {

        TCard cardInfo = trans.getCard();

        if (!cardInfo.isMailOrder() && !cardInfo.isOverTelephone())
            return;


        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiSkipButtonOn, true);

        ui.showInputScreen(ENTER_POST_CODE, map);
        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            cardInfo.setPostCodeNumber(ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1));
        } else {
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }
    }

}
