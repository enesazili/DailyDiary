package com.example.dailydiary.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailydiary.R;
import com.example.dailydiary.domain.model.DayLog;
import com.example.dailydiary.ui.dashboard.DashboardRoutineAdapter;
import com.example.dailydiary.ui.dashboard.DashboardTaskAdapter;
import com.example.dailydiary.util.LocalDates;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

public class DayDetailFragment extends Fragment {

    private DashboardTaskAdapter taskAdapter;
    private DashboardRoutineAdapter routineAdapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_day_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String date = getArguments() == null ? null : getArguments().getString("localDate");
        if (date == null) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        DayDetailViewModel viewModel = new ViewModelProvider(this).get(DayDetailViewModel.class);

        taskAdapter = new DashboardTaskAdapter(null);
        routineAdapter = new DashboardRoutineAdapter(null);

        // Header & Back
        MaterialButton buttonBack = view.findViewById(R.id.button_day_detail_back);
        TextView textTitle = view.findViewById(R.id.text_day_detail_title);
        buttonBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Progress
        TextView textProgressPercentage = view.findViewById(R.id.text_day_detail_progress_percentage);
        LinearProgressIndicator progressBar = view.findViewById(R.id.progress_day_detail);
        TextView textProgress = view.findViewById(R.id.text_day_detail_progress);

        // Tasks
        TextView textTasksBadge = view.findViewById(R.id.text_day_tasks_badge);
        TextView textTasksEmpty = view.findViewById(R.id.text_day_tasks_empty);
        RecyclerView listTasks = view.findViewById(R.id.list_day_tasks);
        listTasks.setAdapter(taskAdapter);

        // Routines
        TextView textRoutinesBadge = view.findViewById(R.id.text_day_routines_badge);
        TextView textRoutinesEmpty = view.findViewById(R.id.text_day_routines_empty);
        RecyclerView listRoutines = view.findViewById(R.id.list_day_routines);
        listRoutines.setAdapter(routineAdapter);

        // Journal & Summary
        TextView textJournal = view.findViewById(R.id.text_day_detail_journal);
        TextView textSummary = view.findViewById(R.id.text_day_detail_summary);
        MaterialButton buttonEditJournal = view.findViewById(R.id.button_edit_day_journal);

        buttonEditJournal.setOnClickListener(v -> {
            Bundle arguments = new Bundle();
            arguments.putString("localDate", date);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_dayDetailFragment_to_journalFragment, arguments);
        });

        viewModel.setLocalDate(date);
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                return;
            }

            textTitle.setText(LocalDates.displayLong(state.localDate));

            int completedTasks = state.completedTaskCount();
            int totalTasks = state.tasks.size();
            int completedRoutines = state.completedRoutineCount();
            int totalRoutines = state.routines.size();

            int total = totalTasks + totalRoutines;
            int completed = completedTasks + completedRoutines;
            int percentage = total > 0 ? (completed * 100) / total : 0;

            textProgressPercentage.setText(String.format(Locale.getDefault(), "%%%d", percentage));
            progressBar.setMax(Math.max(1, total));
            progressBar.setProgress(completed);
            textProgress.setText(getString(R.string.day_detail_progress, completed, total));

            // Tasks
            textTasksBadge.setText(String.format(Locale.getDefault(), "%d / %d", completedTasks, totalTasks));
            if (state.tasks.isEmpty()) {
                textTasksEmpty.setVisibility(View.VISIBLE);
                listTasks.setVisibility(View.GONE);
            } else {
                textTasksEmpty.setVisibility(View.GONE);
                listTasks.setVisibility(View.VISIBLE);
                taskAdapter.submitList(requireContext(), state.tasks);
            }

            // Routines
            textRoutinesBadge.setText(String.format(Locale.getDefault(), "%d / %d", completedRoutines, totalRoutines));
            if (state.routines.isEmpty()) {
                textRoutinesEmpty.setVisibility(View.VISIBLE);
                listRoutines.setVisibility(View.GONE);
            } else {
                textRoutinesEmpty.setVisibility(View.GONE);
                listRoutines.setVisibility(View.VISIBLE);
                routineAdapter.submitList(state.routines, state.completedRoutineIds);
            }

            // Log & Summary
            bindLog(textJournal, textSummary, state.dayLog);
        });
    }

    private void bindLog(TextView journal, TextView summary, DayLog log) {
        String journalText = log == null ? null : log.getFreeText();
        String summaryText = log == null ? null : log.getSummaryText();
        journal.setText(journalText == null || journalText.trim().isEmpty()
                ? getString(R.string.day_detail_no_journal) : journalText);
        summary.setText(summaryText == null || summaryText.trim().isEmpty()
                ? getString(R.string.day_detail_no_summary) : summaryText);
    }
}
