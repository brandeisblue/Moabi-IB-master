package com.ivorybridge.moabi.database.firebase;

import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by skim2 on 11/28/2017.
 */

public class FirebaseManager {

    private DatabaseReference databaseRef;
    private DatabaseReference appConnectedServicesRef;
    private DatabaseReference appFitbitSettingsRef;
    private DatabaseReference collectiveUserInputsRef;
    private DatabaseReference collectiveHashTagsRef;

    private DatabaseReference userTableRef;
    private DatabaseReference userRef;
    private DatabaseReference groupsRef;
    private DatabaseReference userGroupsRef;
    private DatabaseReference weatherDataRef;
    private DatabaseReference profileRef;
    private DatabaseReference apiCredentials;

    private DatabaseReference connectedServicesRef;
    private DatabaseReference fitbitRef;
    private DatabaseReference fitbitProfileRef;
    private DatabaseReference fitbitCredentialRef;
    private DatabaseReference fitbitTodayRef;
    private DatabaseReference fitbitYesterdayRef;
    private DatabaseReference fitbitTodaySummaryRef;
    private DatabaseReference fitbitGoalsRef;
    private DatabaseReference fitbitRecommendedGoalsRef;
    private DatabaseReference googleFitRef;
    private DatabaseReference googleFitTodayRef;
    private DatabaseReference googleFitYesterdayRef;
    private DatabaseReference googleFitGoalsRef;
    private DatabaseReference googleFitRecommendedGoalsRef;
    private DatabaseReference appUsageRef;
    private DatabaseReference thisDeviceRef;
    private DatabaseReference appUsageTodayRef;
    private DatabaseReference builtInFitnessTrackerRef;
    private DatabaseReference builtInFitnessTrackerThisDeviceRef;

    private DatabaseReference userInputsRef;
    private DatabaseReference moodAndEnergyRef;
    private DatabaseReference moodAndEnergyLevelTodayRef;
    private DatabaseReference personalHashTagRef;
    private DatabaseReference personalHashTagTodayRef;
    private DatabaseReference activityRef;
    private DatabaseReference activityTodayRef;
    private DatabaseReference stressRef;
    private DatabaseReference dailyReviewRef;
    private DatabaseReference statisticsRef;
    private DatabaseReference regressionRef;
    private DatabaseReference moodRegressionRef;
    private DatabaseReference energyLevelRegressionRef;
    private DatabaseReference phq9Ref;
    private DatabaseReference gad7Ref;
    private DatabaseReference stopWatchRef;

    private static final String TAG = FirebaseManager.class.getSimpleName();
    private static final String USERS_TABLE = "users";

    public FirebaseManager() {

        // initialize a reference to the root of the clood database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String todaysDate = setUpDatesForToday();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        // keep the database synced so that it can be used even when offline
        databaseRef.keepSynced(true);
        // set up nodes for non-user-specific data

        appConnectedServicesRef = databaseRef.child("connectedServices");
        appFitbitSettingsRef = appConnectedServicesRef.child("fitbit");
        collectiveUserInputsRef = databaseRef.child("userInputs");
        collectiveHashTagsRef = collectiveUserInputsRef.child("hashTags");

        // set up nodes for user-specific data
        userTableRef = databaseRef.child(USERS_TABLE);
        userRef = userTableRef.child(user.getUid());
        groupsRef = databaseRef.child("groups");
        userGroupsRef = userTableRef.child("groups");

        // profile
        profileRef = userRef.child("profile");
        // profileRef.child("userId").setValue(user.getUid());
        profileRef.child("displayName").setValue(user.getDisplayName());
        //profileRef.child("provider").setValue(user.getProviderId());

        // weather
        weatherDataRef = userRef.child("weather");

        // API Credentials
        apiCredentials = userRef.child("api");
        fitbitCredentialRef = apiCredentials.child("fitbit");

        // connected services (including tracking app usage)
        connectedServicesRef = userRef.child("connectedServices");
        appUsageRef = connectedServicesRef.child("phoneUsage");
        thisDeviceRef = appUsageRef.child(getDeviceName());
        appUsageTodayRef = thisDeviceRef.child(todaysDate);
        fitbitRef = connectedServicesRef.child("fitbit");
        fitbitProfileRef = fitbitRef.child("profile");
        fitbitTodayRef = fitbitRef.child(todaysDate);
        fitbitYesterdayRef = fitbitRef.child(setUpDateForYesterday());
        fitbitTodaySummaryRef = fitbitTodayRef.child("summary");
        fitbitGoalsRef = fitbitTodayRef.child("goals");
        fitbitRecommendedGoalsRef = fitbitTodayRef.child("recommendedGoals");
        googleFitRef = connectedServicesRef.child("googleFit");
        googleFitTodayRef = googleFitRef.child(todaysDate);
        googleFitYesterdayRef = googleFitRef.child(setUpDateForYesterday());
        googleFitGoalsRef = googleFitTodayRef.child("goals");
        googleFitRecommendedGoalsRef = googleFitTodayRef.child("recommendedGoals");
        builtInFitnessTrackerRef = connectedServicesRef.child("moabi");
        builtInFitnessTrackerThisDeviceRef = builtInFitnessTrackerRef.child(getDeviceName());

        // user inputs
        userInputsRef = userRef.child("userInputs");
        moodAndEnergyRef = userInputsRef.child("moodAndEnergy");
        moodAndEnergyLevelTodayRef = moodAndEnergyRef.child(todaysDate);
        activityRef = userInputsRef.child("baActivity");
        activityTodayRef = activityRef.child(setUpDatesForToday());
        personalHashTagRef = userInputsRef.child("hashTags");
        personalHashTagTodayRef = personalHashTagRef.child(setUpDatesForToday());
        stressRef = userInputsRef.child("stress");
        dailyReviewRef = userInputsRef.child("dailyReview");
        phq9Ref = userInputsRef.child("phq9");
        gad7Ref = userInputsRef.child("gad7");
        stopWatchRef = userInputsRef.child("stopWatch");
        
        // statistics
        statisticsRef = userRef.child("stats");
        regressionRef = statisticsRef.child("regression");
        moodRegressionRef = regressionRef.child("mood");
        energyLevelRegressionRef = regressionRef.child("energyLevel");
    }

    public DatabaseReference getDatabaseRef() {
        return databaseRef;
    }

    public DatabaseReference getAppConnectedServicesRef() {
        return appConnectedServicesRef;
    }

    public DatabaseReference getAppFitbitSettingsRef() {
        return appFitbitSettingsRef;
    }

    public DatabaseReference getCollectiveUserInputsRef() {
        return collectiveUserInputsRef;
    }

    public DatabaseReference getCollectiveHashTagsRef() {
        return collectiveHashTagsRef;
    }

    public DatabaseReference getUserTableRef() {
        return userTableRef;
    }

    public DatabaseReference getUserRef() {
        return userRef;
    }

    public DatabaseReference getGroupsRef() {
        return groupsRef;
    }

    public DatabaseReference getUserGroupsRef() {
        return userGroupsRef;
    }

    public DatabaseReference getWeatherDataRef() {
        return weatherDataRef;
    }

    public DatabaseReference getProfileRef() {
        return profileRef;
    }

    public DatabaseReference getApiCredentials() {
        return apiCredentials;
    }

    public DatabaseReference getConnectedServicesRef() {
        return connectedServicesRef;
    }

    public DatabaseReference getFitbitRef() {
        return fitbitRef;
    }

    public DatabaseReference getFitbitProfileRef() {
        return fitbitProfileRef;
    }

    public DatabaseReference getFitbitCredentialRef() {
        return fitbitCredentialRef;
    }

    public DatabaseReference getFitbitTodayRef() {
        return fitbitTodayRef;
    }

    public DatabaseReference getFitbitYesterdayRef() {
        return fitbitYesterdayRef;
    }

    public DatabaseReference getFitbitTodaySummaryRef() {
        return fitbitTodaySummaryRef;
    }

    public DatabaseReference getFitbitGoalsRef() {
        return fitbitGoalsRef;
    }

    public DatabaseReference getFitbitRecommendedGoalsRef() {
        return fitbitRecommendedGoalsRef;
    }

    public DatabaseReference getGoogleFitRef() {
        return googleFitRef;
    }

    public DatabaseReference getGoogleFitTodayRef() {
        return googleFitTodayRef;
    }

    public DatabaseReference getGoogleFitYesterdayRef() {
        return googleFitYesterdayRef;
    }

    public DatabaseReference getGoogleFitGoalsRef() {
        return googleFitGoalsRef;
    }

    public DatabaseReference getGoogleFitRecommendedGoalsRef() {
        return googleFitRecommendedGoalsRef;
    }

    public DatabaseReference getAppUsageRef() {
        return appUsageRef;
    }

    public DatabaseReference getThisDeviceRef() {
        return thisDeviceRef;
    }

    public DatabaseReference getAppUsageTodayRef() {
        return appUsageTodayRef;
    }

    public DatabaseReference getBuiltInFitnessTrackerRef() {
        return builtInFitnessTrackerRef;
    }

    public DatabaseReference getBuiltInFitnessTrackerThisDeviceRef() {
        return builtInFitnessTrackerThisDeviceRef;
    }

    public DatabaseReference getUserInputsRef() {
        return userInputsRef;
    }

    public DatabaseReference getMoodAndEnergyRef() {
        return moodAndEnergyRef;
    }

    public DatabaseReference getMoodAndEnergyLevelTodayRef() {
        return moodAndEnergyLevelTodayRef;
    }

    public DatabaseReference getPersonalHashTagRef() {
        return personalHashTagRef;
    }

    public DatabaseReference getPersonalHashTagTodayRef() {
        return personalHashTagTodayRef;
    }

    public DatabaseReference getActivityRef() {
        return activityRef;
    }

    public DatabaseReference getActivityTodayRef() {
        return activityTodayRef;
    }

    public DatabaseReference getStressRef() {
        return stressRef;
    }

    public DatabaseReference getDailyReviewRef() {
        return dailyReviewRef;
    }

    public DatabaseReference getStatisticsRef() {
        return statisticsRef;
    }

    public DatabaseReference getRegressionRef() {
        return regressionRef;
    }

    public DatabaseReference getMoodRegressionRef() {
        return moodRegressionRef;
    }

    public DatabaseReference getEnergyLevelRegressionRef() {
        return energyLevelRegressionRef;
    }

    public DatabaseReference getPhq9Ref() {
        return phq9Ref;
    }

    public DatabaseReference getGad7Ref() {
        return gad7Ref;
    }

    public DatabaseReference getStopWatchRef() {
        return stopWatchRef;
    }



    private String setUpDatesForToday() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return mdformat.format(calendar.getTime());
    }

    private String setUpDateForYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return mdformat.format(calendar.getTime());
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return (model.toUpperCase());
        }
        return (manufacturer.toUpperCase()) + " " + model;
    }
}
