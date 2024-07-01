package com.linkly.payment.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.linkly.payment.application.StartupSequence;

import timber.log.Timber;

public class ActLauncher extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("ActLauncher onCreate");
        super.onCreate(savedInstanceState);

        StartupSequence.ServiceStartRequest(getApplicationContext());

        finishAfterTransition();
    }
}

