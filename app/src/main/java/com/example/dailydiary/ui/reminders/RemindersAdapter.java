package com.example.dailydiary.ui.reminders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailydiary.R;
import com.example.dailydiary.domain.model.Reminder;
import com.example.dailydiary.domain.model.RepeatRule;
import com.example.dailydiary.util.LocalDates;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.ReminderHolder> {

    public interface Listener {
        void onEnabledChanged(Reminder reminder, boolean enabled);
        void onEdit(RemindersViewModel.ReminderRow row);
        void onDelete(Reminder reminder);
    }

    private final Listener listener;
    private final List<RemindersViewModel.ReminderRow> rows = new ArrayList<>();

    public RemindersAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<RemindersViewModel.ReminderRow> values) {
        rows.clear();
        if (values != null) rows.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReminderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderHolder holder, int position) {
        holder.bind(rows.get(position), listener);
    }

    @Override public int getItemCount() { return rows.size(); }

    static class ReminderHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView details;
        private final SwitchMaterial enabled;
        private final View edit;
        private final View delete;

        ReminderHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_reminder_title);
            details = itemView.findViewById(R.id.text_reminder_details);
            enabled = itemView.findViewById(R.id.switch_reminder_enabled);
            edit = itemView.findViewById(R.id.button_edit_reminder);
            delete = itemView.findViewById(R.id.button_delete_reminder);
        }

        void bind(RemindersViewModel.ReminderRow row, Listener listener) {
            Reminder reminder = row.reminder;
            title.setText(row.title.isEmpty() ? itemView.getContext().getString(R.string.reminder_target_missing) : row.title);
            title.setAlpha(reminder.isEnabled() ? 1f : 0.55f);
            details.setText(LocalDates.formatDateTime(reminder.getTriggerAt()) + " · "
                    + repeatLabel(itemView, reminder.getRepeatRule()));
            enabled.setOnCheckedChangeListener(null);
            enabled.setChecked(reminder.isEnabled());
            enabled.setOnCheckedChangeListener((button, checked) -> listener.onEnabledChanged(reminder, checked));
            edit.setOnClickListener(v -> listener.onEdit(row));
            delete.setOnClickListener(v -> listener.onDelete(reminder));
            itemView.setOnClickListener(v -> listener.onEdit(row));
        }

        private String repeatLabel(View view, RepeatRule rule) {
            if (rule == RepeatRule.DAILY) return view.getContext().getString(R.string.repeat_daily);
            if (rule == RepeatRule.WEEKDAYS) return view.getContext().getString(R.string.repeat_weekdays);
            if (rule == RepeatRule.WEEKLY) return view.getContext().getString(R.string.repeat_weekly);
            return view.getContext().getString(R.string.repeat_none);
        }
    }
}
