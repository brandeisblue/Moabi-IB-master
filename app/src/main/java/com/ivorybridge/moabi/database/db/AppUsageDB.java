package com.ivorybridge.moabi.database.db;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import com.ivorybridge.moabi.database.dao.AppUsageDao;
import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;


@Database(entities = {AppUsageSummary.class}, version = 6, exportSchema = false)
public abstract class AppUsageDB extends RoomDatabase {

    public abstract AppUsageDao AppUsageDao();
    private static AppUsageDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new AppUsageDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static AppUsageDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppUsageDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppUsageDB.class, "app_usage_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final AppUsageDao mDao;

        PopulateDbAsync(AppUsageDB db) {
            mDao = db.AppUsageDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAll();
            AppUsageSummary blankActivity = new AppUsageSummary();
            blankActivity.setDate("1990-01-01");
            mDao.insert(blankActivity);
            return null;
        }
    }
}
