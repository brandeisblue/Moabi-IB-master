package com.ivorybridge.moabi.service;

import android.app.Application;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.dailyreview.DailyDailyReview;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;
import com.ivorybridge.moabi.repository.AppUsageRepository;
import com.ivorybridge.moabi.repository.BAActivityRepository;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.repository.FitbitRepository;
import com.ivorybridge.moabi.repository.GoogleFitRepository;
import com.ivorybridge.moabi.repository.DailyReviewRepository;
import com.ivorybridge.moabi.repository.TimedActivityRepository;
import com.ivorybridge.moabi.repository.WeatherRepository;
import com.ivorybridge.moabi.repository.statistics.PredictionsRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;

import androidx.annotation.NonNull;

public class DailyReviewInsightCalculatorJob extends Job {

    public static final String TAG = "daily_review_insight_calculator_job";
    private FormattedTime formattedTime;
    private Application application;
    private Long yesterday;
    private DailyReviewRepository dailyReviewRepository;
    private FitbitRepository fitbitRepository;
    private GoogleFitRepository googleFitRepository;
    private AppUsageRepository appUsageRepository;
    private BuiltInFitnessRepository builtInFitnessRepository;
    private WeatherRepository weatherRepository;
    private BAActivityRepository baActivityRepository;
    private TimedActivityRepository timedActivityRepository;
    private PredictionsRepository predictionsRepository;


    public DailyReviewInsightCalculatorJob(Application application) {
        this.application = application;
        this.formattedTime = new FormattedTime();
        this.yesterday = formattedTime.convertStringYYYYMMDDToLong(formattedTime.getYesterdaysDateAsYYYYMMDD());
        dailyReviewRepository = new DailyReviewRepository(application);
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
        calculateDailyReviewLevelRegression(7);
        calculateDailyReviewLevelRegression(28);
        calculateDailyReviewLevelRegression(182);
        calculateDailyReviewLevelRegression(392);
        return Result.SUCCESS;
    }


    public static void scheduleJob() {
        new JobRequest.Builder(DailyReviewInsightCalculatorJob.TAG)
                .startNow()
                //.setPeriodic(TimeUnit.MINUTES.toMillis(30), TimeUnit.MINUTES.toMillis(15))
                //.setRequiresDeviceIdle(true)
                //.setUpdateCurrent(true)
                //.setRequirementsEnforced(true)
                .build()
                .schedule();
    }


    private void calculateDailyReviewLevelRegression(int numOfDays) {
        Log.i("RegressionJob", "Starting calculation");
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<DailyDailyReview> dailyDailyReviews = dailyReviewRepository.getDailyDailyReviewsNow(startOfData, yesterday);
        if (dailyDailyReviews != null && dailyDailyReviews.size() > 2) {
            Log.i("RegressionJob", dailyDailyReviews.toString());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    calculateRegressionWithGoogleFit(dailyDailyReviews, numOfDays);
                    calculateRegressionWithFitbit(dailyDailyReviews, numOfDays);
                    calculateRegressionWithAppUsage(dailyDailyReviews, numOfDays);
                    calculateRegressionWithBuiltInFitness(dailyDailyReviews, numOfDays);
                    calculateRegressionWithWeather(dailyDailyReviews, numOfDays);
                    calculateRegressionWithBAActivity(dailyDailyReviews, numOfDays);
                    calculateRegressionWithTimedActivity(dailyDailyReviews, numOfDays);
                }
            }).start();
        }
    }

    private void calculateRegressionWithGoogleFit(
            final List<DailyDailyReview> dailyDailyReviewList, int numOfDays) {
        Log.i("RegressionJob", "Calculating regression with GoogleFit");
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<GoogleFitSummary> googleFitSummaries = googleFitRepository.getAllNow(startOfData, yesterday);
        if (googleFitSummaries != null && googleFitSummaries.size() > 2) {
            Log.i("RegressionJob", googleFitSummaries.toString());
            predictionsRepository.processDailyReviewWithGoogleFit(dailyDailyReviewList, googleFitSummaries, numOfDays);
        }
    }

    private void calculateRegressionWithFitbit(
            final List<DailyDailyReview> dailyDailyReviewList, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<FitbitDailySummary> fitbitDailySummaries = fitbitRepository.getAllNow(startOfData, yesterday);
        if (fitbitDailySummaries != null && fitbitDailySummaries.size() > 2) {
            predictionsRepository.processDailyReviewWithFitbit(dailyDailyReviewList, fitbitDailySummaries, numOfDays);
        }

    }

    private void calculateRegressionWithAppUsage(
            final List<DailyDailyReview> dailyDailyReviewList, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<AppUsageSummary> appUsageSummaries = appUsageRepository.getAllNow(startOfData, yesterday);
        if (appUsageSummaries != null && appUsageSummaries.size() > 2) {
            predictionsRepository.processDailyReviewWithAppUsage(dailyDailyReviewList, appUsageSummaries, numOfDays);
        }
    }

    private void calculateRegressionWithBuiltInFitness(
            final List<DailyDailyReview> dailyDailyReviewList, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<BuiltInActivitySummary> activitySummaries = builtInFitnessRepository.getActivitySummariesNow(startOfData, yesterday);
        if (activitySummaries != null && activitySummaries.size() > 2) {
            predictionsRepository.processDailyReviewWithBuiltInFitness(dailyDailyReviewList, activitySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithWeather(
            final List<DailyDailyReview> dailyDailyReviewList, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<WeatherDailySummary> weatherDailySummaries = weatherRepository.getAllNow(startOfData, yesterday);
        if (weatherDailySummaries != null && weatherDailySummaries.size() > 2) {
            predictionsRepository.processDailyReviewWithWeather(dailyDailyReviewList, weatherDailySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithBAActivity(
            final List<DailyDailyReview> dailyDailyReviewList, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<BAActivityEntry> activitySummaries = baActivityRepository.getActivityEntriesNow(startOfData, yesterday);
        if (activitySummaries != null && activitySummaries.size() > 2) {
            predictionsRepository.processDailyReviewWithBAActivity(dailyDailyReviewList, activitySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithTimedActivity(
            final List<DailyDailyReview> dailyDailyReviewList, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<TimedActivitySummary> timedActivities = timedActivityRepository.getAllNow(startOfData, yesterday);
        if (timedActivities != null && timedActivities.size() > 2) {
            predictionsRepository.processDailyReviewWithTimedActivity(dailyDailyReviewList, timedActivities, numOfDays);
        }
    }
}
