package com.example.dailydiary.domain.streak;

import com.example.dailydiary.domain.model.StreakSnapshot;
import com.example.dailydiary.util.LocalDates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class StreakCalculator {

    private StreakCalculator() {
    }

    public static StreakSnapshot compute(Set<String> activityDates, String today) {
        Set<String> dates = activityDates == null ? new HashSet<>() : new HashSet<>(activityDates);
        dates.remove(null);
        if (dates.isEmpty()) {
            return StreakSnapshot.empty();
        }

        String startDate = dates.contains(today) ? today : LocalDates.yesterday();
        int current = 0;
        String cursor = startDate;
        String lastQualified = null;
        while (dates.contains(cursor)) {
            current++;
            lastQualified = cursor;
            cursor = LocalDates.plusDays(cursor, -1);
        }
        if (!dates.contains(today) && current == 0) {
            lastQualified = null;
        }

        int best = longestRun(dates);
        return new StreakSnapshot(current, Math.max(best, current), lastQualified);
    }

    private static int longestRun(Set<String> dates) {
        List<String> sorted = new ArrayList<>(dates);
        Collections.sort(sorted);
        int best = 1;
        int run = 1;
        for (int i = 1; i < sorted.size(); i++) {
            if (LocalDates.plusDays(sorted.get(i - 1), 1).equals(sorted.get(i))) {
                run++;
                best = Math.max(best, run);
            } else {
                run = 1;
            }
        }
        return sorted.isEmpty() ? 0 : best;
    }
}
