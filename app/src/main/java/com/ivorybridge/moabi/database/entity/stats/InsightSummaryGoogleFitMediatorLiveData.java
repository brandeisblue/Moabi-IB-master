package com.ivorybridge.moabi.database.entity.stats;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;


import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;

import java.util.List;

public class InsightSummaryGoogleFitMediatorLiveData extends MediatorLiveData<Pair<List<GoogleFitSummary>, List<SimpleRegressionSummary>>> {

    public InsightSummaryGoogleFitMediatorLiveData(LiveData<List<GoogleFitSummary>> googleFitSummaries, LiveData<List<SimpleRegressionSummary>> simpleRegressionSummaryList) {
        addSource(googleFitSummaries, new Observer<List<GoogleFitSummary>>() {
            @Override
            public void onChanged(@Nullable List<GoogleFitSummary> dailyEnergyList) {
                setValue(Pair.create(dailyEnergyList, simpleRegressionSummaryList.getValue()));
            }
        });
        addSource(simpleRegressionSummaryList, new Observer<List<SimpleRegressionSummary>>() {
            @Override
            public void onChanged(@Nullable List<SimpleRegressionSummary> simpleRegressionSummaries) {
                setValue(Pair.create(googleFitSummaries.getValue(), simpleRegressionSummaries));
            }
        });
    }
}