package com.ivorybridge.moabi.database.entity.moodandenergy;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import java.util.List;

public class DailyMoodAndEnergyMediatorLiveData extends MediatorLiveData<Pair<List<DailyMood>, List<DailyEnergy>>> {

    public DailyMoodAndEnergyMediatorLiveData(LiveData<List<DailyMood>> dailyMoods, LiveData<List<DailyEnergy>> dailyEnergys) {
        addSource(dailyMoods, new Observer<List<DailyMood>>() {
            @Override
            public void onChanged(@Nullable List<DailyMood> dailyMoodList) {
                setValue(Pair.create(dailyMoodList, dailyEnergys.getValue()));
            }
        });
        addSource(dailyEnergys, new Observer<List<DailyEnergy>>() {
            @Override
            public void onChanged(@Nullable List<DailyEnergy> dailyEnergyList) {
                setValue(Pair.create(dailyMoods.getValue(), dailyEnergyList));
            }
        });
    }
}
