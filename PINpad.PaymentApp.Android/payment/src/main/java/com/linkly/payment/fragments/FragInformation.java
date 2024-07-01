package com.linkly.payment.fragments;

import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;

import android.graphics.drawable.Animatable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.kotlinandroidtoolkit.extensionfuncs.BundleExtensionsKt;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libui.IUIDisplay;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActTransaction;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.viewmodel.FragInfoViewModel;
import com.linkly.payment.viewmodel.data.UIFragData;

import timber.log.Timber;

public class FragInformation extends BaseFragment<ActivityTransBinding, FragInfoViewModel> {

    public static final String TAG = FragInformation.class.getSimpleName();
    private FragInfoViewModel fragInfoViewModel;
    FragLed ctls_leds;
    private ToneGenerator toneG;

    public static FragInformation newInstance() {
        Bundle args = new Bundle();
        FragInformation fragment = new FragInformation();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragInfoViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_information;
    }

    @Override
    public FragInfoViewModel getViewModel() {
        fragInfoViewModel = ViewModelProviders.of(this).get(FragInfoViewModel.class);
        fragInfoViewModel.init(ACT_INFORMATION);
        return fragInfoViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        if (!fragInfoViewModel.getFragData().hasActiveObservers()) {
            fragInfoViewModel.getFragData().observe(getViewLifecycleOwner(), this::onDisplayChanged);
        }
        return v;
    }

    DisplayKiosk.NavigationBarState state;

    @Override
    public void onPause() {
        super.onPause();

        FragLed.pauseLedTimer();
        if (fragInfoViewModel.getFragData().hasObservers())
            fragInfoViewModel.getFragData().removeObserver(this::onDisplayChanged);
    }

    @Override
    public void onResume() {

        super.onResume();
        if (fragInfoViewModel.getDisplay().getValue() != null) {
            showScreen(fragInfoViewModel.getFragData().getValue());
            FragLed.startLedTimer(ctls_leds);
        }

        state = new DisplayKiosk.NavigationBarState();
        if(fragInfoViewModel.getFragData().getValue().enableBackButton.getValue()) {
            DisplayKiosk.getInstance().onResume(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        FragLed.pauseLedTimer();
        if (fragInfoViewModel.getFragData().hasObservers())
            fragInfoViewModel.getFragData().removeObserver(this::onDisplayChanged);
        if (toneG != null) {
            toneG.release();
        }
    }


    @VisibleForTesting
    public void onDisplayChanged(UIFragData fragData) {

        showScreen(fragData);
        fragInfoViewModel.getFragData().removeObserver(this::onDisplayChanged);
    }

    public void showScreen(UIFragData fragData) {



        SetHeader(false, false);
        ctls_leds = (FragLed) this.getChildFragmentManager().findFragmentById(R.id.ctls_leds_frag);

        super.showScreen();

        if (fragInfoViewModel.getDisplay().getValue() != null) {
            IUIDisplay.SCREEN_ICON infoIcon = fragData.getInfoIcon().getValue();
            setErrorBeeps(infoIcon);
            setCtlsLeds(infoIcon);
            setButton(infoIcon, fragData.buttonPrompt.getValue());
        }
    }


    private void setErrorBeeps(IUIDisplay.SCREEN_ICON infoIcon) {
        if (infoIcon == IUIDisplay.SCREEN_ICON.ERROR_ICON_WITH_BEEP) {
            playErrorBeeps();
        }
    }
    private void setCtlsLeds(IUIDisplay.SCREEN_ICON infoIcon) {
        if (infoIcon == IUIDisplay.SCREEN_ICON.ERROR_ICON) {
            if (ctls_leds != null && ctls_leds.getView() != null) {
                ctls_leds.getView().setVisibility(View.INVISIBLE);
                Timber.i("CTLS LEDS INVISIBLE");
            }
        }
    }

    private void setButton(IUIDisplay.SCREEN_ICON infoIcon, String prompt) {
        Button button = getView().findViewById(R.id.button);
        if (button != null) {
            if (infoIcon == IUIDisplay.SCREEN_ICON.BUTTON_ICON) {
                button.setVisibility(VISIBLE);
                button.setText(prompt);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendResponse(IUIDisplay.UIResultCode.OK, "", "");
                    }
                });
                LinearLayout bgColour = getView().findViewById(R.id.bg_color);
                if (bgColour != null) {
                    bgColour.setVisibility(GONE);
                }
            } else {
                button.setVisibility(GONE);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void setNewBackground() {

        IUIDisplay.SCREEN_ICON infoIcon = BundleExtensionsKt.getSerializableCompat(
                fragInfoViewModel.getDisplay().getValue().getUiExtras(),
                IUIDisplay.uiScreenIcon,
                IUIDisplay.SCREEN_ICON.class);

        ImageView mImgProcessing = getView().findViewById(R.id.imageProcessing);
        ImageView mImgCheck = getView().findViewById(R.id.imageCheck);

        if (mImgProcessing == null || mImgCheck == null)
            return;

        mImgProcessing.clearAnimation();
        mImgCheck.clearAnimation();

        if (infoIcon == IUIDisplay.SCREEN_ICON.SUCCESS_ICON) {
            Animatable d = (Animatable) mImgCheck.getDrawable();
            if (d != null)
                d.start();
        } else if (infoIcon == IUIDisplay.SCREEN_ICON.PROCESSING_ICON) {
            Animation rotation = AnimationUtils.loadAnimation(getBaseActivity(), R.anim.clockwise_rotation);
            mImgProcessing.startAnimation(rotation);
        }
    }

    @SuppressWarnings("deprecation")
    void playErrorBeeps() {
        int durationMS = 500;
        if (!CoreOverrides.get().isSpoofComms()) {
            //This Makes it Beep
            try {
                toneG = new ToneGenerator( AudioManager.STREAM_ALARM, 100 );
                toneG.startTone( ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, durationMS );
            } catch ( Exception e ){
                Timber.w(e);
            }
        }
        //This Makes it Vibrate.
        Vibrator v = (Vibrator) getBaseActivity().getSystemService(ActTransaction.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(200, DEFAULT_AMPLITUDE));
        } else { v.vibrate(durationMS); }
    }
}


