package com.ivorybridge.moabi.database.entity.builtinfitness;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "built_in_activity_summary_table")
public class BuiltInActivitySummary {

    @PrimaryKey
    @NonNull
    public String date;
    public Long dateInLong;
    public Long timeOfEntry;
    public Long lastSensorTimeStamp;
    public Long steps;
    public Long activeMinutes;
    public Long sedentaryMinutes;
    public Double distance;
    public Double calories;

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

    public Long getLastSensorTimeStamp() {
        return lastSensorTimeStamp;
    }

    public void setLastSensorTimeStamp(Long lastSensorTimeStamp) {
        this.lastSensorTimeStamp = lastSensorTimeStamp;
    }

    public Long getSteps() {
        return steps;
    }

    public void setSteps(Long steps) {
        this.steps = steps;
    }

    public Long getActiveMinutes() {
        return activeMinutes;
    }

    public void setActiveMinutes(Long activeMinutes) {
        this.activeMinutes = activeMinutes;
    }

    public Long getSedentaryMinutes() {
        return sedentaryMinutes;
    }

    public void setSedentaryMinutes(Long sedentaryMinutes) {
        this.sedentaryMinutes = sedentaryMinutes;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }
}
