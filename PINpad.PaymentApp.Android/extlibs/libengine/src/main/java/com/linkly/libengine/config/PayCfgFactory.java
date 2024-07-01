package com.linkly.libengine.config;

import android.content.Context;

import com.linkly.libconfig.HotLoadParameters;
import com.linkly.libconfig.InitialParameters;
import com.linkly.libconfig.OverrideParameters;
import com.linkly.libengine.config.paycfg.PayCfgImpl;
import com.linkly.libmal.IMal;
import com.linkly.libmal.IMalFile;
import com.linkly.libmal.global.config.XmlParse;

import java.io.File;

import timber.log.Timber;

/***
 * Wrapper class for paycfg.
 */
public class PayCfgFactory {

    public static final String HOTLOADPARAMS_XML = "hotloadparams.xml";
    public static final String OVERRIDEPARAMS_XML = "overrideparams.xml";
    public static final String INITIALPARAMS_XML = "initialparams.xml";
    public static final String HOTLOADPARAMS_XSD = "hotloadparams.xsd";
    public static final String INITIALPARAMS_XSD = "initialparams.xsd";
    public static final String OVERRIDEPARAMS_XSD = "overrideparams.xsd";


    /***
     * Checks to see if we need to load config file.
     * This can be called externally
     * @param cfg our paycfgimpl config holder.
     * @param fileSystem mal filesystem to look where our folder is.
     * @return true if loaded config, false if nothing happened.
     */
    public boolean loadHotloadParams(PayCfg cfg, IMalFile fileSystem) {
        File hotloadFile = getConfigFile(fileSystem, HOTLOADPARAMS_XML);

        if(hotloadFile.exists() && requireConfigReload(cfg, hotloadFile)) {
            HotLoadParameters params = new XmlParse().parse(HOTLOADPARAMS_XML,  HotLoadParameters.class, HOTLOADPARAMS_XSD);
            return cfg.loadHotloadParams(params, hotloadFile.lastModified());
        }

        return false;
    }

    /***
     * Config params
     * @param cfg config object
     * @param fileSystem required for accessing the file.
     * @return return true loaded, false if not laaded.
     */
    private boolean loadInitialParams(PayCfg cfg, IMalFile fileSystem) {
        File initialFile = getConfigFile(fileSystem, INITIALPARAMS_XML);

        if(initialFile.exists() && requireConfigReload(cfg, initialFile)) {
            InitialParameters params = new XmlParse().parse(INITIALPARAMS_XML,  InitialParameters.class, INITIALPARAMS_XSD);
            return cfg.loadInitialParams(params, initialFile.lastModified());
        }

        return false;
    }


    /***
     * Load override params.
     * @param cfg config where we load
     * @param fileSystem filesystem
     * @param customerName customer name
     * @return returns true if config loaded. False if no config load happened.
     */
    private boolean loadOverrideParams(PayCfg cfg, IMalFile fileSystem, String customerName) {
        File overrideParams = getConfigFile(fileSystem, OVERRIDEPARAMS_XML);

        if(overrideParams.exists() && requireConfigReload(cfg, overrideParams)) {
            OverrideParameters params = new XmlParse().parse(OVERRIDEPARAMS_XML,  OverrideParameters.class, OVERRIDEPARAMS_XSD);
            return cfg.loadOverrideParams(customerName, params, overrideParams.lastModified());
        }

        return false;
    }


    /***
     * Generates a new paycfg that points to already loaded config.
     * Multiple copies will point to the same config.
     * @param context android config.
     * @return
     */
    public PayCfg getConfig(Context context) {
        return new PayCfgImpl(context);
    }

    /**
     * Performs our initialisation of config. Intended use is one time on start up or when new config is pushed to the terminal.
     * If called multiple times, still should be safe.
     * Once performed should setup our paycfg. Otherwise if obtaining config post start up should use getConfig
     * public PayCfg getConfig(Context context);
     * @param context android context
     * @param mal mal
     * @param customerName customer name obtained from ProfileConfig... this one is weird as it was everywhere.
     * @param softwareVersion the current version software
     * @return our initialised obejct of PayCfg.
     */
    public PayCfg initialiseConfig(Context context, IMal mal, String customerName, String softwareVersion) {
        PayCfgImpl cfg = new PayCfgImpl(context);

        // Load Initial Params
        if(!loadInitialParams(cfg, mal.getFile())) {
            Timber.e("Did Not Load Initial Params");
        }
        // Load Override Params
        if(!loadOverrideParams(cfg, mal.getFile(), customerName)) {
            Timber.e("Did Not Load Override Params");
        }

        if(!loadHotloadParams(cfg, mal.getFile())) {
            Timber.e("Did Not Load Hotload Params");
        }

        cfg.setPaymentAppVersion(softwareVersion);
        //cfg.debugAllParams();
        // Return our object.
        return cfg;
    }


    private File getConfigFile(IMalFile fileSystem, String fileName) {
        String cfgDirFile = fileSystem.getWorkingDir();
        String xmlFilePath = cfgDirFile + File.separator + fileName;
        return new File(xmlFilePath);
    }

    /***
     * Checks based on configuration stored in paycfg if the file has changed or not.
     * @param cfg config where the files have been parsed and stored
     * @return true if file is different OR not loaded, false
     */
    private boolean requireConfigReload(PayCfg cfg,File file) {
        long lastModified = 0;

        switch (file.getName()) {
            case HOTLOADPARAMS_XML:
                lastModified = cfg.getConfigFileMetaDataLastModifiedHotloadParams();
                break;
            case OVERRIDEPARAMS_XML:
                lastModified = cfg.getConfigFileMetaDataLastModifiedOverrideParams();
                break;
            case INITIALPARAMS_XML:
                lastModified = cfg.getConfigFileMetaDataLastModifiedInitialParams();
                break;
            default:
                Timber.e("File: %s not matching", file.getName());
                break;
        }

        return (lastModified == 0 || file.lastModified() != lastModified);
    }
}
