package com.linkly.payment.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.payment.R;
import com.linkly.payment.fragments.FragSettings;
import com.linkly.payment.fragments.FragTotalsSettings;

public class ActTotalsSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_totals_settings);
        //To make the status bar color green
        IDependency d = Engine.getDep();
        if (d != null) {
            getWindow().setStatusBarColor(d.getConfig().getPayCfg().getBrandDisplayStatusBarColourOrDefault(getColor(R.color.color_linkly_primary)));
        }
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new FragTotalsSettings())
                .commit();
    }
}
