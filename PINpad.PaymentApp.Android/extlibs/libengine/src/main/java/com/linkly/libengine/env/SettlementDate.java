package com.linkly.libengine.env;

public class SettlementDate extends EnvVar {
    private static final String ENV_VAR_NAME = "SETTLEMENT_DATE";

    SettlementDate() {
    }

    /**
     * gets env var as current stored settlement date
     */
    public static String getCurValue() {
        return getEnvValueString(ENV_VAR_NAME);
    }

    /**
     * sets env var with provided settlement date
     */
    public static void setNewValue(String value) {
        setEnvValue(ENV_VAR_NAME, value);
    }

    /**
     * Check the settlement date with stored value
     *
     * @return true if not the same, false if same settlement date
     * @Param settlementDate String input of settlement date to be compared against stored settlement date
     */
    public static boolean isSettlementDateChanged(String settlementDate) {
        return !settlementDate.equals(getCurValue());
    }
}
