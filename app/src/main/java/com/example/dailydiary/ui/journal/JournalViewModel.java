package com.example.dailydiary.ui.journal;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.dailydiary.DailyDiaryApp;
import com.example.dailydiary.data.repository.DayLogRepository;
import com.example.dailydiary.data.repository.RoutineRepository;
import com.example.dailydiary.data.repository.StreakRepository;
import com.example.dailydiary.data.repository.TaskRepository;
import com.example.dailydiary.di.AppContainer;
import com.example.dailydiary.domain.model.DayLog;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.RoutineCompletion;
import com.example.dailydiary.domain.model.StreakSnapshot;
import com.example.dailydiary.domain.model.Task;
import com.example.dailydiary.domain.summary.DailySummaryBuilder;
import com.example.dailydiary.util.LiveDataCombiner;

import java.util.ArrayList;
import java.util.List;

public class JournalViewModel extends AndroidViewModel {

    private final DayLogRepository dayLogRepository;
    private final TaskRepository taskRepository;
    private final RoutineRepository routineRepository;
    private final StreakRepository streakRepository;
    private final MutableLiveData<String> localDate = new MutableLiveData<>();

    public JournalViewModel(@NonNull Application application) {
        super(application);
        AppContainer container = ((DailyDiaryApp) application).getAppContainer();
        dayLogRepository = container.getDayLogRepository();
        taskRepository = container.getTaskRepository();
        routineRepository = container.getRoutineRepository();
        streakRepository = container.getStreakRepository();
    }

    public void setLocalDate(String date) {
        if (date != null && !date.equals(localDate.getValue())) {
            localDate.setValue(date);
        }
    }

    public LiveData<JournalState> getState() {
        return Transformations.switchMap(localDate, date -> LiveDataCombiner.combine(
                dayLogRepository.observeByLocalDate(date),
                taskRepository.observeByDueDate(date),
                routineRepository.observeCompletions(date),
                LiveDataCombiner.combine(
                        routineRepository.observeAll(),
                        streakRepository.observeSnapshot(),
                        (routines, streak) -> new Object[]{routines, streak}
                ),
                (log, tasks, completions, extra) -> toState(date, log, tasks, completions, extra)
        ));
    }

    public void saveNote(String note) {
        String date = localDate.getValue();
        if (date == null) {
            return;
        }
        dayLogRepository.saveFreeText(date, note);
    }

    public void refreshSummary(JournalState state) {
        if (state == null) {
            return;
        }
        String summary = DailySummaryBuilder.build(
                getApplication(),
                state.completedTasks,
                state.totalTasks,
                state.completedRoutines,
                state.totalRoutines,
                state.streak
        );
        DayLog current = state.dayLog;
        String freeText = current == null ? "" : current.getFreeText();
        dayLogRepository.saveCloseDay(state.localDate, freeText, summary);
    }

    private JournalState toState(
            String date,
            DayLog log,
            List<Task> tasks,
            List<RoutineCompletion> completions,
            Object[] extra
    ) {
        @SuppressWarnings("unchecked")
        List<Routine> routines = extra == null ? new ArrayList<>() : (List<Routine>) extra[0];
        StreakSnapshot streak = extra != null && extra[1] instanceof StreakSnapshot
                ? (StreakSnapshot) extra[1]
                : StreakSnapshot.empty();
        int totalTasks = tasks == null ? 0 : tasks.size();
        int completedTasks = 0;
        List<Task> doneTasks = new ArrayList<>();
        if (tasks != null) {
            for (Task task : tasks) {
                if (task.isCompleted()) {
                    completedTasks++;
                    doneTasks.add(task);
                }
            }
        }
        int totalRoutines = 0;
        List<Routine> doneRoutines = new ArrayList<>();
        if (routines != null) {
            for (Routine routine : routines) {
                if (routine.isScheduledOn(date)) {
                    totalRoutines++;
                    if (completions != null) {
                        for (RoutineCompletion completion : completions) {
                            if (completion.getRoutineId() == routine.getId()) {
                                doneRoutines.add(routine);
                                break;
                            }
                        }
                    }
                }
            }
        }
        String autoSummary = DailySummaryBuilder.build(
                getApplication(),
                completedTasks,
                totalTasks,
                doneRoutines.size(),
                totalRoutines,
                streak
        );
        return new JournalState(
                date,
                log,
                doneTasks,
                doneRoutines,
                completedTasks,
                totalTasks,
                doneRoutines.size(),
                totalRoutines,
                streak,
                autoSummary
        );
    }

    public static final class JournalState {
        public final String localDate;
        public final DayLog dayLog;
        public final List<Task> completedTaskList;
        public final List<Routine> completedRoutineList;
        public final int completedTasks;
        public final int totalTasks;
        public final int completedRoutines;
        public final int totalRoutines;
        public final StreakSnapshot streak;
        public final String autoSummary;

        JournalState(
                String localDate,
                DayLog dayLog,
                List<Task> completedTaskList,
                List<Routine> completedRoutineList,
                int completedTasks,
                int totalTasks,
                int completedRoutines,
                int totalRoutines,
                StreakSnapshot streak,
                String autoSummary
        ) {
            this.localDate = localDate;
            this.dayLog = dayLog;
            this.completedTaskList = completedTaskList;
            this.completedRoutineList = completedRoutineList;
            this.completedTasks = completedTasks;
            this.totalTasks = totalTasks;
            this.completedRoutines = completedRoutines;
            this.totalRoutines = totalRoutines;
            this.streak = streak;
            this.autoSummary = autoSummary;
        }
    }
}
