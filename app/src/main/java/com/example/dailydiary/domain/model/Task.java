package com.example.dailydiary.domain.model;

import java.util.Objects;

public final class Task {

    private final long id;
    private final String title;
    private final String notes;
    private final String category;
    private final String dueDate;
    private final boolean completed;
    private final Long completedAt;
    private final int sortOrder;

    public Task(
            long id,
            String title,
            String notes,
            String category,
            String dueDate,
            boolean completed,
            Long completedAt,
            int sortOrder
    ) {
        this.id = id;
        this.title = title;
        this.notes = notes;
        this.category = category == null || category.isEmpty()
                ? TaskCategories.defaultId()
                : category;
        this.dueDate = dueDate;
        this.completed = completed;
        this.completedAt = completedAt;
        this.sortOrder = sortOrder;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNotes() {
        return notes;
    }

    public String getCategory() {
        return category;
    }

    public String getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public Task withCompleted(boolean isCompleted, Long at) {
        return new Task(id, title, notes, category, dueDate, isCompleted, at, sortOrder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Task)) {
            return false;
        }
        Task task = (Task) o;
        return id == task.id
                && completed == task.completed
                && sortOrder == task.sortOrder
                && Objects.equals(title, task.title)
                && Objects.equals(notes, task.notes)
                && Objects.equals(category, task.category)
                && Objects.equals(dueDate, task.dueDate)
                && Objects.equals(completedAt, task.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, notes, category, dueDate, completed, completedAt, sortOrder);
    }
}
