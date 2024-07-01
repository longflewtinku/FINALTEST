package com.linkly.payment.activities;

import static com.linkly.libpositive.messages.IMessages.LOGIN_RESULT;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.linkly.libengine.action.user_action.MainMenu;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.users.UserManager;
import com.linkly.libengine.workflow.Startup;
import com.linkly.libengine.workflow.WorkflowAddActions;
import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.platform.Platform;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.payment.BuildConfig;
import com.linkly.payment.R;
import com.linkly.payment.application.StartupSequence;
import com.linkly.payment.fragments.FragHeader;
import com.linkly.payment.utilities.AutoSettlementWatcher;
import com.linkly.payment.utilities.Pci24HourRebootWatcher;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Since the Application is tightly coupled with this Activity and considers it "the application
 *  Activity", it is expected that the launched Activity remains in the task stack and can be brought
 *  to foreground when needed.
 */
public class ActIdle extends AppCompatActivity {

    private static boolean onDisplay = false;
    EFTActivityProcessor task;
    ScheduledExecutorService executor;
    private static long lastStuckTime = 0;

    private DisplayKiosk.NavigationBarState state;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int resultCode = intent.getExtras().getInt("Result");

            if (resultCode == Activity.RESULT_CANCELED) {
                if (UserManager.getActiveUser() == null || UserManager.isAutoUserLogin(Engine.getDep(), context.getApplicationContext()) && EFTPlatform.getPlatform() == Platform.PlatformType.PaxTerminal) {
                    Timber.i("EXIT REQUEST");
                    // TODO FIXME relying on ActIdle sitting in the task stack.
                    // A main use-case for EXIT is physical CANCEL button. This mechanism assumes that
                    //  is coming from an Activity that needs to be cancelled (which is no longer
                    //  the case for FragInputAmountIdle, so its special-case is handled in ActMainMenu).
                    Intent intentHome = new Intent(Intent.ACTION_MAIN);
                    intentHome.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intentHome);
                } else {
                    Timber.i("LOGOUT REQUEST ");
                    UserManager.logoutActiveUser();

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate...");
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .permitDiskReads() // suppresses spamming in logcat
                    .permitDiskWrites() // suppresses spamming in logcat
                    .build());
        }

        super.onCreate(savedInstanceState);


        if(StartupSequence.checkAppConfiguredPostCrash(this, true)) {
            Timber.e("Quickly finishing due to app not configured!");
            this.finishAfterTransition();
            return;
        }

        // Set status bar color
        resetLastStuckTime();
        setContentView(R.layout.activity_idle);
        IDependency d = Engine.getDep();
        if (d != null) {
            getWindow().setStatusBarColor(d.getPayCfg().getBrandDisplayStatusBarColourOrDefault(getColor(R.color.color_linkly_primary)));
        }
        MalFactory.getInstance().initialiseFiles(this);



        //Hide MainMenu Drop down - header_fragment
        FragHeader header = (FragHeader) this.getSupportFragmentManager().findFragmentById(R.id.headlines_fragment);
        header.setDropDownVisibility(false);
        header.setHeaderBarVisibility(false);
        if (d != null && d.getCustomer().hideBrandDisplayLogoHeader()) {
            header.setCustomerLogoVisibility(false);
        }

        //Hide the Soft Keys, we don't want the user to be able to do anything when this screen is showing
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver, new IntentFilter(LOGIN_RESULT));

        TextView textPleaseWait = findViewById(R.id.infoPrompt);
        if(d == null) {
            d = Engine.getDep();
        }
        if(d != null) {
            textPleaseWait.setText(d.getPrompt(String_id.STR_PLEASE_WAIT_BR));
        }
        AppMain.getApp().setAppActivity(this);

        Timber.i("onCreate");
        EFTPlatform.configureFromIntent(getIntent());

        Pci24HourRebootWatcher.init(d.getPayCfg());
        AutoSettlementWatcher.init(getApplicationContext(), d.getPayCfg());

        if (EFTPlatform.isAppHidden()) {
            moveTaskToBack(true);
        }

        WorkflowScheduler.getInstance().queueWorkflow(new Startup(), true, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume...");

        if (EFTPlatform.isAppHidden()) {
            Timber.i("Move task to back from ActIdle onResume as app is hidden");
            moveTaskToBack(true);
        }
        else if (UserManager.getActiveUser() == null && EFTPlatform.startupParams.hideWhenDone && AppMain.isForeground() && StartupSequence.isInitialised() ) {
            Timber.i("Move task to back from ActIdle onResume");
            moveTaskToBack(true);
        } else {
            onDisplay = true;
            resetLastStuckTime();
        }

        if(onDisplay) {
            // If we have reached here. we are going to the foreground. (On display set as true)
            // This mirrors onpause overload.
            state = new DisplayKiosk.NavigationBarState();
            DisplayKiosk.getInstance().enterKioskMode(this);
        }
    }

    private boolean checkForExit(Intent intent) {
        if (intent.getBooleanExtra("EXIT", false)) {
            Timber.e("Finishing due to EXIT intent!");
            finishAfterTransition();
            return true;
        }
        return false;
    }

    /*
     There are only 3 use-cases established for this class handling Intents:
     1) as part of app launch (which itself is done for a variety of reasons),
     2) to exit the app,
     3) to display "PleaseWait" when busy.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Timber.d("onNewIntent...uri: %s", intent.toUri(0));
        super.onNewIntent(intent);

        overridePendingTransition(0, 0);

        if (checkForExit(intent))
            return;

        EFTPlatform.configureFromIntent(intent);

        if(EFTPlatform.isAppHidden()) {
            Timber.i("...Move task to back 2 from ActIdle onNewIntent...");
            moveTaskToBack(true);
        }
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        Timber.i("onPause...");
        super.onPause();
        // Only revert if we were recently displayed...
        // Issue is that ActIdle doesn't show anything important. Used for background work.
        if(onDisplay) {
            DisplayKiosk.getInstance().setNavigationBarAndButtonsState(state, true);
        }
        onDisplay = false;
        resetLastStuckTime();
    }

    @Override
    protected void onStop() {
        Timber.i("onStop...");
        super.onStop();
    }

    @Override
    protected void onStart() {
        Timber.i("onStart...");
        super.onStart();

        if (executor == null) {
            executor = Executors.newScheduledThreadPool(5);
            task = new EFTActivityProcessor(Engine.getDep(), getApplicationContext());
            executor.scheduleAtFixedRate(task, 0, 200, TimeUnit.MILLISECONDS);
        }

    }

    @Override
    protected void onDestroy() {
        Timber.d("onDestroy...");
        super.onDestroy();

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }

        // removes all of the Activities belonging to a specific application from the current task eg ActSplash etc.
        // This is required to make sure intent on ActSplash is cleared.
        // Resolves the issue where the app maintains older intent values
        finishAffinity();

        // clear the referenced activity
        if(AppMain.getApp() != null){
            AppMain.getApp().setAppActivity(null);
        }
    }

    private void resetLastStuckTime() {
        lastStuckTime = 0;
    }

    public static class EFTActivityProcessor implements Runnable {

        static boolean isRunning = false;

        IDependency dep = null;
        Context context = null;
        public EFTActivityProcessor(IDependency dependency, Context context) {
            super();
            dep = dependency;
            this.context = context;
        }

        @Override
        public void run() {
            ActIdle m = AppMain.getApp().getAppActivity();
            if (m != null) {
                m.runOnUiThread(() -> {
                    try {
                        if (!isRunning) {
                            isRunning = true;
                            checkForRunningTasks();
                            isRunning = false;
                        } else {
                            Timber.i("Duplicated run of checkTimerTask");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } else {
                Timber.e("EFTActivityProcessor run failed, AppMain$appActivity was null!");
            }
        }

        void checkForRunningTasks() {
            if (dep.getJobs().pending()) {
                Timber.d("checkForRunningTasks...perform jobs...");
                dep.getJobs().perform(dep, context);
            } else if (WorkflowScheduler.getInstance().taskRunning()) {
                // If the app is stuck in this state and there is more than 1 Action on the queue,
                //  the current Action is probably blocking processing of the queue. Something to be
                //  avoided since e.g. PCI Reboot can be added to any queue at any time.
            } else if ( EFTPlatform.startupParams.backgroundTask) {
                Timber.e("------------------- Background task must have finished -------------------");
                EFTPlatform.startupParams.backgroundTask = false;
            } else if (onDisplay && UserManager.getActiveUser() == null && AppMain.isForeground()) {
                Timber.i("checkForRunningTasks...Run the user login...");
                UserManager.userLogin(dep, context);
            } else if (onDisplay && UserManager.getActiveUser() != null && AppMain.isForeground() && WorkflowScheduler.getInstance().getFGQueueCount() == 0) {
                Timber.i("checkForRunningTasks...Queue a main menu run...");
                WorkflowScheduler.getInstance().queueWorkflow(new WorkflowAddActions(new MainMenu()), false, false, false);
            } else if (WorkflowScheduler.getInstance().getTotalQueueCount() > 0) {
                Timber.d("checkForRunningTasks...check threads running...");
                WorkflowScheduler.getInstance().checkThreadsRunning(true);
            } else if ( onDisplay ) {
                Timber.d("checkForRunningTasks...onDisplay...");
                if (ActIdle.lastStuckTime == 0) {
                    lastStuckTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() > (lastStuckTime + 10000) && lastStuckTime > 0) {
                    Timber.d("checkForRunningTasks...Force a main menu to appear, as we are stuck on please wait...");
                    WorkflowScheduler.getInstance().queueWorkflow(new WorkflowAddActions(new MainMenu()), false, false, false);
                    lastStuckTime = 0;
                }

            }
        }
    }
}
