package com.ivorybridge.moabi.service;

import android.app.Application;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;
import com.ivorybridge.moabi.repository.AsyncCallsMasterRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class AsyncCallsForYesterdayJob extends DailyJob {

    public static final String TAG = "async_calls_yesterday_job";
    private FormattedTime formattedTime;
    private Application application;

    public AsyncCallsForYesterdayJob(Application application) {
        this.application = application;
        formattedTime = new FormattedTime();
    }

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {
        formattedTime = new FormattedTime();
        AsyncCallsMasterRepository asyncCallsMasterRepository = new AsyncCallsMasterRepository(application, formattedTime.getYesterdaysDateAsYYYYMMDD());
        asyncCallsMasterRepository.makeCallsToConnectedServices();
        return DailyJobResult.SUCCESS;
    }

    public static void scheduleJob() {
        DailyJob.schedule(new JobRequest.Builder(TAG),
                TimeUnit.HOURS.toMillis(0),
                TimeUnit.HOURS.toMillis(0) + TimeUnit.MINUTES.toMillis(10));
        /*
        new JobRequest.Builder(BodyInsightCalculatorDailyJob.TAG)
                .startNow()
                //.setPeriodic(TimeUnit.MINUTES.toMillis(30), TimeUnit.MINUTES.toMillis(15))
                //.setRequiresDeviceIdle(true)
                //.setUpdateCurrent(true)
                //.setRequirementsEnforced(true)
                .build()
                .schedule();*/
    }

    /*
    public static void scheduleJob() {
        new JobRequest.Builder(AsyncCallsForTodayJob.TAG)
                .setPeriodic(TimeUnit.HOURS.toMillis(6), TimeUnit.HOURS.toMillis(5))
                .setUpdateCurrent(true)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .build()
                .schedule();
    }*/
}
