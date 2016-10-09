package com.mahnke.todolist;

import java.util.Calendar;

public class TodoItem {
    private Calendar dueDate;
    private String title;
    private boolean notify;

    public TodoItem(String title, boolean notify) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        this.dueDate = tomorrow;
        this.title = title;
        this.notify = notify;
    }

    public TodoItem(Calendar dueDate, String title, boolean notify) {
        this.dueDate = dueDate;
        this.title = title;
        this.notify = notify;
    }

    @Override
    public String toString() {
        return title;
    }

    public Calendar getDueDate() {
        return dueDate;
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }
}
