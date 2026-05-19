package com.swaroop.recurringreminders;

import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.swaroop.recurringreminders.databinding.ActivityAddReminderBinding;
import com.swaroop.recurringreminders.models.Reminder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddReminderActivity extends AppCompatActivity {

    private static final int REQUEST_RINGTONE = 1001;

    private ActivityAddReminderBinding binding;
    private ReminderRepository repo;

    private String selectedSoundUri = null;
    private String selectedSoundName = null;

    private static final String[] EMOJIS = {
            "💧", "🚶", "🧘", "📖", "💊", "🍎", "☕", "🌿", "🔔", "💪", "🎯", "🧠"
    };

    private static final int[] INTERVAL_VALUES = {15, 30, 60, 90, 120, 180, 240};
    private static final String[] INTERVAL_LABELS = {"15 min", "30 min", "1 hour", "90 min", "2 hours", "3 hours", "4 hours"};

    private static final String[] DAY_LABELS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    // Time presets: 6:00 AM to 10:00 PM in 30-min increments
    private static final int[] TIME_HOURS;
    private static final int[] TIME_MINUTES;
    private static final String[] TIME_LABELS;

    static {
        List<Integer> hours = new ArrayList<>();
        List<Integer> mins = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int h = 6; h <= 22; h++) {
            for (int m : new int[]{0, 30}) {
                hours.add(h);
                mins.add(m);
                int h12 = h % 12 == 0 ? 12 : h % 12;
                String ampm = h < 12 ? "AM" : "PM";
                labels.add(String.format("%d:%02d %s", h12, m, ampm));
            }
        }
        TIME_HOURS = hours.stream().mapToInt(i -> i).toArray();
        TIME_MINUTES = mins.stream().mapToInt(i -> i).toArray();
        TIME_LABELS = labels.toArray(new String[0]);
    }

    private int selectedEmojiIndex = 8; // 🔔
    private int selectedIntervalIndex = 2; // 1 hour
    private int selectedStartIndex = 6;  // 9:00 AM
    private int selectedEndIndex = 22;   // 5:00 PM
    private final boolean[] selectedDays = {true, true, true, true, true, true, true};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddReminderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repo = new ReminderRepository(this);

        setupToolbar();
        setupEmojiChips();
        setupIntervalChips();
        setupDayChips();
        setupTimeSpinners();
        setupSoundPicker();
        setupSaveButton();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupEmojiChips() {
        for (int i = 0; i < EMOJIS.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(EMOJIS[i]);
            chip.setCheckable(true);
            chip.setChecked(i == selectedEmojiIndex);
            chip.setTextSize(20);
            final int index = i;
            chip.setOnClickListener(v -> {
                selectedEmojiIndex = index;
                updateChipGroup(binding.chipGroupEmoji, index);
            });
            binding.chipGroupEmoji.addView(chip);
        }
    }

    private void setupIntervalChips() {
        for (int i = 0; i < INTERVAL_LABELS.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(INTERVAL_LABELS[i]);
            chip.setCheckable(true);
            chip.setChecked(i == selectedIntervalIndex);
            final int index = i;
            chip.setOnClickListener(v -> {
                selectedIntervalIndex = index;
                updateChipGroup(binding.chipGroupInterval, index);
                updatePreview();
            });
            binding.chipGroupInterval.addView(chip);
        }
    }

    private void setupDayChips() {
        for (int i = 0; i < DAY_LABELS.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(DAY_LABELS[i]);
            chip.setCheckable(true);
            chip.setChecked(selectedDays[i]);
            final int index = i;
            chip.setOnClickListener(v -> {
                selectedDays[index] = !selectedDays[index];
                chip.setChecked(selectedDays[index]);
                updatePreview();
            });
            binding.chipGroupDays.addView(chip);
        }
    }

    private void setupTimeSpinners() {
        // Start time
        binding.spinnerStart.setMinValue(0);
        binding.spinnerStart.setMaxValue(TIME_LABELS.length - 1);
        binding.spinnerStart.setDisplayedValues(TIME_LABELS);
        binding.spinnerStart.setValue(selectedStartIndex);
        binding.spinnerStart.setWrapSelectorWheel(false);
        binding.spinnerStart.setOnValueChangedListener((picker, oldVal, newVal) -> {
            selectedStartIndex = newVal;
            // Ensure end >= start
            if (selectedEndIndex <= selectedStartIndex) {
                selectedEndIndex = selectedStartIndex + 1;
                binding.spinnerEnd.setValue(selectedEndIndex);
            }
            updatePreview();
        });

        // End time
        binding.spinnerEnd.setMinValue(0);
        binding.spinnerEnd.setMaxValue(TIME_LABELS.length - 1);
        binding.spinnerEnd.setDisplayedValues(TIME_LABELS);
        binding.spinnerEnd.setValue(selectedEndIndex);
        binding.spinnerEnd.setWrapSelectorWheel(false);
        binding.spinnerEnd.setOnValueChangedListener((picker, oldVal, newVal) -> {
            selectedEndIndex = newVal;
            updatePreview();
        });
    }

    private void setupSoundPicker() {
        // Show system default initially
        binding.tvSoundName.setText("Default notification sound");

        binding.btnPickSound.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select notification sound");
            if (selectedSoundUri != null) {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        Uri.parse(selectedSoundUri));
            }
            startActivityForResult(intent, REQUEST_RINGTONE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RINGTONE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                selectedSoundUri = uri.toString();
                // Get human-readable name
                android.media.Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                selectedSoundName = ringtone.getTitle(this);
                binding.tvSoundName.setText(selectedSoundName);
            }
        }
    }

    private void setupSaveButton() {
        updatePreview();
        binding.btnSave.setOnClickListener(v -> saveReminder());
    }

    private void saveReminder() {
        String label = binding.etLabel.getText().toString().trim();
        if (label.isEmpty()) {
            binding.etLabel.setError("Please enter a name");
            binding.etLabel.requestFocus();
            return;
        }

        if (selectedEndIndex <= selectedStartIndex) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> activeDays = new ArrayList<>();
        for (int i = 0; i < selectedDays.length; i++) {
            if (selectedDays[i]) activeDays.add(i);
        }
        if (activeDays.isEmpty()) {
            Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show();
            return;
        }

        Reminder reminder = new Reminder();
        reminder.setLabel(label);
        reminder.setEmoji(EMOJIS[selectedEmojiIndex]);
        reminder.setIntervalMinutes(INTERVAL_VALUES[selectedIntervalIndex]);
        reminder.setStartHour(TIME_HOURS[selectedStartIndex]);
        reminder.setStartMinute(TIME_MINUTES[selectedStartIndex]);
        reminder.setEndHour(TIME_HOURS[selectedEndIndex]);
        reminder.setEndMinute(TIME_MINUTES[selectedEndIndex]);
        reminder.setActiveDays(activeDays);
        reminder.setSoundUri(selectedSoundUri);
        reminder.setSoundName(selectedSoundName);
        reminder.setEnabled(true);

        repo.save(reminder);
        ReminderScheduler.scheduleNext(this, reminder);

        Toast.makeText(this, "Reminder saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updatePreview() {
        if (binding == null) return;
        int occurrences = 0;
        if (selectedEndIndex > selectedStartIndex) {
            int startMins = TIME_HOURS[selectedStartIndex] * 60 + TIME_MINUTES[selectedStartIndex];
            int endMins = TIME_HOURS[selectedEndIndex] * 60 + TIME_MINUTES[selectedEndIndex];
            occurrences = (endMins - startMins) / INTERVAL_VALUES[selectedIntervalIndex] + 1;
        }
        String preview = occurrences + " notification" + (occurrences != 1 ? "s" : "")
                + " per day · " + TIME_LABELS[selectedStartIndex]
                + " – " + TIME_LABELS[selectedEndIndex];
        binding.tvPreview.setText(preview);
    }

    private void updateChipGroup(com.google.android.material.chip.ChipGroup group, int selectedIndex) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(i == selectedIndex);
            }
        }
    }
}
