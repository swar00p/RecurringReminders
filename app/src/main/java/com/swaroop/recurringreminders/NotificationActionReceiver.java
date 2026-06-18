package com.swaroop.recurringreminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.util.Log;

public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationActionReceiver";
    public static final String ACTION_STOP_FOR_TODAY = 
        "com.swaroop.recurringreminders.STOP_FOR_TODAY";
    public static final String ACTION_DISMISS = 
        "com.swaroop.recurringreminders.DISMISS";
    public static final String EXTRA_REMINDER_ID = "reminder_id";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String reminderId = intent.getStringExtra(EXTRA_REMINDER_ID);
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);

        // Dismiss the notification
        if (notificationId != -1) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            nm.cancel(notificationId);
        }

        if (ACTION_STOP_FOR_TODAY.equals(action) && reminderId != null) {
            Log.d(TAG, "Stopping notifications for today: " + reminderId);
            StopForTodayManager.stopForToday(context, reminderId);
            // Cancel the next scheduled alarm for today
            cancelTodaysAlarms(context, reminderId);
        } else if (ACTION_DISMISS.equals(action)) {
            Log.d(TAG, "Dismissed notification for: " + reminderId);
            // Nothing extra needed — notification already dismissed above
        }
    }

    private void cancelTodaysAlarms(Context context, String reminderId) {
        ReminderRepository repo = new ReminderRepository(context);
        com.swaroop.recurringreminders.models.Reminder reminder = repo.getById(reminderId);
        if (reminder == null) return;

        // Cancel the current scheduled alarm
        ReminderScheduler.cancel(context, reminder);

        // Reschedule starting from tomorrow
        ReminderScheduler.scheduleNextFromTomorrow(context, reminder);
    }
}
