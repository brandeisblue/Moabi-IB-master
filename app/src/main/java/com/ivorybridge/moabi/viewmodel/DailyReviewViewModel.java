package com.ivorybridge.moabi.viewmodel;

import android.app.Application;

import com.ivorybridge.moabi.database.entity.dailyreview.DailyDailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.DailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.MonthlyDailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.WeeklyDailyReview;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.DailyReviewRepository;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class DailyReviewViewModel extends AndroidViewModel {

    private static final String TAG = DailyReviewViewModel.class.getSimpleName();
    private FirebaseManager firebaseManager;
    private DailyReviewRepository dailyReviewRepository;
    private LiveData<List<DailyReview>> mDailyReviewList;

    public DailyReviewViewModel(Application application) {
        super(application);
        dailyReviewRepository = new DailyReviewRepository(application);
        firebaseManager = new FirebaseManager();
    }

    public LiveData<DailyReview> get(Long timeOfEntry) {
        return dailyReviewRepository.get(timeOfEntry);
    }

    public LiveData<List<DailyReview>> getEntries(Long start, Long end) {
        return dailyReviewRepository.getEntries(start, end);
    }

    public LiveData<List<DailyReview>> getAllEntries() {
        return dailyReviewRepository.getAllEntries();
    }

    public LiveData<List<DailyDailyReview>> getDailyDailyReviews(Long start, Long end) {
        return dailyReviewRepository.getDailyDailyReviews(start, end);
    }

    public LiveData<List<WeeklyDailyReview>> getWeeklyDailyReviews(Long start, Long end) {
        return dailyReviewRepository.getWeeklyDailyReviews(start, end);
    }

    public LiveData<List<MonthlyDailyReview>> getMonthlyDailyReviews(Long start, Long end) {
        return dailyReviewRepository.getMonthlyDailyReviews(start, end);
    }

    public boolean insert(DailyReview dailyReview, String date) {
        return dailyReviewRepository.insert(dailyReview, date);
    }

    public void processDailyReview(List<DailyReview> dailyReviewList) {
        this.dailyReviewRepository.processDailyReview(dailyReviewList);
    }
}

