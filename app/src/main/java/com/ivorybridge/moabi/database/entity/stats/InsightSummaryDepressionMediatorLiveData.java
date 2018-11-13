package com.ivorybridge.moabi.database.entity.stats;

import com.ivorybridge.moabi.database.entity.depression.DailyPhq9;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class InsightSummaryDepressionMediatorLiveData extends MediatorLiveData<Pair<List<DailyPhq9>, List<SimpleRegressionSummary>>> {

    public InsightSummaryDepressionMediatorLiveData(LiveData<List<DailyPhq9>> dailyPhq9s, LiveData<List<SimpleRegressionSummary>> simpleRegressionSummaryList) {
        addSource(dailyPhq9s, new Observer<List<DailyPhq9>>() {
            @Override
            public void onChanged(@Nullable List<DailyPhq9> dailyPhq9List) {
                setValue(Pair.create(dailyPhq9List, simpleRegressionSummaryList.getValue()));
            }
        });
        addSource(simpleRegressionSummaryList, new Observer<List<SimpleRegressionSummary>>() {
            @Override
            public void onChanged(@Nullable List<SimpleRegressionSummary> simpleRegressionSummaries) {
                setValue(Pair.create(dailyPhq9s.getValue(), simpleRegressionSummaries));
            }
        });
    }
}