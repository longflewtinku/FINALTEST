package com.linkly.launcher.service;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.linkly.launcher.ServiceFrontEnd;

import java.util.Timer;
import java.util.TimerTask;

public class LauncherUtils {

    static NotificationManager manager;

    public static void POSitiveSvcNotify(Context context, String msg) {
        try {

            Notification myNotication;
            manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            Intent intent = new Intent(context, ServiceFrontEnd.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Channel1");

            builder.setAutoCancel(false);
            builder.setContentTitle("EFT Service");
            builder.setContentText(msg);
            builder.setSmallIcon(android.R.color.transparent);
            builder.setContentIntent(pendingIntent);
            builder.setOngoing(true);
            // builder.setSubText("This is subtext...");   //API level 16
            builder.setNumber(100);
            builder.build();

            myNotication = builder.build();
            manager.notify(11, myNotication);


            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                        manager.cancel(11);
                    }
            }, 5000, 4000);



        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
