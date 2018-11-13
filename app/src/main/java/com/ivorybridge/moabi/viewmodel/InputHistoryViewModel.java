package com.ivorybridge.moabi.viewmodel;

import android.app.Application;

import com.ivorybridge.moabi.database.entity.util.InputDate;
import com.ivorybridge.moabi.database.entity.util.InputHistory;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.InputHistoryRepository;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class InputHistoryViewModel extends AndroidViewModel {

    private static final String TAG = InputHistoryViewModel.class.getSimpleName();
    private InputHistoryRepository inputHistoryRepository;
    private FirebaseManager firebaseManager;
    private LiveData<List<InputHistory>> mInputHistorySummaries;
    private Application application;

    public InputHistoryViewModel(Application application) {
        super(application);
        this.inputHistoryRepository = new InputHistoryRepository(application);
        firebaseManager = new FirebaseManager();
        this.application = application;
    }

    public LiveData<List<InputHistory>> getAll() {
        return inputHistoryRepository.getAll();
    }

    public LiveData<List<InputHistory>> getInputHistory(String date) {
        return inputHistoryRepository.getInputHistory(date);
    }

    public LiveData<List<InputDate>> getAllInputDates() {
        return inputHistoryRepository.getInputDates();
    }

    public LiveData<List<String>> getAllInputTypes(Long start, Long end) {
        return inputHistoryRepository.getInputTypes(start, end);
    }
}