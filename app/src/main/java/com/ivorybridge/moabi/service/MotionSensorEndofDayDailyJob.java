package com.ivorybridge.moabi.service;

import android.app.Application;
import android.content.Intent;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInProfile;
import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.AsyncCallsMasterRepository;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.repository.DataInUseRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class MotionSensorEndofDayDailyJob extends DailyJob {

    public static final String TAG = "motion_sensor_end_of_daydaily_job";
    private Application application;
    private FirebaseManager firebaseManager;
    private FormattedTime formattedTime;

    public MotionSensorEndofDayDailyJob(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {
        formattedTime = new FormattedTime();
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataInUseRepository dataInUseRepository = new DataInUseRepository(application);
                List<InputInUse> inputInUseList = dataInUseRepository.getAllInputsInUseNow();
                List<ConnectedService> connectedServiceList = dataInUseRepository.getAllConnectedServicesNow();
                if (inputInUseList != null && inputInUseList.size() > 0 &&
                        connectedServiceList != null && connectedServiceList.size() > 0) {
                    for (InputInUse inputInUse: inputInUseList) {
                        if (inputInUse.isInUse()) {
                            for (ConnectedService connectedService: connectedServiceList) {
                                if (connectedService.getName().equals(inputInUse.getName()) &&
                                        connectedService.isConnected()) {
                                    if (connectedService.getName().equals(application.getString(R.string.fitbit_camel_case))) {
                                        AsyncCallsMasterRepository asyncCallsMasterRepository = new AsyncCallsMasterRepository(application, formattedTime.getCurrentDateAsYYYYMMDD());
                                        asyncCallsMasterRepository.makeCallsToConnectedServices();
                                    } else if (connectedService.getName().equals(application.getString(R.string.googlefit_camel_case))) {
                                        AsyncCallsMasterRepository asyncCallsMasterRepository = new AsyncCallsMasterRepository(application, formattedTime.getCurrentDateAsYYYYMMDD());
                                        asyncCallsMasterRepository.makeCallsToConnectedServices();
                                    } else if (connectedService.getName().equals(application.getString(R.string.moabi_tracker_camel_case))) {
                                        BuiltInFitnessRepository builtInFitnessRepository = new BuiltInFitnessRepository(application);
                                        FormattedTime formattedTime = new FormattedTime();
                                        List<BuiltInProfile> builtInProfiles = builtInFitnessRepository.getUserProfileNow();
                                        double bmr = 1577.5;
                                        double distance = 0;
                                        double calories = 0;
                                        long activeMins = 0;
                                        long sedentaryMins = 0;
                                        if (builtInProfiles != null && builtInProfiles.size() > 0) {
                                            BuiltInProfile profile = builtInProfiles.get(0);
                                            if (profile.getHeight() != null) {
                                                if (profile.getGender() == null) {
                                                    bmr = 1577.5;
                                                } else if (profile.getGender().equals(application.getString(R.string.profile_sex_male))) {
                                                    bmr = profile.getWeight() * 10 + 6.25 * profile.getHeight();
                                                    if (profile.getAge() != null) {
                                                        bmr = bmr - 5 * profile.getAge() + 5;
                                                    }
                                                } else {
                                                    bmr = profile.getWeight() * 10 + 6.25 * profile.getHeight();
                                                    if (profile.getAge() != null) {
                                                        bmr = bmr - 5 * profile.getAge() - 161;
                                                    }
                                                }
                                            }
                                            BuiltInActivitySummary activitySummary = builtInFitnessRepository.getNow(formattedTime.getCurrentDateAsYYYYMMDD());
                                            if (activitySummary != null) {
                                                long steps = activitySummary.getSteps();
                                                activeMins = activitySummary.getActiveMinutes();
                                                sedentaryMins = activitySummary.getSedentaryMinutes();
                                                if (profile.getHeight() != null) {
                                                    if (profile.getGender() == null) {
                                                        distance = steps * (profile.getHeight() * 0.414) / 100;
                                                    } else if (profile.getGender().equals(application.getString(R.string.profile_sex_male))) {
                                                        distance = steps * profile.getHeight() * 0.415 / 100;
                                                    } else {
                                                        distance = steps * profile.getHeight() * 0.413 / 100;
                                                    }
                                                } else {
                                                    distance = steps * 0.762;
                                                }
                                                calories = activitySummary.getCalories();
                                                double elapsedTime = formattedTime.getCurrentTimeInMilliSecs() - formattedTime.getStartOfDay(formattedTime.getCurrentDateAsYYYYMMDD());
                                                calories = (double) steps / 1000 * 40 + bmr * (elapsedTime / 86400000);
                                                sedentaryMins = formattedTime.getCurrentTimeInMilliSecs() - formattedTime.getStartOfDay(formattedTime.getCurrentDateAsYYYYMMDD()) - activeMins;
                                                activitySummary.setCalories(calories);
                                                activitySummary.setSedentaryMinutes(sedentaryMins);
                                                activitySummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
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
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                application.stopService(new Intent(application, MotionSensorService.class));
                application.startService(new Intent(application, MotionSensorService.class));
            }
        }).start();
        return DailyJobResult.SUCCESS;
    }

    public static void scheduleJob() {
        DailyJob.schedule(new JobRequest.Builder(TAG), TimeUnit.HOURS.toMillis(23)+TimeUnit.MINUTES.toMillis(55)+TimeUnit.SECONDS.toMillis(0),
                TimeUnit.HOURS.toMillis(23)+TimeUnit.MINUTES.toMillis(59)+TimeUnit.SECONDS.toMillis(0));
    }
}
