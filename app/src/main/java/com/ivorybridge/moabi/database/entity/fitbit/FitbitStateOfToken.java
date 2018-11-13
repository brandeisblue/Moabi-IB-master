package com.ivorybridge.moabi.database.entity.fitbit;

import androidx.annotation.Keep;

@Keep
public class FitbitStateOfToken {

    public Boolean active;

    public FitbitStateOfToken() {

    }

    public FitbitStateOfToken(Boolean active) {
        this.active = active;
    }
}
