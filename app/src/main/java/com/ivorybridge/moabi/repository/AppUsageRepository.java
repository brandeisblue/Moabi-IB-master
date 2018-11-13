package com.ivorybridge.moabi.repository;

import android.app.AppOpsManager;
import android.app.Application;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.AppUsageDao;
import com.ivorybridge.moabi.database.dao.AsyncTaskBooleanDao;
import com.ivorybridge.moabi.database.db.AppUsageDB;
import com.ivorybridge.moabi.database.db.AsyncTaskBooleanDB;
import com.ivorybridge.moabi.database.entity.appusage.AppUsage;
import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.database.entity.appusage.CustomUsageSummary;
import com.ivorybridge.moabi.database.entity.appusage.Stat;
import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;
import com.ivorybridge.moabi.database.entity.util.InputHistory;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.util.FormattedTime;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class AppUsageRepository {

    //TODO - figure out if I want async task.
    private static final String TAG = AppUsageRepository.class.getSimpleName();
    private FirebaseManager firebaseManager;
    private UsageStatsManager mUsageStatsManager;
    private AsyncTaskBooleanDao mTaskBooleanDao;
    private Application application;
    private AppUsageDao mUsageDao;
    private InputHistoryRepository inputHistoryRepository;
    private FormattedTime formattedTime;

    public AppUsageRepository(Application application) {
        this.application = application;
        AppUsageDB appUsageDB = AppUsageDB.getDatabase(application);
        mUsageDao = appUsageDB.AppUsageDao();
        AsyncTaskBooleanDB successDB = AsyncTaskBooleanDB.getDatabase(application);
        mTaskBooleanDao = successDB.asyncTaskBooleanDao();
        firebaseManager = new FirebaseManager();
        inputHistoryRepository = new InputHistoryRepository(application);
        formattedTime = new FormattedTime();
    }

    public LiveData<AppUsageSummary> get(String date) {
        return mUsageDao.get(date);
    }

    public LiveData<List<AppUsageSummary>> getAll() {
        return mUsageDao.getAll();
    }

    public LiveData<List<AppUsageSummary>> getAll(Long start, Long end) {
        return mUsageDao.getAll(start, end);
    }

    public List<AppUsageSummary> getAllNow(Long start, Long end) {
        return mUsageDao.getAllNow(start, end);
    }

    public boolean query(String date) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                queryAppUsage(date);
            }
        });
        thread.start();
        return true;
        /*
        AsyncTask.Status status = new queryAsyncTask(this).execute(date).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }*/
    }

    public void sync() {
        firebaseManager.getThisDeviceRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dateSnap : dataSnapshot.getChildren()) {
                    AppUsageSummary appUsageSummary = new AppUsageSummary();
                    List<AppUsage> appUsageList = new ArrayList<>();
                    String date = "";
                    if (dateSnap.getKey() != null) {
                        date = dateSnap.getKey();
                        appUsageSummary.setDate(date);
                    }
                    for (DataSnapshot appSnap : dateSnap.getChildren()) {
                        if (appSnap.getKey() != null && appSnap.hasChildren()) {
                            AppUsage appUsage = appSnap.getValue(AppUsage.class);
                            appUsageList.add(appUsage);
                        }
                    }
                    appUsageSummary.setActivities(appUsageList);
                    appUsageSummary.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
                    appUsageSummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                    insertAppUsage(appUsageSummary, date);
                    //firebaseManager.getDaysWithDataRef().child(dateSnap.getKey()).child(application.getString(R.string.phone_usage_camel_case)).setValue(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private boolean insertAppUsage(AppUsageSummary appUsageSummary, String date) {
        InputHistory inputHistory = new InputHistory();
        inputHistory.setInputType(application.getString(R.string.phone_usage_camel_case));
        inputHistory.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
        inputHistory.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
        inputHistory.setDate(date);
        inputHistoryRepository.insert(inputHistory);
        AsyncTask.Status status = new insertAppUsageAsyncTask(mUsageDao).execute(appUsageSummary).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private void queryAppUsage(String date) {
        if (isUsagePermissionGranted()) {
            mUsageStatsManager = (UsageStatsManager) application.getSystemService(Context.USAGE_STATS_SERVICE);
            List<Stat> listToSort = getDailyUsageStatisticsWithCustomQuery(date);
            Collections.sort(listToSort);
            Map<String, Object> mapToUpdate = new LinkedHashMap<>();
            AppUsage appUsageToUpload = new AppUsage();
            AppUsageSummary appUsage = new AppUsageSummary();
            appUsage.setDate(date);
            List<AppUsage> activitiesList = new ArrayList<>();
            for (int i = 0; i < listToSort.size(); i++) {
                long minute = (listToSort.get(i).getTotalTime() / (1000 * 60)) % 60;
                long hour = (listToSort.get(i).getTotalTime() / (1000 * 60 * 60)) % 24;
                String timeElapsed = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                Log.i(TAG, listToSort.get(i).getAppName() + ": " + timeElapsed);
                if (!listToSort.get(i).getAppName().contains(".")
                        && !listToSort.get(i).getAppName().contains("#")
                        && !listToSort.get(i).getAppName().contains("$")
                        && !listToSort.get(i).getAppName().contains("[")
                        && !listToSort.get(i).getAppName().contains("]")) {
                    appUsageToUpload = new AppUsage(listToSort.get(i).getAppName(), listToSort.get(i).getTotalTime(), i + 1L);
                    activitiesList.add(appUsageToUpload);
                    mapToUpdate.put(appUsageToUpload.getAppName(), appUsageToUpload);
                }
            }
            appUsage.setActivities(activitiesList);
            appUsage.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
            appUsage.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
            insertAppUsage(appUsage, date);
            AsyncTaskBoolean appUsageAsyncBoolean = new AsyncTaskBoolean(application.getString(R.string.phone_usage_camel_case));
            appUsageAsyncBoolean.setResult(true);
            insertSuccess(appUsageAsyncBoolean);
            InputHistory appUsageInputHistory = new InputHistory();
            appUsageInputHistory.setInputType(application.getString(R.string.phone_usage_camel_case));
            appUsageInputHistory.setDate(date);
            appUsageInputHistory.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
            appUsageInputHistory.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
            inputHistoryRepository.insert(appUsageInputHistory);
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                firebaseManager.getAppUsageRef().child(date).updateChildren(mapToUpdate);
                firebaseManager.getAppUsageLast30DaysRef().child(date).updateChildren(mapToUpdate);
                firebaseManager.getDaysWithDataRef().child(date).child(application.getString(R.string.phone_usage_camel_case)).setValue(true);
            }
        }
    }

    private List<Stat> getDailyUsageStatisticsWithCustomQuery(String date) {
        // mark the start of an async task
        AsyncTaskBoolean appUsageAsyncBoolean = new AsyncTaskBoolean(application.getString(R.string.phone_usage_camel_case));
        appUsageAsyncBoolean.setResult(false);
        insertSuccess(appUsageAsyncBoolean);

        long timeNow = System.currentTimeMillis();
        long start = formattedTime.getStartOfDay(date);
        long endOfDay = formattedTime.getEndOfDay(date);
        Log.i(TAG, "Querying from " + formattedTime.convertLongToMDHHMMaa(start) + " to " + formattedTime.convertLongToMDHHMMaa(endOfDay));
        Set<String> appNameSet = new HashSet<>();
        Long totalUsageTime = 0L;
        List<Stat> stats = new ArrayList<>();
        List<Stat> finalStats = new ArrayList<>();
        Map<String, CustomUsageSummary> sortedUsageMap = new LinkedHashMap<>();
        UsageEvents systemEvents = mUsageStatsManager.queryEvents(start, endOfDay);
        PackageManager pm = application.getPackageManager();
        
        Map<String, Long> appUsageMap = new LinkedHashMap<>();
        long startTime = 0;
        long endTime = 0;
        long totalTime = 0;
        long previousEventType = 0;
        String pkgName = "";
        String appName = "";

        while (systemEvents.hasNextEvent()) {
            // get event
            UsageEvents.Event event = new UsageEvents.Event();
            systemEvents.getNextEvent(event);

            //Log.i(TAG, event.getPackageName() + ": " + "Event type - " + event.getEventType() + " at " + formattedTime.convertLongToHHMM(event.getTimeStamp()) + "(" + event.getTimeStamp() + ")");
             if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND || event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                 if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                     startTime = event.getTimeStamp();
                     previousEventType = event.getEventType();
                     pkgName = event.getPackageName();
                 } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                     endTime = event.getTimeStamp();
                     if (previousEventType == UsageEvents.Event.MOVE_TO_FOREGROUND && pkgName.equals(event.getPackageName())) {
                         previousEventType = event.getEventType();
                         pkgName = event.getPackageName();
                         appName = pkgName;
                         Drawable appIcon;
                         try {
                             appName = pm.getApplicationInfo(pkgName, 0).loadLabel(pm).toString();
                         } catch (PackageManager.NameNotFoundException e) {
                             appName = "Unknown";
                             e.printStackTrace();
                         }
                         // get the app icon
                         try {
                             appIcon = pm.getApplicationIcon(event.getPackageName());
                         } catch (PackageManager.NameNotFoundException e) {
                             e.printStackTrace();
                             appIcon = ContextCompat
                                     .getDrawable(application.getApplicationContext(), R.drawable.ic_android);
                         }

                         // If both start and end are defined, we have a session
                         if (startTime != 0L && endTime != 0L) {

                             // this take cares of a case when there are multiple background events following a foreground event
                             if (startTime > endTime) {
                                 endTime = 0L;
                             } else {
                                 // Add the session time to the total time
                                 totalTime += endTime - startTime;
                                 // Reset the start/end times to 0
                                 startTime = 0L;
                                 endTime = 0L;
                             }
                         }

                         if (sortedUsageMap.get(pkgName) != null) {
                             CustomUsageSummary customUsageSummary = sortedUsageMap.get(pkgName);
                             long old = customUsageSummary.totalTime;
                             customUsageSummary.appName = appName;
                             customUsageSummary.appIcon = appIcon;
                             customUsageSummary.totalTime = old + totalTime;
                             sortedUsageMap.put(pkgName, customUsageSummary);
                         } else {
                             CustomUsageSummary customUsageSummary = new CustomUsageSummary();
                             customUsageSummary.appName = appName;
                             customUsageSummary.appIcon = appIcon;
                             customUsageSummary.totalTime = totalTime;
                             sortedUsageMap.put(pkgName, customUsageSummary);
                         }
                         //Log.i(TAG, appName + ": " + "Event type - " + event.getEventType() + " at " + formattedTime.convertLongToHHMM(event.getTimeStamp()));
                         //Log.i(TAG, pkgName + ": " + sortedUsageMap.get(pkgName).totalTime);

                         totalTime = 0;
                         pkgName = event.getPackageName();

                     } else {

                         startTime = 0;
                         endTime = 0;
                         totalTime = 0;
                         pkgName = event.getPackageName();

                     }
                 }
            }
        }

        for (Map.Entry<String, CustomUsageSummary> entry: sortedUsageMap.entrySet()) {
            Log.i(TAG, entry.getKey() + ": " + entry.getValue().totalTime);
            Stat stat = new Stat();
            stat.setAppName(entry.getValue().appName);
            stat.setTotalTime(entry.getValue().totalTime);
            finalStats.add(stat);
        }

        //Log.i(TAG, "Number of apps: " + finalStats.size());
        for (Stat s : finalStats) {
            if (s.getTotalTime() > 0) {
                totalUsageTime += s.getTotalTime();
            } else {
                totalUsageTime += 0;
            }
            long minute = (s.getTotalTime() / (1000 * 60)) % 60;
            long hour = (s.getTotalTime() / (1000 * 60 * 60)) % 24;
            String timeElapsed = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            Log.i(TAG, s.getAppName() + ": " + timeElapsed + " " + s.getTotalTime());
        }
        Stat totalUsageStat = new Stat();
        totalUsageStat.setAppName("Total");
        totalUsageStat.setTotalTime(totalUsageTime);
        finalStats.add(totalUsageStat);
        long startMinute = (start / (1000 * 60)) % 60;
        long startHour = (start / (1000 * 60 * 60)) % 24;
        long lastMinute = (timeNow / (1000 * 60)) % 60;
        long lastHour = (timeNow / (1000 * 60 * 60)) % 24;
        //String timeElapsed = String.format(Locale.getDefault(), "Start - %02d:%02d, Last - %02d:%02d", startHour, startMinute, lastHour, lastMinute);
        //Log.i(TAG, timeElapsed);
        return finalStats;
    }


    private static class queryAsyncTask extends AsyncTask<String, Void, Void> {

        private WeakReference<AppUsageRepository> weakReference;

        queryAsyncTask(AppUsageRepository context) {
            this.weakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... strings) {
            AppUsageRepository repository = weakReference.get();
            repository.queryAppUsage(strings[0]);
            return null;
        }
    }

    private static class insertAppUsageAsyncTask extends AsyncTask<AppUsageSummary, Void, Void> {

        private AppUsageDao mAsyncTaskDao;

        insertAppUsageAsyncTask(AppUsageDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(AppUsageSummary... appUsageSummaries) {
            mAsyncTaskDao.insert(appUsageSummaries[0]);
            return null;
        }
    }

    private boolean insertSuccess(AsyncTaskBoolean asyncTaskSuccess) {
        AsyncTask.Status status = new insertSuccessAsyncTask(mTaskBooleanDao).execute(asyncTaskSuccess).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertSuccessAsyncTask extends AsyncTask<AsyncTaskBoolean, Void, Void> {

        private AsyncTaskBooleanDao mAsyncTaskDao;

        insertSuccessAsyncTask(AsyncTaskBooleanDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final AsyncTaskBoolean... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }


    private boolean isUsagePermissionGranted() {
        AppOpsManager appOps = (AppOpsManager) application.getSystemService(Context.APP_OPS_SERVICE);
        int mode = -1;
        try {
            if (appOps != null) {
                mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), application.getPackageName());
            }
            return mode == MODE_ALLOWED;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class LastTimeLaunchedComparatorDesc implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats left, UsageStats right) {
            return Long.compare(right.getLastTimeUsed(), left.getLastTimeUsed());
        }
    }
}
