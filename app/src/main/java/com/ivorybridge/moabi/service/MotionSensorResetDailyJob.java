package com.ivorybridge.moabi.service;

import android.app.Application;
import android.content.Intent;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class MotionSensorResetDailyJob extends DailyJob {

    public static final String TAG = "motion_sensor_daily_job";
    private Application application;
    private FirebaseManager firebaseManager;

    public MotionSensorResetDailyJob(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {
        BuiltInFitnessRepository builtInFitnessRepository = new BuiltInFitnessRepository(application);
        FormattedTime formattedTime = new FormattedTime();
        BuiltInActivitySummary activitySummary = new BuiltInActivitySummary();
        activitySummary.setSteps(0L);
        activitySummary.setDistance(0d);
        activitySummary.setActiveMinutes(0L);
        activitySummary.setSedentaryMinutes(0L);
        activitySummary.setCalories(0d);
        activitySummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
        activitySummary.setLastSensorTimeStamp(formattedTime.getCurrentTimeInMilliSecs());
        activitySummary.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
        activitySummary.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
        builtInFitnessRepository.insert(activitySummary, formattedTime.getCurrentDateAsYYYYMMDD());
        firebaseManager = new FirebaseManager();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseManager.getConnectedServicesRef().child(application.getString(R.string.moabi_tracker_camel_case))
                    .child(formattedTime.getCurrentDateAsYYYYMMDD())
                    .setValue(activitySummary);
        }
        application.stopService(new Intent(application, MotionSensorService.class));
        application.startService(new Intent(application, MotionSensorService.class));
        return DailyJobResult.SUCCESS;
    }

    public static void scheduleJob() {
        DailyJob.schedule(new JobRequest.Builder(TAG),
                TimeUnit.HOURS.toMillis(0),
                TimeUnit.HOURS.toMillis(0) + TimeUnit.SECONDS.toMillis(5));
    }
}
