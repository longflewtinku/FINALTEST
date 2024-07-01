package com.linkly.libpositivesvc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.linkly.libconfig.DownloadCfg;
import com.linkly.libconfig.MalConfig;
import com.linkly.libmal.global.util.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class POSitiveSvcCheck {

    private static final String TAG = "POSitiveSvcCheck";

    public static boolean EFTCheckAutoRebootSchedule(Context context, String time) {
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance

        String timeCfg = "03:00";
        if (!Util.isNullOrWhitespace(time)) {
            timeCfg = time;
        }

        /*Check Auto Rec MalConfig*/
        boolean isAutoRecTime = false;       //Is it time to do an Auto rec
        Date date = new Date();   // given date
        calendar.setTime(date);   // assigns calendar to given date
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        int min = calendar.get(Calendar.MINUTE);        // gets hour in 12h format


        int cfgHr = Integer.parseInt(timeCfg.substring(0, timeCfg.indexOf(':')));
        int cfgMin = Integer.parseInt(timeCfg.substring(timeCfg.indexOf(':') + 1));

        if (hour >= cfgHr  && hour <= cfgHr + 1) {
            //If its the same hour check the min, else don't
            if (cfgHr == hour && cfgMin <= min) {
                isAutoRecTime = true;
            } else if (cfgHr != hour) {
                isAutoRecTime = true;
            }
        }

        /*Check if the AutoRec Should be Requested */
        long elapsedTime = SystemClock.elapsedRealtime();
        if (isAutoRecTime && ( elapsedTime > (cfgHr * 3600000))) { /* check we have been up for as long as the minimum time today*/
            return true;
        }

        if (elapsedTime > (24 * 3600000))
            return true;

        return false;
    }



    public static boolean EFTCheckAppUpdateSchedule(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("com.linkly.service.cfg", Context.MODE_PRIVATE);
        long lastUpdate = sharedPref.getLong("lastUpdateCheck", 0);
        DownloadCfg cfg = MalConfig.getInstance().getDownloadCfg();
        boolean isDownloadDay = false;
        boolean isDownloadTime = false;
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int now = (hour * 60) + min;

        if (cfg.getHeartbeat() == null || Util.isNullOrWhitespace(cfg.getHeartbeat().getDay()))
            return false;

        if ("*".equalsIgnoreCase(cfg.getHeartbeat().getDay()) || "0-7".equalsIgnoreCase(cfg.getHeartbeat().getDay())) {
            isDownloadDay = true;
        } else {
            /*Check the Day of Week - Convert from CRON 1= Monday to Java 2= Monday (1= Sunday)*/
            int cfgDay = 0;
            if (cfg.getHeartbeat().getDay() != null && cfg.getHeartbeat().getDay().length() > 0) {
                cfgDay = Integer.parseInt(cfg.getHeartbeat().getDay());
            }
            cfgDay += 1;
            if (cfgDay == 8)   //Check for Sunday, Wrap it back
            {
                cfgDay = 1;
            }

            if (cfgDay == day) {
                isDownloadDay = true;
            }
        }


        if (!isDownloadDay) {
            return false;
        }

        /*Check We are With the Download Request Window */
        String timeCfg = cfg.getHeartbeat().getStartTime();
        int strHr = Integer.parseInt(timeCfg.substring(0, timeCfg.indexOf(':')));
        int strMin = Integer.parseInt(timeCfg.substring(timeCfg.indexOf(':') + 1));
        timeCfg = cfg.getHeartbeat().getEndTime();
        int endHr = Integer.parseInt(timeCfg.substring(0, timeCfg.indexOf(':')));
        int endMin = Integer.parseInt(timeCfg.substring(timeCfg.indexOf(':') + 1));

        int window;

        /*Calculate Window in Minutes*/
        if (endHr < strHr) {
            //This Wraps Midnight
            window = ((24 - strHr) + strHr) * 60;
        } else {
            window = (endHr - strHr) * 60;
        }
        window -= strMin;
        window += endMin;


        /*Check if we are in the download Window.*/
        if (now >= ((strHr * 60) + strMin) && now < ((strHr * 60) + strMin) + window) {
            isDownloadTime = true;
        }

        /*Check if the AutoRec Should be Requested*/
        if (isDownloadTime && (lastUpdate != (long) calendar.get(Calendar.DAY_OF_YEAR))) {
            return true;
        }
        return false;
    }

    public static boolean EFTSetAppUpdateSchedule(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("com.linkly.service.cfg", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String logtxt = sdf.format(date);
        editor.putString("lastUpdateCheckLog", logtxt);

        /*Update the Time of the Event*/
        editor.putLong("lastUpdateCheck", calendar.get(Calendar.DAY_OF_YEAR));
        editor.commit();
        return true;
    }


    public static boolean EFTSetLastRecDoneToday(Context context) {
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        SharedPreferences sharedPref = context.getSharedPreferences("com.linkly.service.cfg", Context.MODE_PRIVATE);
        Date date = new Date();   // given date
        calendar.setTime(date);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("lastRec", calendar.get(Calendar.DAY_OF_YEAR));
        editor.apply();
        return false;
    }
}
