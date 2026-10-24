package com.example.dailydiary.domain.model;

public final class StreakSnapshot {

    private final int currentCount;
    private final int bestCount;
    private final String lastQualifiedDate;

    public StreakSnapshot(int currentCount, int bestCount, String lastQualifiedDate) {
        this.currentCount = currentCount;
        this.bestCount = bestCount;
        this.lastQualifiedDate = lastQualifiedDate;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getBestCount() {
        return bestCount;
    }

    public String getLastQualifiedDate() {
        return lastQualifiedDate;
    }

    public static StreakSnapshot empty() {
        return new StreakSnapshot(0, 0, null);
    }
}
