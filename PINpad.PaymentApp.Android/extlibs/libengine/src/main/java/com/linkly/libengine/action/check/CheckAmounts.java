package com.linkly.libengine.action.check;

import static com.linkly.libengine.users.User.Privileges.SUPERVISOR;
import static com.linkly.libengine.users.User.Privileges.USER;
import static com.linkly.libui.IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.UIScreenDef.AMOUNT_TOO_LARGE;
import static com.linkly.libui.UIScreenDef.AMOUNT_TOO_SMALL;
import static com.linkly.libui.UIScreenDef.CASHBACK_TOO_LARGE;
import static com.linkly.libui.UIScreenDef.PERMISSIONS_ERROR_SUPERVISOR_LOGIN;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TSurcharge;
import com.linkly.libengine.status.IStatus;
import com.linkly.libengine.users.User;
import com.linkly.libengine.users.UserManager;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;

import timber.log.Timber;

public class CheckAmounts extends IAction {

    private IProto.RejectReasonType internalRejectReason;

    @Override
    public String getName() {
        return "CheckAmounts";
    }

    @Override
    public void run() {
        calculateSurcharge();

        long saleAmount = trans.getAmounts().getAmount();
        TCard cardinfo = trans.getCard();
        long cashbackAmount = trans.getAmounts().getCashbackAmount();

        if (cardinfo.getCardIndex() == -1) {
            return;
        }

        long minValue = trans.getMinValueAllowedDollars(d.getPayCfg());
        double maxValue = trans.getMaxValueAllowedDollars(d.getPayCfg());
        double maxCashValue = trans.getMaxCashValueAllowedDollars(d.getPayCfg(), trans.isCash());

        // overriding max value from the offline config if the device is in offline mode
        if (trans.isStartedInOfflineMode()) {
            internalRejectReason = IProto.RejectReasonType.OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED;
            maxValue = (double) Math.max(d.getPayCfg().getOfflineTransactionCeilingLimitCentsContact(), d.getPayCfg().getOfflineTransactionCeilingLimitCentsContactless()) / 100;
        }

        int currencyMultiplier = TAmounts.getAmountCurExMul(d.getPayCfg().getCurrencyNum());
        minValue *= currencyMultiplier;
        maxValue *= currencyMultiplier;
        maxCashValue *= currencyMultiplier;

        if (maxValue <= 0) {
            maxValue = 2000000000;
        }

        if (minValue <= 0) {
            minValue = 1;
        }

        if (maxValue < minValue) {
            maxValue = minValue;
        }

        if (maxCashValue <= 0)
            maxCashValue = (double) d.getPayCfg().getCashoutLimitCents() / 100;

        Timber.i("Amount:" + saleAmount + " Normal Amount:" + trans.getAmounts().getAmount());
        Timber.i("MIN: " + minValue + " MAX: " + maxValue + " CASHMAX: " + maxCashValue);

        String saleFormattedAmount = curr.formatUIAmount((saleAmount < minValue) ? String.valueOf(minValue) : String.valueOf(maxValue), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        String cashFormattedAmount = curr.formatUIAmount((cashbackAmount < minValue) ? String.valueOf(minValue) : String.valueOf(maxValue), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        if ((trans.isSale() || trans.isPreAuth()) && (saleAmount < minValue || saleAmount > maxValue)) {

            if (saleAmount < minValue) {
                handleInvalidAmount(String.valueOf(saleFormattedAmount), IStatus.STATUS_EVENT.STATUS_ERR_AMOUNT_LOW, AMOUNT_TOO_SMALL);
            } else {
                handleInvalidAmount(String.valueOf(saleFormattedAmount), IStatus.STATUS_EVENT.STATUS_ERR_AMOUNT_HIGH, AMOUNT_TOO_LARGE);
            }

            return;
        } else if (trans.isCashback() || trans.isCash()) {
            if (trans.isCashback()) {
                if (saleAmount < minValue) {
                    handleInvalidAmount(String.valueOf(saleFormattedAmount), IStatus.STATUS_EVENT.STATUS_ERR_AMOUNT_LOW, AMOUNT_TOO_SMALL);
                    return;
                }
                if (saleAmount > maxValue) {
                    handleInvalidAmount(String.valueOf(saleFormattedAmount), IStatus.STATUS_EVENT.STATUS_ERR_AMOUNT_HIGH, AMOUNT_TOO_LARGE);
                    return;
                }
                if (cashbackAmount > maxCashValue) {
                    handleInvalidAmount(String.valueOf(cashFormattedAmount), IStatus.STATUS_EVENT.STATUS_ERR_CASHBACK_TOO_HIGH, CASHBACK_TOO_LARGE);
                    return;
                }
            } else if (trans.isCash() && (cashbackAmount > maxCashValue)) {
                handleInvalidAmount(String.valueOf(cashFormattedAmount), IStatus.STATUS_EVENT.STATUS_ERR_CASHBACK_TOO_HIGH, CASHBACK_TOO_LARGE);
                return;
            }
        }

        int userLimit = trans.getCard().getCardsConfig(d.getPayCfg()).getLimits().getMangerAuthMax() * TAmounts.getAmountCurExMul(d.getPayCfg().getCurrencyNum());
        User.Privileges privileges = UserManager.getActiveUser().getPrivileges();
        if ((userLimit > 0) && (userLimit < saleAmount) && (privileges == USER) && (d.getUsrMgr().getUpgradedUser() == null)) {
            User newUser = UserManager.upgradeUserLevel(SUPERVISOR, ui);
            if (newUser == null) {
                ui.showScreen(PERMISSIONS_ERROR_SUPERVISOR_LOGIN);
                ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            }

        }
    }

    protected void handleInvalidAmount(String amount, IStatus.STATUS_EVENT statusError, UIScreenDef scrdef) {
        d.getStatusReporter().reportStatusEvent(statusError , trans.isSuppressPosDialog());
        ui.showScreen(scrdef, amount);
        ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
        if (internalRejectReason != null) {
            d.getProtocol().setInternalRejectReason(trans, internalRejectReason);
        }
        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
    }

    private void calculateSurcharge() {
        switch (trans.getCard().getCaptureMethod()) {
            case MANUAL:
            case ICC_FALLBACK_KEYED:
            case SWIPED:
            case ICC_FALLBACK_SWIPED:
                trans.getAmounts().setSurcharge(0);
                TSurcharge.surchargeForSwiped(trans, d.getPayCfg());
                break;
            case ICC_OFFLINE:
            case ICC:
                trans.getAmounts().setSurcharge(0);
                TSurcharge.surchargeForICC(trans, d);
                break;
            case CTLS:
            case CTLS_MSR:
                // Contactless Surcharge must be calculated already and provided to Ctls module, see TSurcharge.calculateSurchargeForCtls()
                break;
            case NOT_CAPTURED:
            case SCAN_VOUCHER:
            case RRN_ENTERED:
            default:
                trans.getAmounts().setSurcharge(0);
                // surcharge not supported for these capture methods
                // Don't throw an error
                Timber.e("Surcharge not supported for this capture type [%s]",
                        trans.getCard().getCaptureMethod());
                break;

        }
    }

}
