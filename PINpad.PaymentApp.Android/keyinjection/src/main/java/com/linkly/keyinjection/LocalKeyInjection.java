package com.linkly.keyinjection;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import timber.log.Timber;

public class LocalKeyInjection extends AppCompatActivity implements KeyListViewAdapter.ItemClickListener {
    ArrayList<KeySet.KeyVal> testKeysList;
    KeyListViewAdapter adapter;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_key_injection);
        //display version number
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pinfo.versionName;
            TextView textVersion = (TextView) findViewById(R.id.text_version_no);
            if (textVersion != null) {
                textVersion.setText(getText(R.string.version) + version);
            }

        } catch (PackageManager.NameNotFoundException e) {
            Timber.w(e);
        }

        // Check if there is keyset.json available in filesystem
        File file = new File(getApplicationContext().getFilesDir(), "keysets.json");
        String keysetsjson = "";
        try {
            if (file.exists()) {
                keysetsjson = inputStreamToString(new FileInputStream(getApplicationContext().getFilesDir() + "/" + "keysets.json"));
            } else {
                keysetsjson = inputStreamToString(getResources().getAssets().open("keysets.json"));
            }
            KeySet keyset = new Gson().fromJson(keysetsjson, KeySet.class);
            testKeysList = keyset.key;
        } catch (FileNotFoundException e) {
            Timber.w(e);
        } catch (IOException e) {
            Timber.w(e);
        }

        //Display the list in a recyclerview
        RecyclerView recyclerView = findViewById(R.id.rvkeys);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new KeyListViewAdapter(this, testKeysList);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        //Set Remote key Injection
        Button btnRKI = findViewById(R.id.btn_rki);
        btnRKI.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction("com.linkly.APP_SEND_KEY_INJECTION");
            intent.putExtra("INJECTION_TYPE", "REMOTE");
            sendBroadcast(intent);
        });
    }

    public String inputStreamToString(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, bytes.length);
            String json = new String(bytes);
            return json;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void onItemClick(View view, int position) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        KeySet.KeyVal keyVal = testKeysList.get(position);

        //Send the key data to SecApp Via intent
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.linkly.secapp", "com.linkly.secapp.service.P2PEReceiver"));
        intent.setAction("com.linkly.APP_SEND_KEY_INJECTION");
        intent.putExtra("INJECTION_TYPE", "TEST");
        intent.putExtra("SKTCU_MOD", keyVal.skTcuMod);
        intent.putExtra("SKTCU_EXP", keyVal.skTcuExp);
        intent.putExtra("SK_MAN_PK_TCU_MOD", keyVal.skManPkTcuMod);
        intent.putExtra("SK_MAN_PK_TCU_EXP", keyVal.skManPkTcuExp);
        intent.putExtra("PPID", keyVal.ppid);
        intent.putExtra("KEEP_CURRENT_KEYS", keyVal.keepCurrentKeys);

        if (keyVal.dukptRandomTrsm && keyVal.dukptIssuerId.length() != 0 && keyVal.dukptBdkIdx.length() != 0 && keyVal.dukptVendorId.length() != 0) {
            // generates a ksn with provided issuer id, bdk idx, vendor id, and randomised trsm
            String ksn = DukptKeys.generateKsn(keyVal);
            String initialKey = DukptKeys.generateInitialKey(keyVal.dukptBdk, ksn);
            intent.putExtra("DUKPT_INITIAL_KEY", initialKey);
            intent.putExtra("DUKPT_INITIAL_KSN", ksn);
            intent.putExtra("DUKPT_KEY_SLOT", keyVal.dukptKeySlot);
        } else if(keyVal.dukptInitialKey != null && keyVal.dukptInitialKsn != null){
            // regular dukpt
            intent.putExtra("DUKPT_INITIAL_KEY", keyVal.dukptInitialKey);
            intent.putExtra("DUKPT_INITIAL_KSN", keyVal.dukptInitialKsn);
            intent.putExtra("DUKPT_KEY_SLOT", keyVal.dukptKeySlot);
        }
        sendBroadcast(intent);

    }



    @Override
    protected void onStart() {
        super.onStart();
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
    }
}