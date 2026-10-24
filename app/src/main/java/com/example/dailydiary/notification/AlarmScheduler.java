package com.example.dailydiary.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.dailydiary.data.repository.ReminderRepository;
import com.example.dailydiary.domain.model.Reminder;
import com.example.dailydiary.domain.model.RepeatRule;
import com.example.dailydiary.util.LocalDates;

import java.util.Calendar;
import java.util.List;

public class AlarmScheduler {

    public static final String ACTION_REMINDER = "com.example.dailydiary.action.REMINDER";
    public static final String EXTRA_REMINDER_ID = "reminder_id";

    private final Context appContext;
    private final ReminderRepository reminderRepository;

    public AlarmScheduler(Context context, ReminderRepository reminderRepository) {
        this.appContext = context.getApplicationContext();
        this.reminderRepository = reminderRepository;
    }

    public void schedule(Reminder reminder) {
        if (reminder == null || !reminder.isEnabled()) {
            return;
        }
        long triggerAt = nextTrigger(reminder.getTriggerAt(), reminder.getRepeatRule());
        if (triggerAt != reminder.getTriggerAt() && reminder.getId() > 0) {
            reminderRepository.updateSync(reminder.withTriggerAt(triggerAt));
        }
        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        PendingIntent pendingIntent = pendingIntent(reminder.getId());
        alarmManager.cancel(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    public void cancel(long reminderId) {
        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent(reminderId));
        }
    }

    public void rescheduleEnabled() {
        List<Reminder> reminders = reminderRepository.getEnabledSync();
        for (Reminder reminder : reminders) {
            schedule(reminder);
        }
    }

    public static long nextTrigger(long triggerAt, RepeatRule rule) {
        long now = System.currentTimeMillis();
        if (triggerAt > now) {
            return triggerAt;
        }
        if (rule == null || rule == RepeatRule.NONE) {
            return triggerAt;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(triggerAt);
        while (calendar.getTimeInMillis() <= now) {
            if (rule == RepeatRule.DAILY) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            } else if (rule == RepeatRule.WEEKLY) {
                calendar.add(Calendar.DAY_OF_MONTH, 7);
            } else if (rule == RepeatRule.WEEKDAYS) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                String date = LocalDates.format(calendar);
                if (!LocalDates.isWeekday(date)) {
                    continue;
                }
            } else {
                break;
            }
        }
        return calendar.getTimeInMillis();
    }

    private PendingIntent pendingIntent(long reminderId) {
        Intent intent = new Intent(appContext, ReminderReceiver.class);
        intent.setAction(ACTION_REMINDER);
        intent.putExtra(EXTRA_REMINDER_ID, reminderId);
        return PendingIntent.getBroadcast(
                appContext,
                (int) reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
