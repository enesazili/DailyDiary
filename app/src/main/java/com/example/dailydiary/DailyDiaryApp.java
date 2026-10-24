package com.example.dailydiary;

import android.app.Application;

import com.example.dailydiary.data.settings.SettingsStore;
import com.example.dailydiary.di.AppContainer;

public class DailyDiaryApp extends Application {

    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        SettingsStore settingsStore = new SettingsStore(this);
        SettingsStore.applyTheme(settingsStore.getThemeMode());
        appContainer = new AppContainer(this);
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }
}
