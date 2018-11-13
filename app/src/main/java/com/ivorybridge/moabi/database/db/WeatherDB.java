package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.WeatherDailySummaryDao;
import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;


@Database(entities = {WeatherDailySummary.class}, version = 1, exportSchema = false)
public abstract class WeatherDB extends RoomDatabase {

    public abstract WeatherDailySummaryDao weatherDailySummaryDao();
    private static WeatherDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new WeatherDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    private static Migration MIGRATION_7_1 = new Migration(7, 1) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            /*
            database.execSQL("CREATE TABLE weather_daily_summary_table_new (date TEXT NOT NULL," +
                    " dateInLong INTEGER, maxTempC REAL, maxTempF REAL, minTempC REAL, " +
                    " minTempF REAL, avgTempC REAL, avgTempF REAL, " + " totalPrecipmm REAL, " +
                    " totalPrecipin REAL, avgHumidity REAL, condition TEXT, imageUrl TEXT, " +
                    " PRIMARY KEY(date))");
            database.execSQL("DROP TABLE weather_daily_summary_table");
            database.execSQL("ALTER TABLE weather_daily_summary_table_new " +
                    "RENAME TO weather_daily_summary_table");   */
            database.execSQL("ALTER TABLE weather_daily_summary_table " + "ADD COLUMN 'imageUrl' TEXT");
        }
    };

    public static WeatherDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WeatherDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WeatherDB.class, "weather_database")
                            .addMigrations(MIGRATION_7_1)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final WeatherDailySummaryDao mDao;

        PopulateDbAsync(WeatherDB db) {
            mDao = db.weatherDailySummaryDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAll();
            WeatherDailySummary blankActivity = new WeatherDailySummary();
            blankActivity.date = "01-01-1900";
            mDao.insert(blankActivity);
            return null;
        }
    }
}
