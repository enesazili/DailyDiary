package com.example.dailydiary.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.dailydiary.DailyDiaryApp;
import com.example.dailydiary.di.AppContainer;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        final PendingResult pendingResult = goAsync();
        AppContainer container = ((DailyDiaryApp) context.getApplicationContext()).getAppContainer();
        container.getDiskIoExecutor().execute(() -> {
            try {
                container.getAlarmScheduler().rescheduleEnabled();
            } finally {
                pendingResult.finish();
            }
        });
    }
}
