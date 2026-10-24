package com.example.dailydiary.ui.dashboard;

import com.example.dailydiary.domain.model.DayLog;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.RoutineCompletion;
import com.example.dailydiary.domain.model.StreakSnapshot;
import com.example.dailydiary.domain.model.Task;
import com.example.dailydiary.domain.model.TaskCategories;
import com.example.dailydiary.util.GreetingPeriod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DashboardUiState {

    public final GreetingPeriod greetingPeriod;
    public final String todayDate;
    public final StreakSnapshot streak;
    public final List<Task> tasks;
    public final List<Routine> todayRoutines;
    public final List<RoutineCompletion> completions;
    public final DayLog dayLog;

    public DashboardUiState(
            GreetingPeriod greetingPeriod,
            String todayDate,
            StreakSnapshot streak,
            List<Task> tasks,
            List<Routine> todayRoutines,
            List<RoutineCompletion> completions,
            DayLog dayLog
    ) {
        this.greetingPeriod = greetingPeriod;
        this.todayDate = todayDate;
        this.streak = streak == null ? StreakSnapshot.empty() : streak;
        this.tasks = tasks == null ? new ArrayList<>() : tasks;
        this.todayRoutines = todayRoutines == null ? new ArrayList<>() : todayRoutines;
        this.completions = completions == null ? new ArrayList<>() : completions;
        this.dayLog = dayLog;
    }

    public int completedTaskCount() {
        int count = 0;
        for (Task task : tasks) {
            if (task.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public int totalTaskCount() {
        return tasks.size();
    }

    public int completedRoutineCount() {
        int count = 0;
        for (Routine routine : todayRoutines) {
            for (RoutineCompletion completion : completions) {
                if (completion.getRoutineId() == routine.getId()) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    public List<DashboardItem> taskItems(java.util.function.Function<String, String> labels) {
        Map<String, List<Task>> grouped = new LinkedHashMap<>();
        for (String id : TaskCategories.builtInIds()) {
            grouped.put(id, new ArrayList<>());
        }
        List<Task> extras = new ArrayList<>();
        for (Task task : tasks) {
            if (grouped.containsKey(task.getCategory())) {
                grouped.get(task.getCategory()).add(task);
            } else {
                extras.add(task);
            }
        }
        List<DashboardItem> items = new ArrayList<>();
        for (Map.Entry<String, List<Task>> entry : grouped.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            items.add(new DashboardItem.Header(labels.apply(entry.getKey())));
            for (Task task : entry.getValue()) {
                items.add(new DashboardItem.TaskRow(task));
            }
        }
        if (!extras.isEmpty()) {
            items.add(new DashboardItem.Header(labels.apply(TaskCategories.DIGER)));
            for (Task task : extras) {
                items.add(new DashboardItem.TaskRow(task));
            }
        }
        return items;
    }
}
