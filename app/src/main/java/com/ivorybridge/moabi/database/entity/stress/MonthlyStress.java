package com.ivorybridge.moabi.database.entity.stress;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

@Keep
@Entity(tableName = "monthly_stress_table")
public class MonthlyStress {

    @PrimaryKey
    @NonNull
    public String YYYYMM;
    public Double averageStress;
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

    public Double getAverageStress() {
        return averageStress;
    }

    public void setAverageStress(Double averageStress) {
        this.averageStress = averageStress;
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
        return getYYYYMM() + ": " + getAverageStress();
    }
}

