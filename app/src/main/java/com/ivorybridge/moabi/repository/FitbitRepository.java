package com.ivorybridge.moabi.repository;

import android.app.Application;
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
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.network.auth.FitbitService;
import com.ivorybridge.moabi.util.FormattedTime;

import org.jetbrains.annotations.NotNull;

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
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
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
                        //Log.i(TAG, "Summary Request Success: " + isSuccessful.getValue());
                        if (response.isSuccessful()) {
                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                firebaseManager.getFitbitRef().child(date).child("activitySummary").setValue(response.body());
                                firebaseManager.getFitbitLast30DaysRef().child(date).child("activitySummary").setValue(response.body());
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
                                            firebaseManager.getFitbitLast30DaysRef().child(date)
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
                                                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                                                firebaseManager.getDaysWithDataRef().child(date).child(mApplication.getString(R.string.fitbit_camel_case)).setValue(true);
                                                            }

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

        /*
        firebaseManager.getFitbitCredentialRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FitbitDailySummary fitbitDailySummary = new FitbitDailySummary();
                fitbitDailySummary.setDate(date);
                fitbitDailySummary.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                fitbitDailySummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                AsyncTaskBoolean fitbitTaskSuccess = new AsyncTaskBoolean(mApplication.getString(R.string.fitbit_title));
                if (dataSnapshot.child("access_token").getValue() != null) {
                    final String accessToken = "Bearer "
                            + (String) dataSnapshot.child("access_token").getValue();
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
                            //Log.i(TAG, "Summary Request Success: " + isSuccessful.getValue());
                            if (response.isSuccessful()) {
                                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                    firebaseManager.getFitbitRef().child(date).child("activitySummary").setValue(response.body());
                                    firebaseManager.getFitbitLast30DaysRef().child(date).child("activitySummary").setValue(response.body());
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
                                                firebaseManager.getFitbitLast30DaysRef().child(date)
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
                                                                    firebaseManager.getFitbitTodayRef().child("device").setValue(response.body());
                                                                }
                                                                insert(fitbitDailySummary);
                                                                InputHistory fitbitInput = new InputHistory();
                                                                fitbitInput.setDate(date);
                                                                fitbitInput.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                                                fitbitInput.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                                                                fitbitInput.setInputType(mApplication.getString(R.string.fitbit_title));
                                                                inputHistoryRepository.insert(fitbitInput);
                                                                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                                                    firebaseManager.getDaysWithDataRef().child(date).child(mApplication.getString(R.string.fitbit_title)).setValue(true);
                                                                }

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
                } else {
                    getAccessTokenWithRefreshToken(date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });*/
    }

    /*
    public void downloadDataFromYesterday() {
        firebaseManager.getFitbitIsConnectedRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Boolean isConnected = (Boolean) dataSnapshot.getValue();
                    if (isConnected) {
                        fetchYesterday();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchYesterday() {
        firebaseManager.getFitbitCredentialRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final FitbitDailySummary fitbitDailySummary = new FitbitDailySummary();
                fitbitDailySummary.setDate(formattedTime.getYesterdaysDateAsYYYYMMDD());
                if (dataSnapshot.child("access_token").getValue() != null) {
                    final String accessToken = "Bearer " + (String) dataSnapshot.child("access_token").getValue();
                    Call<FitbitActivitySummary> fitbitDailySummaryCall = fitbitService.getDailySummary(accessToken, formattedTime.getYesterdaysDateAsYYYYMMDD());
                    fitbitDailySummaryCall.enqueue(new Callback<FitbitActivitySummary>() {
                        @Override
                        public void onResponse(@NotNull Call<FitbitActivitySummary> call, @NotNull Response<FitbitActivitySummary> response) {
                            Log.i(TAG, response.toString());
                            if (response.isSuccessful()) {
                                fitbitDailySummary.setActivitySummary(response.body());
                                firebaseManager.getFitbitYesterdayRef().child("activitySummary").setValue(response.body());
                                firebaseManager.getFitbitLast30DaysYesterdayRef().child("activitySummary").setValue(response.body());
                                Call<FitbitSleepSummary> fitbitSleepSummaryCall = fitbitService.getSleepSummary(accessToken, formattedTime.getYesterdaysDateAsYYYYMMDD());
                                fitbitSleepSummaryCall.enqueue(new Callback<FitbitSleepSummary>() {
                                    @Override
                                    public void onResponse(Call<FitbitSleepSummary> call, Response<FitbitSleepSummary> response) {
                                        if (response.isSuccessful()) {
                                            fitbitDailySummary.setSleepSummary(response.body());
                                            firebaseManager.getFitbitYesterdayRef().child("sleep").setValue(response.body());
                                            firebaseManager.getFitbitLast30DaysYesterdayRef().child("sleep").setValue(response.body());
                                            DatabaseReference deviceRef = firebaseManager.getFitbitYesterdayRef().child("device").child("0").getRef();
                                            if (deviceRef != null) {
                                                deviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot != null) {
                                                            FitbitDeviceStatusSummary devices = dataSnapshot.getValue(FitbitDeviceStatusSummary.class);
                                                            fitbitDailySummary.setDeviceStatusSummary(devices);
                                                            insert(fitbitDailySummary);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        } else {
                                            Log.i(TAG, response.toString());
                                            getAccessTokenWithRefreshToken();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<FitbitSleepSummary> call, Throwable t) {
                                        getAccessTokenWithRefreshToken();
                                    }
                                });
                                //TODO - chain sleep and device status calls here
                            } else {
                                getAccessTokenWithRefreshToken();
                            }
                        }

                        @Override
                        public void onFailure(Call<FitbitActivitySummary> call, Throwable t) {
                            Log.i(TAG, t.getMessage());
                            getAccessTokenWithRefreshToken();
                        }
                    });
                } else {
                    getAccessTokenWithRefreshToken();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }*/

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
                    firebaseManager.getDaysWithDataRef().child(date).child(mApplication.getString(R.string.fitbit_title)).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private void getAccessTokenWithRefreshToken(String date) {

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
                Call<FitbitAuthCredentialsSummary> fitbitAuthCredentialsSummaryCall = fitbitService.getAccessTokenWithRefreshToken(authHeader, contentType, grantType, refreshToken, expiresIn);
                fitbitAuthCredentialsSummaryCall.enqueue(new Callback<FitbitAuthCredentialsSummary>() {
                    @Override
                    public void onResponse(Call<FitbitAuthCredentialsSummary> call, Response<FitbitAuthCredentialsSummary> response) {
                        Log.i(TAG, response.toString());
                        Log.i(TAG, "Access token refresh success: " + response.isSuccessful());
                        if (response.isSuccessful()) {
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
                        } else {
                            ConnectedService connectedService = new ConnectedService();
                            connectedService.setName(mApplication.getString(R.string.fitbit_camel_case));
                            connectedService.setConnected(false);
                            connectedService.setType("tracker");
                            dataInUseRepository.insert(connectedService);
                            fitbitTaskSuccess.setResult(true);
                            insertSuccess(fitbitTaskSuccess);
                            //TODO - when this happens, show a notification to let the user know something is wrong
                        }
                    }

                    @Override
                    public void onFailure(Call<FitbitAuthCredentialsSummary> call, Throwable t) {
                        Log.i(TAG, t.toString());
                    }
                });
            }
        }).start();
        /*
        firebaseManager.getFitbitCredentialRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey() != null) {
                    if (dataSnapshot.child("refresh_token").getValue() != null) {
                        String refreshToken = "";
                        AsyncTaskBoolean fitbitTaskSuccess = new AsyncTaskBoolean(mApplication.getString(R.string.fitbit_camel_case));
                        refreshToken = (String) dataSnapshot.child("refresh_token").getValue();
                        Call<FitbitAuthCredentialsSummary> fitbitAuthCredentialsSummaryCall = fitbitService.getAccessTokenWithRefreshToken(authHeader, contentType, grantType, refreshToken, expiresIn);
                        fitbitAuthCredentialsSummaryCall.enqueue(new Callback<FitbitAuthCredentialsSummary>() {
                            @Override
                            public void onResponse(Call<FitbitAuthCredentialsSummary> call, Response<FitbitAuthCredentialsSummary> response) {
                                Log.i(TAG, response.toString());
                                Log.i(TAG, "Access token refresh success: " + response.isSuccessful());
                                if (response.isSuccessful()) {
                                    firebaseManager.getFitbitCredentialRef().setValue(response.body())
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    AsyncTaskBoolean fitbitTaskSuccess = new AsyncTaskBoolean(mApplication.getString(R.string.fitbit_title));
                                                    fitbitTaskSuccess.setResult(true);
                                                    insertSuccess(fitbitTaskSuccess);
                                                    firebaseManager.getIsConnectedRef().child(mApplication.getString(R.string.fitbit_title)).setValue(false);
                                                }
                                            })
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    firebaseManager.getIsConnectedRef().child(mApplication.getString(R.string.fitbit_title)).setValue(true);
                                                    downloadData(date);
                                                }
                                            });
                                } else {
                                    firebaseManager.getIsConnectedRef().child(mApplication.getString(R.string.fitbit_title)).setValue(false);
                                    fitbitTaskSuccess.setResult(true);
                                    insertSuccess(fitbitTaskSuccess);
                                    //TODO - when this happens, show a notification to let the user know something is wrong
                                }
                            }

                            @Override
                            public void onFailure(Call<FitbitAuthCredentialsSummary> call, Throwable t) {
                                Log.i(TAG, t.toString());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });*/
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
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
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
}
