package com.ivorybridge.moabi.service;

import android.app.Application;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.depression.DailyPhq9;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;
import com.ivorybridge.moabi.repository.AppUsageRepository;
import com.ivorybridge.moabi.repository.BAActivityRepository;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.repository.DepressionRepository;
import com.ivorybridge.moabi.repository.FitbitRepository;
import com.ivorybridge.moabi.repository.GoogleFitRepository;
import com.ivorybridge.moabi.repository.TimedActivityRepository;
import com.ivorybridge.moabi.repository.WeatherRepository;
import com.ivorybridge.moabi.repository.statistics.PredictionsRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;

import androidx.annotation.NonNull;

public class DepressionInsightCalculatorJob extends Job {

    public static final String TAG = "depression_insight_calculator_job";
    private FormattedTime formattedTime;
    private Application application;
    private Long yesterday;
    private DepressionRepository depressionRepository;
    private FitbitRepository fitbitRepository;
    private GoogleFitRepository googleFitRepository;
    private AppUsageRepository appUsageRepository;
    private BuiltInFitnessRepository builtInFitnessRepository;
    private WeatherRepository weatherRepository;
    private BAActivityRepository baActivityRepository;
    private TimedActivityRepository timedActivityRepository;
    private PredictionsRepository predictionsRepository;


    public DepressionInsightCalculatorJob(Application application) {
        this.application = application;
        this.formattedTime = new FormattedTime();
        this.yesterday = formattedTime.convertStringYYYYMMDDToLong(formattedTime.getYesterdaysDateAsYYYYMMDD());
        depressionRepository = new DepressionRepository(application);
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
    protected Job.Result onRunJob(@NonNull Job.Params params) {
        calculatePhq9LevelRegression(7);
        calculatePhq9LevelRegression(28);
        calculatePhq9LevelRegression(182);
        calculatePhq9LevelRegression(392);
        return Result.SUCCESS;
    }


    public static void scheduleJob() {
        new JobRequest.Builder(DepressionInsightCalculatorJob.TAG)
                .startNow()
                //.setPeriodic(TimeUnit.MINUTES.toMillis(30), TimeUnit.MINUTES.toMillis(15))
                //.setRequiresDeviceIdle(true)
                //.setUpdateCurrent(true)
                //.setRequirementsEnforced(true)
                .build()
                .schedule();
    }


    private void calculatePhq9LevelRegression(int numOfDays) {
        Log.i("RegressionJob", "Starting calculation");
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<DailyPhq9> dailyPhq9s = depressionRepository.getDailyPhq9sNow(startOfData, yesterday);
        if (dailyPhq9s != null && dailyPhq9s.size() > 2) {
            Log.i("RegressionJob", dailyPhq9s.toString());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    calculateRegressionWithGoogleFit(dailyPhq9s, numOfDays);
                    calculateRegressionWithFitbit(dailyPhq9s, numOfDays);
                    calculateRegressionWithAppUsage(dailyPhq9s, numOfDays);
                    calculateRegressionWithBuiltInFitness(dailyPhq9s, numOfDays);
                    calculateRegressionWithWeather(dailyPhq9s, numOfDays);
                    calculateRegressionWithBAActivity(dailyPhq9s, numOfDays);
                    calculateRegressionWithTimedActivity(dailyPhq9s, numOfDays);
                }
            }).start();
        }
    }

    private void calculateRegressionWithGoogleFit(
            final List<DailyPhq9> dailyPhq9List, int numOfDays) {
        Log.i("RegressionJob", "Calculating regression with GoogleFit");
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<GoogleFitSummary> googleFitSummaries = googleFitRepository.getAllNow(startOfData, yesterday);
        if (googleFitSummaries != null && googleFitSummaries.size() > 2) {
            Log.i("RegressionJob", googleFitSummaries.toString());
            predictionsRepository.processPhq9WithGoogleFit(dailyPhq9List, googleFitSummaries, numOfDays);
        }
    }

    private void calculateRegressionWithFitbit(
            final List<DailyPhq9> dailyPhq9List, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<FitbitDailySummary> fitbitDailySummaries = fitbitRepository.getAllNow(startOfData, yesterday);
        if (fitbitDailySummaries != null && fitbitDailySummaries.size() > 2) {
            predictionsRepository.processPhq9WithFitbit(dailyPhq9List, fitbitDailySummaries, numOfDays);
        }

    }

    private void calculateRegressionWithAppUsage(
            final List<DailyPhq9> dailyPhq9List, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<AppUsageSummary> appUsageSummaries = appUsageRepository.getAllNow(startOfData, yesterday);
        if (appUsageSummaries != null && appUsageSummaries.size() > 2) {
            predictionsRepository.processPhq9WithAppUsage(dailyPhq9List, appUsageSummaries, numOfDays);
        }
    }

    private void calculateRegressionWithBuiltInFitness(
            final List<DailyPhq9> dailyPhq9List, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<BuiltInActivitySummary> activitySummaries = builtInFitnessRepository.getActivitySummariesNow(startOfData, yesterday);
        if (activitySummaries != null && activitySummaries.size() > 2) {
            predictionsRepository.processPhq9WithBuiltInFitness(dailyPhq9List, activitySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithWeather(
            final List<DailyPhq9> dailyPhq9List, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<WeatherDailySummary> weatherDailySummaries = weatherRepository.getAllNow(startOfData, yesterday);
        if (weatherDailySummaries != null && weatherDailySummaries.size() > 2) {
            predictionsRepository.processPhq9WithWeather(dailyPhq9List, weatherDailySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithBAActivity(
            final List<DailyPhq9> dailyPhq9List, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<BAActivityEntry> activitySummaries = baActivityRepository.getActivityEntriesNow(startOfData, yesterday);
        if (activitySummaries != null && activitySummaries.size() > 2) {
            predictionsRepository.processPhq9WithBAActivity(dailyPhq9List, activitySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithTimedActivity(
            final List<DailyPhq9> dailyPhq9List, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<TimedActivitySummary> timedActivities = timedActivityRepository.getAllNow(startOfData, yesterday);
        if (timedActivities != null && timedActivities.size() > 2) {
            predictionsRepository.processPhq9WithTimedActivity(dailyPhq9List, timedActivities, numOfDays);
        }
    }
}

