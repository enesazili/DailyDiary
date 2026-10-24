package com.example.dailydiary.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.dailydiary.data.local.entity.DayLogEntity;

import java.util.List;

@Dao
public interface DayLogDao {

    @Query("SELECT * FROM day_logs WHERE localDate = :localDate LIMIT 1")
    LiveData<DayLogEntity> observeByLocalDate(String localDate);

    @Query("SELECT * FROM day_logs WHERE localDate = :localDate LIMIT 1")
    DayLogEntity getByLocalDate(String localDate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(DayLogEntity entity);

    @Query("DELETE FROM day_logs WHERE localDate = :localDate")
    void deleteByLocalDate(String localDate);

    @Query("SELECT localDate FROM day_logs WHERE freeText IS NOT NULL AND TRIM(freeText) != ''")
    LiveData<List<String>> observeJournalDates();

    @Query("SELECT localDate FROM day_logs WHERE localDate BETWEEN :start AND :end")
    LiveData<List<String>> observeDatesBetween(String start, String end);
}
