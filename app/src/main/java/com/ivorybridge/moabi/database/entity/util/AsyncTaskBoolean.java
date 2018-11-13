package com.ivorybridge.moabi.database.entity.util;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "async_task_table")
public class AsyncTaskBoolean {

    @PrimaryKey
    @ColumnInfo(name = "task_name")
    @NonNull
    public String taskName;
    public Boolean result;

    public AsyncTaskBoolean() {
    }

    @Ignore
    public AsyncTaskBoolean(String taskName) {
        this.taskName = taskName;
        this.result = true;
    }

    @NonNull
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(@NonNull String taskName) {
        this.taskName = taskName;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }
}
