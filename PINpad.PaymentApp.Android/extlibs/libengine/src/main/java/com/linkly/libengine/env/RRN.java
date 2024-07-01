package com.linkly.libengine.env;

public class RRN extends EnvVar {
    private static final String envVarName = "RRN";

    RRN() {
    }

    public static Integer getCurValue() {
        return getEnvValueInteger(envVarName);
    }

    public static Integer getNewValue() {
        return getIntegerAutoIncrement( envVarName, 999999 );
    }
}

