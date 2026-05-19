package com.swaroop.recurringreminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.swaroop.recurringreminders.models.Reminder;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String reminderId = intent.getStringExtra("reminder_id");
        if (reminderId == null) {
            Log.w(TAG, "Received alarm with no reminder_id");
            return;
        }

        ReminderRepository repo = new ReminderRepository(context);
        Reminder reminder = repo.getById(reminderId);

        if (reminder == null) {
            Log.w(TAG, "Reminder not found for id: " + reminderId);
            return;
        }

        if (!reminder.isEnabled()) {
            Log.d(TAG, "Reminder is disabled, skipping: " + reminder.getLabel());
            return;
        }

        // Show the notification
        NotificationHelper.showNotification(context, reminder);
        Log.d(TAG, "Showed notification for: " + reminder.getLabel());

        // Immediately schedule the next occurrence — self-perpetuating chain
        ReminderScheduler.scheduleNext(context, reminder);
    }
}
