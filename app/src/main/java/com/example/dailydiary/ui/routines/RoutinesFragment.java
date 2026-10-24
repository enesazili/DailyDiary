package com.example.dailydiary.ui.routines;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dailydiary.R;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.ScheduleType;
import com.example.dailydiary.util.DaysOfWeek;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RoutinesFragment extends Fragment {

    private RoutinesViewModel viewModel;
    private RoutinesAdapter adapter;
    private List<Routine> routines = new ArrayList<>();
    private Set<Long> completedIds = new LinkedHashSet<>();
    private View empty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_routines, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RoutinesViewModel.class);
        empty = view.findViewById(R.id.text_routines_empty);
        adapter = new RoutinesAdapter(new RoutinesAdapter.Listener() {
            @Override public void onCompletionChanged(Routine routine, boolean completed) {
                viewModel.toggleToday(routine);
            }

            @Override public void onActiveChanged(Routine routine, boolean active) {
                viewModel.setActive(routine, active);
            }

            @Override public void onEdit(Routine routine) {
                showRoutineDialog(routine);
            }

            @Override public void onDelete(Routine routine) {
                confirmDelete(routine);
            }
        });
        androidx.recyclerview.widget.RecyclerView list = view.findViewById(R.id.list_routines);
        list.setAdapter(adapter);
        FloatingActionButton add = view.findViewById(R.id.fab_add_routine);
        add.setOnClickListener(v -> showRoutineDialog(null));

        viewModel.getRoutines().observe(getViewLifecycleOwner(), values -> {
            routines = values == null ? new ArrayList<>() : values;
            render();
        });
        viewModel.getCompletedIds().observe(getViewLifecycleOwner(), ids -> {
            completedIds = ids == null ? new LinkedHashSet<>() : ids;
            render();
        });
    }

    private void render() {
        if (adapter == null) return;
        empty.setVisibility(routines.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.submit(viewModel.getToday(), routines, completedIds);
    }

    private void showRoutineDialog(@Nullable Routine existing) {
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_routine, null, false);
        TextInputEditText title = content.findViewById(R.id.input_routine_title);
        TextInputEditText description = content.findViewById(R.id.input_routine_description);
        RadioGroup schedule = content.findViewById(R.id.group_routine_schedule);
        LinearLayout customDays = content.findViewById(R.id.layout_custom_days);
        SwitchMaterial active = content.findViewById(R.id.switch_routine_dialog_active);
        MaterialCheckBox[] dayChecks = dayChecks(content);

        if (existing == null) {
            schedule.check(R.id.radio_schedule_every_day);
            active.setChecked(true);
        } else {
            title.setText(existing.getTitle());
            description.setText(existing.getDescription());
            active.setChecked(existing.isActive());
            if (existing.getScheduleType() == ScheduleType.WEEKDAYS) {
                schedule.check(R.id.radio_schedule_weekdays);
            } else if (existing.getScheduleType() == ScheduleType.CUSTOM) {
                schedule.check(R.id.radio_schedule_custom);
                Set<Integer> selected = DaysOfWeek.parse(existing.getDaysOfWeek());
                for (int i = 0; i < dayChecks.length; i++) dayChecks[i].setChecked(selected.contains(i + 1));
            } else {
                schedule.check(R.id.radio_schedule_every_day);
            }
        }
        updateCustomDays(schedule.getCheckedRadioButtonId(), customDays);
        schedule.setOnCheckedChangeListener((group, checkedId) -> updateCustomDays(checkedId, customDays));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? R.string.add_routine : R.string.edit_routine)
                .setView(content)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button -> {
            String rawTitle = title.getText() == null ? "" : title.getText().toString().trim();
            if (rawTitle.isEmpty()) {
                title.setError(getString(R.string.routine_title_required));
                return;
            }
            ScheduleType type = selectedSchedule(schedule.getCheckedRadioButtonId());
            Set<Integer> days = selectedDays(dayChecks);
            if (type == ScheduleType.CUSTOM && days.isEmpty()) {
                dayChecks[0].setError(getString(R.string.routine_days_required));
                return;
            }
            String rawDescription = description.getText() == null ? null : description.getText().toString();
            viewModel.saveRoutine(existing == null ? 0 : existing.getId(), rawTitle, rawDescription,
                    type, type == ScheduleType.CUSTOM ? DaysOfWeek.serialize(days) : null,
                    existing == null ? null : existing.getReminderTime(), active.isChecked());
            dialog.dismiss();
        }));
        dialog.show();
    }

    private MaterialCheckBox[] dayChecks(View view) {
        return new MaterialCheckBox[]{view.findViewById(R.id.check_day_monday),
                view.findViewById(R.id.check_day_tuesday), view.findViewById(R.id.check_day_wednesday),
                view.findViewById(R.id.check_day_thursday), view.findViewById(R.id.check_day_friday),
                view.findViewById(R.id.check_day_saturday), view.findViewById(R.id.check_day_sunday)};
    }

    private Set<Integer> selectedDays(MaterialCheckBox[] checks) {
        Set<Integer> result = new LinkedHashSet<>();
        for (int i = 0; i < checks.length; i++) if (checks[i].isChecked()) result.add(i + 1);
        return result;
    }

    private ScheduleType selectedSchedule(int id) {
        if (id == R.id.radio_schedule_weekdays) return ScheduleType.WEEKDAYS;
        if (id == R.id.radio_schedule_custom) return ScheduleType.CUSTOM;
        return ScheduleType.EVERY_DAY;
    }

    private void updateCustomDays(int id, View customDays) {
        customDays.setVisibility(id == R.id.radio_schedule_custom ? View.VISIBLE : View.GONE);
    }

    private void confirmDelete(Routine routine) {
        new AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.delete_routine_message, routine.getTitle()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> viewModel.deleteRoutine(routine))
                .show();
    }
}
