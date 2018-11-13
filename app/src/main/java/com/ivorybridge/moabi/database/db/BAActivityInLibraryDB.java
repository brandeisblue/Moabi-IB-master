package com.ivorybridge.moabi.database.db;

import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.database.dao.BAActivityInLibraryDao;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityInLibrary;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {BAActivityInLibrary.class}, version = 1, exportSchema = false)
public abstract class BAActivityInLibraryDB extends RoomDatabase {

    public abstract BAActivityInLibraryDao baActivityDao();


    private static BAActivityInLibraryDB INSTANCE;
    private static RoomDatabase.Callback todayCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new BAActivityInLibraryDB.PopulateDbAsync(INSTANCE).execute();
        }
    };

    public static BAActivityInLibraryDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BAActivityInLibraryDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            BAActivityInLibraryDB.class, "ba_activity_library_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final BAActivityInLibraryDao mDao;
        private Long EDUCATION_OR_CAREER = 0L;
        private Long SOCIAL = 1L;
        private Long RECREATION_OR_INTERESTS = 2L;
        private Long MIND_PHYSICAL_SPIRITUAL = 3L;
        private Long CHORE = 4L;

        PopulateDbAsync(BAActivityInLibraryDB db) {
            mDao = db.baActivityDao();
        }

        @Override
        protected synchronized Void doInBackground(final Void... params) {
            if (mDao.getAny().length < 1) {
                List<BAActivityInLibrary> activityList = new ArrayList<>();
                BAActivityInLibrary work1 = new BAActivityInLibrary("work", EDUCATION_OR_CAREER, 0L, "ic_work");
                activityList.add(work1);
                BAActivityInLibrary work2 = new BAActivityInLibrary("school", EDUCATION_OR_CAREER, 0L, "ic_school");
                activityList.add(work2);
                BAActivityInLibrary work3 = new BAActivityInLibrary("networking", EDUCATION_OR_CAREER, 0L, "ic_networking");
                activityList.add(work3);
                BAActivityInLibrary work4 = new BAActivityInLibrary("skills\ntraining", EDUCATION_OR_CAREER, 0L, "ic_skills_training");
                activityList.add(work4);
                BAActivityInLibrary work5 = new BAActivityInLibrary("meeting", EDUCATION_OR_CAREER, 0L, "ic_meeting");
                activityList.add(work5);
                BAActivityInLibrary work6 = new BAActivityInLibrary("research", EDUCATION_OR_CAREER, 0L, "ic_research");
                activityList.add(work6);
                BAActivityInLibrary work7 = new BAActivityInLibrary("study", EDUCATION_OR_CAREER, 0L, "ic_education");
                activityList.add(work7);
                BAActivityInLibrary social = new BAActivityInLibrary("social\nactivity", SOCIAL, 2L, "ic_social");
                activityList.add(social);
                BAActivityInLibrary social1 = new BAActivityInLibrary("romance", SOCIAL, 2L, "ic_romance");
                activityList.add(social1);
                BAActivityInLibrary social2 = new BAActivityInLibrary("friends", SOCIAL, 2L, "ic_friends");
                activityList.add(social2);
                BAActivityInLibrary social3 = new BAActivityInLibrary("date", SOCIAL, 2L, "ic_date");
                activityList.add(social3);
                BAActivityInLibrary social4 = new BAActivityInLibrary("night\nout", SOCIAL, 2L, "ic_night_out");
                activityList.add(social4);
                BAActivityInLibrary social5 = new BAActivityInLibrary("advocacy", SOCIAL, 2L, "ic_advocacy");
                activityList.add(social5);
                BAActivityInLibrary recreation = new BAActivityInLibrary("music", RECREATION_OR_INTERESTS, 3L, "ic_music");
                activityList.add(recreation);
                BAActivityInLibrary recreation1 = new BAActivityInLibrary("video", RECREATION_OR_INTERESTS, 3L, "ic_video");
                activityList.add(recreation1);
                BAActivityInLibrary recreation2 = new BAActivityInLibrary("tv", RECREATION_OR_INTERESTS, 3L, "ic_tv");
                activityList.add(recreation2);
                BAActivityInLibrary recreation3 = new BAActivityInLibrary("game", RECREATION_OR_INTERESTS, 3L, "ic_games");
                activityList.add(recreation3);
                BAActivityInLibrary recreation4 = new BAActivityInLibrary("movie", RECREATION_OR_INTERESTS, 3L, "ic_movie");
                activityList.add(recreation4);
                BAActivityInLibrary recreation5 = new BAActivityInLibrary("shopping", RECREATION_OR_INTERESTS, 3L, "ic_shopping");
                activityList.add(recreation5);
                BAActivityInLibrary recreation6 = new BAActivityInLibrary("web\nsurfing", RECREATION_OR_INTERESTS, 3L, "ic_web_surfing");
                activityList.add(recreation6);
                BAActivityInLibrary mps = new BAActivityInLibrary("reading", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_reading");
                activityList.add(mps);
                BAActivityInLibrary mps1 = new BAActivityInLibrary("meditation", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_meditation");
                activityList.add(mps1);
                BAActivityInLibrary mps2 = new BAActivityInLibrary("exercise", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_exercise");
                activityList.add(mps2);
                BAActivityInLibrary mps3 = new BAActivityInLibrary("prayer", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_prayer");
                activityList.add(mps3);
                BAActivityInLibrary mps4 = new BAActivityInLibrary("sports", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_sports");
                activityList.add(mps4);
                BAActivityInLibrary mps5 = new BAActivityInLibrary("workout", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_workout");
                activityList.add(mps5);
                BAActivityInLibrary mps6 = new BAActivityInLibrary("walk", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_walk");
                activityList.add(mps6);
                BAActivityInLibrary mps7 = new BAActivityInLibrary("run", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_run");
                activityList.add(mps7);
                BAActivityInLibrary mps8 = new BAActivityInLibrary("yoga", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_yoga");
                activityList.add(mps8);
                BAActivityInLibrary mps9 = new BAActivityInLibrary("eating", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_eating");
                activityList.add(mps9);
                BAActivityInLibrary mps10 = new BAActivityInLibrary("sleep", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_sleep");
                activityList.add(mps10);
                BAActivityInLibrary chore = new BAActivityInLibrary("cleaning", CHORE, 8L, "ic_chores");
                activityList.add(chore);
                BAActivityInLibrary chore1 = new BAActivityInLibrary("laundry", CHORE, 8L, "ic_laundry");
                activityList.add(chore1);
                BAActivityInLibrary chore2 = new BAActivityInLibrary("cooking", CHORE, 8L, "ic_cooking");
                activityList.add(chore2);
                BAActivityInLibrary chore3 = new BAActivityInLibrary("grooming", CHORE, 8L, "ic_grooming");
                activityList.add(chore3);
                BAActivityInLibrary chore4 = new BAActivityInLibrary("hygiene", CHORE, 8L, "ic_hygiene");
                activityList.add(chore4);
                for (BAActivityInLibrary activity : activityList) {
                    mDao.insert(activity);
                }
            }
            return null;
        }
    }
}
