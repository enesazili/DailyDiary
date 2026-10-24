package com.example.dailydiary.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "routine_completions",
        indices = {@Index(value = {"routineId", "localDate"}, unique = true), @Index("localDate")}
)
public class RoutineCompletionEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long routineId;

    @NonNull
    public String localDate;

    public long completedAt;

    public RoutineCompletionEntity(long id, long routineId, @NonNull String localDate, long completedAt) {
        this.id = id;
        this.routineId = routineId;
        this.localDate = localDate;
        this.completedAt = completedAt;
    }
}
