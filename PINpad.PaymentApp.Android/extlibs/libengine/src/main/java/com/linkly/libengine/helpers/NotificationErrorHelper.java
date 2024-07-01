package com.linkly.libengine.helpers;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.linkly.libmal.MalFactory;

import timber.log.Timber;

public class NotificationErrorHelper {

    private static final String CHANNEL_ID = "PaymentAppNotification";
    private static final Integer NOTIFICATION_ID = 1; // We use the same ID here so if multiple things are wrong we dont spam the bar
    private static final CharSequence CHANNEL_NAME = "NotificationErrorHelper";
    private static final String CHANNEL_DESCRIPTION = "Notification Errors";
    private static final String GROUP = "NotificationErrorHelper";

    @SuppressLint("MissingPermission")
    public static void displayNotification(String title, String message) {
        if(MalFactory.getInstance().getMalContext() == null) {
            Timber.e("Error Trying to display Notification, but context is null");
            return;
        }

        // Send a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MalFactory.getInstance().getMalContext(), CHANNEL_ID)
                .setSmallIcon(android.R.color.transparent)
                .setContentTitle(title)
                .setContentText(message)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(GROUP);

        // This code is required for android versions 26+ otherwise it wont work.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = MalFactory.getInstance().getMalContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MalFactory.getInstance().getMalContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
