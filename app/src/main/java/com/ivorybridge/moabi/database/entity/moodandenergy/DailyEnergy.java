package com.ivorybridge.moabi.database.entity.moodandenergy;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

@Keep
@Entity(tableName = "daily_energy_table")
public class DailyEnergy {

    @PrimaryKey
    @NonNull
    public String date;
    public Double averageEnergy;
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

    public Double getAverageEnergy() {
        return averageEnergy;
    }

    public void setAverageEnergy(Double averageEnergy) {
        this.averageEnergy = averageEnergy;
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

    @NonNull
    @Override
    public String toString() {
        return getDate() + ": " + getAverageEnergy();
    }
}
