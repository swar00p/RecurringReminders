package com.swaroop.recurringreminders.models;

import java.util.List;
import java.util.UUID;

public class Reminder {

    private String id;
    private String label;
    private String emoji;
    private int intervalMinutes;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private List<Integer> activeDays; // 0=Sun, 1=Mon, ..., 6=Sat
    private String soundUri;          // URI string of chosen ringtone
    private String soundName;         // Human-readable name
    private boolean enabled;
    private long createdAt;

    public Reminder() {
        this.id = UUID.randomUUID().toString();
        this.enabled = true;
        this.createdAt = System.currentTimeMillis();
    }

    // ─── Getters ────────────────────────────────────────────────────────────

    public String getId() { return id; }
    public String getLabel() { return label; }
    public String getEmoji() { return emoji; }
    public int getIntervalMinutes() { return intervalMinutes; }
    public int getStartHour() { return startHour; }
    public int getStartMinute() { return startMinute; }
    public int getEndHour() { return endHour; }
    public int getEndMinute() { return endMinute; }
    public List<Integer> getActiveDays() { return activeDays; }
    public String getSoundUri() { return soundUri; }
    public String getSoundName() { return soundName; }
    public boolean isEnabled() { return enabled; }
    public long getCreatedAt() { return createdAt; }

    // ─── Setters ────────────────────────────────────────────────────────────

    public void setId(String id) { this.id = id; }
    public void setLabel(String label) { this.label = label; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public void setIntervalMinutes(int intervalMinutes) { this.intervalMinutes = intervalMinutes; }
    public void setStartHour(int startHour) { this.startHour = startHour; }
    public void setStartMinute(int startMinute) { this.startMinute = startMinute; }
    public void setEndHour(int endHour) { this.endHour = endHour; }
    public void setEndMinute(int endMinute) { this.endMinute = endMinute; }
    public void setActiveDays(List<Integer> activeDays) { this.activeDays = activeDays; }
    public void setSoundUri(String soundUri) { this.soundUri = soundUri; }
    public void setSoundName(String soundName) { this.soundName = soundName; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    public String getFormattedInterval() {
        if (intervalMinutes < 60) return intervalMinutes + " min";
        int hours = intervalMinutes / 60;
        int mins = intervalMinutes % 60;
        return mins == 0 ? hours + "h" : hours + "h " + mins + "m";
    }

    public String getFormattedStartTime() {
        return formatTime(startHour, startMinute);
    }

    public String getFormattedEndTime() {
        return formatTime(endHour, endMinute);
    }

    public String getFormattedDays() {
        if (activeDays == null || activeDays.size() == 7) return "Every day";
        if (activeDays.isEmpty()) return "No days";
        String[] names = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < activeDays.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(names[activeDays.get(i)]);
        }
        return sb.toString();
    }

    public int getDailyOccurrences() {
        int totalMins = (endHour * 60 + endMinute) - (startHour * 60 + startMinute);
        if (totalMins < 0) return 0;
        return totalMins / intervalMinutes + 1;
    }

    private static String formatTime(int hour, int minute) {
        int h = hour % 12 == 0 ? 12 : hour % 12;
        String ampm = hour < 12 ? "AM" : "PM";
        return String.format("%d:%02d %s", h, minute, ampm);
    }
}
