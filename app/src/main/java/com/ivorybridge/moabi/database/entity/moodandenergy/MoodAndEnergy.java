package com.ivorybridge.moabi.database.entity.moodandenergy;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.Keep;

@Keep
@Entity(tableName = "mood_and_energy_entry_table")
public class MoodAndEnergy {

    @NonNull
    @PrimaryKey
    public Long timeOfEntry;
    public Long dateInLong;
    @NonNull
    public Long mood;
    @NonNull
    public Long energyLevel;

    public MoodAndEnergy() {
    }

    @Ignore
    public MoodAndEnergy(Long timeOfEntry,@NonNull Long mood, @NonNull Long energyLevel) {
        this.mood = mood;
        this.energyLevel = energyLevel;
        this.timeOfEntry = timeOfEntry;
    }

    @NonNull
    public Long getTimeOfEntry() {
        return timeOfEntry;
    }

    public void setTimeOfEntry(@NonNull Long timeOfEntry) {
        this.timeOfEntry = timeOfEntry;
    }

    public Long getDateInLong() {
        return dateInLong;
    }

    public void setDateInLong(Long dateInLong) {
        this.dateInLong = dateInLong;
    }

    public void setMood(@NonNull Long mood) {
        this.mood = mood;
    }

    public void setEnergyLevel(@NonNull Long energyLevel) {
        this.energyLevel = energyLevel;
    }

    @NonNull
    public Long getMood() {
        return mood;
    }

    @NonNull
    public Long getEnergyLevel() {
        return energyLevel;
    }

    @Override
    public String toString() {
        return getTimeOfEntry() + ": " + "mood - " + getMood() + ", energyLevel - " + getEnergyLevel();
    }
}
