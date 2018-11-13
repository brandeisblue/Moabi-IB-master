package com.ivorybridge.moabi.database.entity.stats;


import com.ivorybridge.moabi.database.entity.anxiety.DailyGad7;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class InsightSummaryAnxietyMediatorLiveData extends MediatorLiveData<Pair<List<DailyGad7>, List<SimpleRegressionSummary>>> {

    public InsightSummaryAnxietyMediatorLiveData(LiveData<List<DailyGad7>> dailyGad7s, LiveData<List<SimpleRegressionSummary>> simpleRegressionSummaryList) {
        addSource(dailyGad7s, new Observer<List<DailyGad7>>() {
            @Override
            public void onChanged(@Nullable List<DailyGad7> dailyGad7List) {
                setValue(Pair.create(dailyGad7List, simpleRegressionSummaryList.getValue()));
            }
        });
        addSource(simpleRegressionSummaryList, new Observer<List<SimpleRegressionSummary>>() {
            @Override
            public void onChanged(@Nullable List<SimpleRegressionSummary> simpleRegressionSummaries) {
                setValue(Pair.create(dailyGad7s.getValue(), simpleRegressionSummaries));
            }
        });
    }
}
