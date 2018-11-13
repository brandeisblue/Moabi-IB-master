package com.ivorybridge.moabi.database.entity.appusage;

import androidx.room.Ignore;

public class AppUsage {

    public Long totalTime;
    public String appName;
    public Long usageRank;

    public AppUsage() {
    }

    @Ignore
    public AppUsage(String s, Long t, Long r) {
        this.appName = s;
        this.totalTime = t;
        this.usageRank = r;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getUsageRank() {
        return usageRank;
    }

    public void setUsageRank(Long usageRank) {
        this.usageRank = usageRank;
    }

    @Override
    public String toString() {
        return this.getAppName() + ": " + this.getTotalTime();
    }
}
