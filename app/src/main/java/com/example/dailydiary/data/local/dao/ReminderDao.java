package com.example.dailydiary.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dailydiary.data.local.entity.ReminderEntity;

import java.util.List;

@Dao
public interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY triggerAt ASC")
    LiveData<List<ReminderEntity>> observeAll();

    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    List<ReminderEntity> getEnabled();

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    ReminderEntity getById(long id);

    @Insert
    long insert(ReminderEntity entity);

    @Update
    void update(ReminderEntity entity);

    @Query("DELETE FROM reminders WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM reminders WHERE targetType = :targetType AND targetId = :targetId LIMIT 1")
    ReminderEntity getByTarget(String targetType, long targetId);
}
