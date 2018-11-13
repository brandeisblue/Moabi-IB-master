package com.ivorybridge.moabi.service;

import android.app.Application;
import android.util.Log;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;
import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.repository.AppUsageRepository;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.repository.FitbitRepository;
import com.ivorybridge.moabi.repository.GoogleFitRepository;
import com.ivorybridge.moabi.repository.MoodAndEnergyRepository;
import com.ivorybridge.moabi.repository.statistics.PredictionsRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class BodyInsightCalculatorDailyJob extends DailyJob {

    public static final String TAG = "body_insight_calculator_daily_job";
    private FormattedTime formattedTime;
    private Application application;
    private Long yesterday;
    private MoodAndEnergyRepository moodAndEnergyRepository;
    private FitbitRepository fitbitRepository;
    private GoogleFitRepository googleFitRepository;
    private AppUsageRepository appUsageRepository;
    private BuiltInFitnessRepository builtInFitnessRepository;
    private PredictionsRepository predictionsRepository;

    public BodyInsightCalculatorDailyJob(Application application) {
        this.application = application;
        this.formattedTime = new FormattedTime();
        this.yesterday = formattedTime.convertStringYYYYMMDDToLong(formattedTime.getYesterdaysDateAsYYYYMMDD());
        moodAndEnergyRepository = new MoodAndEnergyRepository(application);
        fitbitRepository = new FitbitRepository(application);
        googleFitRepository = new GoogleFitRepository(application);
        appUsageRepository = new AppUsageRepository(application);
        builtInFitnessRepository = new BuiltInFitnessRepository(application);
        predictionsRepository = new PredictionsRepository(application);
    }

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {
        calculateFitbitRegression(7);
        calculateGoogleFitRegression(7);
        calculateFitbitRegression(28);
        calculateGoogleFitRegression(28);
        calculateFitbitRegression(182);
        calculateGoogleFitRegression(182);
        calculateFitbitRegression(392);
        calculateGoogleFitRegression(392);
        calculateMoabiRegression(7);
        calculateMoabiRegression(28);
        calculateMoabiRegression(182);
        calculateMoabiRegression(392);
        return DailyJobResult.SUCCESS;
    }

    public static void scheduleJob() {
        DailyJob.schedule(new JobRequest.Builder(TAG),
                TimeUnit.HOURS.toMillis(0),
                TimeUnit.HOURS.toMillis(0) + TimeUnit.MINUTES.toMillis(10));
    }

    private void calculateFitbitRegression(int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<FitbitDailySummary> fitbitDailySummaries = fitbitRepository.getAllNow(startOfData, yesterday);
        List<AppUsageSummary> appUsageSummaries = appUsageRepository.getAllNow(startOfData, yesterday);
        Map<String, FitbitDailySummary> fitbitDailySummaryMap = new LinkedHashMap<>();
        Map<String, AppUsageSummary> appUsageSummaryMap = new LinkedHashMap<>();
        if (fitbitDailySummaries != null && fitbitDailySummaries.size() > 2) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (FitbitDailySummary fitbitDailySummary: fitbitDailySummaries) {
                        fitbitDailySummaryMap.put(fitbitDailySummary.getDate(), fitbitDailySummary);
                    }
                    for (AppUsageSummary appUsageSummary: appUsageSummaries) {
                        appUsageSummaryMap.put(appUsageSummary.getDate(), appUsageSummary);
                    }
                    Log.i("RegressionJob", "Starting fitbit x fitbit regression");
                    predictionsRepository.processFitbitXFitbitRegression(fitbitDailySummaries, numOfDays);
                    predictionsRepository.processFitbitWithAppUsage(fitbitDailySummaryMap, appUsageSummaryMap, numOfDays);
                }
            }).start();
        }
    }

    private void calculateGoogleFitRegression(int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<GoogleFitSummary> googleFitSummaries = googleFitRepository.getAllNow(startOfData, yesterday);
        List<AppUsageSummary> appUsageSummaries = appUsageRepository.getAllNow(startOfData, yesterday);
        Map<String, GoogleFitSummary> googleFitSummaryMap = new LinkedHashMap<>();
        Map<String, AppUsageSummary> appUsageSummaryMap = new LinkedHashMap<>();
        if (googleFitSummaries != null && googleFitSummaries.size() > 2) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (GoogleFitSummary googleFitSummary: googleFitSummaries) {
                        googleFitSummaryMap.put(googleFitSummary.getDate(), googleFitSummary);
                    }
                    for (AppUsageSummary appUsageSummary: appUsageSummaries) {
                        appUsageSummaryMap.put(appUsageSummary.getDate(), appUsageSummary);
                    }
                    Log.i("RegressionJob", "Starting googlefit x googlefit regression");
                    predictionsRepository.processGoogleFitXGoogleFitRegression(googleFitSummaries, numOfDays);
                    predictionsRepository.processGoogleFitWithAppUsage(googleFitSummaryMap, appUsageSummaryMap, numOfDays);
                }
            }).start();
        }
    }

    private void calculateMoabiRegression(int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<BuiltInActivitySummary> dailySummaries = builtInFitnessRepository.getActivitySummariesNow(startOfData, yesterday);
        List<AppUsageSummary> appUsageSummaries = appUsageRepository.getAllNow(startOfData, yesterday);
        Map<String, BuiltInActivitySummary> dailySummaryMap = new LinkedHashMap<>();
        Map<String, AppUsageSummary> appUsageSummaryMap = new LinkedHashMap<>();
        if (dailySummaries != null && dailySummaries.size() > 2) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (BuiltInActivitySummary builtInActivitySummary: dailySummaries) {
                        dailySummaryMap.put(builtInActivitySummary.getDate(), builtInActivitySummary);
                    }
                    for (AppUsageSummary appUsageSummary: appUsageSummaries) {
                        appUsageSummaryMap.put(appUsageSummary.getDate(), appUsageSummary);
                    }
                    Log.i("RegressionJob", "Starting Moabi X Moabi regression");
                    predictionsRepository.processMoabiXMoabiRegression(dailySummaries, numOfDays);
                    predictionsRepository.processMoabiWithAppUsage(dailySummaryMap, appUsageSummaryMap, numOfDays);
                }
            }).start();
        }
    }
}
