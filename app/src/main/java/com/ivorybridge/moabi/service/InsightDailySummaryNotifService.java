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
import com.ivorybridge.moabi.database.entity.stats.SimpleRegressionSummary;
import com.ivorybridge.moabi.repository.statistics.PredictionsRepository;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class InsightDailySummaryNotifService extends Service {

    private static final String TAG = InsightDailySummaryNotifService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 39040918;
    private static final String NOTIFICATION_CHANNEL_NAME = "Daily Insight Summary";
    private static final String NOTIFICATION_CHANNEL_DESC = "";
    private NotificationManager notificationManager;
    private SharedPreferences notificationSharedPreferences;
    private PredictionsRepository predictionsRepository;
    private FormattedTime formattedTime;

    @Override
    public void onCreate() {
        super.onCreate();
        predictionsRepository = new PredictionsRepository(getApplication());
        notificationSharedPreferences = getSharedPreferences(
                getString(R.string.com_ivorybridge_mobai_NOTIFICATION_SHARED_PREFERENCE),
                Context.MODE_PRIVATE);
        formattedTime = new FormattedTime();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean showNotification = notificationSharedPreferences.getBoolean(getString(R.string.preference_new_recommendations_notification), false);
                long startOfWeek = formattedTime.getStartOfWeek(formattedTime.getCurrentDateAsYYYYMMDD());
                long endOfWeek = formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD());
                boolean hasMindSummaries = false;
                boolean hasBodySummaries = false;
                List<SimpleRegressionSummary> mindSummaries =
                        predictionsRepository.getAllMindSummariesNow(startOfWeek, endOfWeek, 7);
                List<SimpleRegressionSummary> bodySummaries =
                        predictionsRepository.getAllBodySummariesNow(startOfWeek, endOfWeek, 7);
                if (mindSummaries != null && mindSummaries.size() > 0) {
                    hasMindSummaries = true;
                }
                if (bodySummaries != null && bodySummaries.size() > 0) {
                    hasBodySummaries = true;
                }
                if (showNotification) {
                    List<SimpleRegressionSummary> sortedList = new ArrayList<>();
                    if (hasMindSummaries && hasBodySummaries) {
                        showNotification();
                        mindSummaries.addAll(bodySummaries);
                        Collections.sort(mindSummaries, new SimpleRegressionSummary.BestFitComparator());
                        for (SimpleRegressionSummary summary : mindSummaries) {
                            if (summary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                if (summary.getIndepVar().equals(getString(R.string.phone_usage_total_title))) {
                                    sortedList.add(summary);
                                } else if (TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) >= 5) {
                                    sortedList.add(summary);
                                }
                            } else if (summary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                if (TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) >= 5) {
                                    sortedList.add(summary);
                                }
                            } else if (summary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                if (TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) >= 1) {
                                    sortedList.add(summary);
                                }
                            } else {
                                sortedList.add(summary);
                            }
                        }
                    } else if (hasMindSummaries) {
                        showNotification();
                        Collections.sort(mindSummaries, new SimpleRegressionSummary.BestFitComparator());
                        for (SimpleRegressionSummary summary : mindSummaries) {
                            if (summary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                if (summary.getIndepVar().equals(getString(R.string.phone_usage_total_title))) {
                                    sortedList.add(summary);
                                } else if (TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) >= 5) {
                                    sortedList.add(summary);
                                }
                            } else if (summary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                if (TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) >= 5) {
                                    sortedList.add(summary);
                                }
                            } else if (summary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                if (TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) >= 1) {
                                    sortedList.add(summary);
                                }
                            } else {
                                sortedList.add(summary);
                            }
                        }
                    } else if (hasBodySummaries) {
                        showNotification();
                        Collections.sort(bodySummaries, new SimpleRegressionSummary.BestFitComparator());
                        for (SimpleRegressionSummary summary : bodySummaries) {
                            if (summary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                if (summary.getIndepVar().equals(getString(R.string.phone_usage_total_title))) {
                                    sortedList.add(summary);
                                } else if (TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) >= 5) {
                                    sortedList.add(summary);
                                }
                            } else if (summary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                if (TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) >= 5) {
                                    sortedList.add(summary);
                                }
                            } else if (summary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                if (TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) >= 1) {
                                    sortedList.add(summary);
                                }
                            } else {
                                sortedList.add(summary);
                            }
                        }
                    }
                }
            }
        }).start();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("redirected_from", "insight_notif");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getApplicationContext().getString(R.string.USER_GOAL_NOTIF_CHANNEL_ID));
        String title = getString(R.string.cheers_good_morning_msg);
        int notifHour = notificationSharedPreferences.getInt(getString(R.string.preference_daily_new_recommendations_hour), 7);
        if (notifHour < 12) {
            title = getString(R.string.cheers_good_morning_msg);
        } else if (notifHour < 17) {
            title = getString(R.string.cheers_good_afternoon_msg);
        } else if (notifHour < 20) {
            title = getString(R.string.cheers_good_evening_msg);
        } else {
            title = getString(R.string.cheers_good_night_msg);
        }
        builder.setSmallIcon(R.drawable.ic_monogram_white)
                .setContentTitle(title)
                .setContentText(getString(R.string.cheers_recommendations_ready_msg))
                .setTicker(NOTIFICATION_CHANNEL_DESC)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(true)
                .setColor(getColor(R.color.colorPrimary))
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(getApplicationContext().getString(R.string.USER_GOAL_NOTIF_CHANNEL_ID),
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(NOTIFICATION_ID, notification);
    }
}
