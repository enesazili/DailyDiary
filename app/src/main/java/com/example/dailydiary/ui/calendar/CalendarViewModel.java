package com.example.dailydiary.ui.calendar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.dailydiary.DailyDiaryApp;
import com.example.dailydiary.data.repository.DayLogRepository;
import com.example.dailydiary.data.repository.RoutineRepository;
import com.example.dailydiary.data.repository.TaskRepository;
import com.example.dailydiary.di.AppContainer;
import com.example.dailydiary.domain.model.CalendarDayMarker;
import com.example.dailydiary.util.LiveDataCombiner;
import com.example.dailydiary.util.LocalDates;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalendarViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;
    private final RoutineRepository routineRepository;
    private final DayLogRepository dayLogRepository;
    private final MutableLiveData<int[]> month = new MutableLiveData<>();
    private final LiveData<List<CalendarCell>> cells;

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        AppContainer container = ((DailyDiaryApp) application).getAppContainer();
        taskRepository = container.getTaskRepository();
        routineRepository = container.getRoutineRepository();
        dayLogRepository = container.getDayLogRepository();
        Calendar now = Calendar.getInstance();
        month.setValue(new int[]{now.get(Calendar.YEAR), now.get(Calendar.MONTH)});
        LiveData<Range> range = Transformations.map(month, value -> new Range(
                LocalDates.firstDayOfMonth(value[0], value[1]),
                LocalDates.lastDayOfMonth(value[0], value[1]),
                value[0],
                value[1]
        ));
        cells = Transformations.switchMap(range, this::observeCells);
    }

    public LiveData<int[]> getMonth() {
        return month;
    }

    public LiveData<List<CalendarCell>> getCells() {
        return cells;
    }

    public void previousMonth() {
        int[] value = month.getValue();
        if (value == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, value[0]);
        calendar.set(Calendar.MONTH, value[1]);
        calendar.add(Calendar.MONTH, -1);
        month.setValue(new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)});
    }

    public void nextMonth() {
        int[] value = month.getValue();
        if (value == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, value[0]);
        calendar.set(Calendar.MONTH, value[1]);
        calendar.add(Calendar.MONTH, 1);
        month.setValue(new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)});
    }

    private LiveData<List<CalendarCell>> observeCells(Range range) {
        return LiveDataCombiner.combine(
                taskRepository.observeDueDatesBetween(range.start, range.end),
                routineRepository.observeCompletionDatesBetween(range.start, range.end),
                dayLogRepository.observeDatesBetween(range.start, range.end),
                (tasks, routines, journals) -> buildCells(range, tasks, routines, journals)
        );
    }

    private List<CalendarCell> buildCells(
            Range range,
            List<String> taskDates,
            List<String> routineDates,
            List<String> journalDates
    ) {
        Set<String> tasks = new HashSet<>(taskDates == null ? new ArrayList<>() : taskDates);
        Set<String> routines = new HashSet<>(routineDates == null ? new ArrayList<>() : routineDates);
        Set<String> journals = new HashSet<>(journalDates == null ? new ArrayList<>() : journalDates);
        List<CalendarCell> result = new ArrayList<>();
        Calendar cursor = LocalDates.parse(range.start);
        if (cursor == null) {
            return result;
        }
        int leading = LocalDates.isoDayOfWeek(range.start) - 1;
        cursor.add(Calendar.DAY_OF_MONTH, -leading);
        for (int i = 0; i < 42; i++) {
            String date = LocalDates.format(cursor);
            boolean inMonth = date.compareTo(range.start) >= 0 && date.compareTo(range.end) <= 0;
            CalendarDayMarker marker = new CalendarDayMarker(
                    date,
                    tasks.contains(date),
                    routines.contains(date),
                    journals.contains(date)
            );
            result.add(new CalendarCell(date, inMonth, date.equals(LocalDates.today()), marker));
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
        return result;
    }

    private static final class Range {
        final String start;
        final String end;
        final int year;
        final int month;

        Range(String start, String end, int year, int month) {
            this.start = start;
            this.end = end;
            this.year = year;
            this.month = month;
        }
    }

    public static final class CalendarCell {
        public final String localDate;
        public final boolean inMonth;
        public final boolean today;
        public final CalendarDayMarker marker;

        CalendarCell(String localDate, boolean inMonth, boolean today, CalendarDayMarker marker) {
            this.localDate = localDate;
            this.inMonth = inMonth;
            this.today = today;
            this.marker = marker;
        }
    }
}
