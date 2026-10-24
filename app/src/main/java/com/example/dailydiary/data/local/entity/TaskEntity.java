package com.example.dailydiary.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tasks",
        indices = {@Index("dueDate"), @Index("category")}
)
public class TaskEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title;

    @Nullable
    public String notes;

    @NonNull
    public String category;

    @NonNull
    public String dueDate;

    public boolean isCompleted;

    @Nullable
    public Long completedAt;

    public int sortOrder;

    public TaskEntity(
            long id,
            @NonNull String title,
            @Nullable String notes,
            @NonNull String category,
            @NonNull String dueDate,
            boolean isCompleted,
            @Nullable Long completedAt,
            int sortOrder
    ) {
        this.id = id;
        this.title = title;
        this.notes = notes;
        this.category = category;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
        this.sortOrder = sortOrder;
    }
}
