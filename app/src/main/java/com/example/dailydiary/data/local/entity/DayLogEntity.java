package com.example.dailydiary.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "day_logs")
public class DayLogEntity {

    @PrimaryKey
    @NonNull
    public String localDate;

    @Nullable
    public String freeText;

    @Nullable
    public String summaryText;

    @Nullable
    public Long summaryGeneratedAt;

    public DayLogEntity(
            @NonNull String localDate,
            @Nullable String freeText,
            @Nullable String summaryText,
            @Nullable Long summaryGeneratedAt
    ) {
        this.localDate = localDate;
        this.freeText = freeText;
        this.summaryText = summaryText;
        this.summaryGeneratedAt = summaryGeneratedAt;
    }
}
