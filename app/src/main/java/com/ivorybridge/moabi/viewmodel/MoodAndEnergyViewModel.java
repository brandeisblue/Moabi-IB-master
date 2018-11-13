package com.ivorybridge.moabi.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ivorybridge.moabi.database.entity.moodandenergy.DailyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.DailyMood;
import com.ivorybridge.moabi.database.entity.moodandenergy.MonthlyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.MonthlyMood;
import com.ivorybridge.moabi.database.entity.moodandenergy.MoodAndEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.WeeklyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.WeeklyMood;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.MoodAndEnergyRepository;

import java.util.List;

public class MoodAndEnergyViewModel extends AndroidViewModel {

    private static final String TAG = MoodAndEnergyViewModel.class.getSimpleName();
    private FirebaseManager firebaseManager;
    private MoodAndEnergyRepository moodAndEnergyRepository;
    private LiveData<List<MoodAndEnergy>> mMoodAndEnergyList;

    public MoodAndEnergyViewModel(Application application) {
        super(application);
        moodAndEnergyRepository = new MoodAndEnergyRepository(application);
        firebaseManager = new FirebaseManager();
    }

    public LiveData<MoodAndEnergy> get(Long timeOfEntry) {
        return moodAndEnergyRepository.get(timeOfEntry);
    }

    public LiveData<List<MoodAndEnergy>> getEntries(Long start, Long end) {
        return moodAndEnergyRepository.getEntries(start, end);
    }

    public LiveData<List<MoodAndEnergy>> getAllEntries() {
        return moodAndEnergyRepository.getAllEntries();
    }

    public LiveData<List<DailyMood>> getDailyMoods(Long start, Long end) {
        return moodAndEnergyRepository.getDailyMoods(start, end);
    }

    public LiveData<List<WeeklyMood>> getWeeklyMoods(Long start, Long end) {
        return moodAndEnergyRepository.getWeeklyMoods(start, end);
    }

    public LiveData<List<MonthlyMood>> getMonthlyMoods(Long start, Long end) {
        return moodAndEnergyRepository.getMonthlyMoods(start, end);
    }

    public LiveData<List<DailyEnergy>> getDailyEnergies(Long start, Long end) {
        return moodAndEnergyRepository.getDailyEnergies(start, end);
    }

    public LiveData<List<WeeklyEnergy>> getWeeklyEnergies(Long start, Long end) {
        return moodAndEnergyRepository.getWeeklyEnergies(start, end);
    }

    public LiveData<List<MonthlyEnergy>> getMonthlyEnergies(Long start, Long end) {
        return moodAndEnergyRepository.getMonthlyEnergies(start, end);
    }


    public boolean insert(MoodAndEnergy moodAndEnergy, String date) {
        return moodAndEnergyRepository.insert(moodAndEnergy, date);
    }

    public void processMood(List<MoodAndEnergy> moodAndEnergyList) {
        this.moodAndEnergyRepository.processMood(moodAndEnergyList);
    }
}
