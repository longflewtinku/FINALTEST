package com.linkly.libengine.action.user_action;

import static com.linkly.libui.IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT_AMOUNT;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_PICK_GRATUITY;
import static com.linkly.libui.IUIDisplay.String_id.STR_ACCEPT;
import static com.linkly.libui.IUIDisplay.String_id.STR_BACK;
import static com.linkly.libui.IUIDisplay.String_id.STR_CANCEL;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASHBACK;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASHBACK_ONLY;
import static com.linkly.libui.IUIDisplay.String_id.STR_ENTER_TIP;
import static com.linkly.libui.IUIDisplay.String_id.STR_FIFTEEN_PERCENT;
import static com.linkly.libui.IUIDisplay.String_id.STR_FIVE_PERCENT;
import static com.linkly.libui.IUIDisplay.String_id.STR_GRATUITY;
import static com.linkly.libui.IUIDisplay.String_id.STR_NONE;
import static com.linkly.libui.IUIDisplay.String_id.STR_OTHER;
import static com.linkly.libui.IUIDisplay.String_id.STR_SALE;
import static com.linkly.libui.IUIDisplay.String_id.STR_TEN_PERCENT;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTAL;
import static com.linkly.libui.IUIDisplay.String_id.STR_TWENTY_PERCENT;
import static com.linkly.libui.IUIDisplay.UIResultCode.ABORT;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.CASHBACK_TOO_LARGE;
import static com.linkly.libui.UIScreenDef.DO_ADD_GRATUITY;
import static com.linkly.libui.UIScreenDef.ENTER_TIP_SALE_VAR;
import static com.linkly.libui.UIScreenDef.ENTER_TOTAL_SALE_VAR;
import static com.linkly.libui.UIScreenDef.GRATUITY_SCREEN;
import static com.linkly.libui.UIScreenDef.INPUT_TOO_LOW_TRY_AGAIN;
import static com.linkly.libui.UIScreenDef.PLEASE_CHOOSE_SALE;
import static com.linkly.libui.UIScreenDef.SALE_GRATUITY_CASHBACK_TOTAL_CORRECT_SCREEN;
import static com.linkly.libui.UIScreenDef.SALE_GRATUITY_TOTAL_CORRECT_SCREEN;
import static com.linkly.libui.UIScreenDef.TOTAL_AMOUNT_TOO_LARGE;
import static com.linkly.libui.UIScreenDef.VAR_AMT_NL_VAR_NL_EXCEEDS_LIMITS_VAR;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_DEFAULT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_LEFT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_RIGHT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_TRANSPARENT;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libengine.status.IStatus;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.wrappers.TagDataFromPOS;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.currency.CountryCode;
import com.linkly.libui.currency.ISOCountryCodes;
import com.linkly.libui.display.DisplayFragmentOption;
import com.linkly.libui.display.DisplayQuestion;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class InputTip extends IAction {

    @Override
    public String getName() {
        return "InputTip";
    }

    @Override
    public void run() {

        int tipType = 0;
        boolean running = true;



        if (trans.getAmounts().isTipEntered()) {
            Timber.i("Tip already entered");
            return;
        }

        // not sure why these two values just keeping logic from the old app to retain compatibility
        if (!d.getPayCfg().isTipAllowed()) {
            return;
        }

        if (!(trans.isSale() || trans.isCashback()) ||
                (trans.getTransEvent() != null && !trans.getTransEvent().isTipRequired() && ((trans.getTransType() == EngineManager.TransType.SALE_AUTO) ||
                        (trans.getTransType() == EngineManager.TransType.SALE_MOTO_AUTO) ||
                        trans.getTransType() == EngineManager.TransType.CASHBACK_AUTO)))// if the event tells us not to have a tip
            return;

        //Check Tagdata for Tip if it is present, don't display input Tip screen.
        TagDataFromPOS tagData = d.getCurrentTransaction().getTagDataFromPos();
        if (tagData != null) {
            if(tagData.getTIP() != null) {
                trans.getAmounts().setTip(Long.parseLong(tagData.getTIP()));
                trans.getAmounts().setTipEntered(true);
                if (!uiCheckSumAmount(trans, true)) {
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                }
                return;
            }

            if(tagData.getTPO() != null) {
                tipType = TIP_TYPE_PERCENTAGE;
            }
        }

        trans.getAmounts().setTipEntered(true);

        boolean askGratuityQuestion = true;

        // Notify our pos with UI prompt to display the user is entering a tip
        d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_UI_ENTER_TIP , trans.isSuppressPosDialog());

        IUIDisplay.String_id overrideTitle = null;
        if(trans.isCashback())  {
            overrideTitle = STR_CASHBACK;
        }

        while (running) {
            //Check if percentage is forced
            if (d.getPayCfg().isUsePercentageTip()) {
                tipType = TIP_TYPE_PERCENTAGE;
            }

            if (askGratuityQuestion) {
                UIHelpers.YNQuestion res = UIHelpers.uiYesNoCancelQuestion(d, DO_ADD_GRATUITY, overrideTitle);
                if (res == UIHelpers.YNQuestion.NO) {
                    return;
                } else if (res == UIHelpers.YNQuestion.CANCEL) {
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    return;
                }
                askGratuityQuestion = false;
            }

            boolean runChecks = true;
            boolean loopInner = true;
            int tipTypeResult = 0; // Annoyingly we need to record the data for an abort via POS case

            while (loopInner) {
                //Get force Tip Type
                if (tipType == 0) {
                    tipType = uiInputTipType();
                }

                switch (tipType) {
                    case TIP_TYPE_TOTAL:
                        tipTypeResult = uiInputTotalAmount(trans);
                        if (tipTypeResult != TIP_TYPE_CANCEL) {
                            loopInner = false;
                        }
                        break;
                    case TIP_TYPE_TIP:
                        tipTypeResult = uiInputTipAmount(trans);
                        if (tipTypeResult != TIP_TYPE_CANCEL) {
                            loopInner = false;
                        }
                        break;
                    case TIP_TYPE_PERCENTAGE:
                        tipTypeResult = uiInputTipPercentage(trans);
                        if (tipTypeResult == TIP_TYPE_CANCEL) {
                            runChecks = false;
                        }
                        loopInner = false;
                        break;
                    default:
                        loopInner = false;
                        running = false;
                }
            }

            if(tipTypeResult == TIP_TYPE_ABORT) {
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                running = false;
            } else if (running) {
                if (runChecks) {
                    UIHelpers.YNQuestion sum = uiCheckTipAmount(trans, tipType);

                    // check the values entered
                    if (!uiCheckCeilingLimit(trans)) {
                        Timber.i("Ceiling limit check failed");
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                        running = false;
                    } else if (sum == UIHelpers.YNQuestion.NO) {
                        Timber.i("Check tip amounts failed");
                    } else if (sum == UIHelpers.YNQuestion.CANCEL) {
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                        running = false;
                    } else if (!uiCheckSumAmount(trans, true)) {
                        Timber.i("Check sum amounts failed");
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                        running = false;
                    } else if (!uiCheckCashbackAmount(trans, trans.isCashback())) {
                        Timber.i("Check cashback amounts failed");
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                        running = false;
                    } else {
                        Timber.i("All the checks have passed");
                        running = false;
                    }
                } else {
                    askGratuityQuestion = true;
                }
            } else {
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            }
        }
    }

    private boolean uiCheckSumAmount(TransRec trans, boolean includeTip) {

        TAmounts amounts = trans.getAmounts();

        long amount = includeTip ? amounts.getBaseAmount() : amounts.getTotalAmountWithoutTip();
        double totalAllowed = (trans.getMaxValueAllowedDollars(d.getPayCfg()) * TAmounts.getAmountCurExMul(d.getPayCfg().getCurrencyNum()));

        if (amount > totalAllowed) {
            ui.showScreen(TOTAL_AMOUNT_TOO_LARGE);
            ui.getResultCode(ACT_INFORMATION, IUIDisplay.INFO_TIMEOUT);
            return false;
        }
        return true;
    }

    private boolean uiCheckCashbackAmount(TransRec trans,boolean isCashBack) {

        if(isCashBack){
            long amount = trans.getAmounts().getCashbackAmount();
            double totalAllowed = (trans.getMaxCashValueAllowed(d.getPayCfg(),false) * TAmounts.getAmountCurExMul(d.getPayCfg().getCurrencyNum()));

            if(amount > totalAllowed) {
                ui.showScreen(CASHBACK_TOO_LARGE);
                ui.getResultCode(ACT_INFORMATION,IUIDisplay.INFO_TIMEOUT);
                return false;
            }
        } else { Timber.i("Cashback check not required"); }
        return true;
    }

    private boolean uiCheckCeilingLimit(TransRec trans) {

        TAmounts amounts = trans.getAmounts();

        // check the values entered
        long total = amounts.getBaseAmount();
        // MW: Deliberately casting from double to a long so as to not break the logic here
        double max = trans.getMaxValueAllowedDollars(d.getPayCfg()) * TAmounts.getAmountCurExMul(d.getPayCfg().getCurrencyNum());

        String totalFormatted = curr.formatUIAmount(String.valueOf(total), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        String maxFormatted = curr.formatUIAmount(String.valueOf(max), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        if (total > max && max > 0) {
            ui.showScreen(VAR_AMT_NL_VAR_NL_EXCEEDS_LIMITS_VAR, trans.getTransType().displayId, totalFormatted, maxFormatted, d.getPrompt(STR_TOTAL));
            ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, IUIDisplay.INFO_TIMEOUT);
            amounts.setTip(Long.parseLong(ui.getResultText(ACT_INPUT_AMOUNT, IUIDisplay.uiResultText1))); //Reset the tip if total amount is greater than ceiling amount
            return false;
        }
        return true;
    }

    private int uiInputTotalAmount(TransRec trans) {

        while (true) {
            HashMap<String, Object> map = new HashMap<>();

            PayCfg paycfg = d.getPayCfg();
            CountryCode cCode = ISOCountryCodes.getInstance().getCountryFrom3Num(paycfg.getCurrencyNum() + "");
            map.put(IUIDisplay.uiScreenCurrency, cCode.getAlphaCode());
            ui.showScreen(ENTER_TOTAL_SALE_VAR, map, d.getFramework().getCurrency().formatUIAmount(String.valueOf(trans.getAmounts().getAmount()), IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()));
            IUIDisplay.UIResultCode uiRes = ui.getResultCode(ACT_INPUT_AMOUNT, 60000);

            if (uiRes == OK) {
                long total = Long.parseLong(ui.getResultText(ACT_INPUT_AMOUNT, IUIDisplay.uiResultText1));
                if (total < trans.getAmounts().getAmount()) {
                    ui.showScreen(INPUT_TOO_LOW_TRY_AGAIN);
                    ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                } else {
                    trans.getAmounts().setTip(total - trans.getAmounts().getAmount());
                    return TIP_TYPE_TOTAL;
                }
            } else if(uiRes == ABORT) {
                return TIP_TYPE_ABORT;
            } else {
                return TIP_TYPE_CANCEL;
            }
        }
    }

    private UIHelpers.YNQuestion uiCheckTipAmount(TransRec trans, int tipType) {
        long tipMax = 0;
        TAmounts amounts = trans.getAmounts();

        // Max Tip percentage from POS outweighs value from config
        TagDataFromPOS tagData = trans.getTagDataFromPos();
        if (tagData != null && tagData.getTPO() != null) {
            for (int percentFromPOS : tagData.getTPO()) {
                if (percentFromPOS > 0)
                    tipMax = Math.max(tipMax, percentFromPOS);
            }
        }

        if (tipMax == 0) {
            String configMaxTipPercent = d.getPayCfg().getMaxTipPercent();
            tipMax = (!Util.isNullOrEmpty(configMaxTipPercent) &&
                Util.isNumericString(configMaxTipPercent)) ?
                Long.parseLong(configMaxTipPercent) :
                0;
        }

        tipMax = (amounts.getAmount() * tipMax) / 100;
        long tipMaxDisplayed = tipMax;
        if (tipType == TIP_TYPE_TOTAL) {
            tipMaxDisplayed += amounts.getAmount();
        }

        String maxFormatted = curr.formatUIAmount(String.valueOf(tipMaxDisplayed), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        String tipFormatted = curr.formatUIAmount(String.valueOf(amounts.getTip()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        String totalFormatted = curr.formatUIAmount(String.valueOf(amounts.getTotalAmount()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        String amountFormatted = curr.formatUIAmount(String.valueOf(amounts.getAmount()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        String cashbackFormatted = curr.formatUIAmount(String.valueOf(amounts.getCashbackAmount()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());

        UIHelpers.YNQuestion result = UIHelpers.YNQuestion.YES;
        if (tipMax >= 0 && amounts.getTip() > tipMax) {
            // entered amount exceeds max allowed tip
            ui.showScreen(VAR_AMT_NL_VAR_NL_EXCEEDS_LIMITS_VAR, trans.getTransType().displayId, tipFormatted, maxFormatted, d.getPrompt(STR_GRATUITY));
            ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, IUIDisplay.INFO_TIMEOUT);
            amounts.setTip(tipMax);
            result = UIHelpers.YNQuestion.NO;
        } else if (tipType != TIP_TYPE_PERCENTAGE) {
            // confirm the tip amount
            // TODO maybe we don't need a summary when cashback is entered too, or may get it twice etc when tip and cashback
            if (amounts.getCashbackAmount() > 0) {
                result = UIHelpers.uiYesNoCancelQuestion(d, SALE_GRATUITY_CASHBACK_TOTAL_CORRECT_SCREEN, null,
                        amountFormatted,tipFormatted,cashbackFormatted,totalFormatted);
            }
            else{
                result = UIHelpers.uiYesNoCancelQuestion(d, SALE_GRATUITY_TOTAL_CORRECT_SCREEN, null,
                        amountFormatted,tipFormatted,totalFormatted);
            }

            if (result == UIHelpers.YNQuestion.CANCEL) {
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            }
        }
        return result;
    }

    private int uiInputTipPercentage(TransRec trans) {

        while (true) {
            HashMap<String, Object> map = new HashMap<>();

            TAmounts amounts = trans.getAmounts();
            String tipFormatted = curr.formatUIAmount(String.valueOf(amounts.getTip()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
            String totalFormatted = curr.formatUIAmount(String.valueOf(amounts.getTotalAmount()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
            String amountFormatted = curr.formatUIAmount(String.valueOf(amounts.getAmount()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
            String cashbackFormatted = curr.formatUIAmount(String.valueOf(amounts.getCashbackAmount()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());

            ArrayList<DisplayFragmentOption> fragOptions = new ArrayList<>();

            fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_SALE) + ":", amountFormatted));
            fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_GRATUITY) + ":", tipFormatted));
            if (amounts.getCashbackAmount() > 0) {
                fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_CASHBACK_ONLY) + ":", cashbackFormatted));
            }

            fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_TOTAL) + ":", totalFormatted));
            map.put(IUIDisplay.uiScreenFragType, IUIDisplay.FRAG_TYPE.FRAG_GRID);

            map.put(IUIDisplay.uiScreenFragOptionList, fragOptions);

            map.put(IUIDisplay.uiKeepOnScreen, true);
            ArrayList<DisplayQuestion> options = new ArrayList<>();

            options.add(new DisplayQuestion(STR_NONE, "0", BTN_STYLE_LEFT));
            options.add(new DisplayQuestion(STR_OTHER, "Other", BTN_STYLE_RIGHT));

            TagDataFromPOS tagData = trans.getTagDataFromPos();
            if (tagData != null && tagData.getTPO() != null) {
                DisplayQuestion.EButtonStyle buttonStyle = BTN_STYLE_LEFT;
                for (int percent : tagData.getTPO()) {
                    if (percent > 0) {
                        String percentToString = Integer.toString(percent);
                        options.add(new DisplayQuestion(percentToString + "%", percentToString, buttonStyle));
                        buttonStyle = (buttonStyle == BTN_STYLE_LEFT) ? BTN_STYLE_RIGHT : BTN_STYLE_LEFT; // Toggle between left and right
                    } else {
                        Timber.e("Warning invalid percent value passed %d", percent);
                    }
                }
            } else {
                options.add(new DisplayQuestion(STR_FIVE_PERCENT, "5", BTN_STYLE_LEFT));
                options.add(new DisplayQuestion(STR_TEN_PERCENT, "10", BTN_STYLE_RIGHT));
                options.add(new DisplayQuestion(STR_FIFTEEN_PERCENT, "15", BTN_STYLE_LEFT));
                options.add(new DisplayQuestion(STR_TWENTY_PERCENT, "20", BTN_STYLE_RIGHT));
            }
            options.add(new DisplayQuestion(STR_BACK, "Back", BTN_STYLE_TRANSPARENT));
            options.add(new DisplayQuestion(STR_ACCEPT, "Accept", BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
            map.put(IUIDisplay.uiScreenOptionList, options);

            ui.showScreen(GRATUITY_SCREEN, map);
            IUIDisplay.UIResultCode uiRes = ui.getResultCode(ACT_PICK_GRATUITY, IUIDisplay.LONG_TIMEOUT);
            if ( uiRes == IUIDisplay.UIResultCode.OK) {
                String result = ui.getResultText(ACT_PICK_GRATUITY, IUIDisplay.uiResultText1);
                switch (result.toLowerCase()) {
                    case "other":
                        return uiInputTipAmount(trans);
                    case "0":
                        trans.getAmounts().setTip(0);
                        return TIP_TYPE_TIP;
                    case "back":
                        trans.getAmounts().setTip(0);
                        return TIP_TYPE_CANCEL;
                    case "accept":
                        Timber.e("Tip entered : %s", curr.formatUIAmount(String.valueOf(trans.getAmounts().getTip()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()));
                        return TIP_TYPE_TIP;
                }

                UIHelpers.YNQuestion sum = uiCheckTipAmount(trans, TIP_TYPE_PERCENTAGE);

                if (!uiCheckCeilingLimit(trans)) {
                    Timber.i( "Ceiling limit check failed");
                    return TIP_TYPE_ABORT;
                } else if (sum == UIHelpers.YNQuestion.NO) {
                    Timber.i( "Check tip amounts failed");
                } else if (sum == UIHelpers.YNQuestion.CANCEL) {
                    trans.getAmounts().setTip(0);
                    return TIP_TYPE_CANCEL;
                } else if (!uiCheckSumAmount(trans, true)) {
                    Timber.i( "Check sum amounts failed");
                    return TIP_TYPE_ABORT;
                } else if (!uiCheckCashbackAmount(trans, trans.isCashback())) {
                    Timber.i("Check cashback amounts failed");
                    return TIP_TYPE_ABORT;
                }else {
                    Timber.i( "All the checks have passed");
                    long percentTip = Long.parseLong(result);
                    trans.getAmounts().setTip(((trans.getAmounts().getAmount() * percentTip) / 100));
                }
            } else if(uiRes == ABORT) {
                return TIP_TYPE_ABORT;
            } else {
                return TIP_TYPE_CANCEL;
            }
        }
    }

    private int uiInputTipType() {
        ArrayList<DisplayQuestion> options = new ArrayList<>();
        options.add(new DisplayQuestion(STR_ENTER_TIP, "TIP", BTN_STYLE_DEFAULT));
        options.add(new DisplayQuestion(STR_TOTAL, "TOTAL", BTN_STYLE_DEFAULT));
        options.add(new DisplayQuestion(STR_CANCEL, "CANCEL", BTN_STYLE_TRANSPARENT));

        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiScreenOptionList, options);
        ui.showScreen(PLEASE_CHOOSE_SALE, map);
        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_PICK_GRATUITY, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            String result = ui.getResultText(ACT_PICK_GRATUITY, IUIDisplay.uiResultText1);
            switch (result) {
                case "TIP":
                    return TIP_TYPE_TIP;
                case "CANCEL":
                    return TIP_TYPE_CANCEL;
                default:
                    return TIP_TYPE_TOTAL;
            }
        }
        return TIP_TYPE_CANCEL;
    }

    private int uiInputTipAmount(TransRec trans) {

        HashMap<String, Object> map = new HashMap<>();

        PayCfg paycfg = d.getPayCfg();
        CountryCode cCode = ISOCountryCodes.getInstance().getCountryFrom3Num(paycfg.getCurrencyNum() + "");
        map.put(IUIDisplay.uiScreenCurrency, cCode.getAlphaCode());

        ui.showScreen(ENTER_TIP_SALE_VAR, map, curr.formatUIAmount(String.valueOf(trans.getAmounts().getAmount()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()));
        IUIDisplay.UIResultCode uiRes = ui.getResultCode(ACT_INPUT_AMOUNT, 60000);

        if (uiRes == OK) {
            String txtAmount = ui.getResultText(ACT_INPUT_AMOUNT, IUIDisplay.uiResultText1);
            trans.getAmounts().setTip(Long.valueOf(txtAmount));
            Timber.e("Tip entered : %s", curr.formatUIAmount(String.valueOf(trans.getAmounts().getTip()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()));
            return TIP_TYPE_TIP;
        } else if(uiRes == ABORT) {
            return TIP_TYPE_ABORT;
        }
        return TIP_TYPE_CANCEL;

    }

    private static final int REFERENCE_LEN = 20;
    private static final int CARD_EASE_REFERENCE_LEN = 36;
    private static final int TIP_TYPE_TIP = 1;
    private static final int TIP_TYPE_TOTAL = 2;
    private static final int TIP_TYPE_CANCEL = 3;
    private static final int TIP_TYPE_PERCENTAGE = 4;
    private static final int TIP_TYPE_ABORT = 5;
}
