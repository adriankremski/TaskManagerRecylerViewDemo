package com.github.snuffix.recyclerviewdemoapp;

public class TaskCheckStateChangedEvent {
    public boolean isChecked;
    public int taskNumber;

    public TaskCheckStateChangedEvent(boolean isChecked, int taskNumber) {
        this.isChecked = isChecked;
        this.taskNumber = taskNumber;
    }
}
