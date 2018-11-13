package com.ivorybridge.moabi.database.entity.weather;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "weather_daily_summary_table")
public class WeatherDailySummary {

    @PrimaryKey
    @NonNull
    public String date;
    public Long dateInLong;
    public double maxTempC;
    public double maxTempF;
    public double minTempC;
    public double minTempF;
    public double avgTempC;
    public double avgTempF;
    public double totalPrecipmm;
    public double totalPrecipin;
    public double avgHumidity;
    public String condition;
    public String imageUrl;

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

    public double getMaxTempC() {
        return maxTempC;
    }

    public void setMaxTempC(double maxTempC) {
        this.maxTempC = maxTempC;
    }

    public double getMaxTempF() {
        return maxTempF;
    }

    public void setMaxTempF(double maxTempF) {
        this.maxTempF = maxTempF;
    }

    public double getMinTempC() {
        return minTempC;
    }

    public void setMinTempC(double minTempC) {
        this.minTempC = minTempC;
    }

    public double getMinTempF() {
        return minTempF;
    }

    public void setMinTempF(double minTempF) {
        this.minTempF = minTempF;
    }

    public double getAvgTempC() {
        return avgTempC;
    }

    public void setAvgTempC(double avgTempC) {
        this.avgTempC = avgTempC;
    }

    public double getAvgTempF() {
        return avgTempF;
    }

    public void setAvgTempF(double avgTempF) {
        this.avgTempF = avgTempF;
    }

    public double getTotalPrecipmm() {
        return totalPrecipmm;
    }

    public void setTotalPrecipmm(double totalPrecipmm) {
        this.totalPrecipmm = totalPrecipmm;
    }

    public double getTotalPrecipin() {
        return totalPrecipin;
    }

    public void setTotalPrecipin(double totalPrecipin) {
        this.totalPrecipin = totalPrecipin;
    }

    public double getAvgHumidity() {
        return avgHumidity;
    }

    public void setAvgHumidity(double avgHumidity) {
        this.avgHumidity = avgHumidity;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

