package com.linkly.libengine.env;
import com.linkly.libmal.global.util.Util;

import timber.log.Timber;

public class LastLogonTime extends EnvVar {
    private static final String envVarName = "LAST_LOGON_TIME";

    LastLogonTime() {
    }

    public static String getCurValue() {
        return getEnvValueString(envVarName);
    }

    public static void setNewValue(String value) {
        setEnvValue(envVarName, value);
    }

    /**
     * sets env var to current clock time
     */
    public static void setToNow() {
        setNewValue( Util.getNow() );
    }

    /**
     * uses current clock time and compares to last logon time, returns true if >= 1 day has passed
     * if no value has been set, returns true to trigger logon
     *
     * @return false if < 1 day has elapsed since last logon, true if >= 1 days have elapsed
     */
    public static boolean isOverOneDay() {
        if( Util.isNullOrEmpty(getCurValue())) {
            Timber.w( "Env var not set, returning true to trigger logon" );
            return true;
        }
        return Util.isOverOneDay( getCurValue(), Util.getNow() );
    }
}
