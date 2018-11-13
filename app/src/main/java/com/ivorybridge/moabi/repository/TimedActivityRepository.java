package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.TimedActivityDao;
import com.ivorybridge.moabi.database.db.TimedActivityDB;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.database.entity.util.InputHistory;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

//TODO - create an interface for all repositories
public class TimedActivityRepository {

    private static final String TAG = TimedActivityRepository.class.getSimpleName();
    private TimedActivityDao timedActivityDao;
    private FirebaseManager firebaseManager;
    private InputHistoryRepository inputHistoryRepository;
    private FormattedTime formattedTime;
    private Application application;

    public TimedActivityRepository(Application application) {
        this.firebaseManager = new FirebaseManager();
        TimedActivityDB db = TimedActivityDB.getDatabase(application);
        timedActivityDao = db.todayDao();
        inputHistoryRepository = new InputHistoryRepository(application);
        firebaseManager = new FirebaseManager();
        formattedTime = new FormattedTime();
        this.application = application;
    }

    public LiveData<List<String>> getInputDates() {
        return timedActivityDao.getDates();
    }

    public LiveData<List<TimedActivitySummary>> getAll(String date) {
        return timedActivityDao.getAll(date);
    }

    public LiveData<List<TimedActivitySummary>> getAll() {
        return timedActivityDao.getAll();
    }

    public LiveData<List<TimedActivitySummary>> getAll(Long start, Long end) {
        return timedActivityDao.getAll(start, end);
    }

    public List<TimedActivitySummary> getAllNow(Long start, Long end) {
        return timedActivityDao.getAllNow(start, end);
    }

    public boolean insert(TimedActivitySummary timedActivitySummary, String date) {
        AsyncTask.Status status = new TimedActivityRepository.insertAsyncTask(timedActivityDao).execute(timedActivitySummary).getStatus();
        InputHistory inputHistory = new InputHistory();
        inputHistory.setInputType(application.getString(R.string.timer_camel_case));
        inputHistory.setDate(date);
        inputHistory.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
        inputHistory.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
        inputHistoryRepository.insert(inputHistory);
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    public void sync() {
        firebaseManager.getUserInputsRef().child(application.getString(R.string.timer_camel_case)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dailySnap: dataSnapshot.getChildren()) {
                    if (dailySnap.hasChildren() && dailySnap.getKey() != null) {
                        String date = dailySnap.getKey();
                        for (DataSnapshot timeSnap: dailySnap.getChildren()) {
                            if (timeSnap.getKey() != null && timeSnap.hasChildren()) {
                                for (DataSnapshot activitySnap: timeSnap.getChildren()) {
                                    if (activitySnap.getKey() != null && activitySnap.hasChildren()) {
                                        String dateTimeToConvert = date + " " + timeSnap.getKey();
                                        long dateInLong = formattedTime.convertStringYYYYMMDDhhmmToLong(dateTimeToConvert);
                                        TimedActivitySummary timedActivitySummary = new TimedActivitySummary();
                                        timedActivitySummary.setInputName(activitySnap.getKey());
                                        timedActivitySummary.setDateInLong(dateInLong);
                                        timedActivitySummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                        timedActivitySummary.setDuration((Long) activitySnap.getValue());
                                        insert(timedActivitySummary, date);
                                    }
                                }

                            }
                        }
                        firebaseManager.getDaysWithDataRef().child(date).child(application.getString(R.string.timer_camel_case)).setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static class insertAsyncTask extends AsyncTask<TimedActivitySummary, Void, Void> {

        private TimedActivityDao mAsyncTaskDao;

        insertAsyncTask(TimedActivityDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final TimedActivitySummary... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
