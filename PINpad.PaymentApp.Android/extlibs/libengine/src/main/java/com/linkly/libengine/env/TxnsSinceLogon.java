package com.linkly.libengine.env;

public class TxnsSinceLogon extends EnvVar {
    private static final String envVarName = "TXNS_SINCE_LOGON";

    TxnsSinceLogon() {
    }

    public static Integer getNewValue() {
        return getIntegerAutoIncrement( envVarName, 999999 );
    }

    public static Integer getCurValue() {
        return getEnvValueInteger(envVarName);
    }

    public static void reset() {
        setNewValue(0);
    }

    public static void increment() {
        setNewValue( getCurValue() + 1 );
    }

    private static void setNewValue(Integer newValue) {
        setEnvValue(envVarName, newValue);
    }

}
