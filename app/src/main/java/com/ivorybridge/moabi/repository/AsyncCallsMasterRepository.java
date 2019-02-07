package com.ivorybridge.moabi.repository;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.AsyncTaskBooleanDao;
import com.ivorybridge.moabi.database.db.AsyncTaskBooleanDB;
import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;
import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.network.auth.GoogleFitAPI;
import com.ivorybridge.moabi.service.MotionSensorService;
import com.ivorybridge.moabi.service.UserGoalJob;
import com.ivorybridge.moabi.service.UserGoalPeriodicJob;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class AsyncCallsMasterRepository {

    private static final String TAG = AsyncCallsMasterRepository.class.getSimpleName();
    private FitbitRepository fitbitRepository;
    private GoogleFitAPI googleFitAPI;
    private GoogleFitRepository googleFitRepository;
    private AppUsageRepository appUsageRepository;
    private WeatherRepository weatherRepository;
    private MoodAndEnergyRepository moodAndEnergyRepository;
    private BAActivityRepository baActivityRepository;
    private TimedActivityRepository timedActivityRepository;
    private DepressionRepository depressionRepository;
    private AnxietyRepository anxietyRepository;
    private DailyReviewRepository dailyReviewRepository;
    private StressRepository stressRepository;
    private BuiltInFitnessRepository builtInFitnessRepository;
    private DataInUseRepository dataInUseRepository;
    private FormattedTime formattedTime;
    private Context context;
    private AsyncTaskBooleanDao mTaskSuccessDao;
    private String date;
    private Application application;


    public AsyncCallsMasterRepository(AppCompatActivity activity, String date) {
        this.fitbitRepository = new FitbitRepository(activity.getApplication());
        this.googleFitAPI = new GoogleFitAPI(activity);
        this.appUsageRepository = new AppUsageRepository(activity.getApplication());
        this.googleFitRepository = new GoogleFitRepository(activity.getApplication());
        this.weatherRepository = new WeatherRepository(activity.getApplication());
        this.moodAndEnergyRepository = new MoodAndEnergyRepository(activity.getApplication());
        this.baActivityRepository = new BAActivityRepository(activity.getApplication());
        this.timedActivityRepository = new TimedActivityRepository(activity.getApplication());
        this.depressionRepository = new DepressionRepository(activity.getApplication());
        this.anxietyRepository = new AnxietyRepository(activity.getApplication());
        this.stressRepository = new StressRepository(activity.getApplication());
        this.dailyReviewRepository = new DailyReviewRepository(activity.getApplication());
        this.builtInFitnessRepository = new BuiltInFitnessRepository(activity.getApplication());
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
        this.googleFitAPI = new GoogleFitAPI(application);
        this.appUsageRepository = new AppUsageRepository(application);
        this.googleFitRepository = new GoogleFitRepository(application);
        this.weatherRepository = new WeatherRepository(application);
        this.moodAndEnergyRepository = new MoodAndEnergyRepository(application);
        this.baActivityRepository = new BAActivityRepository(application);
        this.timedActivityRepository = new TimedActivityRepository(application);
        this.depressionRepository = new DepressionRepository(application);
        this.anxietyRepository = new AnxietyRepository(application);
        this.stressRepository = new StressRepository(application);
        this.dailyReviewRepository = new DailyReviewRepository(application);
        this.builtInFitnessRepository = new BuiltInFitnessRepository(application);
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
                                        googleFitAPI.downloadData(date);
                                        googleFitAPI.downloadData(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(date, 1));
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
                                    Intent intent = new Intent(application, MotionSensorService.class);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent);
                                    } else {
                                        context.startService(intent);
                                    }
                                    SharedPreferences notificationSharedPreferences =
                                            application.getSharedPreferences(application.getString(R.string.com_ivorybridge_mobai_NOTIFICATION_SHARED_PREFERENCE),
                                    Context.MODE_PRIVATE);
                                    boolean isPersonalGoalNotifEnabled = notificationSharedPreferences.getBoolean(
                                            application.getString(R.string.preference_personal_goal_notification), false);
                                    if (isPersonalGoalNotifEnabled) {
                                        UserGoalJob.runJobImmediately();
                                        UserGoalPeriodicJob.schedulePeriodicJob();
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
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                appUsageRepository.sync();
                googleFitRepository.sync();
                fitbitRepository.sync();
                moodAndEnergyRepository.sync();
                baActivityRepository.sync();
                timedActivityRepository.sync();
                depressionRepository.sync();
                anxietyRepository.sync();
                stressRepository.sync();
                dailyReviewRepository.sync();
                builtInFitnessRepository.sync();
                weatherRepository.sync();
            }
        });
        thread.start();
    }


    private boolean insertSuccess(AsyncTaskBoolean asyncTaskSuccess) {
        AsyncTask.Status status = new insertSuccessAsyncTask(mTaskSuccessDao).execute(asyncTaskSuccess).getStatus();
        return status.equals(AsyncTask.Status.FINISHED);
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
