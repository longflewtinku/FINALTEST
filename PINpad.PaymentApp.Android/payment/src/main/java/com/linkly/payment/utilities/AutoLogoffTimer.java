package com.linkly.payment.utilities;

import static com.linkly.libpositive.messages.IMessages.APP_FINISH_TRANSACTION_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_FINISH_UI_EVENT;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.config.EnvCfg;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class AutoLogoffTimer  {

    private static AutoLogoffTimer autoLogoffTimer = null;
    private Timer logoffTimer = null;

    private AutoLogoffTimer() {
    }

    private void start() {
        cancel();

        logoffTimer = new Timer();
        logoffTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Timber.d("run[logoffTimer]...");
                try {
                    Timber.e("App Timed Out");
                    if (EnvCfg.getInstance().readValue("userID").isEmpty() || !UserManager.isAutoUserLogin(Engine.getDep(), MalFactory.getInstance().getMalContext())) {
                        Timber.e("Logging out current user");
                        UserManager.logoutActiveUser();
                        Intent tempIntent = new Intent();
                        tempIntent.setAction(APP_FINISH_UI_EVENT);
                        LocalBroadcastManager.getInstance(MalFactory.getInstance().getMalContext()).sendBroadcast(tempIntent);
                        Intent alsoFinishActTransactionIntent = new Intent();
                        alsoFinishActTransactionIntent.setAction(APP_FINISH_TRANSACTION_EVENT);
                        LocalBroadcastManager.getInstance(MalFactory.getInstance().getMalContext()).sendBroadcast(alsoFinishActTransactionIntent);
                    }
                } catch ( Exception e) {
                    Timber.e("AutoLogoff timer error");
                }
            }
        }, getAutoLogoffTimeout());
    }

    private void cancel() {
        if (logoffTimer != null) {
            logoffTimer.cancel();
            logoffTimer = null;
        }
    }

    private static long getAutoLogoffTimeout() {
        SharedPreferences timeout = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(MalFactory.getInstance().getMalContext()));
        final String AUTO_LOGOFF_TIMEOUT = "autoLogoffTimeout";

        int logoffTimeout = timeout.getInt(AUTO_LOGOFF_TIMEOUT, 30);
        if (logoffTimeout == 0) {
            logoffTimeout = 1;
            timeout.edit().putInt(AUTO_LOGOFF_TIMEOUT, logoffTimeout).apply();
        }

        return timeout.getInt(AUTO_LOGOFF_TIMEOUT, 30) * 60 * 1000L;
    }

    public static void cancelTimer() {
        if (autoLogoffTimer != null) {
            autoLogoffTimer.cancel();
            autoLogoffTimer = null;
        }
    }

    public static void startTimer() {
        if (autoLogoffTimer == null) {
            autoLogoffTimer = new AutoLogoffTimer();
        }
        if (PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(MalFactory.getInstance().getMalContext())).getBoolean("autoLogoff", true)) {
            autoLogoffTimer.start();
        }
    }

}
