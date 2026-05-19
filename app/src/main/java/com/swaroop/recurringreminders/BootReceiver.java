package com.swaroop.recurringreminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.swaroop.recurringreminders.models.Reminder;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        Log.d(TAG, "Boot/update received — rescheduling all reminders");

        ReminderRepository repo = new ReminderRepository(context);
        List<Reminder> reminders = repo.getAll();

        for (Reminder reminder : reminders) {
            NotificationHelper.createChannelForReminder(context, reminder);
            if (reminder.isEnabled()) {
                ReminderScheduler.scheduleNext(context, reminder);
            }
        }
    }
}
