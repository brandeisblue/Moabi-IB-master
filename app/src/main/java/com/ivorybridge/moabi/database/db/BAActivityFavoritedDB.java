package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.BAActivityFavoritedDao;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityFavorited;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;


@Database(entities = {BAActivityFavorited.class}, version = 1, exportSchema = false)
public abstract class BAActivityFavoritedDB extends RoomDatabase {

    public abstract BAActivityFavoritedDao baActivityDao();

    private static BAActivityFavoritedDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new BAActivityFavoritedDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static BAActivityFavoritedDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BAActivityFavoritedDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            BAActivityFavoritedDB.class, "ba_activity_favorited_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final BAActivityFavoritedDao mDao;

        PopulateDbAsync(BAActivityFavoritedDB db) {
            mDao = db.baActivityDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAll();
            BAActivityFavorited blankActivity = new BAActivityFavorited();
            blankActivity.setName("test");
            mDao.insert(blankActivity);
            return null;
        }
    }
}