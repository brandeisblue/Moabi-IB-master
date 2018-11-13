package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.DataInUseDao;
import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.InputInUse;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {InputInUse.class, ConnectedService.class}, version = 1, exportSchema = false)
public abstract class DataInUseDB extends RoomDatabase {

    public abstract DataInUseDao selectedActivitiesDao();
    private static DataInUseDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new DataInUseDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    private static Migration MIGRATION_2_1 = new Migration(2, 1) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };

    public static DataInUseDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DataInUseDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DataInUseDB.class, "data_in_use_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final DataInUseDao mDao;

        PopulateDbAsync(DataInUseDB db) {
            mDao = db.selectedActivitiesDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAllInputsInUse();
            mDao.deleteAllConnectedServices();
            InputInUse blankActivity = new InputInUse();
            blankActivity.setName("test");
            mDao.insert(blankActivity);
            ConnectedService blankService = new ConnectedService();
            blankService.setName("test");
            mDao.insert(blankService);
            mDao.deleteAllInputsInUse();
            mDao.deleteAllConnectedServices();
            return null;
        }
    }
}
