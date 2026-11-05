
package org.axon.sakina;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

public class SakinaNotificationReceiver extends BroadcastReceiver {

    public static final String EXTRA_ID = "notification_id";
    public static final String EXTRA_TITLE = "notification_title";
    public static final String EXTRA_BODY = "notification_body";
    public static final String EXTRA_SOUND = "notification_sound";
    public static final String EXTRA_VIBRATE = "notification_vibrate";

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = intent.getStringExtra(EXTRA_TITLE);
        String body = intent.getStringExtra(EXTRA_BODY);
        String id = intent.getStringExtra(EXTRA_ID);

        boolean sound = intent.getBooleanExtra(EXTRA_SOUND, true);
        boolean vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true);

        if (title == null) {
            title = "Sakina Prayer Tracker";
        }

        if (body == null) {
            body = "It's time for your prayer.";
        }

        // Android 13+ requires notification permission.
        if (Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(
                        android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                );

        if (manager == null) {
            return;
        }

        String channelId = "sakina_" +
                (sound ? "sound" : "silent") + "_" +
                (vibrate ? "vibrate" : "novibrate");

        // Create notification channels on Android 8+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Prayer reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Prayer time reminders from Sakina");

            if (!sound) {
                channel.setSound(null, null);
            }

            if (!vibrate) {
                channel.enableVibration(false);
            } else {
                channel.enableVibration(true);
                channel.setVibrationPattern(
                        new long[]{0, 400, 200, 400}
                );
            }

            manager.createNotificationChannel(channel);
        }

        // Open the app when the notification is tapped.
        Intent openApp = new Intent(context, MainActivity.class);
        openApp.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                1001,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        (Build.VERSION.SDK_INT >= 23
                                ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, channelId);
        } else {
            builder = new Notification.Builder(context);

            int defaults = 0;

            if (sound) {
                defaults |= Notification.DEFAULT_SOUND;
            }

            if (vibrate) {
                defaults |= Notification.DEFAULT_VIBRATE;
            }

            builder.setDefaults(defaults);
        }

        builder.setSmallIcon(context.getApplicationInfo().icon)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        int notificationId = id != null
                ? id.hashCode()
                : (int) System.currentTimeMillis();

        manager.notify(notificationId, builder.build());
    }
}
