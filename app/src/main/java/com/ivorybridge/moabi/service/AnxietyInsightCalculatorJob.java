package com.ivorybridge.moabi.service;

import android.app.Application;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.anxiety.DailyGad7;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;
import com.ivorybridge.moabi.repository.AppUsageRepository;
import com.ivorybridge.moabi.repository.BAActivityRepository;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.repository.AnxietyRepository;
import com.ivorybridge.moabi.repository.FitbitRepository;
import com.ivorybridge.moabi.repository.GoogleFitRepository;
import com.ivorybridge.moabi.repository.TimedActivityRepository;
import com.ivorybridge.moabi.repository.WeatherRepository;
import com.ivorybridge.moabi.repository.statistics.PredictionsRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;

import androidx.annotation.NonNull;

public class AnxietyInsightCalculatorJob extends Job {

    public static final String TAG = "anxiety_insight_calculator_job";
    private FormattedTime formattedTime;
    private Application application;
    private Long yesterday;
    private AnxietyRepository anxietyRepository;
    private FitbitRepository fitbitRepository;
    private GoogleFitRepository googleFitRepository;
    private AppUsageRepository appUsageRepository;
    private BuiltInFitnessRepository builtInFitnessRepository;
    private WeatherRepository weatherRepository;
    private BAActivityRepository baActivityRepository;
    private TimedActivityRepository timedActivityRepository;
    private PredictionsRepository predictionsRepository;


    public AnxietyInsightCalculatorJob(Application application) {
        this.application = application;
        this.formattedTime = new FormattedTime();
        this.yesterday = formattedTime.convertStringYYYYMMDDToLong(formattedTime.getYesterdaysDateAsYYYYMMDD());
        anxietyRepository = new AnxietyRepository(application);
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
        calculateGad7LevelRegression(7);
        calculateGad7LevelRegression(28);
        calculateGad7LevelRegression(182);
        calculateGad7LevelRegression(392);
        return Result.SUCCESS;
    }


    public static void scheduleJob() {
        new JobRequest.Builder(AnxietyInsightCalculatorJob.TAG)
                .startNow()
                //.setPeriodic(TimeUnit.MINUTES.toMillis(30), TimeUnit.MINUTES.toMillis(15))
                //.setRequiresDeviceIdle(true)
                //.setUpdateCurrent(true)
                //.setRequirementsEnforced(true)
                .build()
                .schedule();
    }


    private void calculateGad7LevelRegression(int numOfDays) {
        Log.i("RegressionJob", "Starting calculation");
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<DailyGad7> dailyGad7s = anxietyRepository.getDailyGad7sNow(startOfData, yesterday);
        if (dailyGad7s != null && dailyGad7s.size() > 2) {
            Log.i("RegressionJob", dailyGad7s.toString());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    calculateRegressionWithGoogleFit(dailyGad7s, numOfDays);
                    calculateRegressionWithFitbit(dailyGad7s, numOfDays);
                    calculateRegressionWithAppUsage(dailyGad7s, numOfDays);
                    calculateRegressionWithBuiltInFitness(dailyGad7s, numOfDays);
                    calculateRegressionWithWeather(dailyGad7s, numOfDays);
                    calculateRegressionWithBAActivity(dailyGad7s, numOfDays);
                    calculateRegressionWithTimedActivity(dailyGad7s, numOfDays);
                }
            }).start();
        }
    }

    private void calculateRegressionWithGoogleFit(
            final List<DailyGad7> dailyGad7List, int numOfDays) {
        Log.i("RegressionJob", "Calculating regression with GoogleFit");
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<GoogleFitSummary> googleFitSummaries = googleFitRepository.getAllNow(startOfData, yesterday);
        if (googleFitSummaries != null && googleFitSummaries.size() > 2) {
            Log.i("RegressionJob", googleFitSummaries.toString());
            predictionsRepository.processGad7WithGoogleFit(dailyGad7List, googleFitSummaries, numOfDays);
        }
    }

    private void calculateRegressionWithFitbit(
            final List<DailyGad7> dailyGad7List, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<FitbitDailySummary> fitbitDailySummaries = fitbitRepository.getAllNow(startOfData, yesterday);
        if (fitbitDailySummaries != null && fitbitDailySummaries.size() > 2) {
            predictionsRepository.processGad7WithFitbit(dailyGad7List, fitbitDailySummaries, numOfDays);
        }

    }

    private void calculateRegressionWithAppUsage(
            final List<DailyGad7> dailyGad7List, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<AppUsageSummary> appUsageSummaries = appUsageRepository.getAllNow(startOfData, yesterday);
        if (appUsageSummaries != null && appUsageSummaries.size() > 2) {
            predictionsRepository.processGad7WithAppUsage(dailyGad7List, appUsageSummaries, numOfDays);
        }
    }

    private void calculateRegressionWithBuiltInFitness(
            final List<DailyGad7> dailyGad7List, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<BuiltInActivitySummary> activitySummaries = builtInFitnessRepository.getActivitySummariesNow(startOfData, yesterday);
        if (activitySummaries != null && activitySummaries.size() > 2) {
            predictionsRepository.processGad7WithBuiltInFitness(dailyGad7List, activitySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithWeather(
            final List<DailyGad7> dailyGad7List, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<WeatherDailySummary> weatherDailySummaries = weatherRepository.getAllNow(startOfData, yesterday);
        if (weatherDailySummaries != null && weatherDailySummaries.size() > 2) {
            predictionsRepository.processGad7WithWeather(dailyGad7List, weatherDailySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithBAActivity(
            final List<DailyGad7> dailyGad7List, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<BAActivityEntry> activitySummaries = baActivityRepository.getActivityEntriesNow(startOfData, yesterday);
        if (activitySummaries != null && activitySummaries.size() > 2) {
            predictionsRepository.processGad7WithBAActivity(dailyGad7List, activitySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithTimedActivity(
            final List<DailyGad7> dailyGad7List, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<TimedActivitySummary> timedActivities = timedActivityRepository.getAllNow(startOfData, yesterday);
        if (timedActivities != null && timedActivities.size() > 2) {
            predictionsRepository.processGad7WithTimedActivity(dailyGad7List, timedActivities, numOfDays);
        }
    }
}


