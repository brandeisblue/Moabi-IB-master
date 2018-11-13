package com.ivorybridge.moabi.database.entity.stats;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.ivorybridge.moabi.database.entity.moodandenergy.DailyEnergy;

import java.util.List;

public class InsightSummaryEnergyMediatorLiveData extends MediatorLiveData<Pair<List<DailyEnergy>, List<SimpleRegressionSummary>>> {

    public InsightSummaryEnergyMediatorLiveData(LiveData<List<DailyEnergy>> dailyEnergys, LiveData<List<SimpleRegressionSummary>> simpleRegressionSummaryList) {
        addSource(dailyEnergys, new Observer<List<DailyEnergy>>() {
            @Override
            public void onChanged(@Nullable List<DailyEnergy> dailyEnergyList) {
                setValue(Pair.create(dailyEnergyList, simpleRegressionSummaryList.getValue()));
            }
        });
        addSource(simpleRegressionSummaryList, new Observer<List<SimpleRegressionSummary>>() {
            @Override
            public void onChanged(@Nullable List<SimpleRegressionSummary> simpleRegressionSummaries) {
                setValue(Pair.create(dailyEnergys.getValue(), simpleRegressionSummaries));
            }
        });
    }
}