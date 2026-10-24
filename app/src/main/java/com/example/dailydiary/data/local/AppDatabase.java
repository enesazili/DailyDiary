package com.example.dailydiary.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.dailydiary.data.local.dao.DayLogDao;
import com.example.dailydiary.data.local.dao.ReminderDao;
import com.example.dailydiary.data.local.dao.RoutineCompletionDao;
import com.example.dailydiary.data.local.dao.RoutineDao;
import com.example.dailydiary.data.local.dao.TaskDao;
import com.example.dailydiary.data.local.entity.DayLogEntity;
import com.example.dailydiary.data.local.entity.ReminderEntity;
import com.example.dailydiary.data.local.entity.RoutineCompletionEntity;
import com.example.dailydiary.data.local.entity.RoutineEntity;
import com.example.dailydiary.data.local.entity.TaskEntity;

@Database(
        entities = {
                DayLogEntity.class,
                TaskEntity.class,
                RoutineEntity.class,
                RoutineCompletionEntity.class,
                ReminderEntity.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract DayLogDao dayLogDao();

    public abstract TaskDao taskDao();

    public abstract RoutineDao routineDao();

    public abstract RoutineCompletionDao routineCompletionDao();

    public abstract ReminderDao reminderDao();
}
