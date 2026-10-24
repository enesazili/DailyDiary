package com.example.dailydiary.domain.model;

public final class RoutineCompletion {

    private final long id;
    private final long routineId;
    private final String localDate;
    private final long completedAt;

    public RoutineCompletion(long id, long routineId, String localDate, long completedAt) {
        this.id = id;
        this.routineId = routineId;
        this.localDate = localDate;
        this.completedAt = completedAt;
    }

    public long getId() {
        return id;
    }

    public long getRoutineId() {
        return routineId;
    }

    public String getLocalDate() {
        return localDate;
    }

    public long getCompletedAt() {
        return completedAt;
    }
}
