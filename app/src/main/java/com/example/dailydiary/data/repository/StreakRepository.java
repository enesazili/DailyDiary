package com.example.dailydiary.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.dailydiary.domain.model.StreakSnapshot;
import com.example.dailydiary.domain.streak.StreakCalculator;
import com.example.dailydiary.util.LiveDataCombiner;
import com.example.dailydiary.util.LocalDates;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StreakRepository {

    private final LiveData<StreakSnapshot> snapshot;

    public StreakRepository(
            TaskRepository taskRepository,
            RoutineRepository routineRepository,
            DayLogRepository dayLogRepository
    ) {
        snapshot = Transformations.map(
                LiveDataCombiner.combine(
                        taskRepository.observeCompletedDates(),
                        routineRepository.observeCompletedDates(),
                        dayLogRepository.observeJournalDates(),
                        this::unionDates
                ),
                dates -> StreakCalculator.compute(dates, LocalDates.today())
        );
    }

    public LiveData<StreakSnapshot> observeSnapshot() {
        return snapshot;
    }

    private Set<String> unionDates(
            List<String> taskDates,
            List<String> routineDates,
            List<String> journalDates
    ) {
        Set<String> dates = new HashSet<>();
        if (taskDates != null) {
            dates.addAll(taskDates);
        }
        if (routineDates != null) {
            dates.addAll(routineDates);
        }
        if (journalDates != null) {
            dates.addAll(journalDates);
        }
        return dates;
    }
}
