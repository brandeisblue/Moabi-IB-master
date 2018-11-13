package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.AsyncTaskBooleanDao;
import com.ivorybridge.moabi.database.dao.BuiltInFitnessDao;
import com.ivorybridge.moabi.database.db.AsyncTaskBooleanDB;
import com.ivorybridge.moabi.database.db.BuiltInFitnessDB;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInProfile;
import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;
import com.ivorybridge.moabi.database.entity.util.InputHistory;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;

import androidx.lifecycle.LiveData;

public class BuiltInFitnessRepository {

    private static final String TAG = BuiltInFitnessRepository.class.getSimpleName();
    private static BuiltInFitnessRepository instance = null;
    private SharedPreferences builtInFitnessSharedPreferences;
    // TODO - Protect the API Key (Convert it to a cryptic key)
    private AsyncTaskBooleanDao mTaskSuccessDao;
    private BuiltInFitnessDao builtInFitnessDao;
    private FormattedTime formattedTime;
    private FirebaseManager firebaseManager;
    private InputHistoryRepository inputHistoryRepository;
    private Application application;

    public BuiltInFitnessRepository(Application application) {
        AsyncTaskBooleanDB successDB = AsyncTaskBooleanDB.getDatabase(application);
        this.mTaskSuccessDao = successDB.asyncTaskBooleanDao();
        BuiltInFitnessDB builtInFitnessDB = BuiltInFitnessDB.getDatabase(application);
        this.builtInFitnessDao = builtInFitnessDB.builtInFitnessDao();
        this.formattedTime = new FormattedTime();
        this.firebaseManager = new FirebaseManager();
        this.inputHistoryRepository = new InputHistoryRepository(application);
        this.application = application;
    }

    public LiveData<BuiltInActivitySummary> get(String date) {
        return builtInFitnessDao.get(date);
    }

    public BuiltInActivitySummary getNow(String date) {
        return builtInFitnessDao.getNow(date);
    }

    public LiveData<List<BuiltInActivitySummary>> getActivitySummaries(Long start, Long end) {
        return builtInFitnessDao.getAll(start, end);
    }

    public List<BuiltInActivitySummary> getActivitySummariesNow(Long start, Long end) {
        return builtInFitnessDao.getAllNow(start, end);
    }

    public LiveData<List<BuiltInProfile>> getUserProfile() {
        return builtInFitnessDao.getProfile();
    }

    public List<BuiltInProfile> getUserProfileNow() {
        return builtInFitnessDao.getProfileNow();
    }

    public void deleteAllUserProfiles() {
       builtInFitnessDao.deleteAllUserProfiles();
    }

    public boolean insert(BuiltInActivitySummary builtInActivitySummary, String date) {
        InputHistory inputHistory = new InputHistory();
        inputHistory.setInputType(application.getString(R.string.moabi_tracker_camel_case));
        inputHistory.setDate(date);
        inputHistory.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
        inputHistory.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
        inputHistoryRepository.insert(inputHistory);
        AsyncTask.Status status = new insertAsyncTask(builtInFitnessDao).execute(builtInActivitySummary).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertAsyncTask extends AsyncTask<BuiltInActivitySummary, Void, Void> {

        private BuiltInFitnessDao builtInFitnessDao;

        insertAsyncTask(BuiltInFitnessDao dao) {
            builtInFitnessDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final BuiltInActivitySummary... params) {
            builtInFitnessDao.insert(params[0]);
            return null;
        }
    }

    public boolean insert(BuiltInProfile builtInProfile) {
        AsyncTask.Status status = new insertProfileAsyncTask(builtInFitnessDao).execute(builtInProfile).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertProfileAsyncTask extends AsyncTask<BuiltInProfile, Void, Void> {

        private BuiltInFitnessDao builtInFitnessDao;

        insertProfileAsyncTask(BuiltInFitnessDao dao) {
            builtInFitnessDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final BuiltInProfile... params) {
            builtInFitnessDao.insert(params[0]);
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
