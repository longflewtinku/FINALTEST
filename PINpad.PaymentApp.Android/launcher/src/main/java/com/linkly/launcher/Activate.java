package com.linkly.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.linkly.libmal.MalFactory;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libpositivesvc.downloader.DownloadDirector;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;

public class Activate extends AppCompatActivity {
    @SuppressWarnings("deprecation")
    ProgressDialog progressDialog = null;
    final int LicenceKey   =1;
    final int StoreKey     =2;
    final int MerchStoreID =3;
    final int DepartmentId =4;


     int itemsCount = StoreKey;

    static String merchStoreId;
    static String departId;
    static String licenceId;
    static String storeKeyId;

    public void selectTestEnv() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(UI.getInstance().getPrompt(String_id.STR_PLEASE_SELECT_ENV))
                .setCancelable(false)
                .setPositiveButton(UI.getInstance().getPrompt(String_id.STR_PREPROD), (dialog, id) -> requestActivationDetail(LicenceKey))
                .setNegativeButton(UI.getInstance().getPrompt(String_id.STR_QA), (dialog, id) -> requestActivationDetail(LicenceKey));
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opto_activate);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(BrandingConfig.getBrandDisplayStatusBarColourOrDefault( getColor(R.color.color_linkly_primary)) );
        }
        //Initialise the text
        TextView textActivate = findViewById(R.id.infoTitle);
        textActivate.setText(UI.getInstance().getPrompt(String_id.STR_ACTIVATING));

        String env = getIntent().getStringExtra("ENV");
        if (env != null && env.compareToIgnoreCase("TEST") == 0) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(UI.getInstance().getPrompt(String_id.STR_PLEASE_SELECT_ENV))
                    .setCancelable(false)
                    .setPositiveButton(UI.getInstance().getPrompt(String_id.STR_TEST), (dialog, id) -> selectTestEnv())
                    .setNegativeButton(UI.getInstance().getPrompt(String_id.STR_LIVE), (dialog, id) -> requestActivationDetail(LicenceKey));
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            requestActivationDetail(LicenceKey);
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver, new IntentFilter(IMessages.APP_SVC_PROGRESS_DIALOG));

    }


    @Override
    protected void onResume() {
        super.onResume();

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

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (IMessages.APP_SVC_PROGRESS_DIALOG.equals(intent.getAction())) {
                checkForProgressDialog(intent);
            }
        }
    };
    @SuppressWarnings("deprecation")
    public void checkForProgressDialog(Intent intent) {

        if (intent.getBooleanExtra("PROGRESS", false)) {
            String title = intent.getStringExtra("TITLE");
            String message = intent.getStringExtra("MESSAGE");

            try {

                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                if (Looper.myLooper() != null)
                    progressDialog = ProgressDialog.show(this, title, message, true);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else if (intent.getBooleanExtra("PROGRESSDISMISS", false)) {
            if(progressDialog !=null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

        }
    }
    @SuppressWarnings("deprecation")
    void requestActivationDetail(int index){
        Intent intent = new Intent(this, InputActivity.class);

        Bundle b = new Bundle();
        b.putInt(IUIDisplay.uiScreenMaxLen, 10);
        b.putInt(IUIDisplay.uiScreenMinLen, 0);

        if(index == MerchStoreID) {
            b.putSerializable(IUIDisplay.uiScreenID, IUIDisplay.SCREEN_ID.GET_INPUT_NUMBER);
            b.putString(IUIDisplay.uiScreenInputHint, UI.getInstance().getPrompt(String_id.STR_MERCHANT_STORE_ID));
            b.putString(IUIDisplay.uiScreenTitle, UI.getInstance().getPrompt(String_id.STR_STORE_ID));
            b.putString(IUIDisplay.uiScreenPrompt, UI.getInstance().getPrompt(String_id.STR_PLEASE_ENTER));
            //Default Value
            b.putString(IUIDisplay.uiScreenDefaultText, merchStoreId);
        } else if(index == DepartmentId) {
            b.putSerializable(IUIDisplay.uiScreenID, IUIDisplay.SCREEN_ID.GET_INPUT_NUMBER);
            b.putString(IUIDisplay.uiScreenInputHint, UI.getInstance().getPrompt(String_id.STR_DEPARTMENT_ID));
            b.putString(IUIDisplay.uiScreenTitle, UI.getInstance().getPrompt(String_id.STR_DEPARTMENT_ID));
            b.putString(IUIDisplay.uiScreenPrompt, UI.getInstance().getPrompt(String_id.STR_PLEASE_ENTER));
            //Default Value
            b.putString(IUIDisplay.uiScreenDefaultText,departId);
        } else if(index == LicenceKey) {
            b.putSerializable(IUIDisplay.uiScreenID, IUIDisplay.SCREEN_ID.GET_INPUT_ALPHA_NUMERIC_CAPS);
            b.putString(IUIDisplay.uiScreenInputHint, UI.getInstance().getPrompt(String_id.STR_LICENSE_KEY));
            b.putString(IUIDisplay.uiScreenTitle, UI.getInstance().getPrompt(String_id.STR_LICENSE_KEY));
            b.putString(IUIDisplay.uiScreenPrompt, UI.getInstance().getPrompt(String_id.STR_PLEASE_ENTER));
            //Default Value
            b.putString(IUIDisplay.uiScreenDefaultText, licenceId);
        } else if(index == StoreKey) {
            b.putSerializable(IUIDisplay.uiScreenID, IUIDisplay.SCREEN_ID.GET_INPUT_NUMBER);
            b.putString(IUIDisplay.uiScreenInputHint, UI.getInstance().getPrompt(String_id.STR_STORE_KEY));
            b.putString(IUIDisplay.uiScreenTitle, UI.getInstance().getPrompt(String_id.STR_STORE_KEY));
            b.putString(IUIDisplay.uiScreenPrompt, UI.getInstance().getPrompt(String_id.STR_PLEASE_ENTER));
            //Default Value
            b.putString(IUIDisplay.uiScreenDefaultText, storeKeyId);
        }

        intent.putExtras(b); //Put your id to your next Intent

        startActivityForResult(intent, index);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if ( resultCode == Activity.RESULT_OK ) {
            String value = data.getStringExtra( "resultText" );

            switch ( requestCode ) {
                case MerchStoreID:
                    merchStoreId = value;
                    break;
                case DepartmentId:
                    departId = value;
                    break;
                case LicenceKey:
                    licenceId = value;
                    break;
                case StoreKey:
                    storeKeyId = value;
                    break;
            }

            if ( requestCode == itemsCount ) {
                //All Parameters Received - do Activation
                DownloadDirector.activateDevice( this, merchStoreId, departId, licenceId, storeKeyId );
                startChecker();
            } else {
                requestActivationDetail( requestCode + 1 );

            }
        } else {
            finishAfterTransition();
        }
    }


    void startChecker(){
        TaskChecker task = new TaskChecker( this );
        Thread thread = new Thread(task);
        thread.start();
    }


    private static class TaskChecker implements Runnable {

        private Activity host;
        TaskChecker(Activity hostActivity) {
            super();
            host = hostActivity;
        }

        @Override
        public void run() {

            while (DownloadDirector.isInProgress()){
                MalFactory.getInstance().getHardware().sleepMS(500);
            }

            if(!DownloadDirector.isLastRequestSuccess()){

                DownloadDirector.newProgressDialog(UI.getInstance().getPrompt(String_id.STR_ACTIVATE_FAILED), UI.getInstance().getPrompt(String_id.STR_PLEASE_RETRY), true);
                DownloadDirector.dismissProgressDialog();
                host.finishAfterTransition();
                return;
            }

            host.runOnUiThread(() -> DownloadDirector.forceUpdate(host));

            while (!DownloadDirector.isInProgress()){
                MalFactory.getInstance().getHardware().sleepMS(100);
            }

            while (DownloadDirector.isInProgress()){
                MalFactory.getInstance().getHardware().sleepMS(100);
            }


            /* wait for it to complete */
            if(!DownloadDirector.isLastRequestSuccess()){
                DownloadDirector.newProgressDialog(UI.getInstance().getPrompt(String_id.STR_UPDATE_FAILED), UI.getInstance().getPrompt(String_id.STR_PLEASE_RETRY), true);
                DownloadDirector.dismissProgressDialog();
                host.finishAfterTransition();
                return;
            }

            host.finishAfterTransition();
        }

    }
}
