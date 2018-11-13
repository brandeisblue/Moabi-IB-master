package com.ivorybridge.moabi.viewmodel;

import android.app.Application;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.database.firebase.FirebaseQueryLiveData;
import com.ivorybridge.moabi.repository.FitbitRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

public class FitbitViewModel extends AndroidViewModel {

    private static final String TAG = FitbitViewModel.class.getSimpleName();
    private LiveData<List<FitbitDailySummary>> dailySummary;
    private LiveData<Boolean> fitbitCallStatus;
    private LiveData<Boolean> isRefreshSuccessful;
    private Application mApplication;
    private FitbitRepository fitbitRepo;
    private FirebaseManager firebaseManager = new FirebaseManager();


    public FitbitViewModel(Application application) {
        super(application);
        this.fitbitRepo = new FitbitRepository(application);
    }

    public LiveData<List<FitbitDailySummary>> getAll() {
        return fitbitRepo.getAll();
    }

    public LiveData<List<FitbitDailySummary>> getAll(Long start, Long end) {
        return fitbitRepo.getAll(start, end);
    }

    public boolean insert(FitbitDailySummary dailySummary, String date) {
        return fitbitRepo.insert(dailySummary, date);
    }

    public void downloadData(String date) {
        this.fitbitRepo.downloadData(date);
    }

    public void sync() {
        this.fitbitRepo.sync();
    }

    public LiveData<Boolean> getFitbitDataFromFirebase() {
        DatabaseReference fitbitRef = firebaseManager.getFitbitRef();
        //fitbitRef.keepSynced(true);
        FirebaseQueryLiveData mLiveData = new FirebaseQueryLiveData(fitbitRef);
        return Transformations.map(mLiveData, new ProcessServiceStatus());
    }

    private class ProcessServiceStatus implements Function<DataSnapshot, Boolean> {

        private Boolean serviceStatus = false;

        @Override
        public Boolean apply(DataSnapshot dataSnapshot) {
            if (dataSnapshot.child("isConnected").getValue() != null) {
                serviceStatus = (Boolean) dataSnapshot.child("isConnected").getValue();
            }
            return serviceStatus;
        }
    }

    public LiveData<DataSnapshot> getFitbitTodaySummaryFromFirebase() {
        firebaseManager = new FirebaseManager();
        DatabaseReference fitbitTodayRef = firebaseManager.getFitbitTodayRef();
        FirebaseQueryLiveData mLiveData = new FirebaseQueryLiveData(fitbitTodayRef);
        return Transformations.map(mLiveData, new ProcessDailySummary());
    }

    private class ProcessDailySummary implements Function<DataSnapshot, DataSnapshot> {

        private Map<String, Object> todaySummaryMap = new LinkedHashMap<>();

        @Override
        public DataSnapshot apply(DataSnapshot dataSnapshot) {
            return dataSnapshot;
        }
    }
}
