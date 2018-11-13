package com.ivorybridge.moabi.database.entity.baactivity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "ba_activity_entry_table")
public class BAActivityEntry {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "entry_id")
    public int id;
    @NonNull
    public String name;
    @NonNull
    public Long timeOfEntry;
    public Long dateInLong;
    @NonNull
    public Long activityType;
    public Long importanceRating;
    public Long enjoymentRating;

    public BAActivityEntry() {
    }

    @Ignore
    public BAActivityEntry(@NonNull String name, @NonNull Long timeOfEntry, @NonNull Long activityType) {
        this.name = name;
        this.timeOfEntry = timeOfEntry;
        this.activityType = activityType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
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

    @NonNull
    public Long getActivityType() {
        return activityType;
    }

    public void setActivityType(@NonNull Long activityType) {
        this.activityType = activityType;
    }

    public Long getImportanceRating() {
        return importanceRating;
    }

    public void setImportanceRating(Long importanceRating) {
        this.importanceRating = importanceRating;
    }

    public Long getEnjoymentRating() {
        return enjoymentRating;
    }

    public void setEnjoymentRating(Long enjoymentRating) {
        this.enjoymentRating = enjoymentRating;
    }

    @Override
    public String toString() {
        return getId() + " " + getName() + " @" + getDateInLong() + "\n";
    }
}
