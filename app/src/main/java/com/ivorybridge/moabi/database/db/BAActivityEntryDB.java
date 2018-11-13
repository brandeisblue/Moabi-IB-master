package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.BAActivityEntryDao;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;


@Database(entities = {BAActivityEntry.class}, version = 2, exportSchema = false)
public abstract class BAActivityEntryDB extends RoomDatabase {

    public abstract BAActivityEntryDao baActivityDao();
    private static BAActivityEntryDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new BAActivityEntryDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static BAActivityEntryDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BAActivityEntryDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            BAActivityEntryDB.class, "ba_activity_entry_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final BAActivityEntryDao mDao;

        PopulateDbAsync(BAActivityEntryDB db) {
            mDao = db.baActivityDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAll();
            BAActivityEntry blankActivity = new BAActivityEntry();
            blankActivity.setName("test");
            blankActivity.setTimeOfEntry(0L);
            blankActivity.setDateInLong(0L);
            mDao.insert(blankActivity);
            return null;
        }
    }
}
