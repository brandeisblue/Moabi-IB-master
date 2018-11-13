package com.ivorybridge.moabi.database.entity.stats;



import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class InsightSummaryPhoneUsageMediatorLiveData extends MediatorLiveData<Pair<List<AppUsageSummary>, List<SimpleRegressionSummary>>> {

    public InsightSummaryPhoneUsageMediatorLiveData(LiveData<List<AppUsageSummary>> dailyPhoneUsages, LiveData<List<SimpleRegressionSummary>> simpleRegressionSummaryList) {
        addSource(dailyPhoneUsages, new Observer<List<AppUsageSummary>>() {
            @Override
            public void onChanged(@Nullable List<AppUsageSummary> dailyPhoneUsageList) {
                setValue(Pair.create(dailyPhoneUsageList, simpleRegressionSummaryList.getValue()));
            }
        });
        addSource(simpleRegressionSummaryList, new Observer<List<SimpleRegressionSummary>>() {
            @Override
            public void onChanged(@Nullable List<SimpleRegressionSummary> simpleRegressionSummaries) {
                setValue(Pair.create(dailyPhoneUsages.getValue(), simpleRegressionSummaries));
            }
        });
    }
}
