package com.linkly.libengine.env;

public class Stan extends EnvVar {
    private static final String envVarName = "STAN";

    Stan() {
    }

    public static Integer getNewValue() {
        return getIntegerAutoIncrement( envVarName, 999999 );
    }

    public static Integer getCurValue() {
        Integer ret = getEnvValueInteger(envVarName);

        // always start at no 1
        if( ret == 0 )
            ret = 1;

        return ret;
    }

    public static void setNewValue(Integer newValue) {
        setEnvValue(envVarName, newValue);
    }

}
