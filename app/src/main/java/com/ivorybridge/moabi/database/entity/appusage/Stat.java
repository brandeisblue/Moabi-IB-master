package com.ivorybridge.moabi.database.entity.appusage;

import androidx.annotation.NonNull;

import java.util.List;

public class Stat implements Comparable<Stat>{
    String appName;
    Long totalTime;
    Long startTime;
    List<Long> startTimes;
    Long timeStamp;
    Integer eventType;

    @Override
    public int compareTo(@NonNull Stat other) {
        // by descending order
        return other.totalTime.compareTo(totalTime);
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public List<Long> getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(List<Long> startTimes) {
        this.startTimes = startTimes;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }
}