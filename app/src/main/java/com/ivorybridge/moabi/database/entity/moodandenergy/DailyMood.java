package com.ivorybridge.moabi.database.entity.moodandenergy;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

@Keep
@Entity(tableName = "daily_mood_table")
public class DailyMood {

    @PrimaryKey
    @NonNull
    public String date;
    public Double averageMood;
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

    public Double getAverageMood() {
        return averageMood;
    }

    public void setAverageMood(Double averageMood) {
        this.averageMood = averageMood;
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
        return getDate() + ": " + getAverageMood();
    }
}
