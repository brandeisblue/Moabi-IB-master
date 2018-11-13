package com.ivorybridge.moabi.network.auth;

import com.ivorybridge.moabi.database.entity.fitbit.FitbitActivitySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitAuthCredentialsSummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDeviceStatusSummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitSleepSummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitStateOfToken;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FitbitService {

    @GET("/1/user/-/activities/date/{date}.json")
    Call<FitbitActivitySummary> getDailySummary(@Header("Authorization") String accessToken, @Path("date") String date);

    @GET("/1/user/-/devices.json")
    Call<List<FitbitDeviceStatusSummary>> getDeviceStatus(@Header("Authorization") String accessToken);

    @GET("/1.2/user/-/sleep/date/{todaysDate}.json")
    Call<FitbitSleepSummary> getSleepSummary(@Header("Authorization") String accessToken,
                                             //@Path("yesterdaysDate") String yesterdaysDate,
                                             @Path("todaysDate") String todaysDate);

    @FormUrlEncoded
    @POST("/oauth2/token")
    Call<FitbitAuthCredentialsSummary> getAccessTokenWithRefreshToken(@Header("Authorization") String authHeader,
                                                                      @Header("Content-Type") String contentTypeHeader,
                                                                      @Field("grant_type") String grantType,
                                                                      @Field("refresh_token") String refreshToken,
                                                                      @Field("expires_in") String expiresIn);
    @FormUrlEncoded
    @POST("/1.1/oauth2/introspect")
    Call<FitbitStateOfToken> getTokenState(@Header("Authorization") String authHeader,
                                           @Header("Accept-Language") String contentTypeHeader,
                                           @Field("token") String token);
}
