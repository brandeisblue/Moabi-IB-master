package com.ivorybridge.moabi;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.FirebaseDatabase;
import com.ivorybridge.moabi.service.AndroidJobCreator;
import com.jakewharton.threetenabp.AndroidThreeTen;

import io.fabric.sdk.android.Fabric;

public class MoabiApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        AppEventsLogger.activateApp(this);
        AndroidThreeTen.init(this);
        JobManager.create(this).addJobCreator(new AndroidJobCreator(this));

        /*
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    //.detectDiskReads()
                    //.detectDiskWrites()
                    .detectCustomSlowCalls()
                    //.detectResourceMismatches()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    //.penaltyDeath()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    //.detectAll()
                    .penaltyLog()
                    //.penaltyDeath()
                    .build());
        }*/
    }


}
