package com.ivorybridge.moabi.service;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.appusage.AppUsage;
import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitActivitySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitSleepSummary;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.database.entity.util.UserGoal;
import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;
import com.ivorybridge.moabi.repository.AppUsageRepository;
import com.ivorybridge.moabi.repository.BAActivityRepository;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.repository.FitbitRepository;
import com.ivorybridge.moabi.repository.GoogleFitRepository;
import com.ivorybridge.moabi.repository.TimedActivityRepository;
import com.ivorybridge.moabi.repository.UserGoalRepository;
import com.ivorybridge.moabi.repository.WeatherRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;
import java.util.concurrent.TimeUnit;

//TODO - figure out why progress tracking doesn't work when outside (except Moabi)
public class UserGoalJob extends Job {

    public static final String TAG = "user_goal_job";
    private FormattedTime formattedTime;
    private Application application;
    private Long yesterday;
    private FitbitRepository fitbitRepository;
    private GoogleFitRepository googleFitRepository;
    private AppUsageRepository appUsageRepository;
    private BuiltInFitnessRepository builtInFitnessRepository;
    private BAActivityRepository baActivityRepository;
    private TimedActivityRepository timedActivityRepository;
    private UserGoalRepository userGoalRepository;
    private WeatherRepository weatherRepository;
    public static int jobId;
    private long startOfDay;
    private long endOfDay;

    public UserGoalJob(Application application) {
        this.application = application;
        this.formattedTime = new FormattedTime();
        fitbitRepository = new FitbitRepository(application);
        googleFitRepository = new GoogleFitRepository(application);
        appUsageRepository = new AppUsageRepository(application);
        builtInFitnessRepository = new BuiltInFitnessRepository(application);
        baActivityRepository = new BAActivityRepository(application);
        timedActivityRepository = new TimedActivityRepository(application);
        userGoalRepository = new UserGoalRepository(application);
        weatherRepository = new WeatherRepository(application);
        startOfDay = formattedTime.getStartOfDay(formattedTime.getCurrentDateAsYYYYMMDD());
        endOfDay = formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD());
    }

    @Override
    protected Result onRunJob(Params params) {
        UserGoal userGoal = userGoalRepository.getGoalNow(0);
        if (userGoal == null || userGoal.getGoalName().isEmpty() || userGoal.getGoalType() == null
        || userGoal.getGoal() == null || userGoal.getDate() == null) {
            return Result.FAILURE;
        }
        String goalType = userGoal.getGoalType();
        double goal = userGoal.getGoal();
        String goalName = userGoal.getGoalName();
        Long progressLong = null;
        Double progressDouble = null;
        if (goalType.equals(application.getString(R.string.googlefit_camel_case))) {
            List<GoogleFitSummary> dailySummaries = googleFitRepository.getAllNow(startOfDay, endOfDay);
            if (dailySummaries != null && dailySummaries.size() > 0) {
                Log.i(TAG, "Size is " + dailySummaries.size());
                GoogleFitSummary today = dailySummaries.get(0);
                List<GoogleFitSummary.Summary> summaries = today.getSummaries();
                for (GoogleFitSummary.Summary summary : summaries) {
                    if (summary.getName().equals(goalName)) {
                        Log.i(TAG, "Current progress is " + summary.getName() + ": " + summary.getValue());
                        if (summary.getName().contains("Minutes")) {
                            Long minutes = TimeUnit.MILLISECONDS.toMinutes(summary.getValue().longValue());
                            goal = TimeUnit.MILLISECONDS.toMinutes((long) goal);
                            progressLong = minutes;
                        } else if (summary.getName().contains(application.getString(R.string.activity_distance_camel_case))) {
                            progressDouble = summary.getValue() / 1000;
                            goal = goal / 1000;
                        } else if (summary.getName().contains(application.getString(R.string.activity_steps_camel_case))) {
                            progressLong = summary.getValue().longValue();
                        } else {
                            progressDouble = summary.getValue();
                        }
                    }
                }
            } else {
                if (goalName.contains("Minutes")) {
                    goal = TimeUnit.MILLISECONDS.toMinutes((long) goal);
                    progressLong = 0L;
                } else if (goalName.contains(application.getString(R.string.activity_distance_camel_case))) {
                    goal = goal / 1000;
                    progressDouble = 0d;
                } else if (goalName.contains(application.getString(R.string.activity_steps_camel_case))) {
                    progressLong = 0L;
                } else {
                    progressDouble = 0d;}

            }
        } else if (goalType.equals(getContext().getString(R.string.fitbit_camel_case))) {
            List<FitbitDailySummary> dailySummaries = fitbitRepository.getAllNow(startOfDay, endOfDay);
            if (dailySummaries != null && dailySummaries.size() > 0) {
                Log.i(TAG, "Size is " + dailySummaries.size());
                FitbitDailySummary today = dailySummaries.get(0);
                FitbitActivitySummary summary = today.getActivitySummary();
                FitbitSleepSummary sleepSummary = today.getSleepSummary();
                if (goalName.equals(application.getString(R.string.activity_steps_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + summary.getSummary().getSteps());
                    progressLong = summary.getSummary().getSteps();
                } else if (goalName.equals(application.getString(R.string.activity_distance_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + summary.getSummary().getDistances().get(0).getDistance());
                    progressDouble = summary.getSummary().getDistances().get(0).getDistance();
                } else if (goalName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + summary.getSummary().getSedentaryMinutes());
                    progressLong = summary.getSummary().getSedentaryMinutes();
                } else if (goalName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + summary.getSummary().getFairlyActiveMinutes() + summary.getSummary().getVeryActiveMinutes());
                    progressLong = summary.getSummary().getFairlyActiveMinutes() + summary.getSummary().getVeryActiveMinutes();
                } else if (goalName.equals(application.getString(R.string.activity_floors_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + summary.getSummary().getFloors());
                    progressLong = summary.getSummary().getFloors();
                } else if (goalName.equals(application.getString(R.string.activity_calories_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + summary.getSummary().getCaloriesOut());
                    progressLong = summary.getSummary().getCaloriesOut();
                } else if (goalName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + sleepSummary.getSummary().getTotalMinutesAsleep());
                    progressLong = sleepSummary.getSummary().getTotalMinutesAsleep();
                }
            } else {
                if (goalName.equals(application.getString(R.string.activity_steps_camel_case))) {
                    progressLong = 0L;
                } else if (goalName.equals(application.getString(R.string.activity_distance_camel_case))) {
                    progressDouble = 0d;
                } else if (goalName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                    progressLong = 0L;
                } else if (goalName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                    progressLong = 0L;
                } else if (goalName.equals(application.getString(R.string.activity_floors_camel_case))) {
                    progressLong = 0L;
                } else if (goalName.equals(application.getString(R.string.activity_calories_camel_case))) {
                    progressLong = 0L;
                } else if (goalName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                    progressLong = 0L;
                }
            }
        } else if (goalType.equals(application.getString(R.string.moabi_tracker_camel_case))) {
            List<BuiltInActivitySummary> dailySummaries = builtInFitnessRepository.getActivitySummariesNow(startOfDay, endOfDay);
            if (dailySummaries != null && dailySummaries.size() > 0) {
                Log.i(TAG, "Size is " + dailySummaries.size());
                BuiltInActivitySummary today = dailySummaries.get(0);
                if (goalName.equals(application.getString(R.string.activity_steps_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + today.getSteps());
                    progressLong = today.getSteps();
                } else if (goalName.equals(application.getString(R.string.activity_distance_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + today.getDistance());
                    progressDouble = today.getDistance() / 1000;
                    goal = goal / 1000;
                } else if (goalName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + today.getSedentaryMinutes());
                    progressLong = TimeUnit.MILLISECONDS.toMinutes(today.getSedentaryMinutes());
                    goal = TimeUnit.MILLISECONDS.toMinutes((long) goal);
                } else if (goalName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + today.getActiveMinutes());
                    progressLong = TimeUnit.MILLISECONDS.toMinutes(today.getActiveMinutes());
                    goal = TimeUnit.MILLISECONDS.toMinutes((long) goal);
                } else if (goalName.equals(application.getString(R.string.activity_calories_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + today.getCalories());
                    progressDouble = today.getCalories();
                }
            } else {
                if (goalName.equals(application.getString(R.string.activity_steps_camel_case))) {
                    progressLong = 0L;
                } else if (goalName.equals(application.getString(R.string.activity_distance_camel_case))) {
                    progressDouble = 0d;
                    goal = goal / 1000;
                } else if (goalName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                    progressLong = 0L;
                    goal = TimeUnit.MILLISECONDS.toMinutes((long) goal);
                } else if (goalName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                    progressLong = 0L;
                    goal = TimeUnit.MILLISECONDS.toMinutes((long) goal);
                } else if (goalName.equals(application.getString(R.string.activity_calories_camel_case))) {
                    progressDouble = 0d;
                }
            }
        } else if (goalType.equals(application.getString(R.string.timer_camel_case))) {
            List<TimedActivitySummary> dailySummaries = timedActivityRepository.getAllNow(startOfDay, endOfDay);
            if (dailySummaries != null && dailySummaries.size() > 0) {
                for (TimedActivitySummary timedActivitySummary: dailySummaries) {
                    if (timedActivitySummary.getInputName().equals(goalName)) {
                        if (progressLong == null) {
                            progressLong = TimeUnit.MILLISECONDS.toMinutes(timedActivitySummary.getDuration());
                        } else {
                            progressLong += TimeUnit.MILLISECONDS.toMinutes(timedActivitySummary.getDuration());
                        }
                    }
                }
                Log.i(TAG, "Current progress is " + goalName + ": " + progressLong);
            } else {
                progressLong = 0L;
            }
            goal = TimeUnit.MILLISECONDS.toMinutes((long) goal);
        } else if (goalType.equals(application.getString(R.string.weather_camel_case))) {
            List<WeatherDailySummary> dailySummaries = weatherRepository.getAllNow(startOfDay, endOfDay);
            if (dailySummaries != null && dailySummaries.size() > 0) {
                Log.i(TAG, "Size is " + dailySummaries.size());
                WeatherDailySummary today = dailySummaries.get(0);
                if (goalName.equals(application.getString(R.string.humidity_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + today.getAvgHumidity());
                    progressDouble = today.getAvgHumidity();
                } else if (goalName.equals(application.getString(R.string.temperature_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + today.getAvgTempC());
                    progressDouble = today.getAvgTempC();
                } else if (goalName.equals(application.getString(R.string.precipitation_camel_case))) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + today.getTotalPrecipmm());
                    progressDouble = today.getTotalPrecipmm();
                }
            } else {
                progressDouble = 0d;
            }
        } else if (goalType.equals(application.getString(R.string.phone_usage_camel_case))) {
            List<AppUsageSummary> dailySummaries = appUsageRepository.getAllNow(startOfDay, endOfDay);
            if (dailySummaries != null && dailySummaries.size() > 0) {
                Log.i(TAG, "Size is " + dailySummaries.size());
                AppUsageSummary today = dailySummaries.get(0);
                List<AppUsage> summaries = today.getActivities();
                for (AppUsage appUsage : summaries) {
                    if (appUsage.getAppName().equals(goalName)) {
                        if (appUsage.getAppName().equals("Total")) {
                            goalName = application.getString(R.string.activity_phone_usage_camel_case);
                        }
                        progressLong = TimeUnit.MILLISECONDS.toMinutes(appUsage.getTotalTime());
                    }
                }
                if (progressLong != null) {
                    Log.i(TAG, "Current progress is " + goalName + ": " + progressLong);
                } else {
                    Log.i(TAG, "Current progress is " + goalName + ": " + 0);
                    progressLong = 0L;
                }
                goal = TimeUnit.MILLISECONDS.toMinutes((long) goal);
            } else {
                goal = TimeUnit.MILLISECONDS.toMinutes((long) goal);
                progressLong = 0L;
            }
        } else if (goalType.equals(application.getString(R.string.baactivity_camel_case))) {
            List<BAActivityEntry> dailySummaries = baActivityRepository.getActivityEntriesNow(startOfDay, endOfDay);
            if (dailySummaries != null && dailySummaries.size() > 0) {
                Log.i(TAG, "Size is " + dailySummaries.size());
                int counter = 0;
                for (BAActivityEntry activity : dailySummaries) {
                    if (activity.getName().equals(goalName)) {
                        counter++;
                    }
                }
                progressLong = (long) counter;
                Log.i(TAG, "Current progress is " + goalName + ": " + counter);
            } else {
                progressLong = 0L;
            }
        }
        Intent serviceIntent = new Intent(application, UserGoalService.class);
        serviceIntent.putExtra("goalType", goalType);
        serviceIntent.putExtra("goal", goal);
        serviceIntent.putExtra("goalName", goalName);
        if (progressLong != null) {
            serviceIntent.putExtra("progress", progressLong);
            serviceIntent.putExtra("hasLong", true);
        } else if (progressDouble != null) {
            serviceIntent.putExtra("progress", progressDouble);
            serviceIntent.putExtra("hasLong", false);
        }
        application.startService(serviceIntent);
        return Result.SUCCESS;
    }


    public static void runJobImmediately() {
        jobId = new JobRequest.Builder(UserGoalJob.TAG)
                .startNow()
                .build()
                .schedule();
    }

    public static void schedulePeriodicJob() {
        jobId = new JobRequest.Builder(UserGoalJob.TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(5))
                .setRequiresDeviceIdle(false)
                .setUpdateCurrent(true)
                .setRequirementsEnforced(true)
                .build()
                .schedule();
    }

    public static void cancelJob() {
        JobManager.instance().cancel(jobId);
    }
}