package com.swaroop.recurringreminders;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.swaroop.recurringreminders.models.Reminder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReminderRepository {

    private static final String PREFS_NAME = "recurring_reminders";
    private static final String KEY_REMINDERS = "reminders";

    private final SharedPreferences prefs;
    private final Gson gson;

    public ReminderRepository(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<Reminder> getAll() {
        String json = prefs.getString(KEY_REMINDERS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Reminder>>() {}.getType();
        List<Reminder> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public void save(Reminder reminder) {
        List<Reminder> reminders = getAll();
        // Replace if exists, otherwise add
        boolean found = false;
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(reminder.getId())) {
                reminders.set(i, reminder);
                found = true;
                break;
            }
        }
        if (!found) reminders.add(reminder);
        persist(reminders);
    }

    public void delete(String reminderId) {
        List<Reminder> reminders = getAll();
        reminders.removeIf(r -> r.getId().equals(reminderId));
        persist(reminders);
    }

    public Reminder getById(String reminderId) {
        for (Reminder r : getAll()) {
            if (r.getId().equals(reminderId)) return r;
        }
        return null;
    }

    private void persist(List<Reminder> reminders) {
        prefs.edit()
                .putString(KEY_REMINDERS, gson.toJson(reminders))
                .apply();
    }
}
