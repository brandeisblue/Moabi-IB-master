package com.ivorybridge.moabi.service;

import android.app.Application;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.ivorybridge.moabi.repository.AsyncCallsMasterRepository;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class AsyncCallsForTodayJob extends Job {

    public static final String TAG = "async_calls_today_job";
    private FormattedTime formattedTime;
    private Application application;

    public AsyncCallsForTodayJob(Application application) {
        this.application = application;
        formattedTime = new FormattedTime();
    }

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        // run your job here
        formattedTime = new FormattedTime();
        AsyncCallsMasterRepository asyncCallsMasterRepository = new AsyncCallsMasterRepository(application, formattedTime.getCurrentDateAsYYYYMMDD());
        asyncCallsMasterRepository.makeCallsToConnectedServices();

        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        new JobRequest.Builder(AsyncCallsForTodayJob.TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(5))
                .setUpdateCurrent(true)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .build()
                .schedule();
    }
}
