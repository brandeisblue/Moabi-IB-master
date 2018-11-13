package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.DailyReviewDao;
import com.ivorybridge.moabi.database.entity.dailyreview.DailyDailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.DailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.MonthlyDailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.WeeklyDailyReview;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {DailyReview.class, DailyDailyReview.class, WeeklyDailyReview.class, MonthlyDailyReview.class}, version = 2, exportSchema = false)
public abstract class DailyReviewDB extends RoomDatabase {

    public abstract DailyReviewDao dao();

    private static DailyReviewDB INSTANCE;
    private static RoomDatabase.Callback DailyReviewCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new DailyReviewDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static DailyReviewDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DailyReviewDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DailyReviewDB.class, "daily_review_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final DailyReviewDao dao;

        PopulateDbAsync(DailyReviewDB db) {
            dao = db.dao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            dao.deleteAll();
            DailyReview blankActivity = new DailyReview();
            blankActivity.setDateInLong(0L);
            blankActivity.setTimeOfEntry(0L);
            dao.insert(blankActivity);
            dao.deleteAllDailyEntries();
            dao.deleteAllWeeklyEntries();
            dao.deleteAllMonthlyEntries();
            DailyDailyReview dailyDailyReview = new DailyDailyReview();
            dailyDailyReview.setDate("1990-01-01");
            WeeklyDailyReview weeklyDailyReview = new WeeklyDailyReview();
            weeklyDailyReview.setYYYYW("1990-01");
            MonthlyDailyReview monthlyDailyReview = new MonthlyDailyReview();
            monthlyDailyReview.setYYYYMM("1990-01");
            dao.insert(dailyDailyReview);
            dao.insert(weeklyDailyReview);
            dao.insert(monthlyDailyReview);
            return null;
        }
    }
}
