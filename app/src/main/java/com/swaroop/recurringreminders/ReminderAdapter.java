package com.swaroop.recurringreminders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swaroop.recurringreminders.models.Reminder;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    public interface Listener {
        void onToggle(Reminder reminder, boolean enabled);
        void onDelete(Reminder reminder);
    }

    private final List<Reminder> reminders;
    private final Listener listener;

    public ReminderAdapter(List<Reminder> reminders, Listener listener) {
        this.reminders = reminders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.bind(reminder, listener);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEmoji;
        private final TextView tvLabel;
        private final TextView tvInterval;
        private final TextView tvTime;
        private final TextView tvDays;
        private final TextView tvSound;
        private final Switch swEnabled;
        private final TextView btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tv_emoji);
            tvLabel = itemView.findViewById(R.id.tv_label);
            tvInterval = itemView.findViewById(R.id.tv_interval);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvDays = itemView.findViewById(R.id.tv_days);
            tvSound = itemView.findViewById(R.id.tv_sound);
            swEnabled = itemView.findViewById(R.id.sw_enabled);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(Reminder reminder, Listener listener) {
            tvEmoji.setText(reminder.getEmoji());
            tvLabel.setText(reminder.getLabel());
            tvInterval.setText("Every " + reminder.getFormattedInterval());
            tvTime.setText(reminder.getFormattedStartTime()
                    + " – " + reminder.getFormattedEndTime()
                    + "  ·  " + reminder.getDailyOccurrences() + "× daily");
            tvDays.setText(reminder.getFormattedDays());

            if (reminder.getSoundName() != null && !reminder.getSoundName().isEmpty()) {
                tvSound.setVisibility(View.VISIBLE);
                tvSound.setText("🔊 " + reminder.getSoundName());
            } else {
                tvSound.setVisibility(View.GONE);
            }

            // Set switch without triggering listener
            swEnabled.setOnCheckedChangeListener(null);
            swEnabled.setChecked(reminder.isEnabled());
            swEnabled.setOnCheckedChangeListener((btn, checked) ->
                    listener.onToggle(reminder, checked));

            itemView.setAlpha(reminder.isEnabled() ? 1f : 0.5f);

            btnDelete.setOnClickListener(v -> listener.onDelete(reminder));
        }
    }
}
