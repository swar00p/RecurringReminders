package com.swaroop.recurringreminders;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class StopForTodayManager {

    private static final String PREFS_NAME = "stop_for_today";

    private static String todayTag() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private static String key(String reminderId) {
        return reminderId + "_" + todayTag();
    }

    public static void stopForToday(Context context, String reminderId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(key(reminderId), true).apply();
        cleanStaleKeys(context);
    }

    public static boolean isStoppedForToday(Context context, String reminderId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(key(reminderId), false);
    }

    public static void clearForReminder(Context context, String reminderId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(key(reminderId)).apply();
    }

    // Remove keys from previous days to avoid accumulation
    private static void cleanStaleKeys(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String today = todayTag();
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (!entry.getKey().endsWith(today)) {
                editor.remove(entry.getKey());
            }
        }
        editor.apply();
    }
}
