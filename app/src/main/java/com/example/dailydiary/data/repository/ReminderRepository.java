package com.example.dailydiary.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.dailydiary.data.local.dao.ReminderDao;
import com.example.dailydiary.data.local.entity.ReminderEntity;
import com.example.dailydiary.domain.model.Reminder;
import com.example.dailydiary.domain.model.ReminderTargetType;
import com.example.dailydiary.domain.model.RepeatRule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.LongConsumer;

public class ReminderRepository {

    private final ReminderDao reminderDao;
    private final Executor diskIoExecutor;

    public ReminderRepository(ReminderDao reminderDao, Executor diskIoExecutor) {
        this.reminderDao = reminderDao;
        this.diskIoExecutor = diskIoExecutor;
    }

    public LiveData<List<Reminder>> observeAll() {
        return Transformations.map(reminderDao.observeAll(), this::toDomainList);
    }

    public void insert(Reminder reminder, LongConsumer onInsertedId) {
        diskIoExecutor.execute(() -> {
            long id = reminderDao.insert(toEntity(reminder));
            if (onInsertedId != null) {
                onInsertedId.accept(id);
            }
        });
    }

    public void update(Reminder reminder) {
        diskIoExecutor.execute(() -> reminderDao.update(toEntity(reminder)));
    }

    public void update(Reminder reminder, Runnable onUpdated) {
        diskIoExecutor.execute(() -> {
            reminderDao.update(toEntity(reminder));
            if (onUpdated != null) {
                onUpdated.run();
            }
        });
    }

    public void updateSync(Reminder reminder) {
        reminderDao.update(toEntity(reminder));
    }

    public void deleteById(long id) {
        diskIoExecutor.execute(() -> reminderDao.deleteById(id));
    }

    public Reminder getByIdSync(long id) {
        return toDomain(reminderDao.getById(id));
    }

    public List<Reminder> getEnabledSync() {
        return toDomainList(reminderDao.getEnabled());
    }

    private List<Reminder> toDomainList(List<ReminderEntity> entities) {
        List<Reminder> reminders = new ArrayList<>();
        if (entities == null) {
            return reminders;
        }
        for (ReminderEntity entity : entities) {
            Reminder reminder = toDomain(entity);
            if (reminder != null) {
                reminders.add(reminder);
            }
        }
        return reminders;
    }

    private Reminder toDomain(ReminderEntity entity) {
        if (entity == null) {
            return null;
        }
        ReminderTargetType targetType;
        RepeatRule repeatRule;
        try {
            targetType = ReminderTargetType.valueOf(entity.targetType);
        } catch (IllegalArgumentException exception) {
            targetType = ReminderTargetType.TASK;
        }
        try {
            repeatRule = RepeatRule.valueOf(entity.repeatRule);
        } catch (IllegalArgumentException exception) {
            repeatRule = RepeatRule.NONE;
        }
        return new Reminder(
                entity.id,
                targetType,
                entity.targetId,
                entity.triggerAt,
                repeatRule,
                entity.isEnabled
        );
    }

    private ReminderEntity toEntity(Reminder reminder) {
        return new ReminderEntity(
                reminder.getId(),
                reminder.getTargetType().name(),
                reminder.getTargetId(),
                reminder.getTriggerAt(),
                reminder.getRepeatRule().name(),
                reminder.isEnabled()
        );
    }
}
