package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.StressDao;
import com.ivorybridge.moabi.database.db.StressDB;
import com.ivorybridge.moabi.database.entity.stress.DailyStress;
import com.ivorybridge.moabi.database.entity.stress.MonthlyStress;
import com.ivorybridge.moabi.database.entity.stress.Stress;
import com.ivorybridge.moabi.database.entity.stress.WeeklyStress;
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

public class StressRepository {

    private static final String TAG = StressRepository.class.getSimpleName();
    private StressDao mDao;
    private Application application;
    private LiveData<List<Stress>> mStressDailySummaries;
    private FirebaseManager firebaseManager;
    private InputHistoryRepository inputHistoryRepository;
    private FormattedTime formattedTime;

    public StressRepository(Application application) {
        this.firebaseManager = new FirebaseManager();
        StressDB db = StressDB.getDatabase(application);
        mDao = db.dao();
        inputHistoryRepository = new InputHistoryRepository(application);
        formattedTime = new FormattedTime();
        this.application = application;
    }

    // wrapper for getDailySummary
    public LiveData<List<Stress>> getAllEntries() {
        return mDao.getAll();
    }

    public LiveData<Stress> get(Long dateInLong) {
        return mDao.get(dateInLong);
    }

    public LiveData<List<Stress>> getEntries(Long start, Long end) {
        return mDao.getAll(start, end);
        
    }

    public LiveData<List<DailyStress>> getDailyStresss(Long start, Long end) {
        return mDao.getAllDailyEntries(start, end);
    }

    public List<DailyStress> getDailyStresssNow(Long start, Long end) {
        return mDao.getAllDailyEntriesNow(start, end);
    }

    public LiveData<List<WeeklyStress>> getWeeklyStresss(Long start, Long end) {
        return mDao.getAllWeeklyEntries(start, end);
    }

    public LiveData<List<MonthlyStress>> getMonthlyStresss(Long start, Long end) {
        return mDao.getAllMonthlyEntries(start, end);
    }

    public void sync() {
        firebaseManager.getStressRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dailySnap : dataSnapshot.getChildren()) {
                    if (dailySnap.hasChildren() && dailySnap.getKey() != null) {
                        String date = dailySnap.getKey();
                        for (DataSnapshot timeSnap : dailySnap.getChildren()) {
                            if (timeSnap.getKey() != null) {
                                String dateTimeToConvert = date + " " + timeSnap.getKey();
                                long dateInLong = formattedTime.convertStringYYYYMMDDhhmmToLong(dateTimeToConvert);
                                Stress entry = new Stress();
                                entry.setStress((Double) timeSnap.child(application.getString(R.string.stress_camel_case)).getValue());
                                entry.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                entry.setDateInLong(dateInLong);
                                Log.i(TAG, dateTimeToConvert + ": " + entry.getStress() + ", ");
                                insert(entry, date);
                            }
                        }
                        firebaseManager.getDaysWithDataRef().child(date).child(application.getString(R.string.stress_camel_case)).setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean insert(Stress stress, String date) {
        AsyncTask.Status status = new insertAsyncTask(mDao).execute(stress).getStatus();
        InputHistory inputHistory = new InputHistory();
        inputHistory.setInputType(application.getString(R.string.stress_camel_case));
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

    public boolean processStress(List<Stress> stressList) {
        AsyncTask.Status status = new processStressAsyncTask(this, stressList).execute().getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class processStressAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<StressRepository> weakReference;
        private List<Stress> stressList;

        processStressAsyncTask(StressRepository context, List<Stress> stressList) {
            this.weakReference = new WeakReference<>(context);
            this.stressList = stressList;
        }

        @Override
        protected Void doInBackground(final Void... args) {
            StressRepository repository = weakReference.get();
            repository.processRawData(stressList);
            return null;
        }
    }

    private void processRawData(List<Stress> stressList) {
        if (stressList != null) {
            if (stressList.size() > 0) {
                Log.i(TAG, stressList.toString());
                List<String> entryDatesList = new ArrayList<>();
                Map<String, Long> numDailyEntries = new LinkedHashMap<>();
                Map<String, Float> stressDailyEntries = new LinkedHashMap<>();
                Map<String, Long> numWeeklyEntries = new LinkedHashMap<>();
                Map<String, Float> stressWeeklyEntries = new LinkedHashMap<>();
                Map<String, Long> numMonthlyEntries = new LinkedHashMap<>();
                Map<String, Float> stressMonthlyEntries = new LinkedHashMap<>();

                for (Stress entry: stressList) {
                    String date = formattedTime.convertLongToYYYYMMDD(entry.getDateInLong());
                    String weekDate = formattedTime.convertLongToYYYYW(entry.getDateInLong());
                    Log.i(TAG, weekDate);
                    String monthDate = formattedTime.convertLongToYYYYMM(entry.getDateInLong());
                    if (stressDailyEntries.get(date) != null) {
                        Float old = stressDailyEntries.get(date);
                        stressDailyEntries.put(date, old + entry.getStress().floatValue());
                    } else {
                        stressDailyEntries.put(date, entry.getStress().floatValue());
                    }
                    if (numDailyEntries.get(date) != null) {
                        Long counter = numDailyEntries.get(date);
                        numDailyEntries.put(date, counter + 1L);
                    } else {
                        numDailyEntries.put(date, 1L);
                    }
                    if (stressWeeklyEntries.get(weekDate) != null) {
                        Float old = stressWeeklyEntries.get(weekDate);
                        stressWeeklyEntries.put(weekDate, old + entry.getStress().floatValue());
                    } else {
                        stressWeeklyEntries.put(weekDate, entry.getStress().floatValue());
                    }
                    if (numWeeklyEntries.get(weekDate) != null) {
                        Long counter = numWeeklyEntries.get(weekDate);
                        numWeeklyEntries.put(weekDate, counter + 1L);
                    } else {
                        numWeeklyEntries.put(weekDate, 1L);
                    }
                    if (stressMonthlyEntries.get(monthDate) != null) {
                        Float old = stressMonthlyEntries.get(monthDate);
                        stressMonthlyEntries.put(monthDate, old + entry.getStress().floatValue());
                    } else {
                        stressMonthlyEntries.put(monthDate, entry.getStress().floatValue());
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
                    if (stressDailyEntries.get(date) != null) {
                        stressDailyEntries.put(date, stressDailyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Long> numEntry: numWeeklyEntries.entrySet()) {
                    String date = numEntry.getKey();
                    Long numEntries = numEntry.getValue();
                    Log.i(TAG, date);
                    if (stressWeeklyEntries.get(date) != null) {
                        stressWeeklyEntries.put(date, stressWeeklyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Long> numEntry: numMonthlyEntries.entrySet()) {
                    String date = numEntry.getKey();
                    Long numEntries = numEntry.getValue();
                    Log.i(TAG, date);
                    if (stressMonthlyEntries.get(date) != null) {
                        stressMonthlyEntries.put(date, stressMonthlyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Float> stressEntry: stressDailyEntries.entrySet()) {
                    String date = stressEntry.getKey();
                    Float averageStress = stressEntry.getValue();
                    DailyStress stress = new DailyStress();
                    stress.setDate(date);
                    stress.setAverageStress(averageStress.doubleValue());
                    stress.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                    stress.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    stress.setNumOfEntries(numDailyEntries.get(date));
                    insertDailyStress(stress);
                }

                for (Map.Entry<String, Float> stressEntry: stressWeeklyEntries.entrySet()) {
                    String date = stressEntry.getKey();
                    Float averageStress = stressEntry.getValue();
                    WeeklyStress stress = new WeeklyStress();
                    stress.setYYYYW(date);
                    stress.setAverageStress(averageStress.doubleValue());
                    int year = Integer.parseInt(date.substring(0, 4));
                    int week = Integer.parseInt(date.substring(5));
                    Long startTime = formattedTime.getStartTimeOfYYYYW(week, year);
                    Long endTime = formattedTime.getEndTimeOfYYYYW(week, year);
                    stress.setStartDateInLong(startTime);
                    stress.setEndDateInLong(endTime);
                    Log.i(TAG,startTime + ", " + endTime);
                    stress.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    stress.setNumOfEntries(numWeeklyEntries.get(date));
                    insertWeeklyStress(stress);
                }

                for (Map.Entry<String, Float> stressEntry: stressMonthlyEntries.entrySet()) {
                    String date = stressEntry.getKey();
                    Float averageStress = stressEntry.getValue();
                    MonthlyStress stress = new MonthlyStress();
                    stress.setYYYYMM(date);
                    stress.setAverageStress(averageStress.doubleValue());
                    Long startTime = formattedTime.getStartTimeOfYYYYMM(date);
                    Long endTime = formattedTime.getEndTimeOfYYYYMM(date);
                    stress.setStartDateInLong(startTime);
                    stress.setEndDateInLong(endTime);
                    Log.i(TAG,startTime + ", " + endTime);
                    stress.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    stress.setNumOfEntries(numMonthlyEntries.get(date));
                    insertMonthlyStress(stress);
                }
            }
        }
    }

    private static class insertAsyncTask extends AsyncTask<Stress, Void, Void> {

        private StressDao mAsyncTaskDao;

        insertAsyncTask(StressDao mDao) {
            mAsyncTaskDao = mDao;
        }

        @Override
        protected synchronized Void doInBackground(final Stress... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private boolean insertDailyStress(DailyStress dailyStress) {
        AsyncTask.Status status = new insertDailyStressAsyncTask(mDao).execute(dailyStress).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertDailyStressAsyncTask extends AsyncTask<DailyStress, Void, Void> {

        private StressDao mAsyncTaskDao;

        insertDailyStressAsyncTask(StressDao mDao) {
            mAsyncTaskDao = mDao;
        }

        @Override
        protected Void doInBackground(DailyStress... dailyStresss) {
            mAsyncTaskDao.insert(dailyStresss[0]);
            return null;
        }
    }

    private boolean insertWeeklyStress(WeeklyStress weeklyStress) {
        AsyncTask.Status status = new insertWeeklyStressAsyncTask(mDao).execute(weeklyStress).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertWeeklyStressAsyncTask extends AsyncTask<WeeklyStress, Void, Void> {

        private StressDao mAsyncTaskDao;

        insertWeeklyStressAsyncTask(StressDao mDao) {
            mAsyncTaskDao = mDao;
        }

        @Override
        protected Void doInBackground(WeeklyStress... weeklyStresss) {
            mAsyncTaskDao.insert(weeklyStresss[0]);
            return null;
        }
    }

    private boolean insertMonthlyStress(MonthlyStress monthlyStress) {
        AsyncTask.Status status = new insertMonthlyStressAsyncTask(mDao).execute(monthlyStress).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertMonthlyStressAsyncTask extends AsyncTask<MonthlyStress, Void, Void> {

        private StressDao mAsyncTaskDao;

        insertMonthlyStressAsyncTask(StressDao mDao) {
            mAsyncTaskDao = mDao;
        }

        @Override
        protected Void doInBackground(MonthlyStress... monthlyStresss) {
            mAsyncTaskDao.insert(monthlyStresss[0]);
            return null;
        }
    }
}
