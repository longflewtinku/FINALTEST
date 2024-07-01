package com.linkly.libengine.engine.transactions;

import com.linkly.libengine.env.EnvVar;

public class PanHash extends EnvVar {
    private static final String envVarName = "MASKED_PAN_HASH";

    PanHash() {
    }

    public static String getCurValue() {
        return getEnvValueString(envVarName);
    }

    public static void setNewValue( String value ) {
        setEnvValue( envVarName, value );
    }
}
