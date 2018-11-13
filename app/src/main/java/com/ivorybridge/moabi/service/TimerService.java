package com.ivorybridge.moabi.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.DataInUseRepository;
import com.ivorybridge.moabi.repository.TimedActivityRepository;
import com.ivorybridge.moabi.ui.activity.TimerActivity;
import com.ivorybridge.moabi.util.FormattedTime;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

/**
 * Service for Timer.
 * It is implemented as a foreground service so that it will be rarely killed even if resources are scarce.
 */
public class TimerService extends Service implements PropertyChangeListener {

    private static final String TAG = TimerService.class.getSimpleName();
    private static final String ACTION_CHANGESTATE = "com.ivorybridge.moabi.service.timerservice.action_changestate";
    private static final String ACTION_RESET = "com.ivorybridge.moabi.service.timerservice.action_reset";
    private static final String ACTION_EXIT = "com.ivorybridge.moabi.service.timerservice.action_exit";
    private static final String ACTION_SAVE = "com.ivorybridge.moabi.service.timerservice.action_save";
    private static final String ACTION_STOP = "com.ivorybridge.moabi.service.timerservice.action_stop";
    private static final int NOTIFICATION_ID = 590123562;
    private static final String NOTIFICATION_CHANNEL_NAME = "TIMER_SERVICE";
    private static final String NOTIFICATION_CHANNEL_DESC = "TIMED_ACTIVITY_TRACKING";
    private static final int CHANNEL_ID = 34123512;
    private NotificationManager notificationManager;
    private boolean isNotificationShowing;
    private BroadcastReceiver recieverStateChange;
    private BroadcastReceiver recieverReset;
    private BroadcastReceiver recieverExit;
    private BroadcastReceiver receiverSave;
    private BroadcastReceiver receiverStop;
    private FormattedTime formattedTime;
    private Timer timer;
    private TimedActivityRepository timedActivityRepository;
    private DataInUseRepository dataInUseRepository;
    private FirebaseManager firebaseManager;
    private PendingIntent changeStatePendingIntent;
    private PendingIntent resetPendingIntent;
    private PendingIntent stopPendingIntent;
    private PendingIntent exitPendingIntent;
    private PendingIntent savePendingIntent;
    private Notification customNotification;
    private SharedPreferences timedActivitySharedPreference;
    private SharedPreferences notificationSharedPreferences;
    private SharedPreferences.Editor timedActivitySPEditor;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isNotificationShowing = false;
        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        timedActivityRepository = new TimedActivityRepository(getApplication());
        dataInUseRepository = new DataInUseRepository(getApplication());
        timedActivitySharedPreference = getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_TIMED_ACTIVITY_SHARED_PREFERENCE_KEY),
                Context.MODE_PRIVATE);
        notificationSharedPreferences = getSharedPreferences(
                getString(R.string.com_ivorybridge_mobai_NOTIFICATION_SHARED_PREFERENCE),
                Context.MODE_PRIVATE);
        timedActivitySPEditor = timedActivitySharedPreference.edit();
        formattedTime = new FormattedTime();
        firebaseManager = new FirebaseManager();
        IntentFilter filterPlay = new IntentFilter(ACTION_CHANGESTATE);
        recieverStateChange = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (TimeContainer.getInstance().getCurrentState() == TimeContainer.STATE_RUNNING) {
                    TimeContainer.getInstance().pause();
                } else {
                    TimeContainer.getInstance().start();
                }
                updateNotification();
            }
        };
        registerReceiver(recieverStateChange, filterPlay);

        recieverReset = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //TimeContainer.getInstance().pause();
                //TimeContainer.getInstance().reset();
                TimeContainer.getInstance().stopAndReset();
                timedActivitySPEditor.putString("current_activity", null);
                timedActivitySPEditor.commit();
                updateNotification();
            }
        };
        IntentFilter filterReset = new IntentFilter(ACTION_RESET);
        registerReceiver(recieverReset, filterReset);

        receiverStop = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TimeContainer.getInstance().stopAndReset();
                updateNotification();
            }
        };
        IntentFilter filterStop = new IntentFilter(ACTION_STOP);
        registerReceiver(receiverStop, filterStop);

        IntentFilter filterExit = new IntentFilter(ACTION_EXIT);
        recieverExit = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TimeContainer.getInstance().stopAndReset();
                stopForeground(true);
                isNotificationShowing = false;
                stopSelf();
                notificationManager.cancel(NOTIFICATION_ID);
            }
        };
        registerReceiver(recieverExit, filterExit);
        IntentFilter filterSave = new IntentFilter(ACTION_SAVE);
        receiverSave = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                InputInUse inputInUse = new InputInUse();
                inputInUse.setName(getString(R.string.timer_camel_case));
                inputInUse.setType("tracker");
                inputInUse.setInUse(true);
                dataInUseRepository.insert(inputInUse);
                TimedActivitySummary timedActivitySummary = new TimedActivitySummary();
                timedActivitySummary.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                String name = timedActivitySharedPreference.getString("current_activity", null);
                if (name == null || name.isEmpty()) {
                    name = "Unknown";
                }
                timedActivitySummary.setInputName(name);
                timedActivitySummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                timedActivitySummary.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                timedActivitySummary.setDuration(TimeContainer.getInstance().getElapsedTime());
                timedActivityRepository.insert(timedActivitySummary, formattedTime.getCurrentDateAsYYYYMMDD());
                TimerService.TimeContainer.getInstance().stopAndReset();
                firebaseManager.getUserInputsRef().child(getString(R.string.timer_camel_case))
                        .child(formattedTime.getCurrentDateAsYYYYMMDD())
                        .child(formattedTime.convertLongToHHMM(timedActivitySummary.getDateInLong()))
                        .child(name).setValue(timedActivitySummary.getDuration());
                firebaseManager.getDaysWithDataTodayRef().child(getString(R.string.timer_camel_case)).setValue(true);
            }
        };
        registerReceiver(receiverSave, filterSave);
        startUpdateTimer();
        TimeContainer.getInstance().isServiceRunning.set(true);

        Intent changeStateIntent = new Intent(ACTION_CHANGESTATE, null);
        changeStatePendingIntent = PendingIntent.getBroadcast(this, 0, changeStateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent resetIntent = new Intent(ACTION_RESET, null);
        resetPendingIntent = PendingIntent.getBroadcast(this, 0, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent stopIntent = new Intent(ACTION_STOP, null);
        stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent exitIntent = new Intent(ACTION_EXIT, null);
        exitPendingIntent = PendingIntent.getBroadcast(this, 0, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent saveIntent = new Intent(ACTION_SAVE, null);
        savePendingIntent = PendingIntent.getBroadcast(this, 0, saveIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //TODO - figure out what the second button will actually do. Now, it's just a placeholder.

        return START_STICKY;
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }


    public void startUpdateTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (notificationSharedPreferences.getBoolean(getString(R.string.preference_timer_notification), false)) {
                    updateNotification();
                }
            }
        }, 0, 1000);
    }

    private synchronized void updateNotification() {

        String name = timedActivitySharedPreference.getString("current_activity", null);
        if (name == null || name.isEmpty()) {
            name = "Unknown";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getApplicationContext().getString(R.string.CHRONOMETER_NOTIF_CHANNEL_ID))
                .setSmallIcon(R.drawable.ic_logo_monogram_white)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setOnlyAlertOnce(true);

        Intent notificationIntent = new Intent(this, TimerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, PendingIntent.FLAG_UPDATE_CURRENT, notificationIntent, 0);
        //PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(intent);

        if (TimeContainer.getInstance().getCurrentState() == TimeContainer.STATE_RUNNING) {
            customNotification = builder
                    .setSmallIcon(R.drawable.ic_logo_monogram_white)
                    .setContentTitle(name)
                    .setContentText(msToHourMinSec(TimeContainer.getInstance().getElapsedTime()))
                    .addAction(R.drawable.ic_reset, getString(R.string.reset_title), resetPendingIntent)
                    .addAction(R.drawable.ic_stop_black, getString(R.string.stop_title), stopPendingIntent)
                    .addAction(R.drawable.ic_pause_black, getString(R.string.pause_title), changeStatePendingIntent)
                    .addAction(R.drawable.ic_save, getString(R.string.save_title), savePendingIntent)
                    .addAction(R.drawable.ic_cancel, getString(R.string.exit_title), exitPendingIntent)
                    .setDeleteIntent(exitPendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setWhen(TimeContainer.getInstance().getStartTime())
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowCancelButton(true)
                            .setCancelButtonIntent(exitPendingIntent)
                            .setShowActionsInCompactView(1, 2, 3))
                    .build();
        } else {
            customNotification = builder
                    .setSmallIcon(R.drawable.ic_logo_monogram_white)
                    .setContentTitle(name)
                    .setContentText(msToHourMinSec(TimeContainer.getInstance().getElapsedTime()))
                    .addAction(R.drawable.ic_reset, getString(R.string.reset_title), resetPendingIntent)
                    .addAction(R.drawable.ic_stop_black, getString(R.string.stop_title), stopPendingIntent)
                    .addAction(R.drawable.ic_pause_black, getString(R.string.pause_title), changeStatePendingIntent)
                    .addAction(R.drawable.ic_save, getString(R.string.save_title), savePendingIntent)
                    .addAction(R.drawable.ic_cancel, getString(R.string.exit_title), exitPendingIntent)
                    .setDeleteIntent(exitPendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setWhen(TimeContainer.getInstance().getStartTime())
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowCancelButton(true)
                            .setCancelButtonIntent(exitPendingIntent)
                            .setShowActionsInCompactView(1, 2, 3))
                    .build();
        }
       //this.startForeground(NOTIFICATION_ID, customNotification);
        if(Build.VERSION.SDK_INT>=26) {
            NotificationChannel channel = new NotificationChannel(getApplicationContext().getString(R.string.CHRONOMETER_NOTIF_CHANNEL_ID),
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            notificationManager.createNotificationChannel(channel);
        }
        if (isNotificationShowing) {
            notificationManager.notify(NOTIFICATION_ID, customNotification);
        } else {
            startForeground(NOTIFICATION_ID, customNotification);
            isNotificationShowing = true;
            notificationManager.notify(NOTIFICATION_ID, customNotification);
        }
        //startForeground(NOTIFICATION_ID, customNotification);

    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        unregisterReceiver(recieverExit);
        unregisterReceiver(recieverReset);
        unregisterReceiver(recieverStateChange);
        TimeContainer.getInstance().isServiceRunning.set(false);
        notificationManager.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class TimeContainer {

        public static final int STATE_STOPPED = 0;
        public static final int STATE_PAUSED = 1;
        public static final int STATE_RUNNING = 2;

        private static TimeContainer instance;
        public AtomicBoolean isServiceRunning;
        private PropertyChangeSupport observers;
        private String name;

        public static final String STATE_CHANGED = "state_changed";

        private int currentState;
        private long startTime;
        private long elapsedTime;

        private final Object mSynchronizedObject = new Object();

        private TimeContainer() {
            isServiceRunning = new AtomicBoolean(false);
            observers = new PropertyChangeSupport(this);
        }

        public void addObserver(PropertyChangeListener listener) {
            observers.addPropertyChangeListener(listener);
        }

        public void removeObserver(PropertyChangeListener listener) {
            observers.removePropertyChangeListener(listener);
        }

        public static TimeContainer getInstance() {
            if (instance == null) {
                instance = new TimeContainer();
            }
            return instance;
        }

        public void notifyStateChanged() {
            observers.firePropertyChange(STATE_CHANGED, null, currentState);
        }

        public int getCurrentState() {
            return currentState;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getElapsedTime() {
            if (startTime == 0) {
                return elapsedTime;
            } else {
                return elapsedTime + (System.currentTimeMillis() - startTime);
            }
        }

        public void start() {
            synchronized (mSynchronizedObject) {
                startTime = System.currentTimeMillis();
                currentState = STATE_RUNNING;
                notifyStateChanged();
            }
        }

        public void reset() {
            synchronized (mSynchronizedObject) {
                if (currentState == STATE_RUNNING) {
                    startTime = System.currentTimeMillis();
                    elapsedTime = 0;
                    currentState = STATE_PAUSED;
                    notifyStateChanged();
                } else {
                    startTime = 0;
                    elapsedTime = 0;
                    currentState = STATE_STOPPED;
                    notifyStateChanged();
                }
            }
        }

        public void stopAndReset() {
            synchronized (mSynchronizedObject) {
                startTime = 0;
                elapsedTime = 0;
                currentState = STATE_STOPPED;
                notifyStateChanged();
            }
        }

        public void pause() {
            synchronized (mSynchronizedObject) {
                elapsedTime = elapsedTime + (System.currentTimeMillis() - startTime);
                startTime = 0;
                currentState = STATE_PAUSED;
                notifyStateChanged();
            }
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    private String msToHourMinSec(long ms) {
        if (ms == 0) {
            return "00:00";
        } else {
            long seconds = (ms / 1000) % 60;
            long minutes = (ms / 1000) / 60;
            long hours = minutes / 60;

            StringBuilder sb = new StringBuilder();
            if (hours > 0) {
                sb.append(hours);
                sb.append(':');
            }
            if (minutes > 0) {
                minutes = minutes % 60;
                if (minutes >= 10) {
                    sb.append(minutes);
                } else {
                    sb.append(0);
                    sb.append(minutes);
                }
            } else {
                sb.append('0');
                sb.append('0');
            }
            sb.append(':');
            if (seconds > 0) {
                if (seconds >= 10) {
                    sb.append(seconds);
                } else {
                    sb.append(0);
                    sb.append(seconds);
                }
            } else {
                sb.append('0');
                sb.append('0');
            }
            return sb.toString();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName() == TimeContainer.STATE_CHANGED) {
            startUpdateTimer();
        }
    }

}
