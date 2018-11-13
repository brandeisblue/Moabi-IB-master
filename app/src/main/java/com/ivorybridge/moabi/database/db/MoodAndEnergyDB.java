package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.EnergyDao;
import com.ivorybridge.moabi.database.dao.MoodAndEnergyDao;
import com.ivorybridge.moabi.database.dao.MoodDao;
import com.ivorybridge.moabi.database.entity.moodandenergy.DailyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.DailyMood;
import com.ivorybridge.moabi.database.entity.moodandenergy.MonthlyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.MonthlyMood;
import com.ivorybridge.moabi.database.entity.moodandenergy.MoodAndEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.WeeklyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.WeeklyMood;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {MoodAndEnergy.class, DailyMood.class, WeeklyMood.class, MonthlyMood.class,
DailyEnergy.class, WeeklyEnergy.class, MonthlyEnergy.class}, version = 1, exportSchema = false)
public abstract class MoodAndEnergyDB extends RoomDatabase {

    public abstract MoodAndEnergyDao moodAndEnergyDao();
    public abstract MoodDao moodDao();
    public abstract EnergyDao energyDao();

    private static MoodAndEnergyDB INSTANCE;
    private static RoomDatabase.Callback MoodAndEnergyCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new MoodAndEnergyDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static MoodAndEnergyDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MoodAndEnergyDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MoodAndEnergyDB.class, "mood_and_energy_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final MoodAndEnergyDao mDao;
        private final MoodDao moodDao;
        private final EnergyDao energyDao;

        PopulateDbAsync(MoodAndEnergyDB db) {
            mDao = db.moodAndEnergyDao();
            moodDao = db.moodDao();
            energyDao = db.energyDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAll();
            MoodAndEnergy blankActivity = new MoodAndEnergy();
            blankActivity.setDateInLong(0L);
            blankActivity.setTimeOfEntry(0L);
            mDao.insert(blankActivity);
            moodDao.deleteAllDailyEntries();
            moodDao.deleteAllWeeklyEntries();
            moodDao.deleteAllMonthlyEntries();
            DailyMood dailyMood = new DailyMood();
            dailyMood.setDate("1990-01-01");
            WeeklyMood weeklyMood = new WeeklyMood();
            weeklyMood.setYYYYW("1990-01");
            MonthlyMood monthlyMood = new MonthlyMood();
            monthlyMood.setYYYYMM("1990-01");
            moodDao.insert(dailyMood);
            moodDao.insert(weeklyMood);
            moodDao.insert(monthlyMood);
            energyDao.deleteAllDailyEntries();
            energyDao.deleteAllWeeklyEntries();
            energyDao.deleteAllMonthlyEntries();
            DailyEnergy dailyEnergy = new DailyEnergy();
            dailyEnergy.setDate("1990-01-01");
            WeeklyEnergy weeklyEnergy = new WeeklyEnergy();
            weeklyEnergy.setYYYYW("1990-01");
            MonthlyEnergy monthlyEnergy = new MonthlyEnergy();
            monthlyEnergy.setYYYYMM("1990-01");
            energyDao.insert(dailyEnergy);
            energyDao.insert(weeklyEnergy);
            energyDao.insert(monthlyEnergy);
            return null;
        }
    }
}
