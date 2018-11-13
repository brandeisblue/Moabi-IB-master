package com.ivorybridge.moabi.repository.statistics;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.AsyncTaskBooleanDao;
import com.ivorybridge.moabi.database.dao.RegressionSummaryDao;
import com.ivorybridge.moabi.database.db.AsyncTaskBooleanDB;
import com.ivorybridge.moabi.database.db.RegressionSummaryDB;
import com.ivorybridge.moabi.database.entity.anxiety.DailyGad7;
import com.ivorybridge.moabi.database.entity.appusage.AppUsage;
import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.dailyreview.DailyDailyReview;
import com.ivorybridge.moabi.database.entity.depression.DailyPhq9;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.entity.moodandenergy.AverageMood;
import com.ivorybridge.moabi.database.entity.stats.RegressionSummary;
import com.ivorybridge.moabi.database.entity.stats.SimpleRegressionSummary;
import com.ivorybridge.moabi.database.entity.stress.DailyStress;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivity;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.DataInUseRepository;
import com.ivorybridge.moabi.repository.InputHistoryRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import androidx.lifecycle.LiveData;

public class PredictionsRepository {

    private static final String TAG = PredictionsRepository.class.getSimpleName();
    private FirebaseManager firebaseManager;
    private AsyncTaskBooleanDao mTaskBooleanDao;
    private Application application;
    private RegressionSummaryDao regressionSummaryDao;
    private InputHistoryRepository inputHistoryRepository;
    private DataInUseRepository dataInUseRepository;
    private FormattedTime formattedTime;

    public PredictionsRepository(Application application) {
        this.application = application;
        RegressionSummaryDB regressionSummaryDB = RegressionSummaryDB.getDatabase(application);
        regressionSummaryDao = regressionSummaryDB.regressionSummaryDao();
        AsyncTaskBooleanDB successDB = AsyncTaskBooleanDB.getDatabase(application);
        mTaskBooleanDao = successDB.asyncTaskBooleanDao();
        firebaseManager = new FirebaseManager();
        inputHistoryRepository = new InputHistoryRepository(application);
        dataInUseRepository = new DataInUseRepository(application);
        formattedTime = new FormattedTime();
    }

    public LiveData<List<SimpleRegressionSummary>> getAllMindSummaries(Long start, Long end, int duration) {
        return regressionSummaryDao.getAllMindSummaries(start, end, duration);
    }

    public LiveData<List<SimpleRegressionSummary>> getAllBodySummaries(Long start, Long end, int duration) {
        return regressionSummaryDao.getAllBodySummaries(start, end, duration);
    }


    public void processGoogleFitXGoogleFitRegression(List<GoogleFitSummary> googleFitSummaries, int duration) {
        Set<String> activitiesSet = new LinkedHashSet<>();
        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));

        List<Double> stepsList = new ArrayList<>();
        List<Double> caloriesList = new ArrayList<>();
        List<Double> distanceList = new ArrayList<>();
        List<Double> activeMinsList = new ArrayList<>();
        List<Double> sedentaryMinsList = new ArrayList<>();
        double stepsTotal = 0;
        double caloriesTotal = 0;
        double distancesTotal = 0;
        double activeMinsTotal = 0;
        double sedentaryMinsTotal = 0;

        for (GoogleFitSummary activitySummary : googleFitSummaries) {
            List<GoogleFitSummary.Summary> summaries = activitySummary.getSummaries();
            for (GoogleFitSummary.Summary summary : summaries) {
                if (summary.getName().equals(application.getString(R.string.activity_steps_camel_case))) {
                    stepsList.add(summary.getValue());
                    stepsTotal += summary.getValue();
                } else if (summary.getName().equals(application.getString(R.string.activity_calories_camel_case))) {
                    caloriesList.add(summary.getValue());
                    caloriesTotal += summary.getValue();
                } else if (summary.getName().equals(application.getString(R.string.activity_distance_camel_case))) {
                    distanceList.add(summary.getValue());
                    distancesTotal += summary.getValue();
                } else if (summary.getName().equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                    activeMinsList.add(summary.getValue());
                    activeMinsTotal += summary.getValue();
                } else if (summary.getName().equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                    sedentaryMinsList.add(summary.getValue());
                    sedentaryMinsTotal += summary.getValue();
                }
            }
        }

        Log.i(TAG, stepsList.toString());
        Log.i(TAG, caloriesList.toString());
        Log.i(TAG, distanceList.toString());
        Log.i(TAG, activeMinsList.toString());
        Log.i(TAG, sedentaryMinsList.toString());


        for (String activityName : activitiesSet) {
            double[][] regression1 = new double[stepsList.size()][2];
            double[][] regression2 = new double[stepsList.size()][2];
            double[][] regression3 = new double[stepsList.size()][2];
            double[][] regression4 = new double[stepsList.size()][2];


            for (int index = 0; index < distanceList.size(); index++) {
                if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                    regression1[index][1] = stepsList.get(index);
                    regression1[index][0] = caloriesList.get(index);
                    regression2[index][1] = stepsList.get(index);
                    regression2[index][0] = distanceList.get(index);
                    regression3[index][1] = stepsList.get(index);
                    regression3[index][0] = activeMinsList.get(index);
                    regression4[index][1] = stepsList.get(index);
                    regression4[index][0] = sedentaryMinsList.get(index);
                } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                    regression1[index][1] = caloriesList.get(index);
                    regression1[index][0] = stepsList.get(index);
                    regression2[index][1] = caloriesList.get(index);
                    regression2[index][0] = distanceList.get(index);
                    regression3[index][1] = caloriesList.get(index);
                    regression3[index][0] = activeMinsList.get(index);
                    regression4[index][1] = caloriesList.get(index);
                    regression4[index][0] = sedentaryMinsList.get(index);
                } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                    regression1[index][1] = distanceList.get(index);
                    regression1[index][0] = caloriesList.get(index);
                    regression2[index][1] = distanceList.get(index);
                    regression2[index][0] = stepsList.get(index);
                    regression3[index][1] = distanceList.get(index);
                    regression3[index][0] = activeMinsList.get(index);
                    regression4[index][1] = distanceList.get(index);
                    regression4[index][0] = sedentaryMinsList.get(index);
                } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                    regression1[index][1] = activeMinsList.get(index);
                    regression1[index][0] = caloriesList.get(index);
                    regression2[index][1] = activeMinsList.get(index);
                    regression2[index][0] = distanceList.get(index);
                    regression3[index][1] = activeMinsList.get(index);
                    regression3[index][0] = stepsList.get(index);
                    regression4[index][1] = activeMinsList.get(index);
                    regression4[index][0] = sedentaryMinsList.get(index);
                } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                    regression1[index][1] = sedentaryMinsList.get(index);
                    regression1[index][0] = caloriesList.get(index);
                    regression2[index][1] = sedentaryMinsList.get(index);
                    regression2[index][0] = distanceList.get(index);
                    regression3[index][1] = sedentaryMinsList.get(index);
                    regression3[index][0] = activeMinsList.get(index);
                    regression4[index][1] = sedentaryMinsList.get(index);
                    regression4[index][0] = stepsList.get(index);
                }
            }


            // linear  regression
            SimpleRegression regression = new SimpleRegression();

            double slope = 0;
            double yintercept = 0;
            double rsqured = 0;
            double correlation = 0;
            double recommendedActivityLevel = 0;
            //double depVarAverage = 0;
            double indepVarAverage1 = 0;
            double indepVarAverage2 = 0;
            double indepVarAverage3 = 0;
            double indepVarAverage4 = 0;

            double stepsAverage = stepsTotal / regression1.length;
            double caloriesAvg = caloriesTotal / regression1.length;
            double activeMinsAvg = activeMinsTotal / regression1.length;
            double sedentaryMinsAvg = sedentaryMinsTotal / regression1.length;
            double distancesAvg = distancesTotal / regression1.length;

            double stepsGoal = stepsAverage * 1.01; // TODO - set up multiplier for mood goal
            double caloriesGoal = caloriesAvg * 1.01; // TODO - set up multiplier for energy goal
            double activeMinsGoal = activeMinsAvg * 1.01; // TODO - set up multiplier for mood goal
            double sedentaryMinsGoal = sedentaryMinsAvg * 0.99; // TODO - set up multiplier for energy goal
            double distancesGoal = distancesAvg * 1.01; // TODO - set up multiplier for mood goal

            String depVar = activityName;
            String indepVar1 = "";
            String indepVar2 = "";
            String indepVar3 = "";
            String indepVar4 = "";
            String serviceType = application.getString(R.string.googlefit_camel_case);

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = sedentaryMinsAvg;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_steps_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = stepsAverage;
                indepVarAverage4 = sedentaryMinsAvg;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_steps_camel_case);
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = stepsAverage;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_steps_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = stepsAverage;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = sedentaryMinsAvg;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                indepVar1 = application.getString(R.string.activity_steps_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                indepVarAverage1 = stepsAverage;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = sedentaryMinsAvg;
            }


            // regression for steps x other fitbit measurements
            regression.addData(regression1);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression1));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar1, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage1, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar1 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(serviceType);
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage1 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();

            // regression for steps x other fitbit measurements
            regression.addData(regression2);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression2));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar2, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage2, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar2 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(serviceType);
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage2 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();

            // regression for steps x other fitbit measurements
            regression.addData(regression3);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression3));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar3, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage3, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar3 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(serviceType);
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage3 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();

            // regression for steps x other fitbit measurements
            regression.addData(regression4);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression4));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar4, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage4, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar4 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(serviceType);
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage4 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();
        }
    }

    public void processGoogleFitWithAppUsage(Map<String, GoogleFitSummary> googleFitSummaryMap, Map<String, AppUsageSummary> appUsageSummaryMap, int duration) {
        List<List<AppUsage>> appUsageListsList = new ArrayList<>();
        List<GoogleFitSummary> googleFitSummaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();

        for (Map.Entry<String, GoogleFitSummary> googleFitEntry : googleFitSummaryMap.entrySet()) {
            if (appUsageSummaryMap.get(googleFitEntry.getKey()) != null) {
                googleFitSummaries.add(googleFitEntry.getValue());
                AppUsageSummary appUsageSummary = appUsageSummaryMap.get(googleFitEntry.getKey());
                if (appUsageSummary.getActivities() != null) {
                    appUsageListsList.add(appUsageSummary.getActivities());
                }
            }
        }

        for (List<AppUsage> summary : appUsageListsList) {
            for (AppUsage appUsage : summary) {
                activitiesSet.add(appUsage.getAppName());
            }
        }

        List<Double> stepsList = new ArrayList<>();
        List<Double> caloriesList = new ArrayList<>();
        List<Double> distanceList = new ArrayList<>();
        List<Double> activeMinsList = new ArrayList<>();
        List<Double> sedentaryMinsList = new ArrayList<>();
        double stepsTotal = 0;
        double caloriesTotal = 0;
        double distancesTotal = 0;
        double activeMinsTotal = 0;
        double sedentaryMinsTotal = 0;

        for (GoogleFitSummary activitySummary : googleFitSummaries) {
            List<GoogleFitSummary.Summary> summaries = activitySummary.getSummaries();
            for (GoogleFitSummary.Summary summary : summaries) {
                if (summary.getName().equals(application.getString(R.string.activity_steps_camel_case))) {
                    stepsList.add(summary.getValue());
                    stepsTotal += summary.getValue();
                } else if (summary.getName().equals(application.getString(R.string.activity_calories_camel_case))) {
                    caloriesList.add(summary.getValue());
                    caloriesTotal += summary.getValue();
                } else if (summary.getName().equals(application.getString(R.string.activity_distance_camel_case))) {
                    distanceList.add(summary.getValue());
                    distancesTotal += summary.getValue();
                } else if (summary.getName().equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                    activeMinsList.add(summary.getValue());
                    activeMinsTotal += summary.getValue();
                } else if (summary.getName().equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                    sedentaryMinsList.add(summary.getValue());
                    sedentaryMinsTotal += summary.getValue();
                }
            }
        }

        for (String activityName : activitiesSet) {
            double[][] regression1 = new double[distanceList.size()][2];
            double[][] regression2 = new double[distanceList.size()][2];
            double[][] regression3 = new double[distanceList.size()][2];
            double[][] regression4 = new double[distanceList.size()][2];
            double[][] regression5 = new double[distanceList.size()][2];
            double appUsageTotal = 0;

            for (int i = 0; i < distanceList.size(); i++) {
                regression1[i][1] = stepsList.get(i);
                regression2[i][1] = caloriesList.get(i);
                regression3[i][1] = distanceList.get(i);
                regression4[i][1] = activeMinsList.get(i);
                regression5[i][1] = sedentaryMinsList.get(i);
                List<AppUsage> summaries = appUsageListsList.get(i);
                for (AppUsage activity : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(activity.getAppName())) {
                        regression1[i][0] = activity.getTotalTime();
                        regression2[i][0] = activity.getTotalTime();
                        regression3[i][0] = activity.getTotalTime();
                        regression4[i][0] = activity.getTotalTime();
                        regression5[i][0] = activity.getTotalTime();
                        appUsageTotal += activity.getTotalTime();
                        break;
                    } else {
                        // if the app was not used for the day, set the time to 0.
                        regression1[i][0] = 0;
                        regression2[i][0] = 0;
                        regression3[i][0] = 0;
                        regression4[i][0] = 0;
                        regression5[i][0] = 0;
                    }
                }
            }

            // linear  regression
            SimpleRegression regression = new SimpleRegression();

            double slope = 0;
            double yintercept = 0;
            double rsqured = 0;
            double correlation = 0;
            double recommendedActivityLevel = 0;
            double indepVarAverage = appUsageTotal / regression1.length;

            if (TimeUnit.MILLISECONDS.toMinutes((long) indepVarAverage) > 5) {

                double stepsAverage = stepsTotal / regression1.length;
                double caloriesAvg = caloriesTotal / regression1.length;
                double activeMinsAvg = activeMinsTotal / regression1.length;
                double sedentaryMinsAvg = sedentaryMinsTotal / regression1.length;
                double distancesAvg = distancesTotal / regression1.length;

                double stepsGoal = stepsAverage * 1.01; // TODO - set up multiplier for mood goal
                double caloriesGoal = caloriesAvg * 1.01; // TODO - set up multiplier for energy goal
                double activeMinsGoal = activeMinsAvg * 1.01; // TODO - set up multiplier for mood goal
                double sedentaryMinsGoal = sedentaryMinsAvg * 0.99; // TODO - set up multiplier for energy goal
                double distancesGoal = distancesAvg * 1.01; // TODO - set up multiplier for mood goal

                String depVar = application.getString(R.string.activity_steps_camel_case);
                String serviceType = application.getString(R.string.googlefit_camel_case);

                // regression for steps x other fitbit measurements
                regression.addData(regression1);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
                Log.i(TAG, Arrays.deepToString(regression1));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + serviceType + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(serviceType);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();

                // regression for steps x other fitbit measurements
                regression.addData(regression2);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;

                depVar = application.getString(R.string.activity_calories_camel_case);

                Log.i(TAG, Arrays.deepToString(regression2));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + serviceType + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(serviceType);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();


                // regression for steps x other fitbit measurements
                regression.addData(regression3);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (distancesGoal - yintercept) / slope;

                depVar = application.getString(R.string.activity_distance_camel_case);

                Log.i(TAG, Arrays.deepToString(regression3));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + serviceType + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(serviceType);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }


                regression.clear();

                // regression for steps x other fitbit measurements
                regression.addData(regression4);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
                depVar = application.getString(R.string.activity_active_minutes_camel_case);

                Log.i(TAG, Arrays.deepToString(regression4));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + serviceType + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(serviceType);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();

                // regression for steps x other fitbit measurements
                regression.addData(regression5);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
                depVar = application.getString(R.string.activity_sedentary_minutes_camel_case);

                Log.i(TAG, Arrays.deepToString(regression5));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + serviceType + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(serviceType);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processFitbitXFitbitRegression(List<FitbitDailySummary> fitbitDailySummaries, int duration) {
        Set<String> activitiesSet = new LinkedHashSet<>();
        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sleep_camel_case));
        activitiesSet.add(application.getString(R.string.activity_floors_camel_case));

        for (String activityName : activitiesSet) {
            int index = 0;
            double[][] regression1 = new double[fitbitDailySummaries.size()][2];
            double[][] regression2 = new double[fitbitDailySummaries.size()][2];
            double[][] regression3 = new double[fitbitDailySummaries.size()][2];
            double[][] regression4 = new double[fitbitDailySummaries.size()][2];
            double[][] regression5 = new double[fitbitDailySummaries.size()][2];
            double[][] regression6 = new double[fitbitDailySummaries.size()][2];
            double stepsTotal = 0;
            double caloriesTotal = 0;
            double distancesTotal = 0;
            double activeMinsTotal = 0;
            double sedentaryMinsTotal = 0;
            double sleepTotal = 0;
            double floorsTotal = 0;

            // iterate through each day
            for (FitbitDailySummary activitySummary : fitbitDailySummaries) {
                // if the app was used for the day, get the recorded time
                if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                    regression1[index][1] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    regression1[index][0] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    regression2[index][1] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    regression2[index][0] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    regression3[index][1] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    regression3[index][0] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    regression4[index][1] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    regression4[index][0] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    regression5[index][1] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    regression5[index][0] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    regression6[index][1] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    regression6[index][0] = activitySummary.getActivitySummary().getSummary().getFloors();
                    stepsTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    caloriesTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    distancesTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    activeMinsTotal += activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    sedentaryMinsTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    sleepTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    floorsTotal += activitySummary.getActivitySummary().getSummary().getFloors();
                } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                    regression1[index][1] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    regression1[index][0] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    regression2[index][1] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    regression2[index][0] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    regression3[index][1] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    regression3[index][0] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    regression4[index][1] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    regression4[index][0] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    regression5[index][1] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    regression5[index][0] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    regression6[index][1] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    regression6[index][0] = activitySummary.getActivitySummary().getSummary().getFloors();
                    stepsTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    caloriesTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    distancesTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    activeMinsTotal += activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    sedentaryMinsTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    sleepTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    floorsTotal += activitySummary.getActivitySummary().getSummary().getFloors();
                } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                    regression1[index][1] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    regression1[index][0] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    regression2[index][1] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    regression2[index][0] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    regression3[index][1] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    regression3[index][0] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    regression4[index][1] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    regression4[index][0] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    regression5[index][1] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    regression5[index][0] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    regression6[index][1] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    regression6[index][0] = activitySummary.getActivitySummary().getSummary().getFloors();
                    stepsTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    caloriesTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    distancesTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    activeMinsTotal += activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    sedentaryMinsTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    sleepTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    floorsTotal += activitySummary.getActivitySummary().getSummary().getFloors();
                } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                    regression1[index][1] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    regression1[index][0] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    regression2[index][1] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    regression2[index][0] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    regression3[index][1] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    regression3[index][0] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    regression4[index][1] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    regression4[index][0] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    regression5[index][1] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    regression5[index][0] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    regression6[index][1] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    regression6[index][0] = activitySummary.getActivitySummary().getSummary().getFloors();
                    stepsTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    caloriesTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    distancesTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    activeMinsTotal += activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    sedentaryMinsTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    sleepTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    floorsTotal += activitySummary.getActivitySummary().getSummary().getFloors();
                } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                    regression1[index][1] = activitySummary.getActivitySummary().getSummary().getFloors();
                    regression1[index][0] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    regression2[index][1] = activitySummary.getActivitySummary().getSummary().getFloors();
                    regression2[index][0] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    regression3[index][1] = activitySummary.getActivitySummary().getSummary().getFloors();
                    regression3[index][0] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    regression4[index][1] = activitySummary.getActivitySummary().getSummary().getFloors();
                    regression4[index][0] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    regression5[index][1] = activitySummary.getActivitySummary().getSummary().getFloors();
                    regression5[index][0] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    regression6[index][1] = activitySummary.getActivitySummary().getSummary().getFloors();
                    regression6[index][0] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    stepsTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    caloriesTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    distancesTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    activeMinsTotal += activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    sedentaryMinsTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    sleepTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    floorsTotal += activitySummary.getActivitySummary().getSummary().getFloors();
                } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                    regression1[index][1] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    regression1[index][0] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    regression2[index][1] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    regression2[index][0] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    regression3[index][1] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    regression3[index][0] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    regression4[index][1] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    regression4[index][0] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    regression5[index][1] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    regression5[index][0] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    regression6[index][1] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    regression6[index][0] = activitySummary.getActivitySummary().getSummary().getFloors();
                    stepsTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    caloriesTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    distancesTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    activeMinsTotal += activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    sedentaryMinsTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    sleepTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    floorsTotal += activitySummary.getActivitySummary().getSummary().getFloors();
                } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                    regression1[index][1] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    regression1[index][0] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    regression2[index][1] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    regression2[index][0] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    regression3[index][1] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    regression3[index][0] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    regression4[index][1] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    regression4[index][0] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    regression5[index][1] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    regression5[index][0] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    regression6[index][1] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    regression6[index][0] = activitySummary.getActivitySummary().getSummary().getFloors();
                    stepsTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    caloriesTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    distancesTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    activeMinsTotal += activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                    sedentaryMinsTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                    sleepTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                    floorsTotal += activitySummary.getActivitySummary().getSummary().getFloors();
                }
                index++;
            }
            
            // linear  regression
            SimpleRegression regression = new SimpleRegression();

            double slope = 0;
            double yintercept = 0;
            double rsqured = 0;
            double correlation = 0;
            double recommendedActivityLevel = 0;
            //double depVarAverage = 0;
            double indepVarAverage1 = 0;
            double indepVarAverage2 = 0;
            double indepVarAverage3 = 0;
            double indepVarAverage4 = 0;
            double indepVarAverage5 = 0;
            double indepVarAverage6 = 0;

            double stepsAverage = stepsTotal / regression1.length;
            double caloriesAvg = caloriesTotal / regression1.length;
            double activeMinsAvg = activeMinsTotal / regression1.length;
            double sedentaryMinsAvg = sedentaryMinsTotal / regression1.length;
            double sleepAvg = sleepTotal / regression1.length;
            double distancesAvg = distancesTotal / regression1.length;
            double floorsAvg = floorsTotal / regression1.length;

            double stepsGoal = stepsAverage * 1.01; // TODO - set up multiplier for mood goal
            double caloriesGoal = caloriesAvg * 1.01; // TODO - set up multiplier for energy goal
            double activeMinsGoal = activeMinsAvg * 1.01; // TODO - set up multiplier for mood goal
            double sedentaryMinsGoal = sedentaryMinsAvg * 0.99; // TODO - set up multiplier for energy goal
            double floorsGoal = floorsAvg * 1.01; // TODO - set up multiplier for mood goal
            double sleepGoal = sleepAvg * 1.01; // TODO - set up multiplier for energy goal
            double distancesGoal = distancesAvg * 1.01; // TODO - set up multiplier for mood goal

            String depVar = activityName;
            String indepVar1 = "";
            String indepVar2 = "";
            String indepVar3 = "";
            String indepVar4 = "";
            String indepVar5 = "";
            String indepVar6 = "";
            String serviceType = application.getString(R.string.fitbit_camel_case);

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                indepVar5 = application.getString(R.string.activity_sleep_camel_case);
                indepVar6 = application.getString(R.string.activity_floors_camel_case);
                //depVarAverage = stepsAverage;
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = sedentaryMinsAvg;
                indepVarAverage5 = sleepAvg;
                indepVarAverage6 = floorsAvg;
            } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                indepVar5 = application.getString(R.string.activity_steps_camel_case);
                indepVar6 = application.getString(R.string.activity_floors_camel_case);
                //depVarAverage = sleepAvg;
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = sedentaryMinsAvg;
                indepVarAverage5 = stepsAverage;
                indepVarAverage6 = floorsAvg;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_steps_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                indepVar5 = application.getString(R.string.activity_sleep_camel_case);
                indepVar6 = application.getString(R.string.activity_floors_camel_case);
                //depVarAverage = activeMinsAvg;
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = stepsAverage;
                indepVarAverage4 = sedentaryMinsAvg;
                indepVarAverage5 = sleepAvg;
                indepVarAverage6 = floorsAvg;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_steps_camel_case);
                indepVar5 = application.getString(R.string.activity_sleep_camel_case);
                indepVar6 = application.getString(R.string.activity_floors_camel_case);
                //depVarAverage = sedentaryMinsAvg;
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = stepsAverage;
                indepVarAverage5 = sleepAvg;
                indepVarAverage6 = floorsAvg;
            } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                indepVar5 = application.getString(R.string.activity_sleep_camel_case);
                indepVar6 = application.getString(R.string.activity_steps_camel_case);
                //depVarAverage = floorsAvg;
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = sedentaryMinsAvg;
                indepVarAverage5 = sleepAvg;
                indepVarAverage6 = stepsAverage;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_steps_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                indepVar5 = application.getString(R.string.activity_sleep_camel_case);
                indepVar6 = application.getString(R.string.activity_floors_camel_case);
                //depVarAverage = distancesAvg;
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = stepsAverage;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = sedentaryMinsAvg;
                indepVarAverage5 = sleepAvg;
                indepVarAverage6 = floorsAvg;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                indepVar1 = application.getString(R.string.activity_steps_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                indepVar5 = application.getString(R.string.activity_sleep_camel_case);
                indepVar6 = application.getString(R.string.activity_floors_camel_case);
                //depVarAverage = caloriesAvg;
                indepVarAverage1 = stepsAverage;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = sedentaryMinsAvg;
                indepVarAverage5 = sleepAvg;
                indepVarAverage6 = floorsAvg;
            }

            // regression for steps x other fitbit measurements
            regression.addData(regression1);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                recommendedActivityLevel = (floorsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                recommendedActivityLevel = (sleepGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression1));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar1, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage1, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar1 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage1 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();

            // regression for steps x other fitbit measurements
            regression.addData(regression2);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                recommendedActivityLevel = (floorsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                recommendedActivityLevel = (sleepGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression2));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar2, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage2, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar2 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage2 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();

            // regression for steps x other fitbit measurements
            regression.addData(regression3);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                recommendedActivityLevel = (floorsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                recommendedActivityLevel = (sleepGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression3));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar3, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage3, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar3 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage3 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();

            // regression for steps x other fitbit measurements
            regression.addData(regression4);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                recommendedActivityLevel = (floorsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                recommendedActivityLevel = (sleepGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression4));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar4, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage4, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar4 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage4 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();

            // regression for steps x other fitbit measurements
            regression.addData(regression5);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                recommendedActivityLevel = (floorsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                recommendedActivityLevel = (sleepGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression5));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar5, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage5, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar5 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage5 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();

            // regression for steps x other fitbit measurements
            regression.addData(regression6);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                recommendedActivityLevel = (floorsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                recommendedActivityLevel = (sleepGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression6));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar6, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage6, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar6 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage6 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();
        }
    }

    public void processFitbitWithAppUsage(Map<String, FitbitDailySummary> fitbitDailySummaryMap, Map<String, AppUsageSummary> appUsageSummaryMap, int duration) {
        List<List<AppUsage>> appUsageListsList = new ArrayList<>();
        List<FitbitDailySummary> fitbitDailySummaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();

        for (Map.Entry<String, FitbitDailySummary> fitbitEntry : fitbitDailySummaryMap.entrySet()) {
            if (appUsageSummaryMap.get(fitbitEntry.getKey()) != null) {
                fitbitDailySummaries.add(fitbitEntry.getValue());
                AppUsageSummary appUsageSummary = appUsageSummaryMap.get(fitbitEntry.getKey());
                if (appUsageSummary.getActivities() != null) {
                    appUsageListsList.add(appUsageSummary.getActivities());
                }
            }
        }

        for (List<AppUsage> summary : appUsageListsList) {
            for (AppUsage appUsage : summary) {
                activitiesSet.add(appUsage.getAppName());
            }
        }

        for (String activityName : activitiesSet) {
            int index = 0;
            double[][] regression1 = new double[fitbitDailySummaries.size()][2];
            double[][] regression2 = new double[fitbitDailySummaries.size()][2];
            double[][] regression3 = new double[fitbitDailySummaries.size()][2];
            double[][] regression4 = new double[fitbitDailySummaries.size()][2];
            double[][] regression5 = new double[fitbitDailySummaries.size()][2];
            double[][] regression6 = new double[fitbitDailySummaries.size()][2];
            double[][] regression7 = new double[fitbitDailySummaries.size()][2];
            double appUsageTotal = 0;
            double stepsTotal = 0;
            double caloriesTotal = 0;
            double distancesTotal = 0;
            double activeMinsTotal = 0;
            double sedentaryMinsTotal = 0;
            double sleepTotal = 0;
            double floorsTotal = 0;


            // iterate through each day
            for (List<AppUsage> summaries : appUsageListsList) {
                for (AppUsage activity : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(activity.getAppName())) {
                        regression1[index][0] = activity.getTotalTime();
                        regression2[index][0] = activity.getTotalTime();
                        regression3[index][0] = activity.getTotalTime();
                        regression4[index][0] = activity.getTotalTime();
                        regression5[index][0] = activity.getTotalTime();
                        regression6[index][0] = activity.getTotalTime();
                        regression7[index][0] = activity.getTotalTime();
                        appUsageTotal += activity.getTotalTime();
                        break;
                    } else {
                        // if the app was not used for the day, set the time to 0.
                        regression1[index][0] = 0;
                        regression2[index][0] = 0;
                        regression3[index][0] = 0;
                        regression4[index][0] = 0;
                        regression5[index][0] = 0;
                        regression6[index][0] = 0;
                        regression7[index][0] = 0;
                        appUsageTotal += 0;
                    }
                }
                index++;
            }

            int i = 0;
            // iterate through each day
            for (FitbitDailySummary activitySummary : fitbitDailySummaries) {
                // if the app was used for the day, get the recorded time
                regression1[i][1] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                regression2[i][1] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                regression3[i][1] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                regression4[i][1] = activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                regression5[i][1] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                regression6[i][1] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                regression7[i][1] = activitySummary.getActivitySummary().getSummary().getFloors();
                stepsTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                caloriesTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                distancesTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                activeMinsTotal += activitySummary.getActivitySummary().getSummary().getFairlyActiveMinutes() + activitySummary.getActivitySummary().getSummary().getVeryActiveMinutes();
                sedentaryMinsTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes();
                sleepTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep();
                floorsTotal += activitySummary.getActivitySummary().getSummary().getFloors();
                i++;
            }


            // linear  regression
            SimpleRegression regression = new SimpleRegression();

            double slope = 0;
            double yintercept = 0;
            double rsqured = 0;
            double correlation = 0;
            double recommendedActivityLevel = 0;
            double indepVarAverage = appUsageTotal / regression1.length;

            if (TimeUnit.MILLISECONDS.toMinutes((long) indepVarAverage) > 5) {

                double stepsAverage = stepsTotal / regression1.length;
                double caloriesAvg = caloriesTotal / regression1.length;
                double activeMinsAvg = activeMinsTotal / regression1.length;
                double sedentaryMinsAvg = sedentaryMinsTotal / regression1.length;
                double sleepAvg = sleepTotal / regression1.length;
                double distancesAvg = distancesTotal / regression1.length;
                double floorsAvg = floorsTotal / regression1.length;

                double stepsGoal = stepsAverage * 1.01; // TODO - set up multiplier for mood goal
                double caloriesGoal = caloriesAvg * 1.01; // TODO - set up multiplier for energy goal
                double activeMinsGoal = activeMinsAvg * 1.01; // TODO - set up multiplier for mood goal
                double sedentaryMinsGoal = sedentaryMinsAvg * 0.99; // TODO - set up multiplier for energy goal
                double floorsGoal = floorsAvg * 1.01; // TODO - set up multiplier for mood goal
                double sleepGoal = sleepAvg * 1.01; // TODO - set up multiplier for energy goal
                double distancesGoal = distancesAvg * 1.01; // TODO - set up multiplier for mood goal

                String depVar = application.getString(R.string.activity_steps_camel_case);

                // regression for steps x other fitbit measurements
                regression.addData(regression1);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (stepsGoal - yintercept) / slope;

                Log.i(TAG, Arrays.deepToString(regression1));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + application.getString(R.string.fitbit_camel_case) + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();

                // regression for steps x other fitbit measurements
                regression.addData(regression2);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
                depVar = application.getString(R.string.activity_calories_camel_case);

                Log.i(TAG, Arrays.deepToString(regression2));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + application.getString(R.string.fitbit_camel_case) + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();


                // regression for steps x other fitbit measurements
                regression.addData(regression3);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
                depVar = application.getString(R.string.activity_distance_camel_case);

                Log.i(TAG, Arrays.deepToString(regression3));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + application.getString(R.string.fitbit_camel_case) + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }


                regression.clear();

                // regression for steps x other fitbit measurements
                regression.addData(regression4);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
                depVar = application.getString(R.string.activity_active_minutes_camel_case);

                Log.i(TAG, Arrays.deepToString(regression4));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + application.getString(R.string.fitbit_camel_case) + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();

                // regression for steps x other fitbit measurements
                regression.addData(regression5);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
                depVar = application.getString(R.string.activity_sedentary_minutes_camel_case);

                Log.i(TAG, Arrays.deepToString(regression5));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + application.getString(R.string.fitbit_camel_case) + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();

                // regression for steps x other fitbit measurements
                regression.addData(regression6);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (sleepGoal - yintercept) / slope;
                depVar = application.getString(R.string.activity_sleep_camel_case);

                Log.i(TAG, Arrays.deepToString(regression6));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + application.getString(R.string.fitbit_camel_case) + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();

                // regression for steps x other fitbit measurements
                regression.addData(regression7);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (floorsGoal - yintercept) / slope;
                depVar = application.getString(R.string.activity_floors_camel_case);

                Log.i(TAG, Arrays.deepToString(regression7));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + application.getString(R.string.fitbit_camel_case) + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.fitbit_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();
            }
        }
    }

    public void processMoabiXMoabiRegression(List<BuiltInActivitySummary> dailySummaries, int duration) {
        Set<String> activitiesSet = new LinkedHashSet<>();
        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));

        for (String activityName : activitiesSet) {
            int index = 0;
            double[][] regression1 = new double[dailySummaries.size()][2];
            double[][] regression2 = new double[dailySummaries.size()][2];
            double[][] regression3 = new double[dailySummaries.size()][2];
            double[][] regression4 = new double[dailySummaries.size()][2];
            double[][] regression5 = new double[dailySummaries.size()][2];
            double[][] regression6 = new double[dailySummaries.size()][2];
            double stepsTotal = 0;
            double caloriesTotal = 0;
            double distancesTotal = 0;
            double activeMinsTotal = 0;
            double sedentaryMinsTotal = 0;
            double sleepTotal = 0;
            double floorsTotal = 0;

            // iterate through each day
            for (BuiltInActivitySummary activitySummary : dailySummaries) {
                // if the app was used for the day, get the recorded time
                if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                    regression1[index][1] = activitySummary.getSteps().doubleValue();
                    regression1[index][0] = activitySummary.getCalories();
                    regression2[index][1] = activitySummary.getSteps().doubleValue();
                    regression2[index][0] = activitySummary.getDistance();
                    regression3[index][1] = activitySummary.getSteps().doubleValue();
                    regression3[index][0] = activitySummary.getActiveMinutes();
                    regression4[index][1] = activitySummary.getSteps().doubleValue();
                    regression4[index][0] = activitySummary.getSedentaryMinutes();
                    stepsTotal += activitySummary.getSteps().doubleValue();
                    caloriesTotal += activitySummary.getCalories();
                    distancesTotal += activitySummary.getDistance();
                    activeMinsTotal += activitySummary.getActiveMinutes();
                    sedentaryMinsTotal += activitySummary.getSedentaryMinutes();
                } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                    regression1[index][1] = activitySummary.getActiveMinutes();
                    regression1[index][0] = activitySummary.getCalories();
                    regression2[index][1] = activitySummary.getActiveMinutes();
                    regression2[index][0] = activitySummary.getDistance();
                    regression3[index][1] = activitySummary.getActiveMinutes();
                    regression3[index][0] = activitySummary.getSteps().doubleValue();
                    regression4[index][1] = activitySummary.getActiveMinutes();
                    regression4[index][0] = activitySummary.getSedentaryMinutes();
                    stepsTotal += activitySummary.getSteps().doubleValue();
                    caloriesTotal += activitySummary.getCalories();
                    distancesTotal += activitySummary.getDistance();
                    activeMinsTotal += activitySummary.getActiveMinutes();
                    sedentaryMinsTotal += activitySummary.getSedentaryMinutes();
                } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                    regression1[index][1] = activitySummary.getSedentaryMinutes();
                    regression1[index][0] = activitySummary.getCalories();
                    regression2[index][1] = activitySummary.getSedentaryMinutes();
                    regression2[index][0] = activitySummary.getDistance();
                    regression3[index][1] = activitySummary.getSedentaryMinutes();
                    regression3[index][0] = activitySummary.getActiveMinutes();
                    regression4[index][1] = activitySummary.getSedentaryMinutes();
                    regression4[index][0] = activitySummary.getSteps().doubleValue();
                    stepsTotal += activitySummary.getSteps().doubleValue();
                    caloriesTotal += activitySummary.getCalories();
                    distancesTotal += activitySummary.getDistance();
                    activeMinsTotal += activitySummary.getActiveMinutes();
                    sedentaryMinsTotal += activitySummary.getSedentaryMinutes();
                } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                    regression1[index][1] = activitySummary.getDistance();
                    regression1[index][0] = activitySummary.getCalories();
                    regression2[index][1] = activitySummary.getDistance();
                    regression2[index][0] = activitySummary.getSteps().doubleValue();
                    regression3[index][1] = activitySummary.getDistance();
                    regression3[index][0] = activitySummary.getActiveMinutes();
                    regression4[index][1] = activitySummary.getDistance();
                    regression4[index][0] = activitySummary.getSedentaryMinutes();
                    stepsTotal += activitySummary.getSteps().doubleValue();
                    caloriesTotal += activitySummary.getCalories();
                    distancesTotal += activitySummary.getDistance();
                    activeMinsTotal += activitySummary.getActiveMinutes();
                    sedentaryMinsTotal += activitySummary.getSedentaryMinutes();
                } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                    regression1[index][1] = activitySummary.getCalories();
                    regression1[index][0] = activitySummary.getSteps().doubleValue();
                    regression2[index][1] = activitySummary.getCalories();
                    regression2[index][0] = activitySummary.getDistance();
                    regression3[index][1] = activitySummary.getCalories();
                    regression3[index][0] = activitySummary.getActiveMinutes();
                    regression4[index][1] = activitySummary.getCalories();
                    regression4[index][0] = activitySummary.getSedentaryMinutes();
                    stepsTotal += activitySummary.getSteps().doubleValue();
                    caloriesTotal += activitySummary.getCalories();
                    distancesTotal += activitySummary.getDistance();
                    activeMinsTotal += activitySummary.getActiveMinutes();
                    sedentaryMinsTotal += activitySummary.getSedentaryMinutes();
                }
                index++;
            }


            // linear  regression
            SimpleRegression regression = new SimpleRegression();

            double slope = 0;
            double yintercept = 0;
            double rsqured = 0;
            double correlation = 0;
            double recommendedActivityLevel = 0;
            //double depVarAverage = 0;
            double indepVarAverage1 = 0;
            double indepVarAverage2 = 0;
            double indepVarAverage3 = 0;
            double indepVarAverage4 = 0;

            double stepsAverage = stepsTotal / regression1.length;
            double caloriesAvg = caloriesTotal / regression1.length;
            double activeMinsAvg = activeMinsTotal / regression1.length;
            double sedentaryMinsAvg = sedentaryMinsTotal / regression1.length;
            double sleepAvg = sleepTotal / regression1.length;
            double distancesAvg = distancesTotal / regression1.length;
            double floorsAvg = floorsTotal / regression1.length;

            double stepsGoal = stepsAverage * 1.01; // TODO - set up multiplier for mood goal
            double caloriesGoal = caloriesAvg * 1.01; // TODO - set up multiplier for energy goal
            double activeMinsGoal = activeMinsAvg * 1.01; // TODO - set up multiplier for mood goal
            double sedentaryMinsGoal = sedentaryMinsAvg * 0.99; // TODO - set up multiplier for energy goal
            double distancesGoal = distancesAvg * 1.01; // TODO - set up multiplier for mood goal

            String depVar = activityName;
            String indepVar1 = "";
            String indepVar2 = "";
            String indepVar3 = "";
            String indepVar4 = "";
            String serviceType = application.getString(R.string.moabi_tracker_camel_case);

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                //depVarAverage = stepsAverage;
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = sedentaryMinsAvg;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_steps_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                //depVarAverage = activeMinsAvg;
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = stepsAverage;
                indepVarAverage4 = sedentaryMinsAvg;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_steps_camel_case);
                //depVarAverage = sedentaryMinsAvg;
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = stepsAverage;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                indepVar1 = application.getString(R.string.activity_calories_camel_case);
                indepVar2 = application.getString(R.string.activity_steps_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                //depVarAverage = distancesAvg;
                indepVarAverage1 = caloriesAvg;
                indepVarAverage2 = stepsAverage;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = sedentaryMinsAvg;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                indepVar1 = application.getString(R.string.activity_steps_camel_case);
                indepVar2 = application.getString(R.string.activity_distance_camel_case);
                indepVar3 = application.getString(R.string.activity_active_minutes_camel_case);
                indepVar4 = application.getString(R.string.activity_sedentary_minutes_camel_case);
                //depVarAverage = caloriesAvg;
                indepVarAverage1 = stepsAverage;
                indepVarAverage2 = distancesAvg;
                indepVarAverage3 = activeMinsAvg;
                indepVarAverage4 = sedentaryMinsAvg;
            }

            // regression for steps x other fitbit measurements
            regression.addData(regression1);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression1));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar1, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage1, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar1 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(serviceType);
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage1 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();

            // regression for steps x other fitbit measurements
            regression.addData(regression2);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression2));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar2, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage2, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar2 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(serviceType);
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage2 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();

            // regression for steps x other fitbit measurements
            regression.addData(regression3);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression3));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar3, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage3, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar3 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(serviceType);
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage3 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();

            // regression for steps x other fitbit measurements
            regression.addData(regression4);
            slope = regression.getSlope();
            yintercept = regression.getIntercept();
            rsqured = regression.getRSquare();
            correlation = regression.getR();

            if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                recommendedActivityLevel = (stepsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
            } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
            }

            Log.i(TAG, Arrays.deepToString(regression4));
            if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                        depVar,
                        indepVar4, serviceType,
                        slope, yintercept, rsqured, correlation,
                        indepVarAverage4, recommendedActivityLevel);
                regressionResult.setDepXIndepVars(indepVar4 + serviceType + "X" + depVar + serviceType + "X" + +duration);
                regressionResult.setDepVarType(1L);
                regressionResult.setDuration(duration);
                regressionResult.setDepVarTypeString(serviceType);
                regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                regressionResult.setNumOfData((long) regression1.length);
                Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                        + yintercept + ", Average is " + indepVarAverage4 + ", Recommended activity level is "
                        + recommendedActivityLevel
                        + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                insert(regressionResult);
            }

            regression.clear();
        }
    }

    public void processMoabiWithAppUsage(Map<String, BuiltInActivitySummary> fitbitDailySummaryMap, Map<String, AppUsageSummary> appUsageSummaryMap, int duration) {
        List<List<AppUsage>> appUsageListsList = new ArrayList<>();
        List<BuiltInActivitySummary> dailySummaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();

        for (Map.Entry<String, BuiltInActivitySummary> fitbitEntry : fitbitDailySummaryMap.entrySet()) {
            if (appUsageSummaryMap.get(fitbitEntry.getKey()) != null) {
                dailySummaries.add(fitbitEntry.getValue());
                AppUsageSummary appUsageSummary = appUsageSummaryMap.get(fitbitEntry.getKey());
                if (appUsageSummary.getActivities() != null) {
                    appUsageListsList.add(appUsageSummary.getActivities());
                }
            }
        }

        for (List<AppUsage> summary : appUsageListsList) {
            for (AppUsage appUsage : summary) {
                activitiesSet.add(appUsage.getAppName());
            }
        }

        String serviceType = application.getString(R.string.moabi_tracker_camel_case);

        for (String activityName : activitiesSet) {
            int index = 0;
            double[][] regression1 = new double[dailySummaries.size()][2];
            double[][] regression2 = new double[dailySummaries.size()][2];
            double[][] regression3 = new double[dailySummaries.size()][2];
            double[][] regression4 = new double[dailySummaries.size()][2];
            double[][] regression5 = new double[dailySummaries.size()][2];
            double appUsageTotal = 0;
            double stepsTotal = 0;
            double caloriesTotal = 0;
            double distancesTotal = 0;
            double activeMinsTotal = 0;
            double sedentaryMinsTotal = 0;


            // iterate through each day
            for (List<AppUsage> summaries : appUsageListsList) {
                for (AppUsage activity : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(activity.getAppName())) {
                        regression1[index][0] = activity.getTotalTime();
                        regression2[index][0] = activity.getTotalTime();
                        regression3[index][0] = activity.getTotalTime();
                        regression4[index][0] = activity.getTotalTime();
                        regression5[index][0] = activity.getTotalTime();
                        appUsageTotal += activity.getTotalTime();
                        break;
                    } else {
                        // if the app was not used for the day, set the time to 0.
                        regression1[index][0] = 0;
                        regression2[index][0] = 0;
                        regression3[index][0] = 0;
                        regression4[index][0] = 0;
                        regression5[index][0] = 0;
                        appUsageTotal += 0;
                    }
                }
                index++;
            }

            int i = 0;
            // iterate through each day
            for (BuiltInActivitySummary activitySummary : dailySummaries) {
                // if the app was used for the day, get the recorded time
                regression1[i][1] = activitySummary.getSteps().doubleValue();
                regression2[i][1] = activitySummary.getCalories();
                regression3[i][1] = activitySummary.getDistance();
                regression4[i][1] = activitySummary.getActiveMinutes();
                regression5[i][1] = activitySummary.getSedentaryMinutes();
                stepsTotal += activitySummary.getSteps().doubleValue();
                caloriesTotal += activitySummary.getCalories();
                distancesTotal += activitySummary.getDistance();
                activeMinsTotal += activitySummary.getActiveMinutes();
                sedentaryMinsTotal += activitySummary.getSedentaryMinutes();
                i++;
            }


            // linear  regression
            SimpleRegression regression = new SimpleRegression();

            double slope = 0;
            double yintercept = 0;
            double rsqured = 0;
            double correlation = 0;
            double recommendedActivityLevel = 0;
            double indepVarAverage = appUsageTotal / regression1.length;

            if (TimeUnit.MILLISECONDS.toMinutes((long) indepVarAverage) > 5) {

                double stepsAverage = stepsTotal / regression1.length;
                double caloriesAvg = caloriesTotal / regression1.length;
                double activeMinsAvg = activeMinsTotal / regression1.length;
                double sedentaryMinsAvg = sedentaryMinsTotal / regression1.length;
                double distancesAvg = distancesTotal / regression1.length;

                double stepsGoal = stepsAverage * 1.01; // TODO - set up multiplier for mood goal
                double caloriesGoal = caloriesAvg * 1.01; // TODO - set up multiplier for energy goal
                double activeMinsGoal = activeMinsAvg * 1.01; // TODO - set up multiplier for mood goal
                double sedentaryMinsGoal = sedentaryMinsAvg * 0.99; // TODO - set up multiplier for energy goal
                double distancesGoal = distancesAvg * 1.01; // TODO - set up multiplier for mood goal

                String depVar = application.getString(R.string.activity_steps_camel_case);

                // regression for steps x other fitbit measurements
                regression.addData(regression1);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (stepsGoal - yintercept) / slope;

                Log.i(TAG, Arrays.deepToString(regression1));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + serviceType + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(serviceType);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();

                // regression for steps x other fitbit measurements
                regression.addData(regression2);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (caloriesGoal - yintercept) / slope;
                depVar = application.getString(R.string.activity_calories_camel_case);

                Log.i(TAG, Arrays.deepToString(regression2));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + serviceType + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(serviceType);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();


                // regression for steps x other fitbit measurements
                regression.addData(regression3);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (distancesGoal - yintercept) / slope;
                depVar = application.getString(R.string.activity_distance_camel_case);

                Log.i(TAG, Arrays.deepToString(regression3));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + serviceType + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(serviceType);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }


                regression.clear();

                // regression for steps x other fitbit measurements
                regression.addData(regression4);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (activeMinsGoal - yintercept) / slope;
                depVar = application.getString(R.string.activity_active_minutes_camel_case);

                Log.i(TAG, Arrays.deepToString(regression4));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + serviceType + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(serviceType);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();

                // regression for steps x other fitbit measurements
                regression.addData(regression5);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();

                recommendedActivityLevel = (sedentaryMinsGoal - yintercept) / slope;
                depVar = application.getString(R.string.activity_sedentary_minutes_camel_case);

                Log.i(TAG, Arrays.deepToString(regression5));
                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, application.getString(R.string.phone_usage_camel_case),
                            slope, yintercept, rsqured, correlation,
                            indepVarAverage, recommendedActivityLevel);
                    regressionResult.setDepXIndepVars(activityName + application.getString(R.string.phone_usage_camel_case) + "X" + depVar + serviceType + duration);
                    regressionResult.setDepVarType(1L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(serviceType);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regression1.length);
                    Log.i(TAG, regressionResult.getDepXIndepVars() + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Average is " + indepVarAverage + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();
            }
        }
    }

    public void processMoodAndEnergyWithGoogleFit(Map<String, AverageMood> moodsAndEnergyLevelMap, List<GoogleFitSummary> googleFitSummaries, int duration) {
        List<List<GoogleFitSummary.Summary>> summariesListList = new ArrayList<>();
        List<Double> moodsList = new ArrayList<>();
        List<Double> energyLevelsList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        for (GoogleFitSummary googleFitSummary : googleFitSummaries) {
            String date = googleFitSummary.getDate();
            if (moodsAndEnergyLevelMap.get(date) != null) {
                // create a 2d array double[][] data.
                List<GoogleFitSummary.Summary> summaries = googleFitSummary.getSummaries();
                if (moodsAndEnergyLevelMap.get(date).getAverageMood() != null && moodsAndEnergyLevelMap.get(date).getAverageEnergyLevel() != null) {
                    moodsList.add(moodsAndEnergyLevelMap.get(date).getAverageMood());
                    energyLevelsList.add(moodsAndEnergyLevelMap.get(date).getAverageEnergyLevel());
                }
                summariesListList.add(summaries);
                for (GoogleFitSummary.Summary summary : summaries) {
                    activitiesSet.add(summary.getName());
                }
            }
        }
        String indepVarType = application.getString(R.string.googlefit_camel_case);
        if (moodsList.size() > 2) {

            Map<String, Object> moodRegressionMap = new HashMap<>();
            Map<String, Object> energyRegressionMap = new HashMap<>();
            //Map<String, Object> activityAverageMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionMoodsXActivity = new double[moodsList.size()][2];
                double[][] regressionEnergyXActivity = new double[moodsList.size()][2];
                double moodTotal = 0;
                double energyLevelTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (List<GoogleFitSummary.Summary> summaries : summariesListList) {
                    for (GoogleFitSummary.Summary activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity.getName())) {
                            regressionMoodsXActivity[index][0] = activity.getValue();
                            regressionEnergyXActivity[index][0] = activity.getValue();
                            activityTotal += activity.getValue();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionMoodsXActivity[index][0] = 0;
                            regressionEnergyXActivity[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    moodTotal += moodsList.get(index);
                    energyLevelTotal += energyLevelsList.get(index);
                    regressionMoodsXActivity[index][1] = moodsList.get(index);
                    regressionEnergyXActivity[index][1] = energyLevelsList.get(index);
                    index++;
                }

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double moodAverage = moodTotal / regressionMoodsXActivity.length;
                double energyAverage = energyLevelTotal / regressionEnergyXActivity.length;
                double activityAverage = activityTotal / regressionMoodsXActivity.length;
                double moodGoal = moodAverage * 1.01; // TODO - set up multiplier for mood goal
                double energyGoal = energyAverage * 1.01; // TODO - set up multiplier for energy goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionMoodsXActivity);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (moodGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            application.getString(R.string.mood_camel_case),
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    moodRegressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.mood_camel_case) + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();

                // regression for energy x active minutes
                regression.addData(regressionEnergyXActivity);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (energyGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult =
                            new SimpleRegressionSummary(application.getString(R.string.energy_camel_case),
                                    activityName,
                                    application.getString(R.string.googlefit_camel_case), slope, yintercept,
                                    rsqured, correlation,
                                    activityAverage, recommendedActivityLevel);
                    energyRegressionMap.put(activityName, regressionResult);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.energy_camel_case) + duration);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
            Log.i(TAG, moodRegressionMap.toString());
            Log.i(TAG, energyRegressionMap.toString());
        }
    }

    public void processMoodAndEnergyWithFitbit(Map<String, AverageMood> moodsAndEnergyLevelMap, List<FitbitDailySummary> fitbitDailySummaries, int duration) {
        List<Double> moodsList = new ArrayList<>();
        List<Double> energyLevelsList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<FitbitDailySummary> summaries = new ArrayList<>();
        for (FitbitDailySummary fitbitDailySummary : fitbitDailySummaries) {
            String date = fitbitDailySummary.getDate();
            if (moodsAndEnergyLevelMap.get(date) != null) {
                // create a 2d array double[][] data.
                summaries.add(fitbitDailySummary);
                if (moodsAndEnergyLevelMap.get(date).getAverageMood() != null && moodsAndEnergyLevelMap.get(date).getAverageEnergyLevel() != null) {
                    moodsList.add(moodsAndEnergyLevelMap.get(date).getAverageMood());
                    energyLevelsList.add(moodsAndEnergyLevelMap.get(date).getAverageEnergyLevel());
                }
            }
        }

        String indepVarType = application.getString(R.string.fitbit_camel_case);
        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sleep_camel_case));
        activitiesSet.add(application.getString(R.string.activity_floors_camel_case));

        if (moodsList.size() > 2) {

            Map<String, Object> moodRegressionMap = new HashMap<>();
            Map<String, Object> energyRegressionMap = new HashMap<>();
            //Map<String, Object> activityAverageMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionMoodsXActivity = new double[moodsList.size()][2];
                double[][] regressionEnergyXActivity = new double[moodsList.size()][2];
                double moodTotal = 0;
                double energyLevelTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (FitbitDailySummary activitySummary : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                        regressionMoodsXActivity[index][0] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                        regressionEnergyXActivity[index][0] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                        regressionMoodsXActivity[index][0] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep().doubleValue();
                        regressionEnergyXActivity[index][0] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep().doubleValue();
                        activityTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                        Long activeMinutes = activitySummary.getActivitySummary().getSummary()
                                .getFairlyActiveMinutes() + activitySummary.getActivitySummary()
                                .getSummary().getVeryActiveMinutes();
                        regressionMoodsXActivity[index][0] = activeMinutes.doubleValue();
                        regressionEnergyXActivity[index][0] = activeMinutes.doubleValue();
                        activityTotal += activeMinutes.doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                        regressionMoodsXActivity[index][0] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes().doubleValue();
                        regressionEnergyXActivity[index][0] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                        regressionMoodsXActivity[index][0] = activitySummary.getActivitySummary().getSummary().getFloors().doubleValue();
                        regressionEnergyXActivity[index][0] = activitySummary.getActivitySummary().getSummary().getFloors().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getFloors().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                        regressionMoodsXActivity[index][0] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                        regressionEnergyXActivity[index][0] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                        regressionMoodsXActivity[index][0] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                        regressionEnergyXActivity[index][0] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    }

                    moodTotal += moodsList.get(index);
                    energyLevelTotal += energyLevelsList.get(index);
                    regressionMoodsXActivity[index][1] = moodsList.get(index);
                    regressionEnergyXActivity[index][1] = energyLevelsList.get(index);
                    index++;
                }

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double moodAverage = moodTotal / regressionMoodsXActivity.length;
                double energyAverage = energyLevelTotal / regressionEnergyXActivity.length;
                double activityAverage = activityTotal / regressionMoodsXActivity.length;
                double moodGoal = moodAverage * 1.01; // TODO - set up multiplier for mood goal
                double energyGoal = energyAverage * 1.01; // TODO - set up multiplier for energy goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionMoodsXActivity);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (moodGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            application.getString(R.string.mood_camel_case),
                            activityName, application.getString(R.string.fitbit_camel_case),
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    moodRegressionMap.put(activityName, regressionResult);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.mood_camel_case) + duration);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();

                // regression for energy x active minutes
                regression.addData(regressionEnergyXActivity);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (energyGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult =
                            new SimpleRegressionSummary(application.getString(R.string.energy_camel_case),
                                    activityName,
                                    application.getString(R.string.fitbit_camel_case), slope, yintercept,
                                    rsqured, correlation,
                                    activityAverage, recommendedActivityLevel);
                    energyRegressionMap.put(activityName, regressionResult);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.energy_camel_case) + duration);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
            Log.i(TAG, moodRegressionMap.toString());
            Log.i(TAG, energyRegressionMap.toString());
        }
    }

    public void processMoodAndEnergyWithAppUsage(Map<String, AverageMood> moodsAndEnergyLevelMap, List<AppUsageSummary> appUsageSummaries, int duration) {
        List<List<AppUsage>> summariesListList = new ArrayList<>();
        List<Double> moodsList = new ArrayList<>();
        List<Double> energyLevelsList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        for (AppUsageSummary appUsageSummary : appUsageSummaries) {
            String date = appUsageSummary.getDate();
            if (moodsAndEnergyLevelMap.get(date) != null) {
                // create a 2d array double[][] data.
                List<AppUsage> summaries = appUsageSummary.getActivities();
                if (moodsAndEnergyLevelMap.get(date).getAverageMood() != null && moodsAndEnergyLevelMap.get(date).getAverageEnergyLevel() != null) {
                    moodsList.add(moodsAndEnergyLevelMap.get(date).getAverageMood());
                    energyLevelsList.add(moodsAndEnergyLevelMap.get(date).getAverageEnergyLevel());
                }
                summariesListList.add(summaries);
                for (AppUsage summary : summaries) {
                    activitiesSet.add(summary.getAppName());
                }
            }
        }

        String indepVarType = application.getString(R.string.phone_usage_camel_case);
        if (moodsList.size() > 2) {

            Map<String, Object> moodRegressionMap = new HashMap<>();
            Map<String, Object> energyRegressionMap = new HashMap<>();
            //Map<String, Object> activityAverageMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionMoodsXActivity = new double[moodsList.size()][2];
                double[][] regressionEnergyXActivity = new double[moodsList.size()][2];
                double moodTotal = 0;
                double energyLevelTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (List<AppUsage> summaries : summariesListList) {
                    for (AppUsage activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity.getAppName())) {
                            regressionMoodsXActivity[index][0] = activity.getTotalTime();
                            regressionEnergyXActivity[index][0] = activity.getTotalTime();
                            activityTotal += activity.getTotalTime();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionMoodsXActivity[index][0] = 0;
                            regressionEnergyXActivity[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    moodTotal += moodsList.get(index);
                    energyLevelTotal += energyLevelsList.get(index);
                    regressionMoodsXActivity[index][1] = moodsList.get(index);
                    regressionEnergyXActivity[index][1] = energyLevelsList.get(index);
                    index++;
                }
                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionMoodsXActivity));
                //Log.i(TAG, activityName + " x Energy: " + Arrays.deepToString(regressionEnergyXApp));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double moodAverage = moodTotal / regressionMoodsXActivity.length;
                double energyAverage = energyLevelTotal / regressionEnergyXActivity.length;
                double activityAverage = activityTotal / regressionMoodsXActivity.length;

                if (TimeUnit.MILLISECONDS.toMinutes((long) activityAverage) > 5) {
                    double moodGoal = moodAverage * 1.01; // TODO - set up multiplier for mood goal
                    double energyGoal = energyAverage * 1.01; // TODO - set up multiplier for energy goal
                    //activityAverageMap.put(activityName, activityAverage);
                    //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                    //Log.i(TAG, "Energy Level average: " + energyAverage);

                    // regression for mood x app
                    regression.addData(regressionMoodsXActivity);
                    slope = regression.getSlope();
                    yintercept = regression.getIntercept();
                    rsqured = regression.getRSquare();
                    correlation = regression.getR();
                    recommendedActivityLevel = (moodGoal - yintercept) / slope;

                    if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                        SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                                application.getString(R.string.mood_camel_case),
                                activityName, application.getString(R.string.phone_usage_camel_case),
                                slope, yintercept, rsqured, correlation,
                                activityAverage, recommendedActivityLevel);
                        moodRegressionMap.put(activityName, regressionResult);
                        regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.mood_camel_case) + duration);
                        regressionResult.setDepVarType(0L);
                        regressionResult.setDuration(duration);
                        regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                        regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                        regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                        regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                        Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                                + yintercept + ", Recommended activity level is "
                                + recommendedActivityLevel
                                + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                        insert(regressionResult);
                    }

                    regression.clear();

                    // regression for energy x active minutes
                    regression.addData(regressionEnergyXActivity);
                    slope = regression.getSlope();
                    yintercept = regression.getIntercept();
                    rsqured = regression.getRSquare();
                    correlation = regression.getR();
                    recommendedActivityLevel = (energyGoal - yintercept) / slope;

                    if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                        SimpleRegressionSummary regressionResult =
                                new SimpleRegressionSummary(application.getString(R.string.energy_camel_case),
                                        activityName,
                                        application.getString(R.string.phone_usage_camel_case), slope, yintercept,
                                        rsqured, correlation,
                                        activityAverage, recommendedActivityLevel);
                        energyRegressionMap.put(activityName, regressionResult);
                        regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.energy_camel_case) + duration);
                        regressionResult.setDepVarType(0L);
                        regressionResult.setDuration(duration);
                        regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                        regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                        regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                        regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                        Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                                + yintercept + ", Recommended activity level is "
                                + recommendedActivityLevel
                                + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                        insert(regressionResult);
                    }
                    regression.clear();
                }
                Log.i(TAG, moodRegressionMap.toString());
                Log.i(TAG, energyRegressionMap.toString());
            }
        }
    }

    public void processMoodAndEnergyWithBuiltInFitness(Map<String, AverageMood> moodsAndEnergyLevelMap, List<BuiltInActivitySummary> activitySummaries, int duration) {
        List<Double> moodsList = new ArrayList<>();
        List<Double> energyLevelsList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<BuiltInActivitySummary> summaries = new ArrayList<>();
        for (BuiltInActivitySummary activitySummary : activitySummaries) {
            String date = activitySummary.getDate();
            if (moodsAndEnergyLevelMap.get(date) != null) {
                // create a 2d array double[][] data.
                if (moodsAndEnergyLevelMap.get(date).getAverageMood() != null && moodsAndEnergyLevelMap.get(date).getAverageEnergyLevel() != null) {
                    summaries.add(activitySummary);
                    moodsList.add(moodsAndEnergyLevelMap.get(date).getAverageMood());
                    energyLevelsList.add(moodsAndEnergyLevelMap.get(date).getAverageEnergyLevel());
                }
            }
        }

        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));

        String indepVarType = application.getString(R.string.moabi_tracker_camel_case);
        if (moodsList.size() > 2) {

            Map<String, Object> moodRegressionMap = new HashMap<>();
            Map<String, Object> energyRegressionMap = new HashMap<>();
            //Map<String, Object> activityAverageMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionMoodsXActivity = new double[moodsList.size()][2];
                double[][] regressionEnergyXActivity = new double[moodsList.size()][2];
                double moodTotal = 0;
                double energyLevelTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (BuiltInActivitySummary activitySummary : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                        regressionMoodsXActivity[index][0] = activitySummary.getSteps().doubleValue();
                        regressionEnergyXActivity[index][0] = activitySummary.getSteps().doubleValue();
                        activityTotal += activitySummary.getSteps().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                        Long activeMinutes = activitySummary.getActiveMinutes();
                        regressionMoodsXActivity[index][0] = activeMinutes.doubleValue();
                        regressionEnergyXActivity[index][0] = activeMinutes.doubleValue();
                        activityTotal += activeMinutes.doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                        regressionMoodsXActivity[index][0] = activitySummary.getSedentaryMinutes().doubleValue();
                        regressionEnergyXActivity[index][0] = activitySummary.getSedentaryMinutes().doubleValue();
                        activityTotal += activitySummary.getSedentaryMinutes().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                        regressionMoodsXActivity[index][0] = activitySummary.getDistance();
                        regressionEnergyXActivity[index][0] = activitySummary.getDistance();
                        activityTotal += activitySummary.getDistance();
                    } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                        regressionMoodsXActivity[index][0] = activitySummary.getCalories();
                        regressionEnergyXActivity[index][0] = activitySummary.getCalories();
                        activityTotal += activitySummary.getCalories();
                    }

                    moodTotal += moodsList.get(index);
                    energyLevelTotal += energyLevelsList.get(index);
                    regressionMoodsXActivity[index][1] = moodsList.get(index);
                    regressionEnergyXActivity[index][1] = energyLevelsList.get(index);
                    index++;
                }
                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionMoodsXActivity));
                //Log.i(TAG, activityName + " x Energy: " + Arrays.deepToString(regressionEnergyXApp));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double moodAverage = moodTotal / regressionMoodsXActivity.length;
                double energyAverage = energyLevelTotal / regressionEnergyXActivity.length;
                double activityAverage = activityTotal / regressionMoodsXActivity.length;
                double moodGoal = moodAverage * 1.01; // TODO - set up multiplier for mood goal
                double energyGoal = energyAverage * 1.01; // TODO - set up multiplier for energy goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionMoodsXActivity);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (moodGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            application.getString(R.string.mood_camel_case),
                            activityName, application.getString(R.string.moabi_tracker_camel_case),
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    moodRegressionMap.put(activityName, regressionResult);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.mood_camel_case) + duration);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();

                // regression for energy x active minutes
                regression.addData(regressionEnergyXActivity);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (energyGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult =
                            new SimpleRegressionSummary(application.getString(R.string.energy_camel_case),
                                    activityName,
                                    application.getString(R.string.moabi_tracker_camel_case), slope, yintercept,
                                    rsqured, correlation,
                                    activityAverage, recommendedActivityLevel);
                    energyRegressionMap.put(activityName, regressionResult);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.energy_camel_case) + duration);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
            Log.i(TAG, moodRegressionMap.toString());
            Log.i(TAG, energyRegressionMap.toString());
        }
    }

    public void processMoodAndEnergyWithWeather(Map<String, AverageMood> moodsAndEnergyLevelMap, List<WeatherDailySummary> weatherDailySummaries, int duration) {
        List<Double> moodsList = new ArrayList<>();
        List<Double> energyLevelsList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<WeatherDailySummary> summaries = new ArrayList<>();
        for (WeatherDailySummary weatherDailySummary : weatherDailySummaries) {
            String date = weatherDailySummary.getDate();
            if (moodsAndEnergyLevelMap.get(date) != null) {
                // create a 2d array double[][] data.
                if (moodsAndEnergyLevelMap.get(date).getAverageMood() != null && moodsAndEnergyLevelMap.get(date).getAverageEnergyLevel() != null) {
                    summaries.add(weatherDailySummary);
                    moodsList.add(moodsAndEnergyLevelMap.get(date).getAverageMood());
                    energyLevelsList.add(moodsAndEnergyLevelMap.get(date).getAverageEnergyLevel());
                }
            }
        }

        activitiesSet.add(application.getString(R.string.humidity_camel_case));
        activitiesSet.add(application.getString(R.string.temperature_camel_case));
        activitiesSet.add(application.getString(R.string.precipitation_camel_case));

        String indepVarType = application.getString(R.string.weather_camel_case);
        if (moodsList.size() > 2) {

            Map<String, Object> moodRegressionMap = new HashMap<>();
            Map<String, Object> energyRegressionMap = new HashMap<>();
            //Map<String, Object> activityAverageMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionMoodsXActivity = new double[moodsList.size()][2];
                double[][] regressionEnergyXActivity = new double[moodsList.size()][2];
                double moodTotal = 0;
                double energyLevelTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (WeatherDailySummary weatherDailySummary : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(application.getString(R.string.humidity_camel_case))) {
                        regressionMoodsXActivity[index][0] = weatherDailySummary.getAvgHumidity();
                        regressionEnergyXActivity[index][0] = weatherDailySummary.getAvgHumidity();
                        activityTotal += weatherDailySummary.getAvgHumidity();
                    } else if (activityName.equals(application.getString(R.string.temperature_camel_case))) {
                        double avgTempC = weatherDailySummary.getAvgTempC();
                        regressionMoodsXActivity[index][0] = avgTempC;
                        regressionEnergyXActivity[index][0] = avgTempC;
                        activityTotal += avgTempC;
                    } else if (activityName.equals(application.getString(R.string.precipitation_camel_case))) {
                        regressionMoodsXActivity[index][0] = weatherDailySummary.getTotalPrecipmm();
                        regressionEnergyXActivity[index][0] = weatherDailySummary.getTotalPrecipmm();
                        activityTotal += weatherDailySummary.getTotalPrecipmm();
                    }

                    moodTotal += moodsList.get(index);
                    energyLevelTotal += energyLevelsList.get(index);
                    regressionMoodsXActivity[index][1] = moodsList.get(index);
                    regressionEnergyXActivity[index][1] = energyLevelsList.get(index);
                    index++;
                }
                //Log.i(TAG, activityName + " x Energy: " + Arrays.deepToString(regressionEnergyXApp));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double moodAverage = moodTotal / regressionMoodsXActivity.length;
                double energyAverage = energyLevelTotal / regressionEnergyXActivity.length;
                double activityAverage = activityTotal / regressionMoodsXActivity.length;
                double moodGoal = moodAverage * 1.01; // TODO - set up multiplier for mood goal
                double energyGoal = energyAverage * 1.01; // TODO - set up multiplier for energy goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionMoodsXActivity);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (moodGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            application.getString(R.string.mood_camel_case),
                            activityName, application.getString(R.string.weather_camel_case),
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    moodRegressionMap.put(activityName, regressionResult);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.mood_camel_case) + duration);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }

                regression.clear();

                // regression for energy x active minutes
                regression.addData(regressionEnergyXActivity);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (energyGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult =
                            new SimpleRegressionSummary(application.getString(R.string.energy_camel_case),
                                    activityName,
                                    application.getString(R.string.weather_camel_case), slope, yintercept,
                                    rsqured, correlation,
                                    activityAverage, recommendedActivityLevel);
                    energyRegressionMap.put(activityName, regressionResult);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.energy_camel_case) + duration);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDuration(duration);
                    regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
            Log.i(TAG, moodRegressionMap.toString());
            Log.i(TAG, energyRegressionMap.toString());
        }
    }

    public void processMoodAndEnergyWithBAActivity(Map<String, AverageMood> moodsAndEnergyLevelMap, List<BAActivityEntry> baActivityEntries, int duration) {
        Map<String, List<String>> activitiesMap = new LinkedHashMap<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        for (BAActivityEntry activitySummary : baActivityEntries) {
            String date = formattedTime.convertLongToYYYYMMDD(activitySummary.getDateInLong());
            if (moodsAndEnergyLevelMap.get(date) != null) {
                if (activitiesMap.get(date) != null) {
                    List<String> oldList = activitiesMap.get(date);
                    oldList.add(activitySummary.getName());
                    activitiesMap.put(date, oldList);
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(activitySummary.getName());
                    activitiesMap.put(date, list);
                }
                activitiesSet.add(activitySummary.getName());
            }
        }
        List<List<String>> summariesListList = new ArrayList<>();
        List<Double> moodsList = new ArrayList<>();
        List<Double> energyLevelsList = new ArrayList<>();

        for (Map.Entry<String, AverageMood> entry : moodsAndEnergyLevelMap.entrySet()) {
            String date = entry.getKey();
            if (activitiesMap.get(date) != null) {
                List<String> activities = activitiesMap.get(date);
                moodsList.add(moodsAndEnergyLevelMap.get(date).getAverageMood());
                energyLevelsList.add(moodsAndEnergyLevelMap.get(date).getAverageEnergyLevel());
                summariesListList.add(activities);
            }
        }

        String indepVarType = application.getString(R.string.baactivity_camel_case);
        if (moodsList.size() > 2) {

            Map<String, Object> moodRegressionMap = new HashMap<>();
            Map<String, Object> energyRegressionMap = new HashMap<>();
            //Map<String, Object> activityAverageMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionMoodsXActivity = new double[moodsList.size()][2];
                double[][] regressionEnergyXActivity = new double[moodsList.size()][2];
                double moodTotal = 0;
                double energyLevelTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (List<String> summaries : summariesListList) {
                    for (String activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity)) {
                            regressionMoodsXActivity[index][0] = Collections.frequency(summaries, activityName);
                            regressionEnergyXActivity[index][0] = Collections.frequency(summaries, activityName);
                            activityTotal += Collections.frequency(summaries, activityName);
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionMoodsXActivity[index][0] = 0;
                            regressionEnergyXActivity[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    moodTotal += moodsList.get(index);
                    energyLevelTotal += energyLevelsList.get(index);
                    regressionMoodsXActivity[index][1] = moodsList.get(index);
                    regressionEnergyXActivity[index][1] = energyLevelsList.get(index);
                    index++;
                }
                //Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionMoodsXActivity));
                //Log.i(TAG, activityName + " x Energy: " + Arrays.deepToString(regressionEnergyXApp));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double moodAverage = moodTotal / regressionMoodsXActivity.length;
                double energyAverage = energyLevelTotal / regressionEnergyXActivity.length;
                double activityAverage = activityTotal / regressionMoodsXActivity.length;

                if (TimeUnit.MILLISECONDS.toMinutes((long) activityAverage) > 5) {
                    double moodGoal = moodAverage * 1.01; // TODO - set up multiplier for mood goal
                    double energyGoal = energyAverage * 1.01; // TODO - set up multiplier for energy goal
                    //activityAverageMap.put(activityName, activityAverage);
                    //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                    //Log.i(TAG, "Energy Level average: " + energyAverage);

                    // regression for mood x app
                    regression.addData(regressionMoodsXActivity);
                    slope = regression.getSlope();
                    yintercept = regression.getIntercept();
                    rsqured = regression.getRSquare();
                    correlation = regression.getR();
                    recommendedActivityLevel = (moodGoal - yintercept) / slope;

                    if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                        SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                                application.getString(R.string.mood_camel_case),
                                activityName, application.getString(R.string.baactivity_camel_case),
                                slope, yintercept, rsqured, correlation,
                                activityAverage, recommendedActivityLevel);
                        moodRegressionMap.put(activityName, regressionResult);
                        regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.mood_camel_case) + duration);
                        regressionResult.setDepVarType(0L);
                        regressionResult.setDuration(duration);
                        regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                        regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                        regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                        regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                        Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                                + yintercept + ", Recommended activity level is "
                                + recommendedActivityLevel
                                + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                        insert(regressionResult);
                    }

                    regression.clear();

                    // regression for energy x active minutes
                    regression.addData(regressionEnergyXActivity);
                    slope = regression.getSlope();
                    yintercept = regression.getIntercept();
                    rsqured = regression.getRSquare();
                    correlation = regression.getR();
                    recommendedActivityLevel = (energyGoal - yintercept) / slope;

                    if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                        SimpleRegressionSummary regressionResult =
                                new SimpleRegressionSummary(application.getString(R.string.energy_camel_case),
                                        activityName,
                                        application.getString(R.string.baactivity_camel_case), slope, yintercept,
                                        rsqured, correlation,
                                        activityAverage, recommendedActivityLevel);
                        energyRegressionMap.put(activityName, regressionResult);
                        regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.energy_camel_case) + duration);
                        regressionResult.setDepVarType(0L);
                        regressionResult.setDuration(duration);
                        regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                        regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                        regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                        regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                        Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                                + yintercept + ", Recommended activity level is "
                                + recommendedActivityLevel
                                + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                        insert(regressionResult);
                    }
                    regression.clear();
                }
                Log.i(TAG, moodRegressionMap.toString());
                Log.i(TAG, energyRegressionMap.toString());
            }
        }
    }

    public void processMoodAndEnergyWithTimedActivity(Map<String, AverageMood> moodsAndEnergyLevelMap, List<TimedActivitySummary> timedActivities, int duration) {
        Map<String, List<TimedActivity>> activitiesMap = new LinkedHashMap<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        for (TimedActivitySummary timedActivitySummary : timedActivities) {
            String date = timedActivitySummary.getDate();
            if (moodsAndEnergyLevelMap.get(date) != null) {
                if (activitiesMap.get(date) != null) {
                    List<TimedActivity> oldList = activitiesMap.get(date);
                    Long d = timedActivitySummary.getDuration();
                    Boolean hasOld = false;
                    for (TimedActivity activity : oldList) {
                        if (activity.getInputName().equals(timedActivitySummary.getInputName())) {
                            activity.setDuration(activity.getDuration() + d);
                            hasOld = true;
                        }
                    }
                    if (hasOld) {
                    } else {
                        TimedActivity activity = new TimedActivity();
                        activity.setInputName(timedActivitySummary.getInputName());
                        activity.setDuration(timedActivitySummary.getDuration());
                        oldList.add(activity);
                    }
                    activitiesMap.put(date, oldList);
                } else {
                    List<TimedActivity> list = new ArrayList<>();
                    TimedActivity activity = new TimedActivity();
                    activity.setInputName(timedActivitySummary.getInputName());
                    activity.setDuration(timedActivitySummary.getDuration());
                    list.add(activity);
                    activitiesMap.put(date, list);
                }
                activitiesSet.add(timedActivitySummary.getInputName());
            }
        }
        List<List<TimedActivity>> summariesListList = new ArrayList<>();
        List<Double> moodsList = new ArrayList<>();
        List<Double> energyLevelsList = new ArrayList<>();

        for (Map.Entry<String, AverageMood> entry : moodsAndEnergyLevelMap.entrySet()) {
            String date = entry.getKey();
            if (activitiesMap.get(date) != null) {
                List<TimedActivity> activities = activitiesMap.get(date);
                moodsList.add(moodsAndEnergyLevelMap.get(date).getAverageMood());
                energyLevelsList.add(moodsAndEnergyLevelMap.get(date).getAverageEnergyLevel());
                summariesListList.add(activities);
            }
        }

        String indepVarType = application.getString(R.string.timer_camel_case);
        if (moodsList.size() > 2) {

            Map<String, Object> moodRegressionMap = new HashMap<>();
            Map<String, Object> energyRegressionMap = new HashMap<>();
            //Map<String, Object> activityAverageMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionMoodsXActivity = new double[moodsList.size()][2];
                double[][] regressionEnergyXActivity = new double[moodsList.size()][2];
                double moodTotal = 0;
                double energyLevelTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (List<TimedActivity> summaries : summariesListList) {
                    for (TimedActivity timedActivity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(timedActivity.getInputName())) {
                            regressionMoodsXActivity[index][0] = timedActivity.getDuration();
                            regressionEnergyXActivity[index][0] = timedActivity.getDuration();
                            activityTotal += timedActivity.getDuration();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionMoodsXActivity[index][0] = 0;
                            regressionEnergyXActivity[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    moodTotal += moodsList.get(index);
                    energyLevelTotal += energyLevelsList.get(index);
                    regressionMoodsXActivity[index][1] = moodsList.get(index);
                    regressionEnergyXActivity[index][1] = energyLevelsList.get(index);
                    index++;
                }
                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionMoodsXActivity));
                //Log.i(TAG, activityName + " x Energy: " + Arrays.deepToString(regressionEnergyXApp));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double moodAverage = moodTotal / regressionMoodsXActivity.length;
                double energyAverage = energyLevelTotal / regressionEnergyXActivity.length;
                double activityAverage = activityTotal / regressionMoodsXActivity.length;

                if (TimeUnit.MILLISECONDS.toMinutes((long) activityAverage) > 5) {
                    double moodGoal = moodAverage * 1.01; // TODO - set up multiplier for mood goal
                    double energyGoal = energyAverage * 1.01; // TODO - set up multiplier for energy goal
                    //activityAverageMap.put(activityName, activityAverage);
                    //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                    //Log.i(TAG, "Energy Level average: " + energyAverage);

                    // regression for mood x app
                    regression.addData(regressionMoodsXActivity);
                    slope = regression.getSlope();
                    yintercept = regression.getIntercept();
                    rsqured = regression.getRSquare();
                    correlation = regression.getR();
                    recommendedActivityLevel = (moodGoal - yintercept) / slope;

                    if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                        SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                                application.getString(R.string.mood_camel_case),
                                activityName, application.getString(R.string.timer_camel_case),
                                slope, yintercept, rsqured, correlation,
                                activityAverage, recommendedActivityLevel);
                        moodRegressionMap.put(activityName, regressionResult);
                        regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.mood_camel_case) + duration);
                        regressionResult.setDepVarType(0L);
                        regressionResult.setDuration(duration);
                        regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                        regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                        regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                        regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                        Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                                + yintercept + ", Recommended activity level is "
                                + recommendedActivityLevel
                                + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                        insert(regressionResult);
                    }

                    regression.clear();

                    // regression for energy x active minutes
                    regression.addData(regressionEnergyXActivity);
                    slope = regression.getSlope();
                    yintercept = regression.getIntercept();
                    rsqured = regression.getRSquare();
                    correlation = regression.getR();
                    recommendedActivityLevel = (energyGoal - yintercept) / slope;

                    if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                        SimpleRegressionSummary regressionResult =
                                new SimpleRegressionSummary(application.getString(R.string.energy_camel_case),
                                        activityName,
                                        application.getString(R.string.timer_camel_case), slope, yintercept,
                                        rsqured, correlation,
                                        activityAverage, recommendedActivityLevel);
                        energyRegressionMap.put(activityName, regressionResult);
                        regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + application.getString(R.string.energy_camel_case) + duration);
                        regressionResult.setDepVarType(0L);
                        regressionResult.setDuration(duration);
                        regressionResult.setDepVarTypeString(application.getString(R.string.mood_and_energy_camel_case));
                        regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                        regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                        regressionResult.setNumOfData((long) regressionMoodsXActivity.length);
                        Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                                + yintercept + ", Recommended activity level is "
                                + recommendedActivityLevel
                                + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                        insert(regressionResult);
                    }
                    regression.clear();
                }
                Log.i(TAG, moodRegressionMap.toString());
                Log.i(TAG, energyRegressionMap.toString());
            }
        }
    }

    public void processStressWithFitbit(List<DailyStress> dailyStresses, List<FitbitDailySummary> fitbitDailySummaries, int duration) {
        String depVar = application.getString(R.string.stress_camel_case);
        String indepVarType = application.getString(R.string.fitbit_camel_case);
        List<FitbitDailySummary> summaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyStress> refinedList = new ArrayList<>();
        for (FitbitDailySummary fitbitDailySummary : fitbitDailySummaries) {
            String date = fitbitDailySummary.getDate();
            for (DailyStress dailyStress : dailyStresses) {
                if (dailyStress.getDate().equals(date)) {
                    refinedList.add(dailyStress);
                    summaries.add(fitbitDailySummary);
                    break;
                }
            }
        }

        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sleep_camel_case));
        activitiesSet.add(application.getString(R.string.activity_floors_camel_case));

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (FitbitDailySummary activitySummary : summaries) {
                    if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep().doubleValue();
                        activityTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                        Long activeMinutes = activitySummary.getActivitySummary().getSummary()
                                .getFairlyActiveMinutes() + activitySummary.getActivitySummary()
                                .getSummary().getVeryActiveMinutes();
                        regressionDepVarXIndepVar[index][0] = activeMinutes.doubleValue();
                        activityTotal += activeMinutes.doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getFloors().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getFloors().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    }
                    depVarTotal += refinedList.get(index).getAverageStress();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageStress();
                    index++;
                }
                //Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processStressWithGoogleFit(List<DailyStress> dailyStresses, List<GoogleFitSummary> googleFitSummaries, int duration) {
        String depVar = application.getString(R.string.stress_camel_case);
        String indepVarType = application.getString(R.string.googlefit_camel_case);
        List<List<GoogleFitSummary.Summary>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyStress> refinedList = new ArrayList<>();
        for (GoogleFitSummary googleFitSummary : googleFitSummaries) {
            String date = googleFitSummary.getDate();
            for (DailyStress dailyStress : dailyStresses) {
                if (dailyStress.getDate().equals(date)) {
                    refinedList.add(dailyStress);
                    List<GoogleFitSummary.Summary> summaries = googleFitSummary.getSummaries();
                    summariesListList.add(summaries);
                    for (GoogleFitSummary.Summary summary : summaries) {
                        activitiesSet.add(summary.getName());
                    }
                    break;
                }
            }
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (List<GoogleFitSummary.Summary> summaries : summariesListList) {
                    for (GoogleFitSummary.Summary activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity.getName())) {
                            regressionDepVarXIndepVar[index][0] = activity.getValue();
                            activityTotal += activity.getValue();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageStress();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageStress();
                    index++;
                }
                //Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processStressWithAppUsage(List<DailyStress> dailyStresses, List<AppUsageSummary> appUsageSummaries, int duration) {
        String depVar = application.getString(R.string.stress_camel_case);
        String indepVarType = application.getString(R.string.phone_usage_camel_case);
        List<List<AppUsage>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyStress> refinedList = new ArrayList<>();
        for (AppUsageSummary appUsageSummary : appUsageSummaries) {
            String date = appUsageSummary.getDate();
            for (DailyStress dailyStress : dailyStresses) {
                if (dailyStress.getDate().equals(date)) {
                    refinedList.add(dailyStress);
                    List<AppUsage> summaries = appUsageSummary.getActivities();
                    summariesListList.add(summaries);
                    for (AppUsage summary : summaries) {
                        activitiesSet.add(summary.getAppName());
                    }
                    break;
                }
            }
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (List<AppUsage> summaries : summariesListList) {
                    for (AppUsage activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity.getAppName())) {
                            regressionDepVarXIndepVar[index][0] = activity.getTotalTime();
                            activityTotal += activity.getTotalTime();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageStress();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageStress();
                    index++;
                }
                //Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processStressWithBuiltInFitness(List<DailyStress> dailyStresses, List<BuiltInActivitySummary> activitySummaries, int duration) {
        String depVar = application.getString(R.string.stress_camel_case);
        String indepVarType = application.getString(R.string.moabi_tracker_camel_case);
        List<BuiltInActivitySummary> summaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyStress> refinedList = new ArrayList<>();
        for (BuiltInActivitySummary activitySummary : activitySummaries) {
            String date = activitySummary.getDate();
            for (DailyStress dailyStress : dailyStresses) {
                if (dailyStress.getDate().equals(date)) {
                    refinedList.add(dailyStress);
                    summaries.add(activitySummary);
                    break;
                }
            }
        }

        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (BuiltInActivitySummary activitySummary : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getSteps().doubleValue();
                        activityTotal += activitySummary.getSteps().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                        Long activeMinutes = activitySummary.getActiveMinutes();
                        regressionDepVarXIndepVar[index][0] = activeMinutes.doubleValue();
                        activityTotal += activeMinutes.doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getSedentaryMinutes().doubleValue();
                        activityTotal += activitySummary.getSedentaryMinutes().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getDistance();
                        activityTotal += activitySummary.getDistance();
                    } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getCalories();
                        activityTotal += activitySummary.getCalories();
                    }
                    depVarTotal += refinedList.get(index).getAverageStress();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageStress();
                    index++;
                }

                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processStressWithWeather(List<DailyStress> dailyStresses, List<WeatherDailySummary> weatherDailySummaries, int duration) {
        String depVar = application.getString(R.string.stress_camel_case);
        String indepVarType = application.getString(R.string.weather_camel_case);
        List<WeatherDailySummary> summaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyStress> refinedList = new ArrayList<>();
        for (WeatherDailySummary weatherDailySummary : weatherDailySummaries) {
            String date = weatherDailySummary.getDate();
            for (DailyStress dailyStress : dailyStresses) {
                if (dailyStress.getDate().equals(date)) {
                    refinedList.add(dailyStress);
                    summaries.add(weatherDailySummary);
                    break;
                }
            }
        }
        activitiesSet.add(application.getString(R.string.humidity_camel_case));
        activitiesSet.add(application.getString(R.string.temperature_camel_case));
        activitiesSet.add(application.getString(R.string.precipitation_camel_case));

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                for (WeatherDailySummary weatherDailySummary : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(application.getString(R.string.humidity_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = weatherDailySummary.getAvgHumidity();
                        activityTotal += weatherDailySummary.getAvgHumidity();
                    } else if (activityName.equals(application.getString(R.string.temperature_camel_case))) {
                        double avgTempC = weatherDailySummary.getAvgTempC();
                        regressionDepVarXIndepVar[index][0] = avgTempC;
                        activityTotal += avgTempC;
                    } else if (activityName.equals(application.getString(R.string.precipitation_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = weatherDailySummary.getTotalPrecipmm();
                        activityTotal += weatherDailySummary.getTotalPrecipmm();
                    }
                    depVarTotal += refinedList.get(index).getAverageStress();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageStress();
                    index++;
                }

                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processStressWithTimedActivity(List<DailyStress> dailyStresses, List<TimedActivitySummary> timedActivities, int duration) {
        String depVar = application.getString(R.string.stress_camel_case);
        String indepVarType = application.getString(R.string.timer_camel_case);
        Map<String, List<TimedActivity>> activitiesMap = new LinkedHashMap<>();
        List<List<TimedActivity>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyStress> refinedList = new ArrayList<>();
        for (TimedActivitySummary timedActivitySummary : timedActivities) {
            String date = timedActivitySummary.getDate();
            for (DailyStress dailyStress : dailyStresses) {
                if (dailyStress.getDate().equals(date)) {
                    if (activitiesMap.get(date) != null) {
                        List<TimedActivity> oldList = activitiesMap.get(date);
                        Long d = timedActivitySummary.getDuration();
                        Boolean hasOld = false;
                        for (TimedActivity activity : oldList) {
                            if (activity.getInputName().equals(timedActivitySummary.getInputName())) {
                                activity.setDuration(activity.getDuration() + d);
                                hasOld = true;
                            }
                        }
                        if (hasOld) {
                        } else {
                            TimedActivity activity = new TimedActivity();
                            activity.setInputName(timedActivitySummary.getInputName());
                            activity.setDuration(timedActivitySummary.getDuration());
                            oldList.add(activity);
                        }
                        activitiesMap.put(date, oldList);
                    } else {
                        List<TimedActivity> list = new ArrayList<>();
                        TimedActivity activity = new TimedActivity();
                        activity.setInputName(timedActivitySummary.getInputName());
                        activity.setDuration(timedActivitySummary.getDuration());
                        list.add(activity);
                        activitiesMap.put(date, list);
                    }
                    refinedList.add(dailyStress);
                    activitiesSet.add(timedActivitySummary.getInputName());
                    break;
                }
            }
        }

        for (Map.Entry<String, List<TimedActivity>> entry : activitiesMap.entrySet()) {
            summariesListList.add(entry.getValue());
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                for (List<TimedActivity> summaries : summariesListList) {
                    for (TimedActivity timedActivity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(timedActivity.getInputName())) {
                            regressionDepVarXIndepVar[index][0] = timedActivity.getDuration();
                            activityTotal += timedActivity.getDuration();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageStress();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageStress();
                    index++;
                }

                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processStressWithBAActivity(List<DailyStress> dailyStresses, List<BAActivityEntry> baActivityEntries, int duration) {
        String depVar = application.getString(R.string.stress_camel_case);
        String indepVarType = application.getString(R.string.baactivity_camel_case);
        Map<String, List<String>> activitiesMap = new LinkedHashMap<>();
        List<List<String>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyStress> refinedList = new ArrayList<>();
        for (BAActivityEntry activitySummary : baActivityEntries) {
            String date = formattedTime.convertLongToYYYYMMDD(activitySummary.getDateInLong());
            for (DailyStress dailyStress : dailyStresses) {
                if (dailyStress.getDate().equals(date)) {
                    if (activitiesMap.get(date) != null) {
                        List<String> oldList = activitiesMap.get(date);
                        oldList.add(activitySummary.getName());
                        activitiesMap.put(date, oldList);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(activitySummary.getName());
                        activitiesMap.put(date, list);
                    }
                    refinedList.add(dailyStress);
                    activitiesSet.add(activitySummary.getName());
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : activitiesMap.entrySet()) {
            String date = entry.getKey();
            summariesListList.add(entry.getValue());
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                for (List<String> summaries : summariesListList) {
                    for (String activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity)) {
                            regressionDepVarXIndepVar[index][0] = Collections.frequency(summaries, activityName);
                            activityTotal += Collections.frequency(summaries, activityName);
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageStress();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageStress();
                    index++;
                }

                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processDailyReviewWithFitbit(List<DailyDailyReview> dailyDailyReviews, List<FitbitDailySummary> fitbitDailySummaries, int duration) {
        String depVar = application.getString(R.string.daily_review_camel_case);
        String indepVarType = application.getString(R.string.fitbit_camel_case);
        List<FitbitDailySummary> summaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyDailyReview> refinedList = new ArrayList<>();
        for (FitbitDailySummary fitbitDailySummary : fitbitDailySummaries) {
            String date = fitbitDailySummary.getDate();
            for (DailyDailyReview dailyDailyReview : dailyDailyReviews) {
                if (dailyDailyReview.getDate().equals(date)) {
                    refinedList.add(dailyDailyReview);
                    summaries.add(fitbitDailySummary);
                    break;
                }
            }
        }

        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sleep_camel_case));
        activitiesSet.add(application.getString(R.string.activity_floors_camel_case));

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (FitbitDailySummary activitySummary : summaries) {
                    if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep().doubleValue();
                        activityTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                        Long activeMinutes = activitySummary.getActivitySummary().getSummary()
                                .getFairlyActiveMinutes() + activitySummary.getActivitySummary()
                                .getSummary().getVeryActiveMinutes();
                        regressionDepVarXIndepVar[index][0] = activeMinutes.doubleValue();
                        activityTotal += activeMinutes.doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getFloors().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getFloors().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    }
                    depVarTotal += refinedList.get(index).getAverageDailyReview();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageDailyReview();
                    index++;
                }
                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processDailyReviewWithGoogleFit(List<DailyDailyReview> dailyDailyReviews, List<GoogleFitSummary> googleFitSummaries, int duration) {
        String depVar = application.getString(R.string.daily_review_camel_case);
        String indepVarType = application.getString(R.string.googlefit_camel_case);
        List<List<GoogleFitSummary.Summary>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyDailyReview> refinedList = new ArrayList<>();
        for (GoogleFitSummary googleFitSummary : googleFitSummaries) {
            String date = googleFitSummary.getDate();
            for (DailyDailyReview dailyDailyReview : dailyDailyReviews) {
                if (dailyDailyReview.getDate().equals(date)) {
                    refinedList.add(dailyDailyReview);
                    List<GoogleFitSummary.Summary> summaries = googleFitSummary.getSummaries();
                    summariesListList.add(summaries);
                    for (GoogleFitSummary.Summary summary : summaries) {
                        activitiesSet.add(summary.getName());
                    }
                    break;
                }
            }
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (List<GoogleFitSummary.Summary> summaries : summariesListList) {
                    for (GoogleFitSummary.Summary activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity.getName())) {
                            regressionDepVarXIndepVar[index][0] = activity.getValue();
                            activityTotal += activity.getValue();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageDailyReview();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageDailyReview();
                    index++;
                }
                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processDailyReviewWithAppUsage(List<DailyDailyReview> dailyDailyReviews, List<AppUsageSummary> appUsageSummaries, int duration) {
        String depVar = application.getString(R.string.daily_review_camel_case);
        String indepVarType = application.getString(R.string.phone_usage_camel_case);
        List<List<AppUsage>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyDailyReview> refinedList = new ArrayList<>();
        for (AppUsageSummary appUsageSummary : appUsageSummaries) {
            String date = appUsageSummary.getDate();
            for (DailyDailyReview dailyDailyReview : dailyDailyReviews) {
                if (dailyDailyReview.getDate().equals(date)) {
                    refinedList.add(dailyDailyReview);
                    List<AppUsage> summaries = appUsageSummary.getActivities();
                    summariesListList.add(summaries);
                    for (AppUsage summary : summaries) {
                        activitiesSet.add(summary.getAppName());
                    }
                    break;
                }
            }
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (List<AppUsage> summaries : summariesListList) {
                    for (AppUsage activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity.getAppName())) {
                            regressionDepVarXIndepVar[index][0] = activity.getTotalTime();
                            activityTotal += activity.getTotalTime();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageDailyReview();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageDailyReview();
                    index++;
                }
                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processDailyReviewWithBuiltInFitness(List<DailyDailyReview> dailyDailyReviews, List<BuiltInActivitySummary> activitySummaries, int duration) {
        String depVar = application.getString(R.string.daily_review_camel_case);
        String indepVarType = application.getString(R.string.moabi_tracker_camel_case);
        List<BuiltInActivitySummary> summaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyDailyReview> refinedList = new ArrayList<>();
        for (BuiltInActivitySummary activitySummary : activitySummaries) {
            String date = activitySummary.getDate();
            for (DailyDailyReview dailyDailyReview : dailyDailyReviews) {
                if (dailyDailyReview.getDate().equals(date)) {
                    refinedList.add(dailyDailyReview);
                    summaries.add(activitySummary);
                    break;
                }
            }
        }

        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (BuiltInActivitySummary activitySummary : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getSteps().doubleValue();
                        activityTotal += activitySummary.getSteps().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                        Long activeMinutes = activitySummary.getActiveMinutes();
                        regressionDepVarXIndepVar[index][0] = activeMinutes.doubleValue();
                        activityTotal += activeMinutes.doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getSedentaryMinutes().doubleValue();
                        activityTotal += activitySummary.getSedentaryMinutes().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getDistance();
                        activityTotal += activitySummary.getDistance();
                    } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getCalories();
                        activityTotal += activitySummary.getCalories();
                    }
                    depVarTotal += refinedList.get(index).getAverageDailyReview();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageDailyReview();
                    index++;
                }

                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processDailyReviewWithWeather(List<DailyDailyReview> dailyDailyReviews, List<WeatherDailySummary> weatherDailySummaries, int duration) {
        String depVar = application.getString(R.string.daily_review_camel_case);
        String indepVarType = application.getString(R.string.weather_camel_case);
        List<WeatherDailySummary> summaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyDailyReview> refinedList = new ArrayList<>();
        for (WeatherDailySummary weatherDailySummary : weatherDailySummaries) {
            String date = weatherDailySummary.getDate();
            for (DailyDailyReview dailyDailyReview : dailyDailyReviews) {
                if (dailyDailyReview.getDate().equals(date)) {
                    refinedList.add(dailyDailyReview);
                    summaries.add(weatherDailySummary);
                    break;
                }
            }
        }

        activitiesSet.add(application.getString(R.string.humidity_camel_case));
        activitiesSet.add(application.getString(R.string.temperature_camel_case));
        activitiesSet.add(application.getString(R.string.precipitation_camel_case));

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                for (WeatherDailySummary weatherDailySummary : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(application.getString(R.string.humidity_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = weatherDailySummary.getAvgHumidity();
                        activityTotal += weatherDailySummary.getAvgHumidity();
                    } else if (activityName.equals(application.getString(R.string.temperature_camel_case))) {
                        double avgTempC = weatherDailySummary.getAvgTempC();
                        regressionDepVarXIndepVar[index][0] = avgTempC;
                        activityTotal += avgTempC;
                    } else if (activityName.equals(application.getString(R.string.precipitation_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = weatherDailySummary.getTotalPrecipmm();
                        activityTotal += weatherDailySummary.getTotalPrecipmm();
                    }
                    depVarTotal += refinedList.get(index).getAverageDailyReview();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageDailyReview();
                    index++;
                }

                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processDailyReviewWithTimedActivity(List<DailyDailyReview> dailyDailyReviews, List<TimedActivitySummary> timedActivities, int duration) {
        String depVar = application.getString(R.string.daily_review_camel_case);
        String indepVarType = application.getString(R.string.timer_camel_case);
        Map<String, List<TimedActivity>> activitiesMap = new LinkedHashMap<>();
        List<List<TimedActivity>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyDailyReview> refinedList = new ArrayList<>();
        for (TimedActivitySummary timedActivitySummary : timedActivities) {
            String date = timedActivitySummary.getDate();
            for (DailyDailyReview dailyDailyReview : dailyDailyReviews) {
                if (dailyDailyReview.getDate().equals(date)) {
                    if (activitiesMap.get(date) != null) {
                        List<TimedActivity> oldList = activitiesMap.get(date);
                        Long d = timedActivitySummary.getDuration();
                        Boolean hasOld = false;
                        for (TimedActivity activity : oldList) {
                            if (activity.getInputName().equals(timedActivitySummary.getInputName())) {
                                activity.setDuration(activity.getDuration() + d);
                                hasOld = true;
                            }
                        }
                        if (hasOld) {
                        } else {
                            TimedActivity activity = new TimedActivity();
                            activity.setInputName(timedActivitySummary.getInputName());
                            activity.setDuration(timedActivitySummary.getDuration());
                            oldList.add(activity);
                        }
                        activitiesMap.put(date, oldList);
                    } else {
                        List<TimedActivity> list = new ArrayList<>();
                        TimedActivity activity = new TimedActivity();
                        activity.setInputName(timedActivitySummary.getInputName());
                        activity.setDuration(timedActivitySummary.getDuration());
                        list.add(activity);
                        activitiesMap.put(date, list);
                    }
                    refinedList.add(dailyDailyReview);
                    activitiesSet.add(timedActivitySummary.getInputName());
                    break;
                }
            }
        }

        for (Map.Entry<String, List<TimedActivity>> entry : activitiesMap.entrySet()) {
            summariesListList.add(entry.getValue());
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                for (List<TimedActivity> summaries : summariesListList) {
                    for (TimedActivity timedActivity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(timedActivity.getInputName())) {
                            regressionDepVarXIndepVar[index][0] = timedActivity.getDuration();
                            activityTotal += timedActivity.getDuration();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageDailyReview();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageDailyReview();
                    index++;
                }

                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processDailyReviewWithBAActivity(List<DailyDailyReview> dailyDailyReviews, List<BAActivityEntry> baActivityEntries, int duration) {
        String depVar = application.getString(R.string.daily_review_camel_case);
        String indepVarType = application.getString(R.string.baactivity_camel_case);
        Map<String, List<String>> activitiesMap = new LinkedHashMap<>();
        List<List<String>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyDailyReview> refinedList = new ArrayList<>();
        for (BAActivityEntry activitySummary : baActivityEntries) {
            String date = formattedTime.convertLongToYYYYMMDD(activitySummary.getDateInLong());
            for (DailyDailyReview dailyDailyReview : dailyDailyReviews) {
                if (dailyDailyReview.getDate().equals(date)) {
                    if (activitiesMap.get(date) != null) {
                        List<String> oldList = activitiesMap.get(date);
                        oldList.add(activitySummary.getName());
                        activitiesMap.put(date, oldList);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(activitySummary.getName());
                        activitiesMap.put(date, list);
                    }
                    refinedList.add(dailyDailyReview);
                    activitiesSet.add(activitySummary.getName());
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : activitiesMap.entrySet()) {
            String date = entry.getKey();
            summariesListList.add(entry.getValue());
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                for (List<String> summaries : summariesListList) {
                    for (String activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity)) {
                            regressionDepVarXIndepVar[index][0] = Collections.frequency(summaries, activityName);
                            activityTotal += Collections.frequency(summaries, activityName);
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageDailyReview();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageDailyReview();
                    index++;
                }

                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processPhq9WithFitbit(List<DailyPhq9> dailyPhq9es, List<FitbitDailySummary> fitbitDailySummaries, int duration) {
        String depVar = application.getString(R.string.depression_phq9_camel_case);
        String indepVarType = application.getString(R.string.fitbit_camel_case);
        List<FitbitDailySummary> summaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyPhq9> refinedList = new ArrayList<>();
        for (FitbitDailySummary fitbitDailySummary : fitbitDailySummaries) {
            String date = fitbitDailySummary.getDate();
            for (DailyPhq9 dailyPhq9 : dailyPhq9es) {
                if (dailyPhq9.getDate().equals(date)) {
                    refinedList.add(dailyPhq9);
                    summaries.add(fitbitDailySummary);
                    break;
                }
            }
        }

        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sleep_camel_case));
        activitiesSet.add(application.getString(R.string.activity_floors_camel_case));

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (FitbitDailySummary activitySummary : summaries) {
                    if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep().doubleValue();
                        activityTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                        Long activeMinutes = activitySummary.getActivitySummary().getSummary()
                                .getFairlyActiveMinutes() + activitySummary.getActivitySummary()
                                .getSummary().getVeryActiveMinutes();
                        regressionDepVarXIndepVar[index][0] = activeMinutes.doubleValue();
                        activityTotal += activeMinutes.doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getFloors().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getFloors().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }
                Log.i(TAG, activityName + " X Phq:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for phq goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "PhqAndEnergy average: " + phqAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for phq x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processPhq9WithGoogleFit(List<DailyPhq9> dailyPhq9es, List<GoogleFitSummary> googleFitSummaries, int duration) {
        String depVar = application.getString(R.string.depression_phq9_camel_case);
        String indepVarType = application.getString(R.string.googlefit_camel_case);
        List<List<GoogleFitSummary.Summary>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyPhq9> refinedList = new ArrayList<>();
        for (GoogleFitSummary googleFitSummary : googleFitSummaries) {
            String date = googleFitSummary.getDate();
            for (DailyPhq9 dailyPhq9 : dailyPhq9es) {
                if (dailyPhq9.getDate().equals(date)) {
                    refinedList.add(dailyPhq9);
                    List<GoogleFitSummary.Summary> summaries = googleFitSummary.getSummaries();
                    summariesListList.add(summaries);
                    for (GoogleFitSummary.Summary summary : summaries) {
                        activitiesSet.add(summary.getName());
                    }
                    break;
                }
            }
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (List<GoogleFitSummary.Summary> summaries : summariesListList) {
                    for (GoogleFitSummary.Summary activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity.getName())) {
                            regressionDepVarXIndepVar[index][0] = activity.getValue();
                            activityTotal += activity.getValue();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }
                Log.i(TAG, activityName + " X Phq:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for phq goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "PhqAndEnergy average: " + phqAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for phq x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processPhq9WithBuiltInFitness(List<DailyPhq9> dailyPhq9es, List<BuiltInActivitySummary> activitySummaries, int duration) {
        String depVar = application.getString(R.string.depression_phq9_camel_case);
        String indepVarType = application.getString(R.string.moabi_tracker_camel_case);
        List<BuiltInActivitySummary> summaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyPhq9> refinedList = new ArrayList<>();
        for (BuiltInActivitySummary activitySummary : activitySummaries) {
            String date = activitySummary.getDate();
            for (DailyPhq9 dailyPhq9 : dailyPhq9es) {
                if (dailyPhq9.getDate().equals(date)) {
                    refinedList.add(dailyPhq9);
                    summaries.add(activitySummary);
                    break;
                }
            }
        }

        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (BuiltInActivitySummary activitySummary : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getSteps().doubleValue();
                        activityTotal += activitySummary.getSteps().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                        Long activeMinutes = activitySummary.getActiveMinutes();
                        regressionDepVarXIndepVar[index][0] = activeMinutes.doubleValue();
                        activityTotal += activeMinutes.doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getSedentaryMinutes().doubleValue();
                        activityTotal += activitySummary.getSedentaryMinutes().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getDistance();
                        activityTotal += activitySummary.getDistance();
                    } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getCalories();
                        activityTotal += activitySummary.getCalories();
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }

                Log.i(TAG, activityName + " X Phq:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for phq goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "PhqAndEnergy average: " + phqAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for phq x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processPhq9WithWeather(List<DailyPhq9> dailyPhq9es, List<WeatherDailySummary> weatherDailySummaries, int duration) {
        String depVar = application.getString(R.string.depression_phq9_camel_case);
        String indepVarType = application.getString(R.string.weather_camel_case);
        List<WeatherDailySummary> summaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyPhq9> refinedList = new ArrayList<>();
        for (WeatherDailySummary weatherDailySummary : weatherDailySummaries) {
            String date = weatherDailySummary.getDate();
            for (DailyPhq9 dailyPhq9 : dailyPhq9es) {
                if (dailyPhq9.getDate().equals(date)) {
                    refinedList.add(dailyPhq9);
                    summaries.add(weatherDailySummary);
                    break;
                }
            }
        }

        activitiesSet.add(application.getString(R.string.humidity_camel_case));
        activitiesSet.add(application.getString(R.string.temperature_camel_case));
        activitiesSet.add(application.getString(R.string.precipitation_camel_case));

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                for (WeatherDailySummary weatherDailySummary : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(application.getString(R.string.humidity_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = weatherDailySummary.getAvgHumidity();
                        activityTotal += weatherDailySummary.getAvgHumidity();
                    } else if (activityName.equals(application.getString(R.string.temperature_camel_case))) {
                        double avgTempC = weatherDailySummary.getAvgTempC();
                        regressionDepVarXIndepVar[index][0] = avgTempC;
                        activityTotal += avgTempC;
                    } else if (activityName.equals(application.getString(R.string.precipitation_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = weatherDailySummary.getTotalPrecipmm();
                        activityTotal += weatherDailySummary.getTotalPrecipmm();
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }

                Log.i(TAG, activityName + " X Phq:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for phq goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "PhqAndEnergy average: " + phqAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for phq x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processPhq9WithTimedActivity(List<DailyPhq9> dailyPhq9es, List<TimedActivitySummary> timedActivities, int duration) {
        String depVar = application.getString(R.string.depression_phq9_camel_case);
        String indepVarType = application.getString(R.string.timer_camel_case);
        Map<String, List<TimedActivity>> activitiesMap = new LinkedHashMap<>();
        List<List<TimedActivity>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyPhq9> refinedList = new ArrayList<>();
        for (TimedActivitySummary timedActivitySummary : timedActivities) {
            String date = timedActivitySummary.getDate();
            for (DailyPhq9 dailyPhq9 : dailyPhq9es) {
                if (dailyPhq9.getDate().equals(date)) {
                    if (activitiesMap.get(date) != null) {
                        List<TimedActivity> oldList = activitiesMap.get(date);
                        Long d = timedActivitySummary.getDuration();
                        Boolean hasOld = false;
                        for (TimedActivity activity : oldList) {
                            if (activity.getInputName().equals(timedActivitySummary.getInputName())) {
                                activity.setDuration(activity.getDuration() + d);
                                hasOld = true;
                            }
                        }
                        if (hasOld) {
                        } else {
                            TimedActivity activity = new TimedActivity();
                            activity.setInputName(timedActivitySummary.getInputName());
                            activity.setDuration(timedActivitySummary.getDuration());
                            oldList.add(activity);
                        }
                        activitiesMap.put(date, oldList);
                    } else {
                        List<TimedActivity> list = new ArrayList<>();
                        TimedActivity activity = new TimedActivity();
                        activity.setInputName(timedActivitySummary.getInputName());
                        activity.setDuration(timedActivitySummary.getDuration());
                        list.add(activity);
                        activitiesMap.put(date, list);
                    }
                    refinedList.add(dailyPhq9);
                    activitiesSet.add(timedActivitySummary.getInputName());
                    break;
                }
            }
        }

        for (Map.Entry<String, List<TimedActivity>> entry : activitiesMap.entrySet()) {
            summariesListList.add(entry.getValue());
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                for (List<TimedActivity> summaries : summariesListList) {
                    for (TimedActivity timedActivity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(timedActivity.getInputName())) {
                            regressionDepVarXIndepVar[index][0] = timedActivity.getDuration();
                            activityTotal += timedActivity.getDuration();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }

                Log.i(TAG, activityName + " X Phq9:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for phq9 goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "Phq9AndEnergy average: " + phq9Average);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for phq9 x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary phq9Result = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, phq9Result);
                    phq9Result.setDepVarType(0L);
                    phq9Result.setDepXIndepVars(depVar + "X" + indepVarType + activityName + duration);
                    phq9Result.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    phq9Result.setDepVarTypeString(depVar);
                    phq9Result.setDuration(duration);
                    phq9Result.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    phq9Result.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(phq9Result);
                }
                regression.clear();
            }
        }
    }

    public void processPhq9WithBAActivity(List<DailyPhq9> dailyPhq9es, List<BAActivityEntry> baActivityEntries, int duration) {
        String depVar = application.getString(R.string.depression_phq9_camel_case);
        String indepVarType = application.getString(R.string.baactivity_camel_case);
        Map<String, List<String>> activitiesMap = new LinkedHashMap<>();
        List<List<String>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyPhq9> refinedList = new ArrayList<>();
        for (BAActivityEntry activitySummary : baActivityEntries) {
            String date = formattedTime.convertLongToYYYYMMDD(activitySummary.getDateInLong());
            for (DailyPhq9 dailyPhq9 : dailyPhq9es) {
                if (dailyPhq9.getDate().equals(date)) {
                    if (activitiesMap.get(date) != null) {
                        List<String> oldList = activitiesMap.get(date);
                        oldList.add(activitySummary.getName());
                        activitiesMap.put(date, oldList);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(activitySummary.getName());
                        activitiesMap.put(date, list);
                    }
                    refinedList.add(dailyPhq9);
                    activitiesSet.add(activitySummary.getName());
                }
            }
        }

        for (Map.Entry<String, List<String>> entry: activitiesMap.entrySet()) {
            String date = entry.getKey();
            summariesListList.add(entry.getValue());
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                for (List<String> summaries : summariesListList) {
                    for (String activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity)) {
                            regressionDepVarXIndepVar[index][0] = Collections.frequency(summaries, activityName);
                            activityTotal += Collections.frequency(summaries, activityName);
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }

                Log.i(TAG, activityName + " X Phq:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for phq goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "PhqAndEnergy average: " + phqAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for phq x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processPhq9WithAppUsage(List<DailyPhq9> dailyPhq9es, List<AppUsageSummary> appUsageSummaries, int duration) {
        String depVar = application.getString(R.string.depression_phq9_camel_case);
        String indepVarType = application.getString(R.string.phone_usage_camel_case);
        List<List<AppUsage>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyPhq9> refinedList = new ArrayList<>();
        for (AppUsageSummary appUsageSummary : appUsageSummaries) {
            String date = appUsageSummary.getDate();
            for (DailyPhq9 dailyPhq9 : dailyPhq9es) {
                if (dailyPhq9.getDate().equals(date)) {
                    refinedList.add(dailyPhq9);
                    List<AppUsage> summaries = appUsageSummary.getActivities();
                    summariesListList.add(summaries);
                    for (AppUsage summary : summaries) {
                        activitiesSet.add(summary.getAppName());
                    }
                    break;
                }
            }
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (List<AppUsage> summaries : summariesListList) {
                    for (AppUsage activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity.getAppName())) {
                            regressionDepVarXIndepVar[index][0] = activity.getTotalTime();
                            activityTotal += activity.getTotalTime();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }
                Log.i(TAG, activityName + " X Mood:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for mood goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, application.getString(R.string.mood_and_energy_camel_case) average: " + moodAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for mood x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processGad7WithFitbit(List<DailyGad7> dailyGad7es, List<FitbitDailySummary> fitbitDailySummaries, int duration) {
        String depVar = application.getString(R.string.anxiety_gad7_camel_case);
        String indepVarType = application.getString(R.string.fitbit_camel_case);
        List<FitbitDailySummary> summaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyGad7> refinedList = new ArrayList<>();
        for (FitbitDailySummary fitbitDailySummary : fitbitDailySummaries) {
            String date = fitbitDailySummary.getDate();
            for (DailyGad7 dailyGad7 : dailyGad7es) {
                if (dailyGad7.getDate().equals(date)) {
                    refinedList.add(dailyGad7);
                    summaries.add(fitbitDailySummary);
                    break;
                }
            }
        }

        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sleep_camel_case));
        activitiesSet.add(application.getString(R.string.activity_floors_camel_case));

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (FitbitDailySummary activitySummary : summaries) {
                    if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().summary.getSteps().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sleep_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep().doubleValue();
                        activityTotal += activitySummary.getSleepSummary().getSummary().getTotalMinutesAsleep().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                        Long activeMinutes = activitySummary.getActivitySummary().getSummary()
                                .getFairlyActiveMinutes() + activitySummary.getActivitySummary()
                                .getSummary().getVeryActiveMinutes();
                        regressionDepVarXIndepVar[index][0] = activeMinutes.doubleValue();
                        activityTotal += activeMinutes.doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getSedentaryMinutes().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getSedentaryMinutes().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_floors_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getFloors().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getFloors().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getDistances().get(0).getDistance();
                    } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                        activityTotal += activitySummary.getActivitySummary().getSummary().getCaloriesOut().doubleValue();
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }
                Log.i(TAG, activityName + " X Phq:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for phq goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "PhqAndEnergy average: " + phqAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for phq x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processGad7WithGoogleFit(List<DailyGad7> dailyGad7es, List<GoogleFitSummary> googleFitSummaries, int duration) {
        String depVar = application.getString(R.string.anxiety_gad7_camel_case);
        String indepVarType = application.getString(R.string.googlefit_camel_case);
        List<List<GoogleFitSummary.Summary>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyGad7> refinedList = new ArrayList<>();
        for (GoogleFitSummary googleFitSummary : googleFitSummaries) {
            String date = googleFitSummary.getDate();
            for (DailyGad7 dailyGad7 : dailyGad7es) {
                if (dailyGad7.getDate().equals(date)) {
                    refinedList.add(dailyGad7);
                    List<GoogleFitSummary.Summary> summaries = googleFitSummary.getSummaries();
                    summariesListList.add(summaries);
                    for (GoogleFitSummary.Summary summary : summaries) {
                        activitiesSet.add(summary.getName());
                    }
                    break;
                }
            }
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (List<GoogleFitSummary.Summary> summaries : summariesListList) {
                    for (GoogleFitSummary.Summary activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity.getName())) {
                            regressionDepVarXIndepVar[index][0] = activity.getValue();
                            activityTotal += activity.getValue();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }
                Log.i(TAG, activityName + " X Phq:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for phq goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "PhqAndEnergy average: " + phqAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for phq x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processGad7WithBuiltInFitness(List<DailyGad7> dailyGad7es, List<BuiltInActivitySummary> activitySummaries, int duration) {
        String depVar = application.getString(R.string.anxiety_gad7_camel_case);
        String indepVarType = application.getString(R.string.moabi_tracker_camel_case);
        List<BuiltInActivitySummary> summaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyGad7> refinedList = new ArrayList<>();
        for (BuiltInActivitySummary activitySummary : activitySummaries) {
            String date = activitySummary.getDate();
            for (DailyGad7 dailyGad7 : dailyGad7es) {
                if (dailyGad7.getDate().equals(date)) {
                    refinedList.add(dailyGad7);
                    summaries.add(activitySummary);
                    break;
                }
            }
        }

        activitiesSet.add(application.getString(R.string.activity_steps_camel_case));
        activitiesSet.add(application.getString(R.string.activity_calories_camel_case));
        activitiesSet.add(application.getString(R.string.activity_distance_camel_case));
        activitiesSet.add(application.getString(R.string.activity_active_minutes_camel_case));
        activitiesSet.add(application.getString(R.string.activity_sedentary_minutes_camel_case));

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (BuiltInActivitySummary activitySummary : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(application.getString(R.string.activity_steps_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getSteps().doubleValue();
                        activityTotal += activitySummary.getSteps().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                        Long activeMinutes = activitySummary.getActiveMinutes();
                        regressionDepVarXIndepVar[index][0] = activeMinutes.doubleValue();
                        activityTotal += activeMinutes.doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_sedentary_minutes_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getSedentaryMinutes().doubleValue();
                        activityTotal += activitySummary.getSedentaryMinutes().doubleValue();
                    } else if (activityName.equals(application.getString(R.string.activity_distance_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getDistance();
                        activityTotal += activitySummary.getDistance();
                    } else if (activityName.equals(application.getString(R.string.activity_calories_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = activitySummary.getCalories();
                        activityTotal += activitySummary.getCalories();
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }

                Log.i(TAG, activityName + " X Phq:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for phq goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "PhqAndEnergy average: " + phqAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for phq x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processGad7WithWeather(List<DailyGad7> dailyGad7es, List<WeatherDailySummary> weatherDailySummaries, int duration) {
        String depVar = application.getString(R.string.anxiety_gad7_camel_case);
        String indepVarType = application.getString(R.string.weather_camel_case);
        List<WeatherDailySummary> summaries = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyGad7> refinedList = new ArrayList<>();
        for (WeatherDailySummary weatherDailySummary : weatherDailySummaries) {
            String date = weatherDailySummary.getDate();
            for (DailyGad7 dailyGad7 : dailyGad7es) {
                if (dailyGad7.getDate().equals(date)) {
                    refinedList.add(dailyGad7);
                    summaries.add(weatherDailySummary);
                    break;
                }
            }
        }

        activitiesSet.add(application.getString(R.string.humidity_camel_case));
        activitiesSet.add(application.getString(R.string.temperature_camel_case));
        activitiesSet.add(application.getString(R.string.precipitation_camel_case));

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                for (WeatherDailySummary weatherDailySummary : summaries) {
                    // if the app was used for the day, get the recorded time
                    if (activityName.equals(application.getString(R.string.humidity_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = weatherDailySummary.getAvgHumidity();
                        activityTotal += weatherDailySummary.getAvgHumidity();
                    } else if (activityName.equals(application.getString(R.string.temperature_camel_case))) {
                        double avgTempC = weatherDailySummary.getAvgTempC();
                        regressionDepVarXIndepVar[index][0] = avgTempC;
                        activityTotal += avgTempC;
                    } else if (activityName.equals(application.getString(R.string.precipitation_camel_case))) {
                        regressionDepVarXIndepVar[index][0] = weatherDailySummary.getTotalPrecipmm();
                        activityTotal += weatherDailySummary.getTotalPrecipmm();
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }

                Log.i(TAG, activityName + " X Phq:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for phq goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "PhqAndEnergy average: " + phqAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for phq x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processGad7WithTimedActivity(List<DailyGad7> dailyGad7es, List<TimedActivitySummary> timedActivities, int duration) {
        String depVar = application.getString(R.string.anxiety_gad7_camel_case);
        String indepVarType = application.getString(R.string.timer_camel_case);
        Map<String, List<TimedActivity>> activitiesMap = new LinkedHashMap<>();
        List<List<TimedActivity>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyGad7> refinedList = new ArrayList<>();
        for (TimedActivitySummary timedActivitySummary : timedActivities) {
            String date = timedActivitySummary.getDate();
            for (DailyGad7 dailyGad7 : dailyGad7es) {
                if (dailyGad7.getDate().equals(date)) {
                    if (activitiesMap.get(date) != null) {
                        List<TimedActivity> oldList = activitiesMap.get(date);
                        Long d = timedActivitySummary.getDuration();
                        Boolean hasOld = false;
                        for (TimedActivity activity : oldList) {
                            if (activity.getInputName().equals(timedActivitySummary.getInputName())) {
                                activity.setDuration(activity.getDuration() + d);
                                hasOld = true;
                            }
                        }
                        if (hasOld) {
                        } else {
                            TimedActivity activity = new TimedActivity();
                            activity.setInputName(timedActivitySummary.getInputName());
                            activity.setDuration(timedActivitySummary.getDuration());
                            oldList.add(activity);
                        }
                        activitiesMap.put(date, oldList);
                    } else {
                        List<TimedActivity> list = new ArrayList<>();
                        TimedActivity activity = new TimedActivity();
                        activity.setInputName(timedActivitySummary.getInputName());
                        activity.setDuration(timedActivitySummary.getDuration());
                        list.add(activity);
                        activitiesMap.put(date, list);
                    }
                    refinedList.add(dailyGad7);
                    activitiesSet.add(timedActivitySummary.getInputName());
                    break;
                }
            }
        }

        for (Map.Entry<String, List<TimedActivity>> entry : activitiesMap.entrySet()) {
            summariesListList.add(entry.getValue());
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                for (List<TimedActivity> summaries : summariesListList) {
                    for (TimedActivity timedActivity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(timedActivity.getInputName())) {
                            regressionDepVarXIndepVar[index][0] = timedActivity.getDuration();
                            activityTotal += timedActivity.getDuration();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }

                Log.i(TAG, activityName + " X Gad7:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for phq9 goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "Gad7AndEnergy average: " + phq9Average);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for phq9 x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary phq9Result = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, phq9Result);
                    phq9Result.setDepVarType(0L);
                    phq9Result.setDepXIndepVars(depVar + "X" + indepVarType + activityName + duration);
                    phq9Result.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    phq9Result.setDepVarTypeString(depVar);
                    phq9Result.setDuration(duration);
                    phq9Result.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    phq9Result.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(phq9Result);
                }
                regression.clear();
            }
        }
    }

    public void processGad7WithBAActivity(List<DailyGad7> dailyGad7es, List<BAActivityEntry> baActivityEntries, int duration) {
        String depVar = application.getString(R.string.anxiety_gad7_camel_case);
        String indepVarType = application.getString(R.string.baactivity_camel_case);
        Map<String, List<String>> activitiesMap = new LinkedHashMap<>();
        List<List<String>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyGad7> refinedList = new ArrayList<>();
        for (BAActivityEntry activitySummary : baActivityEntries) {
            String date = formattedTime.convertLongToYYYYMMDD(activitySummary.getDateInLong());
            for (DailyGad7 dailyGad7 : dailyGad7es) {
                if (dailyGad7.getDate().equals(date)) {
                    if (activitiesMap.get(date) != null) {
                        List<String> oldList = activitiesMap.get(date);
                        oldList.add(activitySummary.getName());
                        activitiesMap.put(date, oldList);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(activitySummary.getName());
                        activitiesMap.put(date, list);
                    }
                    refinedList.add(dailyGad7);
                    activitiesSet.add(activitySummary.getName());
                }
            }
        }

        for (Map.Entry<String, List<String>> entry: activitiesMap.entrySet()) {
            String date = entry.getKey();
            summariesListList.add(entry.getValue());
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                for (List<String> summaries : summariesListList) {
                    for (String activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity)) {
                            regressionDepVarXIndepVar[index][0] = Collections.frequency(summaries, activityName);
                            activityTotal += Collections.frequency(summaries, activityName);
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }

                Log.i(TAG, activityName + " X Phq:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for phq goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "PhqAndEnergy average: " + phqAverage);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for phq x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary regressionResult = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, regressionResult);
                    regressionResult.setDepVarType(0L);
                    regressionResult.setDepXIndepVars(activityName + indepVarType + "X" + depVar + duration);
                    regressionResult.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionResult.setDepVarTypeString(depVar);
                    regressionResult.setDuration(duration);
                    regressionResult.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionResult.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(regressionResult);
                }
                regression.clear();
            }
        }
    }

    public void processGad7WithAppUsage(List<DailyGad7> dailyGad7es, List<AppUsageSummary> appUsageSummaries, int duration) {
        String depVar = application.getString(R.string.anxiety_gad7_camel_case);
        String indepVarType = application.getString(R.string.phone_usage_camel_case);
        List<List<AppUsage>> summariesListList = new ArrayList<>();
        Set<String> activitiesSet = new LinkedHashSet<>();
        List<DailyGad7> refinedList = new ArrayList<>();
        for (AppUsageSummary appUsageSummary : appUsageSummaries) {
            String date = appUsageSummary.getDate();
            for (DailyGad7 dailyGad7 : dailyGad7es) {
                if (dailyGad7.getDate().equals(date)) {
                    refinedList.add(dailyGad7);
                    List<AppUsage> summaries = appUsageSummary.getActivities();
                    summariesListList.add(summaries);
                    for (AppUsage summary : summaries) {
                        activitiesSet.add(summary.getAppName());
                    }
                    break;
                }
            }
        }

        if (refinedList.size() > 2) {

            Map<String, Object> regressionMap = new HashMap<>();

            // iterate through each app
            for (String activityName : activitiesSet) {
                int index = 0;
                double[][] regressionDepVarXIndepVar = new double[refinedList.size()][2];
                double depVarTotal = 0;
                double activityTotal = 0;

                // iterate through each day
                for (List<AppUsage> summaries : summariesListList) {
                    for (AppUsage activity : summaries) {
                        // if the app was used for the day, get the recorded time
                        if (activityName.equals(activity.getAppName())) {
                            regressionDepVarXIndepVar[index][0] = activity.getTotalTime();
                            activityTotal += activity.getTotalTime();
                            break;
                        } else {
                            // if the app was not used for the day, set the time to 0.
                            regressionDepVarXIndepVar[index][0] = 0;
                            activityTotal += 0;
                        }
                    }
                    depVarTotal += refinedList.get(index).getAverageScore();
                    regressionDepVarXIndepVar[index][1] = refinedList.get(index).getAverageScore();
                    index++;
                }
                Log.i(TAG, activityName + " X Gad7:" + Arrays.deepToString(regressionDepVarXIndepVar));

                // linear  regression
                SimpleRegression regression = new SimpleRegression();

                double slope = 0;
                double yintercept = 0;
                double rsqured = 0;
                double correlation = 0;
                double recommendedActivityLevel = 0;
                double depVarAverage = depVarTotal / regressionDepVarXIndepVar.length;
                double activityAverage = activityTotal / regressionDepVarXIndepVar.length;
                double depVarGoal = depVarAverage * 1.01; // TODO - set up multiplier for gad7 goal
                //activityAverageMap.put(activityName, activityAverage);
                //Log.i(TAG, "Gad7AndEnergy average: " + gad7Average);
                //Log.i(TAG, "Energy Level average: " + energyAverage);

                // regression for gad7 x app
                regression.addData(regressionDepVarXIndepVar);
                slope = regression.getSlope();
                yintercept = regression.getIntercept();
                rsqured = regression.getRSquare();
                correlation = regression.getR();
                recommendedActivityLevel = (depVarGoal - yintercept) / slope;

                if (!Double.isNaN(regression.getSlope()) && regression.getSlope() != 0 && !(recommendedActivityLevel < 0)) {
                    SimpleRegressionSummary gad7Result = new SimpleRegressionSummary(
                            depVar,
                            activityName, indepVarType,
                            slope, yintercept, rsqured, correlation,
                            activityAverage, recommendedActivityLevel);
                    regressionMap.put(activityName, gad7Result);
                    gad7Result.setDepVarType(0L);
                    gad7Result.setDepXIndepVars(depVar + "X" + indepVarType + activityName + duration);
                    gad7Result.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    gad7Result.setDepVarTypeString(depVar);
                    gad7Result.setDuration(duration);
                    gad7Result.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    gad7Result.setNumOfData((long) regressionDepVarXIndepVar.length);
                    Log.i(TAG, activityName + ": " + "Slope is " + slope + ", Y-intercept is "
                            + yintercept + ", Recommended activity level is "
                            + recommendedActivityLevel
                            + ", R square is " + rsqured + ", Correlation is " + correlation + ", Duration is " + duration);
                    insert(gad7Result);
                }
                regression.clear();
            }
        }
    }

    public void processData(double indepVarForPrediction, long depVarType, String
            xName, String yName, double[] x, double[] y) {
        // linear
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Double> rsquaredvalues = new ArrayList<>();
                Map<Double, String> rsquaredMap = new TreeMap<>();
                TrendLine linear = new PolyTrendLine(1);
                linear.setValues(y, x);
                double linearrsquared = linear.getAdjRSquared();
                rsquaredMap.put(linearrsquared, "Linear");
                TrendLine quadratic = new PolyTrendLine(2);
                quadratic.setValues(y, x);
                double quaadraticsquared = quadratic.getAdjRSquared();
                rsquaredMap.put(quaadraticsquared, "Quadratic");
                TrendLine cubic = new PolyTrendLine(3);
                cubic.setValues(y, x);
                double cubicrsquared = quadratic.getAdjRSquared();
                rsquaredMap.put(cubicrsquared, "Cubic");
                TrendLine quartic = new PolyTrendLine(4);
                TrendLine power = new PowerTrendLine();
                power.setValues(y, x);
                double powerrsquared = power.getAdjRSquared();
                rsquaredMap.put(powerrsquared, "Power");
                TrendLine log = new LogTrendLine();
                log.setValues(y, x);
                double logrsquared = log.getAdjRSquared();
                rsquaredMap.put(logrsquared, "Log");
                TrendLine expo = new ExpTrendLine();
                expo.setValues(y, x);
                double exporsquared = expo.getAdjRSquared();
                rsquaredMap.put(exporsquared, "Exponential");

                int i = 0;
                String bestFit = "";
                for (Map.Entry<Double, String> rsquared : rsquaredMap.entrySet()) {
                    Log.i(TAG, rsquared.getValue() + ": " + rsquared.getKey());
                    if (i == rsquaredMap.size() - 1) {
                        bestFit = rsquared.getValue();
                        Log.i(TAG, "Best Fit: " + bestFit);
                    }
                    i++;
                }
                double adjRSquared = 0;
                double standardError = 0;
                double predicted = 0;
                RealMatrix coefs = null;
                if (bestFit.equals("Linear")) {
                    Log.i(TAG, "Predicted value: " + linear.predict(indepVarForPrediction));
                    adjRSquared = linear.getAdjRSquared();
                    standardError = linear.getStandardError();
                    predicted = linear.predict(indepVarForPrediction);
                    coefs = linear.getCoefficients();
                } else if (bestFit.equals("Quadratic")) {
                    Log.i(TAG, "Predicted value: " + quadratic.predict(indepVarForPrediction));
                    adjRSquared = quadratic.getAdjRSquared();
                    standardError = quadratic.getStandardError();
                    predicted = quadratic.predict(indepVarForPrediction);
                    coefs = quadratic.getCoefficients();
                } else if (bestFit.equals("Cubic")) {
                    Log.i(TAG, "Predicted value: " + cubic.predict(indepVarForPrediction));
                    adjRSquared = cubic.getAdjRSquared();
                    standardError = cubic.getStandardError();
                    predicted = cubic.predict(indepVarForPrediction);
                    coefs = cubic.getCoefficients();
                } else if (bestFit.equals("Power")) {
                    Log.i(TAG, "Predicted value: " + power.predict(indepVarForPrediction));
                    adjRSquared = power.getAdjRSquared();
                    standardError = power.getStandardError();
                    predicted = power.predict(indepVarForPrediction);
                    coefs = power.getCoefficients();
                } else if (bestFit.equals("Log")) {
                    Log.i(TAG, "Predicted value: " + log.predict(indepVarForPrediction));
                    adjRSquared = log.getAdjRSquared();
                    standardError = log.getStandardError();
                    predicted = log.predict(indepVarForPrediction);
                    coefs = log.getCoefficients();
                } else if (bestFit.equals("Exponential")) {
                    Log.i(TAG, "Predicted value: " + expo.predict(indepVarForPrediction));
                    adjRSquared = expo.getAdjRSquared();
                    standardError = expo.getStandardError();
                    predicted = expo.predict(indepVarForPrediction);
                    coefs = expo.getCoefficients();
                }
                if (coefs != null) {
                    Log.i(TAG, bestFit + ": " + yName + "X" + xName + ", adjR^2: " + adjRSquared + ", Std Error: " + standardError + ", " + "Predicted: " + predicted + ", Coefficients: " + coefs.toString());
                    RegressionSummary regressionSummary = new RegressionSummary();
                    regressionSummary.setDepXIndepVariables(yName + "X" + xName);
                    regressionSummary.setDepVar(yName);
                    regressionSummary.setIndepVar(xName);
                    regressionSummary.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    regressionSummary.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    regressionSummary.setAdjRSquared(adjRSquared);
                    regressionSummary.setStandardError(standardError);
                    regressionSummary.setType(depVarType);
                    regressionSummary.setPredictedDepVar(predicted);
                    insert(regressionSummary);
                }
            }
        }).start();
    }


    public boolean insert(RegressionSummary regressionSummary) {
        AsyncTask.Status status = new insertAsyncTask(regressionSummaryDao).execute(regressionSummary).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertAsyncTask extends AsyncTask<RegressionSummary, Void, Void> {

        private RegressionSummaryDao mAsyncTaskDao;

        insertAsyncTask(RegressionSummaryDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final RegressionSummary... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public boolean insert(SimpleRegressionSummary simpleRegressionSummary) {
        AsyncTask.Status status = new insertSimpleRegressionAsyncTask(regressionSummaryDao).execute(simpleRegressionSummary).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertSimpleRegressionAsyncTask extends AsyncTask<SimpleRegressionSummary, Void, Void> {

        private RegressionSummaryDao mAsyncTaskDao;

        insertSimpleRegressionAsyncTask(RegressionSummaryDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final SimpleRegressionSummary... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
