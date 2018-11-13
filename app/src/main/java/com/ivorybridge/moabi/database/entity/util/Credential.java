package com.ivorybridge.moabi.database.entity.util;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "credential_table")
public class Credential {

    @NonNull
    @PrimaryKey
    public String serviceName;
    public String accessToken;
    public String refreshToken;
    public String userID;
    public String authID;
    public String tokenType;

    @NonNull
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(@NonNull String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getAuthID() {
        return authID;
    }

    public void setAuthID(String authID) {
        this.authID = authID;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
