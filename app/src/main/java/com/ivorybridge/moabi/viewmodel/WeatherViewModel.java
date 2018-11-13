package com.ivorybridge.moabi.viewmodel;

import android.app.Application;

import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.WeatherRepository;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class WeatherViewModel extends AndroidViewModel {

    private static final String TAG = WeatherViewModel.class.getSimpleName();
    private WeatherRepository weatherRepository;
    private FirebaseManager firebaseManager;
    private Application application;

    public WeatherViewModel(Application application) {
        super(application);
        this.weatherRepository = new WeatherRepository(application);
        firebaseManager = new FirebaseManager();
        this.application = application;
    }

    public void insert(WeatherDailySummary weatherDailySummary) {
        this.weatherRepository.insert(weatherDailySummary);
    }

    public LiveData<List<WeatherDailySummary>> getAll(Long start, Long end) {
        return weatherRepository.getAll(start, end);
    }
}
