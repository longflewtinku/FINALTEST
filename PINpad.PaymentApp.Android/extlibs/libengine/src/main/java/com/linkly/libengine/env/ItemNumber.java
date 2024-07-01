package com.linkly.libengine.env;

public class ItemNumber extends EnvVar {
    private static final String envVarName = "ITEM_NO";

    ItemNumber() {
    }


    public static Integer getNewValue() {
        return getIntegerAutoIncrement(envVarName, 999);
    }

    public static Integer getCurValue() {
        Integer ret = getEnvValueInteger(envVarName);
        return ret;
    }

    public static void setNewValue(Integer newValue) {
        setEnvValue(envVarName, newValue);
    }



}
