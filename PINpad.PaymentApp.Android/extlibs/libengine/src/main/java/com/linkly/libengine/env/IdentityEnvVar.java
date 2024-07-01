package com.linkly.libengine.env;

import com.linkly.libmal.global.util.Util;

/**
 * Child class of {@link EnvVar} which will be used to store & get Identity of
 * {@link com.linkly.libengine.config.PayCfg stid} & {@link com.linkly.libengine.config.PayCfg mid}
 * */

public class IdentityEnvVar extends EnvVar {
    private final String TAG = IdentityEnvVar.class.getSimpleName();
    /**
     * Environment variable Key for MID
     * */
    private static final String MID_KEY = "MID";
    /**
     * Environment variable Key for TID
     * */
    private static final String TID_KEY = "TID";

    /**
     * Sets MID environment variable in {@link EnvVarManager} using {@link #MID_KEY}
     * @param mid Merchant ID to be set, cannot be Null or empty
     * */
    public static void setMid( String mid ){
        if( !Util.isNullOrEmpty( mid ) ) {
            EnvVar.setEnvValue( MID_KEY, mid );
        }
    }

    /**
     * Sets TID environment variable in {@link EnvVarManager} using {@link #TID_KEY}
     * @param tid Terminal ID to be set, cannot be null or empty
     * */
    public static void setTid( String tid ){
        if( !Util.isNullOrEmpty( tid ) ) {
            EnvVar.setEnvValue( TID_KEY, tid );
        }
    }

    /**
     * Gets MID environment variable in {@link EnvVarManager} using {@link #MID_KEY}
     * @return Merchant Id: Can be null or empty
     * */
    public static String getMid( ){
        return EnvVar.getEnvValueString( MID_KEY );
    }

    /**
     * Gets TID environment variable in {@link EnvVarManager} using {@link #TID_KEY}
     * @return Terminal ID: Can be null or empty
     * */
    public static String getTid( ){
        return EnvVar.getEnvValueString( TID_KEY );
    }

}
