package com.linkly.launcher;

import static com.linkly.libpositive.messages.IMessages.APP_SEND_EFT_FILES_EVENT;
import static java.lang.Thread.sleep;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.linkly.launcher.service.LauncherController;
import com.linkly.launcher.work.UnattendedRebootWorker;
import com.linkly.libconfig.MalConfig;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libmal.IMalHardware;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.messages.Messages;
import com.linkly.libpositivesvc.POSitiveSvcLib;
import com.linkly.libui.UI;

import java.io.File;

import timber.log.Timber;

/*
Responsible for:
 1) Permission Acquisition,
 2) Loading branded files,
 3) Startup:
 3.1) Initialise Mal,
 3.2) Start POSitive Service,
 3.3) Send resources.
 4) Route based on Unattended Mode or not to either:
 4.1) LockedDownActivity, or
 4.2) ServiceFrontEnd Activity.

 Should never fatally crash even if parsing problems occur (e.g. during installation of the suite),
  should instead fail to be operational.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    boolean waitForPerm = false;
    static boolean readyToShow = false;
    private String verText;

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        String[] permissions,
        int[] grantResults
    ) {
        if (grantResults.length == 0 || grantResults[0] == -1) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        waitForPerm = false;
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        UnattendedRebootWorker.cancelSelf(this);
        // Force Connect Config request in case of launcher crash and restart.
        Messages.getInstance().sendConnectConfigRequest(this);
        //To make the status bar color green
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(BrandingConfig.getBrandDisplayStatusBarColourOrDefault(getColor(R.color.color_linkly_primary)));
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            waitForPerm = true;
        }

        /*Try and Load Branded Gear Here */
        try {

            MalFactory.getInstance().initialiseFiles(this);
            /* attempt to install resources */
            if (!readyToShow && !POSitiveSvcLib.checkResources(this, "com.linkly.res", "com.linkly.launcher.SplashActivity"))
                readyToShow = true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        checkForIncomingFiles(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkForIncomingFiles(intent);
    }

    private void checkForIncomingFiles(Intent intent) {

        if (intent == null) {
            Timber.e("check: APP_SEND_EFT_FILES_EVENT: INTENT NULL");
            return;
        }
        Timber.e("check: APP_SEND_EFT_FILES_EVENT:" + intent.getAction());
        if (APP_SEND_EFT_FILES_EVENT.equals(intent.getAction())) {
            try {
                MalFactory.getInstance().initialiseFiles(this);
                POSitiveSvcLib.copyResources(this, intent, MalFactory.getInstance().getFile().getCommonDir(), false);
            } catch (Exception e ) {
                Timber.w(e);
            }
            readyToShow = true;
            updateViews();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("deprecation")
    public void updateViews() {
        Context context = getApplicationContext();
        String packageName = context.getPackageName();

        RelativeLayout rootView = findViewById(R.id.rootViewrootView);
        ProfileCfg profileConfig = MalConfig.getInstance().getProfileCfg();
        if (profileConfig != null) {
            if (rootView != null && !profileConfig.isUnattendedModeAllowed()) {
                rootView.setVisibility(View.VISIBLE);
            }
        } else {
            // Just sit on the SplashScreen, do not allow any normal operation if
            //  configs did not load properly.
            Timber.e("ProfileConfig not loaded!");
        }

        File imgFile = new File("/data/data/" + packageName + "/files/EFT/" + BrandingConfig.getBrandDisplayLogoSplashOrDefault());
        ImageView splashLogo = findViewById(R.id.splashlogo);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            splashLogo.setImageBitmap(myBitmap);
        } else {
            splashLogo.setImageResource(R.mipmap.splashlogo);
        }

        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pinfo.versionName;
//            String version = UI.getInstance().getPrompt(String_id.STR_VERSION) + versionName; //Update this later as Mal context is null here.
            TextView ver = (TextView) findViewById(R.id.infoTitle);
            if (ver != null) {
                if (Util.isNullOrEmpty(verText))
                    verText = ver.getText().toString();
                ver.setText(verText + version);
            }

        } catch (PackageManager.NameNotFoundException e) {
            Timber.w(e);
        }
        startApp();
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (readyToShow)
            updateViews();
    }

    private Thread thr;
    public void startApp() {

        if (thr == null || !thr.isAlive()) {
            thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (waitForPerm) {
                            try {
                                Thread.sleep(100);
                            } catch (Exception ex) {
                                Timber.w(ex);
                            }
                        }

                        Timber.d("launcher initialiseMal");
                        MalFactory.getInstance().initialiseMal(getApplicationContext());
                        UI.getInstance().initialiseUI(null);

                        IMalHardware term = MalFactory.getInstance().getHardware();
                        term.hideNavigationBar();
                        LauncherController.startPOSitiveSvc(getApplicationContext());
                        POSitiveSvcLib.sendResources(getApplicationContext());
                        while (MalConfig.getInstance().isConfigLoaded() == false) {
                           sleep(500);
                        }

                        ProfileCfg profileConfig = MalConfig.getInstance().getProfileCfg();
                        if (profileConfig != null) {
                            if (profileConfig.isUnattendedModeAllowed()) {
                                Intent intent = new Intent(getApplicationContext(), LockedDownActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(getApplicationContext(), ServiceFrontEnd.class);
                                startActivity(intent);
                            }
                        } else {
                            // Just sit on the SplashScreen, do not allow any normal operation if
                            //  configs did not load properly.
                            Timber.e("ProfileConfig not loaded!");
                        }
                    } catch (Exception e) {
                        Timber.w(e);
                        finishAfterTransition();
                    }
                }
            });
            thr.start();
        }
    }
}

