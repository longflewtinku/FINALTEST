package com.linkly.libengine.env;

public class IncrementDukptKsn extends EnvVar {
    private static final String envVarName = "INCREMENT_KSN";

    public static boolean getCurValue() {
        return getEnvValueBoolean(envVarName);
    }

    public static void setNewValue(boolean newValue) {
        setEnvValue(envVarName, newValue);
    }
}
