
package org.axon.sakina;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

public class SakinaNotificationScheduler {

    private static final String PREFS = "sakina_notifications";
    private static final String KEY_ITEMS = "scheduled_items";

    private static PendingIntent createPendingIntent(
            Context context,
            JSONObject item
    ) {
        String id = item.optString("id", "");

        Intent intent = new Intent(
                context,
                SakinaNotificationReceiver.class
        );

        intent.putExtra(
                SakinaNotificationReceiver.EXTRA_ID, id
        );

        intent.putExtra(
                SakinaNotificationReceiver.EXTRA_TITLE,
                item.optString("title", "Sakina Prayer Tracker")
        );

        intent.putExtra(
                SakinaNotificationReceiver.EXTRA_BODY,
                item.optString("body", "It's time for your prayer.")
        );

        intent.putExtra(
                SakinaNotificationReceiver.EXTRA_SOUND,
                item.optBoolean("sound", true)
        );

        intent.putExtra(
                SakinaNotificationReceiver.EXTRA_VIBRATE,
                item.optBoolean("vibrate", true)
        );

        int requestCode = id.hashCode();

        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static void scheduleOne(
            Context context,
            JSONObject item
    ) {
        long triggerAt = item.optLong("triggerAt", 0);

        if (triggerAt <= System.currentTimeMillis()) {
            return;
        }

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent =
                createPendingIntent(context, item);

        try {
            boolean exactAllowed = true;

            if (Build.VERSION.SDK_INT >= 31) {
                exactAllowed = alarmManager.canScheduleExactAlarms();
            }

            if (exactAllowed) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                );
            } else {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                );
            }

        } catch (SecurityException e) {

            // Use an inexact alarm if exact scheduling is blocked.
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
            );
        }
    }

    private static void cancelOne(
            Context context,
            JSONObject item
    ) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        PendingIntent pendingIntent =
                createPendingIntent(context, item);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        pendingIntent.cancel();
    }

    private static JSONArray getSavedItems(Context context) {
        String saved = context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        ).getString(KEY_ITEMS, "[]");

        try {
            return new JSONArray(saved);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private static void saveItems(
            Context context,
            JSONArray items
    ) {
        context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        ).edit()
                .putString(KEY_ITEMS, items.toString())
                .apply();
    }

    public static JSONObject scheduleNotifications(
            Context context,
            String scheduleJson
    ) {
        JSONObject result = new JSONObject();

        try {
            // Remove the previous schedule first.
            cancelAll(context);

            JSONObject schedule = new JSONObject(scheduleJson);
            JSONArray notifications =
                    schedule.optJSONArray("notifications");

            JSONArray savedItems = new JSONArray();
            int scheduled = 0;

            if (notifications != null) {
                for (int i = 0; i < notifications.length(); i++) {

                    JSONObject item = notifications.optJSONObject(i);

                    if (item == null ||
                            !item.optBoolean("enabled", true)) {
                        continue;
                    }

                    long triggerAt =
                            item.optLong("triggerAt", 0);

                    if (triggerAt <= System.currentTimeMillis()) {
                        continue;
                    }

                    scheduleOne(context, item);

                    savedItems.put(item);
                    scheduled++;
                }
            }

            saveItems(context, savedItems);

            result.put("ok", true);
            result.put("scheduled", scheduled);

        } catch (Exception e) {

            try {
                result.put("ok", false);
                result.put("scheduled", 0);
                result.put("error", e.getMessage());
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    public static void cancelNotifications(
            Context context,
            String idsJson
    ) {
        try {
            JSONArray ids = new JSONArray(idsJson);
            JSONArray savedItems = getSavedItems(context);
            JSONArray remaining = new JSONArray();

            for (int i = 0; i < savedItems.length(); i++) {

                JSONObject item = savedItems.optJSONObject(i);

                if (item == null) {
                    continue;
                }

                String itemId = item.optString("id", "");
                boolean shouldCancel = false;

                for (int j = 0; j < ids.length(); j++) {
                    if (itemId.equals(ids.optString(j))) {
                        shouldCancel = true;
                        break;
                    }
                }

                if (shouldCancel) {
                    cancelOne(context, item);
                } else {
                    remaining.put(item);
                }
            }

            saveItems(context, remaining);

        } catch (Exception ignored) {
        }
    }

    public static void cancelAll(Context context) {

        JSONArray savedItems = getSavedItems(context);

        for (int i = 0; i < savedItems.length(); i++) {
            JSONObject item = savedItems.optJSONObject(i);

            if (item != null) {
                cancelOne(context, item);
            }
        }

        saveItems(context, new JSONArray());
    }

    public static int getScheduledCount(Context context) {
        return getSavedItems(context).length();
    }

    public static void restoreSavedSchedule(Context context) {

        JSONArray savedItems = getSavedItems(context);
        JSONArray futureItems = new JSONArray();

        for (int i = 0; i < savedItems.length(); i++) {

            JSONObject item = savedItems.optJSONObject(i);

            if (item == null) {
                continue;
            }

            if (item.optLong("triggerAt", 0) >
                    System.currentTimeMillis()) {

                scheduleOne(context, item);
                futureItems.put(item);
            }
        }

        saveItems(context, futureItems);
    }
}
