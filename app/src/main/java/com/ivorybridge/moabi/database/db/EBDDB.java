package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.Gad7Dao;
import com.ivorybridge.moabi.database.dao.Phq9Dao;
import com.ivorybridge.moabi.database.entity.anxiety.DailyGad7;
import com.ivorybridge.moabi.database.entity.anxiety.Gad7;
import com.ivorybridge.moabi.database.entity.anxiety.MonthlyGad7;
import com.ivorybridge.moabi.database.entity.anxiety.WeeklyGad7;
import com.ivorybridge.moabi.database.entity.depression.DailyPhq9;
import com.ivorybridge.moabi.database.entity.depression.MonthlyPhq9;
import com.ivorybridge.moabi.database.entity.depression.Phq9;
import com.ivorybridge.moabi.database.entity.depression.WeeklyPhq9;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Phq9.class, DailyPhq9.class, WeeklyPhq9.class, MonthlyPhq9.class,
Gad7.class, DailyGad7.class, WeeklyGad7.class, MonthlyGad7.class}, version = 2, exportSchema = false)
public abstract class EBDDB extends RoomDatabase {

    public abstract Phq9Dao phq9Dao();
    public abstract Gad7Dao gad7Dao();

    private static EBDDB INSTANCE;
    private static RoomDatabase.Callback EBDCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new EBDDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static EBDDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (EBDDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            EBDDB.class, "ebd_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final Phq9Dao phq9Dao;
        private final Gad7Dao gad7Dao;

        PopulateDbAsync(EBDDB db) {
            phq9Dao = db.phq9Dao();
            gad7Dao = db.gad7Dao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            phq9Dao.deleteAll();
            Phq9 blankActivity = new Phq9();
            blankActivity.setDateInLong(0L);
            blankActivity.setTimeOfEntry(0L);
            phq9Dao.insert(blankActivity);
            phq9Dao.deleteAllDailyEntries();
            phq9Dao.deleteAllWeeklyEntries();
            phq9Dao.deleteAllMonthlyEntries();
            DailyPhq9 dailyPhq9 = new DailyPhq9();
            dailyPhq9.setDate("1990-01-01");
            WeeklyPhq9 weeklyPhq9 = new WeeklyPhq9();
            weeklyPhq9.setYYYYW("1990-01");
            MonthlyPhq9 monthlyPhq9 = new MonthlyPhq9();
            monthlyPhq9.setYYYYMM("1990-01");
            phq9Dao.insert(dailyPhq9);
            phq9Dao.insert(weeklyPhq9);
            phq9Dao.insert(monthlyPhq9);
            phq9Dao.deleteAll();
            Gad7 blankActivity1 = new Gad7();
            blankActivity.setDateInLong(0L);
            blankActivity.setTimeOfEntry(0L);
            gad7Dao.insert(blankActivity1);
            gad7Dao.deleteAllDailyEntries();
            gad7Dao.deleteAllWeeklyEntries();
            gad7Dao.deleteAllMonthlyEntries();
            DailyGad7 dailyGad7 = new DailyGad7();
            dailyPhq9.setDate("1990-01-01");
            WeeklyGad7 weeklyGad7 = new WeeklyGad7();
            weeklyPhq9.setYYYYW("1990-01");
            MonthlyGad7 monthlyGad7 = new MonthlyGad7();
            monthlyPhq9.setYYYYMM("1990-01");
            gad7Dao.insert(dailyGad7);
            gad7Dao.insert(weeklyGad7);
            gad7Dao.insert(monthlyGad7);
            gad7Dao.deleteAll();
            return null;
        }
    }
}
