package com.ivorybridge.moabi.database.entity.stats;


import com.ivorybridge.moabi.database.entity.stress.DailyStress;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class InsightSummaryStressMediatorLiveData extends MediatorLiveData<Pair<List<DailyStress>, List<SimpleRegressionSummary>>> {

    public InsightSummaryStressMediatorLiveData(LiveData<List<DailyStress>> dailyStresss, LiveData<List<SimpleRegressionSummary>> simpleRegressionSummaryList) {
        addSource(dailyStresss, new Observer<List<DailyStress>>() {
            @Override
            public void onChanged(@Nullable List<DailyStress> dailyStressList) {
                setValue(Pair.create(dailyStressList, simpleRegressionSummaryList.getValue()));
            }
        });
        addSource(simpleRegressionSummaryList, new Observer<List<SimpleRegressionSummary>>() {
            @Override
            public void onChanged(@Nullable List<SimpleRegressionSummary> simpleRegressionSummaries) {
                setValue(Pair.create(dailyStresss.getValue(), simpleRegressionSummaries));
            }
        });
    }
}
