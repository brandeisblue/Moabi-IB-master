package com.ivorybridge.moabi.database.entity.builtinfitness;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "built_in_profile_table")
public class BuiltInProfile {

    @PrimaryKey
    @NonNull
    public String uniqueID;
    public String name;
    public String gender;
    public Double height;
    public Double BMR;
    public Double weight;
    public String dateOfBirth;
    public String dateOfRegistration;

    @NonNull
    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(@NonNull String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getBMR() {
        return BMR;
    }

    public void setBMR(Double BMR) {
        this.BMR = BMR;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfRegistration() {
        return dateOfRegistration;
    }

    public void setDateOfRegistration(String dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
    }
}
