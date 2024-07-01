package com.linkly.payment.application;

import static com.linkly.payment.utilities.UIUtilities.launchActivity;
import static com.linkly.payment.utilities.UIUtilities.screenSaverRequest;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;

import com.linkly.libengine.application.IAppCallbacks;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.helpers.NotificationInternetHelper;
import com.linkly.libengine.users.UserManager;
import com.linkly.libengine.workflow.IWorkflowFactory;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.platform.StartupParams;
import com.linkly.libui.IUICallbacks;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.payment.activities.ActIdle;
import com.linkly.payment.activities.ActMainMenu;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.activities.ActTransaction;
import com.linkly.payment.activities.AppMain;
import com.linkly.payment.utilities.AutoLogoffTimer;
import com.linkly.payment.utilities.AutoSettlementWatcher;
import com.linkly.payment.viewmodel.data.UITransData;
import com.linkly.payment.workflows.generic.GenericWorkflowFactory;
import com.linkly.payment.workflows.livegroup.LiveGroupWorkflowFactory;
import com.linkly.payment.workflows.suncorp.SuncorpWorkflowFactory;
import com.linkly.payment.workflows.till.TillWorkflowFactory;
import com.linkly.payment.workflows.woolworths.WoolworthsWorkflowFactory;

import timber.log.Timber;

/**
 * AppCallbacks exists because LibPositive/etc do not have access to AppMain (the context that should
 *  be tied to) and there is a two-way passing of information from Android
 *  into the "domain" (libs) resulting in a bit of convolution. It is generally better to (as much
 *  as possible) have libraries pass one-way to Android, even substitute where needed (exposing
 *  Flows may be a great way to do that when we're using Kotlin everywhere).
 *  Casting LibMal's reference of ApplicationContext is not possible as the libraries
 *  don't have knowledge of AppMain class, AppMain could have implemented IAppCallbacks and
 *  IUICallbacks though and the casting would work.
 */
public class AppCallbacks implements IAppCallbacks, IUICallbacks {

    private static final String TAG = "AppCallbacks";
    private static AppCallbacks instance = null;

    private IWorkflowFactory workflowFactory = new GenericWorkflowFactory();

    // Application-and-library-wide flag that Transactions/Cancel-callbacks/etc. can set to have the
    //  next display of MainMenu routed to either FragInputAmountIdle or FragMainMenu.
    private Boolean shouldMainMenuDisplayInputAmountIdle = true;

    public static AppCallbacks getInstance() {

        if (instance == null) {
            instance = new AppCallbacks();
        }
        return instance;
    }

    public IAppCallbacks Initialise(IDependency d) {
        InitialiseWorkflowFactory(d);
        return this;
    }

    private void InitialiseWorkflowFactory(IDependency d) {
        PayCfg payCfg = d.getPayCfg();

        IWorkflowFactory w = null;
        if (payCfg.getCustomerName() == null) {
        } else if (payCfg.getCustomerName().contains("Demo")) {
            w = new WoolworthsWorkflowFactory();
        } else if (payCfg.getCustomerName().contains("Suncorp")) {
            w = new SuncorpWorkflowFactory();
        } else if (payCfg.getCustomerName().contains("Woolworths")) {
            w = new WoolworthsWorkflowFactory();
        } else if (payCfg.getCustomerName().contains("LiveGroup")) {
            w = new LiveGroupWorkflowFactory();
        } else if (payCfg.getCustomerName().contains("Till")) {
        	w = new TillWorkflowFactory();
    	}

        if( w != null ) {
            setWorkflowFactory(w);
        }
    }


    public void runApplication(Context context, StartupParams startupParams) {
        Timber.d("runApplication...setting up SelfLauncher...");
        class SelfLauncher implements Runnable {
            Context context;
            private StartupParams startupParams;


            SelfLauncher(Context context, StartupParams startupParams) {
                this.context = context;
                this.startupParams = startupParams;
            }

            public void run() {
                Timber.d("run[SelfLauncher]...");
                try {
                    StartupSequence.startAppWithParams(context, startupParams);
                } catch (Exception ex) {
                    Timber.w(ex);
                }
            }
        }
        Thread t = new Thread(new SelfLauncher(context, startupParams));
        t.start();
    }

    public void runPleaseWaitScreen() {
        Timber.d("runPleaseWaitScreen...");
        Intent intent = new Intent(AppMain.getApp().getApplicationContext(), ActIdle.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("BUSY", true);
        StartupSequence.populateLaunchIntent(intent);
        AppMain.getApp().getApplicationContext().startActivity(intent);
    }


    public void exitApplication() {
        Timber.d("exitApplication...");
        ActScreenSaver.cancelScreenSaver();
        Intent intent = new Intent(MalFactory.getInstance().getMalContext().getApplicationContext(), ActIdle.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("EXIT", true);
        MalFactory.getInstance().getMalContext().getApplicationContext().startActivity(intent);
    }

    /**
     * Check and initalise our P2Pe secapp.
     * @param context applicaiton context.
     * @return true if initialised. Note if already initialise it will return false.
     */
    public boolean initialiseP2Pe(Context context) {
        if (StartupSequence.initP2PEApp(context)) {
            StartupSequence.reconnectToP2PEAppOnUIThread(context);
            return true;
        }

        Timber.d("P2Pe already initialised");
        return false;
    }

    public void DisplayCallback(DisplayRequest displayRequest) {

        AppMain.getApp().getAppActivity().runOnUiThread(() -> {
            switch(displayRequest.getActivityID()) {
                case ACT_INPUT_AMOUNT_IDLE:
                    // TODO the ACT_INPUT_AMOUNT_IDLE case isn't accurate as
                    //  FragInputAmountIdle will be the first screen when in use, it is
                    //  hosted by ActMainMenu so there should be no need to relaunch
                    //  ActMainMenu (aside from the UIDisplay framework mechanism demanding
                    //  that). Overall there is a lack of domain modelling here, instead low level
                    //  mechanical details are passed around and the end goal is harder to identify
                    //  as a result.
                case ACT_MAIN_MENU:
                    Timber.d("...launching ActMainMenu as special-case DisplayAction...");
                    UITransData.getInstance().setDisplayRequest(displayRequest);
                    launchActivity(ActMainMenu.class,false, displayRequest, true);
                    break;
                case ACT_SCREEN_SAVER:     screenSaverRequest(displayRequest);
                    break;
                default:
                    Timber.d("...launching ActTransaction as default DisplayAction...");
                    UITransData.getInstance().setDisplayRequest(displayRequest);
                    launchActivity(ActTransaction.class, false,displayRequest, false);
                    break;
            }
        });
    }

    public void enterMainMenuIdleState() { AutoSettlementWatcher.enterIdleState(); }
    public void exitMainMenuIdleState() { AutoSettlementWatcher.exitIdleState(); }

    @Override
    public void displayInternetAvailabilityNotice(String message, String customNotificationSoundPath) {
        Timber.d("displayAsyncNotice...message: %s, soundPath: %s", message, customNotificationSoundPath);
        displayLocalNotification(message, customNotificationSoundPath);
    }

    // Ruthlessly cancels the last Async Notification.
    public void cancelInternetAvailabilityNotice() {
        Timber.d("cancelAsyncNotice...");
        NotificationInternetHelper.cancelNotification();
    }

    private void displayLocalNotification(String message, String customNotificationSoundPath) {
        Timber.d("displayLocalNotification...message: %s", message);
        // Maps message to Title in the Notification itself.
        NotificationInternetHelper.displayNotification(
                message,
                Notification.FLAG_ONGOING_EVENT,
                customNotificationSoundPath);
    }

    @Override
    public void onUserInteraction() {
        Timber.d("onUserInteraction...");
        if (UserManager.getActiveUser() != null) {
            AutoLogoffTimer.startTimer();
        }
    }

    @Override
    public void onTransactionFlowEntered() {
        Timber.d("onTransactionFlowEntered...");
        AutoLogoffTimer.cancelTimer();
    }

    @Override
    public void onTransactionFlowExited() {
        Timber.d("onTransactionFlowExited...");
        if (UserManager.getActiveUser() != null) {
            AutoLogoffTimer.startTimer();
        }
    }

    @Override
    public void onLogin() {
        Timber.d("onLogin...");
        AutoLogoffTimer.startTimer();
    }

    @Override
    public void onLogout() {
        Timber.d("onLogoff...");
        AutoLogoffTimer.cancelTimer();
    }

    @Override
    public IWorkflowFactory getWorkflowFactory() {
        return this.workflowFactory;
    }

    @Override
    public void setWorkflowFactory(IWorkflowFactory workflowFactory) {
        this.workflowFactory = workflowFactory;
    }

    @Override
    public void setShouldMainMenuDisplayInputAmountIdle(boolean shouldMainMenuDisplayInputAmountIdle) {
        Timber.d("setShouldMainMenuDisplayInputAmountIdle...should it: %b", shouldMainMenuDisplayInputAmountIdle);
        this.shouldMainMenuDisplayInputAmountIdle = shouldMainMenuDisplayInputAmountIdle;
    }

    @Override
    public boolean getShouldMainMenuDisplayInputAmountIdle() {
        return shouldMainMenuDisplayInputAmountIdle;
    }
}
