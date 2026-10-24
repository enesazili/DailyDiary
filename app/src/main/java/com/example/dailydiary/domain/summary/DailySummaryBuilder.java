package com.example.dailydiary.domain.summary;

import android.content.Context;

import com.example.dailydiary.R;
import com.example.dailydiary.domain.model.StreakSnapshot;

public final class DailySummaryBuilder {

    private DailySummaryBuilder() {
    }

    public static String build(
            Context context,
            int completedTasks,
            int totalTasks,
            int completedRoutines,
            int totalRoutines,
            StreakSnapshot streak
    ) {
        int streakCount = streak == null ? 0 : streak.getCurrentCount();
        return context.getString(
                R.string.auto_summary_body,
                totalTasks,
                completedTasks,
                totalRoutines,
                completedRoutines,
                streakCount
        );
    }
}
