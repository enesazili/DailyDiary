package com.example.dailydiary.data.local;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public final class MIGRATION_1_2 extends Migration {

    public static final MIGRATION_1_2 INSTANCE = new MIGRATION_1_2();

    private MIGRATION_1_2() {
        super(1, 2);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS day_logs_new ("
                        + "localDate TEXT NOT NULL PRIMARY KEY, "
                        + "freeText TEXT, "
                        + "summaryText TEXT, "
                        + "summaryGeneratedAt INTEGER)"
        );
        db.execSQL(
                "INSERT INTO day_logs_new (localDate, freeText, summaryText, summaryGeneratedAt) "
                        + "SELECT localDate, freeText, summaryText, summaryGeneratedAt FROM day_logs"
        );
        db.execSQL("DROP TABLE day_logs");
        db.execSQL("ALTER TABLE day_logs_new RENAME TO day_logs");

        db.execSQL("ALTER TABLE tasks ADD COLUMN category TEXT NOT NULL DEFAULT 'KISISEL'");

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS routines ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                        + "title TEXT NOT NULL, "
                        + "description TEXT, "
                        + "scheduleType TEXT NOT NULL, "
                        + "daysOfWeek TEXT, "
                        + "reminderTime TEXT, "
                        + "isActive INTEGER NOT NULL)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS routine_completions ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                        + "routineId INTEGER NOT NULL, "
                        + "localDate TEXT NOT NULL, "
                        + "completedAt INTEGER NOT NULL)"
        );
        db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_routine_completions_routineId_localDate "
                        + "ON routine_completions(routineId, localDate)"
        );
        db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_routine_completions_localDate "
                        + "ON routine_completions(localDate)"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS reminders ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                        + "targetType TEXT NOT NULL, "
                        + "targetId INTEGER NOT NULL, "
                        + "triggerAt INTEGER NOT NULL, "
                        + "repeatRule TEXT NOT NULL, "
                        + "isEnabled INTEGER NOT NULL)"
        );
        db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_triggerAt ON reminders(triggerAt)");
        db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_targetType ON reminders(targetType)");
        db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_category ON tasks(category)");
    }
}
