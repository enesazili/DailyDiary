package com.example.dailydiary.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.dailydiary.data.local.dao.RoutineCompletionDao;
import com.example.dailydiary.data.local.dao.RoutineDao;
import com.example.dailydiary.data.local.entity.RoutineCompletionEntity;
import com.example.dailydiary.data.local.entity.RoutineEntity;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.RoutineCompletion;
import com.example.dailydiary.domain.model.ScheduleType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.LongConsumer;

public class RoutineRepository {

    private final RoutineDao routineDao;
    private final RoutineCompletionDao completionDao;
    private final Executor diskIoExecutor;

    public RoutineRepository(
            RoutineDao routineDao,
            RoutineCompletionDao completionDao,
            Executor diskIoExecutor
    ) {
        this.routineDao = routineDao;
        this.completionDao = completionDao;
        this.diskIoExecutor = diskIoExecutor;
    }

    public LiveData<List<Routine>> observeAll() {
        return Transformations.map(routineDao.observeAll(), this::toRoutineList);
    }

    public LiveData<List<RoutineCompletion>> observeCompletions(String localDate) {
        return Transformations.map(completionDao.observeByDate(localDate), this::toCompletionList);
    }

    public LiveData<List<String>> observeCompletedDates() {
        return Transformations.map(completionDao.observeCompletedDates(), dates ->
                dates == null ? Collections.emptyList() : dates);
    }

    public LiveData<List<String>> observeCompletionDatesBetween(String start, String end) {
        return Transformations.map(completionDao.observeDatesBetween(start, end), dates ->
                dates == null ? Collections.emptyList() : dates);
    }

    public void insert(Routine routine, LongConsumer onInsertedId) {
        diskIoExecutor.execute(() -> {
            long id = routineDao.insert(toEntity(routine));
            if (onInsertedId != null) {
                onInsertedId.accept(id);
            }
        });
    }

    public void update(Routine routine) {
        diskIoExecutor.execute(() -> routineDao.update(toEntity(routine)));
    }

    public void deleteById(long id) {
        diskIoExecutor.execute(() -> {
            completionDao.deleteByRoutineId(id);
            routineDao.deleteById(id);
        });
    }

    public void toggleCompletion(long routineId, String localDate) {
        diskIoExecutor.execute(() -> {
            RoutineCompletionEntity existing = completionDao.get(routineId, localDate);
            if (existing == null) {
                completionDao.insert(new RoutineCompletionEntity(
                        0,
                        routineId,
                        localDate,
                        System.currentTimeMillis()
                ));
            } else {
                completionDao.delete(routineId, localDate);
            }
        });
    }

    public Routine getByIdSync(long id) {
        return toDomain(routineDao.getById(id));
    }

    private List<Routine> toRoutineList(List<RoutineEntity> entities) {
        List<Routine> routines = new ArrayList<>();
        if (entities == null) {
            return routines;
        }
        for (RoutineEntity entity : entities) {
            routines.add(toDomain(entity));
        }
        return routines;
    }

    private List<RoutineCompletion> toCompletionList(List<RoutineCompletionEntity> entities) {
        List<RoutineCompletion> completions = new ArrayList<>();
        if (entities == null) {
            return completions;
        }
        for (RoutineCompletionEntity entity : entities) {
            completions.add(new RoutineCompletion(
                    entity.id,
                    entity.routineId,
                    entity.localDate,
                    entity.completedAt
            ));
        }
        return completions;
    }

    private Routine toDomain(RoutineEntity entity) {
        if (entity == null) {
            return null;
        }
        ScheduleType type;
        try {
            type = ScheduleType.valueOf(entity.scheduleType);
        } catch (IllegalArgumentException exception) {
            type = ScheduleType.EVERY_DAY;
        }
        return new Routine(
                entity.id,
                entity.title,
                entity.description,
                type,
                entity.daysOfWeek,
                entity.reminderTime,
                entity.isActive
        );
    }

    private RoutineEntity toEntity(Routine routine) {
        return new RoutineEntity(
                routine.getId(),
                routine.getTitle(),
                routine.getDescription(),
                routine.getScheduleType().name(),
                routine.getDaysOfWeek(),
                routine.getReminderTime(),
                routine.isActive()
        );
    }
}
