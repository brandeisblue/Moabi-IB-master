package com.ivorybridge.moabi.database.entity.googlefit;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.annotation.Keep;

@Keep
@Entity(foreignKeys = @ForeignKey(entity = GoogleFitSummary.class, parentColumns = "date", childColumns = "name, recurrenceUnit, recurrenceFreq, goal"))
public class GoogleFitGoal {

    public String name;
    public String recurrenceUnit;
    public Long recurrenceFreq;
    public String goal;

    public GoogleFitGoal() {
    }

    @Ignore
    public GoogleFitGoal(String n, String u, Long f, String g) {
        this.name = n;
        this.recurrenceUnit = u;
        this.recurrenceFreq = f;
        this.goal = g;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRecurrenceUnit() {
        return recurrenceUnit;
    }

    public void setRecurrenceUnit(String recurrenceUnit) {
        this.recurrenceUnit = recurrenceUnit;
    }

    public Long getRecurrenceFreq() {
        return recurrenceFreq;
    }

    public void setRecurrenceFreq(Long recurrenceFreq) {
        this.recurrenceFreq = recurrenceFreq;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    @Override
    public String toString() {
        return "Goal: " + getName() + " - " + getGoal() + " " + getRecurrenceUnit();
    }
}