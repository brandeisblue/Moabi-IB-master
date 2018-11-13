package com.ivorybridge.moabi.database.entity.fitbit;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.TypeConverters;
import androidx.annotation.Keep;

import com.ivorybridge.moabi.database.converter.FitbitTypeConverter;

import java.util.List;

/**
 * Created by skim2 on 1/16/2018.
 */

@Keep
@Entity(foreignKeys = @ForeignKey(entity = FitbitDailySummary.class, parentColumns = "date", childColumns = "activityId, summary, goals"))
public class FitbitActivitySummary {

    /*
    @ColumnInfo(name="activity_id")
    public int activityId;*/
    @Embedded
    public Summary summary;
    @Embedded
    public Goals goals;

    public FitbitActivitySummary() {
    }

    @Ignore
    public FitbitActivitySummary(Summary summary, Goals goals) {
        this.summary = summary;
        this.goals = goals;
    }

    /*
    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activitySummaryId) {
        this.activityId = activitySummaryId;
    }*/

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public Goals getGoals() {
        return goals;
    }

    public void setGoals(Goals goals) {
        this.goals = goals;
    }

    @Override
    public String toString() {
        return summary.toString() + ", " + goals.toString();
    }

    @Keep
    public static class Goals {
        /*
        @ColumnInfo(name="activity_goals_id")
        public int activityGoalsId;*/
        @ColumnInfo(name = "steps_goal")
        public Long steps;
        @ColumnInfo(name = "distance_goal")
        public Double distance;
        @ColumnInfo(name = "calories_goal")
        public Long caloriesOut;
        @ColumnInfo(name = "floors_goal")
        public Long floors;
        @ColumnInfo(name = "activeMinutes_goal")
        public Long activeMinutes;

        public Goals() {
        }

        @Ignore
        public Goals(Long steps, Double distance, Long caloriesOut, Long floors, Long activeMinutes) {
            this.steps = steps;
            this.distance = distance;
            this.caloriesOut = caloriesOut;
            this.floors = floors;
            this.activeMinutes = activeMinutes;
        }

        /*
        public int getActivityGoalsId() {
            return activityGoalsId;
        }

        public void setActivityGoalsId(int activityGoalsId) {
            this.activityGoalsId = activityGoalsId;
        }*/

        public Long getSteps() {
            return steps;
        }

        public void setSteps(Long steps) {
            this.steps = steps;
        }

        public Double getDistance() {
            return distance;
        }

        public void setDistance(Double distance) {
            this.distance = distance;
        }

        public Long getCaloriesOut() {
            return caloriesOut;
        }

        public void setCaloriesOut(Long caloriesOut) {
            this.caloriesOut = caloriesOut;
        }

        public Long getFloors() {
            return floors;
        }

        public void setFloors(Long floors) {
            this.floors = floors;
        }

        public Long getActiveMinutes() {
            return activeMinutes;
        }

        public void setActiveMinutes(Long activeMinutes) {
            this.activeMinutes = activeMinutes;
        }

        @Override
        public String toString() {
            return "Goals - " + " Steps: " + getSteps() + " Distance: " + getDistance() + " Calories: " + getCaloriesOut();
        }
    }

    @Keep
    public static class Summary {
        /*
        @ColumnInfo(name="activity_summary_id")
        public int activitySummaryId;*/
        public Long activityCalories;
        public Long caloriesBMR;
        public Long caloriesOut;
        @TypeConverters(FitbitTypeConverter.class)
        public List<Distance> distances;
        public Long fairlyActiveMinutes;
        public Long floors;
        public Long lightlyActiveMinutes;
        public Long marginalCalories;
        public Long sedentaryMinutes;
        public Long steps;
        public Long veryActiveMinutes;

        public Summary() {
        }

        @Ignore
        public Summary(Long activityCalories, Long caloriesBMR, Long caloriesOut,
                       List<Distance> distances, Long fairlyActiveMinutes, Long floors,
                       Long lightlyActiveMinutes, Long marginalCalories, Long sedentaryMinutes,
                       Long steps, Long veryActiveMinutes) {
            this.activityCalories = activityCalories;
            this.caloriesBMR = caloriesBMR;
            this.caloriesOut = caloriesOut;
            this.distances = distances;
            this.fairlyActiveMinutes = fairlyActiveMinutes;
            this.floors = floors;
            this.lightlyActiveMinutes = lightlyActiveMinutes;
            this.marginalCalories = marginalCalories;
            this.sedentaryMinutes = sedentaryMinutes;
            this.steps = steps;
            this.veryActiveMinutes = veryActiveMinutes;
        }

        /*
        public int getActivitySummaryId() {
            return activitySummaryId;
        }

        public void setActivitySummaryId(int activitySummaryId) {
            this.activitySummaryId = activitySummaryId;
        }*/

        public Long getActivityCalories() {
            return activityCalories;
        }

        public void setActivityCalories(Long activityCalories) {
            this.activityCalories = activityCalories;
        }

        public Long getCaloriesBMR() {
            return caloriesBMR;
        }

        public void setCaloriesBMR(Long caloriesBMR) {
            this.caloriesBMR = caloriesBMR;
        }

        public Long getCaloriesOut() {
            return caloriesOut;
        }

        public void setCaloriesOut(Long caloriesOut) {
            this.caloriesOut = caloriesOut;
        }

        public List<Distance> getDistances() {
            return distances;
        }

        public void setDistances(List<Distance> distances) {
            this.distances = distances;
        }

        public Long getFairlyActiveMinutes() {
            return fairlyActiveMinutes;
        }

        public void setFairlyActiveMinutes(Long fairlyActiveMinutes) {
            this.fairlyActiveMinutes = fairlyActiveMinutes;
        }

        public Long getFloors() {
            return floors;
        }

        public void setFloors(Long floors) {
            this.floors = floors;
        }

        public Long getLightlyActiveMinutes() {
            return lightlyActiveMinutes;
        }

        public void setLightlyActiveMinutes(Long lightlyActiveMinutes) {
            this.lightlyActiveMinutes = lightlyActiveMinutes;
        }

        public Long getMarginalCalories() {
            return marginalCalories;
        }

        public void setMarginalCalories(Long marginalCalories) {
            this.marginalCalories = marginalCalories;
        }

        public Long getSedentaryMinutes() {
            return sedentaryMinutes;
        }

        public void setSedentaryMinutes(Long sedentaryMinutes) {
            this.sedentaryMinutes = sedentaryMinutes;
        }

        public Long getSteps() {
            return steps;
        }

        public void setSteps(Long steps) {
            this.steps = steps;
        }

        public Long getVeryActiveMinutes() {
            return veryActiveMinutes;
        }

        public void setVeryActiveMinutes(Long veryActiveMinutes) {
            this.veryActiveMinutes = veryActiveMinutes;
        }

        @Override
        public String toString() {
            return "Summary - " + " Steps: " + getSteps() + ", Distance: " + getDistances() + ", Calories: " + getCaloriesOut() + "VeryActiveMinutes: " + getVeryActiveMinutes();
        }

        @Keep
        public static class Distance {
            /*
            @ColumnInfo(name="distance_id")
            public int distanceId;*/
            public String activity;
            public Double distance;

            public Distance() {
            }

            @Ignore
            public Distance(String activity, Double distance) {
                this.activity = activity;
                this.distance = distance;
            }

            public String getActivity() {
                return activity;
            }

            public void setActivity(String activity) {
                this.activity = activity;
            }

            public Double getDistance() {
                return distance;
            }

            public void setDistance(Double distance) {
                this.distance = distance;
            }

            @Override
            public String toString() {
                return this.activity + ": " + this.distance;
            }
        }
    }
}

