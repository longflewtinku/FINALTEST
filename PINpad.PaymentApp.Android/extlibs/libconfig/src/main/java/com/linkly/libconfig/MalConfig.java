package com.linkly.libconfig;

import timber.log.Timber;

public class MalConfig implements IMalConfig {

    private static final String TAG = "MalConfig";
    private static MalConfig ourInstance = new MalConfig();
    private boolean configLoaded = false;
    protected MalConfig() {
    }

    public static MalConfig getInstance() {
        return ourInstance;
    }

    public boolean isConfigLoaded() {
        return configLoaded;
    }

    public boolean loadConfig() {

        final long startTime = System.currentTimeMillis();
        /* get the download config first */
        DownloadCfg.getInstance().parse();
        final long endTime = System.currentTimeMillis();
        configLoaded = true;
        Timber.i("Total MalConfig Parse Time: " + (endTime - startTime));
        return true;
    }

    public DownloadCfg getDownloadCfg() {
        return DownloadCfg.getInstance();
    }

    public ProfileCfg getProfileCfg() {
        return ProfileCfg.getInstance();
    }

}
