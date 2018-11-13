package com.ivorybridge.moabi.viewmodel;

import android.app.Application;

import com.ivorybridge.moabi.database.entity.stress.DailyStress;
import com.ivorybridge.moabi.database.entity.stress.MonthlyStress;
import com.ivorybridge.moabi.database.entity.stress.Stress;
import com.ivorybridge.moabi.database.entity.stress.WeeklyStress;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.StressRepository;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class StressViewModel extends AndroidViewModel {

    private static final String TAG = StressViewModel.class.getSimpleName();
    private FirebaseManager firebaseManager;
    private StressRepository stressRepository;
    private LiveData<List<Stress>> mStressList;

    public StressViewModel(Application application) {
        super(application);
        stressRepository = new StressRepository(application);
        firebaseManager = new FirebaseManager();
    }

    public LiveData<Stress> get(Long timeOfEntry) {
        return stressRepository.get(timeOfEntry);
    }

    public LiveData<List<Stress>> getEntries(Long start, Long end) {
        return stressRepository.getEntries(start, end);
    }

    public LiveData<List<Stress>> getAllEntries() {
        return stressRepository.getAllEntries();
    }

    public LiveData<List<DailyStress>> getDailyStress(Long start, Long end) {
        return stressRepository.getDailyStresss(start, end);
    }

    public LiveData<List<WeeklyStress>> getWeeklyStress(Long start, Long end) {
        return stressRepository.getWeeklyStresss(start, end);
    }

    public LiveData<List<MonthlyStress>> getMonthlyStress(Long start, Long end) {
        return stressRepository.getMonthlyStresss(start, end);
    }

    public boolean insert(Stress stress, String date) {
        return stressRepository.insert(stress, date);
    }

    public void processStress(List<Stress> stressList) {
        this.stressRepository.processStress(stressList);
    }
}

