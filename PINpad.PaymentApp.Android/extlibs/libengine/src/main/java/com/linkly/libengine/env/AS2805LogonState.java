package com.linkly.libengine.env;

import timber.log.Timber;

public class AS2805LogonState extends EnvVar {
    private static final String envVarName = "AS2805LOGON_STATE"; // indicate if RSA had been done
    private static final String TAG = AS2805LogonState.class.getSimpleName();

    public enum LogonState {
        RSA_LOGON, // rsa key init part 1
        KTM_LOGON, // rsa key init part 2
        KEK_LOGON, // rsa key init part 3
        SESSION_KEY_LOGON, // session key exchange - includes KEK rolling, always follows on from part 3
        LOGON_REQUIRED, // session key exchange/regular logon. no KEK rolling
        FILE_UPDATE_REQUIRED,
        LOGGED_ON,
        DUKPT_REGISTRATION_REQUIRED, // DUKPT registration required (for Wpay DUKPT terminal)
    }

    AS2805LogonState() {
    }

    public static LogonState getCurValue() {
        // retrieve as int. returns zero if not found, which will map to RSA_LOGON, which is what we want
        int intVal = getEnvValueInteger(envVarName);
        return LogonState.values()[intVal];
    }

    public static void setNewValue( LogonState value ) {
        // set as int
        Timber.i( "Setting AS2805 logon state to " + value.toString() );
        setEnvValue(envVarName, value.ordinal());
    }
}
