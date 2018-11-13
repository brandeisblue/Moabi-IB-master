package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.BAActivityEntryDao;
import com.ivorybridge.moabi.database.dao.BAActivityFavoritedDao;
import com.ivorybridge.moabi.database.dao.BAActivityInLibraryDao;
import com.ivorybridge.moabi.database.db.BAActivityEntryDB;
import com.ivorybridge.moabi.database.db.BAActivityFavoritedDB;
import com.ivorybridge.moabi.database.db.BAActivityInLibraryDB;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityFavorited;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityInLibrary;
import com.ivorybridge.moabi.database.entity.util.InputHistory;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class BAActivityRepository {

    private static final String TAG = BAActivityRepository.class.getSimpleName();
    private BAActivityEntryDao entryDao;
    private BAActivityFavoritedDao favoritedDao;
    private BAActivityInLibraryDao libraryDao;
    private InputHistoryRepository inputHistoryRepository;
    private FormattedTime formattedTime;
    private FirebaseManager firebaseManager;
    private Application application;

    public BAActivityRepository(Application application) {
        BAActivityEntryDB entryDB = BAActivityEntryDB.getDatabase(application);
        BAActivityFavoritedDB favoritedDB = BAActivityFavoritedDB.getDatabase(application);
        BAActivityInLibraryDB libraryDB = BAActivityInLibraryDB.getDatabase(application);
        entryDao = entryDB.baActivityDao();
        favoritedDao = favoritedDB.baActivityDao();
        libraryDao = libraryDB.baActivityDao();
        inputHistoryRepository = new InputHistoryRepository(application);
        formattedTime = new FormattedTime();
        firebaseManager = new FirebaseManager();
        this.application = application;
    }

    public LiveData<List<BAActivityEntry>> getActivityEntries(Long start, Long end) {
        return entryDao.getAll(start, end);
    }

    public List<BAActivityEntry> getActivityEntriesNow(Long start, Long end) {
        return entryDao.getAllNow(start, end);
    }

    public LiveData<List<BAActivityEntry>> getAllActivityEntries() {
        return entryDao.getAll();
    }

    public LiveData<List<BAActivityInLibrary>> getAllActivitiesInLibrary() {
        return libraryDao.getAll();
    }

    public LiveData<List<BAActivityFavorited>> getAllFavoritedActivities() {
        return favoritedDao.getAll();
    }

    public void sync() {
        firebaseManager.getActivityRef().addListenerForSingleValueEvent(new ValueEventListener() {
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
                                        long timeOfEntry = formattedTime.convertStringYYYYMMDDhhmmToLong(dateTimeToConvert);
                                        BAActivityEntry entry = new BAActivityEntry();
                                        if (activitySnap.child("type").getKey() != null
                                                && activitySnap.child("type").getValue() != null) {
                                            entry.setActivityType((Long) activitySnap.child("type").getValue());
                                            entry.setName(activitySnap.getKey());
                                            entry.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                            entry.setDateInLong(timeOfEntry);
                                            Log.i(TAG, timeOfEntry + ": " + entry.getName() + ", " + entry.getActivityType());
                                            insertActivityEntry(entry, date);
                                        }
                                    }
                                }
                            }
                        }
                        firebaseManager.getDaysWithDataRef().child(date).child(application.getString(R.string.baactivity_camel_case)).setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean deleteActivityEntry(String timeOfEntry, String name) {
        AsyncTask.Status status = new deleteActivityEntryAsyncTask(entryDao, timeOfEntry, name).execute().getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class deleteActivityEntryAsyncTask extends AsyncTask<Void, Void, Void> {

        private BAActivityEntryDao mAsyncTaskDao;
        private String timeOfEntry;
        private String name;

        deleteActivityEntryAsyncTask(BAActivityEntryDao dao, String timeOfEntry, String name) {
            mAsyncTaskDao = dao;
            this.timeOfEntry = timeOfEntry;
            this.name = name;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.delete(timeOfEntry, name);
            return null;
        }
    }


    public boolean deleteAllFavoritedActivities() {
        AsyncTask.Status status = new deleteAllFavoritedActivitiesAsyncTask(favoritedDao).execute().getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class deleteAllFavoritedActivitiesAsyncTask extends AsyncTask<Void, Void, Void> {

        private BAActivityFavoritedDao mAsyncTaskDao;

        deleteAllFavoritedActivitiesAsyncTask(BAActivityFavoritedDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
           mAsyncTaskDao.deleteAll();
           return null;
        }
    }

    public boolean deleteAllActivitiesInLibrary() {
        AsyncTask.Status status = new deleteAllActivitiesInLibraryAsyncTask(libraryDao).execute().getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class deleteAllActivitiesInLibraryAsyncTask extends AsyncTask<Void, Void, Void> {

        private BAActivityInLibraryDao mAsyncTaskDao;

        deleteAllActivitiesInLibraryAsyncTask(BAActivityInLibraryDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    public boolean deleteFavoritedActivity(String name) {
        AsyncTask.Status status = new deleteFavoritedActivityAsyncTask(favoritedDao).execute(name).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class deleteFavoritedActivityAsyncTask extends AsyncTask<String, Void, Void> {

        private BAActivityFavoritedDao mAsyncTaskDao;

        deleteFavoritedActivityAsyncTask(BAActivityFavoritedDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final String... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    public boolean insertActivityEntry(BAActivityEntry entry, String date) {
        InputHistory inputHistory = new InputHistory();
        inputHistory.setInputType(application.getString(R.string.baactivity_camel_case));
        inputHistory.setDate(date);
        inputHistory.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
        inputHistory.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
        inputHistoryRepository.insert(inputHistory);
        AsyncTask.Status status = new insertActivityEntryAsyncTask(entryDao).execute(entry).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertActivityEntryAsyncTask extends AsyncTask<BAActivityEntry, Void, Void> {

        private BAActivityEntryDao mAsyncTaskDao;

        insertActivityEntryAsyncTask(BAActivityEntryDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final BAActivityEntry... params) {
            Log.i(TAG, "Inserting activity - " + params[0].getName() + " at " + params[0].getDateInLong());
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public boolean insertFavoritedActivity(BAActivityFavorited activityInUse) {
        AsyncTask.Status status = new insertFavoritedActivityAsyncTask(favoritedDao).execute(activityInUse).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertFavoritedActivityAsyncTask extends AsyncTask<BAActivityFavorited, Void, Void> {

        private BAActivityFavoritedDao mAsyncTaskDao;
        insertFavoritedActivityAsyncTask(BAActivityFavoritedDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final BAActivityFavorited... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public boolean insertActivityToLibrary(BAActivityInLibrary activityInDB) {
        AsyncTask.Status status = new insertActivityToLibraryAsyncTask(libraryDao).execute(activityInDB).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertActivityToLibraryAsyncTask extends AsyncTask<BAActivityInLibrary, Void, Void> {

        private BAActivityInLibraryDao mAsyncTaskDao;

        insertActivityToLibraryAsyncTask(BAActivityInLibraryDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final BAActivityInLibrary... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
