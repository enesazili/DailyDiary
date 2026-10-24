package com.example.dailydiary.data.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class SettingsStore {

    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    private static final String PREFS = "daily_diary_settings";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_SUMMARY_HOUR = "day_end_hour";
    private static final String KEY_SUMMARY_MINUTE = "day_end_minute";
    private static final String KEY_THEME = "theme_mode";

    private final SharedPreferences preferences;

    public SettingsStore(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean areNotificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public int getDayEndHour() {
        return preferences.getInt(KEY_SUMMARY_HOUR, 21);
    }

    public int getDayEndMinute() {
        return preferences.getInt(KEY_SUMMARY_MINUTE, 0);
    }

    public void setDayEndTime(int hour, int minute) {
        preferences.edit()
                .putInt(KEY_SUMMARY_HOUR, hour)
                .putInt(KEY_SUMMARY_MINUTE, minute)
                .apply();
    }

    public String getThemeMode() {
        return preferences.getString(KEY_THEME, THEME_SYSTEM);
    }

    public void setThemeMode(String mode) {
        preferences.edit().putString(KEY_THEME, mode).apply();
        applyTheme(mode);
    }

    public static void applyTheme(String mode) {
        int nightMode;
        if (THEME_LIGHT.equals(mode)) {
            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (THEME_DARK.equals(mode)) {
            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}
