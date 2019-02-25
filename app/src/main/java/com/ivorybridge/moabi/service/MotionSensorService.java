package com.ivorybridge.moabi.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInProfile;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitActivitySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDeviceStatusSummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitSleepSummary;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.repository.FitbitRepository;
import com.ivorybridge.moabi.repository.GoogleFitRepository;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.util.FormattedTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;

public class MotionSensorService extends Service implements SensorEventListener {

    private static final String TAG = MotionSensorService.class.getSimpleName();
    private SensorManager sensorManager;
    // Generates an event each time a step is taken by the user.
    // The timestamp of the event sensors_event_t.timestamp corresponds to when the foot hit the ground, generating a high variation in acceleration.
    private Sensor stepDetectorSensor;
    // Reports the number of steps taken by the user since the last reboot while activated.
    // The measurement is reported as a uint64_t in sensors_event_t.step_counter and is reset to zero only on a system reboot.
    // Compared to the step detector, the step counter can have a higher latency (up to 10 seconds).
    // Thanks to this latency, this sensor has a high accuracy;
    // While this sensor operates, it shall not disrupt any other sensors, in particular, the accelerometer, which might very well be in use.
    private Sensor stepCounterSensor;
    // Gravity for accelerometer data
    private float[] gravity = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];
    // sensor manager
    // sensor gravity
    private Sensor sensorGravity;
    private double bearing = 0;
    private double prevY;
    private double threshold;
    private boolean ignore;
    private int countdown;
    private long timeOfLastEntry = 0;
    private long steps = 0;
    private long activeMins = 0;
    private long sedentaryMins = 0;
    private double calories = 0;
    private double distance = 0;
    private Sensor tiltSensor;
    private static final int NOTIFICATION_ID = 561092162;
    private static final String NOTIFICATION_CHANNEL_NAME = "MOTION_SENSOR_SERVICE";
    private static final String NOTIFICATION_CHANNEL_DESC = "TRACKING_VARIOUS_MOTIONS";
    private NotificationManager notificationManager;
    private boolean isNotificationShowing;
    private Notification customNotification;
    private FormattedTime formattedTime;
    private BuiltInFitnessRepository builtInFitnessRepository;
    private BuiltInActivitySummary activitySummary;
    private BuiltInProfile profile;
    private NotificationCompat.Builder builder;
    private SharedPreferences notificationSharedPreferences;
    private SharedPreferences unitSharedPreferences;
    private FirebaseManager firebaseManager;
    private FitbitRepository fitbitRepository;
    private GoogleFitRepository googleFitRepository;
    private String dataSource;
    private String mainMeasure;
    private double bmr;
    private String unit;

    @Override
    public void onCreate() {
        super.onCreate();
        builder = new NotificationCompat.Builder(this,
                getApplicationContext().getString(R.string.MOTION_SENSOR_NOTIF_CHANNEL_ID));
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        formattedTime = new FormattedTime();
        firebaseManager = new FirebaseManager();
        builtInFitnessRepository = new BuiltInFitnessRepository(getApplication());
        notificationSharedPreferences = getSharedPreferences(
                getString(R.string.com_ivorybridge_mobai_NOTIFICATION_SHARED_PREFERENCE),
                Context.MODE_PRIVATE);
        unitSharedPreferences = getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_UNIT_SHARED_PREFERENCE_KEY),
                Context.MODE_PRIVATE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        fitbitRepository = new FitbitRepository(getApplication());
        googleFitRepository = new GoogleFitRepository(getApplication());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<BuiltInProfile> builtInProfiles = builtInFitnessRepository.getUserProfileNow();
                if (builtInProfiles != null && builtInProfiles.size() > 0) {
                    profile = builtInProfiles.get(0);
                    if (profile.getHeight() != null) {
                        if (profile.getGender() == null) {
                            bmr = 1577.5;
                        } else if (profile.getGender().equals(getString(R.string.profile_sex_male))) {
                            bmr = profile.getWeight() * 10 + 6.25 * profile.getHeight();
                            if (profile.getAge() != null) {
                                bmr = bmr - 5 * profile.getAge() + 5;
                            }
                        } else if (profile.getGender().equals(getString(R.string.profile_sex_female))) {
                            bmr = profile.getWeight() * 10 + 6.25 * profile.getHeight();
                            if (profile.getAge() != null) {
                                bmr = bmr - 5 * profile.getAge() - 161;
                            }
                        } else {
                            bmr = 1577.5;
                        }
                    }
                } else {
                    BuiltInProfile builtInProfile = new BuiltInProfile();
                    builtInProfile.setBMR(1577.5);
                    builtInProfile.setDateOfRegistration(formattedTime.getCurrentDateAsYYYYMMDD());
                    builtInProfile.setHeight(170d);
                    builtInProfile.setWeight(70d);
                    builtInProfile.setUniqueID(UUID.randomUUID().toString());
                    builtInFitnessRepository.insert(builtInProfile);
                }
                unit = unitSharedPreferences.getString(getString(R.string.com_ivorybridge_mobai_UNIT_KEY), getString(R.string.preference_unit_si_title));
                dataSource = notificationSharedPreferences.getString(getString(R.string.preference_fitness_tracker_source_notification),
                        getString(R.string.moabi_tracker_title));
                mainMeasure = notificationSharedPreferences.getString(getString(R.string.preference_fitness_tracker_activity_type_notification),
                        getString(R.string.activity_steps_title));
                Log.i(TAG, dataSource + ": " + mainMeasure);
                activitySummary = builtInFitnessRepository.getNow(formattedTime.getCurrentDateAsYYYYMMDD());
                if (activitySummary == null) {
                    activitySummary = new BuiltInActivitySummary();
                }
                if (activitySummary != null) {
                    activitySummary.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    activitySummary.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                    if (activitySummary.getSteps() != null) {
                        steps = activitySummary.getSteps();
                    }
                    if (activitySummary.getActiveMinutes() != null) {
                        activeMins = activitySummary.getActiveMinutes();
                    }
                    if (activitySummary.getSedentaryMinutes() != null) {
                        sedentaryMins = activitySummary.getSedentaryMinutes();
                    }
                    if (activitySummary.getDistance() != null) {
                        distance = activitySummary.getDistance();
                    }
                    if (activitySummary.getCalories() != null) {
                        calories = activitySummary.getCalories();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (notificationSharedPreferences.getBoolean(getString(R.string.preference_fitness_tracker_notification), false)) {
                                showNotification();
                            } else {
                                stopForeground(true);
                            }
                        }
                    });
                }
            }
        }).start();
        //showNotification();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.ivorybridge.moabi.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(MotionSensorService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MotionSensorService.this, 0, notificationIntent, 0);
        String measure = "Today: ";
        if (dataSource.equals(getString(R.string.moabi_tracker_title))) {
            if (mainMeasure.equals(getString(R.string.activity_steps_title))) {
                measure = getString(R.string.activity_steps_title) + ": " + steps + " " + getString(R.string.unit_step_plur);
            } else if (mainMeasure.equals(getString(R.string.activity_distance_title))) {
                if (unit.equals(getString(R.string.preference_unit_si_title))) {
                    measure = getString(R.string.activity_distance_title) + ": " + String.format(Locale.US, "%.2f", distance / 1000) + " " + getString(R.string.unit_distance_si);
                } else {
                    measure = getString(R.string.activity_distance_title) + ": " + String.format(Locale.US, "%.2f", distance / 1000 * 0.621371f) + " " + getString(R.string.unit_distance_usc);
                }
            } else if (mainMeasure.equals(getString(R.string.activity_active_minutes_title))) {
                measure = getString(R.string.activity_active_minutes_title) + ": " + TimeUnit.MILLISECONDS.toMinutes(activeMins) + " " + getString(R.string.unit_time_sing);
            } else if (mainMeasure.equals(getString(R.string.activity_sedentary_minutes_title))) {
                measure = getString(R.string.activity_sedentary_minutes_title) + ": " + TimeUnit.MILLISECONDS.toMinutes(sedentaryMins) + " " + getString(R.string.unit_time_sing);
            } else if (mainMeasure.equals(getString(R.string.activity_calories_title))) {
                measure = getString(R.string.activity_calories_title) + ": " + String.format(Locale.US, "%.0f", calories) + " " + getString(R.string.unit_calories);
            }
            builder.setSmallIcon(R.drawable.ic_monogram_white)
                    .setContentTitle(measure)
                    .setContentText(null)
                    //.setContentText("Today: " + String.format(Locale.US, "%.2f", distance / 1000) + " km, " + TimeUnit.MILLISECONDS.toMinutes(activeMins) + " mins, " + TimeUnit.MILLISECONDS.toMinutes(sedentaryMins) + " mins, " + calories + " Cal")
                    .setColor(getColor(R.color.colorPrimary))
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(pendingIntent);
            Notification notification = builder.build();
            notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel(getApplicationContext().getString(R.string.MOTION_SENSOR_NOTIF_CHANNEL_ID),
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(NOTIFICATION_CHANNEL_DESC);
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
            }
            startForeground(NOTIFICATION_ID, notification);
        } else if (dataSource.equals(getString(R.string.googlefit_title))) {
            Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String gFitMeasure = "Today: ";
                    List<GoogleFitSummary> activitySummaries = googleFitRepository.getAllNow(
                            formattedTime.getStartOfDay(formattedTime.getCurrentDateAsYYYYMMDD()),
                            formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD()));
                    if (activitySummaries != null && activitySummaries.size() > 0) {
                        GoogleFitSummary todaySummary = activitySummaries.get(0);
                        List<GoogleFitSummary.Summary> todaySummarySummaries = todaySummary.getSummaries();
                        if (mainMeasure.equals(getString(R.string.activity_steps_title))) {
                            for (GoogleFitSummary.Summary summary : todaySummarySummaries) {
                                if (summary.getName().equals(getString(R.string.activity_steps_camel_case))) {
                                    gFitMeasure = getString(R.string.activity_steps_title) + ": " + summary.getValue().longValue() + " " + getString(R.string.unit_step_plur);
                                }
                            }
                        } else if (mainMeasure.equals(getString(R.string.activity_distance_title))) {
                            for (GoogleFitSummary.Summary summary : todaySummarySummaries) {
                                if (summary.getName().equals(getString(R.string.activity_distance_camel_case))) {
                                    if (unit.equals(getString(R.string.preference_unit_si_title))) {
                                        gFitMeasure = getString(R.string.activity_distance_title) + ": " + String.format(Locale.US, "%.2f", summary.getValue() / 1000) + " " + getString(R.string.unit_distance_si);
                                    } else {
                                        gFitMeasure = getString(R.string.activity_distance_title) + ": " + String.format(Locale.US, "%.2f", summary.getValue() / 1000 * 0.621371f) + " " + getString(R.string.unit_distance_usc);
                                    }
                                }
                            }
                        } else if (mainMeasure.equals(getString(R.string.activity_active_minutes_title))) {
                            for (GoogleFitSummary.Summary summary : todaySummarySummaries) {
                                if (summary.getName().equals(getString(R.string.activity_active_minutes_camel_case))) {
                                    gFitMeasure = getString(R.string.activity_active_minutes_title) + ": " + TimeUnit.MILLISECONDS.toMinutes(summary.getValue().longValue()) + " " + getString(R.string.unit_time_sing);
                                }
                            }
                        } else if (mainMeasure.equals(getString(R.string.activity_sedentary_minutes_title))) {
                            for (GoogleFitSummary.Summary summary : todaySummarySummaries) {
                                if (summary.getName().equals(getString(R.string.activity_sedentary_minutes_camel_case))) {
                                    gFitMeasure = getString(R.string.activity_sedentary_minutes_title) + ": " + TimeUnit.MILLISECONDS.toMinutes(summary.getValue().longValue()) + " " + getString(R.string.unit_time_sing);
                                }
                            }
                        } else if (mainMeasure.equals(getString(R.string.activity_calories_title))) {
                            for (GoogleFitSummary.Summary summary : todaySummarySummaries) {
                                if (summary.getName().equals(getString(R.string.activity_calories_camel_case))) {
                                    gFitMeasure = getString(R.string.activity_calories_title) + ": " + String.format(Locale.US, "%.0f", summary.getValue()) + " " + getString(R.string.unit_calories);
                                }
                            }
                        }
                        final String s = gFitMeasure;
                        final String lastSyncTime = todaySummary.getLastSyncTime();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                builder.setSmallIcon(R.drawable.ic_monogram_white)
                                        .setContentTitle(s)
                                        .setShowWhen(false)
                                        .setContentText(getString(R.string.tracker_google_fit_last_sync_prompt) + " " + lastSyncTime)
                                        .setColor(getColor(R.color.colorPrimary))
                                        .setOngoing(true)
                                        .setOnlyAlertOnce(true)
                                        .setContentIntent(pendingIntent);
                                Notification notification = builder.build();
                                notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
                                if (Build.VERSION.SDK_INT >= 26) {
                                    NotificationChannel channel = new NotificationChannel(getApplicationContext().getString(R.string.MOTION_SENSOR_NOTIF_CHANNEL_ID),
                                            NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                                    channel.setDescription(NOTIFICATION_CHANNEL_DESC);
                                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.createNotificationChannel(channel);
                                }
                                startForeground(NOTIFICATION_ID, notification);
                            }
                        });
                    } else {
                        if (mainMeasure.equals(getString(R.string.activity_steps_title))) {
                            gFitMeasure = getString(R.string.activity_steps_title) + ": " + 0 + " " + getString(R.string.unit_step_sing);
                        } else if (mainMeasure.equals(getString(R.string.activity_distance_title))) {
                            if (unit.equals(getString(R.string.preference_unit_si_title))) {
                                gFitMeasure = getString(R.string.activity_distance_title) + ": " + 0 + " " + getString(R.string.unit_distance_si);
                            } else {
                                gFitMeasure = getString(R.string.activity_distance_title) + ": " + 0 + " " + getString(R.string.unit_distance_usc);
                            }
                        } else if (mainMeasure.equals(getString(R.string.activity_active_minutes_title))) {
                            gFitMeasure = getString(R.string.activity_active_minutes_title) + ": " + 0 + " " + getString(R.string.unit_time_sing);
                        } else if (mainMeasure.equals(getString(R.string.activity_sedentary_minutes_title))) {
                            gFitMeasure = getString(R.string.activity_sedentary_minutes_title) + ": " + 0 + " " + getString(R.string.unit_time_sing);
                        } else if (mainMeasure.equals(getString(R.string.activity_calories_title))) {
                            gFitMeasure = getString(R.string.activity_calories_title) + ": " + 0 + " " + getString(R.string.unit_calories);
                        }
                        final String s = gFitMeasure;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                builder.setSmallIcon(R.drawable.ic_monogram_white)
                                        .setContentTitle(s)
                                        .setShowWhen(false)
                                        .setContentText(getString(R.string.tracker_google_fit_last_sync_prompt) + " " + formattedTime.getCurrentTimeAsHMMA())
                                        .setColor(getColor(R.color.colorPrimary))
                                        .setOngoing(true)
                                        .setOnlyAlertOnce(true)
                                        .setContentIntent(pendingIntent);
                                Notification notification = builder.build();
                                notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
                                if (Build.VERSION.SDK_INT >= 26) {
                                    NotificationChannel channel = new NotificationChannel(getApplicationContext().getString(R.string.MOTION_SENSOR_NOTIF_CHANNEL_ID),
                                            NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                                    channel.setDescription(NOTIFICATION_CHANNEL_DESC);
                                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.createNotificationChannel(channel);
                                }
                                startForeground(NOTIFICATION_ID, notification);
                            }
                        });
                    }
                }
            }).start();
        } else if (dataSource.equals(getString(R.string.fitbit_title))) {
            Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String fitbitMeasure = "Today: ";
                    List<FitbitDailySummary> activitySummaries = fitbitRepository.getAllNow(
                            formattedTime.getStartOfDay(formattedTime.getCurrentDateAsYYYYMMDD()),
                            formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD()));
                    if (activitySummaries != null && activitySummaries.size() > 0) {
                        FitbitDailySummary todaySummary = activitySummaries.get(0);
                        FitbitActivitySummary activitySummary = todaySummary.getActivitySummary();
                        FitbitSleepSummary sleepSummary = todaySummary.getSleepSummary();
                        FitbitDeviceStatusSummary deviceSummary = todaySummary.getDeviceStatusSummary();
                        if (mainMeasure.equals(getString(R.string.activity_steps_title))) {
                            fitbitMeasure = getString(R.string.activity_steps_title) + ": " + activitySummary.getSummary().getSteps() + " " + getString(R.string.unit_step_plur);
                        } else if (mainMeasure.equals(getString(R.string.activity_distance_title))) {
                            if (unit.equals(getString(R.string.preference_unit_si_title))) {
                                fitbitMeasure = getString(R.string.activity_distance_title) + ": " + activitySummary.getSummary().getDistances().get(0).getDistance() + " " + getString(R.string.unit_distance_si);
                            } else {
                                fitbitMeasure = getString(R.string.activity_distance_title) + ": " + activitySummary.getSummary().getDistances().get(0).getDistance() * 0.621371f + " " + getString(R.string.unit_distance_si);
                            }
                        } else if (mainMeasure.equals(getString(R.string.activity_active_minutes_title))) {
                            int activeMins = activitySummary.getSummary().getFairlyActiveMinutes().intValue() + activitySummary.getSummary().getVeryActiveMinutes().intValue();
                            fitbitMeasure = getString(R.string.activity_active_minutes_title) + ": " + activeMins + " " + getString(R.string.unit_time_sing);
                        } else if (mainMeasure.equals(getString(R.string.activity_sedentary_minutes_title))) {
                            fitbitMeasure = getString(R.string.activity_sedentary_minutes_title) + ": " + activitySummary.getSummary().getSedentaryMinutes() + " " + getString(R.string.unit_time_sing);
                        } else if (mainMeasure.equals(getString(R.string.activity_calories_title))) {
                            fitbitMeasure = getString(R.string.activity_calories_title) + ": " + activitySummary.getSummary().getCaloriesOut() + " " + getString(R.string.unit_calories);
                        } else if (mainMeasure.equals(getString(R.string.activity_floors_title))) {
                            fitbitMeasure = getString(R.string.activity_floors_title) + ": " + activitySummary.getSummary().getFloors() + " " + getString(R.string.unit_floor_plur);
                        } else if (mainMeasure.equals(getString(R.string.activity_sleep_title))) {
                            fitbitMeasure = getString(R.string.activity_sleep_title) + ": " + sleepSummary.getSummary().getTotalMinutesAsleep() + " " + getString(R.string.unit_time_sing);
                        }
                        final String s = fitbitMeasure;
                        String lastSyncTimeHHMM = "";
                        if (deviceSummary != null) {
                            if (deviceSummary.getLastSyncTime() != null) {
                                String lastSyncTime = deviceSummary.getLastSyncTime();
                                if (lastSyncTime != null) {
                                    lastSyncTimeHHMM = lastSyncTime.substring(11, lastSyncTime.length() - 7);
                                }
                                SimpleDateFormat truncatedDF = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                                SimpleDateFormat timeOnlyDF = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
                                try {
                                    Date truncatedDate = truncatedDF.parse(lastSyncTimeHHMM);
                                    String timeOnly = timeOnlyDF.format(truncatedDate);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            builder.setSmallIcon(R.drawable.ic_monogram_white)
                                                    .setContentTitle(s)
                                                    .setShowWhen(false)
                                                    .setContentText(getString(R.string.tracker_fitbit_last_sync_prompt) + " " + timeOnly)
                                                    .setColor(getColor(R.color.colorPrimary))
                                                    .setOngoing(true)
                                                    .setOnlyAlertOnce(true)
                                                    .setContentIntent(pendingIntent);
                                            Notification notification = builder.build();
                                            notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
                                            if (Build.VERSION.SDK_INT >= 26) {
                                                NotificationChannel channel = new NotificationChannel(getApplicationContext().getString(R.string.MOTION_SENSOR_NOTIF_CHANNEL_ID),
                                                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                                                channel.setDescription(NOTIFICATION_CHANNEL_DESC);
                                                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                                notificationManager.createNotificationChannel(channel);
                                            }
                                            startForeground(NOTIFICATION_ID, notification);
                                        }
                                    });
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    builder.setSmallIcon(R.drawable.ic_monogram_white)
                                            .setContentTitle(s)
                                            .setShowWhen(false)
                                            .setContentText(getString(R.string.tracker_fitbit_last_sync_prompt) + " " + lastSyncTimeHHMM)
                                            .setColor(getColor(R.color.colorPrimary))
                                            .setOngoing(true)
                                            .setOnlyAlertOnce(true)
                                            .setContentIntent(pendingIntent);
                                    Notification notification = builder.build();
                                    notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
                                    if (Build.VERSION.SDK_INT >= 26) {
                                        NotificationChannel channel = new NotificationChannel(getApplicationContext().getString(R.string.MOTION_SENSOR_NOTIF_CHANNEL_ID),
                                                NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                                        channel.setDescription(NOTIFICATION_CHANNEL_DESC);
                                        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        notificationManager.createNotificationChannel(channel);
                                    }
                                    startForeground(NOTIFICATION_ID, notification);
                                }
                            }
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    builder.setSmallIcon(R.drawable.ic_monogram_white)
                                            .setContentTitle(s)
                                            .setShowWhen(false)
                                            .setContentText(getString(R.string.tracker_fitbit_last_sync_prompt) + " " + formattedTime.getCurrentTimeAsHMMA())
                                            .setColor(getColor(R.color.colorPrimary))
                                            .setOngoing(true)
                                            .setOnlyAlertOnce(true)
                                            .setContentIntent(pendingIntent);
                                    Notification notification = builder.build();
                                    notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
                                    if (Build.VERSION.SDK_INT >= 26) {
                                        NotificationChannel channel = new NotificationChannel(getApplicationContext().getString(R.string.MOTION_SENSOR_NOTIF_CHANNEL_ID),
                                                NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                                        channel.setDescription(NOTIFICATION_CHANNEL_DESC);
                                        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        notificationManager.createNotificationChannel(channel);
                                    }
                                    startForeground(NOTIFICATION_ID, notification);
                                }
                            });
                        }
                    } else {
                        if (mainMeasure.equals(getString(R.string.activity_steps_title))) {
                            fitbitMeasure = getString(R.string.activity_steps_title) + ": " + 0 + " " + getString(R.string.unit_step_sing);
                        } else if (mainMeasure.equals(getString(R.string.activity_distance_title))) {
                            if (unit.equals(getString(R.string.preference_unit_si_title))) {
                                fitbitMeasure = getString(R.string.activity_distance_title) + ": " + 0 + " " + getString(R.string.unit_distance_si);
                            } else {
                                fitbitMeasure = getString(R.string.activity_distance_title) + ": " + 0 + " " + getString(R.string.unit_distance_si);
                            }
                        } else if (mainMeasure.equals(getString(R.string.activity_active_minutes_title))) {
                            int activeMins = 0;
                            fitbitMeasure = getString(R.string.activity_active_minutes_title) + ": " + activeMins + " " + getString(R.string.unit_time_sing);
                        } else if (mainMeasure.equals(getString(R.string.activity_sedentary_minutes_title))) {
                            fitbitMeasure = getString(R.string.activity_sedentary_minutes_title) + ": " + 0 + " " + getString(R.string.unit_time_sing);
                        } else if (mainMeasure.equals(getString(R.string.activity_calories_title))) {
                            fitbitMeasure = getString(R.string.activity_calories_title) + ": " + 0 + " " + getString(R.string.unit_calories);
                        } else if (mainMeasure.equals(getString(R.string.activity_floors_title))) {
                            fitbitMeasure = getString(R.string.activity_floors_title) + ": " + 0 + " " + getString(R.string.unit_floor_sing);
                        } else if (mainMeasure.equals(getString(R.string.activity_sleep_title))) {
                            fitbitMeasure = getString(R.string.activity_sleep_title) + ": " + 0 + " " + getString(R.string.unit_time_sing);
                        }
                        final String s = fitbitMeasure;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                builder.setSmallIcon(R.drawable.ic_monogram_white)
                                        .setContentTitle(s)
                                        .setShowWhen(false)
                                        .setContentText(getString(R.string.tracker_fitbit_last_sync_prompt) + " " + formattedTime.getCurrentTimeAsHMMA())
                                        .setColor(getColor(R.color.colorPrimary))
                                        .setOngoing(true)
                                        .setOnlyAlertOnce(true)
                                        .setContentIntent(pendingIntent);
                                Notification notification = builder.build();
                                notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
                                if (Build.VERSION.SDK_INT >= 26) {
                                    NotificationChannel channel = new NotificationChannel(getApplicationContext().getString(R.string.MOTION_SENSOR_NOTIF_CHANNEL_ID),
                                            NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                                    channel.setDescription(NOTIFICATION_CHANNEL_DESC);
                                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.createNotificationChannel(channel);
                                }
                                startForeground(NOTIFICATION_ID, notification);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                activitySummary = builtInFitnessRepository.getNow(formattedTime.getCurrentDateAsYYYYMMDD());
                if (activitySummary == null) {
                    activitySummary = new BuiltInActivitySummary();
                    activitySummary.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                    activitySummary.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                }
                if (activitySummary.getSteps() != null) {
                    steps = activitySummary.getSteps();
                } else {
                    steps = 0;
                }
                if (activitySummary.getActiveMinutes() != null) {
                    activeMins = activitySummary.getActiveMinutes();
                } else {
                    activeMins = 0;
                }
                if (activitySummary.getSedentaryMinutes() != null) {
                    sedentaryMins = activitySummary.getSedentaryMinutes();
                } else {
                    sedentaryMins = 0;
                }
                if (activitySummary.getDistance() != null) {
                    distance = activitySummary.getDistance();
                } else {
                    distance = 0;
                }
                if (activitySummary.getCalories() != null) {
                    calories = activitySummary.getCalories();
                } else {
                    calories = 0;
                }
                if (activitySummary.getLastSensorTimeStamp() != null) {
                    timeOfLastEntry = activitySummary.getLastSensorTimeStamp();
                } else {
                    timeOfLastEntry = 0;
                }
                Sensor sensor = event.sensor;
                float[] values = event.values;
                int value = -1;
                if (values.length > 0) {
                    value = (int) values[0];
                }
                if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                    steps++;
                    if (profile != null && profile.getHeight() != null) {
                        if (profile != null && profile.getGender() == null) {
                            distance = steps * (profile.getHeight() * 0.414) / 100;
                        } else if (profile.getGender().equals(getString(R.string.profile_sex_male))) {
                            distance = steps * profile.getHeight() * 0.415 / 100;
                        } else if (profile.getGender().equals(getString(R.string.profile_sex_female))) {
                            distance = steps * profile.getHeight() * 0.413 / 100;
                        } else {
                            distance = steps * (profile.getHeight() * 0.414) / 100;
                        }
                    } else {
                        distance = steps * 0.762;
                    }
                    long timeNow = (new Date()).getTime()
                            + (event.timestamp - System.nanoTime()) / 1000000L;
                    Log.i(TAG, "Last: " + timeOfLastEntry + ", Now: " + timeNow);
                    if (timeOfLastEntry != 0) {
                        if (timeNow - timeOfLastEntry < 5000 && timeNow - timeOfLastEntry > 0) {
                            activeMins += timeNow - timeOfLastEntry;
                        }
                    } else {
                        activeMins = 0;
                    }
                    double elapsedTime = formattedTime.getCurrentTimeInMilliSecs() - formattedTime.getStartOfDay(formattedTime.getCurrentDateAsYYYYMMDD());
                    calories = (double) steps / 1000 * 40 + bmr * (elapsedTime / 86400000);
                    activitySummary.setLastSensorTimeStamp(timeNow);
                    activitySummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    // we need to use a low pass filter to make data smoothed
                    smoothed = lowPassFilter(event.values, gravity);
                    gravity[0] = smoothed[0];
                    gravity[1] = smoothed[1];
                    gravity[2] = smoothed[2];
                    if (ignore) {
                        countdown--;
                        ignore = (countdown >= 0) && ignore;
                    } else
                        countdown = 22;
                    if ((Math.abs(prevY - gravity[1]) > threshold) && !ignore) {
                        steps++;
                        ignore = true;
                        if (profile != null && profile.getHeight() != null) {
                            if (profile != null && profile.getGender() == null) {
                                distance = steps * (profile.getHeight() * 0.414) / 100;
                            } else if (profile.getGender().equals(getString(R.string.profile_sex_male))) {
                                distance = steps * profile.getHeight() * 0.415 / 100;
                            } else if (profile.getGender().equals(getString(R.string.profile_sex_female))) {
                                distance = steps * profile.getHeight() * 0.413 / 100;
                            } else {
                                distance = steps * (profile.getHeight() * 0.414) / 100;
                            }
                        } else {
                            distance = steps * 0.762;
                        }
                        long timeNow = (new Date()).getTime()
                                + (event.timestamp - System.nanoTime()) / 1000000L;
                        Log.i(TAG, "Last: " + timeOfLastEntry + ", Now: " + timeNow);
                        if (timeOfLastEntry != 0) {
                            if (timeNow - timeOfLastEntry < 5000 && timeNow - timeOfLastEntry > 0) {
                                activeMins += timeNow - timeOfLastEntry;
                            }
                        } else {
                            activeMins = 0;
                        }
                        double elapsedTime = formattedTime.getCurrentTimeInMilliSecs() - formattedTime.getStartOfDay(formattedTime.getCurrentDateAsYYYYMMDD());
                        calories = (double) steps / 1000 * 40 + bmr * (elapsedTime / 86400000);
                        activitySummary.setLastSensorTimeStamp(timeNow);
                        activitySummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    }
                    prevY = gravity[1];
                }
                activitySummary.setSteps(steps);
                activitySummary.setDistance(distance);
                activitySummary.setActiveMinutes(activeMins);
                sedentaryMins = formattedTime.getCurrentTimeInMilliSecs() - formattedTime.getStartOfDay(formattedTime.getCurrentDateAsYYYYMMDD()) - activeMins;
                activitySummary.setSedentaryMinutes(sedentaryMins);
                activitySummary.setCalories(calories);
                builtInFitnessRepository.insert(activitySummary, formattedTime.getCurrentDateAsYYYYMMDD());
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    firebaseManager.getBuiltInFitnessTrackerThisDeviceRef()
                            .child(formattedTime.getCurrentDateAsYYYYMMDD())
                            .setValue(activitySummary);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (notificationSharedPreferences.getBoolean(getString(R.string.preference_fitness_tracker_notification), false)) {
                            if (dataSource.equals(getString(R.string.moabi_tracker_title))) {
                                showNotification();
                            }
                        } else {
                            stopForeground(true);
                        }
                    }
                });
            }
        }).start();
    }

    protected float[] lowPassFilter(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + 1.0f * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
