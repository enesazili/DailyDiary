package com.example.dailydiary.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "reminders",
        indices = {@Index("triggerAt"), @Index("targetType")}
)
public class ReminderEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String targetType;

    public long targetId;

    public long triggerAt;

    @NonNull
    public String repeatRule;

    public boolean isEnabled;

    public ReminderEntity(
            long id,
            @NonNull String targetType,
            long targetId,
            long triggerAt,
            @NonNull String repeatRule,
            boolean isEnabled
    ) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.triggerAt = triggerAt;
        this.repeatRule = repeatRule;
        this.isEnabled = isEnabled;
    }
}
