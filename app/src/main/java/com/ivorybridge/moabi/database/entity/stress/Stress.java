package com.ivorybridge.moabi.database.entity.stress;

import androidx.annotation.Keep;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Keep
@Entity(tableName = "stress_table")
public class Stress {

    @PrimaryKey
    public Long timeOfEntry;
    public Long dateInLong;
    public Double stress;

    public Stress() {
    }

    @Ignore
    public Stress(Long timeOfEntry, Double stress) {
        this.stress = stress;
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

    public Double getStress() {
        return stress;
    }

    public void setStress(Double stress) {
        this.stress = stress;
    }

    @Override
    public String toString() {
        return getDateInLong() + ": " + "stress - " + getStress();
    }
}
