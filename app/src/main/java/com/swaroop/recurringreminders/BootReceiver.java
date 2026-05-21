package com.swaroop.recurringreminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.swaroop.recurringreminders.models.Reminder;

import java.util.List;
import android.app.AlarmManager;
import android.app.PendingIntent;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // Handle daily reschedule trigger
        if ("com.swaroop.recurringreminders.DAILY_RESCHEDULE".equals(action)) {
            Log.d(TAG, "Daily reschedule triggered");
            rescheduleAll(context);
            scheduleDailyReschedule(context); // schedule the next day's reschedule
            return;
        }

        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        Log.d(TAG, "Boot/update received — rescheduling all reminders");

        rescheduleAll(context);
        scheduleDailyReschedule(context);
    }

    public static void scheduleDailyReschedule(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BootReceiver.class);
        intent.setAction("com.swaroop.recurringreminders.DAILY_RESCHEDULE");
        PendingIntent pi = PendingIntent.getBroadcast(
                context, 9999, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule for 6am every day
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 6);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // If 6am already passed today, schedule for tomorrow
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        Log.d(TAG, "Daily reschedule set for: " + cal.getTime());
    }

    private void rescheduleAll(Context context) {
        ReminderRepository repo = new ReminderRepository(context);
        List<Reminder> reminders = repo.getAll();
        for (Reminder reminder : reminders) {
            NotificationHelper.createChannelForReminder(context, reminder);
            if (reminder.isEnabled()) {
                ReminderScheduler.scheduleNext(context, reminder);
                Log.d(TAG, "Rescheduled: " + reminder.getLabel());
            }
        }
    }
}
