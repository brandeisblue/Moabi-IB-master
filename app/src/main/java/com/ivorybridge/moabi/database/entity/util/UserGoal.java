package com.ivorybridge.moabi.database.entity.util;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "user_goal_table")
public class UserGoal {

    @PrimaryKey
    @NonNull
    public int priority;
    public String goalName;
    // mood, energy level, activity, Fitbit, GoogleFit, AppUsage
    public String goalType;
    public String date;
    public Long dateInLong;
    public Double goal;

    @NonNull
    public int getPriority() {
        return priority;
    }

    public void setPriority(@NonNull int priority) {
        this.priority = priority;
    }

    @NonNull
    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(@NonNull String goalName) {
        this.goalName = goalName;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getDateInLong() {
        return dateInLong;
    }

    public void setDateInLong(Long dateInLong) {
        this.dateInLong = dateInLong;
    }

    public Double getGoal() {
        return goal;
    }

    public void setGoal(Double goal) {
        this.goal = goal;
    }
}
