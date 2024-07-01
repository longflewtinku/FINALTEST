package com.linkly.libengine.env;

public class TxnsNoReponse extends EnvVar {
    private static final String ENVVARNAME = "TXNS_NORESPONSE";

    TxnsNoReponse() {
    }

    public static Integer getNewValue() {
        return getIntegerAutoIncrement(ENVVARNAME, 10);
    }

    public static Integer getCurValue() {
        return getEnvValueInteger(ENVVARNAME);
    }

    public static void reset() {
        setNewValue(0);
    }

    public static void increment() {
        setNewValue( getCurValue() + 1 );
    }

    private static void setNewValue(Integer newValue) {
        setEnvValue(ENVVARNAME, newValue);
    }

}
