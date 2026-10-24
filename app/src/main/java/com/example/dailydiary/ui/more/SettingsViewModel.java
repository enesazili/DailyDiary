package com.example.dailydiary.ui.more;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.dailydiary.DailyDiaryApp;
import com.example.dailydiary.data.repository.ReminderRepository;
import com.example.dailydiary.data.settings.SettingsStore;
import com.example.dailydiary.di.AppContainer;
import com.example.dailydiary.domain.model.Reminder;
import com.example.dailydiary.domain.model.ReminderTargetType;
import com.example.dailydiary.domain.model.RepeatRule;
import com.example.dailydiary.notification.AlarmScheduler;
import com.example.dailydiary.util.LocalDates;

public class SettingsViewModel extends AndroidViewModel {

    private final SettingsStore settingsStore;
    private final ReminderRepository reminderRepository;
    private final AlarmScheduler alarmScheduler;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        AppContainer container = ((DailyDiaryApp) application).getAppContainer();
        settingsStore = container.getSettingsStore();
        reminderRepository = container.getReminderRepository();
        alarmScheduler = container.getAlarmScheduler();
    }

    public boolean areNotificationsEnabled() {
        return settingsStore.areNotificationsEnabled();
    }

    public void setNotificationsEnabled(boolean enabled) {
        settingsStore.setNotificationsEnabled(enabled);
        if (!enabled) {
            return;
        }
        scheduleDaySummary();
    }

    public int getDayEndHour() {
        return settingsStore.getDayEndHour();
    }

    public int getDayEndMinute() {
        return settingsStore.getDayEndMinute();
    }

    public void setDayEndTime(int hour, int minute) {
        settingsStore.setDayEndTime(hour, minute);
        scheduleDaySummary();
    }

    public String getThemeMode() {
        return settingsStore.getThemeMode();
    }

    public void setThemeMode(String mode) {
        settingsStore.setThemeMode(mode);
    }

    public void scheduleDaySummary() {
        if (!settingsStore.areNotificationsEnabled()) {
            return;
        }
        long trigger = LocalDates.atTimeMillis(
                LocalDates.today(),
                settingsStore.getDayEndHour(),
                settingsStore.getDayEndMinute()
        );
        Reminder reminder = new Reminder(
                0,
                ReminderTargetType.DAY_SUMMARY,
                0,
                trigger,
                RepeatRule.DAILY,
                true
        );
        reminderRepository.insert(reminder, id ->
                alarmScheduler.schedule(new Reminder(
                        id,
                        ReminderTargetType.DAY_SUMMARY,
                        0,
                        trigger,
                        RepeatRule.DAILY,
                        true
                )));
    }
}
