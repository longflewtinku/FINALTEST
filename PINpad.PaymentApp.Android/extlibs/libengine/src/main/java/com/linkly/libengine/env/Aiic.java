package com.linkly.libengine.env;

public class Aiic extends EnvVar {
    private static final String envVarName = "AIIC";

    Aiic() {
    }

    public static String getCurValue() {
        return getEnvValueString(envVarName);
    }

    public static void setNewValue( String value ) {
        setEnvValue(envVarName, value);
    }
}
