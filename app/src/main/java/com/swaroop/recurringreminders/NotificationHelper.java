package com.swaroop.recurringreminders;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.media.RingtoneManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.content.Intent;
import android.app.PendingIntent;

import androidx.core.app.NotificationCompat;

import com.swaroop.recurringreminders.models.Reminder;

public class NotificationHelper {

    private static final String CHANNEL_ID = "recurring_reminders";
    private static final String CHANNEL_NAME = "Recurring Reminders";


    public static void createChannelForReminder(Context context, Reminder reminder) {
        String channelId = "reminder_" + reminder.getId();
        String channelName = reminder.getLabel();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Reminders for " + reminder.getLabel());
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 250, 250, 250});
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        if (reminder.getSoundUri() != null && !reminder.getSoundUri().isEmpty()) {
            channel.setSound(Uri.parse(reminder.getSoundUri()), audioAttributes);
        } else {
            channel.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    audioAttributes
            );
        }

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        nm.createNotificationChannel(channel);
    }

    public static void showNotification(Context context, Reminder reminder) {
        String channelId = "reminder_" + reminder.getId();

        // Ensure channel exists
        createChannelForReminder(context, reminder);

        String title = reminder.getEmoji() + "  " + reminder.getLabel();
        String body = "Every " + reminder.getFormattedInterval()
                + " · " + reminder.getFormattedStartTime()
                + "–" + reminder.getFormattedEndTime()
                + " · " + reminder.getFormattedDays();

        // Create intent to open MainActivity when notification is tapped
        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent launchPendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(launchPendingIntent);

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        int notificationId = Math.abs(reminder.getId().hashCode());
        nm.notify(notificationId, builder.build());}
    }
