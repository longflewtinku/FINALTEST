package com.linkly.libengine.action;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.LINK_DOWN_EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.CONNECT_FAILED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.ISSUER_UNAVAILABLE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.NO_RESPONSE;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.ENTER_EFB_AUTH_NUMBER;

import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libui.IUIDisplay;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import timber.log.Timber;

public class CheckEFB extends IAction {
    public enum CheckEFBMode{CHECK_EFB_BEFORE_ONLINE_PROCESSING, CHECK_EFB_AFTER_ONLINE_PROCESSING }
    private CheckEFBMode currentMode;
    public CheckEFB(CheckEFBMode mode) {
        currentMode = mode;
    }
    @Override
    public String getName() {
        return "CheckEFB";
    }

    @Override
    public void run() { checkEFB(); }

    private void checkEFB() {
        TProtocol protocol = trans.getProtocol();
        PayCfg payCfg = d.getPayCfg();
        if (payCfg == null) {
            return;
        }

        if (currentMode == CheckEFBMode.CHECK_EFB_BEFORE_ONLINE_PROCESSING) {
            // Check if terminal is in "Continue in Fallback Timer" mode.
            if (protocol.getEfbContinueInFallbackTimeStamp() + payCfg.getEfbContinueInFallbackTimeoutMinutes() * 60 * 1000 < Calendar.getInstance().getTimeInMillis()) {
                return; // no "Continue in Fallback" timer set or expired timer, skip
            }

            if (efbProcessing()) {
                Timber.i("Continue in Fallback mode. EFB checks passed, can approve offline");
                trans.getProtocol().setCanAuthOffline(true);        // do not attempt to go online
                trans.getProtocol().setAuthMethod(OFFLINE_EFB_AUTHORISED);
            }
        } else {
            if (trans.getProtocol().getAuthMethod() == OFFLINE_EFB_AUTHORISED) {
                // authorised already in "Continue in Fallback" mode,
                return;
            }
            if (efbProcessing()) {
                Timber.i("EFB checks passed, approve offline");
                trans.getProtocol().setAuthMethod(trans.getProtocol().getHostResult() == ISSUER_UNAVAILABLE ? LINK_DOWN_EFB_AUTHORISED : EFB_AUTHORISED);
            }
        }
    }

    // Suppressing complexity warning
    @SuppressWarnings("java:S3776")
    public boolean efbProcessing() {
        TProtocol protocol = trans.getProtocol();
        PayCfg payCfg = d.getPayCfg();
        // some checks are different depending what mode EFB check for
        if (currentMode == CheckEFBMode.CHECK_EFB_AFTER_ONLINE_PROCESSING &&
                (protocol.getHostResult() != CONNECT_FAILED && protocol.getHostResult() != NO_RESPONSE && protocol.getHostResult() != ISSUER_UNAVAILABLE)) { // Check Host result
            return false;
        }

        // Check EFB is supported in config
        if (payCfg == null) {
            return false;
        }
        if (!payCfg.isEfbSupported()) {
            return false;
        }
        // if need to check Service code
        if (payCfg.isEfbAcknowledgeServiceCode()) {
            // Check Service Code
            String serviceCode = trans.getCard().getServiceCode();
            if (serviceCode == null || serviceCode.length() < 3 || serviceCode.charAt(1) == '2') {
                Timber.i("EFB not available, Service code check fail");
                return false;
            }
        }

        if (!efbCheckExpiryDate(payCfg.getEfbPlasticCardLifeDays())) {
            Timber.i("EFB not available, Card Expiry checks fail");
            return false;
        }

        if (!trans.isSale() &&
                !trans.isCompletion() &&
                !(trans.isRefund() && payCfg.isEfbRefundAllowed()) &&
                !(trans.isCash() && payCfg.isEfbCashoutAllowed())) {
            Timber.i("EFB not available, Transaction Type not supported");
            return false;
        }

        if (trans.getCard().getCardType() == TCard.CardType.CTLS) {
            return false; // No EFB for Contactless for both modes
        }

        if (currentMode == CheckEFBMode.CHECK_EFB_BEFORE_ONLINE_PROCESSING &&
            trans.getCard().getCardType() == TCard.CardType.EMV) {
                return false; // No "Continue in Fallback" EFB for EMV
        }

        if (isMaxEfbTransLimitReached()) {
            Timber.i("EFB not available, max transaction count reached");
            return false;
        }

        if (isEfbOverFloorLimit()) {
            if (currentMode == CheckEFBMode.CHECK_EFB_BEFORE_ONLINE_PROCESSING) {
                return false;
            } else {
                if (!payCfg.isEfbAuthNumberOverFloorLimitAllowed()) {
                    Timber.i("EFB not available, amount is over floor limit");
                    return false;
                }
                if (!inputAuthNumber()) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean efbCheckExpiryDate(int plasticCardLife) {
        String expiry = trans.getCard().getExpiry();
        if (expiry == null || expiry.length() < 4) {
            return false;
        }

        int expiryDateYear;
        int expiryDateMonth;
        try {
            expiryDateYear = Integer.parseInt(expiry) / 100;
            expiryDateYear += (expiryDateYear < 70 ? 2000 : 1900); // convert to YYYY
            expiryDateMonth = Integer.parseInt(expiry) % 100;
        } catch (NumberFormatException e) {
            Timber.e(e);
            return false;
        }
        if (expiryDateMonth == 0) {
            return false;
        }

        Calendar expiryDate = Calendar.getInstance();
        expiryDate.set(expiryDateYear, expiryDateMonth - 1, 1, 23, 59, 59); // month is 0-based
        expiryDate.set(Calendar.DAY_OF_MONTH, expiryDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date today = Calendar.getInstance().getTime();
        Date expiryTime = expiryDate.getTime();

        if (today.after(expiryTime)) {
            return false;
        }

        // check Expiry Date is under Plastic Card’s Life
        if (plasticCardLife > 0) {
            Calendar plasticCardLifeEndDate = Calendar.getInstance();
            plasticCardLifeEndDate.add(Calendar.DAY_OF_MONTH, -plasticCardLife);
            if (plasticCardLifeEndDate.before(expiryTime)) {
                return false;
            }
        }
        return true;
    }

    private boolean isMaxEfbTransLimitReached() {
        long count = TransRecManager.getInstance().getTransRecDao().getTransCountByMsgStatusAndAuthMethod(TProtocol.MessageStatus.ADVICE_QUEUED, EFB_AUTHORISED, OFFLINE_EFB_AUTHORISED);
        return count >= Integer.parseInt(d.getPayCfg().getMaxEfbTrans());
    }

    private boolean isEfbOverFloorLimit() {
        if (trans.isRefund()) {
            return false; // no floor limit check for Refund
        }

        CardProductCfg cardsCfg = trans.getCard().getCardsConfig(d.getPayCfg());
        long floor = trans.isCash() ? cardsCfg.getLimits().getCashFloor() : cardsCfg.getLimits().getFloor();
        return trans.getAmounts().getTotalAmount() > (floor * TAmounts.getAmountCurExMul(d.getPayCfg().getCurrencyNum()));
    }

    private boolean inputAuthNumber() {
        if (trans.getProtocol().getAuthCode() != null && trans.getProtocol().getAuthCode().length() > 0) {
            return false;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiTitleId, trans.getTransType().displayId);
        // NOTE: Override titleId with displayId
        ui.showInputScreen(ENTER_EFB_AUTH_NUMBER, map);

        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            String result = ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1);
            trans.getProtocol().setAuthCode(result);
            // success
            return true;
        }
        // cancel/timeout
        return false;
    }
}

