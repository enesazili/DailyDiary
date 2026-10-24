package com.example.dailydiary.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.dailydiary.DailyDiaryApp;
import com.example.dailydiary.R;
import com.example.dailydiary.data.repository.ReminderRepository;
import com.example.dailydiary.data.settings.SettingsStore;
import com.example.dailydiary.di.AppContainer;
import com.example.dailydiary.domain.model.Reminder;
import com.example.dailydiary.domain.model.ReminderTargetType;
import com.example.dailydiary.domain.model.RepeatRule;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.Task;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !AlarmScheduler.ACTION_REMINDER.equals(intent.getAction())) {
            return;
        }
        long reminderId = intent.getLongExtra(AlarmScheduler.EXTRA_REMINDER_ID, -1);
        if (reminderId < 0) {
            return;
        }
        final PendingResult pendingResult = goAsync();
        AppContainer container = ((DailyDiaryApp) context.getApplicationContext()).getAppContainer();
        Context appContext = context.getApplicationContext();
        container.getDiskIoExecutor().execute(() -> {
            try {
                SettingsStore settings = container.getSettingsStore();
                if (!settings.areNotificationsEnabled()) {
                    return;
                }
                ReminderRepository reminderRepository = container.getReminderRepository();
                Reminder reminder = reminderRepository.getByIdSync(reminderId);
                if (reminder == null || !reminder.isEnabled()) {
                    return;
                }
                String title = appContext.getString(R.string.notification_default_title);
                String body = appContext.getString(R.string.notification_default_body);
                if (reminder.getTargetType() == ReminderTargetType.TASK) {
                    Task task = container.getTaskRepository().getByIdSync(reminder.getTargetId());
                    if (task != null) {
                        title = task.getTitle();
                        body = appContext.getString(R.string.notification_task_body);
                    }
                } else if (reminder.getTargetType() == ReminderTargetType.ROUTINE) {
                    Routine routine = container.getRoutineRepository().getByIdSync(reminder.getTargetId());
                    if (routine != null) {
                        title = routine.getTitle();
                        body = appContext.getString(R.string.notification_routine_body);
                    }
                } else if (reminder.getTargetType() == ReminderTargetType.DAY_SUMMARY) {
                    title = appContext.getString(R.string.notification_summary_title);
                    body = appContext.getString(R.string.notification_summary_body);
                }
                container.getNotificationHelper().showReminder(reminderId, title, body);
                if (reminder.getRepeatRule() != RepeatRule.NONE) {
                    long next = AlarmScheduler.nextTrigger(
                            reminder.getTriggerAt() + 60_000L,
                            reminder.getRepeatRule()
                    );
                    Reminder updated = reminder.withTriggerAt(next);
                    reminderRepository.updateSync(updated);
                    container.getAlarmScheduler().schedule(updated);
                }
            } finally {
                pendingResult.finish();
            }
        });
    }
}
