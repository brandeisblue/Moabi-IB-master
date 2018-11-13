package com.ivorybridge.moabi.database.entity.stats;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.ivorybridge.moabi.database.entity.moodandenergy.DailyMood;

import java.util.List;

public class InsightSummaryMoodMediatorLiveData extends MediatorLiveData<Pair<List<DailyMood>, List<SimpleRegressionSummary>>> {

    public InsightSummaryMoodMediatorLiveData(LiveData<List<DailyMood>> dailyMoods, LiveData<List<SimpleRegressionSummary>> simpleRegressionSummaryList) {
        addSource(dailyMoods, new Observer<List<DailyMood>>() {
            @Override
            public void onChanged(@Nullable List<DailyMood> dailyMoodList) {
                setValue(Pair.create(dailyMoodList, simpleRegressionSummaryList.getValue()));
            }
        });
        addSource(simpleRegressionSummaryList, new Observer<List<SimpleRegressionSummary>>() {
            @Override
            public void onChanged(@Nullable List<SimpleRegressionSummary> simpleRegressionSummaries) {
                setValue(Pair.create(dailyMoods.getValue(), simpleRegressionSummaries));
            }
        });
    }
}