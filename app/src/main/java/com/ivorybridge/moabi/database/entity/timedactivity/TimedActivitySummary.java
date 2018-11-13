package com.ivorybridge.moabi.database.entity.timedactivity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "timed_activity_table")
public class TimedActivitySummary implements Comparable<TimedActivitySummary> {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "entry_id")
    public int id;
    @NonNull
    public String date;
    public Long dateInLong;
    @NonNull
    public String inputName;
    @NonNull
    public Long duration;
    public Long timeOfEntry;

    public TimedActivitySummary() {
    }

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
    public String getInputName() {
        return inputName;
    }

    public void setInputName(@NonNull String inputName) {
        this.inputName = inputName;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getTimeOfEntry() {
        return timeOfEntry;
    }

    public void setTimeOfEntry(Long timeOfEntry) {
        this.timeOfEntry = timeOfEntry;
    }

    @Override
    public int compareTo(@NonNull TimedActivitySummary o) {
        return this.getDuration().compareTo(o.getDuration());
    }

    @Override
    public String toString() {
        return getId() + " " + getInputName() + ": " + getDate() + " " + getDateInLong() + " " + getDuration();    }

}
