package com.ivorybridge.moabi.viewmodel;

import android.app.Application;

import com.ivorybridge.moabi.database.entity.util.UserGoal;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.UserGoalRepository;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class UserGoalViewModel extends AndroidViewModel {

    private static final String TAG = UserGoalViewModel.class.getSimpleName();
    private UserGoalRepository userGoalRepository;
    private FirebaseManager firebaseManager;
    private Application application;

    public UserGoalViewModel(Application application) {
        super(application);
        this.userGoalRepository = new UserGoalRepository(application);
        firebaseManager = new FirebaseManager();
        this.application = application;
    }

    public void insert(UserGoal userGoal) {
        this.userGoalRepository.insert(userGoal);
    }

    public LiveData<List<UserGoal>> getAll() {
        return userGoalRepository.getAll();
    }

    public LiveData<List<UserGoal>> getAll(Long start, Long end) {
        return userGoalRepository.getAll(start, end);
    }

    public LiveData<UserGoal> getGoal(int priority) {
        return userGoalRepository.getGoal(priority);
    }
}
