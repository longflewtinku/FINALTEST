package com.linkly.libengine.env;

import com.linkly.libengine.engine.Engine;

public class BatchNumber extends EnvVar {
    private static final String envVarName = "BATCH_NO";
    private static final int BATCH_NUMBER_MAX = 999999;

    BatchNumber() {
    }


    public static Integer getNewValue() {
        int max = Engine.getProtocol() == null ? BATCH_NUMBER_MAX : Engine.getProtocol().getMaxBatchNumber();
        return getIntegerAutoIncrement(envVarName, max);
    }

    public static Integer getCurValue() {
        int max = Engine.getProtocol() == null ? BATCH_NUMBER_MAX : Engine.getProtocol().getMaxBatchNumber();
        int ret = getEnvValueInteger(envVarName);

        // always start at no 1
        if (ret == 0 || ret > max ) {
            setEnvValue(envVarName, 1);     // In case the env-var does not exist yet.
            ret = 1;
        }

        return ret;
    }

    public static void setNewValue(Integer newValue) {
        setEnvValue(envVarName, newValue);
    }



}
