package com.ivorybridge.moabi.database.entity.baactivity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import java.util.List;



public class BAActivityMediatorLiveData extends MediatorLiveData<Pair<List<BAActivityInLibrary>, List<BAActivityFavorited>>>{

    public BAActivityMediatorLiveData(LiveData<List<BAActivityInLibrary>> baActivitiesInLibrary, LiveData<List<BAActivityFavorited>> baActivitiesFavorited) {
        addSource(baActivitiesInLibrary, new Observer<List<BAActivityInLibrary>>() {
            @Override
            public void onChanged(@Nullable List<BAActivityInLibrary> baActivityInLibraries) {
                setValue(Pair.create(baActivityInLibraries, baActivitiesFavorited.getValue()));
            }
        });
        addSource(baActivitiesFavorited, new Observer<List<BAActivityFavorited>>() {
            @Override
            public void onChanged(@Nullable List<BAActivityFavorited> baActivityFavoriteds) {
                setValue(Pair.create(baActivitiesInLibrary.getValue(), baActivityFavoriteds));
            }
        });
    }
}
