package com.swaroop.recurringreminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.swaroop.recurringreminders.models.Reminder;

import java.util.Calendar;
import java.util.List;

public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";

    // ─── Schedule next occurrence ─────────────────────────────────────────────
    //
    // Calculates the next occurrence time for this reminder from now,
    // respecting active days and the time window, then sets an exact alarm.
    // When the alarm fires, ReminderReceiver shows the notification and
    // immediately calls scheduleNext() again — creating a self-perpetuating chain.

    public static void scheduleNext(Context context, Reminder reminder) {
        if (!reminder.isEnabled()) return;

        Calendar next = getNextOccurrence(reminder);
        if (next == null) {
            Log.w(TAG, "No next occurrence found for: " + reminder.getLabel());
            return;
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = buildPendingIntent(context, reminder);

        // setAlarmClock is the highest priority alarm type — same API used by
        // the stock Clock app. Fires precisely even in Doze mode.
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                next.getTimeInMillis(),
                pi
        );
        am.setAlarmClock(alarmClockInfo, pi);

        Log.d(TAG, "Scheduled next alarm for \"" + reminder.getLabel()
                + "\" at " + next.getTime());
    }

    public static void cancel(Context context, Reminder reminder) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = buildPendingIntent(context, reminder);
        am.cancel(pi);
        Log.d(TAG, "Cancelled alarm for: " + reminder.getLabel());
    }

    // ─── Next occurrence calculation ──────────────────────────────────────────

    public static Calendar getNextOccurrence(Reminder reminder) {
        List<Integer> activeDays = reminder.getActiveDays();
        Calendar now = Calendar.getInstance();

        // Try each minute slot starting from now, up to 7 days ahead
        // to find the next valid occurrence
        Calendar candidate = (Calendar) now.clone();
        candidate.set(Calendar.SECOND, 0);
        candidate.set(Calendar.MILLISECOND, 0);
        // Advance to next minute
        candidate.add(Calendar.MINUTE, 1);

        for (int day = 0; day < 7; day++) {
            Calendar dayCandidate = (Calendar) candidate.clone();
            dayCandidate.add(Calendar.DAY_OF_YEAR, day);

            int dayOfWeek = dayCandidate.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sun
            if (activeDays != null && !activeDays.isEmpty()
                    && !activeDays.contains(dayOfWeek)) {
                // Not an active day — jump to start of next day
                candidate.add(Calendar.DAY_OF_YEAR, 1);
                candidate.set(Calendar.HOUR_OF_DAY, reminder.getStartHour());
                candidate.set(Calendar.MINUTE, reminder.getStartMinute());
                continue;
            }

            // Find the next occurrence slot within this day's window
            Calendar slot = buildSlotForDay(reminder, dayCandidate);
            if (slot != null) return slot;

            // No slot found today — move to start of next day
            candidate.add(Calendar.DAY_OF_YEAR, 1);
            candidate.set(Calendar.HOUR_OF_DAY, reminder.getStartHour());
            candidate.set(Calendar.MINUTE, reminder.getStartMinute());
        }

        return null;
    }

    private static Calendar buildSlotForDay(Reminder reminder, Calendar base) {
        Calendar now = Calendar.getInstance();

        // Start of window for this day
        Calendar windowStart = (Calendar) base.clone();
        windowStart.set(Calendar.HOUR_OF_DAY, reminder.getStartHour());
        windowStart.set(Calendar.MINUTE, reminder.getStartMinute());
        windowStart.set(Calendar.SECOND, 0);
        windowStart.set(Calendar.MILLISECOND, 0);

        // End of window for this day
        Calendar windowEnd = (Calendar) base.clone();
        windowEnd.set(Calendar.HOUR_OF_DAY, reminder.getEndHour());
        windowEnd.set(Calendar.MINUTE, reminder.getEndMinute());
        windowEnd.set(Calendar.SECOND, 0);
        windowEnd.set(Calendar.MILLISECOND, 0);

        // Iterate through slots in the window
        Calendar slot = (Calendar) windowStart.clone();
        while (!slot.after(windowEnd)) {
            if (slot.after(now)) {
                return slot; // First future slot in this window
            }
            slot.add(Calendar.MINUTE, reminder.getIntervalMinutes());
        }

        return null; // All slots today are in the past
    }

    // ─── PendingIntent ────────────────────────────────────────────────────────

    private static PendingIntent buildPendingIntent(Context context, Reminder reminder) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction("com.swaroop.recurringreminders.ALARM");
        intent.putExtra("reminder_id", reminder.getId());

        // Use a stable request code derived from the reminder ID
        int requestCode = reminder.getId().hashCode();

        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
