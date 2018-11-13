package com.ivorybridge.moabi.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInProfile;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;

import java.util.List;

public class BuiltInFitnessViewModel extends AndroidViewModel {

    private BuiltInFitnessRepository mRepository;

    public BuiltInFitnessViewModel(Application application) {
        super(application);
        this.mRepository = new BuiltInFitnessRepository(application);
    }

    public LiveData<BuiltInActivitySummary> get(String date) {
        return this.mRepository.get(date);
    }

    public List<BuiltInActivitySummary> getAllSummariesNow(Long start, Long end) {
        return this.mRepository.getActivitySummariesNow(start, end);
    }

    public LiveData<List<BuiltInActivitySummary>> getAllSummaries(Long start, Long end) {
        return this.mRepository.getActivitySummaries(start, end);
    }

    public LiveData<List<BuiltInProfile>> getUserProfile() {
        return this.mRepository.getUserProfile();
    }
}
