package com.ivorybridge.moabi.service;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class InsightDailySummaryNotifDailyJob extends DailyJob {

    public static final String TAG = "insight_notif_daily_job";
    private Application application;
    private static int jobId;

    public InsightDailySummaryNotifDailyJob(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Job.Params params) {
        Intent serviceIntent = new Intent(application, InsightDailySummaryNotifService.class);
        application.startService(serviceIntent);
        return DailyJobResult.SUCCESS;
    }

    public static void scheduleJob(int hour, int minute) {
        Log.i(TAG, "Scheduling job at " + hour + ":" + minute);
        DailyJob.schedule(new JobRequest.Builder(TAG),
                TimeUnit.HOURS.toMillis(hour) + TimeUnit.MINUTES.toMillis(minute),
                TimeUnit.HOURS.toMillis(hour) + TimeUnit.MINUTES.toMillis(minute)
                        + TimeUnit.SECONDS.toMillis(30));
    }
}
