package com.ivorybridge.moabi.database.entity.pain;

import com.ivorybridge.moabi.database.converter.StringListTypeConverter;

import java.util.List;

import androidx.annotation.Keep;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Keep
@Entity(tableName = "pain_table")
public class Pain {

    @PrimaryKey
    public Long timeOfEntry;
    public Long dateInLong;
    public Long pain;
    @TypeConverters(StringListTypeConverter.class)
    public List<String> locations;
    @TypeConverters(StringListTypeConverter.class)
    public List<String> dermatomes;
    @TypeConverters(StringListTypeConverter.class)
    public List<String> characteristics;
    @TypeConverters(StringListTypeConverter.class)
    public List<String> aggravatingFactors;
    @TypeConverters(StringListTypeConverter.class)
    public List<String> alleviatingFactors;
    public Long duration;
    public String notes;

    public Pain() {
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

    public Long getPain() {
        return pain;
    }

    public void setPain(Long pain) {
        this.pain = pain;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public List<String> getDermatomes() {
        return dermatomes;
    }

    public void setDermatomes(List<String> dermatomes) {
        this.dermatomes = dermatomes;
    }

    public List<String> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<String> characteristics) {
        this.characteristics = characteristics;
    }

    public List<String> getAggravatingFactors() {
        return aggravatingFactors;
    }

    public void setAggravatingFactors(List<String> aggravatingFactors) {
        this.aggravatingFactors = aggravatingFactors;
    }

    public List<String> getAlleviatingFactors() {
        return alleviatingFactors;
    }

    public void setAlleviatingFactors(List<String> alleviatingFactors) {
        this.alleviatingFactors = alleviatingFactors;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return getDateInLong() + ": " + "pain - " + getPain();
    }
}

