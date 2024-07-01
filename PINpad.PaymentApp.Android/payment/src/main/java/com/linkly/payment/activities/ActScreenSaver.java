package com.linkly.payment.activities;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.payment.R;
import com.linkly.payment.application.StartupSequence;
import com.linkly.payment.utilities.Pci24HourRebootWatcher;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class ActScreenSaver extends AppCompatActivity {

    private static Timer timerTask;
    private ImageView screenImage;
    public static int screenSaverTimeout = 10000;

    public DisplayKiosk.NavigationBarState state;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if (intent == null) {
                return;
            }

            if (IMessages.APP_CHARGING_EVENT.equals(intent.getAction())) {
                boolean deviceCharging = intent.getExtras().getBoolean("Charging");
                if (deviceCharging) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (MalFactory.getInstance().getHardware().processVolumeKey(getApplicationContext(), event))
            return true;

        return super.dispatchKeyEvent(event);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(StartupSequence.checkAppConfiguredPostCrash(this, false)) {
            this.finish();
            return;
        }

        setContentView(R.layout.activity_screensaver);
        //To make the status bar color green
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            IDependency d = Engine.getDep();
            if (d != null) {
                getWindow().setStatusBarColor(d.getPayCfg().getBrandDisplayStatusBarColourOrDefault(getColor(R.color.color_linkly_primary)));
            }
        }

        RelativeLayout root = (RelativeLayout) findViewById(R.id.rootViewrootView);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver, new IntentFilter("com.linkly.CHARGING"));

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = MalFactory.getInstance().getMalContext().registerReceiver(null, filter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean deviceCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        if (deviceCharging) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        root.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finishAfterTransition();
                Timber.e("Screensaver canceled");
            }
        });

        screenImage = (ImageView) findViewById(R.id.screenImage);

        Pci24HourRebootWatcher.setIdle();

        changeScreensaver();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        finishAfterTransition();
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
        Pci24HourRebootWatcher.clearIdle();
    }


    void changeScreensaver(){
        File imgFile;
        Bitmap myBitmap;

        String basePath = MalFactory.getInstance().getFile().getCommonDir();
        imgFile = new File(basePath + "/" + Engine.getDep().getPayCfg().getBrandDisplayLogoIdleOrDefault());
        if (imgFile.exists()) {
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            if (myBitmap != null) {
                screenImage.setImageBitmap(myBitmap);
            }
        } else {
            screenImage.setImageResource(R.mipmap.screensaver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        state = new DisplayKiosk.NavigationBarState();
        DisplayKiosk.getInstance().enterKioskMode(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        DisplayKiosk.getInstance().setNavigationBarAndButtonsState(state, true);
    }

    public static void displayScreenSaver(Context context) {
        Intent intent = new Intent(context, ActScreenSaver.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        DisplayKiosk.getInstance().onResume(false);
        Timber.e("Screensaver displayed");
    }

    public static void enableScreenSaver(Context context) {
        enableScreenSaver(false, context);
    }

    public static void enableScreenSaver(boolean noLoggoff, Context context){
        if (timerTask != null) {
            return;
        }

        timerTask = new Timer();
        SharedPreferences timeout = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            screenSaverTimeout = timeout.getInt("screenSaverTimer", 60);
            if (screenSaverTimeout < 1 || screenSaverTimeout > 120) {
                screenSaverTimeout = 60;
                timeout.edit().putInt("screenSaverTimer", screenSaverTimeout).apply();
            }
        } catch (Exception e) {
            screenSaverTimeout = 60;
            timeout.edit().putInt("screenSaverTimer", screenSaverTimeout).apply();
        }
        timerTask.schedule(new screenSaverTask(), screenSaverTimeout * 1000);

    }


    public static void cancelScreenSaver(){
        if (timerTask != null) {
            timerTask.cancel();
            timerTask.purge();
            timerTask = null;
        }
    }

    public static void resetScreenSaver(Context context){
        cancelScreenSaver();
        ActScreenSaver.enableScreenSaver(context);
    }

    private static class screenSaverTask extends TimerTask {

        @Override
        public void run() {
            displayScreenSaver(MalFactory.getInstance().getMalContext());
        }
    }


}
