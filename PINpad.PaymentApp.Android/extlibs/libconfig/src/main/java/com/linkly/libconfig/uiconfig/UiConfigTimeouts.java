package com.linkly.libconfig.uiconfig;

import android.content.SharedPreferences;

import com.linkly.libconfig.OverrideParameters;
import com.linkly.libmal.global.util.Util;

// MW: I haven't written Unit tests for these because I would have to figure out how to parse
// OverrideParameters.xml file first & all of that code kind of sits in CPAT branch.
// So for now, I will leave it as it is.

/**
 * Will contain timeouts for screens which need to be configured via TMS
 */
public class UiConfigTimeouts {

    SharedPreferences uiConfigTimeoutPreferences;
    private final String accessModeSuffix;

    private static final int DEFAULT_TIMEOUT = 60 * 1000;
    private static final int DEFAULT_TIMEOUT_ACCESS_MODE = 240 * 1000;


    public UiConfigTimeouts(SharedPreferences overrideParamsPreferences, String accessModeSuffixId) {
        uiConfigTimeoutPreferences = overrideParamsPreferences;
        accessModeSuffix = accessModeSuffixId;
    }


    /**
     * Returns timeout from config. If not value exists will return
     * @param timeout timeout type that is expected to be returned.
     * @param isAccessMode if the terminal is in access mode
     * @return returns the timeout value, if not value exists will return default values.
     */
    public int getTimeoutMilliSecs( ConfigTimeouts timeout, boolean isAccessMode ){
        String id = timeout.name() + ((isAccessMode) ? accessModeSuffix : "");

        return uiConfigTimeoutPreferences.getInt(id, ((isAccessMode) ? DEFAULT_TIMEOUT_ACCESS_MODE : DEFAULT_TIMEOUT));
    }
}
