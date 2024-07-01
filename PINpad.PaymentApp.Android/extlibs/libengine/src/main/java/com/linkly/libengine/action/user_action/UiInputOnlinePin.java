package com.linkly.libengine.action.user_action;

import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_ONLINE_PIN;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_PIN_AND_SIG;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_ONLINE_PIN_REQUESTED;
import static com.linkly.libsecapp.IP2PSec.InstalledKeyType.AS2805;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT_PIN;
import static com.linkly.libui.IUIDisplay.SCREEN_ID.ONLINE_PIN;
import static com.linkly.libui.IUIDisplay.String_id;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.IUIDisplay.UIResultCode.TIMEOUT;

import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.env.Stan;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.currency.CountryCode;
import com.linkly.libui.currency.ISOCountryCodes;

import timber.log.Timber;

public class UiInputOnlinePin extends IAction {

    @Override
    public String getName() {
        return "UiInputOnlinePin";
    }

    @Override
    public void run() {
        Timber.e("UiInputOnlinePin entry");

        /* ICC ones do the online pin from a callback to the kernel, so we dont need to do the first one */
        if (trans.getCard().isSwiped() ||
                trans.getCard().isManual() ||
                trans.getCard().isCtlsCaptured()
        ) {
            uiInputOnlinePin();

        } else {
            Timber.i("Not a magnetic, CTLS MSR, PKE or CTLS transaction, skip online pin");
        }

        Timber.e("UiInputOnlinePin exit");
    }

    private void uiInputOnlinePin() {
        TCard.CvmType cvmType = trans.getCard().getCvmType();
        boolean pinResult = false;

        Timber.e("cvmType = %s", cvmType);

        if (cvmType.isOnlinePin()) {
            d.getStatusReporter().reportStatusEvent(STATUS_UI_ONLINE_PIN_REQUESTED, trans.isSuppressPosDialog());
            long amount = trans.getAmounts().getTotalAmount();
            CountryCode cCode = ISOCountryCodes.getInstance().getCountryFrom3Num(d.getPayCfg().getCurrencyNum() + "");

            int tryCount = trans.getCard().getPinTryCount();
            String displayText = d.getPrompt(String_id.STR_ENTER_PIN);

            if (tryCount > 0) {
                displayText += "(" + d.getPrompt(String_id.STR_ENTER_PIN_TRY) +  (tryCount) + ")";
            }

            if (tryCount == 2) {
                displayText = d.getPrompt(String_id.STR_ENTER_PIN_LAST_TRY);
            }

            trans.getCard().setPinTryCount(tryCount + 1);
            ui.getPin(ONLINE_PIN, null, trans.getTransType().getDisplayName(), displayText, amount, cCode);
            int getPinTimeout = super.d.getPayCfg().
                    getUiConfigTimeouts().
                    getTimeoutMilliSecs(
                            ConfigTimeouts.CARD_PIN_ENTRY_TIMEOUT,
                            super.trans.getAudit().isAccessMode()
                    );

            Timber.e("getPinTimeout = %d", getPinTimeout);

            IUIDisplay.UIResultCode uiRes = ui.getResultCode(ACT_INPUT_PIN, getPinTimeout);
            Timber.e("getPin uiRes = %s", uiRes);
            if (uiRes == OK) {
                pinResult = true;
            } else if (uiRes == TIMEOUT) {
                d.getP2PLib().getIP2PSec().cancelPinEntry();
                d.getProtocol().setInternalRejectReason( trans, IProto.RejectReasonType.USER_TIMEOUT );
                Timber.e("Pin Entry Timeout");
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                d.getDebugReporter().reportTimeout(IDebug.DEBUG_POSITION.ENTER_PIN);
            } else {
                d.getP2PLib().getIP2PSec().cancelPinEntry();
                d.getDebugReporter().reportCancelSelect( IDebug.DEBUG_POSITION.ENTER_PIN );
                Timber.e("Pin Entry Timeout");
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            }
            // TODO add in special cases for pin retries, and for set pin etc
        } else {
            Timber.e("UiInputOnlinePin no PIN required");
            // no PIN required. if as2805, we may need a NULL pin block for sending to the host
            if( d.getP2PLib().getIP2PSec().getInstalledKeyType() == AS2805 ) {
                // get NULL pin block.
                // get STAN for transaction - very important as stan is used in KPE pin encryption key calculation
                trans.getProtocol().setStan(Stan.getNewValue());
                d.getP2PLib().getIP2PSec().as2805GetPinBlock( true, "0", trans.getProtocol().getStan(), (int)trans.getAmounts().getTotalAmount(), 1000, null );
            }
        }
    }
}
