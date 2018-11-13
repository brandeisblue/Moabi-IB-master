package com.ivorybridge.moabi.database.entity.dailyreview;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Keep
@Entity(tableName = "monthly_daily_review_table")
public class MonthlyDailyReview {

    @PrimaryKey
    @NonNull
    public String YYYYMM;
    public Double averageDailyReview;
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

    public Double getAverageDailyReview() {
        return averageDailyReview;
    }

    public void setAverageDailyReview(Double averageDailyReview) {
        this.averageDailyReview = averageDailyReview;
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
        return getYYYYMM() + ": " + getAverageDailyReview();
    }
}
