package com.ivorybridge.moabi.viewmodel;

import android.app.Application;

import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.DataInUseRepository;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;


public class DataInUseViewModel extends AndroidViewModel {

    private static final String TAG = DataInUseViewModel.class.getSimpleName();
    private DataInUseRepository mRepository;
    private FirebaseManager firebaseManager;

    public DataInUseViewModel(Application application) {
        super(application);
        this.mRepository = new DataInUseRepository(application);
    }

    public LiveData<List<InputInUse>> getAllInputsInUse() {
        return this.mRepository.getAllInputsInUse();
    }

    public List<InputInUse> getAllInputsInUseNow() {
        return this.mRepository.getAllInputsInUseNow();
    }

    public boolean insert(InputInUse selection) {
        return this.mRepository.insert(selection);
    }

    public LiveData<List<ConnectedService>> getAllConnectedServices() {
        return this.mRepository.getAllConnectedServices();
    }

    public List<ConnectedService> getAllConnectedServicesNow() {
        return this.mRepository.getAllConnectedServicesNow();
    }

    public boolean insert(ConnectedService selection) {
        return this.mRepository.insert(selection);
    }
}
