package com.example.dailydiary.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dailydiary.data.local.entity.TaskEntity;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks WHERE dueDate = :dueDate ORDER BY sortOrder ASC, id ASC")
    LiveData<List<TaskEntity>> observeByDueDate(String dueDate);

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, sortOrder ASC, id ASC")
    LiveData<List<TaskEntity>> observeAll();

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    TaskEntity getById(long id);

    @Insert
    long insert(TaskEntity entity);

    @Update
    void update(TaskEntity entity);

    @Query("DELETE FROM tasks WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT DISTINCT dueDate FROM tasks WHERE isCompleted = 1")
    LiveData<List<String>> observeCompletedDates();

    @Query("SELECT DISTINCT dueDate FROM tasks WHERE dueDate BETWEEN :start AND :end")
    LiveData<List<String>> observeDueDatesBetween(String start, String end);
}
