package com.example.dailydiary.ui.reminders;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dailydiary.R;
import com.example.dailydiary.domain.model.Reminder;
import com.example.dailydiary.domain.model.ReminderTargetType;
import com.example.dailydiary.domain.model.RepeatRule;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.Task;
import com.example.dailydiary.util.LocalDates;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RemindersFragment extends Fragment {

    private RemindersViewModel viewModel;
    private RemindersAdapter adapter;
    private List<Task> tasks = new ArrayList<>();
    private List<Routine> routines = new ArrayList<>();
    private View empty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RemindersViewModel.class);
        empty = view.findViewById(R.id.text_reminders_empty);
        adapter = new RemindersAdapter(new RemindersAdapter.Listener() {
            @Override public void onEnabledChanged(Reminder reminder, boolean enabled) {
                viewModel.setEnabled(reminder, enabled);
            }
            @Override public void onEdit(RemindersViewModel.ReminderRow row) { showEditor(row.reminder); }
            @Override public void onDelete(Reminder reminder) { confirmDelete(reminder); }
        });
        androidx.recyclerview.widget.RecyclerView list = view.findViewById(R.id.list_reminders);
        list.setAdapter(adapter);
        FloatingActionButton add = view.findViewById(R.id.fab_add_reminder);
        add.setOnClickListener(v -> showEditor(null));
        viewModel.getRows().observe(getViewLifecycleOwner(), rows -> {
            empty.setVisibility(rows == null || rows.isEmpty() ? View.VISIBLE : View.GONE);
            adapter.submit(rows);
        });
        viewModel.getTasks().observe(getViewLifecycleOwner(), values ->
                tasks = values == null ? new ArrayList<>() : values);
        viewModel.getRoutines().observe(getViewLifecycleOwner(), values ->
                routines = values == null ? new ArrayList<>() : values);
    }

    private void showEditor(@Nullable Reminder existing) {
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reminder, null, false);
        Spinner targetType = content.findViewById(R.id.spinner_reminder_target_type);
        Spinner target = content.findViewById(R.id.spinner_reminder_target);
        Spinner repeat = content.findViewById(R.id.spinner_reminder_repeat);
        MaterialButton dateButton = content.findViewById(R.id.button_reminder_date);
        MaterialButton timeButton = content.findViewById(R.id.button_reminder_time);
        SwitchMaterial enabled = content.findViewById(R.id.switch_reminder_dialog_enabled);
        Calendar selection = Calendar.getInstance();
        if (existing != null) selection.setTimeInMillis(existing.getTriggerAt());

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{getString(R.string.reminder_target_task), getString(R.string.reminder_target_routine)});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        targetType.setAdapter(typeAdapter);
        targetType.setSelection(existing != null && existing.getTargetType() == ReminderTargetType.ROUTINE ? 1 : 0);
        ArrayAdapter<String> targetAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        target.setAdapter(targetAdapter);
        final long initialTargetId = existing == null ? -1 : existing.getTargetId();
        final boolean[] firstTargetTypeSelection = {true};
        targetType.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            bindTargets(position, targetAdapter, target,
                    firstTargetTypeSelection[0] ? initialTargetId : -1);
            firstTargetTypeSelection[0] = false;
        }));
        bindTargets(targetType.getSelectedItemPosition(), targetAdapter, target, initialTargetId);

        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new String[]{getString(R.string.repeat_none),
                        getString(R.string.repeat_daily), getString(R.string.repeat_weekdays),
                        getString(R.string.repeat_weekly)});
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeat.setAdapter(repeatAdapter);
        repeat.setSelection(repeatIndex(existing == null ? RepeatRule.NONE : existing.getRepeatRule()));
        enabled.setChecked(existing == null || existing.isEnabled());
        updateDateButton(dateButton, selection);
        updateTimeButton(timeButton, selection);
        dateButton.setOnClickListener(v -> new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            selection.set(Calendar.YEAR, year);
            selection.set(Calendar.MONTH, month);
            selection.set(Calendar.DAY_OF_MONTH, day);
            updateDateButton(dateButton, selection);
        }, selection.get(Calendar.YEAR), selection.get(Calendar.MONTH), selection.get(Calendar.DAY_OF_MONTH)).show());
        timeButton.setOnClickListener(v -> new TimePickerDialog(requireContext(), (picker, hour, minute) -> {
            selection.set(Calendar.HOUR_OF_DAY, hour);
            selection.set(Calendar.MINUTE, minute);
            selection.set(Calendar.SECOND, 0);
            selection.set(Calendar.MILLISECOND, 0);
            updateTimeButton(timeButton, selection);
        }, selection.get(Calendar.HOUR_OF_DAY), selection.get(Calendar.MINUTE), true).show());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? R.string.add_reminder : R.string.edit_reminder)
                .setView(content).setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button -> {
            long targetId = selectedTargetId(targetType.getSelectedItemPosition(), target.getSelectedItemPosition());
            if (targetId <= 0) {
                return;
            }
            ReminderTargetType type = targetType.getSelectedItemPosition() == 1
                    ? ReminderTargetType.ROUTINE : ReminderTargetType.TASK;
            RepeatRule rule = repeatRule(repeat.getSelectedItemPosition());
            if (existing == null) {
                viewModel.addReminder(type, targetId, selection.getTimeInMillis(), rule, enabled.isChecked());
            } else {
                viewModel.updateReminder(existing, type, targetId, selection.getTimeInMillis(), rule, enabled.isChecked());
            }
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void bindTargets(int type, ArrayAdapter<String> labels, Spinner target, long selectedId) {
        labels.clear();
        if (type == 0) {
            for (Task task : tasks) labels.add(task.getTitle());
            selectTarget(target, tasks, selectedId);
        } else {
            for (Routine routine : routines) labels.add(routine.getTitle());
            selectRoutineTarget(target, selectedId);
        }
        labels.notifyDataSetChanged();
    }

    private void selectTarget(Spinner target, List<Task> values, long id) {
        for (int i = 0; i < values.size(); i++) if (values.get(i).getId() == id) { target.setSelection(i); return; }
    }

    private void selectRoutineTarget(Spinner target, long id) {
        for (int i = 0; i < routines.size(); i++) if (routines.get(i).getId() == id) { target.setSelection(i); return; }
    }

    private long selectedTargetId(int type, int position) {
        if (position < 0) return -1;
        if (type == 0) return position < tasks.size() ? tasks.get(position).getId() : -1;
        return position < routines.size() ? routines.get(position).getId() : -1;
    }

    private int repeatIndex(RepeatRule rule) {
        if (rule == RepeatRule.DAILY) return 1;
        if (rule == RepeatRule.WEEKDAYS) return 2;
        if (rule == RepeatRule.WEEKLY) return 3;
        return 0;
    }

    private RepeatRule repeatRule(int index) {
        if (index == 1) return RepeatRule.DAILY;
        if (index == 2) return RepeatRule.WEEKDAYS;
        if (index == 3) return RepeatRule.WEEKLY;
        return RepeatRule.NONE;
    }

    private void updateDateButton(MaterialButton button, Calendar value) {
        button.setText(LocalDates.format(value));
    }

    private void updateTimeButton(MaterialButton button, Calendar value) {
        button.setText(LocalDates.formatTime(value.get(Calendar.HOUR_OF_DAY), value.get(Calendar.MINUTE)));
    }

    private void confirmDelete(Reminder reminder) {
        new AlertDialog.Builder(requireContext()).setMessage(R.string.delete_reminder_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> viewModel.delete(reminder)).show();
    }
}
