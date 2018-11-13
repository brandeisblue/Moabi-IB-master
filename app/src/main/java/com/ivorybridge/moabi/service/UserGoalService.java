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

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.repository.DataInUseRepository;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;

//TODO - notification setting in the settings fragment
public class UserGoalService extends Service implements SensorEventListener {

    private static final String TAG = UserGoalService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 590234162;
    private static final String NOTIFICATION_CHANNEL_NAME = "User goal";
    private static final String NOTIFICATION_CHANNEL_DESC = "Custom goal tracking";
    private NotificationManager notificationManager;
    private boolean isNotificationShowing;
    private Notification customNotification;
    private FormattedTime formattedTime;
    private String goalType = "";
    private String goalName = "";
    private Double goal = 0d;
    private Boolean hasLong = false;
    private Long progressLong = 0L;
    private Double progressDouble = 0d;
    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private BuiltInFitnessRepository builtInFitnessRepository;
    private DataInUseRepository dataInUseRepository;
    private BuiltInActivitySummary activitySummary;
    private long timeOfLastEntry = 0;
    private long steps = 0;
    private long activeMins = 0;
    private long sedentaryMins = 0;
    private double calories = 0;
    private double distance = 0;
    private NotificationCompat.Builder builder;
    private SharedPreferences notificationSharedPreferences;
    private SharedPreferences.Editor notificationSPEditor;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationSharedPreferences = getSharedPreferences(
                getString(R.string.com_ivorybridge_mobai_NOTIFICATION_SHARED_PREFERENCE),
                Context.MODE_PRIVATE);
        if (notificationSharedPreferences.getBoolean(
                getString(R.string.preference_personal_goal_notification), false)) {
            this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            this.isNotificationShowing = false;
            this.formattedTime = new FormattedTime();
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            builtInFitnessRepository = new BuiltInFitnessRepository(getApplication());
            dataInUseRepository = new DataInUseRepository(getApplication());
            Log.i(TAG, intent.getStringExtra("goalType") + ": " + intent.getStringExtra("goalName") + " " + intent.getDoubleExtra("goal", 0));
            if (intent.getStringExtra("goalType") != null) {
                goalType = intent.getStringExtra("goalType");
            }
            if (intent.getStringExtra("goalName") != null) {
                goalName = intent.getStringExtra("goalName");
            }
            goal = intent.getDoubleExtra("goal", 0);
            if (goalType.equals(getString(R.string.moabi_tracker_camel_case))) {
                Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
                            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
                            sensorManager.registerListener(UserGoalService.this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
                        }
                        activitySummary = builtInFitnessRepository.getNow(formattedTime.getCurrentDateAsYYYYMMDD());
                        if (activitySummary == null) {
                            activitySummary = new BuiltInActivitySummary();
                            activitySummary.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                            activitySummary.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                        }
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
                                showMoabiNotification();
                            }
                        });
                    }
                }).start();
                return START_STICKY;
            } else {
                if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
                    stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
                    sensorManager.unregisterListener(UserGoalService.this, stepDetectorSensor);
                }
                if (intent.getBooleanExtra("hasLong", false)) {
                    hasLong = true;
                } else {
                    hasLong = false;
                }
                if (hasLong) {
                    progressLong = intent.getLongExtra("progress", 0);
                } else {
                    progressDouble = intent.getDoubleExtra("progress", 0);
                }
                startInForeground();
                return START_STICKY;
            }
        } else{
            stopForeground(true);
            return START_NOT_STICKY;
        }//return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void startInForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        String tracker = "";
        int progressMax = 0;
        int progress = 0;
        if (hasLong) {
            tracker = progressLong + " / " + goal.longValue();
            progressMax = goal.intValue();
            progress = progressLong.intValue();
        } else {
            tracker = String.format(Locale.US, "%.2f", progressDouble) + " / " + String.format(Locale.US, "%.2f", goal);
            progressMax = goal.intValue();
            progress = progressDouble.intValue();
        }
        if (goalName.contains(getString(R.string.activity_steps_camel_case))) {
            if (goal.longValue() > 1) {
                tracker = tracker + " " + getString(R.string.unit_step_plur);
            } else {
                tracker = tracker + " " + getString(R.string.unit_step_sing);
            }
        } else if (goalName.contains("Minutes") || goalName.contains("sleep")) {
            if (goal.longValue() > 1) {
                tracker = tracker + " " + getString(R.string.unit_time_plur);
            } else {
                tracker = tracker + " " + getString(R.string.unit_time_sing);
            }
        } else if (goalName.contains(getString(R.string.humidity_camel_case))) {
            tracker = tracker + " " + getString(R.string.unit_percent);
        } else if (goalName.contains(getString(R.string.temperature_camel_case))) {
            tracker = tracker + " " + getString(R.string.unit_temp_si);
        } else if (goalName.contains(getString(R.string.precipitation_camel_case))) {
            tracker = tracker + " " + getString(R.string.unit_precip_si);
        } else if (goalName.contains(getString(R.string.activity_distance_camel_case))) {
            tracker = tracker + " " + getString(R.string.unit_distance_si);
        } else if (goalName.contains(getString(R.string.activity_calories_camel_case))) {
            tracker = tracker + " Cal";
        } else if (goalName.contains(getString(R.string.activity_floors_camel_case))) {
            if (goal.longValue() > 1) {
                tracker = tracker + " " + getString(R.string.unit_floor_plur);
            } else {
                tracker = tracker + " " + getString(R.string.unit_floor_sing);
            }
        } else if (goalType.contains(getString(R.string.timer_camel_case))) {
            if (goal.longValue() > 1) {
                tracker = tracker + " " + getString(R.string.unit_time_plur);
            } else {
                tracker = tracker + " " + getString(R.string.unit_time_sing);
            }
        } else if (goalType.contains(getString(R.string.phone_usage_camel_case))) {
            if (goal.longValue() > 1) {
                tracker = tracker + " " + getString(R.string.unit_time_plur);
            } else {
                tracker = tracker + " " + getString(R.string.unit_time_sing);
            }
        } else if (goalType.contains(getString(R.string.baactivity_camel_case))) {
            if (goal.longValue() > 1) {
                tracker = tracker + " " + getString(R.string.unit_activity_plur);
            } else {
                tracker = tracker + " " + getString(R.string.unit_activity_sing);
            }
        }
        Log.i(TAG, tracker);
        String title = goalName.substring(0, 1).toUpperCase() + goalName.substring(1);
        if (title.contains("Minutes")) {
            int index = title.indexOf("Minutes");
            title = title.substring(0, index) + " " + title.substring(index);
        }
        builder = new NotificationCompat.Builder(this, getApplicationContext().getString(R.string.USER_GOAL_NOTIF_CHANNEL_ID))
                .setSmallIcon(R.drawable.ic_logo_monogram_white)
                .setContentTitle(title)
                .setContentText(tracker)
                .setTicker(NOTIFICATION_CHANNEL_DESC)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(progressMax, progress, false)
                .setColor(getColor(R.color.colorPrimary))
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(getApplicationContext().getString(R.string.USER_GOAL_NOTIF_CHANNEL_ID),
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (goalType.equals(getString(R.string.moabi_tracker_camel_case))) {
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
                        distance = steps * 0.762;
                        long timeInMillis = (new Date()).getTime()
                                + (event.timestamp - System.nanoTime()) / 1000000L;
                        Log.i(TAG, "Last: " + timeOfLastEntry + ", Now: " + timeInMillis);
                        if (timeOfLastEntry != 0) {
                            if (timeInMillis - timeOfLastEntry < 5000) {
                                activeMins += timeInMillis - timeOfLastEntry;
                            }
                        } else {
                            activeMins = 0;
                        }
                        double elapsedTime = formattedTime.getCurrentTimeInMilliSecs() - formattedTime.getStartOfDay(formattedTime.getCurrentDateAsYYYYMMDD());
                        calories = (double) steps / 1000 * 40 + 1600 * (elapsedTime / 86400000);
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showMoabiNotification();
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void showMoabiNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        String tracker = "";
        int progressMax = 0;
        int progress = 0;
        if (goalName.contains(getString(R.string.activity_steps_camel_case))) {
            tracker = steps + " / " + goal.longValue() + " ";
            if (goal.longValue() > 1) {
                tracker = tracker + getString(R.string.unit_step_plur);
            } else {
                tracker = tracker + getString(R.string.unit_step_sing);
            }
            progressMax = goal.intValue();
            progress = (int) steps;
        }
        else if (goalName.contains(getString(R.string.activity_active_minutes_camel_case))) {
            tracker = TimeUnit.MILLISECONDS.toMinutes(activeMins) + " / " + goal.longValue() + " ";
            if (goal.longValue() > 1) {
                tracker = tracker + getString(R.string.unit_time_plur);
            } else {
                tracker = tracker + getString(R.string.unit_time_sing);
            }
            progressMax = goal.intValue();
            progress = (int) TimeUnit.MILLISECONDS.toMinutes(activeMins);
        }
        else if (goalName.contains(getString(R.string.activity_sedentary_minutes_camel_case))) {
            tracker = TimeUnit.MILLISECONDS.toMinutes(sedentaryMins) + " / " + goal.longValue() + " ";
            if (goal.longValue() > 1) {
                tracker = tracker + getString(R.string.unit_time_plur);
            } else {
                tracker = tracker + getString(R.string.unit_time_sing);
            }
            progressMax = goal.intValue();
            progress = (int) TimeUnit.MILLISECONDS.toMinutes(sedentaryMins);
        }
        else if (goalName.contains(getString(R.string.activity_distance_camel_case))) {
            tracker = String.format(Locale.US, "%.2f", distance / 1000) + " / " + String.format(Locale.US, "%.2f", goal) + " ";
            if (goal.longValue() > 1) {
                tracker = tracker + getString(R.string.unit_distance_si);
            } else {
                tracker = tracker + getString(R.string.unit_distance_si);
            }
            progressMax = goal.intValue();
            progress = (int) distance / 1000;
        }
        else if (goalType.contains(getString(R.string.activity_calories_camel_case))) {
            tracker =  calories + " / " + goal.longValue() + " " + getString(R.string.unit_calories);
            progressMax = goal.intValue();
            progress = (int) calories;
        }
        String title = goalName.substring(0, 1).toUpperCase() + goalName.substring(1);
        if (title.contains("Minutes")) {
            int index = title.indexOf("Minutes");
            title = title.substring(0, index) + " " + title.substring(index);
        }
        Log.i(TAG, tracker);
        builder = new NotificationCompat.Builder(this, getApplicationContext().getString(R.string.USER_GOAL_NOTIF_CHANNEL_ID))
                .setSmallIcon(R.drawable.ic_logo_monogram_white)
                .setContentTitle(title)
                .setContentText(tracker)
                .setTicker(NOTIFICATION_CHANNEL_DESC)
                .setOngoing(true)
                .setProgress(progressMax, progress, false)
                .setColor(getColor(R.color.colorPrimary))
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(getApplicationContext().getString(R.string.USER_GOAL_NOTIF_CHANNEL_ID),
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(NOTIFICATION_ID, notification);
    }
}
