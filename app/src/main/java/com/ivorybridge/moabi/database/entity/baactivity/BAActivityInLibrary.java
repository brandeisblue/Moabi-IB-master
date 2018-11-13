package com.ivorybridge.moabi.database.entity.baactivity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

@Entity(tableName = "ba_activity_library_table")
@Keep
public class BAActivityInLibrary implements Comparable<BAActivityInLibrary> {

    @PrimaryKey
    @NonNull
    public String name;
    public Long activtyType;
    public String resourceID;
    public Long activtyOrder;

    public BAActivityInLibrary() {
    }

    @Ignore
    public BAActivityInLibrary(String n, Long at, Long o, String resourceID) {
        this.name = n;
        this.activtyType = at;
        this.activtyOrder = o;
        this.resourceID = resourceID;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getActivtyType() {
        return activtyType;
    }

    public void setActivtyType(Long activtyType) {
        this.activtyType = activtyType;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public Long getActivtyOrder() {
        return activtyOrder;
    }

    public void setActivtyOrder(Long activtyOrder) {
        this.activtyOrder = activtyOrder;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(BAActivityInLibrary otherObject) {
        return this.getName().compareTo(otherObject.getName());
    }
}