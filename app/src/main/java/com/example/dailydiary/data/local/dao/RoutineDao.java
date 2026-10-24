package com.example.dailydiary.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dailydiary.data.local.entity.RoutineEntity;

import java.util.List;

@Dao
public interface RoutineDao {

    @Query("SELECT * FROM routines ORDER BY id ASC")
    LiveData<List<RoutineEntity>> observeAll();

    @Query("SELECT * FROM routines WHERE isActive = 1 ORDER BY id ASC")
    LiveData<List<RoutineEntity>> observeActive();

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    RoutineEntity getById(long id);

    @Insert
    long insert(RoutineEntity entity);

    @Update
    void update(RoutineEntity entity);

    @Query("DELETE FROM routines WHERE id = :id")
    void deleteById(long id);
}
