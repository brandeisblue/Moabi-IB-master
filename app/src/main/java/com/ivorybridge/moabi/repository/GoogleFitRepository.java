package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.GoogleFitDao;
import com.ivorybridge.moabi.database.db.GoogleFitDB;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitGoal;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.entity.util.InputHistory;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class GoogleFitRepository {

    private static final String TAG = GoogleFitRepository.class.getSimpleName();
    private GoogleFitDao mGoogleFitDao;
    private FirebaseManager firebaseManager;
    private FormattedTime formattedTime;
    private InputHistoryRepository inputHistoryRepository;
    private Application application;


    public GoogleFitRepository(Application application) {
        //this.firebaseManager = new FirebaseManager();
        GoogleFitDB db = GoogleFitDB.getDatabase(application);
        mGoogleFitDao = db.googleFitDao();
        formattedTime = new FormattedTime();
        firebaseManager = new FirebaseManager();
        inputHistoryRepository = new InputHistoryRepository(application);
        this.application = application;
    }

    // wrapper for getting a list of daily summaries
    public LiveData<List<GoogleFitSummary>> getAll() {
        return  mGoogleFitDao.getAll();
    }

    public LiveData<GoogleFitSummary> get(String date) {
        return mGoogleFitDao.get(date);
    }

    public LiveData<List<GoogleFitSummary>> getAll(Long start, Long end) {
        return mGoogleFitDao.getAll(start, end);
    }

    public List<GoogleFitSummary> getAllNow(Long start, Long end) {
        return mGoogleFitDao.getAllNow(start, end);
    }

    public boolean insert(GoogleFitSummary dailySummary, String date) {
        InputHistory inputHistory = new InputHistory();
        inputHistory.setDate(date);
        inputHistory.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
        inputHistory.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
        inputHistory.setInputType(application.getString(R.string.googlefit_camel_case));
        inputHistoryRepository.insert(inputHistory);
        AsyncTask.Status status = new GoogleFitRepository.insertAsyncTask(mGoogleFitDao).execute(dailySummary).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean update(GoogleFitSummary dailySummary) {
        AsyncTask.Status status = new updateAsyncTask(mGoogleFitDao).execute(dailySummary).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertAsyncTask extends AsyncTask<GoogleFitSummary, Void, Void> {

        private GoogleFitDao mAsyncTaskDao;

        insertAsyncTask(GoogleFitDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final GoogleFitSummary... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<GoogleFitSummary, Void, Void> {

        private GoogleFitDao mAsyncTaskDao;

        updateAsyncTask(GoogleFitDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final GoogleFitSummary... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    public void sync() {
        firebaseManager.getGoogleFitRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dateSnap: dataSnapshot.getChildren()) {
                    String date = "";
                    GoogleFitSummary googleFitSummary = new GoogleFitSummary();
                    List<GoogleFitSummary.Summary> summaries = new ArrayList<>();
                    List<GoogleFitGoal> goals = new ArrayList<>();
                    GoogleFitGoal googleFitGoal = new GoogleFitGoal();
                    if (dateSnap.getKey() != null) {
                        date = dateSnap.getKey();
                    }
                    if (dateSnap.child("goals").hasChildren()) {
                        //Log.i(TAG, "activitySummary node has children");
                        for (DataSnapshot goalSnap: dateSnap.child("goals").getChildren()) {
                            GoogleFitGoal goal = goalSnap.getValue(GoogleFitGoal.class);
                            goals.add(goal);
                        }
                    }
                    if (dateSnap.child("summary").hasChildren()) {
                        for (DataSnapshot summarySnap: dateSnap.child("summary").getChildren()) {
                            if (summarySnap.getKey() != null && summarySnap.getKey().equals("lastSync")) {
                                googleFitSummary.setLastSyncTime((String) summarySnap.getValue());
                            } else {
                                if (summarySnap.getKey() != null && summarySnap.getKey().equals(application.getString(R.string.activity_active_minutes_camel_case))) {
                                    Long totalActiveMins = 0L;
                                    Long totalSedentaryMins = 0L;
                                    Log.i(TAG, summarySnap.getKey() + " " + summarySnap.getChildrenCount());
                                    for (DataSnapshot activeMinutesSnap: summarySnap.getChildren()) {
                                        Log.i(TAG, activeMinutesSnap.getKey());
                                        if (activeMinutesSnap.getKey() != null && !activeMinutesSnap.getKey().equals("still") && !activeMinutesSnap.getKey().equals("unknown")) {
                                            totalActiveMins += (long) activeMinutesSnap.getValue();
                                        } else if (activeMinutesSnap.getKey() != null && activeMinutesSnap.getKey().equals("still")) {
                                            totalSedentaryMins += (long) activeMinutesSnap.getValue();
                                        }
                                    }
                                    summaries.add(new GoogleFitSummary.Summary(application.getString(R.string.activity_active_minutes_camel_case), totalActiveMins.doubleValue()));
                                    summaries.add(new GoogleFitSummary.Summary(application.getString(R.string.activity_sedentary_minutes_camel_case), totalSedentaryMins.doubleValue()));
                                } else {
                                    GoogleFitSummary.Summary summary = new GoogleFitSummary.Summary();
                                    if (summarySnap.getValue() instanceof Long) {
                                        Long value = (Long) summarySnap.getValue();
                                        summary.setValue(value.doubleValue());
                                        summary.setName(summarySnap.getKey());
                                    } else if (summarySnap.getValue() instanceof Double){
                                        summary.setValue((Double) summarySnap.getValue());
                                        summary.setName(summarySnap.getKey());
                                    }
                                    summaries.add(summary);
                                }
                            }
                        }
                    }
                    if (dateSnap.child("lastSync").hasChildren()) {
                        googleFitSummary.setLastSyncTime((String) dateSnap.child("lastSync").getValue());
                    }
                    googleFitSummary.setDate(date);
                    googleFitSummary.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                    googleFitSummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    googleFitSummary.setGoals(goals);
                    googleFitSummary.setSummaries(summaries);
                    insert(googleFitSummary, date);
                    InputHistory googleFitInput = new InputHistory();
                    googleFitInput.setDate(date);
                    googleFitInput.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    googleFitInput.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                    googleFitInput.setInputType(application.getString(R.string.googlefit_camel_case));
                    inputHistoryRepository.insert(googleFitInput);
                    firebaseManager.getDaysWithDataRef().child(date).child(application.getString(R.string.googlefit_camel_case)).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
