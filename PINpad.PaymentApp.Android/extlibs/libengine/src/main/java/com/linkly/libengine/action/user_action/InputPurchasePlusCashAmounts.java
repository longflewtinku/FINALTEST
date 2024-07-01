package com.linkly.libengine.action.user_action;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT_AMOUNT;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.CASHBACK_TOO_LARGE;
import static com.linkly.libui.UIScreenDef.ENTER_AMOUNT;
import static com.linkly.libui.UIScreenDef.TOTAL_AMOUNT_TOO_LARGE;

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

public class InputPurchasePlusCashAmounts extends IAction {

    private IUIDisplay.UIResultCode getAmount( IUIDisplay.String_id displayId ) {
        HashMap<String, Object> map = new HashMap<>();
        PayCfg paycfg = d.getPayCfg();
        CountryCode cCode = ISOCountryCodes.getInstance().getCountryFrom3Num(paycfg.getCurrencyNum() + "");
        map.put(IUIDisplay.uiScreenCurrency, cCode.getAlphaCode());
        map.put(IUIDisplay.uiTitleId, displayId);

        // NOTE: Either override the map titleId or add an extra arg here
        ui.showInputScreen(ENTER_AMOUNT, map );
        return ui.getResultCode(ACT_INPUT_AMOUNT, IUIDisplay.LONG_TIMEOUT);
    }

    @Override
    public String getName() {
        return "InputPurchasePlusCashAmounts";
    }

    @SuppressWarnings("java:S3776")// Cognitive complexity(17)
    @Override
    public void run() {

        mal.getHardware().enablePowerKey(false);
        TAmounts amounts = trans.getAmounts();

        // Skip amount input, if the amount is already processed and its a POS triggered transaction.
        // However, for standalone the payment app only might have processed the purchase amount and cash amount is yet to be processed
        if (amounts.getAmount() > 0 && trans.getTransType().autoTransaction) {
            Timber.i("Amount already entered");
        } else {
            String txtAmount = null;

            // skip amount input for purchase, if its already processed in standalone mode
            if (amounts.getAmount() <= 0) {
                do {
                    // get sale amount
                    IUIDisplay.UIResultCode uiRes = getAmount(IUIDisplay.String_id.STR_SALE);
                    if (uiRes != OK) {
                        // user cancel
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                        return;
                    }

                    txtAmount = ui.getResultText(ACT_INPUT_AMOUNT, IUIDisplay.uiResultText1);
                    amounts.setAmountUserEntered(txtAmount);
                    amounts.setAmount(Long.parseLong(txtAmount));

                    // repeat if amount not valid
                } while (!uiCheckSumAmount(trans, false));
            }

            // skip cash amount input for purchase + cashout, if its already processed in standalone mode
            if (amounts.getCashbackAmount() <= 0) {
                do {
                    // enter cash amount
                    IUIDisplay.UIResultCode uiRes = getAmount(IUIDisplay.String_id.STR_CASH);
                    if (uiRes != OK) {
                        // user cancel
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                        return;
                    }

                    txtAmount = ui.getResultText(ACT_INPUT_AMOUNT, IUIDisplay.uiResultText1);

                    // repeat if cashback amt exceeds limits
                } while (!uiCheckCashbackAmount(trans, Long.parseLong(txtAmount)));
            }

            // if we get here, we didn't cancel and amount was fine. save/update total
            // Amount could be already entered in case of state reentry as part of card fallback
            if(txtAmount != null)
                amounts.setCashbackAmount(Long.parseLong(txtAmount));

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

    private boolean uiCheckCashbackAmount(TransRec trans, long cashBackAmount) {

        long amount = cashBackAmount;
        double totalAllowed = (trans.getMaxCashValueAllowedDollars(d.getPayCfg(),false) * TAmounts.getAmountCurExMul(d.getPayCfg().getCurrencyNum()));

        if (amount > totalAllowed) {
            ui.showScreen(CASHBACK_TOO_LARGE);
            ui.getResultCode(ACT_INFORMATION, IUIDisplay.INFO_TIMEOUT);
            return false;
        }
        return true;
    }
}
