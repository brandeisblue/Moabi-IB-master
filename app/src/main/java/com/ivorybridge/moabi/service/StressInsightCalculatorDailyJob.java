package com.ivorybridge.moabi.service;

import android.app.Application;
import android.util.Log;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;
import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.entity.stress.DailyStress;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;
import com.ivorybridge.moabi.repository.AppUsageRepository;
import com.ivorybridge.moabi.repository.BAActivityRepository;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.repository.FitbitRepository;
import com.ivorybridge.moabi.repository.GoogleFitRepository;
import com.ivorybridge.moabi.repository.StressRepository;
import com.ivorybridge.moabi.repository.TimedActivityRepository;
import com.ivorybridge.moabi.repository.WeatherRepository;
import com.ivorybridge.moabi.repository.statistics.PredictionsRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class StressInsightCalculatorDailyJob extends DailyJob {

    public static final String TAG = "stress_insight_calculator_daily_job";
    private FormattedTime formattedTime;
    private Application application;
    private Long yesterday;
    private StressRepository stressRepository;
    private FitbitRepository fitbitRepository;
    private GoogleFitRepository googleFitRepository;
    private AppUsageRepository appUsageRepository;
    private BuiltInFitnessRepository builtInFitnessRepository;
    private WeatherRepository weatherRepository;
    private BAActivityRepository baActivityRepository;
    private TimedActivityRepository timedActivityRepository;
    private PredictionsRepository predictionsRepository;


    public StressInsightCalculatorDailyJob(Application application) {
        this.application = application;
        this.formattedTime = new FormattedTime();
        this.yesterday = formattedTime.convertStringYYYYMMDDToLong(formattedTime.getYesterdaysDateAsYYYYMMDD());
        stressRepository = new StressRepository(application);
        fitbitRepository = new FitbitRepository(application);
        googleFitRepository = new GoogleFitRepository(application);
        appUsageRepository = new AppUsageRepository(application);
        builtInFitnessRepository = new BuiltInFitnessRepository(application);
        weatherRepository = new WeatherRepository(application);
        baActivityRepository = new BAActivityRepository(application);
        timedActivityRepository = new TimedActivityRepository(application);
        predictionsRepository = new PredictionsRepository(application);
    }

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {
        calculateStressLevelRegression(7);
        calculateStressLevelRegression(28);
        calculateStressLevelRegression(182);
        calculateStressLevelRegression(392);
        return DailyJobResult.SUCCESS;
    }


    public static void scheduleJob() {
        DailyJob.schedule(new JobRequest.Builder(TAG),
                TimeUnit.HOURS.toMillis(0),
                TimeUnit.HOURS.toMillis(0) + TimeUnit.MINUTES.toMillis(10));
        /*
        new JobRequest.Builder(BodyInsightCalculatorDailyJob.TAG)
                .startNow()
                //.setPeriodic(TimeUnit.MINUTES.toMillis(30), TimeUnit.MINUTES.toMillis(15))
                //.setRequiresDeviceIdle(true)
                //.setUpdateCurrent(true)
                //.setRequirementsEnforced(true)
                .build()
                .schedule();*/
    }

    private void calculateStressLevelRegression(int numOfDays) {
        Log.i("RegressionJob", "Starting calculation");
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<DailyStress> dailyStresss = stressRepository.getDailyStresssNow(startOfData, yesterday);
        if (dailyStresss != null && dailyStresss.size() > 2) {
            Log.i("RegressionJob", dailyStresss.toString());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    calculateRegressionWithGoogleFit(dailyStresss, numOfDays);
                    calculateRegressionWithFitbit(dailyStresss, numOfDays);
                    calculateRegressionWithAppUsage(dailyStresss, numOfDays);
                    calculateRegressionWithBuiltInFitness(dailyStresss, numOfDays);
                    calculateRegressionWithWeather(dailyStresss, numOfDays);
                    calculateRegressionWithBAActivity(dailyStresss, numOfDays);
                    calculateRegressionWithTimedActivity(dailyStresss, numOfDays);
                }
            }).start();
        }
    }

    private void calculateRegressionWithGoogleFit(
            final List<DailyStress> dailyStressList, int numOfDays) {
        Log.i("RegressionJob", "Calculating regression with GoogleFit");
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<GoogleFitSummary> googleFitSummaries = googleFitRepository.getAllNow(startOfData, yesterday);
        if (googleFitSummaries != null && googleFitSummaries.size() > 2) {
            Log.i("RegressionJob", googleFitSummaries.toString());
            predictionsRepository.processStressWithGoogleFit(dailyStressList, googleFitSummaries, numOfDays);
        }
    }

    private void calculateRegressionWithFitbit(
            final List<DailyStress> dailyStressList, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<FitbitDailySummary> fitbitDailySummaries = fitbitRepository.getAllNow(startOfData, yesterday);
        if (fitbitDailySummaries != null && fitbitDailySummaries.size() > 2) {
            predictionsRepository.processStressWithFitbit(dailyStressList, fitbitDailySummaries, numOfDays);
        }

    }

    private void calculateRegressionWithAppUsage(
            final List<DailyStress> dailyStressList, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<AppUsageSummary> appUsageSummaries = appUsageRepository.getAllNow(startOfData, yesterday);
        if (appUsageSummaries != null && appUsageSummaries.size() > 2) {
            predictionsRepository.processStressWithAppUsage(dailyStressList, appUsageSummaries, numOfDays);
        }
    }

    private void calculateRegressionWithBuiltInFitness(
            final List<DailyStress> dailyStressList, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<BuiltInActivitySummary> activitySummaries = builtInFitnessRepository.getActivitySummariesNow(startOfData, yesterday);
        if (activitySummaries != null && activitySummaries.size() > 2) {
            predictionsRepository.processStressWithBuiltInFitness(dailyStressList, activitySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithWeather(
            final List<DailyStress> dailyStressList, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<WeatherDailySummary> weatherDailySummaries = weatherRepository.getAllNow(startOfData, yesterday);
        if (weatherDailySummaries != null && weatherDailySummaries.size() > 2) {
            predictionsRepository.processStressWithWeather(dailyStressList, weatherDailySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithBAActivity(
            final List<DailyStress> dailyStressList, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<BAActivityEntry> activitySummaries = baActivityRepository.getActivityEntriesNow(startOfData, yesterday);
        if (activitySummaries != null && activitySummaries.size() > 2) {
            predictionsRepository.processStressWithBAActivity(dailyStressList, activitySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithTimedActivity(
            final List<DailyStress> dailyStressList, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<TimedActivitySummary> timedActivities = timedActivityRepository.getAllNow(startOfData, yesterday);
        if (timedActivities != null && timedActivities.size() > 2) {
            predictionsRepository.processStressWithTimedActivity(dailyStressList, timedActivities, numOfDays);
        }
    }
}
