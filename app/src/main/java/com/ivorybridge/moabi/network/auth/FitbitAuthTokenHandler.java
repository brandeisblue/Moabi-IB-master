package com.ivorybridge.moabi.network.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.Credential;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.repository.CredentialRepository;
import com.ivorybridge.moabi.repository.DataInUseRepository;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by skim2 on 11/27/2017.
 */

public class FitbitAuthTokenHandler {

    private static final String TAG = FitbitAuthTokenHandler.class.getSimpleName();
    private Context mContext;
    private String mRedirectUriString;
    private String codeString;
    private String stateString;
    private String encodedCodeString;
    private String CODE_VERIFIER;
    private String CODE_CHALLENGE;
    private static final String CLIENT_ID = "22D6TC";
    private static final String CLIENT_SECRET = "05f59d5f32a14ecba00cd6c2a58fa7a5";
    private static final String FITBIT_TOKEN_PREFERENCES = "FibitTokenPrefs";
    private SharedPreferences.Editor mFitbitEditor;
    private static final String USAGE_PREFERENCES = "UsagePref";
    private RequestQueue queue;
    private static final String accessTokenFitbitUrl = "https://api.fitbit.com/oauth2/token";
    private SharedPreferences sharedpreferences;
    private DataInUseRepository dataInUseRepository;
    private CredentialRepository credentialRepository;

    public FitbitAuthTokenHandler(AppCompatActivity activity, String redirectUri) {
        Log.i(TAG, TAG + " is created");
        mContext = activity.getApplicationContext();
        mRedirectUriString = redirectUri;
        sharedpreferences = mContext.getSharedPreferences(
                FITBIT_TOKEN_PREFERENCES, Context.MODE_PRIVATE);
        mFitbitEditor = sharedpreferences.edit();
        //parseImplicitGrantResponse();
        parseAuthorizationCode();
        String clientCredentials = CLIENT_ID + ":" + CLIENT_SECRET;
        CODE_VERIFIER = sharedpreferences.getString("CODE_VERIFIER", "");
        CODE_CHALLENGE = sharedpreferences.getString("CODE_CHALLENGE", "");
        encodedCodeString = Base64.encodeToString(clientCredentials.getBytes(), 0);
        dataInUseRepository = new DataInUseRepository(activity.getApplication());
        credentialRepository = new CredentialRepository(activity.getApplication());
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("encodedCredentials", encodedCodeString);
        editor.apply();
        queue = Volley.newRequestQueue(mContext);
        StringRequest stringRequest = makePostRequest();
        Log.i(TAG, "Token Request is " + stringRequest);
        queue.add(stringRequest);
    }

    private void parseAuthorizationCode() {
        Log.i(TAG, "parseAuthorizationCode() called");
        int startOfCode = mRedirectUriString.indexOf("code=") + 5;
        int startOfState = mRedirectUriString.indexOf("state=");
        int lastOfCode = mRedirectUriString.lastIndexOf("#_=_");
        codeString = mRedirectUriString.substring(startOfCode, startOfState - 1);
        stateString = mRedirectUriString.substring(startOfState + 6, lastOfCode);
        Log.i(TAG, "Code is " + codeString);
        Log.i(TAG, "State is " + stateString);
    }

    private StringRequest makePostRequest() {
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, accessTokenFitbitUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "Response is " + response);
                        //parseAndSaveTokens(response);
                        InputInUse inputInUse = new InputInUse();
                        inputInUse.setType("tracker");
                        inputInUse.setName(mContext.getString(R.string.fitbit_camel_case));
                        inputInUse.setInUse(true);
                        dataInUseRepository.insert(inputInUse);
                        ConnectedService connectedService = new ConnectedService();
                        connectedService.setType("tracker");
                        connectedService.setName(mContext.getString(R.string.fitbit_camel_case));
                        connectedService.setConnected(true);
                        dataInUseRepository.insert(connectedService);
                        //firebaseManager.getIsConnectedRef().child(mContext.getString(R.string.fitbit_camel_case)).setValue(true);
                        //firebaseManager.getInputsInUseRef().child(mContext.getString(R.string.fitbit_camel_case)).setValue(true);
                        mFitbitEditor.putBoolean("isFitbitConnected", true);
                        mFitbitEditor.apply();
                        Gson gson = new Gson();
                        Map<String, Object> map =
                                gson.fromJson(response,
                                        new TypeToken<HashMap<String, Object>>() {
                                        }.getType());
                        Credential credential = new Credential();
                        credential.setServiceName(mContext.getString(R.string.fitbit_camel_case));
                        credential.setAccessToken((String) map.get("access_token"));
                        credential.setRefreshToken((String) map.get("refresh_token"));
                        credentialRepository.insert(credential);
                        //firebaseManager.getFitbitCredentialRef().updateChildren(map);
                        //firebaseManager.getFitbitCredentialRef().child("time").setValue(getCurrentTime());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse errorRes = error.networkResponse;
                String stringData = "";
                InputInUse inputInUse = new InputInUse();
                inputInUse.setType("tracker");
                inputInUse.setName(mContext.getString(R.string.fitbit_camel_case));
                inputInUse.setInUse(false);
                dataInUseRepository.insert(inputInUse);
                ConnectedService connectedService = new ConnectedService();
                connectedService.setType("tracker");
                connectedService.setName(mContext.getString(R.string.fitbit_camel_case));
                connectedService.setConnected(false);
                dataInUseRepository.insert(connectedService);
                //firebaseManager.getIsConnectedRef().child(mContext.getString(R.string.fitbit_camel_case)).setValue(false);
                //firebaseManager.getInputsInUseRef().child(mContext.getString(R.string.fitbit_camel_case)).setValue(false);
                //firebaseManager.getConnectedServicesRef().child(mContext.getString(R.string.fitbit_camel_case)).child("time").setValue(getCurrentTime());
                mFitbitEditor.putBoolean("isFitbitConnected", false);
                mFitbitEditor.apply();
                Toast.makeText(mContext, "Authentication failed\nPlease try again", Toast.LENGTH_LONG).show();
                if(errorRes != null && errorRes.data != null){
                    try {
                        stringData = new String(errorRes.data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "Error response is " + stringData);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("code", codeString);
                params.put("client_id", CLIENT_ID);
                params.put("grant_type", "authorization_code");
                params.put("redirect_uri", "moabi://logincallback/fitbit");
                params.put("state", stateString);
                params.put("code_verifier", CODE_VERIFIER);
                params.put("expires_in", "3600");
                Log.i(TAG, "Requested code is " + codeString + " State is " + stateString +" code_verifier is " + CODE_VERIFIER);
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Basic " + encodedCodeString);
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        return stringRequest;
    }
}
