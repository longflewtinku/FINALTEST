package com.linkly.libengine.helpers;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_OPERATOR_TIMEOUT;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_CTLS_DECLINED;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_CTLS;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_CTLS_FAULTY;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_EMV;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_EMV_FAULTY;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_NONE;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_TIMEOUT;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_QUESTION;
import static com.linkly.libui.IUIDisplay.UIResultCode.ABORT;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.IUIDisplay.UIResultCode.POS_YES;
import static com.linkly.libui.UIScreenDef.ACT_QUESTION_SCREEN;
import static com.linkly.libui.UIScreenDef.CARD_AUTH_FAIL;
import static com.linkly.libui.UIScreenDef.CARD_BLOCKED;
import static com.linkly.libui.UIScreenDef.CARD_DECLINED;
import static com.linkly.libui.UIScreenDef.CARD_EXPIRED;
import static com.linkly.libui.UIScreenDef.CARD_KEY_ERROR;
import static com.linkly.libui.UIScreenDef.CARD_NO_RESPOND;
import static com.linkly.libui.UIScreenDef.CARD_READ_ERROR;
import static com.linkly.libui.UIScreenDef.CARD_REPORT_ERROR;
import static com.linkly.libui.UIScreenDef.CARD_SIG_ERROR;
import static com.linkly.libui.UIScreenDef.CARD_TERMINATED;
import static com.linkly.libui.UIScreenDef.CARD_TYPE_NOT_ALLOWED;
import static com.linkly.libui.UIScreenDef.CARD_TYPE_NOT_READ;
import static com.linkly.libui.UIScreenDef.CVM_FAIL;
import static com.linkly.libui.UIScreenDef.ERROR_PRESENT_ONE_CARD;
import static com.linkly.libui.UIScreenDef.ISSUER_ERROR;
import static com.linkly.libui.UIScreenDef.MISSING_CAPK;
import static com.linkly.libui.UIScreenDef.ONLINE_REQUIRED;
import static com.linkly.libui.UIScreenDef.OUT_OF_SEQUENCE;
import static com.linkly.libui.UIScreenDef.OVER_MAX_LIMIT;
import static com.linkly.libui.UIScreenDef.PROC_REST_FAIL;
import static com.linkly.libui.UIScreenDef.READ_ERROR_REMOVE_CARD;
import static com.linkly.libui.UIScreenDef.REMOVE_CARD;
import static com.linkly.libui.UIScreenDef.REQ_ONLINE_AUTH;
import static com.linkly.libui.UIScreenDef.SDA_DDN_NO_SUPPORT;
import static com.linkly.libui.UIScreenDef.SD_MEM_ERROR;
import static com.linkly.libui.UIScreenDef.TAA_FAIL;
import static com.linkly.libui.UIScreenDef.TRANS_CANCEL;
import static com.linkly.libui.UIScreenDef.TRANS_TIMEOUT;
import static com.linkly.libui.UIScreenDef.TRM_FAILED;
import static com.linkly.libui.UIScreenDef.TRY_CONTACT_MSR_TRANS;
import static com.linkly.libui.UIScreenDef.TRY_CONTACT_TRANS;
import static com.linkly.libui.UIScreenDef.TRY_INSERT_SWIPE_OTHER_TRANS;
import static com.linkly.libui.UIScreenDef.UNKNOWN_ELEMENT;
import static com.linkly.libui.UIScreenDef.WALLET_TRANS_NOT_ALLOWED_TRY_ANOTHER;
import static com.linkly.libui.UIScreenDef.ZERO_TRANS_AMOUNT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_BORDER_DOUBLE;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_BORDER_LEFT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_RIGHT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_TRANSPARENT_DOUBLE;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;

import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.status.IStatus;
import com.linkly.libmal.IMal;
import com.linkly.libsecapp.IP2PCard;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;
import com.linkly.libui.UIScreenDef;
import com.linkly.libui.display.DisplayFragmentOption;
import com.linkly.libui.display.DisplayQuestion;
import com.linkly.libui.speech.SpeechUtils;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class UIHelpers {
    private final static String TAG = UIHelpers.class.getSimpleName();

    public enum YNQuestion{YES, NO, CANCEL};

    public static void uiShowTryContact(IDependency d, TransRec trans) {
        d.getUI().showScreen(TRY_CONTACT_TRANS, trans.getTransType().displayId);
        d.getUI().getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
    }

    public static void uiShowTryAnotherCard(IDependency d, TransRec trans) {
        d.getUI().showScreen(TRY_INSERT_SWIPE_OTHER_TRANS, trans.getTransType().displayId);
        d.getUI().getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
    }

    public static void uiShowWalletsNotAllowedTryAnother(IDependency d, TransRec trans) {
        d.getUI().showScreen(WALLET_TRANS_NOT_ALLOWED_TRY_ANOTHER, trans.getTransType().displayId);
        d.getUI().getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
    }

    public static void uiShowDismissableScreen(IDependency d, UIScreenDef screenDef ) {
        // Display a dismissible Screen
        IUIDisplay ui = d.getUI();
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<DisplayQuestion> options = new ArrayList<>();

        options.add(new DisplayQuestion(d.getPrompt(String_id.STR_OK), "OP0", BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
        map.put(IUIDisplay.uiScreenOptionList, options);

        ui.showScreen(screenDef, map);
        if (ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.LONG_TIMEOUT) == IUIDisplay.UIResultCode.OK) {
            // The Result is not important in this situation
            ui.getResultText(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.uiResultText1);
        }
    }

    public static UIHelpers.YNQuestion uiYesNoCancelQuestion(IDependency d, UIScreenDef screenDef, String_id overrideTitle, String ... questionArg){
        HashMap<String, Object> map = new HashMap<>();
        // Allow us to override the title. ie Tipping.
        if(overrideTitle != null) {
            map.put(IUIDisplay.uiScreenTitle, UI.getInstance().getPrompt(overrideTitle));
        }

        map.put(IUIDisplay.uiScreenFragType, IUIDisplay.FRAG_TYPE.FRAG_NOT_SET);
        map.put(IUIDisplay.uiPromptIdArg,questionArg);

        ArrayList<DisplayQuestion> options = new ArrayList<>();
        options.add(new DisplayQuestion(String_id.STR_NO, "NO", BTN_STYLE_PRIMARY_BORDER_LEFT));
        options.add(new DisplayQuestion(String_id.STR_YES, "YES", BTN_STYLE_PRIMARY_DEFAULT_RIGHT));
        options.add(new DisplayQuestion(String_id.STR_CANCEL, "Cancel", BTN_STYLE_TRANSPARENT_DOUBLE));
        map.put(IUIDisplay.uiScreenOptionList, options);
        d.getUI().showScreen(screenDef, map);

        IUIDisplay.UIResultCode res = d.getUI().getResultCode(screenDef.id, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            String result = d.getUI().getResultText(screenDef.id, IUIDisplay.uiResultText1);
            if (result.compareTo("YES") == 0) {
                d.getDebugReporter().reportYesNoKeyPressed( IDebug.DEBUG_KEY.YES );
                return UIHelpers.YNQuestion.YES;
            } else if (result.compareTo("Cancel") == 0) {
                d.getDebugReporter().reportYesNoKeyPressed( IDebug.DEBUG_KEY.NO );
                return UIHelpers.YNQuestion.CANCEL;
            }
        } else if( res == ABORT ) {
            return UIHelpers.YNQuestion.CANCEL;
        }
        else if( POS_YES == res ){
            return YNQuestion.YES;
        }

        d.getDebugReporter().reportYesNoKeyPressed( IDebug.DEBUG_KEY.NO );
        return UIHelpers.YNQuestion.NO;
    }

    public static boolean uiYesNoQuestion(IDependency d, String title, String question, ArrayList<DisplayFragmentOption> fragOptions, IUIDisplay.FRAG_TYPE fragType, int timeout) {
        boolean result = false;

        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiScreenTitle, title);
        map.put(IUIDisplay.uiScreenPrompt, question);
        map.put(IUIDisplay.uiScreenFragType, fragType);
        //Investigate this further, print cardholder fragment dies without this boolean
        map.put(IUIDisplay.uiKeepOnScreen, true);

        ArrayList<DisplayQuestion> options = new ArrayList<>();
        options.add(new DisplayQuestion(String_id.STR_YES, "YES", BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
        options.add(new DisplayQuestion(String_id.STR_NO, "NO", BTN_STYLE_PRIMARY_BORDER_DOUBLE));
        map.put(IUIDisplay.uiScreenOptionList, options);

        if (fragOptions != null)
            map.put(IUIDisplay.uiScreenFragOptionList, fragOptions);

        d.getUI().showScreen(ACT_QUESTION_SCREEN, map);
        IUIDisplay.UIResultCode res;
        if (timeout > 0) {
            res = d.getUI().getResultCode(ACT_QUESTION, timeout);
        } else {
             res = d.getUI().getResultCode(ACT_QUESTION, IUIDisplay.LONG_TIMEOUT);
        }

        switch( res ) {
            case OK: {
                String resultText = d.getUI().getResultText( ACT_QUESTION, IUIDisplay.uiResultText1 );
                result = resultText.equals( "YES" );
                d.getDebugReporter().reportYesNoKeyPressed( result ? IDebug.DEBUG_KEY.YES : IDebug.DEBUG_KEY.NO );
                break;
            }
            case POS_YES: {
                result = true;
                d.getDebugReporter().reportYesNoKeyPressed( IDebug.DEBUG_KEY.YES );
                break;
            }
            case TIMEOUT: {
                d.getStatusReporter().reportStatusEvent( STATUS_OPERATOR_TIMEOUT , d.getCurrentTransaction().isSuppressPosDialog() );
                d.getUI().showScreen( TRANS_TIMEOUT );
                break;
            }
            default: {
                Timber.w( "Unhandled Result = " + res.toString() );
                result = false;
                break;
            }
        }
        return result;
    }

    public static boolean uiYesNoQuestion(IDependency d, String title, String question, int timeout) {
        return uiYesNoQuestion(d,title, question, null, IUIDisplay.FRAG_TYPE.FRAG_NOT_SET, timeout);
    }

    public static String uiDisplayCtlsError(IDependency d, TransRec trans) {
        UIScreenDef screenDef;

        switch (trans.getCard().getCtlsResultCode()) {
            case P2P_CTLS_OUT_OF_SEQUENCE:
                screenDef = OUT_OF_SEQUENCE;
                break;
            case P2P_CTLS_FALLBACK_TO_ICC:
                screenDef = TRY_CONTACT_TRANS;
                break;
            case P2P_CTLS_FALLBACK_TO_ICC_OR_MSR:
                screenDef = TRY_CONTACT_MSR_TRANS;
                break;
            case P2P_CTLS_ZERO_TRANS_AMOUNT:
                screenDef = ZERO_TRANS_AMOUNT;
                break;
            case P2P_CTLS_CARD_REPORTED_ERROR:
                screenDef = CARD_REPORT_ERROR;
                break;
            case P2P_CTLS_COLLISION:
                screenDef = ERROR_PRESENT_ONE_CARD;
                break;
            case P2P_CTLS_OVER_MAXIMUM_LIMIT:
                screenDef = OVER_MAX_LIMIT;
                break;
            case P2P_CTLS_REQUEST_ONLINE_AUTH:
                screenDef = REQ_ONLINE_AUTH;
                break;
            case P2P_CTLS_CARD_BLOCKED:
                screenDef = CARD_BLOCKED;
                break;
            case P2P_CTLS_CARD_EXPIRED:
                screenDef = CARD_EXPIRED;
                break;
            case P2P_CTLS_CARD_UNSUPPORTED:
                screenDef = CARD_TYPE_NOT_ALLOWED;
                break;
            case P2P_CTLS_CARD_DID_NOT_RESPOND:
                screenDef = CARD_NO_RESPOND;
                break;
            case P2P_CTLS_UNKNOWN_DATA_ELEMENT:
                screenDef = UNKNOWN_ELEMENT;
                break;
            case P2P_CTLS_REQUIRED_DATA_MISSING:
            case P2P_CTLS_NO_APP:
                screenDef = CARD_TYPE_NOT_READ;
                break;
            case P2P_CTLS_CARD_TERMINATE:
                screenDef = CARD_TERMINATED;
                break;
            case P2P_CTLS_CARD_GENERATED_AAC:
                screenDef = CARD_DECLINED;
                break;
            case P2P_CTLS_CARD_GENERATED_ARQC:
                screenDef = ONLINE_REQUIRED;
                break;
            case P2P_CTLS_SDA_DDA_NOT_SUPPORTED:
                screenDef = SDA_DDN_NO_SUPPORT;
                break;
            case P2P_CTLS_SDA_DDA_MISSING_CAPK:
                screenDef = MISSING_CAPK;
                break;
            case P2P_CTLS_SDA_DDA_ISSUER_PUB_KEY_ERROR:
                screenDef = ISSUER_ERROR;
                break;
            case P2P_CTLS_SDA_AUTH_FAILED:
                screenDef = CARD_AUTH_FAIL;
                break;
            case P2P_CTLS_DDA_PUB_KEY_ERROR:
                screenDef = CARD_KEY_ERROR;
                break;
            case P2P_CTLS_DDA_SIG_VERIFY_ERROR:
                screenDef = CARD_SIG_ERROR;
                break;
            case P2P_CTLS_PROCESSING_RESTRICTIONS_FAILED:
                screenDef = PROC_REST_FAIL;
                break;
            case P2P_CTLS_TRM_FAILED:
                screenDef = TRM_FAILED;
                break;
            case P2P_CTLS_CVM_FAILED:
                screenDef = CVM_FAIL;
                break;
            case P2P_CTLS_TAA_FAILED:
                screenDef = TAA_FAIL;
                break;
            case P2P_CTLS_SD_MEMORY_ERROR:
                screenDef = SD_MEM_ERROR;
                break;
            case P2P_CTLS_USER_TIMEOUT:
                screenDef = TRANS_TIMEOUT;
                break;
            case P2P_CTLS_USER_CANCELLED:
                screenDef = TRANS_CANCEL;
                break;
            case P2P_CTLS_OS_ERROR:
            case P2P_CTLS_READ_ERROR:
            default:
                screenDef = CARD_READ_ERROR;
                break;
        }

        d.getUI().showScreen(screenDef);
        d.getStatusReporter().reportStatusEvent(STATUS_TRANS_CTLS_DECLINED , trans.isSuppressPosDialog());

        // return actual string
        if( screenDef.promptId != null ) {
            return d.getPrompt(screenDef.promptId);
        }

        return null;
    }

    public static void cardRemoveWithMessage(IDependency d, IMal mal, UIScreenDef screenDef, boolean bEnableEmv, boolean bEnableCtls, TransRec trans) {
        if (screenDef == REMOVE_CARD || screenDef == READ_ERROR_REMOVE_CARD) {
            // Note - 'trans' may be null here - e.g. if we're doing a QueryCard operation. If so, use timeout as if access mode were false.
            screenDef.timeout = d.getPayCfg().getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.REMOVE_CARD_TIMEOUT,
                    (trans != null) && trans.getAudit().isAccessMode());
        }
        d.getP2PLib().getIP2PCard().cardReset(false);

        IP2PCard.CardType cType = d.getP2PLib().getIP2PCard().cardGet(false, bEnableEmv, bEnableCtls, 250, true);
        if ((bEnableEmv && (cType == CT_EMV || cType == CT_EMV_FAULTY)) ||
                (bEnableCtls && (cType == CT_CTLS || cType == CT_CTLS_FAULTY))) {
            d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_REMOVE_CARD , (trans != null) && trans.isSuppressPosDialog());
            long startTime = SystemClock.elapsedRealtime(); //fetch starting time
            while (( SystemClock.elapsedRealtime() - startTime ) < screenDef.timeout ) {
                if (trans != null)
                    d.getUI().showScreen(screenDef, trans.getTransType().displayId);
                else
                    d.getUI().showScreen(screenDef);

                cType = d.getP2PLib().getIP2PCard().cardGet(false, bEnableEmv, bEnableCtls, 500, true);
                if (cType == CT_NONE || cType == CT_TIMEOUT) {
                    Timber.i("Card Removed");
                    return;
                }

                mal.getHardware().beep( 500 );
                if( !SpeechUtils.getInstance().isSpeaking() && trans != null && trans.getAudit().isAccessMode() ) {
                    SpeechUtils.getInstance().speak("Please remove card");
                }
            }
        }
    }

    @SuppressWarnings({"deprecation","java:S1874", "java:S1135"}) // deprecation warning, todo warning
    public static void wakeTerminalIfSleeping(Context context){
        // if we get the main 'launch' intent, wake terminal
        PowerManager pm = (PowerManager)context.getSystemService(POWER_SERVICE);
        if (!pm.isInteractive()) {
            // There was an option to use WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON in activity which turns on the screen,
            // however takes a couple of seconds to work, which is not working in our case.
            // So, Using PowerManager.SCREEN_DIM_WAKE_LOCK to wake up the device, if this stops working might need to migrate to WorkManager
            // TODO: need to migrate to use WorkManager in future (https://developer.android.com/topic/libraries/architecture/workmanager)
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "AppName:MessageReceiver");

            // Acquire and release the wakelock after 30 seconds.
            // 30 seconds because during the first transaction requiring a logon could take some time
            // and the device may go back to sleep before next screen is shown
            wl.acquire(30*1000L /*30 seconds*/);

            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
            keyguardLock.disableKeyguard();
        }
    }

}
