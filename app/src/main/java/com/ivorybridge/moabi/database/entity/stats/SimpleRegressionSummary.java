package com.ivorybridge.moabi.database.entity.stats;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import java.util.Comparator;

@Keep
@Entity(tableName = "simple_regression_summary_table")
public class SimpleRegressionSummary {

    @PrimaryKey
    @NonNull
    public String depXIndepVars;
    public String date;
    public Long dateInLong;
    public Long timeOfEntry;
    public int duration;
    public String depVar;
    public String indepVar;
    public String indepVarType;
    // 0 if mind, 1 if body
    public Long depVarType;
    public String depVarTypeString;
    public Double slope;
    public Double intercept;
    public Double coefOfDetermination;
    public Double correlation;
    public Double indepVarAverage;
    public Double recommendedIndepVar;
    public Long numOfData;

    public SimpleRegressionSummary() {
    }

    public SimpleRegressionSummary(String depVar,
                                   String indepVar, String indepVarType, Double slope,
                                   Double yintercept, Double rsquared, Double correlation,
                                   Double indepVarAverage,
                                   Double recommendedIndepVar) {
        this.depVar = depVar;
        this.indepVar = indepVar;
        this.indepVarType = indepVarType;
        this.slope = slope;
        this.intercept = yintercept;
        this.coefOfDetermination = rsquared;
        this.correlation = correlation;
        this.indepVarAverage = indepVarAverage;
        this.recommendedIndepVar = recommendedIndepVar;
    }

    @NonNull
    public String getDepXIndepVars() {
        return depXIndepVars;
    }

    public void setDepXIndepVars(@NonNull String depXIndepVars) {
        this.depXIndepVars = depXIndepVars;
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

    public Long getTimeOfEntry() {
        return timeOfEntry;
    }

    public void setTimeOfEntry(Long timeOfEntry) {
        this.timeOfEntry = timeOfEntry;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Double getSlope() {
        return slope;
    }

    public void setSlope(Double slope) {
        this.slope = slope;
    }

    public Double getIntercept() {
        return intercept;
    }

    public void setIntercept(Double intercept) {
        this.intercept = intercept;
    }

    public Double getCorrelation() {
        return correlation;
    }

    public void setCorrelation(Double correlation) {
        this.correlation = correlation;
    }

    public Double getRecommendedActivityLevel() {
        return recommendedIndepVar;
    }

    public void setRecommendedActivityLevel(Double recommendedActivityLevel) {
        this.recommendedIndepVar = recommendedActivityLevel;
    }

    public Double getAverageActivityLevel() {
        return indepVarAverage;
    }

    public void setAverageActivityLevel(Double averageActivityLevel) {
        this.indepVarAverage = averageActivityLevel;
    }

    public String getDepVar() {
        return depVar;
    }

    public void setDepVar(String depVar) {
        this.depVar = depVar;
    }

    public String getIndepVar() {
        return indepVar;
    }

    public void setIndepVar(String indepVar) {
        this.indepVar = indepVar;
    }

    public String getIndepVarType() {
        return indepVarType;
    }

    public void setIndepVarType(String indepVarType) {
        this.indepVarType = indepVarType;
    }

    public Long getDepVarType() {
        return depVarType;
    }

    public void setDepVarType(Long depVarType) {
        this.depVarType = depVarType;
    }

    public Double getCoefOfDetermination() {
        return coefOfDetermination;
    }

    public void setCoefOfDetermination(Double coefOfDetermination) {
        this.coefOfDetermination = coefOfDetermination;
    }

    public String getDepVarTypeString() {
        return depVarTypeString;
    }

    public void setDepVarTypeString(String depVarTypeString) {
        this.depVarTypeString = depVarTypeString;
    }

    public Long getNumOfData() {
        return numOfData;
    }

    public void setNumOfData(Long numOfData) {
        this.numOfData = numOfData;
    }

    public static class BestFitComparator implements Comparator<SimpleRegressionSummary> {
        @Override
        public int compare(SimpleRegressionSummary o1, SimpleRegressionSummary o2) {
            if (o1.getCoefOfDetermination() != null && o2.getCoefOfDetermination() != null) {
                return Double.compare(o2.getCoefOfDetermination(), o1.getCoefOfDetermination());
            } else {
                return 0;
            }
        }
    }
}

