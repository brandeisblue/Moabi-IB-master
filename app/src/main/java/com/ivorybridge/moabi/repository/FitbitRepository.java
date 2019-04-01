package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.AsyncTaskBooleanDao;
import com.ivorybridge.moabi.database.dao.FitbitDailySummaryDao;
import com.ivorybridge.moabi.database.db.AsyncTaskBooleanDB;
import com.ivorybridge.moabi.database.db.FitbitDailySummaryDB;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitActivitySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitAuthCredentialsSummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDeviceStatusSummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitSleepSummary;
import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;
import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.Credential;
import com.ivorybridge.moabi.database.entity.util.InputHistory;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.network.auth.FitbitService;
import com.ivorybridge.moabi.util.FormattedTime;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FitbitRepository {

    private static final String TAG = FitbitRepository.class.getSimpleName();
    private FitbitService fitbitService;
    private FitbitDailySummaryDao mFitbitDao;
    private AsyncTaskBooleanDao mTaskSuccessDao;
    private FirebaseManager firebaseManager;
    private Application mApplication;
    private FormattedTime formattedTime;
    private InputHistoryRepository inputHistoryRepository;
    private CredentialRepository credentialRepository;
    private DataInUseRepository dataInUseRepository;

    public FitbitRepository(Application application) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.fitbit.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.fitbitService = retrofit.create(FitbitService.class);
        this.firebaseManager = new FirebaseManager();
        FitbitDailySummaryDB db = FitbitDailySummaryDB.getDatabase(application);
        AsyncTaskBooleanDB successDB = AsyncTaskBooleanDB.getDatabase(application);
        inputHistoryRepository = new InputHistoryRepository(application);
        mFitbitDao = db.fitbitDao();
        mTaskSuccessDao = successDB.asyncTaskBooleanDao();
        mApplication = application;
        formattedTime = new FormattedTime();
        this.credentialRepository = new CredentialRepository(application);
        this.dataInUseRepository = new DataInUseRepository(application);
    }


    public LiveData<FitbitDailySummary> get(String date) {
        return mFitbitDao.get(date);
    }

    // wrapper for getDailySummary
    public LiveData<List<FitbitDailySummary>> getAll() {
        return mFitbitDao.getAll();

    }

    public LiveData<List<FitbitDailySummary>> getAll(Long start, Long end) {
        return mFitbitDao.getAll(start, end);
    }

    public List<FitbitDailySummary> getAllNow(Long start, Long end) {
        return mFitbitDao.getAllNow(start, end);
    }

    public boolean insert(FitbitDailySummary dailySummary, String date) {
        InputHistory inputHistory = new InputHistory();
        inputHistory.setDate(date);
        inputHistory.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
        inputHistory.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
        inputHistory.setInputType(mApplication.getString(R.string.fitbit_camel_case));
        inputHistoryRepository.insert(inputHistory);
        AsyncTask.Status status = new insertAsyncTask(mFitbitDao).execute(dailySummary).getStatus();
        return status.equals(AsyncTask.Status.FINISHED);
    }

    public void downloadData(String date) {
        fetch(date);
    }

    private void fetch(String date) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FitbitDailySummary fitbitDailySummary = new FitbitDailySummary();
                fitbitDailySummary.setDate(date);
                fitbitDailySummary.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                fitbitDailySummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                AsyncTaskBoolean fitbitTaskSuccess = new AsyncTaskBoolean(mApplication.getString(R.string.fitbit_camel_case));
                Credential credential = credentialRepository.getNow(mApplication.getString(R.string.fitbit_camel_case));
                final String accessToken = "Bearer "
                        + credential.getAccessToken();
                Call<FitbitActivitySummary> fitbitDailySummaryCall = fitbitService
                        .getDailySummary(accessToken, date);
                fitbitDailySummaryCall.enqueue(new Callback<FitbitActivitySummary>() {
                    @Override
                    public void onResponse(@NotNull Call<FitbitActivitySummary> call,
                                           @NotNull Response<FitbitActivitySummary> response) {
                        Log.i(TAG, response.toString());
                        // TODO - API call limit reached
                        if (response.toString().contains("429")) {
                            fitbitTaskSuccess.setResult(true);
                            insertSuccess(fitbitTaskSuccess);
                            Toast.makeText(mApplication.getApplicationContext(),
                                    "Fitbit call limit reached.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.i(TAG, "Summary Request Success: " + response.isSuccessful());
                        if (response.isSuccessful()) {
                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                firebaseManager.getFitbitRef().child(date).child("activitySummary").setValue(response.body());
                            }
                            fitbitDailySummary.setActivitySummary(response.body());
                            if (response.body() != null) {
                                Log.i(TAG, response.body().toString());
                            }
                            Log.i(TAG, fitbitDailySummary.getActivitySummary().toString());
                            Call<FitbitSleepSummary> fitbitSleepSummaryCall =
                                    fitbitService.getSleepSummary(accessToken, date);
                            fitbitSleepSummaryCall.enqueue(new Callback<FitbitSleepSummary>() {
                                @Override
                                public void onResponse(Call<FitbitSleepSummary> call,
                                                       Response<FitbitSleepSummary> response) {
                                    if (response.isSuccessful()) {
                                        if (response.body() != null) {
                                            Log.i(TAG, response.body().toString());
                                            fitbitDailySummary.setSleepSummary(response.body());
                                            Log.i(TAG, fitbitDailySummary.getSleepSummary().toString());
                                        }
                                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                            firebaseManager.getFitbitRef().child(date)
                                                    .child("sleep").setValue(response.body());
                                        }
                                        Call<List<FitbitDeviceStatusSummary>> fitbitDeviceStatusCall
                                                = fitbitService.getDeviceStatus(accessToken);
                                        fitbitDeviceStatusCall.enqueue(
                                                new Callback<List<FitbitDeviceStatusSummary>>() {
                                                    @Override
                                                    public void onResponse(Call<List<FitbitDeviceStatusSummary>> call,
                                                                           Response<List<FitbitDeviceStatusSummary>> response) {
                                                        if (response.isSuccessful()) {
                                                            if (response.body() != null) {
                                                                Log.i(TAG, response.body().get(0).toString());
                                                                fitbitDailySummary.setDeviceStatusSummary(response.body().get(0));
                                                                Log.i(TAG, fitbitDailySummary.getDeviceStatusSummary().toString());
                                                                fitbitTaskSuccess.setResult(true);
                                                                insertSuccess(fitbitTaskSuccess);
                                                            }
                                                            if (date.equals(formattedTime.getCurrentDateAsYYYYMMDD())) {
                                                                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                                                    firebaseManager.getFitbitTodayRef().child("device").setValue(response.body());
                                                                }
                                                            }
                                                            insert(fitbitDailySummary, date);
                                                        } else {
                                                            getAccessTokenWithRefreshToken(date);
                                                        }
                                                    }
                                                    @Override
                                                    public void onFailure(Call<List<FitbitDeviceStatusSummary>> call, Throwable t) {
                                                        getAccessTokenWithRefreshToken(date);
                                                    }
                                                });
                                    } else {
                                        Log.i(TAG, response.toString());
                                        getAccessTokenWithRefreshToken(date);
                                    }
                                }
                                @Override
                                public void onFailure(Call<FitbitSleepSummary> call, Throwable t) {
                                    getAccessTokenWithRefreshToken(date);
                                }
                            });
                            //TODO - chain sleep and device status calls here
                        } else {
                            getAccessTokenWithRefreshToken(date);
                        }
                    }

                    @Override
                    public void onFailure(Call<FitbitActivitySummary> call, Throwable t) {
                        Log.i(TAG, t.getMessage());
                        getAccessTokenWithRefreshToken(date);
                    }
                });
            }
        }).start();
    }

    public void sync() {
        firebaseManager.getFitbitRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dateSnap : dataSnapshot.getChildren()) {
                    String date = "";
                    FitbitActivitySummary fitbitActivitySummary = new FitbitActivitySummary();
                    FitbitDeviceStatusSummary deviceStatusSummary = new FitbitDeviceStatusSummary();
                    FitbitSleepSummary sleepSummary = new FitbitSleepSummary();
                    FitbitDailySummary dailySummary = new FitbitDailySummary();
                    if (dateSnap.getKey() != null) {
                        date = dateSnap.getKey();
                    }
                    if (dateSnap.child("activitySummary").hasChildren()) {
                        //Log.i(TAG, "activitySummary node has children");
                        fitbitActivitySummary.setSummary(dateSnap.child("activitySummary").child("summary").getValue(FitbitActivitySummary.Summary.class));
                        fitbitActivitySummary.setGoals(dateSnap.child("activitySummary").child("goals").getValue(FitbitActivitySummary.Goals.class));
                    }
                    if (dateSnap.child("device").hasChildren()) {
                        //Log.i(TAG, "device node has children");
                        deviceStatusSummary.setBattery((String) dateSnap.child("device").child("0").child("battery").getValue());
                        deviceStatusSummary.setDeviceVersion((String) dateSnap.child("device").child("0").child("deviceVersion").getValue());
                        deviceStatusSummary.setLastSyncTime((String) dateSnap.child("device").child("0").child("lastSyncTime").getValue());
                    }
                    if (dateSnap.child("sleep").hasChildren()) {
                        //Log.i(TAG, "sleep node has children");
                        sleepSummary.setSummary(dateSnap.child("sleep").child("summary").getValue(FitbitSleepSummary.Summary.class));
                    }
                    dailySummary.setDate(date);
                    dailySummary.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                    dailySummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    Log.i(TAG, "Synced Date: " + date);
                    dailySummary.setActivitySummary(fitbitActivitySummary);
                    Log.i(TAG, fitbitActivitySummary.toString());
                    dailySummary.setDeviceStatusSummary(deviceStatusSummary);
                    Log.i(TAG, deviceStatusSummary.toString());
                    dailySummary.setSleepSummary(sleepSummary);
                    Log.i(TAG, sleepSummary.toString());
                    insert(dailySummary, date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private void getAccessTokenWithRefreshToken(String date) {

        //Toast.makeText(mApplication, "Refreshing token", Toast.LENGTH_LONG).show();
        String CLIENT_ID = "22D6TC";
        String CLIENT_SECRET = "05f59d5f32a14ecba00cd6c2a58fa7a5";
        String clientCredentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String encodedCodeString = Base64.encodeToString(clientCredentials.getBytes(), Base64.NO_WRAP);
        String authHeader = "Basic " + encodedCodeString;
        String contentType = "application/x-www-form-urlencoded";
        String grantType = "refresh_token";
        String expiresIn = "3600";

        new Thread(new Runnable() {
            @Override
            public void run() {
                Credential credential = credentialRepository.getNow(mApplication.getString(R.string.fitbit_camel_case));
                final String refreshToken = credential.getRefreshToken();
                AsyncTaskBoolean fitbitTaskSuccess = new AsyncTaskBoolean(mApplication.getString(R.string.fitbit_camel_case));
                Call<FitbitAuthCredentialsSummary> fitbitAuthCredentialsSummaryCall =
                        fitbitService.getAccessTokenWithRefreshToken(authHeader, contentType,
                                grantType, refreshToken, expiresIn);
                fitbitAuthCredentialsSummaryCall.enqueue(new Callback<FitbitAuthCredentialsSummary>() {
                    @Override
                    public void onResponse(Call<FitbitAuthCredentialsSummary> call,
                                           Response<FitbitAuthCredentialsSummary> response) {
                        Log.i(TAG, response.toString());
                        Log.i(TAG, "Access token refresh success: " + response.isSuccessful());
                        if (response.isSuccessful()) {
                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                firebaseManager.getFitbitRef().child(date).child("log").child("successResponse")
                                        .setValue(response.toString());
                                firebaseManager.getFitbitRef().child(date).child("log").child("successResponseBody")
                                        .setValue(response.body().toString());
                            }
                            credential.setAccessToken(response.body().getAccess_token());
                            credential.setRefreshToken(response.body().getRefresh_token());
                            credential.setTokenType(response.body().getToken_type());
                            credential.setUserID(response.body().getUser_id());
                            credentialRepository.insert(credential);
                            ConnectedService connectedService = new ConnectedService();
                            connectedService.setName(mApplication.getString(R.string.fitbit_camel_case));
                            connectedService.setConnected(true);
                            connectedService.setType("tracker");
                            dataInUseRepository.insert(connectedService);
                            InputInUse inputInUse = new InputInUse();
                            inputInUse.setType("tracker");
                            inputInUse.setName(mApplication.getString(R.string.fitbit_camel_case));
                            inputInUse.setInUse(true);
                            dataInUseRepository.insert(inputInUse);
                            //Toast.makeText(mApplication, "Refreshing token successful.", Toast.LENGTH_LONG).show();
                            wrtieFileOnInternalStorage(mApplication, "successlog.txt", response.body().toString());
                        } else {
                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                firebaseManager.getFitbitRef().child(date).child("log").child("failureResponse")
                                        .setValue(response.toString());
                                firebaseManager.getFitbitRef().child(date).child("log").child("failureResponseBody")
                                        .setValue(response.body().toString());
                            }
                            InputInUse inputInUse = new InputInUse();
                            inputInUse.setType("tracker");
                            inputInUse.setName(mApplication.getString(R.string.fitbit_camel_case));
                            inputInUse.setInUse(false);
                            dataInUseRepository.insert(inputInUse);
                            ConnectedService connectedService = new ConnectedService();
                            connectedService.setName(mApplication.getString(R.string.fitbit_camel_case));
                            connectedService.setConnected(false);
                            connectedService.setType("tracker");
                            dataInUseRepository.insert(connectedService);
                            fitbitTaskSuccess.setResult(true);
                            insertSuccess(fitbitTaskSuccess);
                            wrtieFileOnInternalStorage(mApplication, "faillog.txt", response.toString() + "\n" + response.body().toString());
                            //Toast.makeText(mApplication, "Refreshing token unsuccessful.", Toast.LENGTH_LONG).show();

                            //TODO - when this happens, show a notification to let the user know something is wrong
                        }
                    }

                    @Override
                    public void onFailure(Call<FitbitAuthCredentialsSummary> call, Throwable t) {
                        Log.i(TAG, t.toString());
                        Toast.makeText(mApplication,  t.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private static class insertAsyncTask extends AsyncTask<FitbitDailySummary, Void, Void> {

        private FitbitDailySummaryDao mAsyncTaskDao;

        insertAsyncTask(FitbitDailySummaryDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final FitbitDailySummary... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private boolean insertSuccess(AsyncTaskBoolean asyncTaskSuccess) {
        AsyncTask.Status status = new insertSuccessAsyncTask(mTaskSuccessDao).execute(asyncTaskSuccess).getStatus();
        return status.equals(AsyncTask.Status.FINISHED);
    }

    private static class insertSuccessAsyncTask extends AsyncTask<AsyncTaskBoolean, Void, Void> {

        private AsyncTaskBooleanDao mAsyncTaskDao;

        insertSuccessAsyncTask(AsyncTaskBooleanDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final AsyncTaskBoolean... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public void wrtieFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
        File file = new File(mcoContext.getFilesDir(),"Moabi");
        if(!file.exists()){
            file.mkdir();
        }
        try{
            File gpxfile = new File(file, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();

        }catch (Exception e){
        }
    }
}
