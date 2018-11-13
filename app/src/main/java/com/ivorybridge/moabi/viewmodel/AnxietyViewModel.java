package com.ivorybridge.moabi.viewmodel;

import android.app.Application;

import com.ivorybridge.moabi.database.entity.anxiety.DailyGad7;
import com.ivorybridge.moabi.database.entity.anxiety.Gad7;
import com.ivorybridge.moabi.database.entity.anxiety.MonthlyGad7;
import com.ivorybridge.moabi.database.entity.anxiety.WeeklyGad7;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.AnxietyRepository;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class AnxietyViewModel extends AndroidViewModel {

    private static final String TAG = AnxietyViewModel.class.getSimpleName();
    private FirebaseManager firebaseManager;
    private AnxietyRepository anxietyRepository;

    public AnxietyViewModel(Application application) {
        super(application);
        anxietyRepository = new AnxietyRepository(application);
        firebaseManager = new FirebaseManager();
    }

    public LiveData<Gad7> get(Long timeOfEntry) {
        return anxietyRepository.get(timeOfEntry);
    }

    public LiveData<List<Gad7>> getEntries(Long start, Long end) {
        return anxietyRepository.getEntries(start, end);
    }

    public LiveData<List<Gad7>> getAllEntries() {
        return anxietyRepository.getAllEntries();
    }

    public LiveData<List<DailyGad7>> getDailyGad7s(Long start, Long end) {
        return anxietyRepository.getDailyGad7s(start, end);
    }

    public LiveData<List<WeeklyGad7>> getWeeklyGad7s(Long start, Long end) {
        return anxietyRepository.getWeeklyGad7s(start, end);
    }

    public LiveData<List<MonthlyGad7>> getMonthlyGad7s(Long start, Long end) {
        return anxietyRepository.getMonthlyGad7s(start, end);
    }

    public boolean insert(Gad7 anxiety, String date) {
        return anxietyRepository.insert(anxiety, date);
    }

    public void processGad7(List<Gad7> anxietyList) {
        this.anxietyRepository.processGad7(anxietyList);
    }
}
