package com.example.dailydiary.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.dailydiary.MainActivity;
import com.example.dailydiary.R;

public class NotificationHelper {

    public static final String CHANNEL_ID = "daily_diary_reminders";

    private final Context appContext;

    public NotificationHelper(Context context) {
        this.appContext = context.getApplicationContext();
        createChannel();
    }

    public void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    appContext.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(appContext.getString(R.string.notification_channel_description));
            NotificationManager manager = appContext.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void showReminder(long reminderId, String title, String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(appContext, android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (!NotificationManagerCompat.from(appContext).areNotificationsEnabled()) {
            return;
        }
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                appContext,
                (int) reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat.from(appContext).notify((int) reminderId, builder.build());
    }
}
