package com.ivorybridge.moabi.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.ivorybridge.moabi.database.dao.AsyncTaskBooleanDao;
import com.ivorybridge.moabi.database.db.AsyncTaskBooleanDB;
import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;

import java.util.List;


public class AsyncTaskBooleanRepository {

    private static final String TAG = AsyncTaskBooleanRepository.class.getSimpleName();
    private AsyncTaskBooleanDao mTaskDao;

    public AsyncTaskBooleanRepository(Application application) {
        AsyncTaskBooleanDB db = AsyncTaskBooleanDB.getDatabase(application);
        mTaskDao = db.asyncTaskBooleanDao();
    }

    public LiveData<AsyncTaskBoolean> getTaskBoolean(String taskName) {
        return mTaskDao.get(taskName);
    }

    public LiveData<List<AsyncTaskBoolean>> getAllTasksBoolean() {
        return mTaskDao.getAll();
    }
}
