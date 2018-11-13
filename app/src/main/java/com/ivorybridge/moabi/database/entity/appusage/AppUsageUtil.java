package com.ivorybridge.moabi.database.entity.appusage;

import android.app.usage.UsageStats;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.ArrayMap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AppUsageUtil {

    private static final String TAG = AppUsageUtil.class.getSimpleName();
    private Map<String, UsageStats> usageStatsMap;
    private List<UsageStats> usageStatsList;
    // Constants defining order for display order
    private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
    private static final int _DISPLAY_ORDER_LAST_TIME_USED = 1;
    private static final int _DISPLAY_ORDER_APP_NAME = 2;
    private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
    private static final boolean localLOGV = false;
    private LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
    private UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
    private AppNameComparator mAppLabelComparator;
    private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
    private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();
    private PackageManager mPm;

    public AppUsageUtil(Context context, Map<String, UsageStats> stats, List<UsageStats> statsList) {
        this.usageStatsMap = stats;
        this.usageStatsList = statsList;
        this.mPm = context.getPackageManager();

        ArrayMap<String, UsageStats> map = new ArrayMap<>();
        final int statCount = usageStatsList.size();
        for (int i = 0; i < statCount; i++) {
            final UsageStats pkgStats = usageStatsList.get(i);

            // load application labels for each application
            try {
                ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                String label = appInfo.loadLabel(mPm).toString();
                mAppLabelMap.put(pkgStats.getPackageName(), label);

                UsageStats existingStats =
                        map.get(pkgStats.getPackageName());
                if (existingStats == null) {
                    map.put(pkgStats.getPackageName(), pkgStats);
                } else {
                    existingStats.add(pkgStats);
                }

            } catch (PackageManager.NameNotFoundException e) {
                // This package may be gone.
            }
        }
        mPackageStats.addAll(map.values());

        // Sort list
        mAppLabelComparator = new AppNameComparator(mAppLabelMap);
        sortList();
    }

    public ArrayList<UsageStats> getUsageStatsList() {
        Log.i(TAG, mPackageStats.toString());
        return mPackageStats;
    }

    public ArrayMap<String, String> getAppLabelMap() {
        return mAppLabelMap;
    }

    public static class AppNameComparator implements Comparator<UsageStats> {
        private Map<String, String> mAppLabelList;

        AppNameComparator(Map<String, String> appList) {
            mAppLabelList = appList;
        }

        @Override
        public final int compare(UsageStats a, UsageStats b) {
            String alabel = mAppLabelList.get(a.getPackageName());
            String blabel = mAppLabelList.get(b.getPackageName());
            return alabel.compareTo(blabel);
        }
    }

    public static class LastTimeUsedComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            // return by descending order
            return (int)(b.getLastTimeUsed() - a.getLastTimeUsed());
        }
    }

    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            return (int)(b.getTotalTimeInForeground() - a.getTotalTimeInForeground());
        }
    }

    private void sortList(int sortOrder) {
        if (mDisplayOrder == sortOrder) {
            // do nothing
            return;
        }
        mDisplayOrder= sortOrder;
        sortList();
    }

    private void sortList() {
        if (mDisplayOrder == _DISPLAY_ORDER_USAGE_TIME) {
            if (localLOGV) Log.i(TAG, "Sorting by usage time");
            Collections.sort(mPackageStats, mUsageTimeComparator);
        } else if (mDisplayOrder == _DISPLAY_ORDER_LAST_TIME_USED) {
            if (localLOGV) Log.i(TAG, "Sorting by last time used");
            Collections.sort(mPackageStats, mLastTimeUsedComparator);
        } else if (mDisplayOrder == _DISPLAY_ORDER_APP_NAME) {
            if (localLOGV) Log.i(TAG, "Sorting by application name");
            Collections.sort(mPackageStats, mAppLabelComparator);
        }
        //notifyDataSetChanged();
    }
}
