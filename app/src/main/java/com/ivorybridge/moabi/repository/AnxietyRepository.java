package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.Gad7Dao;
import com.ivorybridge.moabi.database.db.EBDDB;
import com.ivorybridge.moabi.database.entity.anxiety.DailyGad7;
import com.ivorybridge.moabi.database.entity.anxiety.Gad7;
import com.ivorybridge.moabi.database.entity.anxiety.MonthlyGad7;
import com.ivorybridge.moabi.database.entity.anxiety.WeeklyGad7;
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

public class AnxietyRepository {

    private static final String TAG = AnxietyRepository.class.getSimpleName();
    private Gad7Dao gad7Dao;
    private Application application;
    private FirebaseManager firebaseManager;
    private InputHistoryRepository inputHistoryRepository;
    private FormattedTime formattedTime;

    public AnxietyRepository(Application application) {
        this.firebaseManager = new FirebaseManager();
        this.application = application;
        EBDDB db = EBDDB.getDatabase(application);
        gad7Dao = db.gad7Dao();
        inputHistoryRepository = new InputHistoryRepository(application);
        formattedTime = new FormattedTime();
        this.application = application;
    }

    // wrapper for getDailySummary
    public LiveData<List<Gad7>> getAllEntries() {
        return gad7Dao.getAll();
    }

    public LiveData<Gad7> get(Long dateInLong) {
        return gad7Dao.get(dateInLong);
    }

    public LiveData<List<Gad7>> getEntries(Long start, Long end) {
        return gad7Dao.getAll(start, end);
    }

    public LiveData<List<DailyGad7>> getDailyGad7s(Long start, Long end) {
        return gad7Dao.getAllDailyEntries(start, end);
    }

    public List<DailyGad7> getDailyGad7sNow(Long start, Long end) {
        return gad7Dao.getAllDailyEntriesNow(start, end);
    }

    public LiveData<List<WeeklyGad7>> getWeeklyGad7s(Long start, Long end) {
        return gad7Dao.getAllWeeklyEntries(start, end);
    }

    public LiveData<List<MonthlyGad7>> getMonthlyGad7s(Long start, Long end) {
        return gad7Dao.getAllMonthlyEntries(start, end);
    }

    public void sync() {
        firebaseManager.getGad7Ref().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dailySnap : dataSnapshot.getChildren()) {
                    if (dailySnap.hasChildren() && dailySnap.getKey() != null) {
                        String date = dailySnap.getKey();
                        for (DataSnapshot timeSnap : dailySnap.getChildren()) {
                            if (timeSnap.getKey() != null) {
                                String dateTimeToConvert = date + " " + timeSnap.getKey();
                                long dateInLong = formattedTime.convertStringYYYYMMDDhhmmToLong(dateTimeToConvert);
                                Gad7 entry = new Gad7();
                                entry.setScore((Long) timeSnap.getValue());
                                entry.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                entry.setDateInLong(dateInLong);
                                Log.i(TAG, dateTimeToConvert + ": " + entry.getScore() + ", ");
                                insert(entry, date);
                            }
                        }
                        firebaseManager.getDaysWithDataRef().child(date).child(application.getString(R.string.anxiety_gad7_camel_case)).setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean insert(Gad7 gad7, String date) {
        AsyncTask.Status status = new insertAsyncTask(gad7Dao).execute(gad7).getStatus();
        InputHistory inputHistory = new InputHistory();
        inputHistory.setInputType(application.getString(R.string.anxiety_gad7_camel_case));
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

    public boolean processGad7(List<Gad7> gad7List) {
        AsyncTask.Status status = new processGad7AsyncTask(this, gad7List).execute().getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class processGad7AsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<AnxietyRepository> weakReference;
        private List<Gad7> gad7List;

        processGad7AsyncTask(AnxietyRepository context, List<Gad7> gad7List) {
            this.weakReference = new WeakReference<>(context);
            this.gad7List = gad7List;
        }

        @Override
        protected Void doInBackground(final Void... args) {
            AnxietyRepository repository = weakReference.get();
            repository.processRawData(gad7List);
            return null;
        }
    }

    private void processRawData(List<Gad7> gad7List) {
        if (gad7List != null) {
            if (gad7List.size() > 0) {
                Log.i(TAG, gad7List.toString());
                List<String> entryDatesList = new ArrayList<>();
                Map<String, Long> numDailyEntries = new LinkedHashMap<>();
                Map<String, Float> gad7DailyEntries = new LinkedHashMap<>();
                Map<String, Long> numWeeklyEntries = new LinkedHashMap<>();
                Map<String, Float> gad7WeeklyEntries = new LinkedHashMap<>();
                Map<String, Long> numMonthlyEntries = new LinkedHashMap<>();
                Map<String, Float> gad7MonthlyEntries = new LinkedHashMap<>();

                for (Gad7 entry: gad7List) {
                    String date = formattedTime.convertLongToYYYYMMDD(entry.getDateInLong());
                    String weekDate = formattedTime.convertLongToYYYYW(entry.getDateInLong());
                    Log.i(TAG, weekDate);
                    String monthDate = formattedTime.convertLongToYYYYMM(entry.getDateInLong());
                    if (gad7DailyEntries.get(date) != null) {
                        Float old = gad7DailyEntries.get(date);
                        gad7DailyEntries.put(date, old + entry.getScore().floatValue());
                    } else {
                        gad7DailyEntries.put(date, entry.getScore().floatValue());
                    }
                    if (numDailyEntries.get(date) != null) {
                        Long counter = numDailyEntries.get(date);
                        numDailyEntries.put(date, counter + 1L);
                    } else {
                        numDailyEntries.put(date, 1L);
                    }
                    if (gad7WeeklyEntries.get(weekDate) != null) {
                        Float old = gad7WeeklyEntries.get(weekDate);
                        gad7WeeklyEntries.put(weekDate, old + entry.getScore().floatValue());
                    } else {
                        gad7WeeklyEntries.put(weekDate, entry.getScore().floatValue());
                    }
                    if (numWeeklyEntries.get(weekDate) != null) {
                        Long counter = numWeeklyEntries.get(weekDate);
                        numWeeklyEntries.put(weekDate, counter + 1L);
                    } else {
                        numWeeklyEntries.put(weekDate, 1L);
                    }
                    if (gad7MonthlyEntries.get(monthDate) != null) {
                        Float old = gad7MonthlyEntries.get(monthDate);
                        gad7MonthlyEntries.put(monthDate, old + entry.getScore().floatValue());
                    } else {
                        gad7MonthlyEntries.put(monthDate, entry.getScore().floatValue());
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
                    if (gad7DailyEntries.get(date) != null) {
                        gad7DailyEntries.put(date, gad7DailyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Long> numEntry: numWeeklyEntries.entrySet()) {
                    String date = numEntry.getKey();
                    Long numEntries = numEntry.getValue();
                    Log.i(TAG, date);
                    if (gad7WeeklyEntries.get(date) != null) {
                        gad7WeeklyEntries.put(date, gad7WeeklyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Long> numEntry: numMonthlyEntries.entrySet()) {
                    String date = numEntry.getKey();
                    Long numEntries = numEntry.getValue();
                    Log.i(TAG, date);
                    if (gad7MonthlyEntries.get(date) != null) {
                        gad7MonthlyEntries.put(date, gad7MonthlyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Float> gad7Entry: gad7DailyEntries.entrySet()) {
                    String date = gad7Entry.getKey();
                    Float averagegad7 = gad7Entry.getValue();
                    DailyGad7 gad7 = new DailyGad7();
                    gad7.setDate(date);
                    gad7.setAverageScore(averagegad7.doubleValue());
                    gad7.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                    gad7.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    gad7.setNumOfEntries(numDailyEntries.get(date));
                    insertDailyGad7(gad7);
                }

                for (Map.Entry<String, Float> gad7Entry: gad7WeeklyEntries.entrySet()) {
                    String date = gad7Entry.getKey();
                    Float averagegad7 = gad7Entry.getValue();
                    WeeklyGad7 gad7 = new WeeklyGad7();
                    gad7.setYYYYW(date);
                    gad7.setAverageScore(averagegad7.doubleValue());
                    int year = Integer.parseInt(date.substring(0, 4));
                    int week = Integer.parseInt(date.substring(5));
                    Long startTime = formattedTime.getStartTimeOfYYYYW(week, year);
                    Long endTime = formattedTime.getEndTimeOfYYYYW(week, year);
                    gad7.setStartDateInLong(startTime);
                    gad7.setEndDateInLong(endTime);
                    Log.i(TAG,startTime + ", " + endTime);
                    gad7.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    gad7.setNumOfEntries(numWeeklyEntries.get(date));
                    insertWeeklyGad7(gad7);
                }

                for (Map.Entry<String, Float> gad7Entry: gad7MonthlyEntries.entrySet()) {
                    String date = gad7Entry.getKey();
                    Float averagegad7 = gad7Entry.getValue();
                    MonthlyGad7 gad7 = new MonthlyGad7();
                    gad7.setYYYYMM(date);
                    gad7.setAverageScore(averagegad7.doubleValue());
                    Long startTime = formattedTime.getStartTimeOfYYYYMM(date);
                    Long endTime = formattedTime.getEndTimeOfYYYYMM(date);
                    gad7.setStartDateInLong(startTime);
                    gad7.setEndDateInLong(endTime);
                    Log.i(TAG,startTime + ", " + endTime);
                    gad7.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    gad7.setNumOfEntries(numMonthlyEntries.get(date));
                    insertMonthlyGad7(gad7);
                }
            }
        }
    }

    private static class insertAsyncTask extends AsyncTask<Gad7, Void, Void> {

        private Gad7Dao mAsyncTaskDao;

        insertAsyncTask(Gad7Dao gad7Dao) {
            mAsyncTaskDao = gad7Dao;
        }

        @Override
        protected synchronized Void doInBackground(final Gad7... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private boolean insertDailyGad7(DailyGad7 dailyGad7) {
        AsyncTask.Status status = new insertDailyGad7AsyncTask(gad7Dao).execute(dailyGad7).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertDailyGad7AsyncTask extends AsyncTask<DailyGad7, Void, Void> {

        private Gad7Dao mAsyncTaskDao;

        insertDailyGad7AsyncTask(Gad7Dao gad7Dao) {
            mAsyncTaskDao = gad7Dao;
        }

        @Override
        protected Void doInBackground(DailyGad7... dailyGad7s) {
            mAsyncTaskDao.insert(dailyGad7s[0]);
            return null;
        }
    }

    private boolean insertWeeklyGad7(WeeklyGad7 weeklyGad7) {
        AsyncTask.Status status = new insertWeeklyGad7AsyncTask(gad7Dao).execute(weeklyGad7).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertWeeklyGad7AsyncTask extends AsyncTask<WeeklyGad7, Void, Void> {

        private Gad7Dao mAsyncTaskDao;

        insertWeeklyGad7AsyncTask(Gad7Dao gad7Dao) {
            mAsyncTaskDao = gad7Dao;
        }

        @Override
        protected Void doInBackground(WeeklyGad7... weeklyGad7s) {
            mAsyncTaskDao.insert(weeklyGad7s[0]);
            return null;
        }
    }

    private boolean insertMonthlyGad7(MonthlyGad7 monthlyGad7) {
        AsyncTask.Status status = new insertMonthlyGad7AsyncTask(gad7Dao).execute(monthlyGad7).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertMonthlyGad7AsyncTask extends AsyncTask<MonthlyGad7, Void, Void> {

        private Gad7Dao mAsyncTaskDao;

        insertMonthlyGad7AsyncTask(Gad7Dao gad7Dao) {
            mAsyncTaskDao = gad7Dao;
        }

        @Override
        protected Void doInBackground(MonthlyGad7... monthlyGad7s) {
            mAsyncTaskDao.insert(monthlyGad7s[0]);
            return null;
        }
    }
}

