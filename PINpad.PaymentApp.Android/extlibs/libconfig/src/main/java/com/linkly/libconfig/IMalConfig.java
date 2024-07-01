package com.linkly.libconfig;

public interface IMalConfig {

    boolean loadConfig();

    boolean isConfigLoaded();

    DownloadCfg getDownloadCfg();

    ProfileCfg getProfileCfg();
}
