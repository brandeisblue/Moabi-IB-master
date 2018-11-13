package com.ivorybridge.moabi.database.entity.appusage;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.ivorybridge.moabi.database.converter.AppUsageTypeConverter;

import java.util.List;

@Keep
@Entity(tableName = "app_usage_table")
public class AppUsageSummary {

    @PrimaryKey
    @NonNull
    public String date;
    public Long dateInLong;
    public Long timeOfEntry;
    @TypeConverters(AppUsageTypeConverter.class)
    public List<AppUsage> activities;

    public AppUsageSummary() {
    }

    @Ignore
    public AppUsageSummary(String date) {
        this.date = date;
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

    public List<AppUsage> getActivities() {
        return activities;
    }

    public void setActivities(List<AppUsage> activities) {
        this.activities = activities;
    }
}
