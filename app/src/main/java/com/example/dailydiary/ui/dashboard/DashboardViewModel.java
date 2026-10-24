package com.example.dailydiary.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.dailydiary.DailyDiaryApp;
import com.example.dailydiary.data.repository.DayLogRepository;
import com.example.dailydiary.data.repository.ReminderRepository;
import com.example.dailydiary.data.repository.RoutineRepository;
import com.example.dailydiary.data.repository.StreakRepository;
import com.example.dailydiary.data.repository.TaskRepository;
import com.example.dailydiary.di.AppContainer;
import com.example.dailydiary.domain.model.DayLog;
import com.example.dailydiary.domain.model.Reminder;
import com.example.dailydiary.domain.model.ReminderTargetType;
import com.example.dailydiary.domain.model.RepeatRule;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.RoutineCompletion;
import com.example.dailydiary.domain.model.StreakSnapshot;
import com.example.dailydiary.domain.model.Task;
import com.example.dailydiary.domain.summary.DailySummaryBuilder;
import com.example.dailydiary.notification.AlarmScheduler;
import com.example.dailydiary.util.LiveDataCombiner;
import com.example.dailydiary.util.LocalDates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;
    private final RoutineRepository routineRepository;
    private final DayLogRepository dayLogRepository;
    private final ReminderRepository reminderRepository;
    private final AlarmScheduler alarmScheduler;
    private final MutableLiveData<String> today = new MutableLiveData<>();
    private final LiveData<DashboardUiState> uiState;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        AppContainer container = ((DailyDiaryApp) application).getAppContainer();
        taskRepository = container.getTaskRepository();
        routineRepository = container.getRoutineRepository();
        dayLogRepository = container.getDayLogRepository();
        reminderRepository = container.getReminderRepository();
        alarmScheduler = container.getAlarmScheduler();
        StreakRepository streakRepository = container.getStreakRepository();
        today.setValue(LocalDates.today());
        uiState = Transformations.switchMap(today, date -> LiveDataCombiner.combine(
                taskRepository.observeByDueDate(date),
                routineRepository.observeAll(),
                routineRepository.observeCompletions(date),
                LiveDataCombiner.combine(
                        dayLogRepository.observeByLocalDate(date),
                        streakRepository.observeSnapshot(),
                        (dayLog, streak) -> new Object[]{dayLog, streak}
                ),
                (tasks, routines, completions, extra) -> buildState(date, tasks, routines, completions, extra)
        ));
    }

    public LiveData<DashboardUiState> getUiState() {
        return uiState;
    }

    public String getToday() {
        String value = today.getValue();
        return value == null ? LocalDates.today() : value;
    }

    public void refreshToday() {
        String current = LocalDates.today();
        if (!current.equals(today.getValue())) {
            today.setValue(current);
        }
    }

    public void setTaskCompleted(Task task, boolean completed) {
        Long at = completed ? System.currentTimeMillis() : null;
        taskRepository.update(task.withCompleted(completed, at));
    }

    public void addTask(String title, String notes, String category, String dueDate, Long reminderAt) {
        String trimmedTitle = title == null ? "" : title.trim();
        if (trimmedTitle.isEmpty()) {
            return;
        }
        String trimmedNotes = notes == null || notes.trim().isEmpty() ? null : notes.trim();
        String date = dueDate == null ? getToday() : dueDate;
        Task task = new Task(0, trimmedTitle, trimmedNotes, category, date, false, null, 0);
        taskRepository.insert(task, id -> {
            if (reminderAt != null) {
                Reminder reminder = new Reminder(
                        0,
                        ReminderTargetType.TASK,
                        id,
                        reminderAt,
                        RepeatRule.NONE,
                        true
                );
                reminderRepository.insert(reminder, reminderId ->
                        alarmScheduler.schedule(new Reminder(
                                reminderId,
                                reminder.getTargetType(),
                                id,
                                reminderAt,
                                RepeatRule.NONE,
                                true
                        )));
            }
        });
    }

    public void toggleRoutine(Routine routine) {
        routineRepository.toggleCompletion(routine.getId(), getToday());
    }

    public void closeDay(String note, DashboardUiState state) {
        String summary = DailySummaryBuilder.build(
                getApplication(),
                state.completedTaskCount(),
                state.totalTaskCount(),
                state.completedRoutineCount(),
                state.todayRoutines.size(),
                state.streak
        );
        dayLogRepository.saveCloseDay(state.todayDate, note, summary);
    }

    private DashboardUiState buildState(
            String date,
            List<Task> tasks,
            List<Routine> routines,
            List<RoutineCompletion> completions,
            Object[] dayAndStreak
    ) {
        DayLog dayLog = null;
        StreakSnapshot streak = StreakSnapshot.empty();
        if (dayAndStreak != null) {
            dayLog = (DayLog) dayAndStreak[0];
            if (dayAndStreak[1] instanceof StreakSnapshot) {
                streak = (StreakSnapshot) dayAndStreak[1];
            }
        }
        List<Routine> todayRoutines = new ArrayList<>();
        if (routines != null) {
            for (Routine routine : routines) {
                if (routine.appliesOn(date)) {
                    todayRoutines.add(routine);
                }
            }
        }
        return new DashboardUiState(
                LocalDates.greetingPeriod(),
                date,
                streak,
                tasks,
                todayRoutines,
                completions,
                dayLog
        );
    }

    public Set<Long> completedRoutineIds(List<RoutineCompletion> completions) {
        Set<Long> ids = new HashSet<>();
        if (completions == null) {
            return ids;
        }
        for (RoutineCompletion completion : completions) {
            ids.add(completion.getRoutineId());
        }
        return ids;
    }
}
