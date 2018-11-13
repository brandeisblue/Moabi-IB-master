package com.ivorybridge.moabi.database.entity.dailyreview;

import androidx.annotation.Keep;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Keep
@Entity(tableName = "daily_review_table")
public class DailyReview {

    @PrimaryKey
    public Long timeOfEntry;
    public Long dateInLong;
    public Long dailyReview;

    public DailyReview() {
    }

    @Ignore
    public DailyReview(Long timeOfEntry, Long dailyReview) {
        this.dailyReview = dailyReview;
        this.timeOfEntry = timeOfEntry;
    }

    public Long getTimeOfEntry() {
        return timeOfEntry;
    }

    public void setTimeOfEntry(Long timeOfEntry) {
        this.timeOfEntry = timeOfEntry;
    }

    public Long getDateInLong() {
        return dateInLong;
    }

    public void setDateInLong(Long dateInLong) {
        this.dateInLong = dateInLong;
    }

    public Long getDailyReview() {
        return dailyReview;
    }

    public void setDailyReview(Long dailyReview) {
        this.dailyReview = dailyReview;
    }

    @Override
    public String toString() {
        return getDateInLong() + ": " + "dailyReview - " + getDailyReview();
    }
}

