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
import com.ivorybridge.moabi.database.entity.moodandenergy.AverageMood;
import com.ivorybridge.moabi.database.entity.moodandenergy.DailyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.DailyMood;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;
import com.ivorybridge.moabi.repository.AppUsageRepository;
import com.ivorybridge.moabi.repository.BAActivityRepository;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.repository.FitbitRepository;
import com.ivorybridge.moabi.repository.GoogleFitRepository;
import com.ivorybridge.moabi.repository.MoodAndEnergyRepository;
import com.ivorybridge.moabi.repository.TimedActivityRepository;
import com.ivorybridge.moabi.repository.WeatherRepository;
import com.ivorybridge.moabi.repository.statistics.PredictionsRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class MoodAndEnergyInsightCalculatorDailyJob extends DailyJob {

    public static final String TAG = "mood_energy_insight_calculator_daily_job";
    private FormattedTime formattedTime;
    private Application application;
    private Long yesterday;
    private MoodAndEnergyRepository moodAndEnergyRepository;
    private FitbitRepository fitbitRepository;
    private GoogleFitRepository googleFitRepository;
    private AppUsageRepository appUsageRepository;
    private BuiltInFitnessRepository builtInFitnessRepository;
    private WeatherRepository weatherRepository;
    private BAActivityRepository baActivityRepository;
    private TimedActivityRepository timedActivityRepository;
    private PredictionsRepository predictionsRepository;

    public MoodAndEnergyInsightCalculatorDailyJob(Application application) {
        this.application = application;
        this.formattedTime = new FormattedTime();
        this.yesterday = formattedTime.convertStringYYYYMMDDToLong(formattedTime.getYesterdaysDateAsYYYYMMDD());
        moodAndEnergyRepository = new MoodAndEnergyRepository(application);
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
        calculateMoodAndEnergyLevelRegression(7);
        calculateMoodAndEnergyLevelRegression(28);
        calculateMoodAndEnergyLevelRegression(182);
        calculateMoodAndEnergyLevelRegression(392);
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


    private void calculateMoodAndEnergyLevelRegression(int numOfDays) {
        Log.i("RegressionJob", "Starting calculation");
        final Map<String, AverageMood> averageMoodAndEnergyLevelMap = new LinkedHashMap<>();
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<DailyMood> dailyMoods = moodAndEnergyRepository.getDailyMoodsNow(startOfData, yesterday);
        List<DailyEnergy> dailyEnergies = moodAndEnergyRepository.getDailyEnergiesNow(startOfData, yesterday);
        if (dailyMoods != null && dailyMoods.size() > 0 && dailyEnergies != null && dailyEnergies.size() > 0) {
            if (dailyMoods.size() > 2 && dailyEnergies.size() > 2) {
                Log.i("RegressionJob", dailyMoods.toString());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < dailyMoods.size(); i++) {
                            AverageMood am = new AverageMood(dailyMoods.get(i).getAverageMood(), dailyEnergies.get(i).getAverageEnergy());
                            averageMoodAndEnergyLevelMap.put(dailyMoods.get(i).getDate(), am);
                        }
                        if (averageMoodAndEnergyLevelMap.size() > 0) {
                            calculateRegressionWithGoogleFit(averageMoodAndEnergyLevelMap, numOfDays);
                            calculateRegressionWithFitbit(averageMoodAndEnergyLevelMap, numOfDays);
                            calculateRegressionWithAppUsage(averageMoodAndEnergyLevelMap, numOfDays);
                            calculateRegressionWithBuiltInFitness(averageMoodAndEnergyLevelMap, numOfDays);
                            calculateRegressionWithWeather(averageMoodAndEnergyLevelMap, numOfDays);
                            calculateRegressionWithBAActivity(averageMoodAndEnergyLevelMap, numOfDays);
                            calculateRegressionWithTimedActivity(averageMoodAndEnergyLevelMap, numOfDays);
                        }
                    }
                }).start();
            }
        }
    }

    private void calculateRegressionWithGoogleFit(
            final Map<String, AverageMood> moodsAndEnergyLevelMap, int numOfDays) {
        Log.i("RegressionJob","Calculating regression with GoogleFit");
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<GoogleFitSummary> googleFitSummaries = googleFitRepository.getAllNow(startOfData, yesterday);
        if (googleFitSummaries != null && googleFitSummaries.size() > 2) {
            Log.i("RegressionJob",googleFitSummaries.toString());
            predictionsRepository.processMoodAndEnergyWithGoogleFit(moodsAndEnergyLevelMap, googleFitSummaries, numOfDays);
        }
    }

    private void calculateRegressionWithFitbit(
            final Map<String, AverageMood> moodsAndEnergyLevelMap, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<FitbitDailySummary> fitbitDailySummaries = fitbitRepository.getAllNow(startOfData, yesterday);
        if (fitbitDailySummaries != null && fitbitDailySummaries.size() > 2) {
            predictionsRepository.processMoodAndEnergyWithFitbit(moodsAndEnergyLevelMap, fitbitDailySummaries, numOfDays);
        }

    }

    private void calculateRegressionWithAppUsage(
            final Map<String, AverageMood> moodsAndEnergyLevelMap, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<AppUsageSummary> appUsageSummaries = appUsageRepository.getAllNow(startOfData, yesterday);
        if (appUsageSummaries != null && appUsageSummaries.size() > 2) {
            predictionsRepository.processMoodAndEnergyWithAppUsage(moodsAndEnergyLevelMap, appUsageSummaries, numOfDays);
        }
    }

    private void calculateRegressionWithBuiltInFitness(
            final Map<String, AverageMood> moodsAndEnergyLevelMap, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<BuiltInActivitySummary> activitySummaries = builtInFitnessRepository.getActivitySummariesNow(startOfData, yesterday);
        if (activitySummaries != null && activitySummaries.size() > 2) {
            predictionsRepository.processMoodAndEnergyWithBuiltInFitness(moodsAndEnergyLevelMap, activitySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithWeather(
            final Map<String, AverageMood> moodsAndEnergyLevelMap, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<WeatherDailySummary> weatherDailySummaries = weatherRepository.getAllNow(startOfData, yesterday);
        if (weatherDailySummaries != null && weatherDailySummaries.size() > 2) {
            predictionsRepository.processMoodAndEnergyWithWeather(moodsAndEnergyLevelMap, weatherDailySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithBAActivity(
            final Map<String, AverageMood> moodsAndEnergyLevelMap, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<BAActivityEntry> activitySummaries = baActivityRepository.getActivityEntriesNow(startOfData, yesterday);
        if (activitySummaries != null && activitySummaries.size() > 2) {
            predictionsRepository.processMoodAndEnergyWithBAActivity(moodsAndEnergyLevelMap, activitySummaries, numOfDays);
        }
    }

    private void calculateRegressionWithTimedActivity(
            final Map<String, AverageMood> moodsAndEnergyLevelMap, int numOfDays) {
        Long startOfData = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays);
        List<TimedActivitySummary> timedActivities = timedActivityRepository.getAllNow(startOfData, yesterday);
        if (timedActivities != null && timedActivities.size() > 2) {
            predictionsRepository.processMoodAndEnergyWithTimedActivity(moodsAndEnergyLevelMap, timedActivities, numOfDays);
        }
    }
}
