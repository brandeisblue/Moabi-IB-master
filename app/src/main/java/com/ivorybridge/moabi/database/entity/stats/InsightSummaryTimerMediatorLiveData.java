package com.ivorybridge.moabi.database.entity.stats;

import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class InsightSummaryTimerMediatorLiveData extends MediatorLiveData<Pair<List<TimedActivitySummary>, List<SimpleRegressionSummary>>> {

    public InsightSummaryTimerMediatorLiveData(LiveData<List<TimedActivitySummary>> dailyActivities, LiveData<List<SimpleRegressionSummary>> simpleRegressionSummaryList) {
        addSource(dailyActivities, new Observer<List<TimedActivitySummary>>() {
            @Override
            public void onChanged(@Nullable List<TimedActivitySummary> dailyTimerList) {
                setValue(Pair.create(dailyTimerList, simpleRegressionSummaryList.getValue()));
            }
        });
        addSource(simpleRegressionSummaryList, new Observer<List<SimpleRegressionSummary>>() {
            @Override
            public void onChanged(@Nullable List<SimpleRegressionSummary> simpleRegressionSummaries) {
                setValue(Pair.create(dailyActivities.getValue(), simpleRegressionSummaries));
            }
        });
    }
}
