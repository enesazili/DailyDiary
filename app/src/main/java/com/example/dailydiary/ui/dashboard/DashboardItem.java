package com.example.dailydiary.ui.dashboard;

import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.Task;

public abstract class DashboardItem {

    public static final int HEADER = 0;
    public static final int TASK = 1;
    public static final int ROUTINE = 2;

    public abstract int getType();

    public abstract String getKey();

    public static final class Header extends DashboardItem {
        public final String title;

        public Header(String title) {
            this.title = title;
        }

        @Override
        public int getType() {
            return HEADER;
        }

        @Override
        public String getKey() {
            return "h-" + title;
        }
    }

    public static final class TaskRow extends DashboardItem {
        public final Task task;

        public TaskRow(Task task) {
            this.task = task;
        }

        @Override
        public int getType() {
            return TASK;
        }

        @Override
        public String getKey() {
            return "t-" + task.getId();
        }
    }

    public static final class RoutineRow extends DashboardItem {
        public final Routine routine;
        public final boolean completed;

        public RoutineRow(Routine routine, boolean completed) {
            this.routine = routine;
            this.completed = completed;
        }

        @Override
        public int getType() {
            return ROUTINE;
        }

        @Override
        public String getKey() {
            return "r-" + routine.getId() + "-" + completed;
        }
    }
}
