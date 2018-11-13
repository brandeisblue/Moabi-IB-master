package com.ivorybridge.moabi.network.auth;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.ivorybridge.moabi.ui.activity.ConnectServicesActivity;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import java.security.SecureRandom;

/**
 * Created by skim2 on 11/25/2017.
 */

public class FitbitAuthorizationRequest {

    private static final String TAG = FitbitAuthorizationRequest.class.getSimpleName();
    private static final String CLIENT_ID = "22D6TC";
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String CALLBACK_URL =
            "moabi://logincallback/fitbit";
    private final String AUTHORIZATION_URL = "https://www.fitbit.com/oauth2/authorize";
    private final String TOKEN_REQUEST_URL = "https://api.fitbit.com/oauth2/token";
    private final int FITBIT_PERMISSION_REQUEST_CODE = 0;
    private SharedPreferences fitbitSharedPreferences;
    private static final String FITBIT_TOKEN_PREFERENCES = "FibitTokenPrefs";
    private AuthorizationServiceConfiguration serviceConfig;
    private AuthorizationRequest.Builder authRequestBuilder;
    private AuthorizationRequest authRequest;
    private AuthorizationService authService;
    private Context mContext;


    public FitbitAuthorizationRequest(Context context) {

        // generate a random code verifier for fitbit authorization
        String CODE_VERIFIER = randomString(127);
        mContext = context;

        // for use in Fitbit
        // configure AuthorizationServiceConfiguration for fitbit
        serviceConfig = new AuthorizationServiceConfiguration(
                Uri.parse(AUTHORIZATION_URL),
                Uri.parse(TOKEN_REQUEST_URL)
        );

        // build an authorization request.
        authRequestBuilder = new AuthorizationRequest.Builder(
                serviceConfig,
                CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(CALLBACK_URL)
        );

        // make authorization request with additional parameters
        authRequest = authRequestBuilder
                .setScopes("activity", "settings", "sleep", "profile")
                .setCodeVerifier(CODE_VERIFIER)
                .build();
        authService = new AuthorizationService(mContext);
        String codeChallenge = authRequest.codeVerifierChallenge;
        String codeVerifier = authRequest.codeVerifier;
        Log.i(TAG, "CODE_CHALLENGE is " + codeChallenge);
        Log.i(TAG, "CODE_VERIFIER is " + codeVerifier);
        fitbitSharedPreferences = mContext.getSharedPreferences(FITBIT_TOKEN_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor fitbitEditor = fitbitSharedPreferences.edit();
        fitbitEditor.putString("CODE_CHALLENGE", codeChallenge);
        fitbitEditor.putString("CODE_VERIFIER", codeVerifier);
        fitbitEditor.commit();
        fitbitEditor.apply();
    }

    public void makeRequest() {
        Log.i(TAG, "FitbitAuthorizationRequest is made");
        String authorizationURL = authRequest.toUri().toString();
        Log.i(TAG,"AuthorizationURL is " + authorizationURL);
        int indexOfCCStart = authorizationURL.indexOf("code_challenge=") + 15;
        int indexofCCEnd = authorizationURL.indexOf("code_challenge_method=");
        String codeChallenge = authorizationURL.substring(indexOfCCStart, indexofCCEnd - 1);
        Log.i(TAG,"CODE_CHALLENGE is " + codeChallenge);

        // make the authorization request and send the result to the ConnectServicesActivity.
        authService.performAuthorizationRequest(
                authRequest,
                // intent to activity upon success
                PendingIntent.getActivity(mContext, FITBIT_PERMISSION_REQUEST_CODE,
                        new Intent(mContext, ConnectServicesActivity.class), PendingIntent.FLAG_ONE_SHOT),
                // intent to activity upon failure
                PendingIntent.getActivity(mContext, FITBIT_PERMISSION_REQUEST_CODE,
                        new Intent(mContext, ConnectServicesActivity.class), PendingIntent.FLAG_ONE_SHOT));
        Log.i(TAG, "Performing authorization request");
        authService.dispose();
    }

    String randomString( int len ){
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}
