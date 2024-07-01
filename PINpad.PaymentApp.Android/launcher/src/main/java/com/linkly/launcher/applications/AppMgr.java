package com.linkly.launcher.applications;

import static android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS;
import static com.linkly.libpositive.wrappers.OperatingMode.BACKGROUND_CAPABLE;
import static com.linkly.libpositive.wrappers.OperatingMode.FOREGROUND_ONLY;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.linkly.launcher.Activate;
import com.linkly.libconfig.MalConfig;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libpositive.messages.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class AppMgr {

    public static final String LINKLY_CONNECT_APP_PKG_NAME = "com.linkly.connect.linkly";
    private static final String LINKLY_PAYMENT_APP_PKG_NAME = "com.linkly.payment";
    private static final String CFG_LAST_LAUNCH_PACKAGE_NAME = "packageName";
    private static final String CFG_LAST_LAUNCH_ACTIVITY_NAME = "activityName";
    private static final String CFG_LAST_LAUNCH_DISPLAY_NAME = "displayName";
    private static final String CFG_SHARED_PREFERENCES_NAME = "com.service.launcher.cfg";

    private static AppMgr ourInstance = new AppMgr();
    private PackageManager manager;
    private List<AppDetail> apps;

    private AppMgr() {
    }

    public static AppMgr getInstance() {
        return ourInstance;
    }


    private static SharedPreferences getSharedPrefs(Context context){
        return context.getApplicationContext().getSharedPreferences(CFG_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private static void saveLastLaunchedAppInfo(Context context, AppDetail appDetail, boolean clearData){
        SharedPreferences sharedPref = getSharedPrefs(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        if( clearData ) {
            editor.putString(CFG_LAST_LAUNCH_PACKAGE_NAME, "");
            editor.putString(CFG_LAST_LAUNCH_ACTIVITY_NAME, "");
            editor.putString(CFG_LAST_LAUNCH_DISPLAY_NAME, "");
        } else {
            editor.putString(CFG_LAST_LAUNCH_PACKAGE_NAME, appDetail.packageName);
            editor.putString(CFG_LAST_LAUNCH_ACTIVITY_NAME, appDetail.activityName);
            editor.putString(CFG_LAST_LAUNCH_DISPLAY_NAME, appDetail.displayName);
        }
        editor.apply();
    }

    private static AppDetail getLastLaunchedAppInfo(Context context){
        SharedPreferences sharedPref = getSharedPrefs(context);
        String packageName = sharedPref.getString(CFG_LAST_LAUNCH_PACKAGE_NAME, "");
        String activityName = sharedPref.getString(CFG_LAST_LAUNCH_ACTIVITY_NAME, "");
        String displayName = sharedPref.getString(CFG_LAST_LAUNCH_DISPLAY_NAME, "");

        if( packageName.isEmpty() || activityName.isEmpty() || displayName.isEmpty()){
            // don't auto launch
            return null;
        }

        AppDetail appDetail = new AppDetail();
        appDetail.packageName = packageName;
        appDetail.activityName = activityName;
        appDetail.displayName = displayName;
        return appDetail;
    }

    @SuppressWarnings("deprecation")
    public void loadApps(Context context) {
        manager = context.getPackageManager();
        Timber.i( "loading app list started" );
        apps = new ArrayList<>();
        List<ResolveInfo> availableActivities;

        ProfileCfg profileCfg = MalConfig.getInstance().getProfileCfg();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        // During start-up the connect app launcher activity could be disabled. Hence, enabled the options to query the disabled activities as well
        availableActivities = manager.queryIntentActivities(i, MATCH_DISABLED_COMPONENTS);

        for (ResolveInfo ri : availableActivities) {
            // only add apps to the app list for launcher if profile configuration is proper
            // else keep it empty and later show "Profile Configuration Error" to the user
            if (profileCfg != null) {
                String activityName = ri.activityInfo.name;
                if( activityName.contains("leakcanary")){
                    Timber.v("not adding leak canary item to menu [%s]", activityName);
                } else {
                    ProfileCfg.MenuItemCfg menuItem = profileCfg.getMenuItem(ri.activityInfo.packageName, ri.activityInfo.name, manager);
                    if (menuItem != null) {
                        AppDetail app = new AppDetail();
                        app.label = ri.loadLabel(manager).toString();
                        app.packageName = ri.activityInfo.packageName;
                        app.activityName = ri.activityInfo.name;

                        app.icon = ri.activityInfo.loadIcon(manager);

                        if (menuItem.getImageName() != null && !menuItem.getImageName().isEmpty()) {

                            String image = MalFactory.getInstance().getFile().getCommonDir() + "/" + menuItem.getImageName();
                            Rect r = app.icon.copyBounds();
                            app.icon = Drawable.createFromPath(image);
                            if (app.icon != null)
                                app.icon.setBounds(r);
                        }
                        if (app.icon == null)
                            app.icon = ri.activityInfo.loadIcon(manager);

                        app.displayName = menuItem.getDisplayName();
                        app.enableNavBar = menuItem.isEnableNavBar();
                        app.autoStart = menuItem.isAutoStart();
                        app.priority = menuItem.getPriority();
                        try {
                            app.autoStartDelay = Integer.parseInt(menuItem.getAutoStartDelay());
                        } catch (NumberFormatException ex) {
                            app.autoStartDelay = 0;
                        }

                        apps.add(app);
                        Timber.i("adding app %s with priority %d", app.displayName, app.priority);
                    }
                }
            }
        }
        Timber.i( "loading app list finished" );
    }

    public static boolean isAppPresent(Context context, String packageName) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName) != null;
    }

    public void startApp(Context context, AppDetail appDetail) {
        // Set the operating mode for connect app
        setupConnectAppOperatingMode(appDetail, context);

        // Query the package name to identify app status and make use of the launch intent
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appDetail.packageName);
        // Save 'last app launch' application info
        saveLastLaunchedAppInfo(context, appDetail, false);

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                Timber.d("starting app pkg %s, activity name %s, intent = %s", appDetail.packageName, appDetail.activityName, launchIntent.toUri(0));
                context.startActivity(launchIntent);
            } catch (ActivityNotFoundException e) {
                // Handle the case when the app or a component is disabled
                Timber.e("error starting activity, App or component disabled, %s", e.toString());
                // Retry the operating mode setup, as the launch would have failed
                setupConnectAppOperatingMode(appDetail, context);
                try {
                    context.startActivity(launchIntent);
                } catch (Exception ex) {
                    Timber.e("relaunch failed too, error starting activity %s", ex.toString());
                }
            } catch (Exception e) {
                Timber.e("error starting activity %s", e.toString());
            }
        } else {
            // Handle the case when the app is not installed or no launch intent is found
            Timber.e("error starting activity, App not found. Package name = %s", appDetail.packageName);
        }
    }

    private void setupConnectAppOperatingMode(AppDetail appDetail, Context context) {
        if (appDetail.packageName.equals(LINKLY_CONNECT_APP_PKG_NAME)) {
            // if starting the linkly connect app, change it's operating mode to foreground only
            Messages.getInstance().sendOperatingMode(context, FOREGROUND_ONLY.ordinal());
            // short delay is required so connect app can change it's operating mode in time to receive the launch intent
            delay1Sec();
        } else if (appDetail.packageName.equals(LINKLY_PAYMENT_APP_PKG_NAME)) {
            // if starting payment app, change connect app's operating mode to background capable
            Messages.getInstance().sendOperatingMode(context, BACKGROUND_CAPABLE.ordinal());
            // note: no delay required here as we're not launching the connect app
        }
    }

    @SuppressWarnings("java:S2142") // Either re-interrupt this method or rethrow the "InterruptedException" that can be caught here
    private void delay1Sec(){
        try {
            Thread.sleep(1000);
        } catch( Exception ignored ){
            // deliberately empty
        }
    }

    public void startApp(Context context, int pos) {
        if (apps.get(pos).packageName.startsWith("local.func")) {
            if (apps.get(pos).packageName.endsWith("activate")) {
                Intent i = new Intent(context, Activate.class);
                context.startActivity(i);
            }
        } else {
            // send intent to launch selected app
            startApp(context, apps.get(pos));
            if (apps.get(pos).enableNavBar) {
                // disable nav bar for app if required
                DisplayKiosk.getInstance().enableNavBarForExternalApps();
            }
        }
    }

    private static AppDetail getAppDetail(String appPackageName){
        final List<AppDetail> apps = AppMgr.getInstance().getApps();
        for (final AppDetail app : apps) {
            if( app.packageName.contains(appPackageName) ){
                return app;
            }
        }
        return null;
    }

    /**
     * returns info on app to auto-launch, or null if there's nothing to auto-launch
     *
     * priority:
     * 1. key injection app, if present
     * 2. any app with 'auto launch' set true
     * 3. last manually launched app
     *
     * @param context context
     * @return application detail object for app to auto-launch, or null if auto-launch doesn't apply
     */
    public static AppDetail getApkToAutoLaunch(final Context context){
        Timber.d("getApkToAutoLaunch...");
        AppDetail appToLaunch = null;
        final String KEY_INJECTION_PACKAGE_NAME = "com.linkly.keyinjection";
        int lpriority = -1;
        final List<AppDetail> apps = AppMgr.getInstance().getApps();
        AppDetail lastAppLaunched = getLastLaunchedAppInfo(context);

        AppDetail keyInjectionApp = getAppDetail(KEY_INJECTION_PACKAGE_NAME);
        if( keyInjectionApp != null ) {
            // PRIORITY 1 - key injection app, if present
            // skip normal selection process if key injection app is present. Always highest priority
            appToLaunch = keyInjectionApp;
            // override some settings if not set correctly in profile.json
            appToLaunch.autoStart = true;
            appToLaunch.displayName = "Key Injection";
            appToLaunch.autoStartDelay = 2;
            appToLaunch.priority = 0;
        } else {
            // PRIORITY 2 - any app with 'auto launch' set true
            // look through app config from profile.json to find highest priority 'autoStart' app
            for (final AppDetail app : apps) {
                if (app.autoStart && app.priority > lpriority) {
                    appToLaunch = app;
                }
            }
        }

        // PRIORITY 3 - last manually launched app
        // if no app selected above, and we have info on last app launched
        //  Bypassed entirely for Unattended Mode.
        ProfileCfg profileConfig = MalConfig.getInstance().getProfileCfg();
        if (profileConfig != null) {
            if (appToLaunch == null && lastAppLaunched != null
                    && !profileConfig.isUnattendedModeAllowed()) {
                boolean lastAppLaunchedStillPresent = getAppDetail(lastAppLaunched.packageName) != null;
                if (lastAppLaunchedStillPresent) {
                    // auto-launch the last app that was manually launched
                    appToLaunch = lastAppLaunched;
                } else {
                    // else last app launched has been removed (happens sometimes, e.g. key injection app). clear stored 'last app launched' data
                    saveLastLaunchedAppInfo(context, null, true);
                }
            }
        } else {
            Timber.e("ProfileConfig not loaded!");
        }

        return appToLaunch;
    }

    public void autoStartApps(final Context context, final boolean startImmediately) {
        ProfileCfg p = MalConfig.getInstance().getProfileCfg();
        if (p == null) {
            return;
        }
        if( context == null ){
            return;
        }

        Timber.e("Auto starting apps");

        // if key injection app is present, force start it
        AppDetail appToLaunch = getApkToAutoLaunch(context);
        if( appToLaunch == null ){
            // no app to auto launch, exit early
            Timber.i("No app to auto launch");
            return;
        }

        final int autoStartDelay = startImmediately ? 0 : appToLaunch.autoStartDelay;
        final AppDetail finalApkToLaunch = appToLaunch;

        Timber.e("finalApkToLaunch, package = %s, activity = %s", finalApkToLaunch.packageName, finalApkToLaunch.activityName);

        // if APK to launch is selected, launch it, else do nothing
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Timber.e("[AutoLauncher] RUNNING! package/activity: %s/%s", finalApkToLaunch.packageName, finalApkToLaunch.activityName);
                try {
                    startApp(context, finalApkToLaunch);
                } catch (Exception e) {
                    Timber.w(e);
                }
            }
        }, autoStartDelay * 1000L);
    }

    public PackageManager getManager() {
        return this.manager;
    }

    public List<AppDetail> getApps() {
        return this.apps;
    }

    public void setManager(PackageManager manager) {
        this.manager = manager;
    }

    public void setApps(List<AppDetail> apps) {
        this.apps = apps;
    }
}
