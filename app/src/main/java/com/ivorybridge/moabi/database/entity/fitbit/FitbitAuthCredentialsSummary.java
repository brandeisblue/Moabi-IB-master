package com.ivorybridge.moabi.database.entity.fitbit;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.annotation.Keep;

@Keep
@Entity
public class FitbitAuthCredentialsSummary {

    @ColumnInfo(name="auth_id")
    public int authId;
    public String access_token;
    public Long expires_in;
    public String refresh_token;
    public String token_type;
    public String user_id;

    public FitbitAuthCredentialsSummary() {
    }

    @Ignore
    public FitbitAuthCredentialsSummary(String access_token, Long expires_in, String refresh_token,
                                        String token_type, String user_id) {
        this.access_token = access_token;
        this.expires_in = expires_in;
        this.refresh_token = refresh_token;
        this.token_type = token_type;
        this.user_id = user_id;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public Long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Long expires_in) {
        this.expires_in = expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "Access Token: " + getAccess_token() + "\n Refresh Token: " + getRefresh_token()
                + "\n Expires In: " + getExpires_in();
    }
}
