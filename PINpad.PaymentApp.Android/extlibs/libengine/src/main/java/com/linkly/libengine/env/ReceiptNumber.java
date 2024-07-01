package com.linkly.libengine.env;

public class ReceiptNumber extends EnvVar {
    private static final String envVarName = "RECEIPT_NO";

    ReceiptNumber() {
    }

    public static Integer getCurValue() {
        return getEnvValueInteger(envVarName);
    }

    public static Integer getNewValue() {
        return getIntegerAutoIncrement( envVarName, 999999 );
    }

    public static void setNewValue(Integer newValue) {
        setEnvValue(envVarName, newValue);
    }

}
