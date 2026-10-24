package com.example.dailydiary.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.dailydiary.data.local.entity.RoutineCompletionEntity;

import java.util.List;

@Dao
public interface RoutineCompletionDao {

    @Query("SELECT * FROM routine_completions WHERE localDate = :localDate")
    LiveData<List<RoutineCompletionEntity>> observeByDate(String localDate);

    @Query("SELECT * FROM routine_completions WHERE routineId = :routineId AND localDate = :localDate LIMIT 1")
    RoutineCompletionEntity get(long routineId, String localDate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RoutineCompletionEntity entity);

    @Query("DELETE FROM routine_completions WHERE routineId = :routineId AND localDate = :localDate")
    void delete(long routineId, String localDate);

    @Query("DELETE FROM routine_completions WHERE routineId = :routineId")
    void deleteByRoutineId(long routineId);

    @Query("SELECT DISTINCT localDate FROM routine_completions")
    LiveData<List<String>> observeCompletedDates();

    @Query("SELECT DISTINCT localDate FROM routine_completions WHERE localDate BETWEEN :start AND :end")
    LiveData<List<String>> observeDatesBetween(String start, String end);
}
