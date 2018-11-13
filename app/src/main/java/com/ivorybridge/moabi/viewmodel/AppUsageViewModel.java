package com.ivorybridge.moabi.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.repository.AppUsageRepository;

import java.util.List;


public class AppUsageViewModel extends AndroidViewModel {

    private AppUsageRepository mRepository;

    public AppUsageViewModel(Application application) {
        super(application);
        this.mRepository = new AppUsageRepository(application);
    }

    public LiveData<AppUsageSummary> get(String date) {
        return this.mRepository.get(date);
    }

    public void query(String date) {
        this.mRepository.query(date);
    }

    public LiveData<List<AppUsageSummary>> getAll() {
        return this.mRepository.getAll();
    }

    public LiveData<List<AppUsageSummary>> getAll(Long start, Long end) {
        return this.mRepository.getAll(start, end);
    }
}
