package com.ivorybridge.moabi.database.entity.stats;

import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class InsightSummaryActivityMediatorLiveData extends MediatorLiveData<Pair<List<BAActivityEntry>, List<SimpleRegressionSummary>>> {

    public InsightSummaryActivityMediatorLiveData(LiveData<List<BAActivityEntry>> dailyActivities, LiveData<List<SimpleRegressionSummary>> simpleRegressionSummaryList) {
        addSource(dailyActivities, new Observer<List<BAActivityEntry>>() {
            @Override
            public void onChanged(@Nullable List<BAActivityEntry> dailyActivityList) {
                setValue(Pair.create(dailyActivityList, simpleRegressionSummaryList.getValue()));
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
