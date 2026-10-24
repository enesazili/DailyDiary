package com.example.dailydiary.domain.model;

import java.util.Objects;

public final class Reminder {

    private final long id;
    private final ReminderTargetType targetType;
    private final long targetId;
    private final long triggerAt;
    private final RepeatRule repeatRule;
    private final boolean enabled;

    public Reminder(
            long id,
            ReminderTargetType targetType,
            long targetId,
            long triggerAt,
            RepeatRule repeatRule,
            boolean enabled
    ) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.triggerAt = triggerAt;
        this.repeatRule = repeatRule == null ? RepeatRule.NONE : repeatRule;
        this.enabled = enabled;
    }

    public long getId() {
        return id;
    }

    public ReminderTargetType getTargetType() {
        return targetType;
    }

    public long getTargetId() {
        return targetId;
    }

    public long getTriggerAt() {
        return triggerAt;
    }

    public RepeatRule getRepeatRule() {
        return repeatRule;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Reminder withTriggerAt(long nextTriggerAt) {
        return new Reminder(id, targetType, targetId, nextTriggerAt, repeatRule, enabled);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reminder)) {
            return false;
        }
        Reminder reminder = (Reminder) o;
        return id == reminder.id
                && targetId == reminder.targetId
                && triggerAt == reminder.triggerAt
                && enabled == reminder.enabled
                && targetType == reminder.targetType
                && repeatRule == reminder.repeatRule;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, targetType, targetId, triggerAt, repeatRule, enabled);
    }
}
