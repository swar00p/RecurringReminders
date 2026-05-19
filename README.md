# Recurring Reminders (Native Android)

A native Android app built with Java and the Android SDK for setting recurring alarms within a daily time window.

## Requirements

- Java 17
- Android SDK (API 36)
- Gradle 8.6

## Building

### Debug APK (for direct install)
```bash
cd RecurringReminders
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK
```bash
./gradlew assembleRelease
```

## Installing on device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Project structure

```
app/src/main/
  java/com/swaroop/recurringreminders/
    models/
      Reminder.java              # Data model
    MainActivity.java            # Reminder list screen
    AddReminderActivity.java     # Add reminder form
    ReminderAdapter.java         # RecyclerView adapter
    ReminderRepository.java      # SharedPreferences persistence
    ReminderScheduler.java       # AlarmManager scheduling
    NotificationHelper.java      # Notification channel + display
    ReminderReceiver.java        # BroadcastReceiver - alarm fires
    BootReceiver.java            # BroadcastReceiver - reschedule on reboot
  res/
    layout/                      # XML layouts
    values/                      # Colors, strings, themes
    drawable/                    # Icons and shapes
```

## How scheduling works

Instead of pre-scheduling many notifications upfront:

1. When a reminder is saved, `ReminderScheduler.scheduleNext()` calculates
   the next occurrence and sets a single `AlarmManager.setAlarmClock()` alarm
2. When it fires, `ReminderReceiver` shows the notification and immediately
   calls `scheduleNext()` again for the next occurrence
3. This self-perpetuating chain works forever with just one pending alarm
   per reminder at any time
4. `BootReceiver` restarts the chain after a device reboot or app update

`AlarmManager.setAlarmClock()` is the highest priority alarm type —
the same API used by the stock Android Clock app. It fires precisely
even in Doze mode.

## Key advantages over React Native version

- No notification pre-scheduling limit
- No background task needed
- Precise timing via AlarmManager even in Doze mode
- Full system ringtone picker (all notification sounds on the device)
- Survives reboots automatically
- Smaller app size
