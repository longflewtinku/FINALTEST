package com.linkly.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.launcher.fragments.OutOfServiceFragment;
import com.linkly.launcher.fragments.UnattendedIdleScreenFragment;
import com.linkly.libconfig.MalConfig;

import timber.log.Timber;

/*
Four hidden buttons on each corner can be pressed in order, clockwise starting from the top
left in order to break out of this screen, the app then navigates to ServiceFrontEnd which itself
should have a mechanism to prompt for an Access Code and start a timer to force reboot.
Also upon breakout, this screen's background colour is set to the brand primary colour to provide
feedback to the user of successful breakout as navigation is slow. It could also help in situations
where auditing via security camera is necessary.

N.B.: Since the background of this Activity layout is used to provide touch feedback for the hidden
buttons, make sure that child Fragment layouts have a transparent background.

If in (Unattended Integrated) Kiosk Mode, then unattended_idlescreen_fragment is shown with the
kioskModeMessage set in ResApp, otherwise (and always when MPOS is in use) the
unattendedLockedDownMessage is shown.
 */
public class LockedDownActivity extends AppCompatActivity {
    private EscapeLockdownMechanism escapeLockdownMechanism;

    public static Intent buildStartIntent(Context context) {
        Intent intent = new Intent(context, LockedDownActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockeddown);
        ((UnattendedServiceModeAuthorizationHost)getApplication())
                .setUnattendedServiceModeAdminAccessGranted(false);

        LockedDownViewModel mViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.Factory.from(LockedDownViewModel.initializer)
        ).get(LockedDownViewModel.class);

        final Observer<Integer> statusBarColourObserver = newValue -> setupStatusBar();
        mViewModel.getCurrentBrandDisplayStatusBarColour().observe(this, statusBarColourObserver);
    }

    /***
     * If/when sitting on this screen and background broadcasts are received, it is necessary to run
     * the Fragment switching logic. Because ActIdle is started and quickly hides itself upon receipt
     * of APP_STATUS_INFO_REQUEST_EVENT and CONNECT_CONFIG, this onResume will be invoked again and
     * thus the Fragment switching logic can safely exist solely in onResume. If that were to change,
     * would need this logic to respond ad-hoc after resumed state.
     * Another scenario catered to here is when coming back from MPOS app.
     *
     * Note that Coming back from ServiceFrontEnd (Activity) should not be possible as a supported
     * function, however it is possible to see what seems to be that upon a crash (arrives here via
     * SplashActivity).
     */
    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume...");
        Boolean isPosConnected = ((LauncherApplication)getApplication()).isPOSConnected();
        Timber.d("...isPosConnected null (ambiguous state)?: %b", isPosConnected == null);
        Timber.d("...isPosConnected?: %b", isPosConnected);
        Timber.d("...isKioskMode?: %b", MalConfig.getInstance().getProfileCfg().isKioskMode());
        if (MalConfig.getInstance().getProfileCfg().isKioskMode()
            && isPosConnected != null && isPosConnected) {
            setupKioskModeDisplay();
        } else {
            setupOutOfServiceMessage();
        }
        setupBreakoutMechanism();
    }

    private void setupStatusBar() {
        Timber.d("setupStatusBar...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(BrandingConfig.
                    getBrandDisplayStatusBarColourOrDefault(getColor(R.color.color_linkly_primary)));
        }
    }

    // aka Unattended (Integrated) Idle Screen
    private void setupKioskModeDisplay() {
        Timber.d("setupKioskModeDisplay...");
        FragmentContainerView fragmentContainerView = findViewById(R.id.fcv_content);
        if (fragmentContainerView != null) {
            UnattendedIdleScreenFragment fragment = UnattendedIdleScreenFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .disallowAddToBackStack()
                    .replace(R.id.fcv_content, fragment)
                    .commit();
        }
    }

    private void setupOutOfServiceMessage() {
        Timber.d("setupOutOfServiceMessage...");
        FragmentContainerView fragmentContainerView = findViewById(R.id.fcv_content);
        if (fragmentContainerView != null) {
            OutOfServiceFragment fragment = OutOfServiceFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .disallowAddToBackStack()
                    .replace(R.id.fcv_content, fragment)
                    .commit();
        }
    }

    private void setupBreakoutMechanism() {
        Button topStartButton = findViewById(R.id.btn_topstart);
        Button topEndButton = findViewById(R.id.btn_topend);
        Button bottomStartButton = findViewById(R.id.btn_bottomstart);
        Button bottomEndButton = findViewById(R.id.btn_bottomend);
        ConstraintLayout root = findViewById(R.id.rootViewrootView);

        if (topStartButton != null && topEndButton != null && bottomStartButton != null && bottomEndButton != null) {
            if (root != null) {
                root.setBackgroundTintList(null);
            }
            topStartButton.setEnabled(true);
            topEndButton.setEnabled(true);
            bottomStartButton.setEnabled(true);
            bottomEndButton.setEnabled(true);
            escapeLockdownMechanism = new EscapeLockdownMechanism(4, new EscapeLockdownCallbacks() {
                @Override
                public void onEscapeFailed(int value) {
                    Timber.e("onEscapeFailed...value: %d.", value);
                }

                @Override
                public void onEscapeSucceeded() {
                    Timber.e("onEscapeSucceeded...");
                    topStartButton.setEnabled(false);
                    topEndButton.setEnabled(false);
                    bottomStartButton.setEnabled(false);
                    bottomEndButton.setEnabled(false);
                    ConstraintLayout root = findViewById(R.id.rootViewrootView);
                    if (root != null) {
                        root.setBackgroundTintList(ColorStateList.valueOf(
                                BrandingConfig.getInstance().getBrandDisplayButtonColour().getValue()
                        ));
                    }
                    Intent intent = new Intent(getApplicationContext(), ServiceFrontEnd.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    // NOTE: Since ServiceFrontEnd is started in its own Task, don't finish this
                    //  Activity, just leave it at the top of its stack (fail closed).
                }
            });
            topStartButton.setOnClickListener(new EscapeLockDownListener(1));
            topEndButton.setOnClickListener(new EscapeLockDownListener(2));
            bottomEndButton.setOnClickListener(new EscapeLockDownListener(3));
            bottomStartButton.setOnClickListener(new EscapeLockDownListener(4));
        }
    }

    class EscapeLockDownListener implements View.OnClickListener {
        private int buttonNumber;

        EscapeLockDownListener(int buttonNumber) {
            this.buttonNumber = buttonNumber;
        }

        @Override
        public void onClick(View view) {
            escapeLockdownMechanism.enterValue(buttonNumber);
        }
    }

    // Expects values to be input in incrementing sequence until the total.
    // If an entered number is out of sequence then run OnFailedEscapeCallback.
    // If the entered number satisfies the last expected number in the sequence then run onEscape
    //  callback.
    class EscapeLockdownMechanism {
        private static final int INITIAL_VALUE = 0;
        private int total = Integer.MAX_VALUE;  // fail closed
        private int lastValue = INITIAL_VALUE;
        private EscapeLockdownCallbacks callbacks;

        EscapeLockdownMechanism(int total, EscapeLockdownCallbacks callbacks) {
            this.total = total;
            this.callbacks = callbacks;
        }

        public void enterValue(int incoming) {
            Timber.d("enterValue...incoming: %d, last: %d", incoming, lastValue);
            if (incoming <= lastValue || incoming > lastValue + 1) {
                lastValue = INITIAL_VALUE;
                callbacks.onEscapeFailed(incoming);
                return;
            }
            if (incoming >= total) {
                lastValue = INITIAL_VALUE;
                callbacks.onEscapeSucceeded();
                return;
            }
            lastValue = incoming;
        }
    }

    interface EscapeLockdownCallbacks {
        void onEscapeFailed(int value);

        void onEscapeSucceeded();
    }
}
