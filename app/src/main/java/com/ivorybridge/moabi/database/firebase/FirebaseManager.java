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

    private DatabaseReference daysWithDataRef;
    private DatabaseReference daysWithDataTodayRef;
    private DatabaseReference fitbitDayWithDataRef;
    private DatabaseReference googleFitDayWithDataRef;
    private DatabaseReference moodAndEnergyDayWithDataRef;
    private DatabaseReference appUsageDayWithDataRef;
    private DatabaseReference activityDayWithDataRef;
    private DatabaseReference stressDayWithDataRef;
    private DatabaseReference dailyReviewDayWithDataRef;
    private DatabaseReference phq9DayWithDataRef;
    private DatabaseReference gad7DayWithDataRef;

    private DatabaseReference inputsInUseRef;
    private DatabaseReference fitbitIsInUseRef;
    private DatabaseReference googleFitIsInUseRef;
    private DatabaseReference moodAndEnergyIsInUseRef;
    private DatabaseReference behavioralActivationActivityIsInUseRef;
    private DatabaseReference appUsageIsInUseRef;

    private DatabaseReference isConnectedRef;
    private DatabaseReference fitbitIsConnectedRef;
    private DatabaseReference googleFitIsConnectedRef;
    private DatabaseReference appUsageIsConnectedRef;

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

    private DatabaseReference last30DaysRef;
    private DatabaseReference fitbitLast30DaysRef;
    private DatabaseReference fitbitLast30DaysTodayRef;
    private DatabaseReference fitbitLast30DaysYesterdayRef;
    private DatabaseReference fitbitLast30DaysAverageRef;
    private DatabaseReference googleFitLast30DaysRef;
    private DatabaseReference googleFitLast30DaysTodayRef;
    private DatabaseReference googleFitLast30DaysYesterdayRef;
    private DatabaseReference googleFitLast30DaysAverageRef;
    private DatabaseReference appUsageLast30DaysRef;
    private DatabaseReference appUsageThisDeviceLast30DaysRef;
    private DatabaseReference appUsageLast30DaysTodayRef;
    private DatabaseReference appUsageLast30DaysAverageRef;
    private DatabaseReference userInputLast30DaysRef;
    private DatabaseReference personalHashTagLast30DaysRef;
    private DatabaseReference personalHashTagLast30DaysTodayRef;
    private DatabaseReference activityLast30DaysRef;
    private DatabaseReference activityLast30DaysTodayRef;
    private DatabaseReference moodAndEnergyLast30DaysRef;
    private DatabaseReference moodAndEnergyLast30DaysTodayRef;
    private DatabaseReference stressLast30DaysRef;
    private DatabaseReference dailyReviewLast30DaysRef;
    private DatabaseReference statisticsLast30DaysRef;
    private DatabaseReference phq9Last30DaysRef;
    private DatabaseReference gad7Last30DaysRef;
    private DatabaseReference statisticsLast30DaysTodayRef;
    private DatabaseReference meansLast30DaysRef;
    private DatabaseReference regressionLast30DaysRef;
    private DatabaseReference moodRegressionLast30DaysRef;
    private DatabaseReference moodRegressionLast30DaysTodayRef;
    private DatabaseReference energyLevelRegressionLast30DaysRef;
    private DatabaseReference energyLevelRegressionLast30DaysTodayRef;

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

        // days that have data
        daysWithDataRef = userRef.child("daysWithData");
        daysWithDataTodayRef = daysWithDataRef.child(todaysDate);
        fitbitDayWithDataRef = daysWithDataTodayRef.child("fitbit");
        googleFitDayWithDataRef = daysWithDataTodayRef.child("googleFit");
        moodAndEnergyDayWithDataRef = daysWithDataTodayRef.child("moodAndEnergy");
        appUsageDayWithDataRef = daysWithDataTodayRef.child("phoneUsage");
        activityDayWithDataRef = daysWithDataTodayRef.child("");
        stressDayWithDataRef = daysWithDataRef.child("2stress");
        dailyReviewDayWithDataRef = daysWithDataRef.child("dailyReview");
        phq9DayWithDataRef = daysWithDataRef.child("phq9");
        gad7DayWithDataRef = daysWithDataRef.child("gad7");

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

        // inputs in use
        inputsInUseRef = userRef.child("inputsInUse");
        inputsInUseRef.keepSynced(true);
        fitbitIsInUseRef = inputsInUseRef.child("fitbit");
        googleFitIsInUseRef = inputsInUseRef.child("fitbit");
        appUsageIsInUseRef = inputsInUseRef.child("phoneUsage");
        moodAndEnergyIsInUseRef = inputsInUseRef.child("moodAndEnergy");
        behavioralActivationActivityIsInUseRef = inputsInUseRef.child("6behavioralActivationActivity");

        // connected services
        isConnectedRef = userRef.child("isConnected");
        isConnectedRef.keepSynced(true);
        fitbitIsConnectedRef = isConnectedRef.child("fitbit");
        googleFitIsConnectedRef = isConnectedRef.child("googleFit");
        appUsageIsConnectedRef = isConnectedRef.child("phoneUsage");

        // user inputs
        userInputsRef = userRef.child("userInputs");
        moodAndEnergyRef = userInputsRef.child("moodAndEnergy");
        moodAndEnergyLevelTodayRef = moodAndEnergyRef.child(todaysDate);
        activityRef = userInputsRef.child("6behavioralActivationActivity");
        activityTodayRef = activityRef.child(setUpDatesForToday());
        personalHashTagRef = userInputsRef.child("hashTags");
        personalHashTagTodayRef = personalHashTagRef.child(setUpDatesForToday());
        stressRef = userInputsRef.child("2stress");
        dailyReviewRef = userInputsRef.child("dailyReview");
        phq9Ref = userInputsRef.child("phq9");
        gad7Ref = userInputsRef.child("gad7");
        
        // statistics
        statisticsRef = userRef.child("stats");
        regressionRef = statisticsRef.child("regression");
        moodRegressionRef = regressionRef.child("mood");
        energyLevelRegressionRef = regressionRef.child("energyLevel");
        
        // last 30 days
        last30DaysRef = userRef.child("lastThirtyDays");
        statisticsLast30DaysRef = last30DaysRef.child("statistics");
        meansLast30DaysRef = statisticsLast30DaysRef.child("mean");
        regressionLast30DaysRef = statisticsLast30DaysRef.child("regression");
        moodRegressionLast30DaysRef = regressionLast30DaysRef.child("mood");
        energyLevelRegressionLast30DaysRef = regressionLast30DaysRef.child("energyLevel");
        userInputLast30DaysRef = last30DaysRef.child("userInputs");
        personalHashTagLast30DaysRef = userInputLast30DaysRef.child("hashTags");
        personalHashTagLast30DaysTodayRef = personalHashTagLast30DaysRef.child(setUpDatesForToday());
        activityLast30DaysRef = userInputLast30DaysRef.child("6behavioralActivationActivity");
        activityLast30DaysTodayRef = activityLast30DaysRef.child(setUpDatesForToday());
        fitbitLast30DaysRef = last30DaysRef.child("fitbit");
        fitbitLast30DaysTodayRef = fitbitLast30DaysRef.child(todaysDate);
        fitbitLast30DaysYesterdayRef = fitbitLast30DaysRef.child(setUpDateForYesterday());
        fitbitLast30DaysAverageRef = fitbitLast30DaysRef.child("means");
        googleFitLast30DaysRef = last30DaysRef.child("googleFit");
        googleFitLast30DaysTodayRef = googleFitLast30DaysRef.child(todaysDate);
        googleFitLast30DaysYesterdayRef = googleFitLast30DaysRef.child(setUpDateForYesterday());
        googleFitLast30DaysAverageRef = googleFitLast30DaysRef.child("means");
        appUsageLast30DaysRef = last30DaysRef.child("phoneUsage");
        appUsageThisDeviceLast30DaysRef = appUsageLast30DaysRef.child(getDeviceName());
        appUsageLast30DaysAverageRef = appUsageThisDeviceLast30DaysRef.child("means");
        appUsageLast30DaysTodayRef = appUsageThisDeviceLast30DaysRef.child(todaysDate);
        moodAndEnergyLast30DaysRef = userInputLast30DaysRef.child("moodAndEnergy");
        moodAndEnergyLast30DaysTodayRef = moodAndEnergyLast30DaysRef.child(todaysDate);
        stressLast30DaysRef = userInputLast30DaysRef.child("2stress");
        dailyReviewLast30DaysRef = userInputLast30DaysRef.child("dailyReview");
        phq9Last30DaysRef = userInputLast30DaysRef.child("phq9");
        gad7Last30DaysRef = userInputLast30DaysRef.child("gad7");
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

    public DatabaseReference getDaysWithDataRef() {
        return daysWithDataRef;
    }

    public DatabaseReference getDaysWithDataTodayRef() {
        return daysWithDataTodayRef;
    }

    public DatabaseReference getFitbitDayWithDataRef() {
        return fitbitDayWithDataRef;
    }

    public DatabaseReference getGoogleFitDayWithDataRef() {
        return googleFitDayWithDataRef;
    }

    public DatabaseReference getMoodAndEnergyDayWithDataRef() {
        return moodAndEnergyDayWithDataRef;
    }

    public DatabaseReference getAppUsageDayWithDataRef() {
        return appUsageDayWithDataRef;
    }

    public DatabaseReference getActivityDayWithDataRef() {
        return activityDayWithDataRef;
    }

    public DatabaseReference getInputsInUseRef() {
        return inputsInUseRef;
    }

    public DatabaseReference getFitbitIsInUseRef() {
        return fitbitIsInUseRef;
    }

    public DatabaseReference getGoogleFitIsInUseRef() {
        return googleFitIsInUseRef;
    }

    public DatabaseReference getMoodAndEnergyIsInUseRef() {
        return moodAndEnergyIsInUseRef;
    }

    public DatabaseReference getBehavioralActivationActivityIsInUseRef() {
        return behavioralActivationActivityIsInUseRef;
    }

    public DatabaseReference getAppUsageIsInUseRef() {
        return appUsageIsInUseRef;
    }

    public DatabaseReference getIsConnectedRef() {
        return isConnectedRef;
    }

    public DatabaseReference getAppUsageIsConnectedRef() {
        return appUsageIsConnectedRef;
    }

    public DatabaseReference getUserInputsRef() {
        return userInputsRef;
    }

    public DatabaseReference getConnectedServicesRef() {
        return connectedServicesRef;
    }

    public DatabaseReference getFitbitRef() {
        return fitbitRef;
    }

    public DatabaseReference getFitbitIsConnectedRef() {
        return fitbitIsConnectedRef;
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

    public DatabaseReference getGoogleFitIsConnectedRef() {
        return googleFitIsConnectedRef;
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

    public DatabaseReference getLast30DaysRef() {
        return last30DaysRef;
    }

    public DatabaseReference getFitbitLast30DaysRef() {
        return fitbitLast30DaysRef;
    }

    public DatabaseReference getFitbitLast30DaysTodayRef() {
        return fitbitLast30DaysTodayRef;
    }

    public DatabaseReference getFitbitLast30DaysYesterdayRef() {
        return fitbitLast30DaysYesterdayRef;
    }

    public DatabaseReference getFitbitLast30DaysAverageRef() {
        return fitbitLast30DaysAverageRef;
    }

    public DatabaseReference getGoogleFitLast30DaysRef() {
        return googleFitLast30DaysRef;
    }

    public DatabaseReference getGoogleFitLast30DaysTodayRef() {
        return googleFitLast30DaysTodayRef;
    }

    public DatabaseReference getGoogleFitLast30DaysYesterdayRef() {
        return googleFitLast30DaysYesterdayRef;
    }

    public DatabaseReference getGoogleFitLast30DaysAverageRef() {
        return googleFitLast30DaysAverageRef;
    }

    public DatabaseReference getAppUsageLast30DaysRef() {
        return appUsageLast30DaysRef;
    }

    public DatabaseReference getAppUsageThisDeviceLast30DaysRef() {
        return appUsageThisDeviceLast30DaysRef;
    }

    public DatabaseReference getAppUsageLast30DaysTodayRef() {
        return appUsageLast30DaysTodayRef;
    }

    public DatabaseReference getAppUsageLast30DaysAverageRef() {
        return appUsageLast30DaysAverageRef;
    }

    public DatabaseReference getUserInputLast30DaysRef() {
        return userInputLast30DaysRef;
    }

    public DatabaseReference getPersonalHashTagLast30DaysRef() {
        return personalHashTagLast30DaysRef;
    }

    public DatabaseReference getPersonalHashTagLast30DaysTodayRef() {
        return personalHashTagLast30DaysTodayRef;
    }

    public DatabaseReference getActivityLast30DaysRef() {
        return activityLast30DaysRef;
    }

    public DatabaseReference getActivityLast30DaysTodayRef() {
        return activityLast30DaysTodayRef;
    }

    public DatabaseReference getMoodAndEnergyLast30DaysRef() {
        return moodAndEnergyLast30DaysRef;
    }

    public DatabaseReference getMoodAndEnergyLast30DaysTodayRef() {
        return moodAndEnergyLast30DaysTodayRef;
    }

    public DatabaseReference getStatisticsLast30DaysRef() {
        return statisticsLast30DaysRef;
    }

    public DatabaseReference getStatisticsLast30DaysTodayRef() {
        return statisticsLast30DaysTodayRef;
    }

    public DatabaseReference getMeansLast30DaysRef() {
        return meansLast30DaysRef;
    }

    public DatabaseReference getRegressionLast30DaysRef() {
        return regressionLast30DaysRef;
    }

    public DatabaseReference getMoodRegressionLast30DaysRef() {
        return moodRegressionLast30DaysRef;
    }

    public DatabaseReference getMoodRegressionLast30DaysTodayRef() {
        return moodRegressionLast30DaysTodayRef;
    }

    public DatabaseReference getEnergyLevelRegressionLast30DaysRef() {
        return energyLevelRegressionLast30DaysRef;
    }

    public DatabaseReference getEnergyLevelRegressionLast30DaysTodayRef() {
        return energyLevelRegressionLast30DaysTodayRef;
    }

    public DatabaseReference getStressRef() {
        return stressRef;
    }

    public void setStressRef(DatabaseReference stressRef) {
        this.stressRef = stressRef;
    }

    public DatabaseReference getDailyReviewRef() {
        return dailyReviewRef;
    }

    public void setDailyReviewRef(DatabaseReference dailyReviewRef) {
        this.dailyReviewRef = dailyReviewRef;
    }

    public DatabaseReference getStressLast30DaysRef() {
        return stressLast30DaysRef;
    }

    public void setStressLast30DaysRef(DatabaseReference stressLast30DaysRef) {
        this.stressLast30DaysRef = stressLast30DaysRef;
    }

    public DatabaseReference getDailyReviewLast30DaysRef() {
        return dailyReviewLast30DaysRef;
    }

    public void setDailyReviewLast30DaysRef(DatabaseReference dailyReviewLast30DaysRef) {
        this.dailyReviewLast30DaysRef = dailyReviewLast30DaysRef;
    }

    public DatabaseReference getStressDayWithDataRef() {
        return stressDayWithDataRef;
    }

    public DatabaseReference getDailyReviewDayWithDataRef() {
        return dailyReviewDayWithDataRef;
    }

    public DatabaseReference getPhq9DayWithDataRef() {
        return phq9DayWithDataRef;
    }

    public DatabaseReference getGad7DayWithDataRef() {
        return gad7DayWithDataRef;
    }

    public DatabaseReference getPhq9Ref() {
        return phq9Ref;
    }

    public DatabaseReference getGad7Ref() {
        return gad7Ref;
    }

    public DatabaseReference getPhq9Last30DaysRef() {
        return phq9Last30DaysRef;
    }

    public DatabaseReference getGad7Last30DaysRef() {
        return gad7Last30DaysRef;
    }

    private String setUpDatesForToday() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return mdformat.format(calendar.getTime());
    }

    private String setUpDatesForThisMonth() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM", Locale.US);
        return sf.format(calendar.getTime());
    }

    private String setupDatesForThisWeek() {
        Calendar calendar = Calendar.getInstance();
        int weekMonth = calendar.get(Calendar.WEEK_OF_MONTH);
        return "Week" + weekMonth;
    }

    private String setUpDateForYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return mdformat.format(calendar.getTime());
    }

    private String setUpMonthForYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM", Locale.US);
        return sf.format(calendar.getTime());
    }

    private String setupWeekForYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        int weekMonth = calendar.get(Calendar.WEEK_OF_MONTH);
        return "Week" + weekMonth;
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
