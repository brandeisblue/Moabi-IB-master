package com.ivorybridge.moabi.database.entity.googlefit;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.ivorybridge.moabi.database.converter.GoogleFitGoalsTypeConverter;
import com.ivorybridge.moabi.database.converter.GoogleFitSummariesTypeConverter;

import java.util.List;

@Keep
@Entity(tableName = "google_fit_summary_table")
public class GoogleFitSummary {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "date")
    public String date;
    public Long dateInLong;
    public Long timeOfEntry;
    @ColumnInfo(name = "last_sync")
    public String lastSyncTime;
    @TypeConverters(GoogleFitGoalsTypeConverter.class)
    public List<GoogleFitGoal> goals;
    @TypeConverters(GoogleFitSummariesTypeConverter.class)
    public List<Summary> summaries;

    @Ignore
    public GoogleFitSummary() {
    }


    public GoogleFitSummary(String date, List<GoogleFitGoal> goals, List<Summary> summaries) {
        this.date = date;
        this.goals = goals;
        this.summaries = summaries;
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

    public String getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(String lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public Long getTimeOfEntry() {
        return timeOfEntry;
    }

    public void setTimeOfEntry(Long timeOfEntry) {
        this.timeOfEntry = timeOfEntry;
    }

    public List<GoogleFitGoal> getGoals() {
        return goals;
    }

    public void setGoals(List<GoogleFitGoal> goals) {
        this.goals = goals;
    }

    public List<Summary> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<Summary> summaries) {
        this.summaries = summaries;
    }

    @Override
    public String toString() {
        if (getSummaries() != null && getGoals() != null) {
            return getDate() + ": [Goals: " + getGoals().toString() + "]\n[Summaries: " + getSummaries() + "]\n" + getLastSyncTime();
        } else if (getGoals() != null) {
            return getDate() + ": [Goals: " + getGoals().toString() + "]";
        } else if (getSummaries() != null) {
            return getDate() + ": [Summaries: " + getSummaries() + "]";
        } else {
            return getDate();
        }
    }

    @Keep
    public static class Summary {

        public String name;
        public Double value;

        @Ignore
        public Summary(){
        }

        public Summary(String name, Double value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return getName() + ": " + getValue();
        }
    }
}
