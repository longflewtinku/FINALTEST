package com.linkly.payment.application;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.linkly.libconfig.ProfileCfg;
import com.linkly.libengine.P2PCallbackUI;
import com.linkly.libengine.config.PayCfgFactory;
import com.linkly.libengine.debug.DebugReport;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.dependencies.Dependencies;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.comms.CommsFactory;
import com.linkly.libengine.engine.comms.CommsStatusMonitor;
import com.linkly.libengine.engine.customers.ICustomer;
import com.linkly.libengine.engine.protocol.Protocol;
import com.linkly.libengine.jobs.Jobs;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.status.StatusReport;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.IMal;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.config.ConfigExceptions;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.platform.StartupParams;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.messages.Messages;
import com.linkly.libsecapp.IP2PCallback;
import com.linkly.libsecapp.P2PCallbackService;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.UI;
import com.linkly.payment.BuildConfig;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActIdle;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.activities.ActSplash;
import com.linkly.payment.activities.AppMain;
import com.linkly.payment.crashreporting.LinklyCrashlytics;
import com.linkly.payment.customer.CustomerFactory;
import com.linkly.payment.printing.PrintManager;

import java.io.File;

import timber.log.Timber;

public class StartupSequence {

    private static Dependencies d = null;
    private static P2PCallbackImpl iP2pCallback = new P2PCallbackImpl();

    private static boolean initialised = false;
    private static boolean initialising = false;

    private StartupSequence() {
    }

    public static void ServiceStartRequest(Context context) {

        Timber.i("ServiceStartRequest");
        if (EFTPlatform.isPaxTerminal()) {
            d.getMessages().sendServiceStartRequest(context);
        }
    }

    public static boolean initP2PEApp(Context context) {
        Timber.i("InitP2PEApp");
        return P2PLib.getInstance().Init(context.getApplicationContext(), iP2pCallback);
    }

    public static void reconnectToP2PEApp(Context context) {
        /* start this so callbacks can come through */
        Intent myIntent = new Intent(context, P2PCallbackService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(myIntent);
        } else {
            context.startService(myIntent);
        }

        if (P2PLib.getInstance().getIP2PUI() != null) {
            P2PCallbackUI p2PCallbackUI = new P2PCallbackUI();
            P2PLib.getInstance().getIP2PUI().uiInit(p2PCallbackUI);
        }
    }

    public static void reconnectToP2PEAppOnUIThread(Context context) {
        Timber.i("Reconnecting");
        AppMain.getApp().getAppActivity().runOnUiThread(() -> reconnectToP2PEApp(context));
    }

    public static boolean isInitialised() {
        return StartupSequence.initialised;
    }

    private static class P2PCallbackImpl implements IP2PCallback {
        @Override
        public void onDisconnected() {

            // NOT NEEDED FOR NOW as app already stopped on crash
            //Intent it = new Intent("com.eft.eftp2pe.P2P");
            //it.setPackage("com.eft.eftp2pe");
            //MalFactory.getInstance().getMalContext().stopService(it);
        }
    }

    /***
     * TODO: Creating task. Need to refactor/fix.
     * Currently deals with initialisation of objects that require data to be loaded into them.
     * (Eg read file and load into object to used in the app) Annoyingly some these objects are stateful and take a long time to load.
     * If process death happens we are fucked, app will hard crash and forced app restart will happen.
     * We can keep the "loading/processing" of config here as start up but extract the requirement of the objects needing to hold all information.
     * Can be shifted to other areas to fix up potential issues. we are seeing.
     * Goal should be as follows: Initialisation of data required for the app can be done here.
     * However when obtaining the objects to work on the data should be able be quick and not require any work needed
     */
    public static void appStartRequest(Activity activity, boolean runApp, IMal mal) {
        Timber.d("AppstartRequest");

        if( initialising){
            Timber.e("...ignoring AppstartRequest, app already in initialising state!");
            return;
        }

        /* initialisation of things that need the Display thread */
        if (!initialised) {
            initialising = true;
            // TODO: all these objects have requirements on other componients. I.E DebugReporter requires Messages. Will slowly need to decouple these dependencies from each other.
            try {
                Timber.v("----- Starting Main Initialisation of Required Components -----");
                // connect to p2pe app
                Timber.v("Initialise P2Pe");
                initP2PEApp(activity.getApplicationContext());
                // create the one and only dependencies object
                // TODO: REFACTOR SO AND DETACH DEPENDENCIES. Issues with circular dependencies/requirement for other dpendencies to be initialised plus context requirement.
                d = new Dependencies();
                Timber.v("Initialise PayCfg");
                d.setPayCfg(new PayCfgFactory().initialiseConfig(activity.getApplicationContext(), mal, ProfileCfg.getInstance().getCustomerName(), BuildConfig.VERSION_NAME));
                Timber.v("Processing Branding Files");
                new BrandingProcessor().processAndSendBranding(d.getPayCfg(), mal, activity.getApplicationContext());
                Timber.v("Initialise App Callbacks");
                d.setAppCallbacks(AppCallbacks.getInstance());
                Timber.v("Initialise PrintManager");
                d.setPrintManager(PrintManager.getInstance());
                Timber.v("Initialise UI/Framework");
                d.setFramework(UI.getInstance());
                Timber.v("Initialise DisplayCallbacks");
                d.setDisplayCallback(AppCallbacks.getInstance());
                Timber.v("Initialise UserManager");
                d.setUsrMgr(UserManager.getInstance());
                Timber.v("Setting Protocol");
                d.setProtocol(new Protocol());
                Timber.v("Initialise Messages");
                d.setMessages(Messages.getInstance());
                Timber.v("Initialise Jobs");
                d.setJobs(Jobs.getInstance());
                Timber.v("Setting P2PLib");
                d.setP2PLib(P2PLib.getInstance());
                Timber.v("Initialise Status Reporter");
                d.setStatusReporter(StatusReport.getInstance());
                Timber.v("Initialise Debug Reporter");
                DebugReport.getInstance().init(activity.getApplicationContext(), d.getMessages());
                d.setDebugReporter(DebugReport.getInstance());
                Timber.v("Initialise Customer Specific Requirements");
                d.setCustomer(CustomerFactory.createCustomerObj(d));
                Timber.v("Loading Config");
                d.getConfig().loadConfig();
                Timber.v("Validating SecApp Flavor");
                validateSecAppFlavor(d.getCustomer());

                // instantiate comms type based off configuration. MUST RUN AFTER LOADCONFIG
                Timber.v("Initialising Comms Connection Type (Customer Specific");
                CommsFactory commsFactory = new CommsFactory();
                d.setComms(commsFactory.getCommsObj(d));
                Timber.v("Initialising Comms Monitor");
                CommsStatusMonitor commsStatusMonitor = CommsStatusMonitor.getInstance();
                commsStatusMonitor.setCustomNotificationSoundPath("android.resource://" +
                        activity.getApplicationContext()
                                .getPackageName() + File.separator + R.raw.offline_event);

                Timber.v("Running engine Initialisation");
                Engine.init(d, activity.getApplicationContext(), MalFactory.getInstance());
                Timber.v("Initialising Crashlytics.");
                new LinklyCrashlytics("StartupSequence completed.", MalFactory.getInstance().getHardware().getSerialNumber(), d.getPayCfg());
                Timber.v("----- Initialising Finished -----");
                initialised = true;
            } catch( Exception e ){
                d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.CONFIG_FAILURE, e.getMessage());
                Timber.e(e);
                throw new ConfigExceptions.ValidationError(e.getMessage());
            } finally {
                initialising = false;
            }
        }

        // The following must rerun if app Exited and reopened.
        activity.runOnUiThread( () -> {
            reconnectToP2PEApp(activity.getApplicationContext());
            CoreOverrides.initialise();

            if (runApp) {
                AppMain.getApp().setRunning(true);
                StartupSequence.startAppFromMainMenu(AppMain.getApp().getApplicationContext());
            }
            activity.finishAfterTransition();
        } );
        initialising = false;
    }

    public static void startAppWithParams(Context context, StartupParams startupParams) {

        boolean alreadyAutoStarted = EFTPlatform.startupParams.isAutoStarted();

        EFTPlatform.setStartupParams(startupParams);

        boolean exitWhenDone = !AppMain.getApp().isRunning() && startupParams.isExitWhenDone();
        startupParams.setExitWhenDone(exitWhenDone);

        if (!AppMain.getApp().isRunning() && startupParams.isAutoStarted())
            startupParams.setAutoStarted(true);
        else if (alreadyAutoStarted && startupParams.isAutoStarted())
            startupParams.setAutoStarted(true);
        else if (!alreadyAutoStarted && startupParams.isAutoStarted())
            startupParams.setAutoStarted(false);

        startAppFromMainMenu(context);
    }


    public static void populateLaunchIntent(Intent i) {
        i.putExtra("BackgroundTask", EFTPlatform.startupParams.backgroundTask);
        i.putExtra("HideWhenDone",  EFTPlatform.startupParams.hideWhenDone);
        i.putExtra("ExitWhenDone",  EFTPlatform.startupParams.exitWhenDone);
        i.putExtra("SplashScreen",  EFTPlatform.startupParams.splashScreen);
        i.putExtra("AutoStarted",   EFTPlatform.startupParams.autoStarted);
    }

    /**
     * Alright...... So..... Story time....
     * Started seeing our app when crashed have multiple subsequent crashes.
     * Currently we store all our initialisation in memory. I.E config, Mal and P2Pe initialisation etc.
     * These are stored as s singletons etc.
     * Anytime of process death/crashes our application CANNOT restart properly as the Android OS restores the
     * "previous" activity rather than restarting the entire application.
     * While fixing our crashes is fine we still cannot control process death or unknown crashes..
     * Now why not load it in the Application and initialise in onCreate.... Good question...
     * Due to the P2Pe library and service binding, that has to be run in a thread otherwise it just locks up...
     * Plus, this will not resolve our issue as our code source for our entire application  have the assumption that this is loaded.
     * Fuck me right....
     * However, this is just a stop-gap that allows our app to recover reasonably rather than hard crashing.
     * A full refactor would be required on how we handle initialisation and start up of our application as noted below.
     * <a href="https://pceftpos.atlassian.net/wiki/spaces/CPD/pages/2692939783/Payment+App+-+Lifecycle+Refactoring">...</a>
     */
    public static boolean checkAppConfiguredPostCrash(Context context, boolean isActIdle) {
        Intent i;
        if (!AppMain.getApp().isRunning() && (EFTPlatform.isPaxTerminal() || EFTPlatform.isPaxTill())) {
            if(!isActIdle) {
                Timber.d("Forcing shutdown of act idle...");
                ActScreenSaver.cancelScreenSaver();
                Intent intent = new Intent(context.getApplicationContext(), ActIdle.class);
                intent.putExtra("EXIT", true);
                context.getApplicationContext().startActivity(intent);
            }

            Timber.i("launching ActSplash" );
            i = new Intent(context, ActSplash.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            populateLaunchIntent(i);
            context.startActivity(i);
            return true;
        }
        return false;
    }

    public static void startAppFromMainMenu(Context context) {

        Intent i;
        if (!AppMain.getApp().isRunning() && (EFTPlatform.isPaxTerminal() || EFTPlatform.isPaxTill())) {
            Timber.i("launching ActSplash" );
            i = new Intent(context, ActSplash.class);
        } else {
            Timber.i("launching ActIdle" );
            i = new Intent(context, ActIdle.class);
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        populateLaunchIntent(i);
        context.startActivity(i);
    }

    private static void validateSecAppFlavor(ICustomer customer) {
        if (Util.isNullOrEmpty(customer.getRequiredSecAppFlavor()) || !customer.getRequiredSecAppFlavor().equals(P2PLib.getInstance().getIP2PCtls().getSecAppBuildFlavour())) {
            throw new ConfigExceptions.ValidationError("Different version of SecApp is required.");
        }
    }
}
