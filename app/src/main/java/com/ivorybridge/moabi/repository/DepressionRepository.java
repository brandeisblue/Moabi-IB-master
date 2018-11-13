package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.Phq9Dao;
import com.ivorybridge.moabi.database.db.EBDDB;
import com.ivorybridge.moabi.database.entity.depression.DailyPhq9;
import com.ivorybridge.moabi.database.entity.depression.MonthlyPhq9;
import com.ivorybridge.moabi.database.entity.depression.Phq9;
import com.ivorybridge.moabi.database.entity.depression.WeeklyPhq9;
import com.ivorybridge.moabi.database.entity.util.InputHistory;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.util.FormattedTime;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class DepressionRepository {

    private static final String TAG = DepressionRepository.class.getSimpleName();
    private Phq9Dao phq9Dao;
    private Application application;
    private FirebaseManager firebaseManager;
    private InputHistoryRepository inputHistoryRepository;
    private FormattedTime formattedTime;

    public DepressionRepository(Application application) {
        this.firebaseManager = new FirebaseManager();
        EBDDB db = EBDDB.getDatabase(application);
        phq9Dao = db.phq9Dao();
        inputHistoryRepository = new InputHistoryRepository(application);
        formattedTime = new FormattedTime();
        this.application = application;
    }

    // wrapper for getDailySummary
    public LiveData<List<Phq9>> getAllEntries() {
        return phq9Dao.getAll();
    }

    public LiveData<Phq9> get(Long dateInLong) {
        return phq9Dao.get(dateInLong);
    }

    public LiveData<List<Phq9>> getEntries(Long start, Long end) {
        return phq9Dao.getAll(start, end);
    }

    public LiveData<List<DailyPhq9>> getDailyPhq9s(Long start, Long end) {
        return phq9Dao.getAllDailyEntries(start, end);
    }

    public List<DailyPhq9> getDailyPhq9sNow(Long start, Long end) {
        return phq9Dao.getAllDailyEntriesNow(start, end);
    }

    public LiveData<List<WeeklyPhq9>> getWeeklyPhq9s(Long start, Long end) {
        return phq9Dao.getAllWeeklyEntries(start, end);
    }

    public LiveData<List<MonthlyPhq9>> getMonthlyPhq9s(Long start, Long end) {
        return phq9Dao.getAllMonthlyEntries(start, end);
    }

    public void sync() {
        firebaseManager.getPhq9Ref().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dailySnap : dataSnapshot.getChildren()) {
                    if (dailySnap.hasChildren() && dailySnap.getKey() != null) {
                        String date = dailySnap.getKey();
                        for (DataSnapshot timeSnap : dailySnap.getChildren()) {
                            if (timeSnap.getKey() != null) {
                                String dateTimeToConvert = date + " " + timeSnap.getKey();
                                long dateInLong = formattedTime.convertStringYYYYMMDDhhmmToLong(dateTimeToConvert);
                                Phq9 entry = new Phq9();
                                entry.setScore((Long) timeSnap.getValue());
                                entry.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                entry.setDateInLong(dateInLong);
                                Log.i(TAG, dateTimeToConvert + ": " + entry.getScore() + ", ");
                                insert(entry, date);
                            }
                        }
                        firebaseManager.getDaysWithDataRef().child(date).child(application.getString(R.string.depression_phq9_camel_case)).setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean insert(Phq9 phq9, String date) {
        AsyncTask.Status status = new insertAsyncTask(phq9Dao).execute(phq9).getStatus();
        InputHistory inputHistory = new InputHistory();
        inputHistory.setInputType(application.getString(R.string.depression_phq9_camel_case));
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

    public boolean processPhq9(List<Phq9> phq9List) {
        AsyncTask.Status status = new processPhq9AsyncTask(this, phq9List).execute().getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class processPhq9AsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<DepressionRepository> weakReference;
        private List<Phq9> phq9List;

        processPhq9AsyncTask(DepressionRepository context, List<Phq9> phq9List) {
            this.weakReference = new WeakReference<>(context);
            this.phq9List = phq9List;
        }

        @Override
        protected Void doInBackground(final Void... args) {
            DepressionRepository repository = weakReference.get();
            repository.processRawData(phq9List);
            return null;
        }
    }

    private void processRawData(List<Phq9> phq9List) {
        if (phq9List != null) {
            if (phq9List.size() > 0) {
                Log.i(TAG, phq9List.toString());
                List<String> entryDatesList = new ArrayList<>();
                Map<String, Long> numDailyEntries = new LinkedHashMap<>();
                Map<String, Float> phq9DailyEntries = new LinkedHashMap<>();
                Map<String, Long> numWeeklyEntries = new LinkedHashMap<>();
                Map<String, Float> phq9WeeklyEntries = new LinkedHashMap<>();
                Map<String, Long> numMonthlyEntries = new LinkedHashMap<>();
                Map<String, Float> phq9MonthlyEntries = new LinkedHashMap<>();

                for (Phq9 entry: phq9List) {
                    String date = formattedTime.convertLongToYYYYMMDD(entry.getDateInLong());
                    String weekDate = formattedTime.convertLongToYYYYW(entry.getDateInLong());
                    Log.i(TAG, weekDate);
                    String monthDate = formattedTime.convertLongToYYYYMM(entry.getDateInLong());
                    if (phq9DailyEntries.get(date) != null) {
                        Float old = phq9DailyEntries.get(date);
                        phq9DailyEntries.put(date, old + entry.getScore().floatValue());
                    } else {
                        phq9DailyEntries.put(date, entry.getScore().floatValue());
                    }
                    if (numDailyEntries.get(date) != null) {
                        Long counter = numDailyEntries.get(date);
                        numDailyEntries.put(date, counter + 1L);
                    } else {
                        numDailyEntries.put(date, 1L);
                    }
                    if (phq9WeeklyEntries.get(weekDate) != null) {
                        Float old = phq9WeeklyEntries.get(weekDate);
                        phq9WeeklyEntries.put(weekDate, old + entry.getScore().floatValue());
                    } else {
                        phq9WeeklyEntries.put(weekDate, entry.getScore().floatValue());
                    }
                    if (numWeeklyEntries.get(weekDate) != null) {
                        Long counter = numWeeklyEntries.get(weekDate);
                        numWeeklyEntries.put(weekDate, counter + 1L);
                    } else {
                        numWeeklyEntries.put(weekDate, 1L);
                    }
                    if (phq9MonthlyEntries.get(monthDate) != null) {
                        Float old = phq9MonthlyEntries.get(monthDate);
                        phq9MonthlyEntries.put(monthDate, old + entry.getScore().floatValue());
                    } else {
                        phq9MonthlyEntries.put(monthDate, entry.getScore().floatValue());
                    }
                    if (numMonthlyEntries.get(monthDate) != null) {
                        Long counter = numMonthlyEntries.get(monthDate);
                        numMonthlyEntries.put(monthDate, counter + 1L);
                    } else {
                        numMonthlyEntries.put(monthDate, 1L);
                    }
                }

                for (Map.Entry<String, Long> numEntry: numDailyEntries.entrySet()) {
                    String date = numEntry.getKey();
                    Long numEntries = numEntry.getValue();
                    if (phq9DailyEntries.get(date) != null) {
                        phq9DailyEntries.put(date, phq9DailyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Long> numEntry: numWeeklyEntries.entrySet()) {
                    String date = numEntry.getKey();
                    Long numEntries = numEntry.getValue();
                    Log.i(TAG, date);
                    if (phq9WeeklyEntries.get(date) != null) {
                        phq9WeeklyEntries.put(date, phq9WeeklyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Long> numEntry: numMonthlyEntries.entrySet()) {
                    String date = numEntry.getKey();
                    Long numEntries = numEntry.getValue();
                    Log.i(TAG, date);
                    if (phq9MonthlyEntries.get(date) != null) {
                        phq9MonthlyEntries.put(date, phq9MonthlyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Float> phq9Entry: phq9DailyEntries.entrySet()) {
                    String date = phq9Entry.getKey();
                    Float averagePhq9 = phq9Entry.getValue();
                    DailyPhq9 phq9 = new DailyPhq9();
                    phq9.setDate(date);
                    phq9.setAverageScore(averagePhq9.doubleValue());
                    phq9.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                    phq9.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    phq9.setNumOfEntries(numDailyEntries.get(date));
                    insertDailyPhq9(phq9);
                }

                for (Map.Entry<String, Float> phq9Entry: phq9WeeklyEntries.entrySet()) {
                    String date = phq9Entry.getKey();
                    Float averagePhq9 = phq9Entry.getValue();
                    WeeklyPhq9 phq9 = new WeeklyPhq9();
                    phq9.setYYYYW(date);
                    phq9.setAverageScore(averagePhq9.doubleValue());
                    int year = Integer.parseInt(date.substring(0, 4));
                    int week = Integer.parseInt(date.substring(5));
                    Long startTime = formattedTime.getStartTimeOfYYYYW(week, year);
                    Long endTime = formattedTime.getEndTimeOfYYYYW(week, year);
                    phq9.setStartDateInLong(startTime);
                    phq9.setEndDateInLong(endTime);
                    Log.i(TAG,startTime + ", " + endTime);
                    phq9.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    phq9.setNumOfEntries(numWeeklyEntries.get(date));
                    insertWeeklyPhq9(phq9);
                }

                for (Map.Entry<String, Float> phq9Entry: phq9MonthlyEntries.entrySet()) {
                    String date = phq9Entry.getKey();
                    Float averagePhq9 = phq9Entry.getValue();
                    MonthlyPhq9 phq9 = new MonthlyPhq9();
                    phq9.setYYYYMM(date);
                    phq9.setAverageScore(averagePhq9.doubleValue());
                    Long startTime = formattedTime.getStartTimeOfYYYYMM(date);
                    Long endTime = formattedTime.getEndTimeOfYYYYMM(date);
                    phq9.setStartDateInLong(startTime);
                    phq9.setEndDateInLong(endTime);
                    Log.i(TAG,startTime + ", " + endTime);
                    phq9.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    phq9.setNumOfEntries(numMonthlyEntries.get(date));
                    insertMonthlyPhq9(phq9);
                }
            }
        }
    }

    private static class insertAsyncTask extends AsyncTask<Phq9, Void, Void> {

        private Phq9Dao mAsyncTaskDao;

        insertAsyncTask(Phq9Dao phq9Dao) {
            mAsyncTaskDao = phq9Dao;
        }

        @Override
        protected synchronized Void doInBackground(final Phq9... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private boolean insertDailyPhq9(DailyPhq9 dailyPhq9) {
        AsyncTask.Status status = new insertDailyPhq9AsyncTask(phq9Dao).execute(dailyPhq9).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertDailyPhq9AsyncTask extends AsyncTask<DailyPhq9, Void, Void> {

        private Phq9Dao mAsyncTaskDao;

        insertDailyPhq9AsyncTask(Phq9Dao phq9Dao) {
            mAsyncTaskDao = phq9Dao;
        }

        @Override
        protected Void doInBackground(DailyPhq9... dailyPhq9s) {
            mAsyncTaskDao.insert(dailyPhq9s[0]);
            return null;
        }
    }

    private boolean insertWeeklyPhq9(WeeklyPhq9 weeklyPhq9) {
        AsyncTask.Status status = new insertWeeklyPhq9AsyncTask(phq9Dao).execute(weeklyPhq9).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertWeeklyPhq9AsyncTask extends AsyncTask<WeeklyPhq9, Void, Void> {

        private Phq9Dao mAsyncTaskDao;

        insertWeeklyPhq9AsyncTask(Phq9Dao phq9Dao) {
            mAsyncTaskDao = phq9Dao;
        }

        @Override
        protected Void doInBackground(WeeklyPhq9... weeklyPhq9s) {
            mAsyncTaskDao.insert(weeklyPhq9s[0]);
            return null;
        }
    }

    private boolean insertMonthlyPhq9(MonthlyPhq9 monthlyPhq9) {
        AsyncTask.Status status = new insertMonthlyPhq9AsyncTask(phq9Dao).execute(monthlyPhq9).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertMonthlyPhq9AsyncTask extends AsyncTask<MonthlyPhq9, Void, Void> {

        private Phq9Dao mAsyncTaskDao;

        insertMonthlyPhq9AsyncTask(Phq9Dao phq9Dao) {
            mAsyncTaskDao = phq9Dao;
        }

        @Override
        protected Void doInBackground(MonthlyPhq9... monthlyPhq9s) {
            mAsyncTaskDao.insert(monthlyPhq9s[0]);
            return null;
        }
    }
}

