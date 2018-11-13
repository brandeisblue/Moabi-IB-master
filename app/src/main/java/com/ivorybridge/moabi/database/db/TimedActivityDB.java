package com.ivorybridge.moabi.database.db;


import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.TimedActivityDao;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

//@TypeConverters(StringListTypeConverter.class)
@Database(entities = {TimedActivitySummary.class}, version = 2, exportSchema = false)
public abstract class TimedActivityDB extends RoomDatabase {

    public abstract TimedActivityDao todayDao();
    private static TimedActivityDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static TimedActivityDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TimedActivityDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TimedActivityDB.class, "timed_activity_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final TimedActivityDao mDao;

        PopulateDbAsync(TimedActivityDB db) {
            mDao = db.todayDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAll();
            TimedActivitySummary blankActivity = new TimedActivitySummary();
            blankActivity.date = "01-01-1900";
            mDao.insert(blankActivity);
            return null;
        }
    }
}
