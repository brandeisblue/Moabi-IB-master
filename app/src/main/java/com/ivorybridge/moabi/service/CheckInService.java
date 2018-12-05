package com.ivorybridge.moabi.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.activity.MakeEntryActivity;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class CheckInService extends Service {

    private static final String TAG = CheckInService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 109293841;
    private static final String NOTIFICATION_CHANNEL_NAME = "Check-in";
    private static final String NOTIFICATION_CHANNEL_DESC = "";
    private NotificationManager notificationManager;

    private SharedPreferences notificationSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationSharedPreferences = getSharedPreferences(
                getString(R.string.com_ivorybridge_mobai_NOTIFICATION_SHARED_PREFERENCE),
                Context.MODE_PRIVATE);
        boolean showNotif = notificationSharedPreferences.getBoolean(getString(R.string.preference_daily_check_in_notification),
                true);
        if (showNotif) {
            showNotification();
            return START_NOT_STICKY;
        } else {
            stopForeground(true);
            return START_NOT_STICKY;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MakeEntryActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getApplicationContext().getString(R.string.CHECK_IN_NOTIF_CHANNEL_ID));builder.setSmallIcon(R.drawable.ic_monogram_white)
                .setContentTitle(getString(R.string.notif_check_in_title))
                .setContentText(getString(R.string.notif_check_in_description))
                .setTicker(NOTIFICATION_CHANNEL_DESC)
                .setAutoCancel(true)
                .setLights(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), 500, 2000)
                .setColor(getColor(R.color.colorPrimary))
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(getApplicationContext().getString(R.string.CHECK_IN_NOTIF_CHANNEL_ID),
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(NOTIFICATION_ID, notification);
        stopForeground(false);
    }
}
