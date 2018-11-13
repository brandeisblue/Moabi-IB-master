package com.ivorybridge.moabi.database.entity.fitbit;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.annotation.Keep;

@Keep
@Entity(foreignKeys = @ForeignKey(entity = FitbitDailySummary.class, parentColumns = "date", childColumns = "deviceVersion, lastSyncTime, battery"))
public class FitbitDeviceStatusSummary {

    /*
    @ColumnInfo(name="device_id")
    public int deviceId;*/
    public String deviceVersion;
    public String lastSyncTime;
    public String battery;

    public FitbitDeviceStatusSummary() {
    }

    @Ignore
    public FitbitDeviceStatusSummary(String deviceVersion, String lastSyncTime, String battery) {
        this.deviceVersion = deviceVersion;
        this.lastSyncTime = lastSyncTime;
        this.battery = battery;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public String getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(String lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    @Override
    public String toString() {
        return getDeviceVersion() + ": " + getLastSyncTime();
    }
}
