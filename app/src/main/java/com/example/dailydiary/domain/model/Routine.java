package com.example.dailydiary.domain.model;

import com.example.dailydiary.util.DaysOfWeek;
import com.example.dailydiary.util.LocalDates;

import java.util.Objects;
import java.util.Set;

public final class Routine {

    private final long id;
    private final String title;
    private final String description;
    private final ScheduleType scheduleType;
    private final String daysOfWeek;
    private final String reminderTime;
    private final boolean active;

    public Routine(
            long id,
            String title,
            String description,
            ScheduleType scheduleType,
            String daysOfWeek,
            String reminderTime,
            boolean active
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.scheduleType = scheduleType == null ? ScheduleType.EVERY_DAY : scheduleType;
        this.daysOfWeek = daysOfWeek;
        this.reminderTime = reminderTime;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public boolean isActive() {
        return active;
    }

    public boolean appliesOn(String localDate) {
        return active && isScheduledOn(localDate);
    }

    public boolean isScheduledOn(String localDate) {
        switch (scheduleType) {
            case EVERY_DAY:
                return true;
            case WEEKDAYS:
                return LocalDates.isWeekday(localDate);
            case CUSTOM:
                Set<Integer> days = DaysOfWeek.parse(daysOfWeek);
                return days.contains(LocalDates.isoDayOfWeek(localDate));
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Routine)) {
            return false;
        }
        Routine routine = (Routine) o;
        return id == routine.id
                && active == routine.active
                && Objects.equals(title, routine.title)
                && Objects.equals(description, routine.description)
                && scheduleType == routine.scheduleType
                && Objects.equals(daysOfWeek, routine.daysOfWeek)
                && Objects.equals(reminderTime, routine.reminderTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, scheduleType, daysOfWeek, reminderTime, active);
    }
}
