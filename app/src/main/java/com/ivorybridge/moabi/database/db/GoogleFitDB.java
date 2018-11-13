package com.ivorybridge.moabi.database.db;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import com.ivorybridge.moabi.database.dao.GoogleFitDao;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;

@Database(entities = {GoogleFitSummary.class}, version = 7, exportSchema = false)
public abstract class GoogleFitDB extends RoomDatabase {

    public abstract GoogleFitDao googleFitDao();
    private static GoogleFitDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new GoogleFitDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static GoogleFitDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GoogleFitDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            GoogleFitDB.class, "googlefit_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final GoogleFitDao mDao;

        PopulateDbAsync(GoogleFitDB db) {
            mDao = db.googleFitDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAll();
            GoogleFitSummary blankActivity = new GoogleFitSummary();
            blankActivity.date = "01-01-1900";
            mDao.insert(blankActivity);
            return null;
        }
    }
}
