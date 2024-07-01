package com.linkly.libengine.helpers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.linkly.libmal.MalFactory;

import timber.log.Timber;

/*
Intended for exclusive usage by Internet Availability monitoring and related notices.
Mixing with other usage will result in undesirable effects due to cancellation.

Since the Sound setup for a Notification Channel is bound to that channel for life, to change
 sounds we'll need to delete the Notification Channel and create a new one. So use a channel
 name format that includes the app version at the end to help differentiate moving forward.
 */
public class NotificationInternetHelper {
    private NotificationInternetHelper() {}
    private static final String CHANNEL_ID = "PaymentApp:Internet";
    private static final Integer NOTIFICATION_ID = 2; // We use the same ID here so if multiple things are wrong we dont spam the bar
    private static final String CHANNEL_NAME_OLD = "NotificationInternetHelper";
    private static final String CHANNEL_NAME = "NotificationInternetHelper_1p5p3";
    private static final String CHANNEL_DESCRIPTION = "Internet Availability";
    private static final String GROUP = "NotificationInternetHelper";

    // channelSoundUri is only applied the first time this method is called for the lifetime of the
    //  app install. To change it otherwise, the Channel must be destroyed and a new one created.
    @SuppressLint("MissingPermission")
    public static void displayNotification(String title, int flags, String channelSoundUri) {
        if (MalFactory.getInstance().getMalContext() == null) {
            Timber.e("Error Trying to display Notification, but context is null");
            return;
        }

        // Intent intentionally rendered useless so as to not yield an Activity open upon pressing
        //  the associated Notification.
        Intent bogusFullscreenIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(MalFactory.getInstance().getMalContext(), 0,
                bogusFullscreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Send a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                MalFactory.getInstance().getMalContext(), CHANNEL_ID)
                .setSmallIcon(android.R.color.transparent)
                .setContentTitle(title)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setFullScreenIntent(pendingIntent, true)
                .setGroup(GROUP);

        // This code is required for android versions 26+ otherwise it wont work.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            if (channelSoundUri != null) {
                AudioAttributes att = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                channel.setSound(Uri.parse(channelSoundUri), att);
            }
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = MalFactory.getInstance().getMalContext().getSystemService(NotificationManager.class);
            notificationManager.deleteNotificationChannel(CHANNEL_NAME_OLD);
            notificationManager.createNotificationChannel(channel);
        } else {
            if (channelSoundUri != null) {
                builder.setSound(Uri.parse(channelSoundUri));
            }
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MalFactory.getInstance().getMalContext());

        Notification notif = builder.build();
        notif.flags |= flags;

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, notif);
    }

    // This is a ruthless method in that if some other Error Notification was made since the one
    //  intended to be cancelled, it would have overwritten the one that was meant to be cancelled
    //  and the new one will be cancelled.
    public static void cancelNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MalFactory.getInstance().getMalContext());
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
