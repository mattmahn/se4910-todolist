package com.mahnke.todolist;

import java.util.Calendar;

public class TodoItem {

    private long id;
    private Calendar dueDate;
    private String summary;
    private String description;
    private int priority;
    private boolean completed;
    private long datetime;

    public TodoItem(String summary) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        this.dueDate = tomorrow;
        this.summary = summary;
    }

    public TodoItem(Calendar dueDate, String summary) {
        this.dueDate = dueDate;
        this.summary = summary;
    }

    @Override
    public String toString() {
        return summary;
    }

    public Calendar getDueDate() {
        return dueDate;
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String title) {
        this.summary = title;
    }

}
