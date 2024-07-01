package com.linkly.launcher;

import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class FakeLauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //To make the status bar color green
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(BrandingConfig.getBrandDisplayStatusBarColourOrDefault(getColor(R.color.color_linkly_primary)));
        }
    }
}

