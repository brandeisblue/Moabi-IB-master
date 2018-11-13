package com.ivorybridge.moabi.database.entity.stats;


import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class InsightSummaryBuiltInFitnessMediatorLiveData extends MediatorLiveData<Pair<List<BuiltInActivitySummary>, List<SimpleRegressionSummary>>> {

    public InsightSummaryBuiltInFitnessMediatorLiveData(LiveData<List<BuiltInActivitySummary>> summaries, LiveData<List<SimpleRegressionSummary>> simpleRegressionSummaryList) {
        addSource(summaries, new Observer<List<BuiltInActivitySummary>>() {
            @Override
            public void onChanged(@Nullable List<BuiltInActivitySummary> dailyEnergyList) {
                setValue(Pair.create(dailyEnergyList, simpleRegressionSummaryList.getValue()));
            }
        });
        addSource(simpleRegressionSummaryList, new Observer<List<SimpleRegressionSummary>>() {
            @Override
            public void onChanged(@Nullable List<SimpleRegressionSummary> simpleRegressionSummaries) {
                setValue(Pair.create(summaries.getValue(), simpleRegressionSummaries));
            }
        });
    }
}
