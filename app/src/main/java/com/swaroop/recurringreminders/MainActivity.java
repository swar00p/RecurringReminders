package com.swaroop.recurringreminders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.swaroop.recurringreminders.databinding.ActivityMainBinding;
import com.swaroop.recurringreminders.models.Reminder;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ReminderAdapter.Listener {

    private ActivityMainBinding binding;
    private ReminderRepository repo;
    private ReminderAdapter adapter;
    private List<Reminder> reminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repo = new ReminderRepository(this);

        // RecyclerView setup
        reminders = repo.getAll();
        adapter = new ReminderAdapter(reminders, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        updateEmptyState();

        // FAB → open AddReminderActivity
        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddReminderActivity.class);
            startActivity(intent);
        });

        requestNotificationPermission();
        BootReceiver.scheduleDailyReschedule(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from AddReminderActivity
        reminders.clear();
        reminders.addAll(repo.getAll());
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    @Override
    public void onToggle(Reminder reminder, boolean enabled) {
        reminder.setEnabled(enabled);
        repo.save(reminder);

        if (enabled) {
            ReminderScheduler.scheduleNext(this, reminder);
        } else {
            ReminderScheduler.cancel(this, reminder);
        }

        int index = reminders.indexOf(reminder);
        adapter.notifyItemChanged(index);
    }

    @Override
    public void onDelete(Reminder reminder) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Reminder")
                .setMessage("Remove \"" + reminder.getLabel() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ReminderScheduler.cancel(this, reminder);
                    repo.delete(reminder.getId());
                    int index = reminders.indexOf(reminder);
                    reminders.remove(index);
                    adapter.notifyItemRemoved(index);
                    updateEmptyState();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEmptyState() {
        if (reminders.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }
    }
}
