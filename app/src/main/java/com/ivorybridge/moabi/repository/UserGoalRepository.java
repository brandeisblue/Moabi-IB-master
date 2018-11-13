package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.AsyncTaskBooleanDao;
import com.ivorybridge.moabi.database.dao.UserGoalDao;
import com.ivorybridge.moabi.database.db.AsyncTaskBooleanDB;
import com.ivorybridge.moabi.database.db.UserGoalDB;
import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;
import com.ivorybridge.moabi.database.entity.util.UserGoal;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;

import androidx.lifecycle.LiveData;

public class UserGoalRepository {

    private static final String TAG = UserGoalRepository.class.getSimpleName();
    private static UserGoalRepository instance = null;
    private SharedPreferences userGoalSharedPreferences;
    // TODO - Protect the API Key (Convert it to a cryptic key)
    private AsyncTaskBooleanDao mTaskSuccessDao;
    private UserGoalDao userGoalDao;
    private FormattedTime formattedTime;
    private FirebaseManager firebaseManager;
    private InputHistoryRepository inputHistoryRepository;


    public UserGoalRepository(Application application) {
        AsyncTaskBooleanDB successDB = AsyncTaskBooleanDB.getDatabase(application);
        this.mTaskSuccessDao = successDB.asyncTaskBooleanDao();
        UserGoalDB userGoalDB = UserGoalDB.getDatabase(application);
        this.userGoalDao = userGoalDB.userGoalDao();
        this.formattedTime = new FormattedTime();
        this.firebaseManager = new FirebaseManager();
        this.inputHistoryRepository = new InputHistoryRepository(application);
    }

    public UserGoal getGoalNow(int priority) {
        return userGoalDao.getGoalNow(priority);
    }

    public LiveData<UserGoal> getGoal(int priority) {
        return userGoalDao.getGoal(priority);
    }

    public LiveData<List<UserGoal>> getAll() {
        return userGoalDao.getAll();
    }

    public LiveData<List<UserGoal>> getAll(Long start, Long end) {
        return userGoalDao.getAll(start, end);
    }

    public boolean insert(UserGoal userGoal) {
        AsyncTask.Status status = new insertAsyncTask(userGoalDao).execute(userGoal).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertAsyncTask extends AsyncTask<UserGoal, Void, Void> {

        private UserGoalDao userGoalDao;

        insertAsyncTask(UserGoalDao dao) {
            userGoalDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final UserGoal... params) {
            userGoalDao.insert(params[0]);
            return null;
        }
    }



    private boolean insertSuccess(AsyncTaskBoolean asyncTaskSuccess) {
        AsyncTask.Status status = new insertSuccessAsyncTask(mTaskSuccessDao).execute(asyncTaskSuccess).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertSuccessAsyncTask extends AsyncTask<AsyncTaskBoolean, Void, Void> {

        private AsyncTaskBooleanDao mAsyncTaskDao;

        insertSuccessAsyncTask(AsyncTaskBooleanDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final AsyncTaskBoolean... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
