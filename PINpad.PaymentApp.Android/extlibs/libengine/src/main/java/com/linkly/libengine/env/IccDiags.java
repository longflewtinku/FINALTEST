package com.linkly.libengine.env;

public class IccDiags extends EnvVar {
    private static final String envVarName = "ICC_DIAGS";

    IccDiags() {
    }

    public static boolean getCurValue() {
        return getEnvValueBoolean(envVarName);
    }

    public static void setNewValue( boolean value ) {
        setEnvValue(envVarName, value);
    }
}
