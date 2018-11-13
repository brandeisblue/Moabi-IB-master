package com.ivorybridge.moabi.database.entity.fitbit;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "fitbit_daily_summary_table")
public class FitbitDailySummary {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "date")
    public String date;
    public Long dateInLong;
    public Long timeOfEntry;
    @Embedded
    public FitbitActivitySummary activitySummary;
    @Embedded
    public FitbitSleepSummary sleepSummary;
    @Embedded
    public FitbitDeviceStatusSummary deviceStatusSummary;

    public FitbitDailySummary() {
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

    public Long getTimeOfEntry() {
        return timeOfEntry;
    }

    public void setTimeOfEntry(Long timeOfEntry) {
        this.timeOfEntry = timeOfEntry;
    }

    public FitbitActivitySummary getActivitySummary() {
        return activitySummary;
    }

    public void setActivitySummary(FitbitActivitySummary activitySummary) {
        this.activitySummary = activitySummary;
    }

    public FitbitSleepSummary getSleepSummary() {
        return sleepSummary;
    }

    public void setSleepSummary(FitbitSleepSummary sleepSummary) {
        this.sleepSummary = sleepSummary;
    }

    public FitbitDeviceStatusSummary getDeviceStatusSummary() {
        return deviceStatusSummary;
    }

    public void setDeviceStatusSummary(FitbitDeviceStatusSummary deviceStatusSummary) {
        this.deviceStatusSummary = deviceStatusSummary;
    }

    @Override
    public String toString() {
        return getDate() + ": " + getActivitySummary().toString() + "\n" + getSleepSummary().toString() + "\n" + getDeviceStatusSummary().toString();
        //return getDate();
    }
}

