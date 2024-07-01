package com.linkly.payment.utilities;

import static com.linkly.libpositive.events.PositiveEvent.EventType.PCI24HOUR_REBOOT;

import android.os.CountDownTimer;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.jobs.EFTJob;
import com.linkly.libmal.global.util.Util;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class Pci24HourRebootWatcher {
    private static Pci24HourRebootCountDownTimer countDownTimer = null;
    private static boolean idleState = false;
    private static int idleTimeMillis = 0;
    private static final int WAIT_FOR_IDLE_BEFORE_REBOOT_TIME_MS = 30 * 1000;
    private static final int FORCE_REBOOT_TIME_MS = 3 * 60 * 1000;
    private static final int COUNTDOWN_TIMER_INTERVAL_MS = 1000; // 1 second
    private static boolean initialized = false;

    private Pci24HourRebootWatcher() {
    }

    static class Pci24HourRebootCountDownTimer extends CountDownTimer {

        Pci24HourRebootCountDownTimer() {
            super(FORCE_REBOOT_TIME_MS, COUNTDOWN_TIMER_INTERVAL_MS);
        }

        @Override
        public void onTick(long l) {
            if (idleState) {
                if (idleTimeMillis >= WAIT_FOR_IDLE_BEFORE_REBOOT_TIME_MS) {
                    Timber.i("Idling enough, reboot");
                    Engine.getJobs().add(new EFTJob(PCI24HOUR_REBOOT));
                    this.cancel();
                }
                setIdleTimeMillis(idleTimeMillis + COUNTDOWN_TIMER_INTERVAL_MS);
            }
        }

        @Override
        public void onFinish() {
            Engine.getJobs().add(new EFTJob(PCI24HOUR_REBOOT));
            this.cancel();
        }
    }


    public static void init(PayCfg payCfg) {
        if (initialized)
            return;

        int configRebootTimeHours = 0; //set to midnight if failed to get config value
        int configRebootTimeMinutes = 0;
        // get PCI reboot time from Config
        String configTime = payCfg.getPciRebootTime();

        if (!Util.isNullOrEmpty(configTime)) {
            int i;
            try {
                i = Integer.parseInt(configTime);
            } catch (NumberFormatException e) {
                i = 0;
            }

            if (i / 100 <= 23 || i % 100 <= 59)
            {
                configRebootTimeHours = i / 100;
                configRebootTimeMinutes = i % 100;
            }
        }
        Timber.i("PCI 24-hours Reboot time: %d:%d",configRebootTimeHours,configRebootTimeMinutes);

        Calendar dateTimeNow = Calendar.getInstance();
        Calendar dateTimeReboot = Calendar.getInstance();
        dateTimeReboot.set(Calendar.HOUR_OF_DAY,configRebootTimeHours);
        dateTimeReboot.set(Calendar.MINUTE,configRebootTimeMinutes);

        if (dateTimeReboot.compareTo(dateTimeNow) <= 0) {
            // Set reboot to next day
            dateTimeReboot.add(Calendar.DATE,1);
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Timber.i( "Pci24HourRebootWatcher: Reboot time, waiting for Idle");

                // wait for idle
                if (countDownTimer == null) {
                    // UI thread isn't running, just reboot
                    Timber.i( "Pci24HourRebootWatcher: no CountDown timer, reboot now");
                    Engine.getJobs().add(new EFTJob(PCI24HOUR_REBOOT));
                    return;
                }
                countDownTimer.start();
            }
        }, dateTimeReboot.getTime());
        initialized = true;
    }

    public static void setIdle() {
        createCountDownTimer();
        idleState = true;
    }

    public static void clearIdle() {
        createCountDownTimer();
        idleState = false;
        setIdleTimeMillis(0);
    }

    public static void resetIdle() {
        createCountDownTimer();
        setIdleTimeMillis(0);
    }

    private static void createCountDownTimer() {
        if (countDownTimer == null) {
            countDownTimer = new Pci24HourRebootCountDownTimer();
        }
    }

    private static void setIdleTimeMillis(int count) {
        idleTimeMillis = count;
    }

}
