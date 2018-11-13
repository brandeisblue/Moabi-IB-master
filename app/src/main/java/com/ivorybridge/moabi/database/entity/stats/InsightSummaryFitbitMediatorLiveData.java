package com.ivorybridge.moabi.database.entity.stats;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;

import java.util.List;

public class InsightSummaryFitbitMediatorLiveData extends MediatorLiveData<Pair<List<FitbitDailySummary>, List<SimpleRegressionSummary>>> {

    public InsightSummaryFitbitMediatorLiveData(LiveData<List<FitbitDailySummary>> fitbitDailySummaries, LiveData<List<SimpleRegressionSummary>> simpleRegressionSummaryList) {
        addSource(fitbitDailySummaries, new Observer<List<FitbitDailySummary>>() {
            @Override
            public void onChanged(@Nullable List<FitbitDailySummary> dailyEnergyList) {
                setValue(Pair.create(dailyEnergyList, simpleRegressionSummaryList.getValue()));
            }
        });
        addSource(simpleRegressionSummaryList, new Observer<List<SimpleRegressionSummary>>() {
            @Override
            public void onChanged(@Nullable List<SimpleRegressionSummary> simpleRegressionSummaries) {
                setValue(Pair.create(fitbitDailySummaries.getValue(), simpleRegressionSummaries));
            }
        });
    }
}
