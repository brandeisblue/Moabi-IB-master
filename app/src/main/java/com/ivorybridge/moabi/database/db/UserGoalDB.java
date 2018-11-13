package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.UserGoalDao;
import com.ivorybridge.moabi.database.entity.util.UserGoal;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {UserGoal.class}, version = 1, exportSchema = false)
public abstract class UserGoalDB extends RoomDatabase {

    public abstract UserGoalDao userGoalDao();
    private static UserGoalDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static UserGoalDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (UserGoalDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            UserGoalDB.class, "user_goal_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final UserGoalDao mDao;

        PopulateDbAsync(UserGoalDB db) {
            mDao = db.userGoalDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAll();
            UserGoal blankActivity = new UserGoal();
            blankActivity.setGoalName("hello");
            blankActivity.date = "01-01-1900";
            mDao.insert(blankActivity);
            return null;
        }
    }
}

