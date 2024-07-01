package com.linkly.payment.activities;

import static android.view.KeyEvent.KEYCODE_ENTER;
import static com.linkly.libpositive.messages.IMessages.LOGIN_RESULT;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.Keyboard;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.linkly.libengine.action.user_action.MainMenu;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.users.UserManager;
import com.linkly.libengine.workflow.WorkflowAddActions;
import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIKeyboard;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.payment.R;
import com.linkly.payment.application.StartupSequence;
import com.linkly.payment.utilities.UIUtilities;
import com.linkly.payment.viewmodel.BaseViewModel;

import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public abstract class BaseActivity<T extends ViewDataBinding, V extends BaseViewModel> extends AppCompatActivity
        implements View.OnKeyListener {

    // TODO FIXME fallback is a liability. Wonder why something duidn't display when it should? Might have been the fallback.
    //  This is architecture smell, it shouldn't need to exist and the solution means an architectural overhaul as this is a bandaid
    //  to fix unknown probelm(s) in other parts of the framework or its usage.
    // Combined with BaseActivity, which easily becomes a necessity for all Activities to extend, an Activity can be pulled into
    //  this strange band-aid's vortex.
    public static final int BASE_ACTIVITY_DEF_TIMEOUT = 3000; /* timeout when we have finished one fragment and there is no new ones coming in (basically a fallback abort and finish) */
    public boolean cancelDisabled = false;
    private static boolean onDisplay = false;
    private static Timer activityTimeout;

    private boolean deviceCharging = false;
    private T mViewDataBinding;
    private V mViewModel;

    public abstract int getBindingVariable(); // Override for set binding variable
    public abstract @LayoutRes int getLayoutId();
    public abstract V initViewModel();
    public abstract V getViewModel();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(StartupSequence.checkAppConfiguredPostCrash(this, false)) {
            Timber.e("Quickly finishing due to app not configured!");
            this.finishAfterTransition();
            return;
        }
        performDataBinding();

        IDependency d = Engine.getDep();
        if (d != null) {
            getWindow().setStatusBarColor(d.getPayCfg().getBrandDisplayStatusBarColourOrDefault(getColor(R.color.color_linkly_primary)));
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = MalFactory.getInstance().getMalContext().registerReceiver(null, filter);
        if(intent != null) {

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isPluggedIntoPowerSource = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;

            deviceCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL || isPluggedIntoPowerSource;
        }
        if (deviceCharging || this instanceof ActTransaction ) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if( d != null ) {
            boolean isScreenshotAllowed = d.getProfileCfg().isEnableScreenshot();
            Timber.i("IsScreenshot allowed : %b", isScreenshotAllowed);
            if (isScreenshotAllowed) {
                //User should able to take screenshot
                MalFactory.getInstance().getHardware().enablePowerKey(true);
            } else {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            }
        }
    }

    @Override
    @Deprecated
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        UIUtilities.checkPermissionAgain(this);
    }

    private void performDataBinding() {
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId());
        this.mViewModel = mViewModel == null ? initViewModel() : mViewModel;
        if (this.mViewModel != null) {
            mViewDataBinding.setVariable(getBindingVariable(), mViewModel);
            mViewDataBinding.executePendingBindings();
        }
    }

    public DisplayRequest getDisplayRequest() {
        return getViewModel().getDisplay().getValue();
    }

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if (intent != null) {
                String action = intent.getAction();

                if(action != null) {
                    Bundle extras = intent.getExtras();

                    switch (action) {
                        case IMessages.APP_CHARGING_EVENT:
                            if(extras != null)
                                deviceCharging = extras.getBoolean("Charging");
                            break;
                        case IMessages.APP_DISABLE_CANCEL_EVENT:
                            if(extras != null) {
                                cancelDisabled = extras.getBoolean("CancelDisabled");
                                Timber.i("Cancel Disabled = %b", cancelDisabled);
                            }
                            break;
                        case IMessages.APP_REFRESH_SCREEN_EVENT:
                            refreshUI();
                            break;
                        case IMessages.APP_FINISH_UI_EVENT:
                            finishUI();
                            break;
                        case IMessages.APP_FINISH_TRANSACTION_EVENT:
                            finishTransaction();
                            break;
                        // TODO FIXME too many ways to finish BaseActivity, which can be
                        //  devastating.
                        case LOGIN_RESULT:
                            int resultCode = intent.getExtras().getInt("Result");
                            if (resultCode == Activity.RESULT_CANCELED) {
                                finishAfterTransition();
                            }
                            break;
                    }
                }
            }
        }
    };

    protected void refreshUI () { } // does nothing by default, not used by anything but triggered by IMessage.APP_REFRESH_SCREEN_EVENT
    protected void finishUI () { } // does nothing by default, but is overridden by ActMainMenu, so do not delete
    protected void finishTransaction () { } // does nothing by default, but is overridden by ActMainMenu, so do not delete

    @Override
    protected void onPause() {
        super.onPause();
        onDisplay = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deviceCharging) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (EFTPlatform.isAppHidden()) {
            Timber.i("Move task from BaseActivity");
            moveTaskToBack(true);
        }

        if (EFTPlatform.hasPhysicalKeyboard()) {
            RelativeLayout kb = findViewById(R.id.keyRelative);
            if (kb != null) {
                kb.setVisibility(View.GONE);
            }
        }
        /* new screen so no need to timeout */
        cancelActivityTimeout();
        onDisplay = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
        onDisplay = false;
        cancelActivityTimeout();
    }

    public void RegisterAsExtendedActivity(String action) {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver, new IntentFilter(action));
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    // the app should be hidden if it is displaying default screens like the login or main menu
    // AND the app has previously been launched with an automated task that set the hideWhenDOne flags
    public void checkHideWhenDone() {
        if (UserManager.getActiveUser() == null && EFTPlatform.startupParams.hideWhenDone && AppMain.isForeground()) {
            Timber.i("Move task from BaseActivity checkHideWhenDone");
            moveTaskToBack(true);
            Timber.i("Hidden App");
        }
    }

    // This is the override for the System Softkey
    @Override
    @Deprecated
    public void onBackPressed() {
        // All callbacks registered via (OnBackPressedCallback) addCallback are evaluated upon
        //  this super call...but it is not working for some reason!
        // TODO FIXME targeting Android 13+ will need OnBackPressedCallback working.
        if (getDisplayRequest() == null || !getDisplayRequest().getUiExtras().getBoolean(IUIDisplay.uiScreenDisableBack)) {
            getViewModel().sendResponse(IUIDisplay.UIResultCode.ABORT, "Back Pressed", "");
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (MalFactory.getInstance().getHardware().processVolumeKey(getApplicationContext(), event))
            return true;

        return super.dispatchKeyEvent(event);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (keyCode == KEYCODE_ENTER) {
                //Call the Done Call back if provided
                Timber.i("[ACTI DONE  ]%s", this.getClass().toString());
                ((IUIKeyboard.OnDoneClickedListener) this).onDoneClicked();
            } else if (keyCode == Keyboard.KEYCODE_CANCEL) {
                //Call the Done Call back if provided
                ((IUIKeyboard.OnCancelClickedListener) this).onCancelClicked();
            }
        }

        return true;
    }

    public void finishOnTimeout(int timeout) {
        setActivityTimeout(timeout);
    }

    public void returnToMainMenu() {
        Timber.d("returnToMainMenu...");
        boolean autoTrans = false;
        IDependency d = Engine.getDep();
        if (d != null) {
            TransRec trans = d.getCurrentTransaction();
            if (trans != null && trans.getTransType().autoTransaction) {
                autoTrans = true;
            }
        }

        if (this instanceof ActTransaction && !cancelDisabled && !autoTrans) {
            WorkflowScheduler.getInstance().queueWorkflow(new WorkflowAddActions(new MainMenu()), false, true, false);
            Timber.i("Cancel disabled NOT SET");
        }
    }

    public void setActivityTimeout(int activityTimeout) {
        Timber.d("setActivityTimeout...in millis: %d", activityTimeout);
        if (activityTimeout > 0) {
            BaseActivity.activityTimeout = new Timer();
            BaseActivity.activityTimeout.schedule(new TimerTask() {
                @Override
                public void run() {
                Timber.d("ActivityTimeout RUNNING!");
                    if (onDisplay)
                        returnToMainMenu();
                }
            }, activityTimeout);
        }
    }
    public void cancelActivityTimeout() {
        if (activityTimeout != null) {
            activityTimeout.cancel();
            activityTimeout = null;
        }
    }

    public T getMViewDataBinding() {
        return this.mViewDataBinding;
    }

}