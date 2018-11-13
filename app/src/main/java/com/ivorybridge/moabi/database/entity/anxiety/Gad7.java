package com.ivorybridge.moabi.database.entity.anxiety;

import androidx.annotation.Keep;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Keep
@Entity(tableName = "gad7_table")
public class Gad7 {

    @PrimaryKey
    public Long timeOfEntry;
    public Long dateInLong;
    public Long score;

    public Gad7() {
    }

    @Ignore
    public Gad7(Long timeOfEntry, Long score) {
        this.score = score;
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

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return getDateInLong() + ": " + "score - " + getScore();
    }
}
