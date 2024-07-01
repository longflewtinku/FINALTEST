package com.linkly.launcher;


import static com.linkly.libpositive.messages.IMessages.APP_SVC_PROGRESS_DIALOG;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.linkly.launcher.fragments.PairWithBaseStationFragment;
import com.linkly.launcher.fragments.WiFiConfigProgressFragment;
import com.linkly.libmal.MalFactory;


@SuppressWarnings("deprecation")
public class WifiBaseActivity extends AppCompatActivity {
    ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(BrandingConfig.getBrandDisplayStatusBarColourOrDefault(getColor(R.color.color_linkly_primary)));
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        WiFiConfigProgressFragment progressFragment = (WiFiConfigProgressFragment) fragmentManager.findFragmentById(R.id.current_progress_fragment);

        if (progressFragment == null) {
            progressFragment = new WiFiConfigProgressFragment();
            fragmentManager.beginTransaction().replace(R.id.current_progress_fragment, progressFragment).commit();
        }

        Fragment pairBSFragment = fragmentManager.findFragmentById(R.id.settings_fragment);

        if (pairBSFragment == null) {
            pairBSFragment = new PairWithBaseStationFragment();
            fragmentManager.beginTransaction().replace(R.id.settings_fragment, pairBSFragment).addToBackStack(null).commit();
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver, new IntentFilter(APP_SVC_PROGRESS_DIALOG));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }


    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (APP_SVC_PROGRESS_DIALOG.equals(intent.getAction())) {
                checkForProgressDialog(intent);
            }
        }
    };

    public void checkForProgressDialog(Intent intent) {

        if (intent.getBooleanExtra("PROGRESS", false)) {
            String title = intent.getStringExtra("TITLE");
            String message = intent.getStringExtra("MESSAGE");
            Boolean update  = intent.getBooleanExtra("UPDATE", false);

            try {

                if (progressDialog != null && !update) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }

                if (update && progressDialog != null) {
                    progressDialog.setMessage(message);
                }
                else if (Looper.myLooper() != null) {
                    progressDialog = ProgressDialog.show(this, title, message, true);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else if (intent.getBooleanExtra("PROGRESSDISMISS", false)) {
            if(progressDialog !=null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
    }

    public static void newProgressDialog(String title, String message, boolean update) {

        try {
            Intent intent = new Intent();
            intent.setAction(APP_SVC_PROGRESS_DIALOG);
            intent.putExtra("PROGRESS", true);
            intent.putExtra("TITLE", title);
            intent.putExtra("MESSAGE", message);
            intent.putExtra("UPDATE", update);
            LocalBroadcastManager.getInstance(MalFactory.getInstance().getMalContext()).sendBroadcast(intent);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void dismissProgressDialog(int delayInMillis) {

        try {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent intent = new Intent();
                    intent.setAction(APP_SVC_PROGRESS_DIALOG);
                    intent.putExtra("PROGRESSDISMISS", true);
                    LocalBroadcastManager.getInstance(MalFactory.getInstance().getMalContext()).sendBroadcast(intent);
                }
            }, delayInMillis);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
