package com.linkly.payment.activities;

import static com.linkly.libpositive.messages.IMessages.APP_SEND_EFT_FILES_EVENT;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.config.PayCfgFactory;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.config.ConfigExceptions;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositivesvc.POSitiveSvcLib;
import com.linkly.payment.BuildConfig;
import com.linkly.payment.R;
import com.linkly.payment.application.StartupSequence;
import com.linkly.payment.utilities.UIUtilities;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class ActSplash extends AppCompatActivity {

    boolean readyToShow = false;
    boolean waitForPermissions = false;
    private ScheduledExecutorService executorService;
    private String verText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("ActSplash onCreate top");

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .permitDiskReads() // suppresses spamming in logcat
                    .permitDiskWrites() // suppresses spamming in logcat
                    .build());
        }

        Timber.d("ActSplash onCreate");
        super.onCreate(savedInstanceState);

        //To make the status bar color green
        IDependency d = Engine.getDep();
        if (d != null && d.getConfig() != null) {
            getWindow().setStatusBarColor(d.getPayCfg().getBrandDisplayStatusBarColourOrDefault(getColor(R.color.color_linkly_primary)));
            applyStatusBar("Linkly", 10);
        }
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        Timber.d("Number of Core: %d", numberOfCores);

        if (UIUtilities.checkPermission(this))
            waitForPermissions = true;

        EFTPlatform.configureFromIntent(getIntent());

        if (!EFTPlatform.startupParams.splashScreen) {
            Timber.d("Move task to back from splash screen onCreate");
            moveTaskToBack(true);
            startApp();
        } else {
            Timber.d("ActSplash setting content view");
            setContentView(R.layout.activity_splash);
            /*Try and Load Branded Gear Here */
            if (EFTPlatform.isPaxTill()) {
                /* attempt to install resources */
                if (!POSitiveSvcLib.checkResources(this, "com.linkly.res", "com.linkly.payment.activities.ActSplash"))
                    readyToShow = true;

            } else {
                readyToShow = true;
            }
            updateViews();

            }
    }

    private void displayMessage(String message) {
        message = getString(R.string.INVALID_CONFIG_FILE, message);
        RelativeLayout rootLayout = findViewById(R.id.rootViewrootView);
        if (rootLayout == null) {
            Spannable centeredText = new SpannableString(message);
            centeredText.setSpan(
                    new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                    0, message.length() - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
            );
            Toast toast = Toast.makeText(getApplicationContext(), centeredText, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
            toast.show();
            finishAfterTransition();
        } else {
            Snackbar snackbar = Snackbar
                    .make(rootLayout, message, BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setActionTextColor(getColor(R.color.colorPrimary))
                    .setAction(R.string.OK, view -> {
                        //It should automatically dismiss the snackbar
                    });
            View snackbarView = snackbar.getView();
            TextView snackTextView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    finishAfterTransition();
                }
            });
            snackTextView.setMaxLines(message.length());
            snackbar.show();
        }
    }

    private void applyStatusBar(String iconTitle, int notificationId) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "Channel2")
                .setSmallIcon(android.R.color.transparent)
                .setContentTitle(iconTitle);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(notificationId, notification);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0 || grantResults[0] == -1) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }
        waitForPermissions = false;
    }

    @Override
    protected void onStop() {
        Timber.d("onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Timber.d("onDestroy");
        // Stop executorService when the Activity is about to be destroyed
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Timber.d("onNewIntent...intent: %s", intent.toUri(0));
        super.onNewIntent(intent);

        if (CoreOverrides.get().isDisableUITransitions()) {
            overridePendingTransition(0, 0);
        } else {
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_out_left);
        }

        /* for when we receive resources from a package such as resoptomany */
        if (APP_SEND_EFT_FILES_EVENT.equals(intent.getAction())) {
            try {
                POSitiveSvcLib.copyResources(this, intent, MalFactory.getInstance().getFile().getCommonDir(), false);
            } catch (Exception e) {
                Timber.e(e);
            }
            readyToShow = true;
        }
    }

    private void updateViews() {
        if (readyToShow) {
            Timber.d("showing splash logo");
            ImageView splashLogo = findViewById(R.id.splashlogo);
            TextView ver = findViewById(R.id.infoTitle);
            showInfo(splashLogo, ver, null);
        } else {
            Timber.d("not readyToShow");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume called, app hidden = %b", EFTPlatform.isAppHidden());


        updateViews();
        
        if (checkKeyboard()) {
            startApp();
        }

        if (EFTPlatform.isAppHidden()) {
            moveTaskToBack(true);
            Timber.e("Move task to back from splash screen onResume");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @SuppressWarnings("deprecation")
    public void showInfo(ImageView splashLogo, TextView ver, TextView name) {
        try {
            String basePath = MalFactory.getInstance().getFile().getCommonDir();
            if( Engine.getDep() != null && Engine.getDep().getConfig() != null ) {
                File imgFile = new File(basePath, Engine.getDep().getPayCfg().getBrandDisplayLogoSplashOrDefault());

                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    splashLogo.setImageBitmap(myBitmap);
                } else {
                    splashLogo.setImageResource(R.mipmap.splashlogo);
                }
            }

            PackageInfo pinfo = AppMain.getApp().getPackageManager().getPackageInfo(AppMain.getApp().getPackageName(), 0);
            String versionName = pinfo.versionName;
            String version = "(" + versionName + ")";
            if (ver != null) {
                if (Util.isNullOrEmpty(verText)) {
                    verText = ver.getText().toString();
                }
                version = verText + version;
                ver.setText(version);
            }

            PayCfg payCfg = new PayCfgFactory().getConfig(getApplicationContext());

            if (name != null && payCfg != null && !payCfg.getCustomerName().isEmpty()) {
                name.setText(payCfg.getCustomerName());
            }

        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    boolean checkKeyboard() {
        return true;
    }

    public void startApp() {
        // Initialize executorService
        if (executorService == null) {
            executorService = Executors.newSingleThreadScheduledExecutor();
        }

        executorService.scheduleAtFixedRate(() -> {
            if (!waitForPermissions) {
                // Currently catching any parse exception which is critical for the app and exiting from the app.
                // Any class parsing any critical file need to throw ConfigExceptions.ParseErrorException in order
                // to prevent payment app from working without that config file
                try {
                    StartupSequence.appStartRequest(ActSplash.this, true, MalFactory.getInstance());
                } catch (ConfigExceptions.ParseErrorException | ConfigExceptions.ValidationError e) {
                    executorService.shutdown();
                    Timber.e(e);
                    try {
                        runOnUiThread(() -> ActSplash.this.displayMessage(e.getMessage()));
                    }
                    catch (Exception ex) {
                        Timber.e(ex);
                    }
                }
                executorService.shutdown();
                executorService = null;
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }
}
