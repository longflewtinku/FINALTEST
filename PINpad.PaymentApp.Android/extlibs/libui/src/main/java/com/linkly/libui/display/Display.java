package com.linkly.libui.display;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION_BASIC;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT_PIN;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_MAIN_MENU;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_SCREEN_SAVER;
import static com.linkly.libui.IUIDisplay.SCREEN_ICON.IN_PROGRESS;
import static com.linkly.libui.IUIDisplay.SCREEN_ICON.NO_ICON;
import static com.linkly.libui.IUIDisplay.SCREEN_ID.NOT_SET;
import static com.linkly.libui.IUIDisplay.UIResultCode.ABORT;
import static com.linkly.libui.IUIDisplay.UIResultCode.TIMEOUT;
import static com.linkly.libui.IUIDisplay.UIResultCode.UNKNOWN;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;

import com.linkly.kotlinandroidtoolkit.extensionfuncs.BundleExtensionsKt;
import com.linkly.libui.IUICallbacks;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;
import com.linkly.libui.currency.CountryCode;
import com.linkly.libui.currency.Currency;
import com.pax.dal.entity.RSAPinKey;

import java.io.Serializable;
import java.util.HashMap;

import timber.log.Timber;

public class Display implements IUIDisplay {

    private final static String TAG = "Display";
    private static final Object lock1 = new Object();
    private static HashMap<ACTIVITY_ID, DisplayResponse> lastUIresponseMap = new HashMap<>();
    private static UIResultCode PosKeyPress = UNKNOWN;
    private static int iUniqueId = 0;
    private IUICallbacks displayCallback;

    //========================================================================================================
    public Display(IUICallbacks displayCallback) {
        this.displayCallback = displayCallback;
    }

    private static synchronized void setLastUIResponse(IUIDisplay.ACTIVITY_ID activityId, DisplayResponse displayResponse) {
        synchronized (lock1) {
            if (displayResponse == null) {
                Timber.i( "Removing Activity: %s", activityId.name());
                lastUIresponseMap.remove(activityId);
            } else {
                lastUIresponseMap.put(activityId, displayResponse);
            }
            try {
                lock1.notifyAll();
            } catch (Exception e) {
                Timber.v(e);
            }
        }
    }

    /**
     * Stores a keypress in {@link Display#PosKeyPress} class member & synchronised by {@link Display#lock1 }
     * @param code {@link com.linkly.libui.IUIDisplay.UIResultCode} code given
     * */
    public static synchronized void insertResultCode(  UIResultCode code ) {
        Timber.d("Trying to set keypress = " + code.name() );
        synchronized ( lock1 ){
            Display.PosKeyPress = code;
        }
    }

    @SuppressWarnings("deprecation")
    private static synchronized UIResultCode getUIResultCode(IUIDisplay.ACTIVITY_ID activityId) {
        synchronized (lock1) {
            UIResultCode code = UNKNOWN;

            if (lastUIresponseMap != null && lastUIresponseMap.size() > 0 && lastUIresponseMap.containsKey(activityId)) {

                Bundle b = lastUIresponseMap.get(activityId).getUiExtras();
                int iThisUniqueId = b.getInt(uiUniqueId);

                // this checks that we have a response from the right activity
                // AND for info screens we have a response from the latest activity displayed as we don't want results from old invalid ones
                if (activityId != ACT_INFORMATION || iThisUniqueId >= iUniqueId) {
                    Timber.i("Screen IDs " + iThisUniqueId + ":" + iUniqueId + " " + "Activity:" + activityId.name() + ":" + System.currentTimeMillis());
                    code = BundleExtensionsKt.getSerializableCompat(b, uiResultCode, IUIDisplay.UIResultCode.class);
                }
            }

            if( code == UNKNOWN && Display.PosKeyPress != UNKNOWN ){
                code = Display.PosKeyPress;
                Timber.d("Sending in POS Keypress = " + code.name() );
                Display.PosKeyPress = UNKNOWN;
            }

            return code;
        }
    }

    private static synchronized boolean getUISelectChoice(IUIDisplay.ACTIVITY_ID activityId, String textName) {
        synchronized (lock1) {
            if (lastUIresponseMap != null && lastUIresponseMap.size() > 0 && lastUIresponseMap.containsKey(activityId)) {
                return lastUIresponseMap.get(activityId).getUiExtras().getBoolean(textName);
            } else {
                return false;
            }
        }
    }

    private static synchronized String getUIResultText(IUIDisplay.ACTIVITY_ID activityId, String textName) {
        synchronized (lock1) {
            if (lastUIresponseMap != null && lastUIresponseMap.size() > 0 && lastUIresponseMap.containsKey(activityId)) {
                return lastUIresponseMap.get(activityId).getUiExtras().getString(textName);
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static synchronized byte[] getUIResultByteArray(IUIDisplay.ACTIVITY_ID activityId, String textName) {
        synchronized (lock1) {
            if (lastUIresponseMap != null && lastUIresponseMap.size() > 0 && lastUIresponseMap.containsKey(activityId)) {
                return BundleExtensionsKt.getSerializableCompat(lastUIresponseMap.get(activityId).getUiExtras(), textName, byte[].class);
            } else {
                return null;
            }
        }
    }

    private static synchronized boolean getUIResultBoolean(IUIDisplay.ACTIVITY_ID activityId, String boolName) {
        synchronized (lock1) {
            if (lastUIresponseMap != null && lastUIresponseMap.size() > 0 && lastUIresponseMap.containsKey(activityId)) {
                return lastUIresponseMap.get(activityId).getUiExtras().getBoolean(boolName);
            } else {
                return false;
            }
        }
    }

    public byte[] getResultPinBlock(IUIDisplay.ACTIVITY_ID activityId) {
        return getUIResultByteArray(activityId, uiPinBlock);
    }

    // DIRTY way to make sure quick animations are displayed
    // gives them a second to render, before mmoving onto the next screen
    private void waitForAnimation(SCREEN_ICON iScreenIcon) {
        // TODO: wc not using animations on these screens at the moment so no delay
//        if (iScreenIcon == SCREEN_ICON.SUCCESS_ICON || iScreenIcon == SCREEN_ICON.ERROR_ICON ) {
//            getResultCode(ACT_INFORMATION, 2000);
//        }
    }




    public void showScreenImpl(String_id promptId, SCREEN_ICON screenIcon, ACTIVITY_ID id, SCREEN_ID screenId, String_id titleId, String_id buttonPromptId,
                               boolean dismissible, SCREEN_ICON additionalScreenIcon, int timeout, boolean block, HashMap<String, Object> mExtras,  String ... promptArgs) {
        Timber.d("showScreenImpl...screenID: %s, titleId: %s", uiScreenID, titleId);
        HashMap<String, Object> map = new HashMap<>();

        map.put(uiPromptId, promptId);
        map.put(uiScreenIcon, screenIcon);
        map.put(uiScreenID,  screenId);
        map.put(uiTitleId, titleId);
        map.put(uiButtonPromptId, buttonPromptId);
        map.put(uiUserDismissible, dismissible);
        map.put(uiScreenIcon2, additionalScreenIcon);
        map.put(uiScreenTimeout, timeout);
        if( promptArgs != null && promptArgs.length != 0 ) {
            map.put(uiPromptIdArg, promptArgs);
        }
        if( mExtras != null && mExtras.size()!= 0 ) {
            map.putAll(mExtras);
        }

        displayScreen(id, map);

        if (block) {
            getResultCode(ACT_INFORMATION, timeout);
        } else {
            waitForAnimation(screenIcon);
        }
        //displayScreen(def.id, def.screenId, def.screenIcon, def.titleId, def.promptId, def.dismissible);/* replace with above */
    }

    public void showScreen(UIScreenDef def, String ... promptArgs) {
        showScreenImpl(def.promptId, def.screenIcon, def.id, def.screenId, def.titleId, def.buttonPromptId, def.dismissible, def.additionalScreenIcon, def.timeout, def.block, null,  promptArgs);
    }

    public void showScreen(UIScreenDef def, HashMap<String, Object> mExtras, String ... promptArgs) {
        showScreenImpl(def.promptId, def.screenIcon, def.id, def.screenId, def.titleId, def.buttonPromptId, def.dismissible, def.additionalScreenIcon, def.timeout, def.block, mExtras, promptArgs);
    }

    public void showScreen(UIScreenDef def, String_id titleId, String... promptArgs) {
        showScreenImpl(def.promptId, def.screenIcon, def.id, def.screenId, titleId, def.buttonPromptId, def.dismissible, def.additionalScreenIcon, def.timeout, def.block, null, promptArgs);
    }

    public void showInputScreen(UIScreenDef def, HashMap<String, Object> mExtras, String ... promptArgs ){

        if (mExtras == null) {
            mExtras = new HashMap<>();
        }
        mExtras.put(IUIDisplay.uiInputHintId, def.hintId);
        mExtras.put(IUIDisplay.uiScreenMinLen, def.minLen);
        mExtras.put(IUIDisplay.uiScreenMaxLen, def.maxLen);

        showScreenImpl(def.promptId, def.screenIcon, def.id, def.screenId, def.titleId, def.buttonPromptId, def.dismissible, def.additionalScreenIcon, def.timeout, def.block, mExtras, promptArgs );
    }

    public void displayMainMenuScreen(){
        HashMap<String, Object> map = new HashMap<>();

        map.put(uiTitleId, String_id.STR_EMPTY);
        map.put(uiPromptId, String_id.STR_EMPTY);
        map.put(uiUserDismissible, false);
        map.put(uiScreenID, SCREEN_ID.MAIN_MENU);
        map.put(uiScreenIcon, NO_ICON);

        displayScreen(ACT_MAIN_MENU, map);
    }

    //========================================================================================================
    //  ActualInterface functions */
    // Converts the HashMap from Native Java to Android Bundle (Parcelable)
    // @param mExtras
    // @return
    //
    private Bundle generateUIBundle(HashMap<String, Object> mExtras) {
        Bundle uiBundle = new Bundle();
        int i, size = mExtras.size();

        for (i = 0; i < size; i++) {
            String name = (String) (mExtras.keySet().toArray())[i];
            Object obj = mExtras.get(name);
            if (obj != null) {

                if (obj.getClass() == String.class) {
                    uiBundle.putString((String) (mExtras.keySet().toArray())[i], (String) obj);
                } else if (obj.getClass() == Integer.class) {
                    uiBundle.putInt((String) (mExtras.keySet().toArray())[i], (Integer) obj);
                } else if (obj.getClass() == DisplayTableArray.class) {
                    uiBundle.putParcelable((String) (mExtras.keySet().toArray())[i], (Parcelable) obj);
                } else if (obj.getClass() == Boolean.class) {
                    uiBundle.putBoolean((String) (mExtras.keySet().toArray())[i], (Boolean) obj);
                } else if (obj.getClass() == SpannableStringBuilder.class) {
                    SpannableStringBuilder s = (SpannableStringBuilder)obj;
                    uiBundle.putString((String) (mExtras.keySet().toArray())[i], s.toString());
                } else {
                    String str = (String) (mExtras.keySet().toArray())[i];
                    uiBundle.putSerializable(str, (Serializable) obj);
                }
            }
        }

        return uiBundle;
    }

    public boolean getLedOne() { return ledOne; }
    public boolean getLedTwo() { return ledTwo; }
    public boolean getLedThree() { return ledThree; }
    public boolean getLedFour() { return ledFour; }

    private boolean ledOne = false;
    private boolean ledTwo = false;
    private boolean ledThree = false;
    private boolean ledFour = false;

    public void displayLed2(boolean one, boolean two, boolean three, boolean four) {
        ledOne = one;
        ledTwo = two;
        ledThree = three;
        ledFour = four;
    }

    private void displayScreen(ACTIVITY_ID iActivityID, HashMap<String, Object> mExtras) {
        Timber.d("displayScreen...iActivityID: %s", iActivityID.name());
        if (displayCallback != null) {
            Bundle uiExtras;

            setLastUIResponse(iActivityID, null);
            mExtras.put(uiUniqueId, ++iUniqueId);
            uiExtras = generateUIBundle(mExtras);
            displayCallback.DisplayCallback(new DisplayRequest(iActivityID, uiExtras));

        } else {
            Timber.e("Could not displayScreen as displayCallback was null!");
        }
    }

    public void getPin(SCREEN_ID iType, RSAPinKey rsaPinKey, String transactionName, String prompt, long amount, CountryCode cCode) {
        if (displayCallback != null) {
            Bundle uiExtras;
            setLastUIResponse(ACT_INPUT_PIN, null);
            HashMap<String, Object> map = new HashMap<>();

            map.put(uiScreenID, iType);
            map.put(uiScreenTitle, transactionName);
            map.put(uiScreenPrompt, prompt);

            map.put(uiScreenAmount, Currency.getInstance().formatAmount("" + amount, IUICurrency.EAmountFormat.FMT_AMT_FULL, cCode));
            Timber.i( "");
            map.put(uiUniqueId, ++iUniqueId);
            if (rsaPinKey != null) {
                map.put(uiRsaPinKey, rsaPinKey);
            }

            uiExtras = generateUIBundle(map);
            displayCallback.DisplayCallback(new DisplayRequest(ACT_INPUT_PIN, uiExtras));
        }
    }

    private void sendScreenSaverRequest(boolean disableScreensaver) {
        if(displayCallback != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(uiDisableScreensaver, disableScreensaver);
            displayCallback.DisplayCallback(new DisplayRequest(ACT_SCREEN_SAVER, generateUIBundle(map)));
        }
    }

    public void cancelScreenSaver() {
        sendScreenSaverRequest(true);
    }

    public void resetScreenSaver() {
        sendScreenSaverRequest(false);
    }

    // Suppressing the .wait function warning. This is common code used in get result code.
    @SuppressWarnings("java:S2274")
    private synchronized UIResultCode waitForResultCode(ACTIVITY_ID iActivityID) {

        UIResultCode uiResultCode = UNKNOWN;
        try {
            lock1.wait(100);
        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
            uiResultCode = ABORT;
        } catch (Exception ignored) {
            // Ignoring any exception here. Will be timeout exception etc.
        }

        if(Thread.currentThread().isInterrupted()) {
            Timber.d("Thread is interrupted");
            uiResultCode = ABORT;
        }

        if(uiResultCode == UNKNOWN) {
            uiResultCode = getUIResultCode(iActivityID);
        }

        return uiResultCode;
    }

    public UIResultCode getResultCode(ACTIVITY_ID iActivityID, int iTimeoutMS) {
        long startTime = System.currentTimeMillis();

        UIResultCode uiResultCode = getUIResultCode(iActivityID);
        if (iTimeoutMS > 0) {
            while (uiResultCode == UNKNOWN && (System.currentTimeMillis() - startTime) < iTimeoutMS) {
                uiResultCode = waitForResultCode(iActivityID);
            }
        } else if (iTimeoutMS == NO_TIMEOUT) {
            while (uiResultCode == UNKNOWN) {
                uiResultCode = waitForResultCode(iActivityID);
            }
        }

        if (uiResultCode != UNKNOWN) {
            Timber.i(uiResultCode.toString());
        } else {
            Timber.i("iTimeoutMS - %d, System.currentTimeMillis() - startTime = %d, forcing ABORT result code", iTimeoutMS, (System.currentTimeMillis() - startTime));
            uiResultCode = TIMEOUT;
        }

        return uiResultCode;
    }

    public boolean getSelectChoice(ACTIVITY_ID iActivityID, String textName) {
        return getUISelectChoice(iActivityID, textName);
    }

    public String getResultText(ACTIVITY_ID iActivityID, String textName) {
        return getUIResultText(iActivityID, textName);
    }

    public boolean getResultBoolean(ACTIVITY_ID iActivityID, String boolName) {
        return getUIResultBoolean(iActivityID, boolName);
    }

    public void sendResponse(DisplayRequest uiMessage, HashMap<String, Object> map) {
        if (map != null) {

            Bundle uiExtras;
            IUIDisplay.ACTIVITY_ID activityId = uiMessage.getActivityID();
            int iUniqueId = uiMessage.getUiExtras().getInt(uiUniqueId);
            map.put(uiUniqueId, iUniqueId);
            uiExtras = generateUIBundle(map);
            DisplayResponse uiResp = new DisplayResponse(uiExtras);
            uiResp.setIActivityID(activityId);
            setLastUIResponse(activityId, uiResp);


        }
    }

    @SuppressWarnings("deprecation")
    public void sendResponse(DisplayRequest uiMessage, IUIDisplay.UIResultCode uiResultCode, HashMap<String, Object> extraData) {
        if (uiMessage != null) {

            if (!uiMessage.isResponded()) {
                uiMessage.setResponded(true);
                HashMap<String, Object> map = new HashMap<>();
                IUIDisplay.SCREEN_ID iScreenId = BundleExtensionsKt.getSerializableCompat(
                        uiMessage.getUiExtras(), uiScreenID, IUIDisplay.SCREEN_ID.class);

                map.put(uiScreenID, iScreenId);
                map.put(IUIDisplay.uiResultCode, uiResultCode);
                map.putAll(extraData);

                sendResponse(uiMessage, map);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void sendResponse(DisplayRequest uiMessage, IUIDisplay.UIResultCode uiResultCode, String sResult1, String sResult2) {
        if (uiMessage != null) {

            if (!uiMessage.isResponded()) {
                uiMessage.setResponded(true);
                HashMap<String, Object> map = new HashMap<>();
                IUIDisplay.SCREEN_ID iScreenId = BundleExtensionsKt.getSerializableCompat(
                        uiMessage.getUiExtras(), uiScreenID, IUIDisplay.SCREEN_ID.class);

                map.put(uiResultText1, sResult1);
                map.put(uiResultText2, sResult2);
                map.put(uiScreenID, iScreenId);
                map.put(IUIDisplay.uiResultCode, uiResultCode);

                sendResponse(uiMessage, map);
            }
        }
    }

    public void displayPleaseWaitScreen(){
        HashMap<String, Object> map = new HashMap<>();
        map.put(uiScreenID, NOT_SET);
        map.put(uiScreenIcon, IN_PROGRESS);
        map.put(uiTitleId, String_id.STR_EMPTY);
        map.put(uiPromptId, String_id.STR_PLEASE_WAIT_BR);
        map.put(uiUserDismissible, false);
        displayScreen(ACT_INFORMATION_BASIC, map);
    }

}
