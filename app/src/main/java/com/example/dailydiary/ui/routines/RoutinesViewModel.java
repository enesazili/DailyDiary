package com.example.dailydiary.ui.routines;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.dailydiary.DailyDiaryApp;
import com.example.dailydiary.data.repository.ReminderRepository;
import com.example.dailydiary.data.repository.RoutineRepository;
import com.example.dailydiary.di.AppContainer;
import com.example.dailydiary.domain.model.Reminder;
import com.example.dailydiary.domain.model.ReminderTargetType;
import com.example.dailydiary.domain.model.RepeatRule;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.RoutineCompletion;
import com.example.dailydiary.domain.model.ScheduleType;
import com.example.dailydiary.notification.AlarmScheduler;
import com.example.dailydiary.util.LiveDataCombiner;
import com.example.dailydiary.util.LocalDates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoutinesViewModel extends AndroidViewModel {

    private final RoutineRepository routineRepository;
    private final ReminderRepository reminderRepository;
    private final AlarmScheduler alarmScheduler;
    private final String today;
    private final LiveData<List<Routine>> routines;
    private final LiveData<Set<Long>> completedIds;

    public RoutinesViewModel(@NonNull Application application) {
        super(application);
        AppContainer container = ((DailyDiaryApp) application).getAppContainer();
        routineRepository = container.getRoutineRepository();
        reminderRepository = container.getReminderRepository();
        alarmScheduler = container.getAlarmScheduler();
        today = LocalDates.today();
        routines = routineRepository.observeAll();
        completedIds = LiveDataCombiner.combine(
                routineRepository.observeCompletions(today),
                routines,
                (completions, ignored) -> {
                    Set<Long> ids = new HashSet<>();
                    if (completions != null) {
                        for (RoutineCompletion completion : completions) {
                            ids.add(completion.getRoutineId());
                        }
                    }
                    return ids;
                }
        );
    }

    public LiveData<List<Routine>> getRoutines() {
        return routines;
    }

    public LiveData<Set<Long>> getCompletedIds() {
        return completedIds;
    }

    public String getToday() {
        return today;
    }

    public void toggleToday(Routine routine) {
        if (routine.appliesOn(today)) {
            routineRepository.toggleCompletion(routine.getId(), today);
        }
    }

    public void saveRoutine(
            long id,
            String title,
            String description,
            ScheduleType scheduleType,
            String daysOfWeek,
            String reminderTime,
            boolean active
    ) {
        String trimmed = title == null ? "" : title.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        Routine routine = new Routine(
                id,
                trimmed,
                description == null || description.trim().isEmpty() ? null : description.trim(),
                scheduleType,
                daysOfWeek,
                reminderTime,
                active
        );
        if (id == 0) {
            routineRepository.insert(routine, newId -> maybeSchedule(newId, reminderTime, scheduleType));
        } else {
            routineRepository.update(routine);
            maybeSchedule(id, reminderTime, scheduleType);
        }
    }

    public void setActive(Routine routine, boolean active) {
        routineRepository.update(new Routine(
                routine.getId(),
                routine.getTitle(),
                routine.getDescription(),
                routine.getScheduleType(),
                routine.getDaysOfWeek(),
                routine.getReminderTime(),
                active
        ));
    }

    public void deleteRoutine(Routine routine) {
        routineRepository.deleteById(routine.getId());
    }

    private void maybeSchedule(long routineId, String reminderTime, ScheduleType scheduleType) {
        if (reminderTime == null || reminderTime.isEmpty()) {
            return;
        }
        int[] hm = LocalDates.parseTime(reminderTime);
        long trigger = LocalDates.atTimeMillis(today, hm[0], hm[1]);
        final RepeatRule repeat;
        if (scheduleType == ScheduleType.WEEKDAYS) {
            repeat = RepeatRule.WEEKDAYS;
        } else if (scheduleType == ScheduleType.CUSTOM) {
            repeat = RepeatRule.WEEKLY;
        } else {
            repeat = RepeatRule.DAILY;
        }
        Reminder reminder = new Reminder(
                0,
                ReminderTargetType.ROUTINE,
                routineId,
                trigger,
                repeat,
                true
        );
        reminderRepository.insert(reminder, reminderId ->
                alarmScheduler.schedule(new Reminder(
                        reminderId,
                        ReminderTargetType.ROUTINE,
                        routineId,
                        trigger,
                        repeat,
                        true
                )));
    }

    public List<Routine> todayRoutines(List<Routine> all) {
        List<Routine> result = new ArrayList<>();
        if (all == null) {
            return result;
        }
        for (Routine routine : all) {
            if (routine.appliesOn(today)) {
                result.add(routine);
            }
        }
        return result;
    }
}
