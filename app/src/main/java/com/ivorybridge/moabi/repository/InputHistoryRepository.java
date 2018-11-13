package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.InputHistoryDao;
import com.ivorybridge.moabi.database.db.InputHistoryDB;
import com.ivorybridge.moabi.database.entity.util.InputDate;
import com.ivorybridge.moabi.database.entity.util.InputHistory;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;

import java.util.List;

import androidx.lifecycle.LiveData;

public class InputHistoryRepository {

    private static final String TAG = InputHistoryRepository.class.getSimpleName();
    private InputHistoryDao inputHistoryDao;
    private FirebaseManager firebaseManager;

    public InputHistoryRepository(Application application) {
        this.firebaseManager = new FirebaseManager();
        InputHistoryDB db = InputHistoryDB.getDatabase(application);
        inputHistoryDao = db.inputHistoryDao();
    }

    public LiveData<List<InputHistory>> getAll() {
        return inputHistoryDao.getAll();
    }

    public LiveData<List<InputHistory>> getInputHistory(String date) {
        return inputHistoryDao.getAll(date);
    }

    public LiveData<List<String>> getInputTypes(Long start, Long end) {
        return inputHistoryDao.getAllInputTypesByDate(start, end);
    }

    public boolean insert(InputHistory inputHistory) {
        AsyncTask.Status status = new insertAsyncTask(inputHistoryDao).execute(inputHistory).getStatus();
        InputDate inputDate = new InputDate();
        inputDate.setDate(inputHistory.getDate());
        inputDate.setHasData(true);
        insert(inputDate);
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    public LiveData<List<InputDate>> getInputDates() {
        return inputHistoryDao.getInputDates();
    }

    private static class insertAsyncTask extends AsyncTask<InputHistory, Void, Void> {

        private InputHistoryDao mAsyncTaskDao;

        insertAsyncTask(InputHistoryDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final InputHistory... params) {
            mAsyncTaskDao.insertInputHistory(params[0]);
            return null;
        }
    }

    public boolean insert(InputDate inputDate) {
        AsyncTask.Status status = new insertInputDateAsyncTask(inputHistoryDao).execute(inputDate).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertInputDateAsyncTask extends AsyncTask<InputDate, Void, Void> {

        private InputHistoryDao mAsyncTaskDao;

        insertInputDateAsyncTask(InputHistoryDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final InputDate... params) {
            mAsyncTaskDao.insertInputDate(params[0]);
            return null;
        }
    }
}
