package com.ivorybridge.moabi.database.entity.stress;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Keep
@Entity(tableName = "weekly_stress_table")
public class WeeklyStress {

    @PrimaryKey
    @NonNull
    public String YYYYW;
    public Double average;
    public Long startDateInLong;
    public Long endDateInLong;
    public Long timeOfEntry;
    public Long numOfEntries;

    @NonNull
    public String getYYYYW() {
        return YYYYW;
    }

    public void setYYYYW(@NonNull String YYYYW) {
        this.YYYYW = YYYYW;
    }

    public Double getAverageStress() {
        return average;
    }

    public void setAverageStress(Double average) {
        this.average = average;
    }

    public Long getStartDateInLong() {
        return startDateInLong;
    }

    public void setStartDateInLong(Long startDateInLong) {
        this.startDateInLong = startDateInLong;
    }

    public Long getEndDateInLong() {
        return endDateInLong;
    }

    public void setEndDateInLong(Long endDateInLong) {
        this.endDateInLong = endDateInLong;
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
        return getYYYYW() + ": " + getAverageStress();
    }
}
