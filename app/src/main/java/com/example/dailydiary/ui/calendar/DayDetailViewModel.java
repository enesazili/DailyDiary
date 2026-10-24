package com.example.dailydiary.ui.calendar;

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
import com.example.dailydiary.util.LiveDataCombiner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DayDetailViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;
    private final RoutineRepository routineRepository;
    private final DayLogRepository dayLogRepository;
    private final StreakRepository streakRepository;
    private final MutableLiveData<String> localDate = new MutableLiveData<>();

    public DayDetailViewModel(@NonNull Application application) {
        super(application);
        AppContainer container = ((DailyDiaryApp) application).getAppContainer();
        taskRepository = container.getTaskRepository();
        routineRepository = container.getRoutineRepository();
        dayLogRepository = container.getDayLogRepository();
        streakRepository = container.getStreakRepository();
    }

    public void setLocalDate(String date) {
        if (date != null && !date.equals(localDate.getValue())) {
            localDate.setValue(date);
        }
    }

    public LiveData<DayDetailState> getState() {
        return Transformations.switchMap(localDate, date -> LiveDataCombiner.combine(
                taskRepository.observeByDueDate(date),
                routineRepository.observeAll(),
                routineRepository.observeCompletions(date),
                LiveDataCombiner.combine(
                        dayLogRepository.observeByLocalDate(date),
                        streakRepository.observeSnapshot(),
                        (log, streak) -> new Object[]{log, streak}
                ),
                (tasks, routines, completions, extra) -> toState(date, tasks, routines, completions, extra)
        ));
    }

    private DayDetailState toState(
            String date,
            List<Task> tasks,
            List<Routine> routines,
            List<RoutineCompletion> completions,
            Object[] extra
    ) {
        DayLog log = extra == null ? null : (DayLog) extra[0];
        StreakSnapshot streak = extra != null && extra[1] instanceof StreakSnapshot
                ? (StreakSnapshot) extra[1]
                : StreakSnapshot.empty();
        List<Routine> forDay = new ArrayList<>();
        if (routines != null) {
            for (Routine routine : routines) {
                if (routine.isScheduledOn(date)) {
                    forDay.add(routine);
                }
            }
        }
        Set<Long> done = new HashSet<>();
        if (completions != null) {
            for (RoutineCompletion completion : completions) {
                done.add(completion.getRoutineId());
            }
        }
        return new DayDetailState(date, tasks == null ? new ArrayList<>() : tasks, forDay, done, log, streak);
    }

    public static final class DayDetailState {
        public final String localDate;
        public final List<Task> tasks;
        public final List<Routine> routines;
        public final Set<Long> completedRoutineIds;
        public final DayLog dayLog;
        public final StreakSnapshot streak;

        DayDetailState(
                String localDate,
                List<Task> tasks,
                List<Routine> routines,
                Set<Long> completedRoutineIds,
                DayLog dayLog,
                StreakSnapshot streak
        ) {
            this.localDate = localDate;
            this.tasks = tasks;
            this.routines = routines;
            this.completedRoutineIds = completedRoutineIds;
            this.dayLog = dayLog;
            this.streak = streak;
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

        public int completedRoutineCount() {
            int count = 0;
            for (Routine routine : routines) {
                if (completedRoutineIds.contains(routine.getId())) {
                    count++;
                }
            }
            return count;
        }
    }
}
