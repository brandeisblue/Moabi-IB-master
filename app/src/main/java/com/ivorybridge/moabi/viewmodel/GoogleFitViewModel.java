package com.ivorybridge.moabi.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.repository.GoogleFitRepository;


import java.util.List;

public class GoogleFitViewModel extends AndroidViewModel {

    private static final String TAG = GoogleFitViewModel.class.getSimpleName();
    private GoogleFitRepository googleFitRepository;
    private LiveData<List<GoogleFitSummary>> mGoogleFitSummaries;
    private LiveData<GoogleFitSummary> mGoogleFitSummary;

    public GoogleFitViewModel(Application application) {
        super(application);
        this.googleFitRepository = new GoogleFitRepository(application);
    }

    public LiveData<List<GoogleFitSummary>> getAll() {
        return googleFitRepository.getAll();
    }

    public LiveData<List<GoogleFitSummary>> getAll(Long start, Long end) {
        return googleFitRepository.getAll(start, end);
    }

    public LiveData<GoogleFitSummary> get(String date) {
        return this.googleFitRepository.get(date);
    }
}
