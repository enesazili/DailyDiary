package com.example.dailydiary.ui.reminders;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.dailydiary.DailyDiaryApp;
import com.example.dailydiary.data.repository.ReminderRepository;
import com.example.dailydiary.data.repository.RoutineRepository;
import com.example.dailydiary.data.repository.TaskRepository;
import com.example.dailydiary.di.AppContainer;
import com.example.dailydiary.domain.model.Reminder;
import com.example.dailydiary.domain.model.ReminderTargetType;
import com.example.dailydiary.domain.model.RepeatRule;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.Task;
import com.example.dailydiary.notification.AlarmScheduler;
import com.example.dailydiary.util.LiveDataCombiner;

import java.util.ArrayList;
import java.util.List;

public class RemindersViewModel extends AndroidViewModel {

    private final ReminderRepository reminderRepository;
    private final TaskRepository taskRepository;
    private final RoutineRepository routineRepository;
    private final AlarmScheduler alarmScheduler;
    private final LiveData<List<ReminderRow>> rows;

    public RemindersViewModel(@NonNull Application application) {
        super(application);
        AppContainer container = ((DailyDiaryApp) application).getAppContainer();
        reminderRepository = container.getReminderRepository();
        taskRepository = container.getTaskRepository();
        routineRepository = container.getRoutineRepository();
        alarmScheduler = container.getAlarmScheduler();
        rows = LiveDataCombiner.combine(
                reminderRepository.observeAll(),
                taskRepository.observeAll(),
                routineRepository.observeAll(),
                this::toRows
        );
    }

    public LiveData<List<ReminderRow>> getRows() {
        return rows;
    }

    public LiveData<List<Task>> getTasks() {
        return taskRepository.observeAll();
    }

    public LiveData<List<Routine>> getRoutines() {
        return routineRepository.observeAll();
    }

    public void addReminder(
            ReminderTargetType type,
            long targetId,
            long triggerAt,
            RepeatRule repeatRule,
            boolean enabled
    ) {
        Reminder reminder = new Reminder(0, type, targetId, triggerAt, repeatRule, enabled);
        reminderRepository.insert(reminder, id -> {
            Reminder stored = new Reminder(id, type, targetId, triggerAt, repeatRule, enabled);
            if (enabled) {
                alarmScheduler.schedule(stored);
            }
        });
    }

    public void setEnabled(Reminder reminder, boolean enabled) {
        Reminder updated = new Reminder(
                reminder.getId(),
                reminder.getTargetType(),
                reminder.getTargetId(),
                reminder.getTriggerAt(),
                reminder.getRepeatRule(),
                enabled
        );
        alarmScheduler.cancel(reminder.getId());
        if (enabled) {
            reminderRepository.update(updated, () -> alarmScheduler.schedule(updated));
        } else {
            reminderRepository.update(updated);
        }
    }

    public void updateReminder(
            Reminder existing,
            ReminderTargetType type,
            long targetId,
            long triggerAt,
            RepeatRule repeatRule,
            boolean enabled
    ) {
        Reminder updated = new Reminder(existing.getId(), type, targetId, triggerAt, repeatRule, enabled);
        alarmScheduler.cancel(existing.getId());
        reminderRepository.update(updated, () -> {
            if (enabled) {
                alarmScheduler.schedule(updated);
            }
        });
    }

    public void delete(Reminder reminder) {
        alarmScheduler.cancel(reminder.getId());
        reminderRepository.deleteById(reminder.getId());
    }

    private List<ReminderRow> toRows(List<Reminder> reminders, List<Task> tasks, List<Routine> routines) {
        List<ReminderRow> result = new ArrayList<>();
        if (reminders == null) {
            return result;
        }
        for (Reminder reminder : reminders) {
            String title = "";
            if (reminder.getTargetType() == ReminderTargetType.TASK && tasks != null) {
                for (Task task : tasks) {
                    if (task.getId() == reminder.getTargetId()) {
                        title = task.getTitle();
                        break;
                    }
                }
            } else if (reminder.getTargetType() == ReminderTargetType.ROUTINE && routines != null) {
                for (Routine routine : routines) {
                    if (routine.getId() == reminder.getTargetId()) {
                        title = routine.getTitle();
                        break;
                    }
                }
            } else if (reminder.getTargetType() == ReminderTargetType.DAY_SUMMARY) {
                title = getApplication().getString(com.example.dailydiary.R.string.notification_summary_title);
            }
            result.add(new ReminderRow(reminder, title));
        }
        return result;
    }

    public static final class ReminderRow {
        public final Reminder reminder;
        public final String title;

        ReminderRow(Reminder reminder, String title) {
            this.reminder = reminder;
            this.title = title;
        }
    }
}
