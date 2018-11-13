package com.ivorybridge.moabi.database.entity.stats;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

@Keep
@Entity(tableName = "regression_summary_table")
public class RegressionSummary {

    @PrimaryKey
    @NonNull
    public String depXIndepVariables;
    public String depVar;
    public String indepVar;
    // 0 if mind, 1 if body
    public Long type;
    public String date;
    public Long dateInLong;
    public Long timeOfEntry;
    public Double adjRSquared;
    public Double standardError;
    public Double predictedDepVar;

    @NonNull
    public String getDepXIndepVariables() {
        return depXIndepVariables;
    }

    public void setDepXIndepVariables(@NonNull String depXIndepVariables) {
        this.depXIndepVariables = depXIndepVariables;
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

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
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

    public Double getAdjRSquared() {
        return adjRSquared;
    }

    public void setAdjRSquared(Double adjRSquared) {
        this.adjRSquared = adjRSquared;
    }

    public Double getStandardError() {
        return standardError;
    }

    public void setStandardError(Double standardError) {
        this.standardError = standardError;
    }

    public Double getPredictedDepVar() {
        return predictedDepVar;
    }

    public void setPredictedDepVar(Double predictedDepVar) {
        this.predictedDepVar = predictedDepVar;
    }
}
