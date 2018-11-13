package com.ivorybridge.moabi.database.entity.util;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

@Keep
@Entity(tableName = "input_in_use_table")
public class InputInUse {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    public String name;
    public String type;
    public boolean inUse;

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return getName() + ": " + isInUse();
    }
}
