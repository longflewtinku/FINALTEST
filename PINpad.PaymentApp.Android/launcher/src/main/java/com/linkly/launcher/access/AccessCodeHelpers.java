package com.linkly.launcher.access;

import static com.linkly.launcher.ServiceFrontEnd.ACCESSCODE_ADMIN_MENU;
import static com.linkly.launcher.ServiceFrontEnd.ACCESSCODE_EXIT;
import static com.linkly.launcher.ServiceFrontEnd.ACCESSCODE_UNATTENDED_ESCAPEE;

import com.linkly.launcher.BuildConfig;

import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

/*
Not Authentication per se, so wrapped and renamed to be about Access Codes.
 */
public class AccessCodeHelpers {
    private AccessCodeHelpers() {}

    public static void checkAccessCode(String password, int pwdType, AccessCodeCheckCallbacks callbacks) {
        Timber.d("checkAccessCode...");
        // password
        String p = "";

        if (pwdType == ACCESSCODE_ADMIN_MENU || pwdType == ACCESSCODE_UNATTENDED_ESCAPEE) {
            p = String.format( Locale.getDefault(), "3123%03d", Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        } else if (pwdType == ACCESSCODE_EXIT) {
            p = String.format( Locale.getDefault(), "000000%03d", Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        }


        if (BuildConfig.DEBUG) {
            p = "166831";
        }

        if (pwdType == ACCESSCODE_ADMIN_MENU && password.compareTo(p) == 0) {
            callbacks.onAdminMenuGranted();
        } else if (pwdType == ACCESSCODE_EXIT && password.compareTo(p) == 0) {
            callbacks.onExitLauncherGranted();
        } else if (pwdType == ACCESSCODE_UNATTENDED_ESCAPEE && password.compareTo(p) == 0) {
            callbacks.onEnterUnattendedServiceModeModeGranted();
        } else {
            // fail closed. Note this should be the very last thing this method ever does.
            callbacks.onAccessDenied();
        }
    }
}
