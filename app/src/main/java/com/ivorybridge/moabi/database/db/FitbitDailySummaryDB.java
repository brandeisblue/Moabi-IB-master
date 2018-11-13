package com.ivorybridge.moabi.database.db;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import com.ivorybridge.moabi.database.converter.FitbitTypeConverter;
import com.ivorybridge.moabi.database.dao.FitbitDailySummaryDao;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;


@Database(entities = {FitbitDailySummary.class}, version = 7, exportSchema = false)
@TypeConverters(FitbitTypeConverter.class)
public abstract class FitbitDailySummaryDB extends RoomDatabase {

    public abstract FitbitDailySummaryDao fitbitDao();
    private static FitbitDailySummaryDB INSTANCE;
    private static RoomDatabase.Callback fitbitDataBaseCallBack = new RoomDatabase.Callback(){

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static FitbitDailySummaryDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FitbitDailySummaryDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            FitbitDailySummaryDB.class, "fitbit_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final FitbitDailySummaryDao mDao;

        PopulateDbAsync(FitbitDailySummaryDB db) {
            mDao = db.fitbitDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAll();
            FitbitDailySummary blankActivity = new FitbitDailySummary();
            blankActivity.date = "01-01-1900";
            mDao.insert(blankActivity);
            return null;
        }
    }
}
