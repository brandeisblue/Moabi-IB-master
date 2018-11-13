package com.ivorybridge.moabi.database.entity.moodandenergy;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

@Keep
@Entity(tableName = "monthly_mood_table")
public class MonthlyMood {

    @PrimaryKey
    @NonNull
    public String YYYYMM;
    public Double averageMood;
    public Long startDateInLong;
    public Long endDateInLong;
    public Long timeOfEntry;
    public Long numOfEntries;

    @NonNull
    public String getYYYYMM() {
        return YYYYMM;
    }

    public void setYYYYMM(@NonNull String YYYYMM) {
        this.YYYYMM = YYYYMM;
    }

    public Double getAverageMood() {
        return averageMood;
    }

    public void setAverageMood(Double averageMood) {
        this.averageMood = averageMood;
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
        return getYYYYMM() + ": " + getAverageMood();
    }
}
