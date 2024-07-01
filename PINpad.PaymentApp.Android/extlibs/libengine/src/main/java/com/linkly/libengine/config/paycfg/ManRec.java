package com.linkly.libengine.config.paycfg;

import android.content.SharedPreferences;

public class ManRec {
    private final SharedPreferences preferences;

    public ManRec(SharedPreferences pref) {
        preferences = pref;
    }


    public String getStartTime() {
        return preferences.getString("StartTime", "");
    }

    public String getEndTime() {
        return preferences.getString("EndTime", "");
    }

    public boolean isEnabled() {
        return preferences.getBoolean("Enabled", false);
    }
}
