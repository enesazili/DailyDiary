package com.example.dailydiary.ui.dashboard;

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
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailydiary.R;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.Task;
import com.example.dailydiary.domain.model.TaskCategories;
import com.example.dailydiary.ui.common.CategoryLabels;
import com.example.dailydiary.util.GreetingPeriod;
import com.example.dailydiary.util.LocalDates;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private DashboardTaskAdapter taskAdapter;
    private DashboardRoutineAdapter routineAdapter;
    private DashboardUiState currentState;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        taskAdapter = new DashboardTaskAdapter((task, completed) ->
                viewModel.setTaskCompleted(task, completed));

        routineAdapter = new DashboardRoutineAdapter((routine, completed) ->
                viewModel.toggleRoutine(routine));

        // 1. Header views
        TextView textGreeting = view.findViewById(R.id.text_greeting);
        TextView textTodayDate = view.findViewById(R.id.text_today_date);

        // 2. Streak views
        TextView textStreakCurrent = view.findViewById(R.id.text_streak_current);
        TextView textStreakBest = view.findViewById(R.id.text_streak_best);

        // 3. Progress views
        TextView textProgressPercentage = view.findViewById(R.id.text_progress_percentage);
        LinearProgressIndicator progressBarToday = view.findViewById(R.id.progress_bar_today);
        TextView textProgressTasks = view.findViewById(R.id.text_progress_tasks);
        TextView textProgressRoutines = view.findViewById(R.id.text_progress_routines);

        // 4. Tasks views
        TextView textTasksBadge = view.findViewById(R.id.text_tasks_badge);
        TextView textTasksEmpty = view.findViewById(R.id.text_tasks_empty);
        RecyclerView listTasks = view.findViewById(R.id.list_tasks);
        listTasks.setAdapter(taskAdapter);

        // 5. Routines views
        TextView textRoutinesBadge = view.findViewById(R.id.text_routines_badge);
        TextView textRoutinesEmpty = view.findViewById(R.id.text_routines_empty);
        RecyclerView listRoutines = view.findViewById(R.id.list_routines);
        listRoutines.setAdapter(routineAdapter);

        // 6. Close Day views
        MaterialCardView cardCloseDay = view.findViewById(R.id.card_close_day);
        MaterialButton buttonCloseDay = view.findViewById(R.id.button_close_day);
        TextView textCloseDayTitle = view.findViewById(R.id.text_close_day_button_title);
        TextView textCloseDaySubtitle = view.findViewById(R.id.text_close_day_button_subtitle);

        View.OnClickListener closeDayClickListener = v -> showCloseDayDialog();
        cardCloseDay.setOnClickListener(closeDayClickListener);
        buttonCloseDay.setOnClickListener(closeDayClickListener);

        // FAB: Add Task
        FloatingActionButton fabAddTask = view.findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());

        // Observe UI State
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                return;
            }
            currentState = state;

            // 1. Header
            textGreeting.setText(greetingText(state.greetingPeriod));
            textTodayDate.setText(LocalDates.displayMedium(state.todayDate));

            // 2. Streak Card
            int currentStreak = state.streak.getCurrentCount();
            int bestStreak = state.streak.getBestCount();
            textStreakCurrent.setText(getString(R.string.streak_days_format, currentStreak));
            if (bestStreak > 0) {
                textStreakBest.setVisibility(View.VISIBLE);
                textStreakBest.setText(getString(R.string.streak_best_format, bestStreak));
            } else {
                textStreakBest.setVisibility(View.GONE);
            }

            // 3. Progress Card
            int completedTasks = state.completedTaskCount();
            int totalTasks = state.totalTaskCount();
            int completedRoutines = state.completedRoutineCount();
            int totalRoutines = state.todayRoutines.size();

            int totalItems = totalTasks + totalRoutines;
            int completedItems = completedTasks + completedRoutines;
            int percentage = totalItems > 0 ? (completedItems * 100) / totalItems : 0;

            textProgressPercentage.setText(String.format(java.util.Locale.getDefault(), "%%%d", percentage));
            progressBarToday.setMax(Math.max(totalItems, 1));
            progressBarToday.setProgress(completedItems);

            textProgressTasks.setText(getString(R.string.today_tasks_progress, completedTasks, totalTasks));
            textProgressRoutines.setText(getString(R.string.today_routines_progress, completedRoutines, totalRoutines));

            // 4. Tasks Card
            textTasksBadge.setText(String.format(java.util.Locale.getDefault(), "%d / %d", completedTasks, totalTasks));
            if (state.tasks.isEmpty()) {
                textTasksEmpty.setVisibility(View.VISIBLE);
                listTasks.setVisibility(View.GONE);
            } else {
                textTasksEmpty.setVisibility(View.GONE);
                listTasks.setVisibility(View.VISIBLE);
                taskAdapter.submitList(requireContext(), state.tasks);
            }

            // 5. Routines Card
            Set<Long> completedRoutineIds = viewModel.completedRoutineIds(state.completions);
            textRoutinesBadge.setText(String.format(java.util.Locale.getDefault(), "%d / %d", completedRoutines, totalRoutines));
            if (state.todayRoutines.isEmpty()) {
                textRoutinesEmpty.setVisibility(View.VISIBLE);
                listRoutines.setVisibility(View.GONE);
            } else {
                textRoutinesEmpty.setVisibility(View.GONE);
                listRoutines.setVisibility(View.VISIBLE);
                routineAdapter.submitList(state.todayRoutines, completedRoutineIds);
            }

            // 6. Close Day state
            if (state.dayLog != null) {
                textCloseDayTitle.setText(R.string.close_day_done);
                textCloseDaySubtitle.setText(R.string.edit_journal);
                buttonCloseDay.setText(R.string.save);
            } else {
                textCloseDayTitle.setText(R.string.close_day);
                textCloseDaySubtitle.setText(R.string.close_day_description);
                buttonCloseDay.setText(R.string.close_day);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshToday();
        }
    }

    private void showCloseDayDialog() {
        if (currentState == null) {
            return;
        }
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_close_day, null, false);
        TextView tasks = content.findViewById(R.id.text_close_day_tasks);
        TextView routines = content.findViewById(R.id.text_close_day_routines);
        TextView streak = content.findViewById(R.id.text_close_day_streak);
        TextInputEditText note = content.findViewById(R.id.input_close_day_note);

        tasks.setText(getString(R.string.close_day_tasks,
                currentState.completedTaskCount(), currentState.totalTaskCount()));
        routines.setText(getString(R.string.close_day_routines,
                currentState.completedRoutineCount(), currentState.todayRoutines.size()));
        streak.setText(getString(R.string.close_day_streak, currentState.streak.getCurrentCount()));
        if (currentState.dayLog != null) {
            note.setText(currentState.dayLog.getFreeText());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.close_day)
                .setView(content)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String text = note.getText() == null ? "" : note.getText().toString();
                    viewModel.closeDay(text, currentState);
                })
                .show();
    }

    private String greetingText(GreetingPeriod period) {
        if (period == GreetingPeriod.AFTERNOON) {
            return getString(R.string.greeting_afternoon);
        }
        if (period == GreetingPeriod.EVENING) {
            return getString(R.string.greeting_evening);
        }
        return getString(R.string.greeting_morning);
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_task, null, false);
        TextInputEditText titleInput = dialogView.findViewById(R.id.input_task_title);
        TextInputEditText notesInput = dialogView.findViewById(R.id.input_task_notes);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_task_category);

        List<String> categoryIds = TaskCategories.builtInIds();
        List<String> categoryDisplayNames = new ArrayList<>();
        for (String id : categoryIds) {
            categoryDisplayNames.add(CategoryLabels.label(requireContext(), id));
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryDisplayNames
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Default to KISISEL
        int defaultIndex = categoryIds.indexOf(TaskCategories.defaultId());
        if (defaultIndex >= 0) {
            categorySpinner.setSelection(defaultIndex);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_task)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String title = titleInput.getText() == null ? "" : titleInput.getText().toString();
                    if (title.trim().isEmpty()) {
                        return;
                    }
                    String notes = notesInput.getText() == null ? null : notesInput.getText().toString();
                    int selectedPos = categorySpinner.getSelectedItemPosition();
                    String selectedCategory = (selectedPos >= 0 && selectedPos < categoryIds.size())
                            ? categoryIds.get(selectedPos)
                            : TaskCategories.defaultId();

                    viewModel.addTask(
                            title,
                            notes,
                            selectedCategory,
                            viewModel.getToday(),
                            null
                    );
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
