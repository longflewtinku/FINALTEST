package com.linkly.libengine.env;

public class TableVersion extends EnvVar {
    private static final String envVarName1 = "EPAT_VERSION";
    private static final String envVarName2 = "PKT_VERSION";
    private static final String envVarName3 = "FCAT_VERSION";
    private static final String envVarName4 = "CPAT_VERSION";

    TableVersion() {
    }

    public static String getEpatVersion() {
        return getEnvValueString(envVarName1);
    }
    public static void setEpatVersion(String newValue) {
        setEnvValue(envVarName1, newValue);
    }

    public static String getPktVersion() {
        return getEnvValueString(envVarName2);
    }
    public static void setPktVersion(String newValue) {
        setEnvValue(envVarName2, newValue);
    }

    public static String getFcatVersion() {
        return getEnvValueString(envVarName3);
    }
    public static void setFcatVersion(String newValue) {
        setEnvValue(envVarName3, newValue);
    }

    public static String getCpatVersion() {
        return getEnvValueString(envVarName4);
    }
    public static void setCpatVersion(String newValue) {
        setEnvValue(envVarName4, newValue);
    }
}
