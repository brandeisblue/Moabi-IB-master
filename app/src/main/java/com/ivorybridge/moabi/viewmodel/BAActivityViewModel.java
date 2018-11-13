package com.ivorybridge.moabi.viewmodel;

import android.app.Application;

import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityFavorited;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityInLibrary;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.BAActivityRepository;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class BAActivityViewModel extends AndroidViewModel {

    private static final String TAG = BAActivityViewModel.class.getSimpleName();
    private BAActivityRepository mRepository;
    private FirebaseManager firebaseManager;
    public Long EDUCATION_OR_CAREER = 0L;
    public Long SOCIAL = 1L;
    public Long RECREATION_OR_INTERESTS = 2L;
    public Long MIND_PHYSICAL_SPIRITUAL = 3L;
    public Long CHORE = 4L;

    public BAActivityViewModel(Application application) {
        super(application);
        this.mRepository = new BAActivityRepository(application);
        firebaseManager = new FirebaseManager();
    }

    public LiveData<List<BAActivityEntry>> getActivityEntries(Long start, Long end) {
        return this.mRepository.getActivityEntries(start, end);
    }

    public boolean addActivityEntry(BAActivityEntry summary, String date) {
        return mRepository.insertActivityEntry(summary, date);
    }

    public LiveData<List<BAActivityEntry>> getAllActivityEntries() {
        return this.mRepository.getAllActivityEntries();
    }

    public LiveData<List<BAActivityFavorited>> getAllFavoritedActivities() {
        return this.mRepository.getAllFavoritedActivities();
    }


    public LiveData<List<BAActivityInLibrary>> getAllActivitiesInLibrary() {
        return this.mRepository.getAllActivitiesInLibrary();
    }

    public void deleteAllFavoritedActivities() {
        this.mRepository.deleteAllFavoritedActivities();
    }

    public void deleteAllActivitiesInLibrary() {
        this.mRepository.deleteAllActivitiesInLibrary();
    }

    public void initializeBAActivityInLibrary() {
        List<BAActivityInLibrary> activityList = new ArrayList<>();
        BAActivityInLibrary work1 = new BAActivityInLibrary("Work", EDUCATION_OR_CAREER, 0L, "ic_work");
        activityList.add(work1);
        BAActivityInLibrary work2 = new BAActivityInLibrary("School", EDUCATION_OR_CAREER, 0L, "ic_school");
        activityList.add(work2);
        BAActivityInLibrary work3 = new BAActivityInLibrary("Networking", EDUCATION_OR_CAREER, 0L, "ic_networking");
        activityList.add(work3);
        BAActivityInLibrary work4 = new BAActivityInLibrary("Skills-Training", EDUCATION_OR_CAREER, 0L, "ic_skills_training");
        activityList.add(work4);
        BAActivityInLibrary work5 = new BAActivityInLibrary("Meeting", EDUCATION_OR_CAREER, 0L, "ic_meeting");
        activityList.add(work5);
        BAActivityInLibrary work6 = new BAActivityInLibrary("Research", EDUCATION_OR_CAREER, 0L, "ic_research");
        activityList.add(work6);
        BAActivityInLibrary work7 = new BAActivityInLibrary("Study", EDUCATION_OR_CAREER, 0L, "ic_education");
        activityList.add(work7);
        BAActivityInLibrary social = new BAActivityInLibrary("Social-Activity", SOCIAL, 2L, "ic_social");
        activityList.add(social);
        BAActivityInLibrary social1 = new BAActivityInLibrary("Romance", SOCIAL, 2L, "ic_romance");
        activityList.add(social1);
        BAActivityInLibrary social2 = new BAActivityInLibrary("Friends", SOCIAL, 2L, "ic_friends");
        activityList.add(social2);
        BAActivityInLibrary social3 = new BAActivityInLibrary("Date", SOCIAL, 2L, "ic_date");
        activityList.add(social3);
        BAActivityInLibrary social4 = new BAActivityInLibrary("Night-Out", SOCIAL, 2L, "ic_night_out");
        activityList.add(social4);
        BAActivityInLibrary social5 = new BAActivityInLibrary("Advocacy", SOCIAL, 2L, "ic_advocacy");
        activityList.add(social5);
        BAActivityInLibrary recreation = new BAActivityInLibrary("Music", RECREATION_OR_INTERESTS, 3L, "ic_music");
        activityList.add(recreation);
        BAActivityInLibrary recreation1 = new BAActivityInLibrary("Video", RECREATION_OR_INTERESTS, 3L, "ic_video");
        activityList.add(recreation1);
        BAActivityInLibrary recreation2 = new BAActivityInLibrary("TV", RECREATION_OR_INTERESTS, 3L, "ic_tv");
        activityList.add(recreation2);
        BAActivityInLibrary recreation3 = new BAActivityInLibrary("Game", RECREATION_OR_INTERESTS, 3L, "ic_games");
        activityList.add(recreation3);
        BAActivityInLibrary recreation4 = new BAActivityInLibrary("Movie", RECREATION_OR_INTERESTS, 3L, "ic_movie");
        activityList.add(recreation4);
        BAActivityInLibrary recreation5 = new BAActivityInLibrary("Shopping", RECREATION_OR_INTERESTS, 3L, "ic_shopping");
        activityList.add(recreation5);
        BAActivityInLibrary recreation6 = new BAActivityInLibrary("Web-Surfing", RECREATION_OR_INTERESTS, 3L, "ic_web_surfing");
        activityList.add(recreation6);
        BAActivityInLibrary mps = new BAActivityInLibrary("Reading", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_reading");
        activityList.add(mps);
        BAActivityInLibrary mps1 = new BAActivityInLibrary("Meditation", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_meditation");
        activityList.add(mps1);
        BAActivityInLibrary mps2 = new BAActivityInLibrary("Exercise", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_exercise");
        activityList.add(mps2);
        BAActivityInLibrary mps3 = new BAActivityInLibrary("Prayer", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_prayer");
        activityList.add(mps3);
        BAActivityInLibrary mps4 = new BAActivityInLibrary("Sports", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_sports");
        activityList.add(mps4);
        BAActivityInLibrary mps5 = new BAActivityInLibrary("Workout", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_workout");
        activityList.add(mps5);
        BAActivityInLibrary mps6 = new BAActivityInLibrary("Walk", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_walk");
        activityList.add(mps6);
        BAActivityInLibrary mps7 = new BAActivityInLibrary("Run", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_run");
        activityList.add(mps7);
        BAActivityInLibrary mps8 = new BAActivityInLibrary("Yoga", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_yoga");
        activityList.add(mps8);
        BAActivityInLibrary mps9 = new BAActivityInLibrary("Eating", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_eating");
        activityList.add(mps9);
        BAActivityInLibrary mps10 = new BAActivityInLibrary("Sleep", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_sleep");
        activityList.add(mps10);
        BAActivityInLibrary chore = new BAActivityInLibrary("Cleaning", CHORE, 8L, "ic_chores");
        activityList.add(chore);
        BAActivityInLibrary chore1 = new BAActivityInLibrary("Laundry", CHORE, 8L, "ic_laundry");
        activityList.add(chore1);
        BAActivityInLibrary chore2 = new BAActivityInLibrary("Cooking", CHORE, 8L, "ic_cooking");
        activityList.add(chore2);
        BAActivityInLibrary chore3 = new BAActivityInLibrary("Grooming", CHORE, 8L, "ic_grooming");
        activityList.add(chore3);
        BAActivityInLibrary chore4 = new BAActivityInLibrary("Hygiene", CHORE, 8L, "ic_hygiene");
        activityList.add(chore4);
        for (BAActivityInLibrary activity: activityList) {
            mRepository.insertActivityToLibrary(activity);
        }
    }

    public void initializeFavoritedActivities() {
        List<BAActivityFavorited> activityList = new ArrayList<>();
        BAActivityFavorited work1 = new BAActivityFavorited("Work", EDUCATION_OR_CAREER, 0L, "ic_work");
        activityList.add(work1);
        BAActivityFavorited work7 = new BAActivityFavorited("Study", EDUCATION_OR_CAREER, 0L, "ic_education");
        activityList.add(work7);
        BAActivityFavorited social = new BAActivityFavorited("Social-Activity", SOCIAL, 2L, "ic_social");
        activityList.add(social);
        BAActivityFavorited recreation = new BAActivityFavorited("Music", RECREATION_OR_INTERESTS, 3L, "ic_music");
        activityList.add(recreation);
        BAActivityFavorited recreation1 = new BAActivityFavorited("Video", RECREATION_OR_INTERESTS, 3L, "ic_video");
        activityList.add(recreation1);
        BAActivityFavorited mps = new BAActivityFavorited("Reading", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_reading");
        activityList.add(mps);
        BAActivityFavorited mps2 = new BAActivityFavorited("Exercise", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_exercise");
        activityList.add(mps2);
        BAActivityFavorited mps10 = new BAActivityFavorited("Sleep", MIND_PHYSICAL_SPIRITUAL, 5L, "ic_sleep");
        activityList.add(mps10);
        BAActivityFavorited chore4 = new BAActivityFavorited("Hygiene", CHORE, 8L, "ic_hygiene");
        activityList.add(chore4);
        for (BAActivityFavorited activity: activityList) {
            mRepository.insertFavoritedActivity(activity);
        }
    }
}
