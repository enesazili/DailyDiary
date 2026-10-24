package com.example.dailydiary.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.dailydiary.data.local.dao.TaskDao;
import com.example.dailydiary.data.local.entity.TaskEntity;
import com.example.dailydiary.domain.model.Task;
import com.example.dailydiary.domain.model.TaskCategories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.LongConsumer;

public class TaskRepository {

    private final TaskDao taskDao;
    private final Executor diskIoExecutor;

    public TaskRepository(TaskDao taskDao, Executor diskIoExecutor) {
        this.taskDao = taskDao;
        this.diskIoExecutor = diskIoExecutor;
    }

    public LiveData<List<Task>> observeByDueDate(String dueDate) {
        return Transformations.map(taskDao.observeByDueDate(dueDate), this::toDomainList);
    }

    public LiveData<List<Task>> observeAll() {
        return Transformations.map(taskDao.observeAll(), this::toDomainList);
    }

    public LiveData<List<String>> observeCompletedDates() {
        return Transformations.map(taskDao.observeCompletedDates(), dates ->
                dates == null ? Collections.emptyList() : dates);
    }

    public LiveData<List<String>> observeDueDatesBetween(String start, String end) {
        return Transformations.map(taskDao.observeDueDatesBetween(start, end), dates ->
                dates == null ? Collections.emptyList() : dates);
    }

    public void insert(Task task) {
        insert(task, null);
    }

    public void insert(Task task, LongConsumer onInsertedId) {
        diskIoExecutor.execute(() -> {
            long id = taskDao.insert(toEntity(task));
            if (onInsertedId != null) {
                onInsertedId.accept(id);
            }
        });
    }

    public void update(Task task) {
        diskIoExecutor.execute(() -> taskDao.update(toEntity(task)));
    }

    public void deleteById(long id) {
        diskIoExecutor.execute(() -> taskDao.deleteById(id));
    }

    public Task getByIdSync(long id) {
        return toDomain(taskDao.getById(id));
    }

    private List<Task> toDomainList(List<TaskEntity> entities) {
        List<Task> tasks = new ArrayList<>();
        if (entities == null) {
            return tasks;
        }
        for (TaskEntity entity : entities) {
            tasks.add(toDomain(entity));
        }
        return tasks;
    }

    private Task toDomain(TaskEntity entity) {
        if (entity == null) {
            return null;
        }
        String category = entity.category == null ? TaskCategories.defaultId() : entity.category;
        return new Task(
                entity.id,
                entity.title,
                entity.notes,
                category,
                entity.dueDate,
                entity.isCompleted,
                entity.completedAt,
                entity.sortOrder
        );
    }

    private TaskEntity toEntity(Task task) {
        return new TaskEntity(
                task.getId(),
                task.getTitle(),
                task.getNotes(),
                task.getCategory(),
                task.getDueDate(),
                task.isCompleted(),
                task.getCompletedAt(),
                task.getSortOrder()
        );
    }
}
