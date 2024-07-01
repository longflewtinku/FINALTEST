package com.linkly.libengine.env;

/**
 * used by app to detect changes in terminal configuration
 */
public class LastUsedTerminalId extends EnvVar {
    private static final String ENV_VAR_NAME = "LAST_TERMINAL_ID";

    public static String getCurValue() {
        return getEnvValueString(ENV_VAR_NAME);
    }

    public static void setNewValue(String newValue) {
        setEnvValue(ENV_VAR_NAME, newValue);
    }

}
