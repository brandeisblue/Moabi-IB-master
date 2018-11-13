package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.EnergyDao;
import com.ivorybridge.moabi.database.dao.MoodAndEnergyDao;
import com.ivorybridge.moabi.database.dao.MoodDao;
import com.ivorybridge.moabi.database.db.MoodAndEnergyDB;
import com.ivorybridge.moabi.database.entity.moodandenergy.DailyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.DailyMood;
import com.ivorybridge.moabi.database.entity.moodandenergy.MonthlyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.MonthlyMood;
import com.ivorybridge.moabi.database.entity.moodandenergy.MoodAndEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.WeeklyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.WeeklyMood;
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

public class MoodAndEnergyRepository {

    private static final String TAG = MoodAndEnergyRepository.class.getSimpleName();
    private MoodAndEnergyDao mMoodAndEnergyDao;
    private MoodDao moodDao;
    private EnergyDao energyDao;
    private Application application;
    private LiveData<List<MoodAndEnergy>> mMoodAndEnergyDailySummaries;
    private FirebaseManager firebaseManager;
    private InputHistoryRepository inputHistoryRepository;
    private FormattedTime formattedTime;

    public MoodAndEnergyRepository(Application application) {
        this.firebaseManager = new FirebaseManager();
        MoodAndEnergyDB db = MoodAndEnergyDB.getDatabase(application);
        mMoodAndEnergyDao = db.moodAndEnergyDao();
        inputHistoryRepository = new InputHistoryRepository(application);
        formattedTime = new FormattedTime();
        moodDao = db.moodDao();
        energyDao = db.energyDao();
        this.application = application;
    }

    // wrapper for getDailySummary
    public LiveData<List<MoodAndEnergy>> getAllEntries() {
        return mMoodAndEnergyDao.getAll();
    }

    public LiveData<MoodAndEnergy> get(Long dateInLong) {
        return mMoodAndEnergyDao.get(dateInLong);
    }

    public LiveData<List<MoodAndEnergy>> getEntries(Long start, Long end) {
        return mMoodAndEnergyDao.getAll(start, end);
    }

    public LiveData<List<DailyMood>> getDailyMoods(Long start, Long end) {
        return moodDao.getAllDailyEntries(start, end);
    }

    public List<DailyMood> getDailyMoodsNow(Long start, Long end) {
        return moodDao.getAllDailyEntriesNow(start, end);
    }

    public LiveData<List<WeeklyMood>> getWeeklyMoods(Long start, Long end) {
        return moodDao.getAllWeeklyEntries(start, end);
    }

    public LiveData<List<MonthlyMood>> getMonthlyMoods(Long start, Long end) {
        return moodDao.getAllMonthlyEntries(start, end);
    }

    public LiveData<List<DailyEnergy>> getDailyEnergies(Long start, Long end) {
        return energyDao.getAllDailyEntries(start, end);
    }

    public List<DailyEnergy> getDailyEnergiesNow(Long start, Long end) {
        return energyDao.getAllDailyEntriesNow(start, end);
    }

    public LiveData<List<WeeklyEnergy>> getWeeklyEnergies(Long start, Long end) {
        return energyDao.getAllWeeklyEntries(start, end);
    }

    public LiveData<List<MonthlyEnergy>> getMonthlyEnergies(Long start, Long end) {
        return energyDao.getAllMonthlyEntries(start, end);
    }

    public void sync() {
        firebaseManager.getMoodAndEnergyRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dailySnap : dataSnapshot.getChildren()) {
                    if (dailySnap.hasChildren() && dailySnap.getKey() != null) {
                        String date = dailySnap.getKey();
                        for (DataSnapshot timeSnap : dailySnap.getChildren()) {
                            if (timeSnap.getKey() != null) {
                                String dateTimeToConvert = date + " " + timeSnap.getKey();
                                long dateInLong = formattedTime.convertStringYYYYMMDDhhmmToLong(dateTimeToConvert);
                                MoodAndEnergy entry = new MoodAndEnergy();
                                entry.setMood((Long) timeSnap.child(application.getString(R.string.mood_camel_case)).getValue());
                                entry.setEnergyLevel((Long) timeSnap.child(application.getString(R.string.energy_camel_case)).getValue());
                                entry.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                entry.setDateInLong(dateInLong);
                                Log.i(TAG, dateTimeToConvert + ": " + entry.getMood() + ", " + entry.getEnergyLevel());
                                insert(entry, date);
                            }
                        }
                        firebaseManager.getDaysWithDataRef().child(date).child(application.getString(R.string.mood_and_energy_camel_case)).setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean insert(MoodAndEnergy moodAndEnergy, String date) {
        AsyncTask.Status status = new insertAsyncTask(mMoodAndEnergyDao).execute(moodAndEnergy).getStatus();
        InputHistory inputHistory = new InputHistory();
        inputHistory.setInputType(application.getString(R.string.mood_and_energy_camel_case));
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

    public boolean processMood(List<MoodAndEnergy> moodAndEnergyList) {
        AsyncTask.Status status = new processMoodAsyncTask(this, moodAndEnergyList).execute().getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class processMoodAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<MoodAndEnergyRepository> weakReference;
        private List<MoodAndEnergy> moodAndEnergyList;

        processMoodAsyncTask(MoodAndEnergyRepository context, List<MoodAndEnergy> moodAndEnergyList) {
            this.weakReference = new WeakReference<>(context);
            this.moodAndEnergyList = moodAndEnergyList;
        }

        @Override
        protected Void doInBackground(final Void... args) {
            MoodAndEnergyRepository repository = weakReference.get();
            repository.processRawData(moodAndEnergyList);
            return null;
        }
    }

    private void processRawData(List<MoodAndEnergy> moodAndEnergyList) {
        if (moodAndEnergyList != null) {
            if (moodAndEnergyList.size() > 0) {
                Log.i(TAG, moodAndEnergyList.toString());
                List<String> entryDatesList = new ArrayList<>();
                Map<String, Float> moodDailyEntries = new LinkedHashMap<>();
                Map<String, Float> energyDailyEntries = new LinkedHashMap<>();
                Map<String, Long> numDailyEntries = new LinkedHashMap<>();
                Map<String, Long> numWeeklyEntries = new LinkedHashMap<>();
                Map<String, Float> moodWeeklyEntries = new LinkedHashMap<>();
                Map<String, Float> energyWeeklyEntries = new LinkedHashMap<>();
                Map<String, Long> numMonthlyEntries = new LinkedHashMap<>();
                Map<String, Float> moodMonthlyEntries = new LinkedHashMap<>();
                Map<String, Float> energyMonthlyEntries = new LinkedHashMap<>();

                for (MoodAndEnergy entry: moodAndEnergyList) {
                    String date = formattedTime.convertLongToYYYYMMDD(entry.getDateInLong());
                    String weekDate = formattedTime.convertLongToYYYYW(entry.getDateInLong());
                    Log.i(TAG, weekDate);
                    String monthDate = formattedTime.convertLongToYYYYMM(entry.getDateInLong());
                    if (moodDailyEntries.get(date) != null) {
                        Float old = moodDailyEntries.get(date);
                        moodDailyEntries.put(date, old + entry.getMood().floatValue());
                    } else {
                        moodDailyEntries.put(date, entry.getMood().floatValue());
                    }
                    if (energyDailyEntries.get(date) != null) {
                        Float old = energyDailyEntries.get(date);
                        energyDailyEntries.put(date, old + entry.getEnergyLevel().floatValue());
                    } else {
                        energyDailyEntries.put(date, entry.getEnergyLevel().floatValue());
                    }
                    if (numDailyEntries.get(date) != null) {
                        Long counter = numDailyEntries.get(date);
                        numDailyEntries.put(date, counter + 1L);
                    } else {
                        numDailyEntries.put(date, 1L);
                    }
                    if (moodWeeklyEntries.get(weekDate) != null) {
                        Float old = moodWeeklyEntries.get(weekDate);
                        moodWeeklyEntries.put(weekDate, old + entry.getMood().floatValue());
                    } else {
                        moodWeeklyEntries.put(weekDate, entry.getMood().floatValue());
                    }
                    if (energyWeeklyEntries.get(weekDate) != null) {
                        Float old = energyWeeklyEntries.get(weekDate);
                        energyWeeklyEntries.put(weekDate, old + entry.getEnergyLevel().floatValue());
                    } else {
                        energyWeeklyEntries.put(weekDate, entry.getEnergyLevel().floatValue());
                    }
                    if (numWeeklyEntries.get(weekDate) != null) {
                        Long counter = numWeeklyEntries.get(weekDate);
                        numWeeklyEntries.put(weekDate, counter + 1L);
                    } else {
                        numWeeklyEntries.put(weekDate, 1L);
                    }
                    if (moodMonthlyEntries.get(monthDate) != null) {
                        Float old = moodMonthlyEntries.get(monthDate);
                        moodMonthlyEntries.put(monthDate, old + entry.getMood().floatValue());
                    } else {
                        moodMonthlyEntries.put(monthDate, entry.getMood().floatValue());
                    }
                    if (energyMonthlyEntries.get(monthDate) != null) {
                        Float old = energyMonthlyEntries.get(monthDate);
                        energyMonthlyEntries.put(monthDate, old + entry.getEnergyLevel().floatValue());
                    } else {
                        energyMonthlyEntries.put(monthDate, entry.getEnergyLevel().floatValue());
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
                    if (moodDailyEntries.get(date) != null) {
                        moodDailyEntries.put(date, moodDailyEntries.get(date) / numEntries);
                    }
                    if (energyDailyEntries.get(date) != null) {
                        energyDailyEntries.put(date, energyDailyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Long> numEntry: numWeeklyEntries.entrySet()) {
                    String date = numEntry.getKey();
                    Long numEntries = numEntry.getValue();
                    Log.i(TAG, date);
                    if (moodWeeklyEntries.get(date) != null) {
                        moodWeeklyEntries.put(date, moodWeeklyEntries.get(date) / numEntries);
                    }
                    if (energyWeeklyEntries.get(date) != null) {
                        energyWeeklyEntries.put(date, energyWeeklyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Long> numEntry: numMonthlyEntries.entrySet()) {
                    String date = numEntry.getKey();
                    Long numEntries = numEntry.getValue();
                    Log.i(TAG, date);
                    if (moodMonthlyEntries.get(date) != null) {
                        moodMonthlyEntries.put(date, moodMonthlyEntries.get(date) / numEntries);
                    }
                    if (energyMonthlyEntries.get(date) != null) {
                        energyMonthlyEntries.put(date, energyMonthlyEntries.get(date) / numEntries);
                    }
                }

                for (Map.Entry<String, Float> moodEntry: moodDailyEntries.entrySet()) {
                    String date = moodEntry.getKey();
                    Float averageMood = moodEntry.getValue();
                    DailyMood mood = new DailyMood();
                    mood.setDate(date);
                    mood.setAverageMood(averageMood.doubleValue());
                    mood.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                    mood.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    mood.setNumOfEntries(numDailyEntries.get(date));
                    insertDailyMood(mood);
                }

                for (Map.Entry<String, Float> moodEntry: moodWeeklyEntries.entrySet()) {
                    String date = moodEntry.getKey();
                    Float averageMood = moodEntry.getValue();
                    WeeklyMood mood = new WeeklyMood();
                    mood.setYYYYW(date);
                    mood.setAverageMood(averageMood.doubleValue());
                    int year = Integer.parseInt(date.substring(0, 4));
                    int week = Integer.parseInt(date.substring(5));
                    Long startTime = formattedTime.getStartTimeOfYYYYW(week, year);
                    Long endTime = formattedTime.getEndTimeOfYYYYW(week, year);
                    mood.setStartDateInLong(startTime);
                    mood.setEndDateInLong(endTime);
                    Log.i(TAG,startTime + ", " + endTime);
                    mood.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    mood.setNumOfEntries(numWeeklyEntries.get(date));
                    insertWeeklyMood(mood);
                }

                for (Map.Entry<String, Float> moodEntry: moodMonthlyEntries.entrySet()) {
                    String date = moodEntry.getKey();
                    Float averageMood = moodEntry.getValue();
                    MonthlyMood mood = new MonthlyMood();
                    mood.setYYYYMM(date);
                    mood.setAverageMood(averageMood.doubleValue());
                    Long startTime = formattedTime.getStartTimeOfYYYYMM(date);
                    Long endTime = formattedTime.getEndTimeOfYYYYMM(date);
                    mood.setStartDateInLong(startTime);
                    mood.setEndDateInLong(endTime);
                    Log.i(TAG,startTime + ", " + endTime);
                    mood.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    mood.setNumOfEntries(numMonthlyEntries.get(date));
                    insertMonthlyMood(mood);
                }

                for (Map.Entry<String, Float> energyEntry: energyDailyEntries.entrySet()) {
                    String date = energyEntry.getKey();
                    Float averageEnergy = energyEntry.getValue();
                    DailyEnergy energy = new DailyEnergy();
                    energy.setDate(date);
                    energy.setAverageEnergy(averageEnergy.doubleValue());
                    energy.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                    energy.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    insertDailyEnergy(energy);
                }

                for (Map.Entry<String, Float> entry: energyWeeklyEntries.entrySet()) {
                    String date = entry.getKey();
                    Float average = entry.getValue();
                    WeeklyEnergy energy = new WeeklyEnergy();
                    energy.setYYYYW(date);
                    energy.setAverageEnergy(average.doubleValue());
                    int year = Integer.parseInt(date.substring(0, 4));
                    int week = Integer.parseInt(date.substring(5));
                    Long startTime = formattedTime.getStartTimeOfYYYYW(week, year);
                    Long endTime = formattedTime.getEndTimeOfYYYYW(week, year);
                    energy.setStartDateInLong(startTime);
                    energy.setEndDateInLong(endTime);
                    Log.i(TAG,startTime + ", " + endTime);
                    energy.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    energy.setNumOfEntries(numWeeklyEntries.get(date));
                    insertWeeklyEnergy(energy);
                }

                for (Map.Entry<String, Float> entry: energyMonthlyEntries.entrySet()) {
                    String date = entry.getKey();
                    Float averageEnergy = entry.getValue();
                    MonthlyEnergy energy = new MonthlyEnergy();
                    energy.setYYYYMM(date);
                    energy.setAverageEnergy(averageEnergy.doubleValue());
                    Long startTime = formattedTime.getStartTimeOfYYYYMM(date);
                    Long endTime = formattedTime.getEndTimeOfYYYYMM(date);
                    energy.setStartDateInLong(startTime);
                    energy.setEndDateInLong(endTime);
                    Log.i(TAG,startTime + ", " + endTime);
                    energy.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    energy.setNumOfEntries(numMonthlyEntries.get(date));
                    insertMonthlyEnergy(energy);
                }
            }
        }
    }

    private static class insertAsyncTask extends AsyncTask<MoodAndEnergy, Void, Void> {

        private MoodAndEnergyDao mAsyncTaskDao;

        insertAsyncTask(MoodAndEnergyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final MoodAndEnergy... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private boolean insertDailyMood(DailyMood dailyMood) {
        AsyncTask.Status status = new insertDailyMoodAsyncTask(moodDao).execute(dailyMood).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertDailyMoodAsyncTask extends AsyncTask<DailyMood, Void, Void> {

        private MoodDao mAsyncTaskDao;

        insertDailyMoodAsyncTask(MoodDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(DailyMood... dailyMoods) {
            mAsyncTaskDao.insert(dailyMoods[0]);
            return null;
        }
    }

    private boolean insertDailyEnergy(DailyEnergy dailyEnergy) {
        AsyncTask.Status status = new insertDailyEnergyAsyncTask(energyDao).execute(dailyEnergy).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertDailyEnergyAsyncTask extends AsyncTask<DailyEnergy, Void, Void> {

        private EnergyDao mAsyncTaskDao;

        insertDailyEnergyAsyncTask(EnergyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(DailyEnergy... dailyEnergies) {
            mAsyncTaskDao.insert(dailyEnergies[0]);
            return null;
        }
    }

    private boolean insertWeeklyMood(WeeklyMood weeklyMood) {
        AsyncTask.Status status = new insertWeeklyMoodAsyncTask(moodDao).execute(weeklyMood).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertWeeklyMoodAsyncTask extends AsyncTask<WeeklyMood, Void, Void> {

        private MoodDao mAsyncTaskDao;

        insertWeeklyMoodAsyncTask(MoodDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(WeeklyMood... weeklyMoods) {
            mAsyncTaskDao.insert(weeklyMoods[0]);
            return null;
        }
    }

    private boolean insertWeeklyEnergy(WeeklyEnergy weeklyEnergy) {
        AsyncTask.Status status = new insertWeeklyEnergyAsyncTask(energyDao).execute(weeklyEnergy).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertWeeklyEnergyAsyncTask extends AsyncTask<WeeklyEnergy, Void, Void> {

        private EnergyDao mAsyncTaskDao;

        insertWeeklyEnergyAsyncTask(EnergyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(WeeklyEnergy... weeklyEnergys) {
            mAsyncTaskDao.insert(weeklyEnergys[0]);
            return null;
        }
    }

    private boolean insertMonthlyMood(MonthlyMood monthlyMood) {
        AsyncTask.Status status = new insertMonthlyMoodAsyncTask(moodDao).execute(monthlyMood).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertMonthlyMoodAsyncTask extends AsyncTask<MonthlyMood, Void, Void> {

        private MoodDao mAsyncTaskDao;

        insertMonthlyMoodAsyncTask(MoodDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(MonthlyMood... monthlyMoods) {
            mAsyncTaskDao.insert(monthlyMoods[0]);
            return null;
        }
    }

    private boolean insertMonthlyEnergy(MonthlyEnergy monthlyEnergy) {
        AsyncTask.Status status = new insertMonthlyEnergyAsyncTask(energyDao).execute(monthlyEnergy).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertMonthlyEnergyAsyncTask extends AsyncTask<MonthlyEnergy, Void, Void> {

        private EnergyDao mAsyncTaskDao;

        insertMonthlyEnergyAsyncTask(EnergyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(MonthlyEnergy... monthlyEnergys) {
            mAsyncTaskDao.insert(monthlyEnergys[0]);
            return null;
        }
    }
}
