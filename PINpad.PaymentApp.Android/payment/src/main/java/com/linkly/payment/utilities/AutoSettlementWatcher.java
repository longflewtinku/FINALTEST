package com.linkly.payment.utilities;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static com.linkly.libpositive.events.PositiveEvent.EventType.AUTO_SETTLEMENT;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.PowerManager;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.jobs.EFTJob;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.payment.workflows.ActBaseCheckReconciliationResult;

import java.util.Calendar;

import timber.log.Timber;

// TODO: Shift this to a work manager.
public class AutoSettlementWatcher {

    // Some log entries have the "Error" level. For initial testing on "release" version. Can be removed or changed to "information" after testing done

    private static InWindowRetryCountDownTimer inWindowRetryCountDownTimer = null;
    private static AutoSettlementIdleTimer autoSettlementIdleTimer = null;
    private static boolean autoSettlementPending = false;   // flag that autosettlement needs to be performed when all timers finished
    private static boolean autoSettlementTimerTaskScheduled = false; // autosettlement scheduled to next day
    private static boolean inWindowRetryCountDownTimerTriggered = false;
    private static final int COUNTDOWN_TIMER_INTERVAL_MS = 1000; // 1 second
    private static final int MILLIS_IN_A_24H = 24 * 60 * 60 * 1000;
    private static final int IN_WINDOW_RETRY_TIMEOUT = 5 * 60 * 1000; // 5 Minutes
    private static boolean initialized = false;
    private static boolean configAutoSettlementEnabled = false;
    private static int configAutoSettlementHours = 0;
    private static int configAutoSettlementMinutes = 0;
    private static int configAutoSettlementIdleMS = 30 * 60 * 1000;
    private static int configAutoSettlementWindow = 60 * 60 * 1000;
    private static PowerManager.WakeLock wakeLock = null;

    private AutoSettlementWatcher() {
    }

    static class InWindowRetryCountDownTimer extends CountDownTimer {

        InWindowRetryCountDownTimer() {
            super(IN_WINDOW_RETRY_TIMEOUT, COUNTDOWN_TIMER_INTERVAL_MS);
        }

        @Override
        public void onTick(long l) {
            // The method is an intentionally-blank override.
        }

        @Override
        public void onFinish() {
            synchronized(this) {
                Timber.i("InWindowRetryCountDownTimer Finished");
                this.cancel();
                setInWindowRetryCountDownTimerTriggered(false);
                autoSettlementPending = true;
                // InWindowRetryCountDownTimer Finished, check if AutoSettlement is ready to go
                checkAutoSettlementConditions(MalFactory.getInstance().getMalContext()); // No way around this... Should be shifted to workmanager.
            }
        }
    }

    private static void readConfigParameters(PayCfg payCfg) {
        configAutoSettlementEnabled = payCfg.isAutoSettlementEnabled();
        try {
            configAutoSettlementIdleMS = Integer.parseInt(payCfg.getAutoSettlementIdlingPeriod()) * 60 * 1000;    // convert from minutes to ms
            configAutoSettlementWindow = Integer.parseInt(payCfg.getAutoSettlementTimeWindow()) * 60 * 1000;    // convert from minutes to ms
        } catch (NumberFormatException ex) {
            Timber.e("format error");
        }

        if (configAutoSettlementWindow == 0 || configAutoSettlementWindow > MILLIS_IN_A_24H ) {
            Timber.e("set AutoSettlementWindow to 24 Hours");
            configAutoSettlementWindow = MILLIS_IN_A_24H;
        }

        String configAutoSettlementTime = payCfg.getAutoSettlementTime();
        if (!Util.isNullOrEmpty(configAutoSettlementTime)) {
            int i;
            try {
                i = Integer.parseInt(configAutoSettlementTime);
            } catch (NumberFormatException e) {
                i = 0;
            }

            if (i / 100 <= 23 || i % 100 <= 59) {
                configAutoSettlementHours = i / 100;
                configAutoSettlementMinutes = i % 100;
            }
        }
    }

    private static boolean wasSettlementPerformedToday(Context context) {
        Calendar settlementCompletedDateTime = Calendar.getInstance();
        settlementCompletedDateTime.setTimeInMillis(ActBaseCheckReconciliationResult.getLastCompletedSettlementTimestamp(context));
        Calendar calendar = Calendar.getInstance();
        return settlementCompletedDateTime.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                settlementCompletedDateTime.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                settlementCompletedDateTime.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH);
    }

    private static boolean isInWindow() {
        Calendar windowStart = Calendar.getInstance();
        windowStart.set(Calendar.HOUR_OF_DAY, configAutoSettlementHours);
        windowStart.set(Calendar.MINUTE, configAutoSettlementMinutes);
        windowStart.set(Calendar.SECOND, 0);        // Start of the Window from the beginning of the Minute
        windowStart.set(Calendar.MILLISECOND, 0);

        Calendar windowEnd = Calendar.getInstance();
        windowEnd.set(Calendar.HOUR_OF_DAY, configAutoSettlementHours);
        windowEnd.set(Calendar.MINUTE, configAutoSettlementMinutes);
        windowEnd.set(Calendar.SECOND, 0);
        windowEnd.set(Calendar.MILLISECOND, 0);
        windowEnd.add(Calendar.MILLISECOND, configAutoSettlementWindow);

        return windowStart.compareTo(Calendar.getInstance()) <= 0 && windowEnd.compareTo(Calendar.getInstance()) >= 0;
    }

    private static boolean isNextRetryInWindow() {
        Calendar nextRetry = Calendar.getInstance();
        nextRetry.add(Calendar.MILLISECOND, IN_WINDOW_RETRY_TIMEOUT);

        Calendar windowEnd = Calendar.getInstance();
        windowEnd.set(Calendar.HOUR_OF_DAY, configAutoSettlementHours);
        windowEnd.set(Calendar.MINUTE, configAutoSettlementMinutes);
        windowEnd.add(Calendar.MILLISECOND, configAutoSettlementWindow);

        return windowEnd.compareTo(nextRetry) >= 0;
    }

    private static Calendar getNextAutoSettlementConfigTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, configAutoSettlementHours);
        calendar.set(Calendar.MINUTE, configAutoSettlementMinutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.compareTo(Calendar.getInstance()) < 0) {
            // Time in past, set Date to next day
            calendar.add(Calendar.DATE, 1);
        }
        return calendar;
    }

    // Prepare Calendar from config parameters to schedule timer for the next day
    private static Calendar getNextDayAutoSettlementConfigTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, configAutoSettlementHours);
        calendar.set(Calendar.MINUTE, configAutoSettlementMinutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DATE, 1);
        return calendar;
    }

    protected static void onAutoSettlementIdleTimerFinished(Context context) {
        checkAutoSettlementConditions(context);
    }

    private static synchronized void checkAutoSettlementConditions(Context context) {

        if (!autoSettlementPending) {
            Timber.i("No AutoSettlement pending");
            return;
        }

        // Acquire CPU wakelock. Release it after Autosettlement done or out of autosettlement window
        acquireWakeLock();

        if (!autoSettlementTimerTaskScheduled) {
            // Schedule Autosettlement for next day. Do it only once
            // calculate next AutoSettlement Date/Time according to config hour/minute
            Calendar nextAutosettlement = getNextDayAutoSettlementConfigTime();
            // schedule next Automatic Settlement
            scheduleAutoSettlementAlarmManager(nextAutosettlement);
            autoSettlementTimerTaskScheduled = true;
            Timber.e("AutoSettlement: next day scheduled date/time: %s", nextAutosettlement.getTime().toString());
        }

        // Check if all conditions were met prior to run AutoSettlement
        if (inWindowRetryCountDownTimerTriggered || !autoSettlementIdleTimer.isFinished()) {
            // "In window" timer still running or terminal was not idling enough, wait for timer(s) finish
            Timber.e("AutoSettlement, wait for timer(s): 'retry' %b, 'idle' %b", inWindowRetryCountDownTimerTriggered, !autoSettlementIdleTimer.isFinished());
            return;
        }

        Timber.e("AutoSettlement: Settlement done today: %b, In Window: %b.", wasSettlementPerformedToday(context), isInWindow() );
        autoSettlementPending = false;
        if (!wasSettlementPerformedToday(context) && isInWindow()) {
            Timber.e("Adding AutoSettlement job");
            Engine.getJobs().add(new EFTJob(AUTO_SETTLEMENT));
        }

        if (!wasSettlementPerformedToday(context) && isNextRetryInWindow()) {
            // Start InWindowRetryCountDownTimer for "in window" retries
            if (inWindowRetryCountDownTimer != null) {
                setInWindowRetryCountDownTimerTriggered(true);
                inWindowRetryCountDownTimer.start();
                // Do not release wakelock here: need to be awake next IN_WINDOW_RETRY_TIMEOUT (5mins) to be able to retry autosettlement if this one will fail
                // this method (checkAutoSettlementConditions()) will be called again after inWindowRetryCountDownTimer finish
            }
            else {
                releaseWakeLock();
            }
        }
        else {
            // No more Autosettlement activity until next Autosettlement date
            // Must have set timer task scheduled to next date
            Timber.e("AutoSettlement: no more retry attempts");
            releaseWakeLock();
        }
    }

    public static void init(Context context, PayCfg config) {
        if (initialized)
            return;

        readConfigParameters(config);
        if (!configAutoSettlementEnabled)
            return;

        // Create Idle Timer to track terminal idling
        autoSettlementIdleTimer = new AutoSettlementIdleTimer( configAutoSettlementIdleMS );
        // Create In Window Timer and stop it. Will be started on demand
        inWindowRetryCountDownTimer = new InWindowRetryCountDownTimer();
        inWindowRetryCountDownTimer.cancel();

        prepareWakeLock();

        Calendar scheduledTime;
        Timber.e("AutoSettlement: Settlement done today: %b, In Window: %b.", wasSettlementPerformedToday(context), isInWindow() );
        if (wasSettlementPerformedToday(context)) {
            // Autosettlement was done today. Set AutoSettlement to next day according to config hour/minute
            scheduledTime = getNextDayAutoSettlementConfigTime();
            Timber.e("AutoSettlement: Next scheduled time according to config parameters: %s", scheduledTime.getTime().toString());
        } else {
            if (isInWindow()) {
                // Autosettlement was not done today and we are in window. Set AutoSettlement to now plus couple minutes to complete boot up
                scheduledTime = Calendar.getInstance();
                scheduledTime.add(Calendar.MINUTE, 2);
                Timber.e("AutoSettlement: Settlement not done today and in window. Scheduling: %s", scheduledTime.getTime().toString());
            } else {
                // Autosettlement was not done today, not in Window
                // calculate next AutoSettlement Date/Time according to config hour/minute
                // May be Today or Next Day depending if time from the config is in the past or not
                scheduledTime = getNextAutoSettlementConfigTime();
                Timber.e("AutoSettlement: Next scheduled time according to config parameters: %s", scheduledTime.getTime().toString());
            }
        }

        scheduleAutoSettlementAlarmManager(scheduledTime);
        autoSettlementTimerTaskScheduled = true;
        initialized = true;
    }

    protected static void executeAlarm(Context context) {
        Timber.e("AutoSettlement: time to run");
        // If Autosettlement Watcher was not initialized (yet) ignore alarm
        // New Alarm will be created (updated) when initializing
        if (initialized) {
            Calendar terminalTimeNow = Calendar.getInstance();
            Timber.e("AutoSettlement: Execution Time: %s", terminalTimeNow.getTime().toString());
            autoSettlementPending = true;
            autoSettlementTimerTaskScheduled = false;
            checkAutoSettlementConditions(context);
        }
    }

    private static void scheduleAutoSettlementAlarmManager(Calendar time) {
        Context context = MalFactory.getInstance().getMalContext();
        if (context != null) {
            Intent intent = new Intent(context, AutoSettlementBroadcastReceiver.class);
            // Using FLAG_UPDATE_CURRENT to replace pending intent with new one (if any)
            // There will be only one Alarm at given time
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
        }

    }

    public static void resetIdleState() {
        if (autoSettlementIdleTimer != null) {
            Timber.e("AutoSettlement: resetIdleState");
            autoSettlementIdleTimer.restart();
        }
    }

    public static void enterIdleState() {
        if (autoSettlementIdleTimer != null) {
            Timber.e("AutoSettlement: enterIdleState");
            autoSettlementIdleTimer.restart();
        }
    }

    public static void exitIdleState() {
        if (autoSettlementIdleTimer != null) {
            Timber.e("AutoSettlement: exitIdleState");
            autoSettlementIdleTimer.cancel();
        }
    }

    private static synchronized void setInWindowRetryCountDownTimerTriggered(boolean value) { inWindowRetryCountDownTimerTriggered = value; }

    private static void prepareWakeLock() {
        Context context = MalFactory.getInstance().getMalContext();
        if (context != null) {
            PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AppName:AutoSettlementWatcher");
            wakeLock.setReferenceCounted(false);
        }
    }

    private static void acquireWakeLock() {
        if (wakeLock != null) {
            Timber.e("AutoSettlement: acquireWakeLock");
            wakeLock.acquire();
        }
    }

    private static void releaseWakeLock() {
        if (wakeLock != null) {
            Timber.e("AutoSettlement: releaseWakeLock");
            wakeLock.release();
        }
    }
}
