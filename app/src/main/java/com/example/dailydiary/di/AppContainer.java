package com.example.dailydiary.di;

import android.content.Context;

import androidx.room.Room;

import com.example.dailydiary.data.local.AppDatabase;
import com.example.dailydiary.data.local.MIGRATION_1_2;
import com.example.dailydiary.data.repository.DayLogRepository;
import com.example.dailydiary.data.repository.ReminderRepository;
import com.example.dailydiary.data.repository.RoutineRepository;
import com.example.dailydiary.data.repository.StreakRepository;
import com.example.dailydiary.data.repository.TaskRepository;
import com.example.dailydiary.data.settings.SettingsStore;
import com.example.dailydiary.notification.AlarmScheduler;
import com.example.dailydiary.notification.NotificationHelper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppContainer {

    private final Executor diskIoExecutor = Executors.newSingleThreadExecutor();
    private final DayLogRepository dayLogRepository;
    private final TaskRepository taskRepository;
    private final RoutineRepository routineRepository;
    private final ReminderRepository reminderRepository;
    private final StreakRepository streakRepository;
    private final SettingsStore settingsStore;
    private final NotificationHelper notificationHelper;
    private final AlarmScheduler alarmScheduler;

    public AppContainer(Context context) {
        Context appContext = context.getApplicationContext();
        AppDatabase database = Room.databaseBuilder(
                        appContext,
                        AppDatabase.class,
                        "daily_diary.db"
                )
                .addMigrations(MIGRATION_1_2.INSTANCE)
                .build();
        dayLogRepository = new DayLogRepository(database.dayLogDao(), diskIoExecutor);
        taskRepository = new TaskRepository(database.taskDao(), diskIoExecutor);
        routineRepository = new RoutineRepository(
                database.routineDao(),
                database.routineCompletionDao(),
                diskIoExecutor
        );
        reminderRepository = new ReminderRepository(database.reminderDao(), diskIoExecutor);
        streakRepository = new StreakRepository(taskRepository, routineRepository, dayLogRepository);
        settingsStore = new SettingsStore(appContext);
        notificationHelper = new NotificationHelper(appContext);
        alarmScheduler = new AlarmScheduler(appContext, reminderRepository);
        diskIoExecutor.execute(alarmScheduler::rescheduleEnabled);
    }

    public Executor getDiskIoExecutor() {
        return diskIoExecutor;
    }

    public DayLogRepository getDayLogRepository() {
        return dayLogRepository;
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public RoutineRepository getRoutineRepository() {
        return routineRepository;
    }

    public ReminderRepository getReminderRepository() {
        return reminderRepository;
    }

    public StreakRepository getStreakRepository() {
        return streakRepository;
    }

    public SettingsStore getSettingsStore() {
        return settingsStore;
    }

    public NotificationHelper getNotificationHelper() {
        return notificationHelper;
    }

    public AlarmScheduler getAlarmScheduler() {
        return alarmScheduler;
    }
}
