package com.ivorybridge.moabi.database.entity.stress;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Keep
@Entity(tableName = "daily_stress_table")
public class DailyStress {

    @PrimaryKey
    @NonNull
    public String date;
    public Double average;
    public Long dateInLong;
    public Long timeOfEntry;
    public Long numOfEntries;

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public Double getAverageStress() {
        return average;
    }

    public void setAverageStress(Double average) {
        this.average = average;
    }

    public Long getDateInLong() {
        return dateInLong;
    }

    public void setDateInLong(Long dateInLong) {
        this.dateInLong = dateInLong;
    }

    public Long getTimeOfEntry() {
        return timeOfEntry;
    }

    public void setTimeOfEntry(Long timeOfEntry) {
        this.timeOfEntry = timeOfEntry;
    }

    public Long getNumOfEntries() {
        return numOfEntries;
    }

    public void setNumOfEntries(Long numOfEntries) {
        this.numOfEntries = numOfEntries;
    }

    @NonNull
    @Override
    public String toString() {
        return getDate() + ": " + getAverageStress();
    }
}
