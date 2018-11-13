package com.ivorybridge.moabi.viewmodel;

import android.app.Application;

import com.ivorybridge.moabi.database.entity.depression.DailyPhq9;
import com.ivorybridge.moabi.database.entity.depression.MonthlyPhq9;
import com.ivorybridge.moabi.database.entity.depression.Phq9;
import com.ivorybridge.moabi.database.entity.depression.WeeklyPhq9;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.DepressionRepository;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class DepressionViewModel extends AndroidViewModel {

    private static final String TAG = DepressionViewModel.class.getSimpleName();
    private FirebaseManager firebaseManager;
    private DepressionRepository depressionRepository;

    public DepressionViewModel(Application application) {
        super(application);
        depressionRepository = new DepressionRepository(application);
        firebaseManager = new FirebaseManager();
    }

    public LiveData<Phq9> get(Long timeOfEntry) {
        return depressionRepository.get(timeOfEntry);
    }

    public LiveData<List<Phq9>> getEntries(Long start, Long end) {
        return depressionRepository.getEntries(start, end);
    }

    public LiveData<List<Phq9>> getAllEntries() {
        return depressionRepository.getAllEntries();
    }

    public LiveData<List<DailyPhq9>> getDailyPhq9s(Long start, Long end) {
        return depressionRepository.getDailyPhq9s(start, end);
    }

    public LiveData<List<WeeklyPhq9>> getWeeklyPhq9s(Long start, Long end) {
        return depressionRepository.getWeeklyPhq9s(start, end);
    }

    public LiveData<List<MonthlyPhq9>> getMonthlyPhq9s(Long start, Long end) {
        return depressionRepository.getMonthlyPhq9s(start, end);
    }

    public boolean insert(Phq9 depression, String date) {
        return depressionRepository.insert(depression, date);
    }

    public void processPhq9(List<Phq9> depressionList) {
        this.depressionRepository.processPhq9(depressionList);
    }
}
