package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.CredentialDao;
import com.ivorybridge.moabi.database.db.CredentialDB;
import com.ivorybridge.moabi.database.entity.util.Credential;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;

import androidx.lifecycle.LiveData;

public class CredentialRepository {

    private static final String TAG = CredentialRepository.class.getSimpleName();
    private CredentialDao mTaskDao;
    private FitbitRepository fitbitRepository;
    private FormattedTime formattedTime;

    public CredentialRepository(Application application) {
        CredentialDB db = CredentialDB.getDatabase(application);
        formattedTime = new FormattedTime();
        mTaskDao = db.dao();
    }

    public LiveData<List<Credential>> getAll() {
        return mTaskDao.getAll();
    }

    public List<Credential> getAllNow() {
        return mTaskDao.getAllNow();
    }

    public LiveData<Credential> get(String serviceName) {
        return mTaskDao.get(serviceName);
    }

    public Credential getNow(String serviceName) {
        return mTaskDao.getNow(serviceName);
    }

    public boolean insert(Credential Credential) {
        AsyncTask.Status status = new insertAsyncTask(mTaskDao).execute(Credential).getStatus();
        return status.equals(AsyncTask.Status.FINISHED);
    }

    private static class insertAsyncTask extends AsyncTask<Credential, Void, Void> {

        private CredentialDao mAsyncTaskDao;

        insertAsyncTask(CredentialDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final Credential... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}