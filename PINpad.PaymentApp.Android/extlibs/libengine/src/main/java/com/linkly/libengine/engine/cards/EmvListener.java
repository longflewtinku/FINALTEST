package com.linkly.libengine.engine.cards;

import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.SIG;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_OPERATOR_TIMEOUT;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_OFFLINE_PIN_REQUESTED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_ONLINE_PIN_REQUESTED;
import static com.linkly.libsecapp.emv.Tag.account_type;
import static com.linkly.libsecapp.emv.Tag.amt_auth_num;
import static com.linkly.libsecapp.emv.Tag.amt_other_num;
import static com.linkly.libsecapp.emv.Tag.appl_id;
import static com.linkly.libui.IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_ACCESS_APP_SELECTION;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT_PIN;
import static com.linkly.libui.IUIDisplay.SCREEN_ID.OFFLINE_PIN;
import static com.linkly.libui.IUIDisplay.SCREEN_ID.ONLINE_PIN;
import static com.linkly.libui.IUIDisplay.String_id.STR_AMOUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_CANCEL;
import static com.linkly.libui.IUIDisplay.String_id.STR_CHEQUE;
import static com.linkly.libui.IUIDisplay.String_id.STR_CREDIT;
import static com.linkly.libui.IUIDisplay.String_id.STR_SAVINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_SELECT_APPLICATION;
import static com.linkly.libui.IUIDisplay.UIResultCode.ABORT;
import static com.linkly.libui.IUIDisplay.UIResultCode.BYPASSED;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.IUIDisplay.UIResultCode.TIMEOUT;
import static com.linkly.libui.UIScreenDef.APP_SELECTION;
import static com.linkly.libui.UIScreenDef.APP_SELECTION_ACCESS_MODE;

import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.status.IStatus;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.IP2PEMV;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UIScreenDef;
import com.linkly.libui.currency.CountryCode;
import com.linkly.libui.currency.ISOCountryCodes;
import com.linkly.libui.display.DisplayFragmentOption;
import com.linkly.libui.display.DisplayQuestion;
import com.pax.jemv.clcommon.RetCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class EmvListener implements IP2PEMV.EmvDeviceListener {

    private static final String TAG = "EmvListener";
    public boolean signatureRequired = false;

    private IDependency d;
    private IUIDisplay ui;
    public EmvListener(IDependency d) {
        this.d = d;
        this.ui = d.getUI();
    }

    public void emvDisplayApplicationMenu(int tryCount, List<String> list, int appNumber) {
        int i = 0;
        final String CANCEL = "C";
        boolean cancelled = false;
        HashMap<String, Object> map = new HashMap<>();
        Timber.i("emvDisplayApplicationMenu : need to call app select page");
        TAmounts amounts = d.getCurrentTransaction().getAmounts();
        String amountFormatted = d.getFramework().getCurrency().formatUIAmount(String.valueOf(amounts.getTotalAmountWithoutSurcharge()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());

        map.put(IUIDisplay.uiScreenFragType, IUIDisplay.FRAG_TYPE.FRAG_GRID);

        ArrayList<DisplayFragmentOption> fragOptions = new ArrayList<>();
        fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_AMOUNT) + ":", amountFormatted));
        fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_SELECT_APPLICATION)));
        map.put(IUIDisplay.uiScreenFragOptionList, fragOptions);
        //Ask a Question
        map.put(IUIDisplay.uiTitleId, d.getCurrentTransaction().getTransType().displayId);

        ArrayList<DisplayQuestion> options = new ArrayList<>();

        for (String s : list) {
            options.add(new DisplayQuestion(s, i + "", DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
            i++;
        }
        options.add(new DisplayQuestion(STR_CANCEL, CANCEL, DisplayQuestion.EButtonStyle.BTN_STYLE_TRANSPARENT));
        map.put(IUIDisplay.uiScreenOptionList, options);

        d.getCurrentTransaction().getCard().setAppSelected(true);
        if (d.getCurrentTransaction().getAudit().isAccessMode()) {
            ui.showScreen(APP_SELECTION_ACCESS_MODE, map);
        } else {
            ui.showScreen(APP_SELECTION, map);
        }

        IUIDisplay.ACTIVITY_ID respondingActivity = d.getCurrentTransaction().getAudit().isAccessMode() ? ACT_ACCESS_APP_SELECTION : IUIDisplay.ACTIVITY_ID.ACT_SELECT_APPLICATION;
        IUIDisplay.UIResultCode resultCode = ui.getResultCode(respondingActivity,d.getPayCfg().getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.APP_SELECTION_TIMEOUT,d.getCurrentTransaction().getAudit().isAccessMode()));

        switch ( resultCode ) {
            case OK:
                String result = ui.getResultText( respondingActivity, IUIDisplay.uiResultText1 );

                if ( CANCEL.equals( result ) ) {
                    cancelled = true;
                } else {
                    // This is not the account selection screen but for debug purposes we will report it as acc select
                    int selected = Integer.parseInt( result );
                    debugAccountSelectMessage( list, selected );
                    ui.displayPleaseWaitScreen();
                    d.getP2PLib().getIP2PEmv().emvSetCallBackResult( selected ); //force to select the first app
                }
                break;
            case TIMEOUT:
                d.getStatusReporter().reportStatusEvent( STATUS_OPERATOR_TIMEOUT , d.getCurrentTransaction().isSuppressPosDialog());
                d.getP2PLib().getIP2PEmv().emvSetCallBackResult( RetCode.EMV_TIME_OUT );
                d.getProtocol().setInternalRejectReason( d.getCurrentTransaction(), IProto.RejectReasonType.USER_TIMEOUT );
                Timber.e("Application selection Timeout");
                d.getWorkflowEngine().setNextAction( TransactionCanceller.class );
                break;
            default:
                Timber.w( "Unexpected resultCode from Screen = %s", resultCode.toString() );
                cancelled = true;
                break;
        }

        if( cancelled ){
            d.getDebugReporter().reportCancelSelect( IDebug.DEBUG_POSITION.SELECT_APPLICATION );
            d.getP2PLib().getIP2PEmv().emvSetCallBackResult( RetCode.EMV_USER_CANCEL );
            d.getWorkflowEngine().setNextAction( TransactionCanceller.class );
        }

    }

    /**
     * Report account Selected to the POS
     * @param list {@link List} of account strings to choose from
     * @param accountType index selected by user
     * */
    private void debugAccountSelectMessage( List<String> list, int accountType) {
        if( list != null && accountType < list.size() ){
            String account = list.get( accountType ).toUpperCase();
            IDebug.DEBUG_ACCOUNT debugAccount = null;

            if( account.contains( IDebug.DEBUG_ACCOUNT.SAVINGS.getAccountType() ) || account.contains( d.getPrompt( STR_SAVINGS ).toUpperCase() ) ){
                debugAccount = IDebug.DEBUG_ACCOUNT.SAVINGS;
            } else if( account.contains( IDebug.DEBUG_ACCOUNT.CHEQUE.getAccountType() ) || account.contains( d.getPrompt( STR_CHEQUE ).toUpperCase() ) ){
                debugAccount = IDebug.DEBUG_ACCOUNT.CHEQUE;
            } else if( account.contains( IDebug.DEBUG_ACCOUNT.CREDIT.getAccountType() ) || account.contains( d.getPrompt( STR_CREDIT ).toUpperCase() ) ){
                debugAccount = IDebug.DEBUG_ACCOUNT.CREDIT;
            } else {
                Timber.w( "UNKNOWN ACCOUNT SELECTED = " + account );
            }

            if( debugAccount != null ){
                d.getDebugReporter().reportDebugAccountSelect( debugAccount );
            }
        }
    }

    public boolean emvCheckPinEntryBypass() {
        if (d.getCurrentTransaction().isTopupCompletion()) {
            d.getP2PLib().getIP2PEmv().emvSetCallBackResult(RetCode.EMV_NO_PASSWORD);
            return true;
        }

        if (CoreOverrides.get().isDisablePinEntry()) {
            d.getP2PLib().getIP2PEmv().emvSetCallBackResult(RetCode.EMV_NO_PASSWORD);
            return true;
        }
        return false;
    }

    public EMV_PIN_RESULT emvDisplayPinEntryScreen( boolean onlinePin, boolean block, int tryCount, int remainCount) {

        IUIDisplay.UIResultCode uiRet = OK;
        TransRec trans = d.getCurrentTransaction();
        String displayText = d.getPrompt(String_id.STR_ENTER_PIN);

        /* increment our own pin try counter, in case of an emv retry */
        trans.getCard().setPinTryCount(trans.getCard().getPinTryCount() + 1);

        if (tryCount > 0) {
            displayText += "(" + d.getPrompt(String_id.STR_ENTER_PIN_TRY) +  tryCount + ")";
        }

        if (remainCount == 1) {
            displayText = d.getPrompt(String_id.STR_ENTER_PIN_LAST_TRY);
        }

        long amount = trans.getAmounts().getTotalAmount();
        ISOCountryCodes.getInstance().getCountryFrom3Num( Objects.requireNonNull( d.getPayCfg() ).getCurrencyNum() + "" );
        CountryCode cCode = ISOCountryCodes.getInstance().getCountryFrom3Num(d.getPayCfg().getCurrencyNum() + "");

        Timber.i( "getting PIN, type = " + (onlinePin?"ONLINE":"OFFLINE") + ", tryCount = " + tryCount + " remainCnt = " + remainCount);
        d.getStatusReporter().reportStatusEvent( onlinePin ? STATUS_UI_ONLINE_PIN_REQUESTED : STATUS_UI_OFFLINE_PIN_REQUESTED , trans.isSuppressPosDialog() );
        d.getFramework().getUI().getPin( onlinePin ? ONLINE_PIN : OFFLINE_PIN, null, trans.getTransType().getDisplayName(), displayText, amount, cCode);

        int pinEntryTimeout = d.getPayCfg().getUiConfigTimeouts().
                getTimeoutMilliSecs(
                        ConfigTimeouts.CARD_PIN_ENTRY_TIMEOUT,
                        trans.getAudit().isAccessMode()
                );
        if (block) {
            uiRet = ui.getResultCode(ACT_INPUT_PIN, pinEntryTimeout);
        } else {
            Timber.i( "PIN block emvGetHolderPwd");
            Util.Sleep(550); /* workaround for pax kernel delay in getting pin listeners ready DO NOT REMOVE */
        }
        Timber.i( "uiRet:%s", uiRet);
        if (uiRet == BYPASSED) {
            return EMV_PIN_RESULT.BYPASSED;
        } else if (uiRet == ABORT){
            P2PLib.getInstance().getIP2PSec().cancelPinEntry();
            d.getDebugReporter().reportCancelSelect( IDebug.DEBUG_POSITION.ENTER_PIN );
            Timber.e("Pin Entry Cancelled");
            return EMV_PIN_RESULT.CANCEL;
        } else if (uiRet == TIMEOUT) {
            P2PLib.getInstance().getIP2PSec().cancelPinEntry();
            d.getDebugReporter().reportTimeout(IDebug.DEBUG_POSITION.ENTER_PIN);
            d.getProtocol().setInternalRejectReason(d.getCurrentTransaction(), IProto.RejectReasonType.USER_TIMEOUT);
            Timber.e("Pin Entry Timeout");
            return EMV_PIN_RESULT.CANCEL;
        }

        return EMV_PIN_RESULT.OK;
    }

    public int getAmountTrans() {
        Timber.i( "callback getAmountTrans");
        return (int)d.getCurrentTransaction().getAmounts().getTotalAmount();
    }

    public int getAmountOther() {
        Timber.i( "callback getAmountOther");
        return (int)d.getCurrentTransaction().getAmounts().getCashbackAmount();
    }

    public void setPdolTagData() {
        Timber.i( "callback setPdolTagData");
        try {
            TransRec trans = d.getCurrentTransaction();
            IP2PEMV iEmv = P2PLib.getInstance().getIP2PEmv();

            byte[] cardAid = iEmv.emvGetTag(appl_id, true);
            if (cardAid != null && cardAid.length > 0) {
                trans.getCard().setAid(Util.byteArrayToHexString(cardAid));
            }

            // if default account is not set, try to set it now
            if( trans.getProtocol().getAccountType() == 0 ) {
                Emv.getInstance().emvGetDefaultAccount(d, trans);
            }

            // set 5f57 (account type). get from txn data as base 10 decimal integer. convert to BCD
            byte[] accountTypeBcd = Util.DecToBCD(trans.getProtocol().getAccountType(), 1);
            Timber.i( "setting acc type " + Util.bcd2Str(accountTypeBcd));
            iEmv.emvSetTag((short) account_type.value(), accountTypeBcd);

            // set 9f02 (transaction total amount, bcd)
            byte[] amountTotalBcd = Util.DecToBCD(trans.getAmounts().getTotalAmount(), 6);
            Timber.i( "setting amountTotalBcd " + Util.bcd2Str(amountTotalBcd));
            iEmv.emvSetTag((short) amt_auth_num.value(), amountTotalBcd);

            if( trans.getAmounts().getCashbackAmount() > 0 ) {
                // set 9f03 (amount other, bcd), if cashback amount > 0
                byte[] amountOtherBcd = Util.DecToBCD(trans.getAmounts().getCashbackAmount(), 6);
                Timber.i( "setting amountOther " + Util.bcd2Str(amountOtherBcd));
                iEmv.emvSetTag((short) amt_other_num.value(), amountOtherBcd);
            }

            // set 9c, trans type here in case it's required in the GPO (specified in PDOL)
            String ttype = d.getProtocol().getEmvProcessingCode(trans);
            byte[] transType = Util.hexStringToByteArray(ttype);
            iEmv.emvSetTag((short) 0x9c, transType);

        } catch( Exception e ) {
            Timber.w(e);
        }
    }

    public void emvPinEntryHasBeenBypassed() {
        Timber.i( "emvPinEntryHasBeenBypassed");
    }

    public void performSignature() {
        Timber.i( "Callback: performSignature");
        signatureRequired = true;
        Timber.i( "Signature Required set to TRUE");
        d.getCurrentTransaction().getCard().setCvmType(SIG);
    }

    public int emvDisplayError(IP2PEMV.P2P_EMV_ERROR_CODES errorCode) {
        UIScreenDef screenDef;

        Timber.i( "emvDisplayError: " + errorCode);

        switch (errorCode) {
            case P2P_EMV_PIN_BLOCKED:
                screenDef = UIScreenDef.PIN_BLOCKED;
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_CARD_BLOCKED, d.getCurrentTransaction().isSuppressPosDialog());
                break;
            case P2P_EMV_CARD_BLOCKED:
                screenDef = UIScreenDef.CARD_BLOCKED;
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_CARD_BLOCKED, d.getCurrentTransaction().isSuppressPosDialog());
                break;
            case P2P_EMV_APPLICATION_BLOCKED:
                screenDef = UIScreenDef.APP_BLOCKED;
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_CARD_BLOCKED, d.getCurrentTransaction().isSuppressPosDialog());
                break;
            case P2P_EMV_CAPK_ERROR:
                screenDef = UIScreenDef.CAPK_NOT_FOUND;
                break;
            case P2P_EMV_APP_EXPIRED:
                screenDef = UIScreenDef.APPLICATION_EXPIRED;
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_CARD_EXPIRED , d.getCurrentTransaction().isSuppressPosDialog());
                break;
            case P2P_EMV_APP_NOT_EFFECTIVE:
                screenDef = UIScreenDef.APPLICATION_NOT_YET_EFFECTIVE;
                break;
            case P2P_EMV_USER_CANCELLED:
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_USER_CANCELLED , d.getCurrentTransaction().isSuppressPosDialog());
                screenDef = UIScreenDef.TRANSACTION_CANCELLED;
                break;
            case P2P_EMV_LAST_PIN_TRY:
                screenDef = UIScreenDef.LAST_PIN_TRY;
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_PIN_INVALID_LAST_TRY , d.getCurrentTransaction().isSuppressPosDialog());
                break;
            case P2P_EMV_INVALID_PIN:
                screenDef = UIScreenDef.INVALID_PIN_ENTERED;
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_PIN_INVALID_RETRY , d.getCurrentTransaction().isSuppressPosDialog());
                break;
            case P2P_EMV_CANDIDATE_LIST_EMPTY:
                screenDef = UIScreenDef.CHIP_READ_ERROR_CANDIDATE_LIST_EMPTY;
                break;
            case P2P_EMV_CMD_FAILED:
                screenDef = UIScreenDef.CARD_COMMAND_FAILED;
                break;

            case P2P_EMV_USER_TIMEOUT:
            case P2P_EMV_6985:
            case P2P_EMV_6800:
            case P2P_EMV_BAD_RESPONSE:
            case P2P_EMV_COMMS_ERROR: /* display nothing */
            case P2P_EMV_PIN_TRY_LIMIT_EXCEEDED: /* display nothing */
            case P2P_EMV_BAD_DATA_FORMAT: /* display nothing */
            case P2P_EMV_PIN_OKAY: /* display nothing */
            case P2P_EMV_PIN_BYPASSED:
                return 0;
            default:

                return 0;
        }

        ui.showScreen( screenDef );
        return 0;
    }
}
