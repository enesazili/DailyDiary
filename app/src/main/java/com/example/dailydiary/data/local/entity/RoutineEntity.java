package com.example.dailydiary.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "routines")
public class RoutineEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title;

    @Nullable
    public String description;

    @NonNull
    public String scheduleType;

    @Nullable
    public String daysOfWeek;

    @Nullable
    public String reminderTime;

    public boolean isActive;

    public RoutineEntity(
            long id,
            @NonNull String title,
            @Nullable String description,
            @NonNull String scheduleType,
            @Nullable String daysOfWeek,
            @Nullable String reminderTime,
            boolean isActive
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.scheduleType = scheduleType;
        this.daysOfWeek = daysOfWeek;
        this.reminderTime = reminderTime;
        this.isActive = isActive;
    }
}
