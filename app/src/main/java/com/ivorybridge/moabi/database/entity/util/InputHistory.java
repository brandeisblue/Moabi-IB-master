package com.ivorybridge.moabi.database.entity.util;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "input_history_table")
public class InputHistory {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "entry_id")
    public int id;
    @NonNull
    public String date;
    public Long dateInLong;
    @NonNull
    public String inputType;
    public Long timeOfEntry;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public Long getDateInLong() {
        return dateInLong;
    }

    public void setDateInLong(Long dateInLong) {
        this.dateInLong = dateInLong;
    }

    @NonNull
    public String getInputType() {
        return inputType;
    }

    public void setInputType(@NonNull String inputType) {
        this.inputType = inputType;
    }

    public Long getTimeOfEntry() {
        return timeOfEntry;
    }

    public void setTimeOfEntry(Long timeOfEntry) {
        this.timeOfEntry = timeOfEntry;
    }

    @Override
    public String toString() {
        return getInputType() + ": " + getDate() + " " + getDateInLong() + " " + getTimeOfEntry();
    }
}
