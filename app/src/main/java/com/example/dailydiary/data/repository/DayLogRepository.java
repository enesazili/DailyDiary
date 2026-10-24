package com.example.dailydiary.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.dailydiary.data.local.dao.DayLogDao;
import com.example.dailydiary.data.local.entity.DayLogEntity;
import com.example.dailydiary.domain.model.DayLog;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class DayLogRepository {

    private final DayLogDao dayLogDao;
    private final Executor diskIoExecutor;

    public DayLogRepository(DayLogDao dayLogDao, Executor diskIoExecutor) {
        this.dayLogDao = dayLogDao;
        this.diskIoExecutor = diskIoExecutor;
    }

    public LiveData<DayLog> observeByLocalDate(String localDate) {
        return Transformations.map(dayLogDao.observeByLocalDate(localDate), this::toDomain);
    }

    public LiveData<List<String>> observeJournalDates() {
        return Transformations.map(dayLogDao.observeJournalDates(), dates ->
                dates == null ? Collections.emptyList() : dates);
    }

    public LiveData<List<String>> observeDatesBetween(String start, String end) {
        return Transformations.map(dayLogDao.observeDatesBetween(start, end), dates ->
                dates == null ? Collections.emptyList() : dates);
    }

    public void save(DayLog dayLog) {
        diskIoExecutor.execute(() -> dayLogDao.upsert(toEntity(dayLog)));
    }

    public void saveFreeText(String localDate, String freeText) {
        diskIoExecutor.execute(() -> {
            DayLogEntity current = dayLogDao.getByLocalDate(localDate);
            String summary = current == null ? null : current.summaryText;
            Long generatedAt = current == null ? null : current.summaryGeneratedAt;
            if ((freeText == null || freeText.trim().isEmpty()) && summary == null) {
                dayLogDao.deleteByLocalDate(localDate);
                return;
            }
            dayLogDao.upsert(new DayLogEntity(localDate, freeText, summary, generatedAt));
        });
    }

    public void saveCloseDay(String localDate, String freeText, String summaryText) {
        diskIoExecutor.execute(() -> dayLogDao.upsert(new DayLogEntity(
                localDate,
                freeText,
                summaryText,
                System.currentTimeMillis()
        )));
    }

    private DayLog toDomain(DayLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return new DayLog(
                entity.localDate,
                entity.freeText,
                entity.summaryText,
                entity.summaryGeneratedAt
        );
    }

    private DayLogEntity toEntity(DayLog dayLog) {
        return new DayLogEntity(
                dayLog.getLocalDate(),
                dayLog.getFreeText(),
                dayLog.getSummaryText(),
                dayLog.getSummaryGeneratedAt()
        );
    }
}
