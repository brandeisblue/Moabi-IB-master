package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.DataInUseDao;
import com.ivorybridge.moabi.database.db.DataInUseDB;
import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.InputInUse;

import java.util.List;

import androidx.lifecycle.LiveData;

public class DataInUseRepository {

    private static final String TAG = DataInUseRepository.class.getSimpleName();
    private DataInUseDao mTaskDao;

    public DataInUseRepository(Application application) {
        DataInUseDB db = DataInUseDB.getDatabase(application);
        mTaskDao = db.selectedActivitiesDao();
    }

    public LiveData<List<InputInUse>> getAllInputsInUse() {
        return mTaskDao.getAllInputsInUse();
    }

    public List<InputInUse> getAllInputsInUseNow() {
        return mTaskDao.getAllInputsInUseNow();
    }

    public boolean insert(InputInUse InputInUse) {
        AsyncTask.Status status = new insertAsyncTask(mTaskDao).execute(InputInUse).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertAsyncTask extends AsyncTask<InputInUse, Void, Void> {

        private DataInUseDao mAsyncTaskDao;

        insertAsyncTask(DataInUseDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final InputInUse... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public LiveData<List<ConnectedService>> getAllConnectedServices() {
        return mTaskDao.getAllConnectedServices();
    }

    public List<ConnectedService> getAllConnectedServicesNow() {
        return mTaskDao.getAllConnectedServicesNow();
    }

    public boolean insert(ConnectedService connectedService) {
        AsyncTask.Status status = new insertConnectedServiceAsyncTask(mTaskDao).execute(connectedService).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertConnectedServiceAsyncTask extends AsyncTask<ConnectedService, Void, Void> {

        private DataInUseDao mAsyncTaskDao;

        insertConnectedServiceAsyncTask(DataInUseDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final ConnectedService... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}