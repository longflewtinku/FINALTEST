package com.linkly.libengine.config;

import com.linkly.libconfig.DownloadCfg;
import com.linkly.libconfig.IMalConfig;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libsecapp.CtlsCfg;
import com.linkly.libsecapp.EmvCfg;

import java.util.List;

public interface IConfig extends IMalConfig {

    boolean loadConfig();

    PayCfg getPayCfg();

    void setPayCfg(PayCfg config);

    BinRangesCfg getBinRangesCfg();

    DownloadCfg getDownloadCfg();

    ProfileCfg getProfileCfg();

    EmvCfg getEmvCfg();

    CtlsCfg getCtlsCfg();

    BlacklistCfg getBlacklistCfg();

    // Returns a list of config parsing failures
    List<Exception> getConfigErrors();
}
