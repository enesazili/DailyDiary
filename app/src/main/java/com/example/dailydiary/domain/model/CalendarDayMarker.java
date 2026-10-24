package com.example.dailydiary.domain.model;

public final class CalendarDayMarker {

    private final String localDate;
    private final boolean hasTask;
    private final boolean hasRoutine;
    private final boolean hasJournal;

    public CalendarDayMarker(
            String localDate,
            boolean hasTask,
            boolean hasRoutine,
            boolean hasJournal
    ) {
        this.localDate = localDate;
        this.hasTask = hasTask;
        this.hasRoutine = hasRoutine;
        this.hasJournal = hasJournal;
    }

    public String getLocalDate() {
        return localDate;
    }

    public boolean hasTask() {
        return hasTask;
    }

    public boolean hasRoutine() {
        return hasRoutine;
    }

    public boolean hasJournal() {
        return hasJournal;
    }
}
