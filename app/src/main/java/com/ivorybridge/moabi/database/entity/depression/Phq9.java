package com.ivorybridge.moabi.database.entity.depression;

import androidx.annotation.Keep;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Keep
@Entity(tableName = "phq9_table")
public class Phq9 {

    @PrimaryKey
    public Long timeOfEntry;
    public Long dateInLong;
    public Long score;

    public Phq9() {
    }

    @Ignore
    public Phq9(Long timeOfEntry, Long score) {
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
