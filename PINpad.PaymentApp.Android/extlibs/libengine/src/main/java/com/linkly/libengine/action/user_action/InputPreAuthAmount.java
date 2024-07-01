package com.linkly.libengine.action.user_action;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT_AMOUNT;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.ENTER_TOTAL_PREAUTH_AMT;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.currency.CountryCode;
import com.linkly.libui.currency.ISOCountryCodes;

import java.util.HashMap;

import timber.log.Timber;

public class InputPreAuthAmount extends IAction {
    @Override
    public String getName() {
        return "InputPreAuthAmount";
    }

    @Override
    public void run() {

        this.uiInputPreAuthAmount();
    }

    private void uiInputPreAuthAmount() {
        TAmounts amounts = trans.getAmounts();

        if (amounts.getPreAuthedAmount() > 0) {
            Timber.i( "Amount already entered");
        } else {

            HashMap<String, Object> map = new HashMap<>();

            PayCfg paycfg = d.getPayCfg();
            CountryCode cCode = ISOCountryCodes.getInstance().getCountryFrom3Num(paycfg.getCurrencyNum() + "");
            map.put(IUIDisplay.uiScreenCurrency, cCode.getAlphaCode());

            ui.showInputScreen(ENTER_TOTAL_PREAUTH_AMT, map);
            IUIDisplay.UIResultCode uiRes = ui.getResultCode(ACT_INPUT_AMOUNT, IUIDisplay.LONG_TIMEOUT);

            if (uiRes == OK) {
                amounts.setPreAuthedAmount(Integer.valueOf(ui.getResultText(ACT_INPUT_AMOUNT, IUIDisplay.uiResultText1)));
            } else {
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            }
        }
    }
}
