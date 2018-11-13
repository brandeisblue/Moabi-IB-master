package com.ivorybridge.moabi.service;

import android.app.Application;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AndroidJobCreator implements JobCreator {

    private Application application;

    public AndroidJobCreator(Application application) {
        this.application = application;
    }

    @Override
    @Nullable
    public Job create(@NonNull String tag) {
        switch (tag) {
            case AsyncCallsForTodayJob.TAG:
                return new AsyncCallsForTodayJob(application);
            case AsyncCallsForYesterdayJob.TAG:
                return new AsyncCallsForYesterdayJob(application);
            case MoodAndEnergyInsightCalculatorDailyJob.TAG:
                return new MoodAndEnergyInsightCalculatorDailyJob(application);
            case BodyInsightCalculatorDailyJob.TAG:
                return new BodyInsightCalculatorDailyJob(application);
            case MoodAndEnergyInsightCalculatorJob.TAG:
                return new MoodAndEnergyInsightCalculatorJob(application);
            case StressInsightCalculatorJob.TAG:
                return new StressInsightCalculatorJob(application);
            case StressInsightCalculatorDailyJob.TAG:
                return new StressInsightCalculatorDailyJob(application);
            case DailyReviewInsightCalculatorJob.TAG:
                return new DailyReviewInsightCalculatorJob(application);
            case DailyReviewInsightCalculatorDailyJob.TAG:
                return new DailyReviewInsightCalculatorDailyJob(application);
            case UserGoalJob.TAG:
                return new UserGoalJob(application);
            case UserGoalPeriodicJob.TAG:
                return new UserGoalPeriodicJob(application);
            case DepressionInsightCalculatorJob.TAG:
                return new DepressionInsightCalculatorJob(application);
            case AnxietyInsightCalculatorJob.TAG:
                return new AnxietyInsightCalculatorJob(application);
            case MotionSensorResetDailyJob.TAG:
                return new MotionSensorResetDailyJob(application);
            default:
                return null;
        }
    }
}
