package com.ivorybridge.moabi.database.entity.util;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Keep
@Entity(tableName = "connected_service_table")
public class ConnectedService {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    public String name;
    public String type;
    public boolean isConnected;

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return getName() + ": " + isConnected;
    }
}