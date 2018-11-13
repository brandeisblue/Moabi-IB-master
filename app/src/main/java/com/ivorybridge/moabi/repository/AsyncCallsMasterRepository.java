package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.AsyncTaskBooleanDao;
import com.ivorybridge.moabi.database.db.AsyncTaskBooleanDB;
import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;
import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.network.auth.GoogleFitService;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class AsyncCallsMasterRepository {

    private static final String TAG = AsyncCallsMasterRepository.class.getSimpleName();
    private FitbitRepository fitbitRepository;
    private GoogleFitService googleFitService;
    private GoogleFitRepository googleFitRepository;
    private AppUsageRepository appUsageRepository;
    private WeatherRepository weatherRepository;
    private MoodAndEnergyRepository moodAndEnergyRepository;
    private BAActivityRepository baActivityRepository;
    private TimedActivityRepository timedActivityRepository;
    private DataInUseRepository dataInUseRepository;
    private FormattedTime formattedTime;
    private Context context;
    private AsyncTaskBooleanDao mTaskSuccessDao;
    private String date;
    private Application application;


    public AsyncCallsMasterRepository(AppCompatActivity activity, String date) {
        this.fitbitRepository = new FitbitRepository(activity.getApplication());
        this.googleFitService = new GoogleFitService(activity);
        this.appUsageRepository = new AppUsageRepository(activity.getApplication());
        this.googleFitRepository = new GoogleFitRepository(activity.getApplication());
        this.weatherRepository = new WeatherRepository(activity.getApplication());
        this.moodAndEnergyRepository = new MoodAndEnergyRepository(activity.getApplication());
        this.baActivityRepository = new BAActivityRepository(activity.getApplication());
        this.timedActivityRepository = new TimedActivityRepository(activity.getApplication());
        this.dataInUseRepository = new DataInUseRepository(activity.getApplication());
        this.date = date;
        this.context = activity.getApplicationContext();
        AsyncTaskBooleanDB successDB = AsyncTaskBooleanDB.getDatabase(activity.getApplication());
        this.mTaskSuccessDao = successDB.asyncTaskBooleanDao();
        this.formattedTime = new FormattedTime();
        this.application = activity.getApplication();
    }

    public AsyncCallsMasterRepository(Application application, String date) {
        this.fitbitRepository = new FitbitRepository(application);
        this.googleFitService = new GoogleFitService(application);
        this.appUsageRepository = new AppUsageRepository(application);
        this.googleFitRepository = new GoogleFitRepository(application);
        this.weatherRepository = new WeatherRepository(application);
        this.moodAndEnergyRepository = new MoodAndEnergyRepository(application);
        this.baActivityRepository = new BAActivityRepository(application);
        this.timedActivityRepository = new TimedActivityRepository(application);
        this.dataInUseRepository = new DataInUseRepository(application);
        this.date = date;
        this.context = application.getApplicationContext();
        AsyncTaskBooleanDB successDB = AsyncTaskBooleanDB.getDatabase(application);
        this.mTaskSuccessDao = successDB.asyncTaskBooleanDao();
        this.formattedTime = new FormattedTime();
        this.application = application;
    }

    /*
    public AsyncCallsMasterRepository(Application application, String date) {
        this.fitbitRepository = new FitbitRepository(application);
        this.appUsageRepository = new AppUsageRepository(application);
        this.weatherRepository = new WeatherRepository(application);
        this.firebaseManager = new FirebaseManager();
        this.formattedTime = new FormattedTime();
        this.context = application;
        this.date = date;
        this.application = activity.getApplication();
    }*/

    public void makeCallsToConnectedServices() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<InputInUse> inputsInUse = dataInUseRepository.getAllInputsInUseNow();
                List<ConnectedService> connectedServices = dataInUseRepository.getAllConnectedServicesNow();
                if (inputsInUse != null && inputsInUse.size() > 0 &&
                        connectedServices != null && connectedServices.size() > 0) {
                    for (InputInUse inputInUse: inputsInUse) {
                        if (inputInUse.isInUse()) {
                            for (ConnectedService connectedService : connectedServices) {
                                if (connectedService.getName().equals(inputInUse.getName()) &&
                                        connectedService.isConnected) {
                                    String name = connectedService.getName();
                                    if (name.equals(application.getString(R.string.fitbit_camel_case))) {
                                        AsyncTaskBoolean appUsageTaskSuccess = new AsyncTaskBoolean(application.getString(R.string.fitbit_camel_case));
                                        appUsageTaskSuccess.setResult(false);
                                        insertSuccess(appUsageTaskSuccess);
                                        fitbitRepository.downloadData(date);
                                        fitbitRepository.downloadData(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(date, 1));
                                    } else if (name.equals(application.getString(R.string.phone_usage_camel_case))) {
                                        AsyncTaskBoolean appUsageTaskSuccess = new AsyncTaskBoolean(application.getString(R.string.phone_usage_camel_case));
                                        appUsageTaskSuccess.setResult(false);
                                        insertSuccess(appUsageTaskSuccess);
                                        appUsageRepository.query(date);
                                        appUsageRepository.query(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(date, 1));
                                    } else if (name.equals(application.getString(R.string.googlefit_camel_case))) {
                                        AsyncTaskBoolean appUsageTaskSuccess = new AsyncTaskBoolean(application.getString(R.string.googlefit_camel_case));
                                        appUsageTaskSuccess.setResult(false);
                                        insertSuccess(appUsageTaskSuccess);
                                        googleFitService.downloadData(date);
                                        googleFitService.downloadData(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(date, 1));
                                    } else if (name.equals(application.getString(R.string.weather_camel_case))) {
                                        AsyncTaskBoolean appUsageTaskSuccess = new AsyncTaskBoolean(application.getString(R.string.weather_camel_case));
                                        appUsageTaskSuccess.setResult(false);
                                        insertSuccess(appUsageTaskSuccess);
                                        if (!date.equals(formattedTime.getCurrentDateAsYYYYMMDD())) {
                                            weatherRepository.queryWeatherHistory(date);
                                        } else {
                                            weatherRepository.queryCurrentWeather();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }).start();
    }

    public void sync() {
        // if has data...
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                appUsageRepository.sync();
                googleFitRepository.sync();
                fitbitRepository.sync();
                moodAndEnergyRepository.sync();
                baActivityRepository.sync();
                timedActivityRepository.sync();
            }
        });
        thread.start();
    }


    private boolean insertSuccess(AsyncTaskBoolean asyncTaskSuccess) {
        AsyncTask.Status status = new insertSuccessAsyncTask(mTaskSuccessDao).execute(asyncTaskSuccess).getStatus();
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
}
