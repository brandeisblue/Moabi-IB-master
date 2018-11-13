package com.ivorybridge.moabi.network.auth;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.GoalsReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.AsyncTaskBooleanDao;
import com.ivorybridge.moabi.database.dao.GoogleFitDao;
import com.ivorybridge.moabi.database.db.AsyncTaskBooleanDB;
import com.ivorybridge.moabi.database.db.GoogleFitDB;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitDataTypeClassifier;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitGoal;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;
import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.InputHistory;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.DataInUseRepository;
import com.ivorybridge.moabi.repository.InputHistoryRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

public class GoogleFitService {

    private Context context;
    private AppCompatActivity mActivity;
    private static final String TAG = GoogleFitService.class.getSimpleName();
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 6;
    private FirebaseManager firebaseManager;
    private FitnessOptions mFitnessOptions;
    private AsyncTaskBooleanDao mTaskSuccessDao;
    private GoogleFitDao mGoogleFitDao;
    private InputHistoryRepository inputHistoryRepository;
    private DataInUseRepository dataInUseRepository;
    private FormattedTime formattedTime;
    private Application application;

    public GoogleFitService(AppCompatActivity activity) {
        GoogleFitDB db = GoogleFitDB.getDatabase(activity.getApplication());
        AsyncTaskBooleanDB successDB = AsyncTaskBooleanDB.getDatabase(activity.getApplication());
        mFitnessOptions = configureFitnessOptions();
        this.context = activity.getApplicationContext();
        mActivity = activity;
        mFitnessOptions = this.configureFitnessOptions();
        firebaseManager = new FirebaseManager();
        mGoogleFitDao = db.googleFitDao();
        mTaskSuccessDao = successDB.asyncTaskBooleanDao();
        inputHistoryRepository = new InputHistoryRepository(activity.getApplication());
        dataInUseRepository = new DataInUseRepository(activity.getApplication());
        formattedTime = new FormattedTime();
        this.application = activity.getApplication();
    }

    public GoogleFitService(Application application) {
        GoogleFitDB db = GoogleFitDB.getDatabase(application);
        AsyncTaskBooleanDB successDB = AsyncTaskBooleanDB.getDatabase(application);
        mFitnessOptions = configureFitnessOptions();
        this.context = application;
        mFitnessOptions = this.configureFitnessOptions();
        firebaseManager = new FirebaseManager();
        mGoogleFitDao = db.googleFitDao();
        mTaskSuccessDao = successDB.asyncTaskBooleanDao();
        inputHistoryRepository = new InputHistoryRepository(application);
        dataInUseRepository = new DataInUseRepository(application);
        formattedTime = new FormattedTime();
        this.application = application;
    }

    public Boolean hasPermission() {
        if (GoogleSignIn.getLastSignedInAccount(context) != null) {
            return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(context), mFitnessOptions);
        }
        return false;
    }

    public Boolean revokePermission() {
        if (GoogleSignIn.getLastSignedInAccount(context) != null) {
            Fitness.getConfigClient(context, GoogleSignIn.getLastSignedInAccount(context)).disableFit();
            //firebaseManager.getConnectedServicesRef().child(context.context.getString(R.string.googlefit_camel_case)).child("isConnected").setValue(false);
            InputInUse inputInUse = new InputInUse();
            inputInUse.setType("tracker");
            inputInUse.setName(context.getString(R.string.googlefit_camel_case));
            inputInUse.setInUse(false);
            dataInUseRepository.insert(inputInUse);
            ConnectedService connectedService = new ConnectedService();
            connectedService.setType("tracker");
            connectedService.setName(context.getString(R.string.googlefit_camel_case));
            connectedService.setConnected(false);
            dataInUseRepository.insert(connectedService);
            return true;
        }
        return false;
    }

    public void requestPermission() {
        GoogleSignIn.requestPermissions(
                mActivity, // your activity
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(context),
                mFitnessOptions);
    }

    public void downloadData(String date) {
        // start by reading in goals
        GoogleSignInOptionsExtension fitnessOptions = configureFitnessOptions();
        GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(application, fitnessOptions);
        final Map<String, Object> googleFitGoals = new LinkedHashMap<>();
        final List<GoogleFitGoal> goalsList = new ArrayList<>();
        Fitness.getGoalsClient(application, gsa)
                .readCurrentGoals(
                        new GoalsReadRequest.Builder()
                                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
                                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                .addDataType(DataType.TYPE_DISTANCE_DELTA)
                                .addDataType(DataType.AGGREGATE_DISTANCE_DELTA)
                                .addDataType(DataType.TYPE_DISTANCE_CUMULATIVE)
                                .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED)
                                .addDataType(DataType.TYPE_LOCATION_SAMPLE)
                                .addDataType(DataType.TYPE_ACTIVITY_SAMPLES)
                                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                                .addDataType(DataType.TYPE_SPEED)
                                .addDataType(DataType.TYPE_NUTRITION)
                                .build())
                .addOnSuccessListener(new OnSuccessListener<List<Goal>>() {
                    @Override
                    public void onSuccess(List<Goal> goals) {
                        //Log.i(TAG, "Goals: " + goals.toString());
                        for (Goal goal : goals) {
                            Log.i(TAG, "Goal: " + goal.toString());
                            // take care of cases where activities have assigned names
                            // these are cases where they are fitness activites like running, yoga, basketball
                            String goalName = "";
                            int goalUnitInt = 2;
                            int goalObjectiveType;
                            String goalUnit = "";
                            Long goalFreq = 1L;
                            String goalString = "";

                            if (goal.getActivityName() != null) {
                                goalName = goal.getActivityName();
                            }

                            goalObjectiveType = goal.getObjectiveType();
                            switch (goalObjectiveType) {
                                case 1:
                                    goalString = String.valueOf(goal.getMetricObjective().getValue());
                                    if (goalName.isEmpty()) {
                                        GoogleFitDataTypeClassifier gfClassifier = new GoogleFitDataTypeClassifier(goal.getMetricObjective().getDataTypeName());
                                        goalName = gfClassifier.getDataType();
                                    }
                                    break;
                                case 2:
                                    goalString = String.valueOf(goal.getDurationObjective().getDuration(TimeUnit.MILLISECONDS));
                                    if (goalName.isEmpty()) {
                                        goalName = context.getString(R.string.activity_active_minutes_camel_case);
                                    }
                                    break;
                                case 3:
                                    goalString = String.valueOf(goal.getFrequencyObjective().getFrequency());
                                    break;
                                default:
                                    break;
                            }
                            if (goal.getRecurrence() != null) {
                                goalUnitInt = goal.getRecurrence().getUnit();
                                switch (goalUnitInt) {
                                    case 1:
                                        goalUnit = "daily";
                                        break;
                                    case 2:
                                        goalUnit = "weekly";
                                        break;
                                    case 3:
                                        goalUnit = "monthly";
                                        break;
                                    default:
                                        goalUnit = "weekly";
                                        break;
                                }
                                goalFreq = (long) goal.getRecurrence().getCount();
                            }
                            //Log.i(TAG, goal.toString());
                            //Log.i(TAG, goalName + ": " + goalString + " " + goalUnit);\

                            GoogleFitGoal fitGoal = new GoogleFitGoal(goalName, goalUnit, 1L, goalString);
                            Log.i(TAG, fitGoal.toString());
                            goalsList.add(fitGoal);
                            if (googleFitGoals.get(goalName) != null) {
                                googleFitGoals.put(goalName + "1", fitGoal);
                            } else {
                                googleFitGoals.put(goalName, fitGoal);
                            }
                        }
                        GoogleFitSummary fitSummary = new GoogleFitSummary();
                        fitSummary.setDate(date);
                        fitSummary.setGoals(goalsList);
                        getSummary(fitSummary, date);
                        //firebaseManager.getGoogleFitGoalsRef().updateChildren(googleFitGoals);
                    }
                });
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    public void getSummary(final GoogleFitSummary fitSummary, String date) {
        /*
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startTime = cal.getTimeInMillis();*/

        AsyncTaskBoolean gFitTaskBoolean= new AsyncTaskBoolean(context.getString(R.string.googlefit_camel_case));
        gFitTaskBoolean.setResult(false);
        insertSuccess(gFitTaskBoolean);
        DataReadRequest todayRequest =
                new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                        .aggregate(DataType.TYPE_LOCATION_SAMPLE, DataType.AGGREGATE_LOCATION_BOUNDING_BOX)
                        .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                        .aggregate(DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
                        .aggregate(DataType.TYPE_NUTRITION, DataType.AGGREGATE_NUTRITION_SUMMARY)
                        .aggregate(DataType.TYPE_WEIGHT, DataType.AGGREGATE_WEIGHT_SUMMARY)
                        .aggregate(DataType.TYPE_HYDRATION, DataType.AGGREGATE_HYDRATION)
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                        .read(DataType.TYPE_DISTANCE_CUMULATIVE)
                        //.aggregate(DataType.TYPE_DISTANCE_CUMULATIVE, DataType.AGGREGATE_DISTANCE_DELTA)
                        .bucketByActivityType(1, TimeUnit.SECONDS)
                        .setTimeRange(formattedTime.getStartOfDay(date), formattedTime.getEndOfDay(date), TimeUnit.MILLISECONDS)
                        .build();

        final Map<String, Object> activityMap = new LinkedHashMap<>();
        if (GoogleSignIn.getLastSignedInAccount(context) != null) {
            Fitness.getHistoryClient(application, GoogleSignIn.getLastSignedInAccount(context))
                    .readData(todayRequest)
                    .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse dataReadResponse) {
                            Integer steps = 0;
                            Float distance = 0f;
                            Float calories = 0f;
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US);
                            if (dataReadResponse.getBuckets().size() > 0) {
                                for (Bucket bucket : dataReadResponse.getBuckets()) {
                                    List<DataSet> dataSets = bucket.getDataSets();
                                    for (DataSet dataSet : dataSets) {
                                        for (DataPoint dp : dataSet.getDataPoints()) {
                                            //Log.i(TAG, "Data point:");
                                            //Log.i(TAG, "\tType: " + dp.getDataType().getName());
                                            //Log.i(TAG, "\tStart: " + formatter.format(new Date(dp.getStartTime(TimeUnit.MILLISECONDS))));
                                            //Log.i(TAG, "\tEnd: " + formatter.format(new Date(dp.getEndTime(TimeUnit.MILLISECONDS))));
                                            for (Field field : dp.getDataType().getFields()) {
                                                //Log.i(TAG, "\tField: " + field.getName() + ", Value: " + dp.getValue(field));
                                                if (activityMap.get(field.getName()) != null) {
                                                    if (field.getName().equals(context.getString(R.string.activity_steps_camel_case))) {
                                                        steps = dp.getValue(field).asInt();
                                                        steps += (Integer) activityMap.get(field.getName());
                                                        activityMap.put(field.getName(), steps);
                                                    } else if (field.getName().equals(context.getString(R.string.activity_calories_camel_case))) {
                                                        calories = dp.getValue(field).asFloat();
                                                        calories += (Float) activityMap.get(field.getName());
                                                        activityMap.put(field.getName(), calories);
                                                    } else if (field.getName().equals(context.getString(R.string.activity_distance_camel_case))) {
                                                        distance = dp.getValue(field).asFloat();
                                                        distance += (Float) activityMap.get(field.getName());
                                                        activityMap.put(field.getName(), distance);
                                                    }
                                                } else {
                                                    if (field.getName().equals(context.getString(R.string.activity_steps_camel_case))) {
                                                        steps = dp.getValue(field).asInt();
                                                        activityMap.put(field.getName(), steps);
                                                    } else if (field.getName().equals(context.getString(R.string.activity_calories_camel_case))) {
                                                        calories = dp.getValue(field).asFloat();
                                                        activityMap.put(field.getName(), calories);
                                                    } else if (field.getName().equals(context.getString(R.string.activity_distance_camel_case))) {
                                                        distance = dp.getValue(field).asFloat();
                                                        activityMap.put(field.getName(), distance);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (activityMap.get(context.getString(R.string.activity_steps_camel_case)) == null) {
                                activityMap.put(context.getString(R.string.activity_steps_camel_case), 0);
                            }
                            if (activityMap.get(context.getString(R.string.activity_distance_camel_case)) == null) {
                                activityMap.put(context.getString(R.string.activity_distance_camel_case), 0);
                            }
                            if (activityMap.get(context.getString(R.string.activity_calories_camel_case)) == null) {
                                activityMap.put(context.getString(R.string.activity_calories_camel_case), 0);
                            }
                            //activityMap.put("lastSync", getCurrentTime());
                            List<GoogleFitSummary.Summary> summaries = new ArrayList<>();
                            for (Map.Entry<String, Object> entry : activityMap.entrySet()) {
                                if (entry.getValue() instanceof Integer) {
                                    Integer value = (Integer) entry.getValue();
                                    summaries.add(new GoogleFitSummary.Summary(entry.getKey(), value.doubleValue()));
                                } else if (entry.getValue() instanceof Float) {
                                    Float value = (Float) entry.getValue();
                                    summaries.add(new GoogleFitSummary.Summary(entry.getKey(), value.doubleValue()));
                                }
                            }
                            Log.i(TAG, activityMap.toString());
                            getActiveMinutes(fitSummary, summaries, date);
                            //firebaseManager.getGoogleFitRef().child(date).child("summary").updateChildren(activityMap);
                            //firebaseManager.getGoogleFitRef().child(date).child("lastSync").setValue(formattedTime.getCurrentTimeAsHMMA());
                            //firebaseManager.getGoogleFitLast30DaysRef().child(date).child("summary").updateChildren(activityMap);
                            //firebaseManager.getDaysWithDataRef().child(date).child(context.getString(R.string.googlefit_camel_case)).setValue(true);
                            //TODO - change the today call code so that update can be done once.
                        }
                    });
        }
    }

    private void getActiveMinutes(final GoogleFitSummary fitSummary,
                                        final List<GoogleFitSummary.Summary> summaries, String date) {

        final Map<String, Object> timePerActivity = new LinkedHashMap<>();
        // Begin by creating the query.
        DataReadRequest activityRequest = queryActivityMinutes(date);
        if (GoogleSignIn.getLastSignedInAccount(context) != null) {
            Fitness.getHistoryClient(application, GoogleSignIn.getLastSignedInAccount(context))
                    .readData(activityRequest)
                    .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse dataReadResponse) {
                            if (dataReadResponse.getBuckets().size() > 0) {
                                for (Bucket bucket : dataReadResponse.getBuckets()) {
                                    long activeTime = bucket.getEndTime(TimeUnit.MILLISECONDS) - bucket.getStartTime(TimeUnit.MILLISECONDS);
                                    if (timePerActivity.get(bucket.getActivity()) != null) {
                                        Long minutes = (Long) timePerActivity.get(bucket.getActivity());
                                        Long updatedMinutes = minutes + activeTime;
                                        //Log.i(TAG, bucket.getActivity() + ": " + updatedMinutes);
                                        timePerActivity.put(bucket.getActivity(), updatedMinutes);
                                    } else {
                                        timePerActivity.put(bucket.getActivity(), activeTime);
                                    }
                                /*
                                switch (bucket.getActivity()) {
                                    case "walking":
                                        Log.i(TAG, "Walking: " + Long.toString(activeTime));
                                        break;
                                    case "running":
                                        Log.i(TAG, "Running: " + Long.toString(activeTime));
                                        break;
                                    case "biking":
                                        Log.i(TAG, "Biking: " + Long.toString(activeTime));
                                        break;
                                }*/
                                }
                                Long totalActiveMins = 0L;
                                Long totalSedentaryMins = 0L;
                                for (Map.Entry<String, Object> entry : timePerActivity.entrySet()) {
                                    if (entry.getKey() != null && !entry.getKey().equals("still") && !entry.getKey().equals("unknown")) {
                                        totalActiveMins += (long) entry.getValue();
                                    } else if (entry.getKey() != null && entry.getKey().equals("still")) {
                                        totalSedentaryMins += (long) entry.getValue();
                                    }
                                }
                                summaries.add(new GoogleFitSummary.Summary(context.getString(R.string.activity_active_minutes_camel_case), totalActiveMins.doubleValue()));
                                summaries.add(new GoogleFitSummary.Summary(context.getString(R.string.activity_sedentary_minutes_camel_case), totalSedentaryMins.doubleValue()));
                                fitSummary.setSummaries(summaries);
                                fitSummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                fitSummary.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                                fitSummary.setLastSyncTime(formattedTime.getCurrentTimeAsHMMA());
                                insert(fitSummary, date);
                                AsyncTaskBoolean gFitTaskSuccess = new AsyncTaskBoolean(context.getString(R.string.googlefit_camel_case));
                                gFitTaskSuccess.setResult(true);
                                insertSuccess(gFitTaskSuccess);
                                Log.i(TAG, timePerActivity.toString());
                                //firebaseManager.getGoogleFitRef().child(date).child("summary").child(context.getString(R.string.activity_active_minutes_camel_case)).updateChildren(timePerActivity);
                                //firebaseManager.getGoogleFitLast30DaysRef().child(date).child("summary").child(context.getString(R.string.activity_active_minutes_camel_case)).updateChildren(timePerActivity);
                                //firebaseManager.getDaysWithDataRef().child(date).child(context.getString(R.string.googlefit_camel_case)).setValue(true);
                            }
                        }
                    });
        }
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    public void readDataFromYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();
        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.DATE, -1);
        calendar1.set(Calendar.HOUR_OF_DAY, 23);
        calendar1.set(Calendar.MINUTE, 59);
        calendar1.set(Calendar.SECOND, 59);
        calendar1.set(Calendar.MILLISECOND, 999);
        long endTime = calendar1.getTimeInMillis();

        DataReadRequest yesterdayRequest =
                new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                        .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                        .bucketByActivityType(1, TimeUnit.SECONDS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();

        final Map<String, Object> activityMap = new LinkedHashMap<>();
        if (GoogleSignIn.getLastSignedInAccount(context) != null) {
            Fitness.getHistoryClient(application, GoogleSignIn.getLastSignedInAccount(context))
                    .readData(yesterdayRequest)
                    .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse dataReadResponse) {
                            Integer steps = 0;
                            Float distance = 0f;
                            Float calories = 0f;
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US);
                            if (dataReadResponse.getBuckets().size() > 0) {
                                for (Bucket bucket : dataReadResponse.getBuckets()) {
                                    List<DataSet> dataSets = bucket.getDataSets();
                                    for (DataSet dataSet : dataSets) {
                                        for (DataPoint dp : dataSet.getDataPoints()) {
                                        /*
                                        Log.i(TAG, "Data point:");
                                        Log.i(TAG, "\tType: " + dp.getDataType().getName());
                                        Log.i(TAG, "\tStart: " + formatter.format(new Date(dp.getStartTime(TimeUnit.MILLISECONDS))));
                                        Log.i(TAG, "\tEnd: " + formatter.format(new Date(dp.getEndTime(TimeUnit.MILLISECONDS))));*/
                                            for (Field field : dp.getDataType().getFields()) {
                                                //Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                                                if (activityMap.get(field.getName()) != null) {
                                                    if (field.getName().equals(context.getString(R.string.activity_steps_camel_case))) {
                                                        steps = dp.getValue(field).asInt();
                                                        steps += (Integer) activityMap.get(field.getName());
                                                        activityMap.put(field.getName(), steps);
                                                    } else if (field.getName().equals(context.getString(R.string.activity_calories_camel_case))) {
                                                        calories = dp.getValue(field).asFloat();
                                                        calories += (Float) activityMap.get(field.getName());
                                                        activityMap.put(field.getName(), calories);
                                                    } else if (field.getName().equals(context.getString(R.string.activity_distance_camel_case))) {
                                                        distance = dp.getValue(field).asFloat();
                                                        distance += (Float) activityMap.get(field.getName());
                                                        activityMap.put(field.getName(), distance);
                                                    }
                                                } else {
                                                    if (field.getName().equals(context.getString(R.string.activity_steps_camel_case))) {
                                                        steps = dp.getValue(field).asInt();
                                                        activityMap.put(field.getName(), steps);
                                                    } else if (field.getName().equals(context.getString(R.string.activity_calories_camel_case))) {
                                                        calories = dp.getValue(field).asFloat();
                                                        activityMap.put(field.getName(), calories);
                                                    } else if (field.getName().equals(context.getString(R.string.activity_distance_camel_case))) {
                                                        distance = dp.getValue(field).asFloat();
                                                        activityMap.put(field.getName(), distance);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Log.i(TAG, activityMap.toString());
                            //firebaseManager.getGoogleFitYesterdayRef().child("summary").updateChildren(activityMap);
                            //firebaseManager.getGoogleFitLast30DaysRef().child(setUpDateForYesterday()).child("summary").updateChildren(activityMap);
                            //TODO - change the today call code so that update can be done once.
                        }
                    });
        }

        final Map<String, Object> timePerActivity = new LinkedHashMap<>();
        // Begin by creating the query.
        DataReadRequest activityRequest = queryActivityMinutesYesterday();
        if (GoogleSignIn.getLastSignedInAccount(context) != null) {
            Fitness.getHistoryClient(application, GoogleSignIn.getLastSignedInAccount(context))
                    .readData(activityRequest)
                    .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse dataReadResponse) {
                            if (dataReadResponse.getBuckets().size() > 0) {
                                for (Bucket bucket : dataReadResponse.getBuckets()) {
                                    long activeTime = bucket.getEndTime(TimeUnit.MILLISECONDS) - bucket.getStartTime(TimeUnit.MILLISECONDS);
                                    if (timePerActivity.get(bucket.getActivity()) != null) {
                                        Long minutes = (Long) timePerActivity.get(bucket.getActivity());
                                        Long updatedMinutes = minutes + activeTime;
                                        timePerActivity.put(bucket.getActivity(), updatedMinutes);
                                    } else {
                                        timePerActivity.put(bucket.getActivity(), activeTime);
                                    }
                                }
                                Log.i(TAG, timePerActivity.toString());
                                //firebaseManager.getGoogleFitYesterdayRef().child(context.getString(R.string.activity_active_minutes_camel_case)).updateChildren(timePerActivity);
                                //firebaseManager.getGoogleFitLast30DaysRef().child(setUpDateForYesterday()).child("summary").child(context.getString(R.string.activity_active_minutes_camel_case)).updateChildren(timePerActivity);
                            }
                        }
                    });
        }
    }

    public void getSubscriptionsList() {
        // [START list_current_subscriptions]
        if (GoogleSignIn.getLastSignedInAccount(context) != null) {
            Fitness.getRecordingClient(application, GoogleSignIn.getLastSignedInAccount(context))
                    .listSubscriptions()
                    .addOnSuccessListener(new OnSuccessListener<List<Subscription>>() {
                        @Override
                        public void onSuccess(List<Subscription> subscriptions) {
                            for (Subscription sc : subscriptions) {
                                DataType dt = sc.getDataType();
                                if (dt != null) {
                                    Log.i(TAG, "Active subscription for data type: " + dt.getName());
                                }
                            }
                        }
                    });
        }
    }

    private DataReadRequest queryActivityMinutes(String date) {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        //long endTime = cal.getTimeInMillis();
        long endTime = formattedTime.getEndOfDay(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        //long startTime = cal.getTimeInMillis();
        long startTime = formattedTime.getStartOfDay(date);

        DataSource ACTIVITY_SEGMENT = new DataSource.Builder()
                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_activity_segment")
                .setAppPackageName("com.google.android.gms")
                .build();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .bucketByActivitySegment(1, TimeUnit.SECONDS)
                //.bucketByActivitySegment(5, TimeUnit.MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
    }

    public static DataReadRequest queryActivityMinutesYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();
        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.DATE, -1);
        calendar1.set(Calendar.HOUR_OF_DAY, 23);
        calendar1.set(Calendar.MINUTE, 59);
        calendar1.set(Calendar.SECOND, 59);
        calendar1.set(Calendar.MILLISECOND, 999);
        long endTime = calendar1.getTimeInMillis();

        DataSource ACTIVITY_SEGMENT = new DataSource.Builder()
                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_activity_segment")
                .setAppPackageName("com.google.android.gms")
                .build();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .bucketByActivitySegment(1, TimeUnit.SECONDS)
                //.bucketByActivitySegment(5, TimeUnit.MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
    }


    private boolean insert(GoogleFitSummary dailySummary, String date) {
        InputHistory googleFitInputHistory = new InputHistory();
        googleFitInputHistory.setInputType(context.getString(R.string.googlefit_camel_case));
        googleFitInputHistory.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
        googleFitInputHistory.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
        googleFitInputHistory.setDate(date);
        inputHistoryRepository.insert(googleFitInputHistory);
        AsyncTask.Status status = new GoogleFitService.insertAsyncTask(mGoogleFitDao).execute(dailySummary).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertAsyncTask extends AsyncTask<GoogleFitSummary, Void, Void> {

        private GoogleFitDao mAsyncTaskDao;

        insertAsyncTask(GoogleFitDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final GoogleFitSummary... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private boolean insertSuccess(AsyncTaskBoolean taskBoolean) {
        AsyncTask.Status status = new insertSuccessAsyncTask(mTaskSuccessDao).execute(taskBoolean).getStatus();
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

    // get daily total of step, distance, active minutes
    private FitnessOptions configureFitnessOptions() {
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_DISTANCE_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_DISTANCE_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_ACTIVITY_SAMPLES, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_ACTIVITY_SUMMARY, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_LOCATION_BOUNDING_BOX, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_SPEED_SUMMARY, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_SPEED_SUMMARY, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_NUTRITION_SUMMARY, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_NUTRITION, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_WEIGHT_SUMMARY, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_HEIGHT_SUMMARY, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_HYDRATION, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_HYDRATION, FitnessOptions.ACCESS_WRITE)
                        .build();
        return fitnessOptions;
    }

    private String getDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        String format = "EEE MMM dd HH:mm:ss z yyyy";
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
        return df.format(c.getTime());
    }

    private String setUpDateForYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return mdformat.format(calendar.getTime());
    }

    private String setUpDatesForToday() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return mdformat.format(calendar.getTime());
    }
}
