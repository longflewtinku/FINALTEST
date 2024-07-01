package com.linkly.libengine.action.user_action;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT_AMOUNT;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.ENTER_AMOUNT;
import static com.linkly.libui.UIScreenDef.TOTAL_AMOUNT_TOO_LARGE;

import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.currency.CountryCode;
import com.linkly.libui.currency.ISOCountryCodes;

import java.util.HashMap;

import timber.log.Timber;

public class InputAmount extends IAction {
    @Override
    public String getName() {
        return "InputAmount";
    }

    @Override
    public void run() {

        mal.getHardware().enablePowerKey(false);
        TAmounts amounts = trans.getAmounts();

        if (amounts.getAmount() > 0 || amounts.getCashbackAmount() > 0) {
            Timber.i("Amount already entered");
        } else {

            while (true) {
                HashMap<String, Object> map = new HashMap<>();
                PayCfg paycfg = d.getPayCfg();
                CountryCode cCode = ISOCountryCodes.getInstance().getCountryFrom3Num(paycfg.getCurrencyNum() + "");
                map.put(IUIDisplay.uiScreenCurrency, cCode.getAlphaCode());
                map.put(IUIDisplay.uiTitleId, trans.getTransType().displayId);

                // NOTE: Either override the map titleId or add an extra arg here
                ui.showInputScreen(ENTER_AMOUNT, map );
                IUIDisplay.UIResultCode uiRes = ui.getResultCode( ACT_INPUT_AMOUNT,
                        super.d.getPayCfg().
                                getUiConfigTimeouts().
                                getTimeoutMilliSecs( ConfigTimeouts.AMOUNT_ENTRY_TIMEOUT,
                                false ) );

                if (uiRes == OK) {
                    String txtAmount = ui.getResultText(ACT_INPUT_AMOUNT, IUIDisplay.uiResultText1);

                    amounts.setAmountUserEntered(txtAmount);

                    // check we're setting correct amount depending on trans type. if cash only (aka withdrawl), i.e. NOT purchase with cashback, then save trans amt in cashback only
                    if( trans.isCash() ) {
                        amounts.setCashbackAmount(Long.parseLong(txtAmount));
                    } else {
                        amounts.setAmount(Long.parseLong(txtAmount));
                    }

                    if (uiCheckSumAmount(trans, false))
                        break;
                } else {
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    break;
                }

            }

        }
    }

    private boolean uiCheckSumAmount(TransRec trans, boolean includeTip) {

        TAmounts amounts = trans.getAmounts();

        long amount = includeTip ? amounts.getTotalAmount() : amounts.getTotalAmountWithoutTip();
        double totalAllowed = (trans.getMaxValueAllowedDollars(d.getPayCfg()) * TAmounts.getAmountCurExMul(d.getPayCfg().getCurrencyNum()));

        // refunds have their own CheckRefundLimits() action
        if( !trans.isRefund() && (amount > totalAllowed)) {
            ui.showScreen(TOTAL_AMOUNT_TOO_LARGE);
            ui.getResultCode(ACT_INFORMATION, IUIDisplay.INFO_TIMEOUT);
            return false;
        }
        return true;
    }
}
