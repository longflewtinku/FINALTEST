package com.linkly.libengine.config;

import com.linkly.libmal.global.config.JSONParse;

import timber.log.Timber;

public class MaestroCfg {

    private static final String TAG = "MaestroCfg";
    private static MaestroCfg ourInstance = new MaestroCfg();
    private MaestroCfg.BinRange maestro;

    private MaestroCfg() {
    }

    public static MaestroCfg getInstance() {
        if (ourInstance == null) {
            ourInstance = new MaestroCfg();
        }
        return ourInstance;
    }

    /***************************************************************/
    /* debug all of the config */

    public MaestroCfg parse() {

        try {
            JSONParse j = new JSONParse();
            ourInstance = (MaestroCfg)j.parse("cfg_mae.json", MaestroCfg.class);

        } catch (Exception e) {
            Timber.w(e);
            ourInstance = null;
        }
        return ourInstance;

    }

    public BinRange getMaestro() {
        return this.maestro;
    }

    public static class BinRange {
        private String iinRange;

        public String getIinRange() {
            return this.iinRange;
        }
    }

}