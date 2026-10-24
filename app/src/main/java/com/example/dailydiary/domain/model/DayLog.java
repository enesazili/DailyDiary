package com.example.dailydiary.domain.model;

public final class DayLog {

    private final String localDate;
    private final String freeText;
    private final String summaryText;
    private final Long summaryGeneratedAt;

    public DayLog(
            String localDate,
            String freeText,
            String summaryText,
            Long summaryGeneratedAt
    ) {
        this.localDate = localDate;
        this.freeText = freeText;
        this.summaryText = summaryText;
        this.summaryGeneratedAt = summaryGeneratedAt;
    }

    public String getLocalDate() {
        return localDate;
    }

    public String getFreeText() {
        return freeText;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public Long getSummaryGeneratedAt() {
        return summaryGeneratedAt;
    }

    public boolean hasJournalText() {
        return freeText != null && !freeText.trim().isEmpty();
    }
}
