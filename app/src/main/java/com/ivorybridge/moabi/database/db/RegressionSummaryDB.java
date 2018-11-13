package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.RegressionSummaryDao;
import com.ivorybridge.moabi.database.entity.stats.RegressionSummary;
import com.ivorybridge.moabi.database.entity.stats.SimpleRegressionSummary;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {RegressionSummary.class, SimpleRegressionSummary.class}, version = 1, exportSchema = false)
public abstract class RegressionSummaryDB extends RoomDatabase {

    public abstract RegressionSummaryDao regressionSummaryDao();
    private static RegressionSummaryDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new RegressionSummaryDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static RegressionSummaryDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RegressionSummaryDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RegressionSummaryDB.class, "regression_summary_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final RegressionSummaryDao mDao;

        PopulateDbAsync(RegressionSummaryDB db) {
            mDao = db.regressionSummaryDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAll();
            SimpleRegressionSummary blankActivity = new SimpleRegressionSummary();
            blankActivity.setDate("1990-01-01");
            blankActivity.setDepXIndepVars("helloXhello");
            mDao.insert(blankActivity);
            return null;
        }
    }
}