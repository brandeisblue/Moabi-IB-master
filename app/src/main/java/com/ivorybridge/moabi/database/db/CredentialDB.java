package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.CredentialDao;
import com.ivorybridge.moabi.database.entity.util.Credential;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Credential.class}, version = 1, exportSchema = false)
public abstract class CredentialDB extends RoomDatabase {

    public abstract CredentialDao dao();
    private static CredentialDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new CredentialDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static CredentialDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CredentialDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CredentialDB.class, "credential_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final CredentialDao mDao;

        PopulateDbAsync(CredentialDB db) {
            mDao = db.dao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            Credential blankActivity = new Credential();
            blankActivity.setServiceName("test");
            mDao.insert(blankActivity);
            mDao.deleteAll();
            return null;
        }
    }
}
