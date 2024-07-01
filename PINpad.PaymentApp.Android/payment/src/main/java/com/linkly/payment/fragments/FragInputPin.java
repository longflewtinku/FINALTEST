package com.linkly.payment.fragments;

import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_PINLISTENER.P2P_EMV_PINLISTENER_NOT_SET;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_PINLISTENER.P2P_EMV_PINLISTENER_OFFLINE_PIN;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_PINLISTENER.P2P_EMV_PINLISTENER_ONLINE_PIN;
import static com.linkly.libsecapp.IP2PSec.InstalledKeyType.AS2805;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT_PIN;
import static com.linkly.libui.IUIDisplay.SCREEN_ID.OFFLINE_PIN;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.kotlinandroidtoolkit.extensionfuncs.BundleExtensionsKt;
import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.env.Stan;
import com.linkly.libmal.MalFactory;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.Util;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.libui.speech.SpeechUtils;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.viewmodel.FragStandardViewModel;
import com.pax.dal.entity.EKeyCode;

import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public class FragInputPin extends BaseFragment<ActivityTransBinding, FragStandardViewModel> implements ViewTreeObserver.OnWindowFocusChangeListener {

    public static final String TAG = FragInputPin.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;
    private TextView pwdTv;
    private AtomicBoolean isFirstStart = new AtomicBoolean(true);
    private FragInputPin.EEnterPinType enterPinType;
    private Handler handler = new Handler(Looper.myLooper());
    private Thread pinEntry = null;
    private AtomicBoolean pinInit = new AtomicBoolean(false);

    enum PinResult { TIMEOUT, ABORT, OK }

    public static FragInputPin newInstance() {
        Bundle args = new Bundle();
        FragInputPin fragment = new FragInputPin();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_inputpin;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_INPUT_PIN);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        ActScreenSaver.cancelScreenSaver();
        SetHeader(false, false);
        initViews(v);
        if( v != null ) {
            v.getViewTreeObserver().addOnWindowFocusChangeListener( this );
        } else {
            Timber.e("View v is null" );
        }
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.i("On View Created");
        handler = new Handler(Looper.myLooper());

        pwdTv = view.findViewById(R.id.pwd_input_text);
        if (pwdTv != null) {
            pwdTv.setHint(Engine.getDep().getPrompt(String_id.STR_PIN)+"#");
            pwdTv.setInputType(InputType.TYPE_NULL);
            pwdTv.setFocusable(false);
            pwdTv.setFocusableInTouchMode(false);
            pwdTv.setText("");
        }
        TransRec curTrans = Engine.getDep().getCurrentTransaction();
        TextView surchargeSubtitle = view.findViewById(R.id.inc_surcharge);
        if (curTrans.getAmounts().getSurcharge() > 0) {
            surchargeSubtitle.setVisibility(View.VISIBLE);
        } else {
            surchargeSubtitle.setVisibility(View.GONE);
        }
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        Timber.i("onWindowFocusChanged");
        if (hasFocus) {
            runPinEntry();
        }
    }

    public void startAccessModeText() {
        TransRec curTrans = Engine.getDep().getCurrentTransaction();
        if (curTrans != null && curTrans.getAudit().isAccessMode()) {
            SpeechUtils.getInstance().setSpeechRate((float) 0.9);
            String sentence = UI.getInstance().getPrompt( String_id.STR_ACCESS_MODE_PIN_TEXT);
            String[] splitSpeech = sentence.split("\\.");

            for ( String speech : splitSpeech ) {
                SpeechUtils.getInstance().speak( speech, 500 );//500ms
            }
        }
    }

    public void runPinEntry() {

        TransRec curTrans = Engine.getDep().getCurrentTransaction();
        if (curTrans != null && curTrans.getAudit().isAccessMode()) {
            MalFactory.getInstance().getHardware().setAccessMode(curTrans.getAudit().isAccessMode());
        }

        if (isFirstStart.get()) {
            startAccessModeText();
            isFirstStart.set(false);
            if (enterPinType == FragInputPin.EEnterPinType.ONLINE_PIN) {
                enterOnlinePin();
            } else if (enterPinType == FragInputPin.EEnterPinType.OFFLINE_PIN) {
                enterOfflinePin();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        MalFactory.getInstance().getHardware().getDal().getPed().setInputPinListener( null );
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.i("onStart");
        runPinEntry();
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayRequest d = fragStandardViewModel.getDisplay().getValue();
        Timber.i("Title:" + d.getActivityID().name() + " Prompt:" + d.getUiExtras().getString(IUIDisplay.uiScreenPrompt) + " UID: " + d.getUiExtras().getInt(IUIDisplay.uiUniqueId));
    }

    // Override for the base class to allow the sending of the on pause.
    @Override
    public boolean allowOnPauseResponse() {
        Timber.i("allowOnPauseResponse: " + pinInit.get());
        return pinInit.get();
    }

    @Override
    public void onPause() {
        if( getView() != null ) {
            getView().getViewTreeObserver().removeOnWindowFocusChangeListener( this );
        }
        SpeechUtils.getInstance().stop();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Timber.i("onDestroyView");
        P2PLib.getInstance().getIP2PEmv().setEmvPinListener(null, P2P_EMV_PINLISTENER_NOT_SET);
        if(pinEntry != null) {
            pinEntry.interrupt();
        }

        handler = null;
        pwdTv = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Timber.i("onDestroy");
        if(pinEntry != null) {
            pinEntry.interrupt();
        }
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    protected void initViews(View v) {

        /*Get the Values from the Message */
        String title = getDisplayRequest().getUiExtras().getString(IUIDisplay.uiScreenTitle);
        String prompt = getDisplayRequest().getUiExtras().getString(IUIDisplay.uiScreenPrompt);
        IUIDisplay.SCREEN_ID iScreenId = BundleExtensionsKt.getSerializableCompat(
                getDisplayRequest().getUiExtras(), IUIDisplay.uiScreenID, IUIDisplay.SCREEN_ID.class);

        if (iScreenId == OFFLINE_PIN) {
            enterPinType = FragInputPin.EEnterPinType.OFFLINE_PIN;
        } else {
            enterPinType = FragInputPin.EEnterPinType.ONLINE_PIN;
        }

        /*Set the Screen Title*/
        TextView titleTv = v.findViewById(R.id.header_title);
        TextView amountWord = v.findViewById(R.id.amount);
        TextView symTv = v.findViewById(R.id.amount_sym);
        TextView amountTv = v.findViewById(R.id.amount_txt);
        RelativeLayout amountLayout = v.findViewById(R.id.trans_amount_layout);
        TextView promptTv1 = v.findViewById(R.id.prompt_title);


        TransRec curTrans = Engine.getDep().getCurrentTransaction();
        if ( curTrans != null && curTrans.getAudit().isAccessMode()) {
            if (titleTv != null)     titleTv.setVisibility(View.GONE);
            if (amountWord != null)  amountWord.setVisibility(View.GONE);
            if (symTv != null)       symTv.setVisibility(View.GONE);
            if (amountTv != null)    amountTv.setVisibility(View.GONE);
            if (amountLayout != null)amountLayout.setVisibility(View.GONE);
            if (promptTv1 != null)   promptTv1.setVisibility(View.GONE);
            return;
        }

        /* Set the Screen Title */
        if (titleTv != null) {
            titleTv.setText(title);
        }

        if ( amountWord != null && Engine.getDep() != null ) {
            String amount = Engine.getDep().getPrompt( String_id.STR_AMOUNT ) + ":";
            amountWord.setText( amount );
        }

        if (curTrans != null) {

            /*Display the Transaction Amount */
            if (curTrans.getAmounts().getAmountUserEntered() != null && curTrans.getAmounts().getAmountUserEntered().length() > 0) {

                long totalAmount = curTrans.getAmounts().getTotalAmount();
                String strAmount = String.valueOf(totalAmount);

                if (symTv == null) {
                    String amt = UI.getInstance().getCurrency().formatUIAmount(strAmount, IUICurrency.EAmountFormat.FMT_AMT_FULL, Engine.getDep().getPayCfg().getCountryCode());
                    amountTv.setText(amt);
                } else {
                    String amt = UI.getInstance().getCurrency().formatUIAmount(strAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, Engine.getDep().getPayCfg().getCountryCode());
                    amountTv.setText(amt);
                }
            } else {
                /*Hide it the text but keep the space*/
                amountLayout.setVisibility(View.GONE);
            }
        } else {
            amountLayout.setVisibility(View.GONE);
        }

        if (promptTv1 != null)
            promptTv1.setText(prompt);

        pwdTv = v.findViewById(R.id.pwd_input_text);
        if (pwdTv != null) {
            pwdTv.setHint(Engine.getDep().getPrompt(String_id.STR_PIN)+"#");
            pwdTv.setInputType(InputType.TYPE_NULL);
            pwdTv.setFocusable(false);
            pwdTv.setFocusableInTouchMode(false);
            pwdTv.setText("");
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Timber.i("Abort on Key");
            sendResponse(IUIDisplay.UIResultCode.ABORT, "", "");
            return true;
        }

        return super.onKey(v, keyCode, event);
    }

    public void setContentText(final String content) {
        if (handler != null) {
            handler.post( () -> {
                if (pwdTv != null) {
                    pwdTv.setText(content);
                }
            } );
        }
    }

    /**
     * Clears the last character text box & sets the text
     * @return string masked
     * */
    private String clearLastChar(){
        if( pwdTv != null && this.pwdTv.getText().length() > 0 ){
            String temp = this.pwdTv.getText().toString();

            return temp.substring( 0, temp.length() - 1 );
        }
        return "";
    }

    private void enterOnlinePin() {

        pinEntry = new Thread( () -> {
            try {
                P2PLib.getInstance().getIP2PEmv().setEmvPinListener( arg0 -> {
                    String temp = "";
                    if (arg0 == EKeyCode.KEY_CLEAR) {
                        temp = this.clearLastChar();
                    } else if (arg0 == EKeyCode.KEY_ENTER || arg0 == EKeyCode.KEY_CANCEL) {
                        // do nothing
                        return;
                    } else {
                        if( pwdTv.getText() != null ) {
                            temp = pwdTv.getText().toString();
                            temp += "*";
                        }
                    }
                    setContentText(temp);
                }, P2P_EMV_PINLISTENER_ONLINE_PIN );
                Util.Sleep(550); /* workaround for pax kernel delay in getting pin listeners ready DO NOT REMOVE */
                boolean pinResult;
                int res = -1;
                TransRec curTrans = Engine.getDep().getCurrentTransaction();
                int pinEntryTimeout = Engine.getDep().getPayCfg().getUiConfigTimeouts().
                        getTimeoutMilliSecs(
                                ConfigTimeouts.CARD_PIN_ENTRY_TIMEOUT,
                                curTrans.getAudit().isAccessMode()
                        );
                Timber.i("Pin Timeout: %d", pinEntryTimeout);
                pinInit.set(true);
                if( P2PLib.getInstance() != null ) {

                    final String PIN_LENGTHS_ALLOWED = "4,5,6,7,8,9,10,11,12";

                    // work out key mgmt method to use
                    IP2PSec.InstalledKeyType keyTypeToUse = P2PLib.getInstance().getIP2PSec().getInstalledKeyType();

                    switch(keyTypeToUse) {
                        case MS:
                            pinResult = P2PLib.getInstance().getIP2PSec().getPinBlock( ( byte ) IP2PSec.KeyGroup.TRANS_GROUP.getKeyIndex(), PIN_LENGTHS_ALLOWED, pinEntryTimeout, null );
                            break;
                        case DUKPT:
                            // capture PIN without encrypting. No KSN is assigned here, it's assigned at message pack/send time
                            pinResult = P2PLib.getInstance().getIP2PSec().getPinNoEncrypt(PIN_LENGTHS_ALLOWED, pinEntryTimeout);
                            break;
                        case AS2805:
                            pinResult = false;
                            curTrans.getProtocol().setStan(Stan.getNewValue());
                            res = P2PLib.getInstance().getIP2PSec().as2805GetPinBlock( false, PIN_LENGTHS_ALLOWED, curTrans.getProtocol().getStan(), (int)curTrans.getAmounts().getTotalAmount(), pinEntryTimeout, null );
                            break;
                        default:
                            pinResult = false;
                            break;
                    }

                    if (!keyTypeToUse.equals(AS2805)) {
                        if (!pinResult) {
                            Timber.i("No Pin Supplied Abort");
                            P2PLib.getInstance().getIP2PSec().cancelPinEntry();
                            sendResponse(IUIDisplay.UIResultCode.ABORT, null, null);
                        } else {
                            sendResponse(IUIDisplay.UIResultCode.OK, null, null);
                        }
                    } else {
                        if (res == PinResult.TIMEOUT.ordinal()) {
                            sendResponse(IUIDisplay.UIResultCode.TIMEOUT, null, null);
                        } else if(res == PinResult.ABORT.ordinal()) {
                            sendResponse(IUIDisplay.UIResultCode.ABORT, null, null);
                        } else {
                            sendResponse(IUIDisplay.UIResultCode.OK, null, null);
                        }
                    }
                }

            } catch (Exception e) {
                Timber.w(e);
                P2PLib.getInstance().getIP2PSec().cancelPinEntry();
                sendResponse(IUIDisplay.UIResultCode.ABORT, null, null);
            }
        } );

        pinEntry.start();
    }

    public void enterOfflinePin() {
        pinEntry = new Thread( () -> {

            try {

                Timber.d("PIN: emvStartPinListener setInputPinListener");

                P2PLib.getInstance().getIP2PEmv().setEmvPinListener( arg0 -> {
                    String temp = "";
                    Timber.d("PIN: onKeyEvent:" + arg0.name());

                    if (arg0 == EKeyCode.KEY_CLEAR) {
                        temp = this.clearLastChar();
                    } else if (arg0 == EKeyCode.KEY_ENTER) {
                        if ( pwdTv.getText() != null && pwdTv.getText().length() >= 4 ) {
                            sendResponse( IUIDisplay.UIResultCode.OK, null, null );
                        } else if ( pwdTv.getText() != null && pwdTv.getText().length() == 0 ) {
                            sendResponse( IUIDisplay.UIResultCode.BYPASSED, null, null);
                        }
                        return;
                    } else if (arg0 == EKeyCode.KEY_CANCEL) {
                        Timber.i("KEY_CANCEL Supplied Abort");
                        sendResponse(IUIDisplay.UIResultCode.ABORT, null, null);
                        // do nothing
                        return;
                    } else if (arg0 == EKeyCode.NO_KEY) {
                        return;
                    } else {
                        if( pwdTv.getText() != null ) {
                            temp = pwdTv.getText().toString();
                            temp += "*";
                        }
                    }
                    setContentText(temp);
                }, P2P_EMV_PINLISTENER_OFFLINE_PIN );

                sendResponse(IUIDisplay.UIResultCode.READY, null, null);

            } catch (Exception e) {
                Timber.w(e);
                /* sendTCP back result and finish task */
                sendResponse(IUIDisplay.UIResultCode.ABORT, null, null);
            }
        } );

        pinEntry.start();
    }

    public enum EEnterPinType {ONLINE_PIN, OFFLINE_PIN}

}





