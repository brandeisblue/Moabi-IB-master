package com.ivorybridge.moabi.database.db;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import com.ivorybridge.moabi.database.dao.AsyncTaskBooleanDao;
import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;


@Database(entities = {AsyncTaskBoolean.class}, version = 2, exportSchema = false)
public abstract class AsyncTaskBooleanDB extends RoomDatabase {

    public abstract AsyncTaskBooleanDao asyncTaskBooleanDao();
    private static AsyncTaskBooleanDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new AsyncTaskBooleanDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static AsyncTaskBooleanDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AsyncTaskBooleanDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AsyncTaskBooleanDB.class, "async_task_success_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final AsyncTaskBooleanDao mDao;

        PopulateDbAsync(AsyncTaskBooleanDB db) {
            mDao = db.asyncTaskBooleanDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAll();
            AsyncTaskBoolean blankActivity = new AsyncTaskBoolean();
            blankActivity.setTaskName("test");
            mDao.insert(blankActivity);
            return null;
        }
    }
}
