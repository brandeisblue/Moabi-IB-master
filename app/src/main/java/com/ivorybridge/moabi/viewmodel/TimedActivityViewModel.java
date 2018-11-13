package com.ivorybridge.moabi.viewmodel;

import android.app.Application;

import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.TimedActivityRepository;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

// TODO - create an interface for all ViewModels.
public class TimedActivityViewModel extends AndroidViewModel{

    private static final String TAG = TimedActivityViewModel.class.getSimpleName();
    private TimedActivityRepository timedActivityRepository;
    private FirebaseManager firebaseManager;
    private Application application;

    public TimedActivityViewModel(Application application) {
        super(application);
        this.timedActivityRepository = new TimedActivityRepository(application);
        firebaseManager = new FirebaseManager();
        this.application = application;
    }

    public void insert(TimedActivitySummary timedActivitySummary, String date) {
        this.timedActivityRepository.insert(timedActivitySummary, date);
    }

    public LiveData<List<TimedActivitySummary>> getAll(String date) {
        return timedActivityRepository.getAll(date);
    }

    public LiveData<List<TimedActivitySummary>> getAll() {
        return timedActivityRepository.getAll();
    }

    public LiveData<List<TimedActivitySummary>> getAll(Long start, Long end) {
        return timedActivityRepository.getAll(start, end);
    }

    public LiveData<List<String>> getAllInputDates() {
        return timedActivityRepository.getInputDates();
    }
}
