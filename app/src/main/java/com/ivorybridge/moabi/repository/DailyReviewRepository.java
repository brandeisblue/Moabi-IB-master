package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.DailyReviewDao;
import com.ivorybridge.moabi.database.db.DailyReviewDB;
import com.ivorybridge.moabi.database.entity.dailyreview.DailyDailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.DailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.MonthlyDailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.WeeklyDailyReview;
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

public class DailyReviewRepository {

    private static final String TAG = DailyReviewRepository.class.getSimpleName();
    private DailyReviewDao mDao;
    private Application application;
    private LiveData<List<DailyReview>> mDailyReviewDailySummaries;
    private FirebaseManager firebaseManager;
    private InputHistoryRepository inputHistoryRepository;
    private FormattedTime formattedTime;

    public DailyReviewRepository(Application application) {
        this.firebaseManager = new FirebaseManager();
        DailyReviewDB db = DailyReviewDB.getDatabase(application);
        mDao = db.dao();
        inputHistoryRepository = new InputHistoryRepository(application);
        formattedTime = new FormattedTime();
        this.application = application;
    }

    // wrapper for getDailySummary
    public LiveData<List<DailyReview>> getAllEntries() {
        return mDao.getAll();
    }

    public LiveData<DailyReview> get(Long dateInLong) {
        return mDao.get(dateInLong);
    }

    public LiveData<List<DailyReview>> getEntries(Long start, Long end) {
        return mDao.getAll(start, end);
    }

    public LiveData<List<DailyDailyReview>> getDailyDailyReviews(Long start, Long end) {
        return mDao.getAllDailyEntries(start, end);
    }

    public List<DailyDailyReview> getDailyDailyReviewsNow(Long start, Long end) {
        return mDao.getAllDailyEntriesNow(start, end);
    }

    public LiveData<List<WeeklyDailyReview>> getWeeklyDailyReviews(Long start, Long end) {
        return mDao.getAllWeeklyEntries(start, end);
    }

    public LiveData<List<MonthlyDailyReview>> getMonthlyDailyReviews(Long start, Long end) {
        return mDao.getAllMonthlyEntries(start, end);
    }

    public void sync() {
        firebaseManager.getDailyReviewRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dailySnap : dataSnapshot.getChildren()) {
                    if (dailySnap.hasChildren() && dailySnap.getKey() != null) {
                        String date = dailySnap.getKey();
                        for (DataSnapshot timeSnap : dailySnap.getChildren()) {
                            if (timeSnap.getKey() != null) {
                                String dateTimeToConvert = date + " " + timeSnap.getKey();
                                long dateInLong = formattedTime.convertStringYYYYMMDDhhmmToLong(dateTimeToConvert);
                                DailyReview entry = new DailyReview();
                                entry.setDailyReview((Long) timeSnap.child(application.getString(R.string.daily_review_camel_case)).getValue());
                                entry.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                entry.setDateInLong(dateInLong);
                                Log.i(TAG, dateTimeToConvert + ": " + entry.getDailyReview() + ", ");
                                insert(entry, date);
                            }
                        }
                        firebaseManager.getDaysWithDataRef().child(date).child(application.getString(R.string.daily_review_camel_case)).setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean insert(DailyReview dailyReview, String date) {
        AsyncTask.Status status = new insertAsyncTask(mDao).execute(dailyReview).getStatus();
        InputHistory inputHistory = new InputHistory();
        inputHistory.setInputType(application.getString(R.string.daily_review_camel_case));
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

    public boolean processDailyReview(List<DailyReview> dailyReviewList) {
        AsyncTask.Status status = new processDailyReviewAsyncTask(this, dailyReviewList).execute().getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class processDailyReviewAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<DailyReviewRepository> weakReference;
        private List<DailyReview> dailyReviewList;

        processDailyReviewAsyncTask(DailyReviewRepository context, List<DailyReview> dailyReviewList) {
            this.weakReference = new WeakReference<>(context);
            this.dailyReviewList = dailyReviewList;
        }

        @Override
        protected Void doInBackground(final Void... args) {
            DailyReviewRepository repository = weakReference.get();
            repository.processRawData(dailyReviewList);
            return null;
        }
    }

    private void processRawData(List<DailyReview> dailyReviewList) {
        if (dailyReviewList != null) {
            if (dailyReviewList.size() > 0) {
                Log.i(TAG, dailyReviewList.toString());
                List<String> entryDatesList = new ArrayList<>();
                Map<String, Long> numDailyEntries = new LinkedHashMap<>();
                Map<String, Float> dailyReviewDailyEntries = new LinkedHashMap<>();
                Map<String, Long> numWeeklyEntries = new LinkedHashMap<>();
                Map<String, Float> dailyReviewWeeklyEntries = new LinkedHashMap<>();
                Map<String, Long> numMonthlyEntries = new LinkedHashMap<>();
                Map<String, Float> dailyReviewMonthlyEntries = new LinkedHashMap<>();

                for (DailyReview entry : dailyReviewList) {
                    String date = formattedTime.convertLongToYYYYMMDD(entry.getDateInLong());
                    String weekDate = formattedTime.convertLongToYYYYW(entry.getDateInLong());
                    Log.i(TAG, weekDate);
                    String monthDate = formattedTime.convertLongToYYYYMM(entry.getDateInLong());
                    if (dailyReviewDailyEntries.get(date) != null) {
                        Float old = dailyReviewDailyEntries.get(date);
                        dailyReviewDailyEntries.put(date, old + entry.getDailyReview().floatValue());
                    } else {
                        dailyReviewDailyEntries.put(date, entry.getDailyReview().floatValue());
                    }
                    if (numDailyEntries.get(date) != null) {
                        Long counter = numDailyEntries.get(date);
                        numDailyEntries.put(date, counter + 1L);
                    } else {
                        numDailyEntries.put(date, 1L);
                    }
                    if (dailyReviewWeeklyEntries.get(weekDate) != null) {
                        Float old = dailyReviewWeeklyEntries.get(weekDate);
                        dailyReviewWeeklyEntries.put(weekDate, old + entry.getDailyReview().floatValue());
                    } else {
                        dailyReviewWeeklyEntries.put(weekDate, entry.getDailyReview().floatValue());
                    }
                    if (numWeeklyEntries.get(weekDate) != null) {
                        Long counter = numWeeklyEntries.get(weekDate);
                        numWeeklyEntries.put(weekDate, counter + 1L);
                    } else {
                        numWeeklyEntries.put(weekDate, 1L);
                    }
                    if (dailyReviewMonthlyEntries.get(monthDate) != null) {
                        Float old = dailyReviewMonthlyEntries.get(monthDate);
                        dailyReviewMonthlyEntries.put(monthDate, old + entry.getDailyReview().floatValue());
                    } else {
                        dailyReviewMonthlyEntries.put(monthDate, entry.getDailyReview().floatValue());
                    }
                    if (numMonthlyEntries.get(monthDate) != null) {
                        Long counter = numMonthlyEntries.get(monthDate);
                        numMonthlyEntries.put(monthDate, counter + 1L);
                    } else {
                        numMonthlyEntries.put(monthDate, 1L);
                    }
                }

                for (Map.Entry<String, Long> numEntry : numDailyEntries.entrySet()) {
                    String date = numEntry.getKey();
                    Long numEntries = numEntry.getValue();
                    if (dailyReviewDailyEntries.get(date) != null) {
                        dailyReviewDailyEntries.put(date, dailyReviewDailyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Long> numEntry : numWeeklyEntries.entrySet()) {
                    String date = numEntry.getKey();
                    Long numEntries = numEntry.getValue();
                    Log.i(TAG, date);
                    if (dailyReviewWeeklyEntries.get(date) != null) {
                        dailyReviewWeeklyEntries.put(date, dailyReviewWeeklyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Long> numEntry : numMonthlyEntries.entrySet()) {
                    String date = numEntry.getKey();
                    Long numEntries = numEntry.getValue();
                    Log.i(TAG, date);
                    if (dailyReviewMonthlyEntries.get(date) != null) {
                        dailyReviewMonthlyEntries.put(date, dailyReviewMonthlyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Float> dailyReviewEntry : dailyReviewDailyEntries.entrySet()) {
                    String date = dailyReviewEntry.getKey();
                    Float averageDailyReview = dailyReviewEntry.getValue();
                    DailyDailyReview dailyReview = new DailyDailyReview();
                    dailyReview.setDate(date);
                    dailyReview.setAverageDailyReview(averageDailyReview.doubleValue());
                    dailyReview.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                    dailyReview.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    dailyReview.setNumOfEntries(numDailyEntries.get(date));
                    insertDailyDailyReview(dailyReview);
                }

                for (Map.Entry<String, Float> dailyReviewEntry : dailyReviewWeeklyEntries.entrySet()) {
                    String date = dailyReviewEntry.getKey();
                    Float averageDailyReview = dailyReviewEntry.getValue();
                    WeeklyDailyReview dailyReview = new WeeklyDailyReview();
                    dailyReview.setYYYYW(date);
                    dailyReview.setAverageDailyReview(averageDailyReview.doubleValue());
                    int year = Integer.parseInt(date.substring(0, 4));
                    int week = Integer.parseInt(date.substring(5));
                    Long startTime = formattedTime.getStartTimeOfYYYYW(week, year);
                    Long endTime = formattedTime.getEndTimeOfYYYYW(week, year);
                    dailyReview.setStartDateInLong(startTime);
                    dailyReview.setEndDateInLong(endTime);
                    Log.i(TAG, startTime + ", " + endTime);
                    dailyReview.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    dailyReview.setNumOfEntries(numWeeklyEntries.get(date));
                    insertWeeklyDailyReview(dailyReview);
                }

                for (Map.Entry<String, Float> dailyReviewEntry : dailyReviewMonthlyEntries.entrySet()) {
                    String date = dailyReviewEntry.getKey();
                    Float averageDailyReview = dailyReviewEntry.getValue();
                    MonthlyDailyReview dailyReview = new MonthlyDailyReview();
                    dailyReview.setYYYYMM(date);
                    dailyReview.setAverageDailyReview(averageDailyReview.doubleValue());
                    Long startTime = formattedTime.getStartTimeOfYYYYMM(date);
                    Long endTime = formattedTime.getEndTimeOfYYYYMM(date);
                    dailyReview.setStartDateInLong(startTime);
                    dailyReview.setEndDateInLong(endTime);
                    Log.i(TAG, startTime + ", " + endTime);
                    dailyReview.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    dailyReview.setNumOfEntries(numMonthlyEntries.get(date));
                    insertMonthlyDailyReview(dailyReview);
                }
            }
        }
    }

    private static class insertAsyncTask extends AsyncTask<DailyReview, Void, Void> {

        private DailyReviewDao mAsyncTaskDao;

        insertAsyncTask(DailyReviewDao mDao) {
            mAsyncTaskDao = mDao;
        }

        @Override
        protected synchronized Void doInBackground(final DailyReview... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private boolean insertDailyDailyReview(DailyDailyReview dailyDailyReview) {
        AsyncTask.Status status = new insertDailyDailyReviewAsyncTask(mDao).execute(dailyDailyReview).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertDailyDailyReviewAsyncTask extends AsyncTask<DailyDailyReview, Void, Void> {

        private DailyReviewDao mAsyncTaskDao;

        insertDailyDailyReviewAsyncTask(DailyReviewDao mDao) {
            mAsyncTaskDao = mDao;
        }

        @Override
        protected Void doInBackground(DailyDailyReview... dailyDailyReviews) {
            mAsyncTaskDao.insert(dailyDailyReviews[0]);
            return null;
        }
    }

    private boolean insertWeeklyDailyReview(WeeklyDailyReview weeklyDailyReview) {
        AsyncTask.Status status = new insertWeeklyDailyReviewAsyncTask(mDao).execute(weeklyDailyReview).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertWeeklyDailyReviewAsyncTask extends AsyncTask<WeeklyDailyReview, Void, Void> {

        private DailyReviewDao mAsyncTaskDao;

        insertWeeklyDailyReviewAsyncTask(DailyReviewDao mDao) {
            mAsyncTaskDao = mDao;
        }

        @Override
        protected Void doInBackground(WeeklyDailyReview... weeklyDailyReviews) {
            mAsyncTaskDao.insert(weeklyDailyReviews[0]);
            return null;
        }
    }

    private boolean insertMonthlyDailyReview(MonthlyDailyReview monthlyDailyReview) {
        AsyncTask.Status status = new insertMonthlyDailyReviewAsyncTask(mDao).execute(monthlyDailyReview).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertMonthlyDailyReviewAsyncTask extends AsyncTask<MonthlyDailyReview, Void, Void> {

        private DailyReviewDao mAsyncTaskDao;

        insertMonthlyDailyReviewAsyncTask(DailyReviewDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(MonthlyDailyReview... monthlyDailyReviews) {
            mAsyncTaskDao.insert(monthlyDailyReviews[0]);
            return null;
        }
    }
}
