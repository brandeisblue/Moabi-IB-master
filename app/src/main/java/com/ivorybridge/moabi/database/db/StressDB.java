package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.StressDao;
import com.ivorybridge.moabi.database.entity.stress.DailyStress;
import com.ivorybridge.moabi.database.entity.stress.MonthlyStress;
import com.ivorybridge.moabi.database.entity.stress.Stress;
import com.ivorybridge.moabi.database.entity.stress.WeeklyStress;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Stress.class, DailyStress.class, WeeklyStress.class, MonthlyStress.class}, version = 1, exportSchema = false)
public abstract class StressDB extends RoomDatabase {

    public abstract StressDao dao();

    private static StressDB INSTANCE;
    private static RoomDatabase.Callback StressCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new StressDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static StressDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (StressDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            StressDB.class, "stress_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final StressDao dao;

        PopulateDbAsync(StressDB db) {
            dao = db.dao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            dao.deleteAll();
            Stress blankActivity = new Stress();
            blankActivity.setDateInLong(0L);
            blankActivity.setTimeOfEntry(0L);
            dao.insert(blankActivity);
            dao.deleteAllDailyEntries();
            dao.deleteAllWeeklyEntries();
            dao.deleteAllMonthlyEntries();
            DailyStress dailyStress = new DailyStress();
            dailyStress.setDate("1990-01-01");
            WeeklyStress weeklyStress = new WeeklyStress();
            weeklyStress.setYYYYW("1990-01");
            MonthlyStress monthlyStress = new MonthlyStress();
            monthlyStress.setYYYYMM("1990-01");
            dao.insert(dailyStress);
            dao.insert(weeklyStress);
            dao.insert(monthlyStress);
            return null;
        }
    }
}
