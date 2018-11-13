package com.ivorybridge.moabi.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityFavorited;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityInLibrary;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInProfile;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.AsyncCallsMasterRepository;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.service.AnxietyInsightCalculatorJob;
import com.ivorybridge.moabi.service.AsyncCallsForTodayJob;
import com.ivorybridge.moabi.service.AsyncCallsForYesterdayJob;
import com.ivorybridge.moabi.service.BodyInsightCalculatorDailyJob;
import com.ivorybridge.moabi.service.DepressionInsightCalculatorJob;
import com.ivorybridge.moabi.service.MoodAndEnergyInsightCalculatorDailyJob;
import com.ivorybridge.moabi.service.MoodAndEnergyInsightCalculatorJob;
import com.ivorybridge.moabi.service.MotionSensorResetDailyJob;
import com.ivorybridge.moabi.service.DailyReviewInsightCalculatorDailyJob;
import com.ivorybridge.moabi.service.DailyReviewInsightCalculatorJob;
import com.ivorybridge.moabi.service.StressInsightCalculatorDailyJob;
import com.ivorybridge.moabi.service.StressInsightCalculatorJob;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.BAActivityViewModel;

import java.util.List;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

/**
 * This class takes care of everything that happens in a splash activity,
 * the first activity to be launched when the user opens the app.
 * After 2 seconds, a first-time user will be redirected to the intro (login) activity while
 * an authenticated user will be redirected to the main activity.
 * For an authenticated user, network calls to services like Fitbit and Google Fit will be made
 * during the lifespan of this activity to update the user data.
 * For any user, a list of Behavioral Activity Activities (List<BAActivityFavorited>) is initialized
 * if it does not exist on the device.
 */
public class SplashActivity extends AppCompatActivity {


    private static final String TAG = SplashActivity.class.getSimpleName();

    // Splash screen timer - 2 seconds
    private static int SPLASH_TIME_OUT = 2000;
    private boolean isSignedIn;
    private FirebaseAuth mAuth;
    private FirebaseManager firebaseManager;
    private AsyncCallsMasterRepository asyncCallsMasterRepository;
    private SharedPreferences fitbitSharedPreferences;
    private SharedPreferences builtInSharedPreferences;
    private SharedPreferences googleFitSharedPreferences;
    private SharedPreferences appUsageSharedPreferences;
    private SharedPreferences moodAndEnergyUsageSharedPreferences;
    private SharedPreferences unitSharedPreferences;
    private FormattedTime formattedTime;
    private BuiltInFitnessRepository builtInFitnessRepository;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        MotionSensorResetDailyJob.scheduleJob();
        MoodAndEnergyInsightCalculatorDailyJob.scheduleJob();
        MoodAndEnergyInsightCalculatorJob.scheduleJob();
        BodyInsightCalculatorDailyJob.scheduleJob();
        StressInsightCalculatorJob.scheduleJob();
        StressInsightCalculatorDailyJob.scheduleJob();
        DailyReviewInsightCalculatorJob.scheduleJob();
        DailyReviewInsightCalculatorDailyJob.scheduleJob();
        DepressionInsightCalculatorJob.scheduleJob();
        AnxietyInsightCalculatorJob.scheduleJob();
        AsyncCallsForTodayJob.scheduleJob();
        AsyncCallsForYesterdayJob.scheduleJob();
        builtInFitnessRepository = new BuiltInFitnessRepository(getApplication());
        fitbitSharedPreferences = this.getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_FITBIT_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        builtInSharedPreferences = this.getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_BUILT_IN_FITNESS_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        googleFitSharedPreferences = this.getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_GOOGLEFIT_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        appUsageSharedPreferences = this.getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_APP_USAGE_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        moodAndEnergyUsageSharedPreferences = this.getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_MOOD_AND_ENERGY_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        formattedTime = new FormattedTime();
        asyncCallsMasterRepository = new AsyncCallsMasterRepository(this, formattedTime.getCurrentDateAsYYYYMMDD());
        unitSharedPreferences = this.getSharedPreferences(getString(R.string.com_ivorybridge_moabi_UNIT_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        configureSharedPreferences();


        BAActivityViewModel activityViewModel = ViewModelProviders.of(this).get(BAActivityViewModel.class);

        activityViewModel.getAllActivitiesInLibrary().observe(this, new Observer<List<BAActivityInLibrary>>() {
            @Override
            public void onChanged(@Nullable List<BAActivityInLibrary> baActivityInLibraries) {
                if (baActivityInLibraries == null) {
                    Log.i(TAG, "Initializing activities in library");
                } else {
                    Log.i(TAG, baActivityInLibraries.toString());
                    if (baActivityInLibraries.size() == 0) {
                        activityViewModel.initializeBAActivityInLibrary();
                    } else {

                    }
                }
            }
        });

        activityViewModel.getAllFavoritedActivities().observe(this, new Observer<List<BAActivityFavorited>>() {
            @Override
            public void onChanged(@Nullable List<BAActivityFavorited> baActivitiesFavorited) {
                if (baActivitiesFavorited == null) {
                    Log.i(TAG, "Initializing favorite activities");
                    activityViewModel.initializeFavoritedActivities();
                } else {
                    Log.i(TAG, baActivitiesFavorited.toString());
                }
            }
        });

        Handler handler = new Handler();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {
                    //  Launch app intro
                    builtInFitnessRepository.deleteAllUserProfiles();
                    BuiltInProfile builtInProfile = new BuiltInProfile();
                    builtInProfile.setBMR(1577.5);
                    builtInProfile.setDateOfRegistration(formattedTime.getCurrentDateAsYYYYMMDD());
                    builtInProfile.setHeight(170d);
                    builtInProfile.setWeight(70d);
                    builtInProfile.setUniqueID(UUID.randomUUID().toString());
                    builtInFitnessRepository.insert(builtInProfile);
                    //builtInProfile.setId();
                    final Intent i = new Intent(SplashActivity.this, IntroActivity.class);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(i);
                            finish();
                        }
                    }, SPLASH_TIME_OUT);
                } else {
                    //  Launch app intro
                    final Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(i);
                            finish();
                        }
                    }, SPLASH_TIME_OUT);
                }
            }
        });
        // Start the thread
        t.start();

        /*
        // check if a user is signed in
        if (mAuth.getCurrentUser() != null) {
            //asyncCallsMasterRepository.makeCallsToConnectedServices();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // proceed to main activity after 2 seconds if the user is signed in.
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    // close splash activity
                    finish();
                }
            }, SPLASH_TIME_OUT);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // if a user is not signed in, redirect to sign-in activity after 2 seconds.
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.putExtra("redirected_from", "SplashActivity");
                    startActivity(intent);
                    // close splash activity
                    finish();

                }
            }, SPLASH_TIME_OUT);
        }*/
    }

    private void configureSharedPreferences() {
        SharedPreferences.Editor fitbitSPEditor = fitbitSharedPreferences.edit();
        SharedPreferences.Editor googleFitSPEditor = googleFitSharedPreferences.edit();
        SharedPreferences.Editor builtInFitnessSPEditor = builtInSharedPreferences.edit();
        SharedPreferences.Editor appUsageSPEditor = appUsageSharedPreferences.edit();
        SharedPreferences.Editor moodAndEnergySPEditor = moodAndEnergyUsageSharedPreferences.edit();
        SharedPreferences.Editor unitSPEditor = unitSharedPreferences.edit();

        // initialize four fitbit activities to display by default
        if (!fitbitSharedPreferences.contains(getString(R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY))) {
            fitbitSPEditor.putString(getString(R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), getString(R.string.activity_steps_title));
            fitbitSPEditor.apply();
        }
        if (!fitbitSharedPreferences.contains(getString(R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY))) {
            fitbitSPEditor.putString(getString(R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY), getString(R.string.activity_active_minutes_title));
            fitbitSPEditor.apply();
        }
        if (!fitbitSharedPreferences.contains(getString(R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY))) {
            fitbitSPEditor.putString(getString(R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY), getString(R.string.activity_distance_title));
            fitbitSPEditor.apply();
        }
        if (!fitbitSharedPreferences.contains(getString(R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY))) {
            fitbitSPEditor.putString(getString(R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY), getString(R.string.activity_sleep_title));
            fitbitSPEditor.apply();
        }

        // initialize four Google Fit activities to display by default
        if (!googleFitSharedPreferences.contains(getString(R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY))) {
            googleFitSPEditor.putString(getString(R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), getString(R.string.activity_steps_title));
            googleFitSPEditor.apply();
        }
        if (!googleFitSharedPreferences.contains(getString(R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY))) {
            googleFitSPEditor.putString(getString(R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY), getString(R.string.activity_active_minutes_title));
            googleFitSPEditor.apply();
        }
        if (!googleFitSharedPreferences.contains(getString(R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY))) {
            googleFitSPEditor.putString(getString(R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY), getString(R.string.activity_distance_title));
            googleFitSPEditor.apply();
        }
        if (!googleFitSharedPreferences.contains(getString(R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY))) {
            googleFitSPEditor.putString(getString(R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY), getString(R.string.activity_calories_title));
            googleFitSPEditor.apply();
        }

        // initialize four Google Fit activities to display by default
        if (!builtInSharedPreferences.contains(getString(R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY))) {
            builtInFitnessSPEditor.putString(getString(R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),  getString(R.string.activity_steps_title));
            builtInFitnessSPEditor.apply();
        }
        if (!builtInSharedPreferences.contains(getString(R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY))) {
            builtInFitnessSPEditor.putString(getString(R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY), getString(R.string.activity_active_minutes_title));
            builtInFitnessSPEditor.apply();
        }
        if (!builtInSharedPreferences.contains(getString(R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY))) {
            builtInFitnessSPEditor.putString(getString(R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY), getString(R.string.activity_distance_title));
            builtInFitnessSPEditor.apply();
        }
        if (!builtInSharedPreferences.contains(getString(R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY))) {
            builtInFitnessSPEditor.putString(getString(R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY), getString(R.string.activity_calories_title));
            builtInFitnessSPEditor.apply();
        }

        // initialize time range for app usage chart.
        if (!appUsageSharedPreferences.contains(getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY))) {
            appUsageSPEditor.putString(getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),  getString(R.string.day));
            appUsageSPEditor.apply();
        }

        // initialize time range for mood and energy chart.
        if (!moodAndEnergyUsageSharedPreferences.contains(getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY))) {
            moodAndEnergySPEditor.putString(getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY), getString(R.string.day));
            moodAndEnergySPEditor.apply();
        }

        if (!unitSharedPreferences.contains(getString(R.string.com_ivorybridge_mobai_UNIT_KEY))) {
            unitSPEditor.putString(getString(R.string.com_ivorybridge_mobai_UNIT_KEY), getString(R.string.preference_unit_si_title));
            unitSPEditor.apply();
        }
    }
}
