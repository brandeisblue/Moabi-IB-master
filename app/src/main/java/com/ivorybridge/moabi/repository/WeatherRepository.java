package com.ivorybridge.moabi.repository;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.AsyncTaskBooleanDao;
import com.ivorybridge.moabi.database.dao.WeatherDailySummaryDao;
import com.ivorybridge.moabi.database.db.AsyncTaskBooleanDB;
import com.ivorybridge.moabi.database.db.WeatherDB;
import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;
import com.ivorybridge.moabi.database.entity.util.InputHistory;
import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.util.FormattedTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;

public class WeatherRepository {

    private static final String TAG = WeatherRepository.class.getSimpleName();
    private static WeatherRepository instance = null;
    private SharedPreferences weatherSharedPreferences;
    // TODO - Protect the API Key (Convert it to a cryptic key)
    private String WEATHER_CURRENT_REQUEST_URL =
            "http://api.apixu.com/v1/forecast.json?key=4b5c89b7c215496c818191344172911";
    private String WEATHER_HISTORY_REQUEST_URL =
            "http://api.apixu.com/v1/history.json?key=4b5c89b7c215496c818191344172911";
    RequestQueue queue;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Context context;
    private double latitude;
    private double longitude;
    private AsyncTaskBooleanDao mTaskSuccessDao;
    private WeatherDailySummaryDao weatherDao;
    private FormattedTime formattedTime;
    private FirebaseManager firebaseManager;
    private InputHistoryRepository inputHistoryRepository;
    private Application application;


    public WeatherRepository(Application application) {
        weatherSharedPreferences
                = application.getSharedPreferences(application.getString(
                        R.string.com_ivorybridge_moabi_WEATHER_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        context = application;
        queue = Volley.newRequestQueue(application);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application);
        AsyncTaskBooleanDB successDB = AsyncTaskBooleanDB.getDatabase(application);
        this.mTaskSuccessDao = successDB.asyncTaskBooleanDao();
        WeatherDB weatherDB = WeatherDB.getDatabase(application);
        this.weatherDao = weatherDB.weatherDailySummaryDao();
        this.formattedTime = new FormattedTime();
        this.firebaseManager = new FirebaseManager();
        this.inputHistoryRepository = new InputHistoryRepository(application);
        this.application = application;
    }

    public LiveData<List<WeatherDailySummary>> getAll(Long start, Long end) {
        return this.weatherDao.getAll(start, end);
    }

    public List<WeatherDailySummary> getAllNow(Long start, Long end) {
        return this.weatherDao.getAllNow(start, end);
    }

    public void queryCurrentWeather() {
        Log.i(TAG, "querying current weather");
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        String queryURL = WEATHER_CURRENT_REQUEST_URL + "&q=" +
                                latitude
                                + "," + longitude + "&days=1";
                        makeCurrentWeatherRequest(queryURL);
                    }
                }
            });
        } else {
            Toast.makeText(context, "To get weather updates, we need to access your location", Toast.LENGTH_LONG).show();
        }
    }

    public void queryWeatherHistory(String date) {
        Log.i(TAG, "querying weather history");
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        String queryURL = WEATHER_HISTORY_REQUEST_URL + "&q=" +
                                latitude
                                + "," + longitude + "&dt=" + date;
                        makeWeatherHistoryRequest(queryURL);
                    }
                }
            });
        } else {
            Toast.makeText(context, "To get weather updates, we need to access your location", Toast.LENGTH_LONG).show();
        }
    }

    private void makeCurrentWeatherRequest(final String requestURL) {
        Log.i(TAG, "makeCurrentWeatherRequest() called");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        AsyncTaskBoolean taskSuccess = new AsyncTaskBoolean(application.getString(R.string.weather_camel_case));
                        taskSuccess.setResult(true);
                        insertSuccess(taskSuccess);
                        processCurrentWeather(response);
                        Log.i(TAG, "Response is " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse errorRes = error.networkResponse;
                String stringData = "";
                if (errorRes != null && errorRes.data != null) {
                    try {
                        stringData = new String(errorRes.data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "Error response is " + stringData);
            }
        }) {
        };
        queue.add(stringRequest);
    }

    private void makeWeatherHistoryRequest(final String requestURL) {
        Log.i(TAG, "makeWeatherHistoryRequest() called");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        AsyncTaskBoolean taskSuccess = new AsyncTaskBoolean(application.getString(R.string.weather_camel_case));
                        taskSuccess.setResult(true);
                        insertSuccess(taskSuccess);
                        processWeatherHistory(response);
                        Log.i(TAG, "Response is " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse errorRes = error.networkResponse;
                String stringData = "";
                if (errorRes != null && errorRes.data != null) {
                    try {
                        stringData = new String(errorRes.data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "Error response is " + stringData);
            }
        }) {
        };
        queue.add(stringRequest);
    }

    private void processCurrentWeather(String strjsonResponse) {
        Log.i(TAG, "processCurrentWeather called");
        String mLocation = "";
        double mTempHighC;
        double mTempHighF;
        double mTempLowC;
        double mTempLowF;
        double mTempNowC;
        double mTempNowF;
        String mWeatherConditionNow = "";
        int isDay;
        double mAveHumidity;
        double mRainChance;
        double mSnowChance;
        double totalprecipmm;
        double totalprecipin;
        String mImageUrl = "";
        try {
            Log.i(TAG, "Parsing weather JSON");
            JSONObject jsonRootObject = new JSONObject(strjsonResponse);
            JSONObject location = jsonRootObject.optJSONObject("location");
            JSONObject current = jsonRootObject.optJSONObject("current");
            JSONObject forecast = jsonRootObject.optJSONObject("forecast");
            mLocation = location.optString("name");
            Log.i(TAG, "City is " + mLocation);
            mTempNowC = current.optDouble("temp_c");
            mTempNowF = current.optDouble("temp_f");
            Log.i(TAG, "Current temp is " + mTempNowC);
            isDay = current.optInt("is_day");
            Log.i(TAG, "The code for day is " + isDay);
            JSONObject conditionCurrent = current.optJSONObject("condition");
            mImageUrl = conditionCurrent.optString("icon");
            mImageUrl = "http:" + mImageUrl.trim();
            Log.i(TAG, "Image URL is " + mImageUrl);
            mWeatherConditionNow = conditionCurrent.optString("text");
            Log.i(TAG, "Weather condition is " + mWeatherConditionNow);
            JSONArray forecastDayArray = forecast.optJSONArray("forecastday");
            JSONObject forecastDay = forecastDayArray.optJSONObject(0);
            JSONObject forecastToday = forecastDay.optJSONObject("day");
            JSONObject astroToday = forecastDay.optJSONObject("astro");
            JSONArray todayByHour = forecastDay.optJSONArray("hour");
            JSONObject currentHour = todayByHour.optJSONObject(0);
            mTempHighC = forecastToday.optDouble("maxtemp_c");
            Log.i(TAG, "Max temp is " + mTempHighC);
            mTempHighF = forecastToday.optDouble("maxtemp_f");
            Log.i(TAG, "Max temp is " + mTempHighF);
            mTempLowC = forecastToday.optDouble("mintemp_c");
            Log.i(TAG, "Min temp is " + mTempLowC);
            mTempLowF = forecastToday.optDouble("mintemp_f");
            Log.i(TAG, "Min temp is " + mTempLowF);
            mAveHumidity = forecastToday.optDouble("avghumidity");
            Log.i(TAG, "Average humidity is " + mAveHumidity);
            totalprecipmm = forecastToday.optDouble("totalprecip_mm");
            totalprecipin = forecastToday.optDouble("totalprecip_in");
            mRainChance = currentHour.optDouble("chance_of_rain");
            Log.i(TAG, "Chance of rain is " + mRainChance);
            mSnowChance = currentHour.optDouble("chance_of_snow");
            Log.i(TAG, "Chance of snow is " + mSnowChance);
            DecimalFormat decimalFormat = new DecimalFormat("#");
            SharedPreferences.Editor editor = weatherSharedPreferences.edit();
            editor.putString("location", mLocation);
            editor.putString("tempHighC", decimalFormat.format(mTempHighC));
            editor.putString("tempLowC", decimalFormat.format(mTempLowC));
            editor.putString("tempNowC", decimalFormat.format(mTempNowC));
            editor.putString("tempNowF", decimalFormat.format(mTempNowF));
            editor.putString("aveHumidity", decimalFormat.format(mAveHumidity));
            editor.putString("weatherConditionNow", mWeatherConditionNow);
            editor.putString("timeOfDay", Integer.toString(isDay));
            editor.putString("imageUrl", mImageUrl);
            editor.putString("rainChance", decimalFormat.format(mRainChance));
            editor.putString("snowChance", decimalFormat.format(mSnowChance));
            editor.apply();
            WeatherDailySummary weatherDailySummary = new WeatherDailySummary();
            weatherDailySummary.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
            weatherDailySummary.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
            weatherDailySummary.setAvgTempC(mTempNowC);
            weatherDailySummary.setAvgTempF(mTempNowF);
            weatherDailySummary.setMaxTempC(mTempHighC);
            weatherDailySummary.setMaxTempF(mTempHighF);
            weatherDailySummary.setMinTempC(mTempLowC);
            weatherDailySummary.setMinTempF(mTempLowF);
            weatherDailySummary.setCondition(mWeatherConditionNow);
            weatherDailySummary.setTotalPrecipmm(totalprecipmm);
            weatherDailySummary.setTotalPrecipin(totalprecipin);
            weatherDailySummary.setAvgHumidity(mAveHumidity);
            weatherDailySummary.setImageUrl(mImageUrl);
            insert(weatherDailySummary);
            InputHistory inputHistory = new InputHistory();
            inputHistory.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
            inputHistory.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
            inputHistory.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
            inputHistory.setInputType(application.getString(R.string.weather_camel_case));
            inputHistoryRepository.insert(inputHistory);
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                firebaseManager.getDaysWithDataRef().child(formattedTime.getCurrentDateAsYYYYMMDD()).child(application.getString(R.string.weather_camel_case)).setValue(true);
                firebaseManager.getConnectedServicesRef().child(application.getString(R.string.weather_camel_case)).child(formattedTime.getCurrentDateAsYYYYMMDD()).setValue(weatherDailySummary);
            }
            Log.i(TAG, "Image URL is " +
                    weatherSharedPreferences.getString("imageUrl", null));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void processWeatherHistory(String strjsonResponse) {
        Log.i(TAG, "processCurrentWeather called");
        String date;
        String mLocation = "";
        double mTempHighC;
        double mTempLowC;
        double mTempHighF;
        double mTempLowF;
        double mTempAvgC;
        double mTempAvgF;
        double mAveHumidity;
        double totalprecipmm;
        double totalprecipin;
        String condition = "";
        String mImageUrl = "";
        try {
            Log.i(TAG, "Parsing weather JSON");
            JSONObject jsonRootObject = new JSONObject(strjsonResponse);
            JSONObject location = jsonRootObject.optJSONObject("location");
            JSONObject forecast = jsonRootObject.optJSONObject("forecast");
            mLocation = location.optString("name");
            Log.i(TAG, "City is " + mLocation);
            JSONArray forecastDayArray = forecast.optJSONArray("forecastday");
            JSONObject forecastDay = forecastDayArray.optJSONObject(0);
            date = forecastDay.optString("date");
            JSONObject forecastToday = forecastDay.optJSONObject("day");
            JSONObject conditionObject = forecastToday.optJSONObject("condition");
            JSONObject astroToday = forecastDay.optJSONObject("astro");
            JSONArray todayByHour = forecastDay.optJSONArray("hour");
            JSONObject currentHour = todayByHour.optJSONObject(0);
            mTempHighC = forecastToday.optDouble("maxtemp_c");
            Log.i(TAG, "Max temp is " + mTempHighC);
            mTempLowC = forecastToday.optDouble("mintemp_c");
            Log.i(TAG, "Min temp is " + mTempLowC);
            mTempHighF = forecastToday.optDouble("maxtemp_f");
            Log.i(TAG, "Max temp is " + mTempHighF);
            mTempLowF = forecastToday.optDouble("mintemp_f");
            Log.i(TAG, "Min temp is " + mTempLowF);
            mTempAvgC = forecastToday.optDouble("avgtemp_c");
            Log.i(TAG, "Avg temp is " + mTempAvgC);
            mTempAvgF = forecastToday.optDouble("avgtemp_f");
            Log.i(TAG, "Avg temp is " + mTempAvgF);
            mAveHumidity = forecastToday.optDouble("avghumidity");
            Log.i(TAG, "Average humidity is " + mAveHumidity);
            totalprecipmm = forecastToday.optDouble("totalprecip_mm");
            totalprecipin = forecastToday.optDouble("totalprecip_in");
            Log.i(TAG, "Image URL is " + mImageUrl);
            condition = conditionObject.optString("text");
            mImageUrl = conditionObject.optString("icon");
            mImageUrl = "http:" + mImageUrl.trim();
            DecimalFormat decimalFormat = new DecimalFormat("#");
            WeatherDailySummary weatherDailySummary = new WeatherDailySummary();
            weatherDailySummary.setDate(date);
            weatherDailySummary.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
            weatherDailySummary.setAvgTempC(mTempAvgC);
            weatherDailySummary.setAvgTempF(mTempAvgF);
            weatherDailySummary.setMaxTempC(mTempHighC);
            weatherDailySummary.setMaxTempF(mTempHighF);
            weatherDailySummary.setMinTempC(mTempLowC);
            weatherDailySummary.setMinTempF(mTempLowF);
            weatherDailySummary.setCondition(condition);
            weatherDailySummary.setTotalPrecipmm(totalprecipmm);
            weatherDailySummary.setTotalPrecipin(totalprecipin);
            weatherDailySummary.setAvgHumidity(mAveHumidity);
            weatherDailySummary.setImageUrl(mImageUrl);
            insert(weatherDailySummary);
            InputHistory inputHistory = new InputHistory();
            inputHistory.setDate(date);
            inputHistory.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
            inputHistory.setDateInLong(formattedTime.convertStringYYYYMMDDToLong(date));
            inputHistory.setInputType(application.getString(R.string.weather_camel_case));
            inputHistoryRepository.insert(inputHistory);
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                firebaseManager.getDaysWithDataRef().child(date).child(application.getString(R.string.weather_camel_case)).setValue(true);
                firebaseManager.getConnectedServicesRef().child(application.getString(R.string.weather_camel_case)).child(date).setValue(weatherDailySummary);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public boolean insert(WeatherDailySummary weatherDailySummary) {
        AsyncTask.Status status = new insertAsyncTask(weatherDao).execute(weatherDailySummary).getStatus();
        if (status.equals(AsyncTask.Status.FINISHED)) {
            return true;
        } else {
            return false;
        }
    }

    private static class insertAsyncTask extends AsyncTask<WeatherDailySummary, Void, Void> {

        private WeatherDailySummaryDao weatherDailySummaryDao;

        insertAsyncTask(WeatherDailySummaryDao dao) {
            weatherDailySummaryDao = dao;
        }

        @Override
        protected synchronized Void doInBackground(final WeatherDailySummary... params) {
            weatherDailySummaryDao.insert(params[0]);
            return null;
        }
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
