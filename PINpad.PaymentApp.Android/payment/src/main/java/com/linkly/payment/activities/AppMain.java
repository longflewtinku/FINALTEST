package com.linkly.payment.activities;

import static com.linkly.libengine.helpers.UIHelpers.wakeTerminalIfSleeping;
import static com.linkly.payment.workmanager.WorkManagerHotloadParams.StartWorkRequest;

import android.app.Activity;
import android.app.Application;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.StrictMode;

import androidx.multidex.MultiDexApplication;

import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.libmal.MalFactory;
import com.linkly.logger.Logger;
import com.linkly.payment.BuildConfig;
import com.linkly.payment.leakcanarywrapper.ILeakCanaryWrapper;
import com.linkly.payment.leakcanarywrapper.LeakCanaryWatcher;
import com.linkly.payment.positivesvc.MessageReceiver;
import com.linkly.payment.utilities.ConfigFileObserverWrapper;
import com.pax.market.android.app.sdk.BaseApiService;
import com.pax.market.android.app.sdk.StoreSdk;

import java.util.Timer;

import timber.log.Timber;

public class AppMain extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    private static final String APP_KEY = "U5ONXKYWAQQU346A8CF1";
    private static final String APP = "JP98LVCS8T6A3RU5RROSIOM0W4VHBLE3QUWXTPQ7";

    private ConfigFileObserverWrapper fileObserverWrapper;

    private static AppMain application;
    private static Timer t;

    private static boolean isForeground = false;

    private boolean running = false;

    /* activities only used by the Android Display */
    private ActIdle appActivity;

    private int numStarted = 0;

    void registerReceiver(){
        MessageReceiver receiver = new MessageReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.linkly.TRANSACTION_REQUEST");
        intentFilter.addAction("com.linkly.REPORT_REQUEST_EVENT");
        intentFilter.addAction("com.linkly.EVENT");
        intentFilter.addAction("com.linkly.CFG_RESPONSE");
        intentFilter.addAction("com.linkly.SCHD_BATCH");
        intentFilter.addAction("com.linkly.SCHD_RESTART");
        intentFilter.addAction("com.linkly.SCHD_REBOOT");
        intentFilter.addAction("com.linkly.SCHD_SHIFT_TOTALS_RESET");
        intentFilter.addAction("com.linkly.REBOOT_WARNING");
        intentFilter.addAction("com.linkly.REBOOT_SUSPEND");
        intentFilter.addAction("com.linkly.DISABLE_CANCEL");
        intentFilter.addAction("com.linkly.READ_CARD");
        intentFilter.addAction("com.linkly.APP_SEND_EFT_FILES");
        intentFilter.addAction("com.linkly.APP_STATUS_INFO_REQUEST_EVENT");
        intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        intentFilter.addAction("com.nominateloyalty.confirm");
        intentFilter.addAction("com.payment.confirm");
        intentFilter.addAction("com.linkly.LOGON_EVENT");
        intentFilter.addAction("com.linkly.CONFIG_EVENT");
        intentFilter.addAction("com.linkly.POS_KEY_EVENT");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        getApplicationContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup our logging
        Logger.init(BuildConfig.DEBUG);

        Timber.e("Payment app AppMain onCreate");

        MalFactory.getInstance().initialiseMal(getApplicationContext());

        initBaseApi();

        // register broadcast receivers for Android version greater or equal to 10 (8 seems to be working fine for now)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            registerReceiver();
        }

        application = this;

        registerActivityLifecycleCallbacks(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .permitDiskReads() // suppresses spamming in logcat
                .permitDiskWrites() // suppresses spamming in logcat
                .build();
        StrictMode.setThreadPolicy(policy);

        restartMonitoring();
    }

    ConfigFileObserverWrapper.FileObserverCallback callback = (mask, path) -> {

        Timber.d("File change: %d", mask);
        try {
            if((mask & FileObserver.CREATE) !=0 ||
            (mask & FileObserver.MODIFY) != 0) {
                StartWorkRequest(getApp());
            }
        } catch(Exception e) {
            Timber.e(e);
        }

        // Edge case. As documented, if delete self is called (which it is a lot) we are required to "restart" observing.
        if((mask & FileObserver.DELETE_SELF) != 0) {
            Timber.e("Delete Self");
            try {
                // So we need a sleep as if we directly go to restart monitoring.
                // Suffer issues where the monitoring doesn't start.
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            restartMonitoring();
            StartWorkRequest(getApp());
        } else if((mask & 32768) != 0) { // Undocumented mask that is sent...
            Timber.e("Garbage collection...");
        }
    };

    private void restartMonitoring() {
        Timber.e("Starting File Monitoring");
        fileObserverWrapper = new ConfigFileObserverWrapper(MalFactory.getInstance().getFile().getWorkingDir() + "/" + "hotloadparams.xml", FileObserver.ALL_EVENTS, callback);
        fileObserverWrapper.startWatching();
    }

    @Override
    public void onTerminate() {
        if(fileObserverWrapper != null) {
            fileObserverWrapper.stopWatching();
        }
        fileObserverWrapper = null;
        super.onTerminate();
    }

    public static void addWatcher(Object objectToWatch) {
        ILeakCanaryWrapper wrapper = new LeakCanaryWatcher();
        wrapper.addWatcher( objectToWatch );
    }

    public void setAppActivity(ActIdle activity) {
        this.appActivity = activity;
    }

    public void initBaseApi() {

        try {
            //1. Get API URL from PAXSTORE and initialize AppKey,AppSecret, SN
            StoreSdk.getInstance().init(getApplicationContext(), APP_KEY, APP, new BaseApiService.Callback() {
                @Override
                public void initSuccess() {
                    initInquirer();
                }

                @Override
                public void initFailed(RemoteException e) {
                    //TODO Do failed logic here
                }
            });
        } catch ( Exception e) {
            Timber.e(e);
        }
    }

    static boolean isRunningTransaction = false;

    private void initInquirer() {
        //2. Initializing the method/function business logic of your app whether it can be updated
        StoreSdk.getInstance().initInquirer(() -> {
            Timber.i("call business function....isReadyUpdate = %b", !WorkflowScheduler.isTransactionRunning());
            isRunningTransaction = WorkflowScheduler.isTransactionRunning();
            return !isRunningTransaction;
        });
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {
        Timber.d("AppMain onActivityStarted...%s", activity.getLocalClassName());
        if (numStarted == 0) {
            //app went to foreground
            Timber.i("App To Foreground");
            isForeground = true;
            wakeTerminalIfSleeping(this);
        }
        numStarted++;

    }

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {
        Timber.d("AppMain onActivityStopped...%s", activity.getLocalClassName());
        numStarted--;
        if (numStarted == 0) {
            // app went to background
            Timber.i("App To Background");
            isForeground = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}

    public boolean isRunning() {
        return this.running;
    }

    public ActIdle getAppActivity() {
        return this.appActivity;
    }

    public int getNumStarted() {
        return this.numStarted;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setNumStarted(int numStarted) {
        this.numStarted = numStarted;
    }

    public static AppMain getApp() {
        return application;
    }

    public static boolean isForeground() {
        return AppMain.isForeground;
    }

    public static void setForeground(boolean isForeground) {
        AppMain.isForeground = isForeground;
    }
}