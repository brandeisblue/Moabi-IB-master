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
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInProfile;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.repository.DataInUseRepository;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;

public class MotionSensorService extends Service implements SensorEventListener {

    private static final String TAG = MotionSensorService.class.getSimpleName();
    private SensorManager sensorManager;
    // Continuous. possibly for distance.
    // Output of the accelerometer minus the output of the gravity sensor.
    // Reported in m/s^2 in the x, y and z fields of sensors_event_t.acceleration
    private Sensor linearAccelSensor;
    private Sensor accelSensor;
    private Sensor pickUpSensor;
    private Sensor gravitySensor;
    private Sensor gyroSensor;
    private Sensor motionSensor;
    private Sensor stationarySensor;
    // Triggers when the detecting a “significant motion”: a motion that might lead to a change in the user location.
    // Used to reduce the power consumption of location determination.
    // Each sensor event reports 1 in sensors_event_t.data[0]
    private Sensor sigMotionSensor;
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
    private DataInUseRepository dataInUseRepository;
    private BuiltInActivitySummary activitySummary;
    private BuiltInProfile profile;
    private NotificationCompat.Builder builder;
    private SharedPreferences notificationSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        formattedTime = new FormattedTime();
        builtInFitnessRepository = new BuiltInFitnessRepository(getApplication());
        dataInUseRepository = new DataInUseRepository(getApplication());
        notificationSharedPreferences = getSharedPreferences(
                getString(R.string.com_ivorybridge_mobai_NOTIFICATION_SHARED_PREFERENCE),
                Context.MODE_PRIVATE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        builder = new NotificationCompat.Builder(MotionSensorService.this, getApplicationContext().getString(R.string.MOTION_SENSOR_NOTIF_CHANNEL_ID))
                .setSmallIcon(R.drawable.ic_logo_monogram_white)
                .setContentTitle("Today: " + steps + " steps")
                .setContentText("Today: " + String.format(Locale.US, "%.2f", distance / 1000) + " km, " + TimeUnit.MILLISECONDS.toMinutes(activeMins) + " mins, " + TimeUnit.MILLISECONDS.toMinutes(sedentaryMins) + " mins, " + calories + " Cal")
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
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(NOTIFICATION_ID, notification);
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
                    distance = steps * 0.762;
                    long timeNow = (new Date()).getTime()
                            + (event.timestamp - System.nanoTime()) / 1000000L;
                    Log.i(TAG, "Last: " + timeOfLastEntry + ", Now: " + timeNow);
                    if (timeOfLastEntry != 0) {
                        if (timeNow - timeOfLastEntry < 5000) {
                            activeMins += timeNow - timeOfLastEntry;
                        }
                    } else {
                        activeMins = 0;
                    }
                    double elapsedTime = formattedTime.getCurrentTimeInMilliSecs() - formattedTime.getStartOfDay(formattedTime.getCurrentDateAsYYYYMMDD());
                    calories = (double) steps / 1000 * 40 + 1600 * (elapsedTime / 86400000);
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
                        ignore = (countdown < 0) ? false : ignore;
                    } else
                        countdown = 22;
                    if ((Math.abs(prevY - gravity[1]) > threshold) && !ignore) {
                        steps++;
                        ignore = true;
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
