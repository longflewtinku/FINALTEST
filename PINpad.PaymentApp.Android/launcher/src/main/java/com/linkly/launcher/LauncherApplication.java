package com.linkly.launcher;

import static com.linkly.launcher.service.Launcher.READY_TO_UPDATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;

import androidx.multidex.MultiDexApplication;

import com.linkly.libconfig.MalConfig;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libmal.MalFactory;
import com.linkly.logger.Logger;
import com.pax.market.android.app.sdk.BaseApiService;
import com.pax.market.android.app.sdk.StoreSdk;

import timber.log.Timber;

public class LauncherApplication extends MultiDexApplication implements
        UnattendedServiceModeAuthorizationHost, POSConnectionStatusHost {

    private static final String APP_KEY = "9WQGJLHKM06ISHFKU88Z";
    private static final String APP = "NVY5AESESZ6UL2FJD7TPGR42UUKZU0K44J2YDN0H";
    private StoreSdk paxStoreApi = null;
    // simplistic state tracker from Connect App's CONNECT_CONFIG broadcast. Tri-state where null
    //  covers the uninitialised state as Launcher awaits receiving a broadcast from Connect. Tied
    //  to POSConnectionStatusHost.
    private Boolean isPOSConnected = null;
    private static Boolean checkAutoAppStart = false;

    public static Boolean isCheckAutoAppStartSet(){
        return checkAutoAppStart;
    }

    public static void setCheckAutoAppStart(Boolean value){
        checkAutoAppStart = value;
    }
    // Flag to track and communicate authorization across Activities. Should be forced false
    //  whenever LockedDownActivity runs through its onCreate lifecycle.
    private boolean isUnattendedServiceModeAdminAccessGranted = false;

    @Override
    public void onCreate() {
        Logger.init(BuildConfig.DEBUG);
        Timber.i("Launcher app onCreate");
        // initialise MAL early here
        MalFactory.getInstance().initialiseMal(this);

        super.onCreate();

        initPaxStoreSdk();
    }

    private void initPaxStoreSdk() {
        Timber.i("Launcher app initPaxStoreSdk");

        try {
            String key = "";
            String s = "";

            MalFactory.getInstance().initialiseFiles(this);

            if (ProfileCfg.getInstance() != null) {
                key = ProfileCfg.getInstance().getPaxstoreServiceKey();
                s = ProfileCfg.getInstance().getPaxstoreServiceSecret();
            }
            if (key == null || key.isEmpty() || s == null || s.isEmpty()) {
                key = APP_KEY;
                s = APP;
            }

            Timber.v( "Paxstore before init");
            //1. Get API URL from PAXSTORE and initialize AppKey?AppSecret, SN
            paxStoreApi = StoreSdk.getInstance();
            paxStoreApi.init(getApplicationContext(), key, s, new BaseApiService.Callback() {
                @Override
                public void initSuccess() {
                    Timber.e( "Paxstore init success");
                    initInquirer();
                }

                @Override
                public void initFailed(RemoteException e) {
                    Timber.e( "Paxstore init failed, error %s", e);
                }
            });
        } catch ( Exception e) {
            Timber.e(e);
        }
    }

    /**
     * Put init result into sp, incase you need to get params. Before you get params you need to check if api init succeed.
     *
     * @param result
     */
    private void putApiInitResult(boolean result) {
        SharedPreferences userSettings = getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = userSettings.edit();
        editor.putBoolean("init_success", result);
        editor.apply();
    }

    private void initInquirer() {
        //2. Initializing the method/function business logic of your app whether it can be updated
        StoreSdk.getInstance().initInquirer(() -> {
            Timber.i("call business function....isReadyUpdate = %b", READY_TO_UPDATE);
            return READY_TO_UPDATE;
        });
    }

    public void onPOSConnectionLost() {
        Timber.d("onPOSConnectionLost...");
        setPOSConnected(false);
        ProfileCfg profileConfig = MalConfig.getInstance().getProfileCfg();
        if (profileConfig != null) {
            if (profileConfig.isUnattendedModeAllowed()
                && !isUnattendedServiceModeAdminAccessGranted) {
                Intent intent = new Intent(this, LockedDownActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        } else {
            Timber.e("ProfileConfig was null!");
        }
    }

    @Override
    public boolean isUnattendedServiceModeAdminAccessGranted() {
        return isUnattendedServiceModeAdminAccessGranted;
    }

    @Override
    public void setUnattendedServiceModeAdminAccessGranted(boolean isGranted) {
        isUnattendedServiceModeAdminAccessGranted = isGranted;
    }

    @Override
    public Boolean isPOSConnected() {
        return isPOSConnected;
    }

    @Override
    public void setPOSConnected(boolean isConnected) {
        isPOSConnected = isConnected;
    }

    public StoreSdk getPaxStoreApi() {
        return this.paxStoreApi;
    }

    public Boolean getIsPOSConnected() {
        return this.isPOSConnected;
    }

    public void setPaxStoreApi(StoreSdk paxStoreApi) {
        this.paxStoreApi = paxStoreApi;
    }

    public void setIsPOSConnected(Boolean isPOSConnected) {
        this.isPOSConnected = isPOSConnected;
    }
}