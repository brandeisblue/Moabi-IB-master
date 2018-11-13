package com.ivorybridge.moabi.database.entity.dailyreview;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Keep
@Entity(tableName = "daily_daily_review_table")
public class DailyDailyReview {

    @PrimaryKey
    @NonNull
    public String date;
    public Double averageDailyReview;
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

    public Double getAverageDailyReview() {
        return averageDailyReview;
    }

    public void setAverageDailyReview(Double averageDailyReview) {
        this.averageDailyReview = averageDailyReview;
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
        return getDate() + ": " + getAverageDailyReview();
    }
}
