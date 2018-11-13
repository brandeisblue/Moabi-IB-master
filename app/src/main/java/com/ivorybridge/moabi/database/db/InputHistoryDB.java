package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.InputHistoryDao;
import com.ivorybridge.moabi.database.entity.util.InputDate;
import com.ivorybridge.moabi.database.entity.util.InputHistory;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

//@TypeConverters(StringListTypeConverter.class)
@Database(entities = {InputHistory.class, InputDate.class}, version = 1, exportSchema = false)
public abstract class InputHistoryDB extends RoomDatabase {

    public abstract InputHistoryDao inputHistoryDao();
    private static InputHistoryDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    private static Migration MIGRATION_5_1 = new Migration(5, 1) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //database.execSQL("DROP TABLE input_date_table");
            database.execSQL("CREATE TABLE input_date_table (date TEXT NOT NULL," +
                    " hasData INTEGER NOT NULL, " +
                    " PRIMARY KEY(date) )");
        }
    };

    public static InputHistoryDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (InputHistoryDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            InputHistoryDB.class, "input_history_database")
                            .addMigrations(MIGRATION_5_1)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final InputHistoryDao mDao;

        PopulateDbAsync(InputHistoryDB db) {
            mDao = db.inputHistoryDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAllInputHistory();
            InputHistory blankActivity = new InputHistory();
            blankActivity.date = "01-01-1900";
            blankActivity.setInputType("test");
            mDao.insertInputHistory(blankActivity);
            InputDate inputDate = new InputDate();
            inputDate.setDate("01-01-1990");
            inputDate.setHasData(true);
            mDao.insertInputDate(inputDate);
            return null;
        }
    }
}
