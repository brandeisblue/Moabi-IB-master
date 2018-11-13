package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.BuiltInFitnessDao;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInProfile;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {BuiltInActivitySummary.class, BuiltInProfile.class}, version = 1, exportSchema = false)
public abstract class BuiltInFitnessDB extends RoomDatabase {

    public abstract BuiltInFitnessDao builtInFitnessDao();
    private static BuiltInFitnessDB INSTANCE;
    private static RoomDatabase.Callback builtInFitnessFitnessCallBack = new RoomDatabase.Callback(){

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };


    private static Migration MIGRATION_2_1 = new Migration(2, 1) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE built_in_profile_new (uniqueID TEXT NOT NULL," +
                    " name TEXT, gender TEXT, height REAL, BMR REAL, " +
                    " weight REAL, dateOfBirth TEXT, dateOfRegistration TEXT, " +
                    " PRIMARY KEY(uniqueID) )");
            database.execSQL("DROP TABLE built_in_profile_table");
            database.execSQL("ALTER TABLE built_in_profile_new RENAME TO built_in_profile_table");
        }
    };

    private static Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };

    public static BuiltInFitnessDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BuiltInFitnessDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            BuiltInFitnessDB.class, "built_in_fitness_database")
                            .addMigrations(MIGRATION_2_1)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final BuiltInFitnessDao mDao;

        PopulateDbAsync(BuiltInFitnessDB db) {
            mDao = db.builtInFitnessDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            mDao.deleteAll();
            BuiltInActivitySummary blankActivity = new BuiltInActivitySummary();
            blankActivity.date = "01-01-1900";
            mDao.insert(blankActivity);
            return null;
        }
    }
}
