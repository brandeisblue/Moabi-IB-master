package com.ivorybridge.moabi.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;
import com.ivorybridge.moabi.repository.AsyncTaskBooleanRepository;

import java.util.List;

public class AsyncTaskBooleanViewModel extends AndroidViewModel {

    private static final String TAG = AsyncTaskBooleanViewModel.class.getSimpleName();
    private AsyncTaskBooleanRepository mRepository;

    public AsyncTaskBooleanViewModel(Application application) {
        super(application);
        this.mRepository = new AsyncTaskBooleanRepository(application);
    }

    public LiveData<AsyncTaskBoolean> getTaskBoolean(String taskName) {
        return this.mRepository.getTaskBoolean(taskName);
    }

    public LiveData<List<AsyncTaskBoolean>> getAll() {
        return this.mRepository.getAllTasksBoolean();
    }
}
