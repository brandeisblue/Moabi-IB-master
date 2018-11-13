package com.ivorybridge.moabi.database.entity.util;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class DataInUseMediatorLiveData extends MediatorLiveData<Pair<List<InputInUse>, List<ConnectedService>>> {

    public DataInUseMediatorLiveData(LiveData<List<InputInUse>> inputsInUse, LiveData<List<ConnectedService>> connectedServices) {
        addSource(inputsInUse, new Observer<List<InputInUse>>() {
            @Override
            public void onChanged(@Nullable List<InputInUse> inputsInUse) {
                setValue(Pair.create(inputsInUse, connectedServices.getValue()));
            }
        });
        addSource(connectedServices, new Observer<List<ConnectedService>>() {
            @Override
            public void onChanged(@Nullable List<ConnectedService> connectedServices) {
                setValue(Pair.create(inputsInUse.getValue(), connectedServices));
            }
        });
    }
}

