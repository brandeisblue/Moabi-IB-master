package com.ivorybridge.moabi.ui.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitActivitySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitSleepSummary;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;
import com.ivorybridge.moabi.ui.adapter.IconSpinnerAdapter;
import com.ivorybridge.moabi.ui.recyclerviewitem.stats.MeansAndSumItem;
import com.ivorybridge.moabi.ui.util.FitnessTrackerBarChartMarkerView;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.BuiltInFitnessViewModel;
import com.ivorybridge.moabi.viewmodel.FitbitViewModel;
import com.ivorybridge.moabi.viewmodel.GoogleFitViewModel;
import com.ivorybridge.moabi.viewmodel.TimedActivityViewModel;
import com.ivorybridge.moabi.viewmodel.WeatherViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.android.segmented.SegmentedGroup;

public class StatsTrackerFragment extends Fragment {

    private static final String TAG = StatsTrackerFragment.class.getSimpleName();
    @BindView(R.id.fragment_stats_tracker_spinner)
    Spinner spinner;
    @BindView(R.id.fragment_stats_tracker_stats_recyclerview)
    RecyclerView statsRecyclerView;
    @BindView(R.id.fragment_stats_tracker_notabletrends_recyclerview)
    RecyclerView trendsRecyclerView;
    @BindView(R.id.fragment_stats_tracker_average_textview)
    TextView averageTextView;
    @BindView(R.id.fragment_stats_tracker_total_textview)
    TextView totalTextView;
    @BindView(R.id.fragment_stats_tracker_radiogroup)
    SegmentedGroup radioGroup;
    @BindView(R.id.fragment_stats_tracker_week_button)
    RadioButton weekButton;
    @BindView(R.id.fragment_stats_tracker_month_button)
    RadioButton monthButton;
    @BindView(R.id.fragment_stats_tracker_sixmonths_button)
    RadioButton sixMonthsButton;
    @BindView(R.id.fragment_stats_tracker_year_button)
    RadioButton yearButton;
    @BindView(R.id.fragment_stats_tracker_barchart)
    BarChart barChart;
    private FitbitViewModel fitbitViewModel;
    private GoogleFitViewModel googleFitViewModel;
    private BuiltInFitnessViewModel builtInFitnessViewModel;
    private WeatherViewModel weatherViewModel;
    private TimedActivityViewModel timedActivityViewModel;
    private FormattedTime formattedTime;
    private FastAdapter<IItem> statsRecyclerAdapter;
    private ItemAdapter<MeansAndSumItem> meansAndSumItemAdapter;
    private String inputType;
    private Typeface tf;
    private SharedPreferences fitbitSharedPreferences;
    private SharedPreferences googleFitSharedPreferences;
    private SharedPreferences builtInFitnessSharedPreferences;
    private SharedPreferences weatherSharedPreferences;
    private SharedPreferences timedActivitySharedPreferences;
    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        inputType = "Nothing";
        Bundle bundle = this.getArguments();
        if (bundle != null && bundle.getString("inputType") != null) {
            inputType = bundle.getString("inputType");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_stats_tracker, container, false);
        ButterKnife.bind(this, mView);
        context = getContext();
        formattedTime = new FormattedTime();
        fitbitSharedPreferences = context.getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_FITBIT_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        googleFitSharedPreferences = context.getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_GOOGLEFIT_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        builtInFitnessSharedPreferences = context.getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_BUILT_IN_FITNESS_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        weatherSharedPreferences = context.getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_WEATHER_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        timedActivitySharedPreferences = context.getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_TIMED_ACTIVITY_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        radioGroup.setTintColor(ContextCompat.getColor(context, R.color.colorPrimary), Color.WHITE);
        radioGroup.setUnCheckedTintColor(Color.WHITE, Color.BLACK);
        fitbitViewModel = ViewModelProviders.of(this).get(FitbitViewModel.class);
        googleFitViewModel = ViewModelProviders.of(this).get(GoogleFitViewModel.class);
        builtInFitnessViewModel = ViewModelProviders.of(this).get(BuiltInFitnessViewModel.class);
        weatherViewModel = ViewModelProviders.of(this).get(WeatherViewModel.class);
        timedActivityViewModel = ViewModelProviders.of(this).get(TimedActivityViewModel.class);
        statsRecyclerAdapter = new FastAdapter<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(context,
                RecyclerView.VERTICAL, false);
        statsRecyclerView.setLayoutManager(layoutManager);
        meansAndSumItemAdapter = new ItemAdapter<>();
        statsRecyclerAdapter = FastAdapter.with(meansAndSumItemAdapter);

        tf = ResourcesCompat.getFont(context, R.font.source_sans_pro);

        if (getContext() != null) {
            //RadioButton checkedRadioButton = (RadioButton)radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // This will get the radiobutton that has changed in its check state
                    RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
                    // This puts the value (true/false) into the variable
                    boolean isChecked = checkedRadioButton.isChecked();
                    // If the radiobutton that has changed in check state is now checked...
                    if (isChecked) {
                        if (checkedId == R.id.fragment_stats_tracker_week_button) {
                            Log.i(TAG, getContext().getString(R.string.week));
                            setSpinner(7);
                        } else if (checkedId == R.id.fragment_stats_tracker_month_button) {
                            Log.i(TAG, getContext().getString(R.string.month));
                            setSpinner(31);
                        } else if (checkedId == R.id.fragment_stats_tracker_year_button) {
                            Log.i(TAG, "year");
                            setSpinner(395);
                        } else {
                            Log.i(TAG, "6 months");
                            setSpinner(180);
                        }
                    }
                }
            });
        }

        return mView;

    }

    private void setSpinner(int numOfDays) {
        if (inputType.equals(getString(R.string.fitbit_camel_case))) {
            fitbitViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<FitbitDailySummary>>() {
                @Override
                public void onChanged(@Nullable List<FitbitDailySummary> fitbitDailySummaries) {
                    if (fitbitDailySummaries != null) {
                        //Log.i(TAG, fitbitDailySummaries.toString());
                        meansAndSumItemAdapter.clear();
                        Handler handler = new Handler();
                        Map<String, Object> activitySummaryMap = new LinkedHashMap<>();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (FitbitDailySummary dailySummary : fitbitDailySummaries) {
                                    FitbitActivitySummary activitySummary = dailySummary.getActivitySummary();
                                    FitbitSleepSummary sleepSummary = dailySummary.getSleepSummary();
                                    if (activitySummary.getSummary().getFairlyActiveMinutes() != null && activitySummary.getSummary().getVeryActiveMinutes() != null) {
                                        if (activitySummaryMap.get(getString(R.string.activity_active_minutes_camel_case)) != null) {
                                            Long old = (Long) activitySummaryMap.get(getString(R.string.activity_active_minutes_camel_case));
                                            if (old != null) {
                                                activitySummaryMap.put(getString(R.string.activity_active_minutes_camel_case), old + activitySummary.getSummary().getFairlyActiveMinutes() + activitySummary.getSummary().getVeryActiveMinutes());
                                            }
                                        } else {
                                            Long activeMins = activitySummary.getSummary().getFairlyActiveMinutes() + activitySummary.getSummary().getVeryActiveMinutes();
                                            activitySummaryMap.put(getString(R.string.activity_active_minutes_camel_case), activeMins);
                                        }
                                    } else {
                                        activitySummaryMap.put(getString(R.string.activity_active_minutes_camel_case), 0L);
                                    }
                                    if (activitySummary.getSummary().getCaloriesOut() != null) {
                                        if (activitySummaryMap.get(getString(R.string.activity_calories_camel_case)) != null) {
                                            Long old = (Long) activitySummaryMap.get(getString(R.string.activity_calories_camel_case));
                                            if (old != null) {
                                                activitySummaryMap.put(getString(R.string.activity_calories_camel_case), old + activitySummary.getSummary().getCaloriesOut());
                                            }
                                        } else {
                                            activitySummaryMap.put(getString(R.string.activity_calories_camel_case), activitySummary.getSummary().getCaloriesOut());
                                        }
                                    } else {
                                        activitySummaryMap.put(getString(R.string.activity_calories_camel_case), 0L);
                                    }
                                    if (activitySummary.getSummary().getDistances().get(0) != null && activitySummary.getSummary().getDistances().get(0).getDistance() != null) {
                                        if (activitySummaryMap.get(getString(R.string.activity_distance_camel_case)) != null) {
                                            Double old = (Double) activitySummaryMap.get(getString(R.string.activity_distance_camel_case));
                                            if (old != null) {
                                                activitySummaryMap.put(getString(R.string.activity_distance_camel_case), old + activitySummary.getSummary().getDistances().get(0).getDistance());
                                            }
                                        } else {
                                            Double distance = activitySummary.getSummary().getDistances().get(0).getDistance();
                                            activitySummaryMap.put(getString(R.string.activity_distance_camel_case), distance);
                                        }
                                    } else {
                                        activitySummaryMap.put(getString(R.string.activity_distance_camel_case), 0f);
                                    }

                                    if (activitySummary.getSummary().getFloors() != null) {
                                        if (activitySummaryMap.get(getString(R.string.activity_floors_camel_case)) != null) {
                                            Long old = (Long) activitySummaryMap.get(getString(R.string.activity_floors_camel_case));
                                            if (old != null) {
                                                activitySummaryMap.put(getString(R.string.activity_floors_camel_case), old + activitySummary.getSummary().getFloors());
                                            }
                                        } else {
                                            activitySummaryMap.put(getString(R.string.activity_floors_camel_case), activitySummary.getSummary().getFloors());
                                        }
                                    } else {
                                        activitySummaryMap.put(getString(R.string.activity_floors_camel_case), 0L);
                                    }
                                    if (activitySummary.getSummary().getSedentaryMinutes() != null) {
                                        if (activitySummaryMap.get(getString(R.string.activity_sedentary_minutes_camel_case)) != null) {
                                            Long old = (Long) activitySummaryMap.get(getString(R.string.activity_sedentary_minutes_camel_case));
                                            if (old != null) {
                                                activitySummaryMap.put(getString(R.string.activity_sedentary_minutes_camel_case), old + activitySummary.getSummary().getSedentaryMinutes());
                                            }
                                        } else {
                                            activitySummaryMap.put(getString(R.string.activity_sedentary_minutes_camel_case), activitySummary.getSummary().getSedentaryMinutes());
                                        }
                                    } else {
                                        activitySummaryMap.put(getString(R.string.activity_sedentary_minutes_camel_case), 0L);
                                    }

                                    if (sleepSummary.getSummary().getTotalMinutesAsleep() != null) {
                                        if (activitySummaryMap.get(getString(R.string.activity_sleep_camel_case)) != null) {
                                            Long old = (Long) activitySummaryMap.get(getString(R.string.activity_sleep_camel_case));
                                            if (old != null) {
                                                activitySummaryMap.put(getString(R.string.activity_sleep_camel_case), old + sleepSummary.getSummary().getTotalMinutesAsleep());
                                            }
                                        } else {
                                            activitySummaryMap.put(getString(R.string.activity_sleep_camel_case), sleepSummary.getSummary().getTotalMinutesAsleep());
                                        }
                                    } else {
                                        activitySummaryMap.put(getString(R.string.activity_sleep_camel_case), 0L);
                                    }
                                    if (activitySummary.getSummary().getSteps() != null) {
                                        if (activitySummaryMap.get(getString(R.string.activity_steps_camel_case)) != null) {
                                            Long old = (Long) activitySummaryMap.get(getString(R.string.activity_steps_camel_case));
                                            if (old != null) {
                                                activitySummaryMap.put(getString(R.string.activity_steps_camel_case), old + activitySummary.getSummary().getSteps());
                                            }
                                        } else {
                                            activitySummaryMap.put(getString(R.string.activity_steps_camel_case), activitySummary.getSummary().getSteps());
                                        }
                                    } else {
                                        activitySummaryMap.put(getString(R.string.activity_steps_camel_case), 0L);
                                    }

                                }

                                //Log.i(TAG, activitySummaryMap.toString());
                                Set<String> activitiesSet = new TreeSet<>();
                                for (Map.Entry<String, Object> activity : activitySummaryMap.entrySet()) {
                                    activitiesSet.add(activity.getKey());
                                }

                                activitiesSet.add("0 Summary");
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (activitiesSet.size() > 0) {
                                            String[] activitiesArray = activitiesSet.toArray(new String[0]);
                                            int[] imagesArray = new int[activitiesSet.size()];
                                            for (int i = 0; i < activitiesArray.length; i++) {
                                                if (activitiesArray[i].equals(getString(R.string.activity_steps_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_steps_title);
                                                    imagesArray[i] = R.drawable.ic_steps_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_calories_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_calories_title);
                                                    imagesArray[i] = R.drawable.ic_calories_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_floors_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_floors_title);
                                                    imagesArray[i] = R.drawable.ic_floors_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_sedentary_minutes_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_sedentary_minutes_title);
                                                    imagesArray[i] = R.drawable.ic_sedentary_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_active_minutes_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_active_minutes_title);
                                                    imagesArray[i] = R.drawable.ic_active_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_distance_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_distance_title);
                                                    imagesArray[i] = R.drawable.ic_distance_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_sleep_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_sleep_title);
                                                    imagesArray[i] = R.drawable.ic_sleep_black;
                                                } else if (activitiesArray[i].equals("0 Summary")) {
                                                    activitiesArray[i] = getString(R.string.stats_summary);
                                                    imagesArray[i] = R.drawable.ic_summary_black;
                                                }
                                            }
                                            if (activitiesArray != null && context != null) {
                                                int spinnerSelection = fitbitSharedPreferences.getInt("stats_spinner_selection", 0);
                                                IconSpinnerAdapter iconSpinnerAdapter = new IconSpinnerAdapter(context, activitiesArray, imagesArray);
                                                spinner.setAdapter(iconSpinnerAdapter);
                                                spinner.setSelection(spinnerSelection);
                                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        Log.i(TAG, activitiesArray[position] + " is selected");
                                                        SharedPreferences.Editor editor = fitbitSharedPreferences.edit();
                                                        editor.putInt("stats_spinner_selection", position);
                                                        editor.apply();
                                                        if (activitiesArray[position].equals(getString(R.string.stats_summary))) {
                                                            setSummaryData(activitySummaryMap, fitbitDailySummaries.size());
                                                            totalTextView.setVisibility(View.VISIBLE);
                                                            averageTextView.setVisibility(View.VISIBLE);
                                                        } else {
                                                            if (numOfDays == 7 || numOfDays == 31) {
                                                                setDailyBarChart(activitiesArray[position], numOfDays);
                                                            } else if (numOfDays == 180) {
                                                                setWeeklyBarChart(activitiesArray[position], numOfDays);
                                                            } else {
                                                                setMonthlyBarChart(activitiesArray[position], numOfDays);
                                                            }
                                                            totalTextView.setVisibility(View.GONE);
                                                            averageTextView.setVisibility(View.GONE);
                                                        }
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {

                                                    }
                                                });
                                            }
                                        }
                                        statsRecyclerView.setAdapter(statsRecyclerAdapter);
                                    }
                                });
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.googlefit_camel_case))) {
            googleFitViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<GoogleFitSummary>>() {
                @Override
                public void onChanged(@Nullable List<GoogleFitSummary> googleFitSummaries) {
                    if (googleFitSummaries != null) {
                        meansAndSumItemAdapter.clear();
                        Map<String, Object> activitySummaryMap = new LinkedHashMap<>();
                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (GoogleFitSummary dailySummary : googleFitSummaries) {
                                    List<GoogleFitSummary.Summary> summaries = dailySummary.getSummaries();
                                    for (GoogleFitSummary.Summary summary : summaries) {

                                        String name = summary.getName();
                                        /*//name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                        if (name.equals("Activeminutes")) {
                                            name = getString(R.string.activity_active_minutes_camel_case);
                                        }
                                        if (name.equals("Sedentaryminutes")) {
                                            name = getString(R.string.activity_sedentary_minutes_camel_case);
                                        }*/
                                        if (activitySummaryMap.get(name) != null) {
                                            Double old = (Double) activitySummaryMap.get(name);
                                            activitySummaryMap.put(name, old + summary.getValue());
                                        } else {
                                            activitySummaryMap.put(name, summary.getValue());
                                        }
                                    }
                                }
                                Log.i(TAG, activitySummaryMap.toString());
                                Set<String> activitiesSet = new TreeSet<>();
                                for (Map.Entry<String, Object> activity : activitySummaryMap.entrySet()) {
                                    activitiesSet.add(activity.getKey());
                                }
                                activitiesSet.add("0 Summary");
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (activitiesSet.size() > 0) {
                                            String[] activitiesArray = activitiesSet.toArray(new String[0]);
                                            int[] imagesArray = new int[activitiesSet.size()];
                                            for (int i = 0; i < activitiesArray.length; i++) {
                                                if (activitiesArray[i].equals(getString(R.string.activity_steps_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_steps_title);
                                                    imagesArray[i] = R.drawable.ic_steps_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_calories_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_calories_title);
                                                    imagesArray[i] = R.drawable.ic_calories_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_floors_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_floors_title);
                                                    imagesArray[i] = R.drawable.ic_floors_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_sedentary_minutes_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_sedentary_minutes_title);
                                                    imagesArray[i] = R.drawable.ic_sedentary_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_active_minutes_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_active_minutes_title);
                                                    imagesArray[i] = R.drawable.ic_active_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_distance_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_distance_title);
                                                    imagesArray[i] = R.drawable.ic_distance_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_sleep_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_sleep_title);
                                                    imagesArray[i] = R.drawable.ic_sleep_black;
                                                } else if (activitiesArray[i].equals("0 Summary")) {
                                                    activitiesArray[i] = getString(R.string.stats_summary);
                                                    imagesArray[i] = R.drawable.ic_summary_black;
                                                }
                                            }
                                            Log.i(TAG, Arrays.toString(activitiesArray));

                                            if (activitiesArray != null && context != null) {
                                                int spinnerSelection = googleFitSharedPreferences.getInt("stats_spinner_selection", 0);
                                                IconSpinnerAdapter iconSpinnerAdapter = new IconSpinnerAdapter(context, activitiesArray, imagesArray);
                                                spinner.setAdapter(iconSpinnerAdapter);
                                                spinner.setSelection(spinnerSelection);
                                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        Log.i(TAG, activitiesArray[position] + " is selected");
                                                        SharedPreferences.Editor editor = googleFitSharedPreferences.edit();
                                                        editor.putInt("stats_spinner_selection", position);
                                                        editor.apply();
                                                        if (activitiesArray[position].equals(getString(R.string.stats_summary))) {
                                                            setSummaryData(activitySummaryMap, googleFitSummaries.size());
                                                            totalTextView.setVisibility(View.VISIBLE);
                                                            averageTextView.setVisibility(View.VISIBLE);
                                                        } else {
                                                            if (numOfDays == 7 || numOfDays == 31) {
                                                                setDailyBarChart(activitiesArray[position], numOfDays);
                                                            } else if (numOfDays == 180) {
                                                                setWeeklyBarChart(activitiesArray[position], numOfDays);
                                                            } else {
                                                                setMonthlyBarChart(activitiesArray[position], numOfDays);
                                                            }
                                                            totalTextView.setVisibility(View.GONE);
                                                            averageTextView.setVisibility(View.GONE);
                                                        }
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {

                                                    }
                                                });
                                            }
                                        }
                                        statsRecyclerView.setAdapter(statsRecyclerAdapter);
                                    }
                                });
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.moabi_tracker_camel_case))) {
            builtInFitnessViewModel.getAllSummaries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<BuiltInActivitySummary>>() {
                @Override
                public void onChanged(@Nullable List<BuiltInActivitySummary> activitySummaries) {
                    if (activitySummaries != null) {
                        //Log.i(TAG, fitbitDailySummaries.toString());
                        meansAndSumItemAdapter.clear();
                        Handler handler = new Handler();
                        Map<String, Object> activitySummaryMap = new LinkedHashMap<>();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (BuiltInActivitySummary dailySummary : activitySummaries) {
                                    if (dailySummary.getActiveMinutes() != null) {
                                        if (activitySummaryMap.get(getString(R.string.activity_active_minutes_camel_case)) != null) {
                                            Long old = (Long) activitySummaryMap.get(getString(R.string.activity_active_minutes_camel_case));
                                            if (old != null) {
                                                activitySummaryMap.put(getString(R.string.activity_active_minutes_camel_case), old + TimeUnit.MILLISECONDS.toMinutes(dailySummary.getActiveMinutes()));
                                            }
                                        } else {
                                            Long activeMins = TimeUnit.MILLISECONDS.toMinutes(dailySummary.getActiveMinutes());
                                            activitySummaryMap.put(getString(R.string.activity_active_minutes_camel_case), activeMins);
                                        }
                                    } else {
                                        activitySummaryMap.put(getString(R.string.activity_active_minutes_camel_case), 0L);
                                    }
                                    if (dailySummary.getCalories() != null) {
                                        if (activitySummaryMap.get(getString(R.string.activity_calories_camel_case)) != null) {
                                            Long old = (Long) activitySummaryMap.get(getString(R.string.activity_calories_camel_case));
                                            if (old != null) {
                                                activitySummaryMap.put(getString(R.string.activity_calories_camel_case), old + dailySummary.getCalories().longValue());
                                            }
                                        } else {
                                            activitySummaryMap.put(getString(R.string.activity_calories_camel_case), dailySummary.getCalories().longValue());
                                        }
                                    } else {
                                        activitySummaryMap.put(getString(R.string.activity_calories_camel_case), 0L);
                                    }
                                    if (dailySummary.getDistance() != null) {
                                        if (activitySummaryMap.get(getString(R.string.activity_distance_camel_case)) != null) {
                                            Double old = (Double) activitySummaryMap.get(getString(R.string.activity_distance_camel_case));
                                            if (old != null) {
                                                activitySummaryMap.put(getString(R.string.activity_distance_camel_case), old + dailySummary.getDistance() / 1000);
                                            }
                                        } else {
                                            Double distance = dailySummary.getDistance() / 1000;
                                            activitySummaryMap.put(getString(R.string.activity_distance_camel_case), distance);
                                        }
                                    } else {
                                        activitySummaryMap.put(getString(R.string.activity_distance_camel_case), 0f);
                                    }
                                    if (dailySummary.getSedentaryMinutes() != null) {
                                        if (activitySummaryMap.get(getString(R.string.activity_sedentary_minutes_camel_case)) != null) {
                                            Long old = (Long) activitySummaryMap.get(getString(R.string.activity_sedentary_minutes_camel_case));
                                            if (old != null) {
                                                activitySummaryMap.put(getString(R.string.activity_sedentary_minutes_camel_case), old + TimeUnit.MILLISECONDS.toMinutes(dailySummary.getSedentaryMinutes()));
                                            }
                                        } else {
                                            activitySummaryMap.put(getString(R.string.activity_sedentary_minutes_camel_case), TimeUnit.MILLISECONDS.toMinutes(dailySummary.getSedentaryMinutes()));
                                        }
                                    } else {
                                        activitySummaryMap.put(getString(R.string.activity_sedentary_minutes_camel_case), 0L);
                                    }
                                    if (dailySummary.getSteps() != null) {
                                        if (activitySummaryMap.get(getString(R.string.activity_steps_camel_case)) != null) {
                                            Long old = (Long) activitySummaryMap.get(getString(R.string.activity_steps_camel_case));
                                            if (old != null) {
                                                activitySummaryMap.put(getString(R.string.activity_steps_camel_case), old + dailySummary.getSteps());
                                            }
                                        } else {
                                            activitySummaryMap.put(getString(R.string.activity_steps_camel_case), dailySummary.getSteps());
                                        }
                                    } else {
                                        activitySummaryMap.put(getString(R.string.activity_steps_camel_case), 0L);
                                    }

                                }

                                //Log.i(TAG, activitySummaryMap.toString());
                                Set<String> activitiesSet = new TreeSet<>();
                                for (Map.Entry<String, Object> activity : activitySummaryMap.entrySet()) {
                                    activitiesSet.add(activity.getKey());
                                }

                                activitiesSet.add("0 Summary");
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (activitiesSet.size() > 0) {
                                            String[] activitiesArray = activitiesSet.toArray(new String[0]);
                                            int[] imagesArray = new int[activitiesSet.size()];
                                            for (int i = 0; i < activitiesArray.length; i++) {
                                                if (activitiesArray[i].equals(getString(R.string.activity_steps_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_steps_title);
                                                    imagesArray[i] = R.drawable.ic_steps_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_calories_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_calories_title);
                                                    imagesArray[i] = R.drawable.ic_calories_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_sedentary_minutes_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_sedentary_minutes_title);
                                                    imagesArray[i] = R.drawable.ic_sedentary_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_active_minutes_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_active_minutes_title);
                                                    imagesArray[i] = R.drawable.ic_active_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.activity_distance_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.activity_distance_title);
                                                    imagesArray[i] = R.drawable.ic_distance_black;
                                                } else if (activitiesArray[i].equals("0 Summary")) {
                                                    activitiesArray[i] = getString(R.string.stats_summary);
                                                    imagesArray[i] = R.drawable.ic_summary_black;
                                                }
                                            }
                                            if (activitiesArray != null && context != null) {
                                                int spinnerSelection = builtInFitnessSharedPreferences.getInt("stats_spinner_selection", 0);
                                                IconSpinnerAdapter iconSpinnerAdapter = new IconSpinnerAdapter(context, activitiesArray, imagesArray);
                                                spinner.setAdapter(iconSpinnerAdapter);
                                                spinner.setSelection(spinnerSelection);
                                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        Log.i(TAG, activitiesArray[position] + " is selected");
                                                        SharedPreferences.Editor editor = builtInFitnessSharedPreferences.edit();
                                                        editor.putInt("stats_spinner_selection", position);
                                                        editor.apply();
                                                        if (activitiesArray[position].equals(getString(R.string.stats_summary))) {
                                                            setSummaryData(activitySummaryMap, activitySummaries.size());
                                                            totalTextView.setVisibility(View.VISIBLE);
                                                            averageTextView.setVisibility(View.VISIBLE);
                                                        } else {
                                                            if (numOfDays == 7 || numOfDays == 31) {
                                                                setDailyBarChart(activitiesArray[position], numOfDays);
                                                            } else if (numOfDays == 180) {
                                                                setWeeklyBarChart(activitiesArray[position], numOfDays);
                                                            } else {
                                                                setMonthlyBarChart(activitiesArray[position], numOfDays);
                                                            }
                                                            totalTextView.setVisibility(View.GONE);
                                                            averageTextView.setVisibility(View.GONE);
                                                        }
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {

                                                    }
                                                });
                                            }
                                        }
                                        statsRecyclerView.setAdapter(statsRecyclerAdapter);
                                    }
                                });
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.weather_camel_case))) {
            weatherViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<WeatherDailySummary>>() {
                @Override
                public void onChanged(@Nullable List<WeatherDailySummary> summaries) {
                    if (summaries != null) {
                        meansAndSumItemAdapter.clear();
                        Handler handler = new Handler();
                        Map<String, Object> activitySummaryMap = new LinkedHashMap<>();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (WeatherDailySummary dailySummary : summaries) {
                                    if (activitySummaryMap.get(getString(R.string.precipitation_camel_case)) != null) {
                                        double old = (Double) activitySummaryMap.get(getString(R.string.precipitation_camel_case));
                                        activitySummaryMap.put(getString(R.string.precipitation_camel_case), old + dailySummary.getTotalPrecipmm());
                                    } else {
                                        double value = (Double) dailySummary.getTotalPrecipmm();
                                        activitySummaryMap.put(getString(R.string.precipitation_camel_case), value);
                                    }
                                    if (activitySummaryMap.get(getString(R.string.temperature_camel_case)) != null) {
                                        double old = (Double) activitySummaryMap.get(getString(R.string.temperature_camel_case));
                                        activitySummaryMap.put(getString(R.string.temperature_camel_case), old + dailySummary.getAvgTempC());
                                    } else {
                                        double value = (Double) dailySummary.getAvgTempC();
                                        activitySummaryMap.put(getString(R.string.temperature_camel_case), value);
                                    }
                                    if (activitySummaryMap.get(getString(R.string.humidity_camel_case)) != null) {
                                        double old = (Double) activitySummaryMap.get(getString(R.string.humidity_camel_case));
                                        activitySummaryMap.put(getString(R.string.humidity_camel_case), old + dailySummary.getAvgHumidity());
                                    } else {
                                        double value = (Double) dailySummary.getAvgHumidity();
                                        activitySummaryMap.put(getString(R.string.humidity_camel_case), value);
                                    }
                                }

                                //Log.i(TAG, activitySummaryMap.toString());
                                Set<String> activitiesSet = new TreeSet<>();
                                for (Map.Entry<String, Object> activity : activitySummaryMap.entrySet()) {
                                    activitiesSet.add(activity.getKey());
                                }

                                activitiesSet.add("0 Summary");
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (activitiesSet.size() > 0) {
                                            String[] activitiesArray = activitiesSet.toArray(new String[0]);
                                            int[] imagesArray = new int[activitiesSet.size()];
                                            for (int i = 0; i < activitiesArray.length; i++) {
                                                if (activitiesArray[i].equals(getString(R.string.precipitation_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.precipitation_title);
                                                    imagesArray[i] = R.drawable.ic_precipitation_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.temperature_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.temperature_title);
                                                    imagesArray[i] = R.drawable.ic_temperature_black;
                                                } else if (activitiesArray[i].equals(getString(R.string.humidity_camel_case))) {
                                                    activitiesArray[i] = getString(R.string.humidity_title);
                                                    imagesArray[i] = R.drawable.ic_humidity;
                                                } else if (activitiesArray[i].equals("0 Summary")) {
                                                    activitiesArray[i] = getString(R.string.stats_summary);
                                                    imagesArray[i] = R.drawable.ic_summary_black;
                                                }
                                            }
                                            if (activitiesArray != null && context != null) {
                                                int spinnerSelection = weatherSharedPreferences.getInt("stats_spinner_selection", 0);
                                                IconSpinnerAdapter iconSpinnerAdapter = new IconSpinnerAdapter(context, activitiesArray, imagesArray);
                                                spinner.setAdapter(iconSpinnerAdapter);
                                                spinner.setSelection(spinnerSelection);
                                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        Log.i(TAG, activitiesArray[position] + " is selected");
                                                        SharedPreferences.Editor editor = weatherSharedPreferences.edit();
                                                        editor.putInt("stats_spinner_selection", position);
                                                        editor.apply();
                                                        if (activitiesArray[position].equals(getString(R.string.stats_summary))) {
                                                            setSummaryData(activitySummaryMap, summaries.size());
                                                            totalTextView.setVisibility(View.VISIBLE);
                                                            averageTextView.setVisibility(View.VISIBLE);
                                                        } else {
                                                            if (numOfDays == 7 || numOfDays == 31) {
                                                                setDailyBarChart(activitiesArray[position], numOfDays);
                                                            } else if (numOfDays == 180) {
                                                                setWeeklyBarChart(activitiesArray[position], numOfDays);
                                                            } else {
                                                                setMonthlyBarChart(activitiesArray[position], numOfDays);
                                                            }
                                                            totalTextView.setVisibility(View.GONE);
                                                            averageTextView.setVisibility(View.GONE);
                                                        }
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {

                                                    }
                                                });
                                            }
                                        }
                                        statsRecyclerView.setAdapter(statsRecyclerAdapter);
                                    }
                                });
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.timer_camel_case))) {
            timedActivityViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<TimedActivitySummary>>() {
                @Override
                public void onChanged(@Nullable List<TimedActivitySummary> summaries) {
                    if (summaries != null) {
                        meansAndSumItemAdapter.clear();
                        Map<String, Object> activitySummaryMap = new LinkedHashMap<>();
                        Set<String> dates = new LinkedHashSet<>();
                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (TimedActivitySummary summary : summaries) {
                                    String name = summary.getInputName().trim();
                                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                    if (activitySummaryMap.get(name) != null) {
                                        Long duration = (Long) activitySummaryMap.get(name);
                                        activitySummaryMap.put(name, duration + summary.getDuration());
                                    } else {
                                        activitySummaryMap.put(name, summary.getDuration());
                                    }
                                    dates.add(summary.getDate());
                                }
                                Log.i(TAG, activitySummaryMap.toString());
                                Set<String> activitiesSet = new TreeSet<>();
                                for (Map.Entry<String, Object> activity : activitySummaryMap.entrySet()) {
                                    activitiesSet.add(activity.getKey());
                                }
                                activitiesSet.add("0 Summary");
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (activitiesSet.size() > 0) {
                                            String[] activitiesArray = activitiesSet.toArray(new String[0]);
                                            int[] imagesArray = new int[activitiesSet.size()];
                                            for (int i = 0; i < activitiesArray.length; i++) {
                                                if (activitiesArray[i].equals("0 Summary")) {
                                                    activitiesArray[i] = getString(R.string.stats_summary);
                                                    imagesArray[i] = R.drawable.ic_summary_black;
                                                }
                                            }
                                            Log.i(TAG, Arrays.toString(activitiesArray));

                                            if (activitiesArray != null && context != null) {
                                                int spinnerSelection = timedActivitySharedPreferences.getInt("stats_spinner_selection", 0);
                                                IconSpinnerAdapter iconSpinnerAdapter = new IconSpinnerAdapter(context, activitiesArray, imagesArray);
                                                spinner.setAdapter(iconSpinnerAdapter);
                                                spinner.setSelection(spinnerSelection);
                                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        Log.i(TAG, activitiesArray[position] + " is selected");
                                                        SharedPreferences.Editor editor = timedActivitySharedPreferences.edit();
                                                        editor.putInt("stats_spinner_selection", position);
                                                        editor.apply();
                                                        if (activitiesArray[position].equals(getString(R.string.stats_summary))) {
                                                            setSummaryData(activitySummaryMap, dates.size());
                                                            totalTextView.setVisibility(View.VISIBLE);
                                                            averageTextView.setVisibility(View.VISIBLE);
                                                        } else {
                                                            if (numOfDays == 7 || numOfDays == 31) {
                                                                setDailyBarChart(activitiesArray[position], numOfDays);
                                                            } else if (numOfDays == 180) {
                                                                setWeeklyBarChart(activitiesArray[position], numOfDays);
                                                            } else {
                                                                setMonthlyBarChart(activitiesArray[position], numOfDays);
                                                            }
                                                            totalTextView.setVisibility(View.GONE);
                                                            averageTextView.setVisibility(View.GONE);
                                                        }
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {

                                                    }
                                                });
                                            }
                                        }
                                        statsRecyclerView.setAdapter(statsRecyclerAdapter);
                                    }
                                });
                            }
                        }).start();
                    }
                }
            });
        }
    }

    private void setDailyBarChart(String activity, int numOfDays) {
        if (inputType.equals(getString(R.string.fitbit_camel_case))) {
            fitbitViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<FitbitDailySummary>>() {
                @Override
                public void onChanged(@Nullable List<FitbitDailySummary> fitbitDailySummaries) {
                    if (fitbitDailySummaries != null) {
                        styleBarChart();
                        List<FitbitDailySummary> reverseOrderedList = fitbitDailySummaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        if (reverseOrderedList.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfDays; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i), 0L);
                                }

                                //Log.i(TAG, activity + " " + fitbitDailySummaries.toString());

                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                // iterate through the entry dates
                                // iterate through the summary list until we find a match in date
                                // if there is a match, do the thing.
                                // if not, put 0 as value and move on.

                                for (String date : entryDatesList) {
                                    for (FitbitDailySummary fitbitDailySummary : reverseOrderedList) {
                                        // find matching record
                                        if (fitbitDailySummary.getDate().equals(date)) {
                                            if (activity.equals(getString(R.string.activity_steps_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getSteps());
                                                barEntries.add(new BarEntry(i, activitySummary.getSummary().getSteps()));
                                                total += activitySummary.getSummary().getSteps();
                                                validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_active_minutes_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                Long activeMinutes = activitySummary.getSummary().getFairlyActiveMinutes() + activitySummary.getSummary().getVeryActiveMinutes();
                                                entries.put(fitbitDailySummary.getDate(), activeMinutes);
                                                barEntries.add(new BarEntry(i, activeMinutes));
                                                total += activeMinutes;
                                                validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_sedentary_minutes_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getSedentaryMinutes());
                                                barEntries.add(new BarEntry(i, activitySummary.getSummary().getSedentaryMinutes()));
                                                total += activitySummary.getSummary().getSedentaryMinutes();
                                                validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_calories_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getCaloriesOut());
                                                barEntries.add(new BarEntry(i, activitySummary.getSummary().getCaloriesOut()));
                                                total += activitySummary.getSummary().getCaloriesOut();
                                                validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_floors_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getFloors());
                                                barEntries.add(new BarEntry(i, activitySummary.getSummary().getFloors()));
                                                total += activitySummary.getSummary().getFloors();
                                                validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_distance_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getDistances().get(0));
                                                barEntries.add(new BarEntry(i, activitySummary.getSummary().getDistances().get(0).getDistance().floatValue()));
                                                total += activitySummary.getSummary().getDistances().get(0).getDistance().floatValue();
                                                validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_sleep_title))) {
                                                FitbitSleepSummary sleepSummary = fitbitDailySummary.getSleepSummary();
                                                entries.put(fitbitDailySummary.getDate(), sleepSummary.getSummary().getTotalMinutesAsleep());
                                                barEntries.add(new BarEntry(i, sleepSummary.getSummary().getTotalMinutesAsleep()));
                                                total += sleepSummary.getSummary().getTotalMinutesAsleep();
                                                validEntryCounter++;
                                            }
                                        }
                                    }
                                    i++;
                                    if (barEntries.size() < i) {
                                        barEntries.add(new BarEntry(i - 1, 0));
                                    }
                                }

                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                                Log.i(TAG, entries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.googlefit_camel_case))) {
            googleFitViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<GoogleFitSummary>>() {
                @Override
                public void onChanged(@Nullable List<GoogleFitSummary> googleFitSummaries) {
                    if (googleFitSummaries != null) {
                        styleBarChart();
                        List<GoogleFitSummary> reverseOrderedList = googleFitSummaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        if (reverseOrderedList.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfDays; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i), 0L);
                                }

                                //Log.i(TAG, activity + " " + googleFitSummaries.toString());

                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                for (String date : entryDatesList) {
                                    for (GoogleFitSummary googleFitSummary : reverseOrderedList) {
                                        // find matching record
                                        if (googleFitSummary.getDate().equals(date)) {
                                            List<GoogleFitSummary.Summary> summaries = googleFitSummary.getSummaries();
                                            for (GoogleFitSummary.Summary summary : summaries) {
                                                String name = summary.getName().toLowerCase();
                                                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                                if (name.equals("Activeminutes")) {
                                                    name = getString(R.string.activity_active_minutes_title);
                                                } else if (name.equals("Sedentaryminutes")) {
                                                    name = getString(R.string.activity_sedentary_minutes_title);
                                                }
                                                if (name.equals(activity)) {
                                                    if (activity.equals(getString(R.string.activity_active_minutes_title)) || activity.equals(getString(R.string.activity_sedentary_minutes_title))) {
                                                        Double minutes = summary.getValue();
                                                        Long minutesLong = minutes.longValue();
                                                        minutesLong = TimeUnit.MILLISECONDS.toMinutes(minutesLong);
                                                        total += minutesLong;
                                                        barEntries.add(new BarEntry(i, minutesLong));
                                                        validEntryCounter++;
                                                    } else if (activity.equals(getString(R.string.activity_distance_title))) {
                                                        barEntries.add(new BarEntry(i, summary.getValue().floatValue() / 1000));
                                                        total += summary.getValue().floatValue() / 1000;
                                                        validEntryCounter++;
                                                    } else {
                                                        barEntries.add(new BarEntry(i, summary.getValue().floatValue()));
                                                        total += summary.getValue().intValue();
                                                        validEntryCounter++;
                                                    }

                                                }
                                            }
                                        }
                                    }
                                    i++;
                                    if (barEntries.size() < i) {
                                        barEntries.add(new BarEntry(i - 1, 0));
                                    }
                                }

                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                                Log.i(TAG, entries.toString());

                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.moabi_tracker_camel_case))) {
            builtInFitnessViewModel.getAllSummaries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<BuiltInActivitySummary>>() {
                @Override
                public void onChanged(@Nullable List<BuiltInActivitySummary> activitySummaries) {
                    if (activitySummaries != null) {
                        styleBarChart();
                        List<BuiltInActivitySummary> reverseOrderedList = activitySummaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        if (reverseOrderedList.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfDays; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i), 0L);
                                }

                                //Log.i(TAG, activity + " " + fitbitDailySummaries.toString());

                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                // iterate through the entry dates
                                // iterate through the summary list until we find a match in date
                                // if there is a match, do the thing.
                                // if not, put 0 as value and move on.

                                for (String date : entryDatesList) {
                                    for (BuiltInActivitySummary dailySummary : reverseOrderedList) {
                                        // find matching record
                                        if (dailySummary.getDate().equals(date)) {
                                            if (activity.equals(getString(R.string.activity_steps_title))) {
                                                entries.put(dailySummary.getDate(), dailySummary.getSteps());
                                                barEntries.add(new BarEntry(i, dailySummary.getSteps()));
                                                total += dailySummary.getSteps();
                                                validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_active_minutes_title))) {
                                                Long activeMinutes = TimeUnit.MILLISECONDS.toMinutes(dailySummary.getActiveMinutes());
                                                entries.put(dailySummary.getDate(), activeMinutes);
                                                barEntries.add(new BarEntry(i, activeMinutes));
                                                total += activeMinutes;
                                                validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_sedentary_minutes_title))) {
                                                Long sedentaryMinutes = TimeUnit.MILLISECONDS.toMinutes(dailySummary.getSedentaryMinutes());
                                                entries.put(dailySummary.getDate(), sedentaryMinutes);
                                                barEntries.add(new BarEntry(i, sedentaryMinutes));
                                                total += sedentaryMinutes;
                                                validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_calories_title))) {
                                                entries.put(dailySummary.getDate(), dailySummary.getCalories().longValue());
                                                barEntries.add(new BarEntry(i, dailySummary.getCalories().longValue()));
                                                total += dailySummary.getCalories().longValue();
                                                validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_distance_title))) {
                                                entries.put(dailySummary.getDate(), dailySummary.getDistance().longValue() / 1000);
                                                barEntries.add(new BarEntry(i, dailySummary.getDistance().floatValue() / 1000));
                                                total += dailySummary.getDistance().floatValue() / 1000;
                                                validEntryCounter++;
                                            }
                                        }
                                    }
                                    i++;
                                    if (barEntries.size() < i) {
                                        barEntries.add(new BarEntry(i - 1, 0));
                                    }
                                }

                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                                Log.i(TAG, entries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.weather_camel_case))) {
            weatherViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<WeatherDailySummary>>() {
                @Override
                public void onChanged(@Nullable List<WeatherDailySummary> activitySummaries) {
                    if (activitySummaries != null) {
                        styleBarChart();
                        List<WeatherDailySummary> reverseOrderedList = activitySummaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        if (reverseOrderedList.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfDays; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i), 0L);
                                }

                                //Log.i(TAG, activity + " " + fitbitDailySummaries.toString());

                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                // iterate through the entry dates
                                // iterate through the summary list until we find a match in date
                                // if there is a match, do the thing.
                                // if not, put 0 as value and move on.

                                for (String date : entryDatesList) {
                                    for (WeatherDailySummary dailySummary : reverseOrderedList) {
                                        // find matching record
                                        if (dailySummary.getDate().equals(date)) {
                                            if (activity.equals(getString(R.string.precipitation_title))) {
                                                entries.put(dailySummary.getDate(), (long) dailySummary.getTotalPrecipmm());
                                                barEntries.add(new BarEntry(i, (float) dailySummary.getTotalPrecipmm()));
                                                total += dailySummary.getTotalPrecipmm();
                                                validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.temperature_title))) {
                                                entries.put(dailySummary.getDate(), (long) dailySummary.getAvgTempC());
                                                barEntries.add(new BarEntry(i, (float) dailySummary.getAvgTempC()));
                                                total += dailySummary.getAvgTempC();
                                                validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.humidity_title))) {
                                                entries.put(dailySummary.getDate(), (long) dailySummary.getAvgHumidity());
                                                barEntries.add(new BarEntry(i, (float) dailySummary.getAvgHumidity()));
                                                total += dailySummary.getAvgHumidity();
                                                validEntryCounter++;
                                            }
                                        }
                                    }
                                    i++;
                                    if (barEntries.size() < i) {
                                        barEntries.add(new BarEntry(i - 1, 0));
                                    }
                                }

                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                                Log.i(TAG, entries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.timer_camel_case))) {
            timedActivityViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<TimedActivitySummary>>() {
                @Override
                public void onChanged(@Nullable List<TimedActivitySummary> summaries) {
                    if (summaries != null) {
                        styleBarChart();
                        List<TimedActivitySummary> reverseOrderedList = summaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        if (reverseOrderedList.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }
                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfDays; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i));
                                    //entries.put(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i), 0L);
                                }

                                for (TimedActivitySummary timedActivitySummary : reverseOrderedList) {
                                    String name = timedActivitySummary.getInputName().trim();
                                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                    Log.i(TAG, activity + ", " + name + ": " + timedActivitySummary.getDuration());
                                    if (activity.equals(name)) {
                                        Log.i(TAG, "Activity found");
                                        String date = timedActivitySummary.getDate();
                                        if (entries.get(date) != null) {
                                            Long old = entries.get(date);
                                            entries.put(date, old + timedActivitySummary.getDuration());
                                        } else {
                                            entries.put(date, timedActivitySummary.getDuration());
                                        }
                                    }
                                }
                                Log.i(TAG, activity + ": " + entries.toString());
                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                for (String date : entryDatesList) {
                                    if (entries.get(date) != null) {
                                        Long time = TimeUnit.MILLISECONDS.toMinutes(entries.get(date));
                                        Log.i(TAG, date + ": " + time);
                                        total += time;
                                        barEntries.add(new BarEntry(i, time));
                                        validEntryCounter++;
                                    }
                                    i++;
                                    if (barEntries.size() < i) {
                                        barEntries.add(new BarEntry(i - 1, 0));
                                    }
                                }


                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                }
            });
        }
    }

    private void setWeeklyBarChart(String activity, int numOfDays) {
        if (inputType.equals(getString(R.string.fitbit_camel_case))) {
            fitbitViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<FitbitDailySummary>>() {
                @Override
                public void onChanged(@Nullable List<FitbitDailySummary> fitbitDailySummaries) {
                    if (fitbitDailySummaries != null) {
                        styleBarChart();
                        if (fitbitDailySummaries.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        List<FitbitDailySummary> reverseOrderedList = fitbitDailySummaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfWeeks = numOfDays / 7;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfWeeks; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i));
                                }

                                //Log.i(TAG, activity + " " + fitbitDailySummaries.toString());

                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                // iterate through the entry dates
                                // iterate through the summary list until we find a match in date
                                // if there is a match, do the thing.
                                // if not, put 0 as value and move on.

                                for (String YYYW : entryDatesList) {
                                    float weeklyTotal = 0;
                                    for (FitbitDailySummary fitbitDailySummary : reverseOrderedList) {
                                        // find matching record
                                        if (YYYW.equals(formattedTime.convertLongToYYYYW(fitbitDailySummary.getDateInLong()))) {
                                            if (activity.equals(getString(R.string.activity_steps_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getSteps());
                                                weeklyTotal += activitySummary.getSummary().getSteps();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_active_minutes_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                Long activeMinutes = activitySummary.getSummary().getFairlyActiveMinutes() + activitySummary.getSummary().getVeryActiveMinutes();
                                                //entries.put(fitbitDailySummary.getDate(), activeMinutes);
                                                //barEntries.add(new BarEntry(i, activeMinutes));
                                                weeklyTotal += activeMinutes;
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_sedentary_minutes_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getSedentaryMinutes());
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getSedentaryMinutes()));
                                                weeklyTotal += activitySummary.getSummary().getSedentaryMinutes();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_calories_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getCaloriesOut());
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getCaloriesOut()));
                                                weeklyTotal += activitySummary.getSummary().getCaloriesOut();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_floors_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getFloors());
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getFloors()));
                                                weeklyTotal += activitySummary.getSummary().getFloors();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_distance_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getDistances().get(0));
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getDistances().get(0).getDistance().floatValue()));
                                                weeklyTotal += activitySummary.getSummary().getDistances().get(0).getDistance().floatValue();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_sleep_title))) {
                                                FitbitSleepSummary sleepSummary = fitbitDailySummary.getSleepSummary();
                                                //entries.put(fitbitDailySummary.getDate(), sleepSummary.getSummary().getTotalMinutesAsleep());
                                                //barEntries.add(new BarEntry(i, sleepSummary.getSummary().getTotalMinutesAsleep()));
                                                weeklyTotal += sleepSummary.getSummary().getTotalMinutesAsleep();
                                                //validEntryCounter++;
                                            }
                                        }
                                    }
                                    barEntries.add(new BarEntry(i, weeklyTotal));
                                    if (weeklyTotal != 0) {
                                        total += weeklyTotal;
                                        validEntryCounter++;
                                    }
                                    i++;
                                }

                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                                Log.i(TAG, entries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.googlefit_camel_case))) {
            googleFitViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<GoogleFitSummary>>() {
                @Override
                public void onChanged(@Nullable List<GoogleFitSummary> googleFitSummaries) {
                    if (googleFitSummaries != null) {
                        styleBarChart();
                        List<GoogleFitSummary> reverseOrderedList = googleFitSummaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        if (reverseOrderedList.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfWeeks = numOfDays / 7;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfWeeks; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i));
                                }

                                //Log.i(TAG, activity + " " + googleFitSummaries.toString());

                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                for (String YYYW : entryDatesList) {
                                    float weeklyTotal = 0;
                                    for (GoogleFitSummary googleFitSummary : reverseOrderedList) {
                                        // find matching record
                                        if (YYYW.equals(formattedTime.convertLongToYYYYW(googleFitSummary.getDateInLong()))) {
                                            List<GoogleFitSummary.Summary> summaries = googleFitSummary.getSummaries();
                                            for (GoogleFitSummary.Summary summary : summaries) {
                                                String name = summary.getName().toLowerCase();
                                                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                                if (name.equals("Activeminutes")) {
                                                    name = getString(R.string.activity_active_minutes_title);
                                                } else if (name.equals("Sedentaryminutes")) {
                                                    name = getString(R.string.activity_sedentary_minutes_title);
                                                }
                                                if (name.equals(activity)) {
                                                    if (activity.equals(getString(R.string.activity_active_minutes_title)) || activity.equals(getString(R.string.activity_sedentary_minutes_title))) {
                                                        Double minutes = summary.getValue();
                                                        Long minutesLong = minutes.longValue();
                                                        minutesLong = TimeUnit.MILLISECONDS.toMinutes(minutesLong);
                                                        weeklyTotal += minutesLong;
                                                    } else if (activity.equals(getString(R.string.activity_distance_title))) {
                                                        weeklyTotal += summary.getValue().floatValue() / 1000;
                                                    } else {
                                                        weeklyTotal += summary.getValue().floatValue();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    barEntries.add(new BarEntry(i, weeklyTotal));
                                    if (weeklyTotal != 0) {
                                        total += weeklyTotal;
                                        validEntryCounter++;
                                    }
                                    i++;
                                }

                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.moabi_tracker_camel_case))) {
            builtInFitnessViewModel.getAllSummaries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<BuiltInActivitySummary>>() {
                @Override
                public void onChanged(@Nullable List<BuiltInActivitySummary> activitySummaries) {
                    if (activitySummaries != null) {
                        styleBarChart();
                        if (activitySummaries.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        List<BuiltInActivitySummary> reverseOrderedList = activitySummaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfWeeks = numOfDays / 7;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfWeeks; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i));
                                }

                                //Log.i(TAG, activity + " " + fitbitDailySummaries.toString());

                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                // iterate through the entry dates
                                // iterate through the summary list until we find a match in date
                                // if there is a match, do the thing.
                                // if not, put 0 as value and move on.

                                for (String YYYW : entryDatesList) {
                                    float weeklyTotal = 0;
                                    for (BuiltInActivitySummary activitySummary : reverseOrderedList) {
                                        // find matching record
                                        if (YYYW.equals(formattedTime.convertLongToYYYYW(activitySummary.getDateInLong()))) {

                                            if (activity.equals(getString(R.string.activity_steps_title))) {
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getSteps());
                                                weeklyTotal += activitySummary.getSteps();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_active_minutes_title))) {
                                                Long minutes = TimeUnit.MILLISECONDS.toMinutes(activitySummary.getActiveMinutes());
                                                //entries.put(fitbitDailySummary.getDate(), activeMinutes);
                                                //barEntries.add(new BarEntry(i, activeMinutes));
                                                weeklyTotal += minutes;
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_sedentary_minutes_title))) {
                                                Long minutes = TimeUnit.MILLISECONDS.toMinutes(activitySummary.getSedentaryMinutes());
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getSedentaryMinutes());
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getSedentaryMinutes()));
                                                weeklyTotal += minutes;
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_calories_title))) {
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getCaloriesOut());
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getCaloriesOut()));
                                                weeklyTotal += activitySummary.getCalories();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_distance_title))) {
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getDistances().get(0));
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getDistances().get(0).getDistance().floatValue()));
                                                weeklyTotal += activitySummary.getDistance() / 1000;
                                                //validEntryCounter++;
                                            }
                                        }
                                    }
                                    barEntries.add(new BarEntry(i, weeklyTotal));
                                    if (weeklyTotal != 0) {
                                        total += weeklyTotal;
                                        validEntryCounter++;
                                    }
                                    i++;
                                }

                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                                Log.i(TAG, entries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.weather_camel_case))) {
            weatherViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<WeatherDailySummary>>() {
                @Override
                public void onChanged(@Nullable List<WeatherDailySummary> weatherDailySummaries) {
                    if (weatherDailySummaries != null) {
                        styleBarChart();
                        if (weatherDailySummaries.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        List<WeatherDailySummary> reverseOrderedList = weatherDailySummaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfWeeks = numOfDays / 7;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfWeeks; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i));
                                }

                                //Log.i(TAG, activity + " " + fitbitDailySummaries.toString());

                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                // iterate through the entry dates
                                // iterate through the summary list until we find a match in date
                                // if there is a match, do the thing.
                                // if not, put 0 as value and move on.

                                for (String YYYW : entryDatesList) {
                                    float weeklyTotal = 0;
                                    for (WeatherDailySummary dailySummary : reverseOrderedList) {
                                        // find matching record
                                        if (YYYW.equals(formattedTime.convertLongToYYYYW(dailySummary.getDateInLong()))) {
                                            if (activity.equals(getString(R.string.precipitation_title))) {
                                                weeklyTotal += dailySummary.getTotalPrecipmm();
                                            } else if (activity.equals(getString(R.string.temperature_title))) {
                                                weeklyTotal += dailySummary.getAvgTempC();
                                            } else if (activity.equals(getString(R.string.humidity_title))) {
                                                weeklyTotal += dailySummary.getAvgHumidity();
                                            }
                                        }
                                    }
                                    barEntries.add(new BarEntry(i, weeklyTotal));
                                    if (weeklyTotal != 0) {
                                        total += weeklyTotal;
                                        validEntryCounter++;
                                    }
                                    i++;
                                }

                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                                Log.i(TAG, entries.toString());
                            }
                        }).start();
                    }
                }
            });
        }
    }

    private void setMonthlyBarChart(String activity, int numOfDays) {
        if (inputType.equals(getString(R.string.fitbit_camel_case))) {
            fitbitViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<FitbitDailySummary>>() {
                @Override
                public void onChanged(@Nullable List<FitbitDailySummary> fitbitDailySummaries) {
                    if (fitbitDailySummaries != null) {
                        styleBarChart();
                        if (fitbitDailySummaries.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        List<FitbitDailySummary> reverseOrderedList = fitbitDailySummaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfMonths = numOfDays / 30;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfMonths; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i), 0L);
                                }

                                //Log.i(TAG, activity + " " + fitbitDailySummaries.toString());

                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                // iterate through the entry dates
                                // iterate through the summary list until we find a match in date
                                // if there is a match, do the thing.
                                // if not, put 0 as value and move on.

                                for (String YYYYMM : entryDatesList) {
                                    float monthlyTotal = 0;
                                    for (FitbitDailySummary fitbitDailySummary : reverseOrderedList) {
                                        // find matching record
                                        if (YYYYMM.equals(formattedTime.convertLongToYYYYMM(fitbitDailySummary.getDateInLong()))) {
                                            if (activity.equals(getString(R.string.activity_steps_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getSteps());
                                                monthlyTotal += activitySummary.getSummary().getSteps();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_active_minutes_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                Long activeMinutes = activitySummary.getSummary().getFairlyActiveMinutes() + activitySummary.getSummary().getVeryActiveMinutes();
                                                //entries.put(fitbitDailySummary.getDate(), activeMinutes);
                                                //barEntries.add(new BarEntry(i, activeMinutes));
                                                monthlyTotal += activeMinutes;
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_sedentary_minutes_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getSedentaryMinutes());
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getSedentaryMinutes()));
                                                monthlyTotal += activitySummary.getSummary().getSedentaryMinutes();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_calories_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getCaloriesOut());
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getCaloriesOut()));
                                                monthlyTotal += activitySummary.getSummary().getCaloriesOut();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_floors_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getFloors());
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getFloors()));
                                                monthlyTotal += activitySummary.getSummary().getFloors();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_distance_title))) {
                                                FitbitActivitySummary activitySummary = fitbitDailySummary.getActivitySummary();
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getDistances().get(0));
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getDistances().get(0).getDistance().floatValue()));
                                                monthlyTotal += activitySummary.getSummary().getDistances().get(0).getDistance().floatValue();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_sleep_title))) {
                                                FitbitSleepSummary sleepSummary = fitbitDailySummary.getSleepSummary();
                                                //entries.put(fitbitDailySummary.getDate(), sleepSummary.getSummary().getTotalMinutesAsleep());
                                                //barEntries.add(new BarEntry(i, sleepSummary.getSummary().getTotalMinutesAsleep()));
                                                monthlyTotal += sleepSummary.getSummary().getTotalMinutesAsleep();
                                                //validEntryCounter++;
                                            }
                                        }
                                    }
                                    barEntries.add(new BarEntry(i, monthlyTotal));
                                    if (monthlyTotal != 0) {
                                        total += monthlyTotal;
                                        validEntryCounter++;
                                    }
                                    i++;
                                }
                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                                Log.i(TAG, entries.toString());
                            }
                        }).start();
                    }
                }

            });
        } else if (inputType.equals(getString(R.string.googlefit_camel_case))) {
            googleFitViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<GoogleFitSummary>>() {
                @Override
                public void onChanged(@Nullable List<GoogleFitSummary> googleFitSummaries) {
                    if (googleFitSummaries != null) {
                        styleBarChart();
                        List<GoogleFitSummary> reverseOrderedList = googleFitSummaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        if (reverseOrderedList.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfMonths = numOfDays / 30;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfMonths; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i), 0L);
                                }

                                //Log.i(TAG, activity + " " + googleFitSummaries.toString());

                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                for (String YYYYMM : entryDatesList) {
                                    float monthlyTotal = 0;
                                    for (GoogleFitSummary googleFitSummary : reverseOrderedList) {
                                        // find matching record
                                        if (YYYYMM.equals(formattedTime.convertLongToYYYYMM(googleFitSummary.getDateInLong()))) {
                                            List<GoogleFitSummary.Summary> summaries = googleFitSummary.getSummaries();
                                            for (GoogleFitSummary.Summary summary : summaries) {
                                                String name = summary.getName().toLowerCase();
                                                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                                if (name.equals("Activeminutes")) {
                                                    name = getString(R.string.activity_active_minutes_title);
                                                } else if (name.equals("Sedentaryminutes")) {
                                                    name = getString(R.string.activity_sedentary_minutes_title);
                                                }
                                                if (name.equals(activity)) {
                                                    if (activity.equals(getString(R.string.activity_active_minutes_title)) || activity.equals(getString(R.string.activity_sedentary_minutes_title))) {
                                                        Double minutes = summary.getValue();
                                                        Long minutesLong = minutes.longValue();
                                                        minutesLong = TimeUnit.MILLISECONDS.toMinutes(minutesLong);
                                                        monthlyTotal += minutesLong;
                                                    } else if (activity.equals(getString(R.string.activity_distance_title))) {
                                                        monthlyTotal += summary.getValue().floatValue() / 1000;
                                                    } else {
                                                        monthlyTotal += summary.getValue().floatValue();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    barEntries.add(new BarEntry(i, monthlyTotal));
                                    if (monthlyTotal != 0) {
                                        total += monthlyTotal;
                                        validEntryCounter++;
                                    }
                                    i++;
                                }
                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.moabi_tracker_camel_case))) {
            builtInFitnessViewModel.getAllSummaries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<BuiltInActivitySummary>>() {
                @Override
                public void onChanged(@Nullable List<BuiltInActivitySummary> activitySummaries) {
                    if (activitySummaries != null) {
                        styleBarChart();
                        if (activitySummaries.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        List<BuiltInActivitySummary> reverseOrderedList = activitySummaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfMonths = numOfDays / 30;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfMonths; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i), 0L);
                                }

                                //Log.i(TAG, activity + " " + fitbitDailySummaries.toString());

                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                // iterate through the entry dates
                                // iterate through the summary list until we find a match in date
                                // if there is a match, do the thing.
                                // if not, put 0 as value and move on.

                                for (String YYYYMM : entryDatesList) {
                                    float monthlyTotal = 0;
                                    for (BuiltInActivitySummary activitySummary : reverseOrderedList) {
                                        // find matching record
                                        if (YYYYMM.equals(formattedTime.convertLongToYYYYMM(activitySummary.getDateInLong()))) {
                                            if (activity.equals(getString(R.string.activity_steps_title))) {
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getSteps());
                                                monthlyTotal += activitySummary.getSteps();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_active_minutes_title))) {
                                                Long minutes = TimeUnit.MILLISECONDS.toMinutes(activitySummary.getActiveMinutes());
                                                //entries.put(fitbitDailySummary.getDate(), activeMinutes);
                                                //barEntries.add(new BarEntry(i, activeMinutes));
                                                monthlyTotal += minutes;
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_sedentary_minutes_title))) {
                                                Long minutes = TimeUnit.MILLISECONDS.toMinutes(activitySummary.getSedentaryMinutes());
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getSedentaryMinutes());
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getSedentaryMinutes()));
                                                monthlyTotal += minutes;
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_calories_title))) {
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getCaloriesOut());
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getCaloriesOut()));
                                                monthlyTotal += activitySummary.getCalories();
                                                //validEntryCounter++;
                                            } else if (activity.equals(getString(R.string.activity_distance_title))) {
                                                //entries.put(fitbitDailySummary.getDate(), activitySummary.getSummary().getDistances().get(0));
                                                //barEntries.add(new BarEntry(i, activitySummary.getSummary().getDistances().get(0).getDistance().floatValue()));
                                                monthlyTotal += activitySummary.getDistance() / 1000;
                                                //validEntryCounter++;
                                            }
                                        }
                                    }
                                    barEntries.add(new BarEntry(i, monthlyTotal));
                                    if (monthlyTotal != 0) {
                                        total += monthlyTotal;
                                        validEntryCounter++;
                                    }
                                    i++;
                                }
                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                                Log.i(TAG, entries.toString());
                            }
                        }).start();
                    }
                }

            });
        } else if (inputType.equals(getString(R.string.weather_camel_case))) {
            weatherViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<WeatherDailySummary>>() {
                @Override
                public void onChanged(@Nullable List<WeatherDailySummary> weatherDailySummaries) {
                    if (weatherDailySummaries != null) {
                        styleBarChart();
                        if (weatherDailySummaries.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        List<WeatherDailySummary> reverseOrderedList = weatherDailySummaries;
                        List<String> entryDatesList = new ArrayList<>();
                        List<BarEntry> barEntries = new ArrayList<>();
                        Map<String, Long> entries = new LinkedHashMap<>();
                        Collections.reverse(reverseOrderedList);

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfMonths = numOfDays / 30;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfMonths; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i), 0L);
                                }

                                //Log.i(TAG, activity + " " + fitbitDailySummaries.toString());

                                // 1. iterate through the list and get the list of activities
                                // 2. extract only the total from the list
                                // 3. add each total to a map.

                                int i = 0;
                                float total = 0;
                                int validEntryCounter = 0;

                                // iterate through the entry dates
                                // iterate through the summary list until we find a match in date
                                // if there is a match, do the thing.
                                // if not, put 0 as value and move on.

                                for (String YYYYMM : entryDatesList) {
                                    float monthlyTotal = 0;
                                    for (WeatherDailySummary dailySummary : reverseOrderedList) {
                                        // find matching record
                                        if (YYYYMM.equals(formattedTime.convertLongToYYYYMM(dailySummary.getDateInLong()))) {
                                            if (activity.equals(getString(R.string.precipitation_title))) {
                                                monthlyTotal += dailySummary.getTotalPrecipmm();
                                            } else if (activity.equals(getString(R.string.temperature_title))) {
                                                monthlyTotal += dailySummary.getAvgTempC();
                                            } else if (activity.equals(getString(R.string.humidity_title))) {
                                                monthlyTotal += dailySummary.getAvgHumidity();
                                            }
                                        }
                                    }
                                    barEntries.add(new BarEntry(i, monthlyTotal));
                                    if (monthlyTotal != 0) {
                                        total += monthlyTotal;
                                        validEntryCounter++;
                                    }
                                    i++;
                                }

                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawBarChart(barEntries, entryDatesList, activity, numOfDays, average);
                                        }
                                    });
                                }
                                Log.i(TAG, entries.toString());
                            }
                        }).start();
                    }
                }
            });
        }
    }

    private void drawBarChart
            (List<BarEntry> barEntries, List<String> entryDatesList, String activity,
             int numOfDays, float average) {
        if (barEntries.size() == 0) {
            barChart.isEmpty();
            barChart.clear();
            barChart.invalidate();
            return;
        }

        barChart.clear();
        Log.i(TAG, barEntries.toString());
        List<String> formattedEntryDatesList = new ArrayList<>();
        if (numOfDays == 7) {
            for (int i = 0; i < entryDatesList.size(); i++) {
                String newDate = formattedTime.convertStringYYYYMMDDToE(entryDatesList.get(i));
                formattedEntryDatesList.add(newDate.substring(0, 1));
            }
        }
        if (numOfDays == 31) {
            for (int i = 0; i < entryDatesList.size(); i++) {
                String newDate = formattedTime.convertStringYYYYMMDDToMD(entryDatesList.get(i));
                formattedEntryDatesList.add(newDate);
            }
        }

        if (numOfDays == 180) {
            for (int i = 0; i < entryDatesList.size(); i++) {
                String date = entryDatesList.get(i);
                int year = Integer.parseInt(date.substring(0, 4));
                int week = Integer.parseInt(date.substring(5));
                Long endTime = formattedTime.getEndTimeOfYYYYW(week, year);
                String newDate = formattedTime.convertLongToMD(endTime);
                formattedEntryDatesList.add(newDate);
            }
        }
        if (numOfDays == 395) {
            for (int i = 0; i < entryDatesList.size(); i++) {
                String newDate = formattedTime.convertStringYYYYMMToMMM(entryDatesList.get(i));
                formattedEntryDatesList.add(newDate);
            }
        }
        Log.i(TAG, formattedEntryDatesList.toString());
        final int numOfWeeks = numOfDays / 7;
        final int numOfMonths = numOfDays / 30;

        if (entryDatesList.size() != 0 && entryDatesList.get(entryDatesList.size() - 1).equals(setUpDatesForToday())) {
            entryDatesList.set(entryDatesList.size() - 1, getString(R.string.chart_date_today));
        }

        // set up x-axis
        if (entryDatesList.size() > 0) {
            XAxis xAxis = barChart.getXAxis();
            xAxis.setTypeface(tf);
            xAxis.setDrawGridLines(false);
            //xAxis.setAxisMinimum(0f);
            barChart.setDragEnabled(false);
            //xAxis.setLabelCount(7, true);
            xAxis.setDrawAxisLine(true);
            //xAxis.setXOffset(0.5f);
            xAxis.setSpaceMin(0.5f);
            xAxis.setSpaceMax(0.5f);
            xAxis.setTextSize(12);
            xAxis.setTextColor(Color.DKGRAY);
            xAxis.setAxisLineColor(ContextCompat.getColor(context, R.color.transparent_gray));
            xAxis.setCenterAxisLabels(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            if (numOfDays == 180) {
                float xMaximum = numOfWeeks - 1f;
                //xAxis.setAxisMaximum(xMaximum);
                //xAxis.setLabelCount(6, true);
                xAxis.setLabelCount(6);
                xAxis.setSpaceMax(1);
                xAxis.setSpaceMin(1);
                xAxis.setGranularity(1);
                xAxis.setGranularityEnabled(true);
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        //Log.i(TAG, "X-axis - " + value + ", " + (int) value);
                        if (value >= 0) {
                            int index = (int) value;
                            String date = "";
                            if (index < numOfWeeks && index >= 0) {
                                //Log.i(TAG, value + ", " + (int) value + ", " + entryDatesList.get(index));
                                date = formattedEntryDatesList.get(index);
                            }
                            return date;
                        } else {
                            return "";
                        }
                    }
                });
            } else if (numOfDays == 395) {
                float xMaximum = numOfMonths - 1f;
                //xAxis.setAxisMaximum(xMaximum);
                xAxis.setLabelCount(6);
                xAxis.setSpaceMax(1);
                xAxis.setSpaceMin(1);
                xAxis.setGranularity(1);
                xAxis.setGranularityEnabled(true);
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        //Log.i(TAG, value + ", " + (int) value);
                        if (value >= 0) {
                            int index = (int) value;
                            String date = "";
                            if (index < numOfMonths && index >= 0) {
                                //Log.i(TAG, value + ", " + (int) value + ", " + entryDatesList.get(index));
                                date = formattedEntryDatesList.get(index);
                            }
                            return date;
                        } else {
                            return "";
                        }
                    }
                });
            } else {
                float xMaximum = numOfDays - 1f;
                //xAxis.setAxisMaximum(xMaximum);
                if (numOfDays == 7) {
                    xAxis.setGranularity(1);
                    xAxis.setGranularityEnabled(true);
                    xAxis.setLabelCount(7);
                } else {
                    xAxis.setLabelCount(6);
                    xAxis.setSpaceMax(1);
                    xAxis.setGranularity(5);
                    xAxis.setGranularityEnabled(true);
                    xAxis.setSpaceMin(1);
                }
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        //Log.i(TAG, value + ", " + (int) value);
                        if (value >= 0) {
                            int index = (int) value;
                            String date = "";
                            if (index < numOfDays && index >= 0) {
                                //Log.i(TAG, value + ", " + (int) value + ", " + entryDatesList.get(index));
                                date = formattedEntryDatesList.get(index);
                                //Log.i(TAG, date + " at " + index);
                            }
                            return date;
                        } else {
                            return "";
                        }
                    }
                });
            }
        }

        YAxis leftAxis = barChart.getAxisLeft();
        YAxis rightAxis = barChart.getAxisRight();
        leftAxis.setEnabled(true);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextSize(12);
        leftAxis.setTypeface(tf);
        leftAxis.setSpaceMax(1f);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setLabelCount(3, true);
        /*
        leftAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return convertToTimeString(value);
            }
        });*/

        rightAxis.setAxisMinimum(0);
        rightAxis.setEnabled(false);

        leftAxis.removeAllLimitLines();
        LimitLine averageLL = new LimitLine(average, getString(R.string.chart_avg_abbr));
        averageLL.setLineWidth(0.5f);
        averageLL.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        averageLL.setTypeface(tf);
        averageLL.setTextStyle(Paint.Style.FILL);
        averageLL.enableDashedLine(4, 8, 1);
        averageLL.setTextColor(Color.DKGRAY);
        averageLL.setTextSize(12);
        //averageLL.setLineColor(ContextCompat.getColor(context, R.color.colorPrimary));
        averageLL.setLineColor(ContextCompat.getColor(context, R.color.transparent_gray));
        leftAxis.addLimitLine(averageLL);
        //leftAxis.setDrawLimitLinesBehindData(true);
        //leftAxis.addLimitLine(goodLL);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawAxisLine(false);
        //rightAxis.setDrawLimitLinesBehindData(true);
        rightAxis.setEnabled(false);

        BarDataSet set = new BarDataSet(barEntries, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        //set.setColor(ContextCompat.getColor(context, R.color.colorPrimary));

        //set.setLineWidth(1f);
        //Log.i(TAG, set.toString());
        BarData barData = new BarData(set);
        barData.setDrawValues(false);
        //barData.setValueTextSize(12f);

        int height = barChart.getHeight();
        Paint paint = barChart.getRenderer().getPaintRender();
        int[] colors = new int[]{ContextCompat.getColor(context, R.color.colorPrimaryDark), ContextCompat.getColor(context, R.color.colorPrimary),
                ContextCompat.getColor(context, R.color.white)};
        float[] positions = new float[]{0, 0.5f, 1};
        /*
        LinearGradient linearGradient = new LinearGradient(0, 0, 0, height,
                ContextCompat.getColor(context, R.color.colorPrimary), ContextCompat.getColor(context, R.color.white),
                Shader.TileMode.REPEAT);*/
        LinearGradient linearGradient = new LinearGradient(0, 0, 0, height,
                colors, positions,
                Shader.TileMode.REPEAT);
        paint.setShader(linearGradient);
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e.getY() <= 0) {
                    barChart.highlightValue(e.getX(), -1, false);
                    //barChart.highlightValue(h, false);
                }
            }

            @Override
            public void onNothingSelected() {
                //barData.setHighlightEnabled(true);
            }
        });

        if (numOfDays == 31) {
            barData.setBarWidth(0.6f);
        } else {
            barData.setBarWidth(0.6f);
        }

        //barChart.animateY(1000, Easing.EasingOption.Linear);
        barChart.setFitBars(true);
        barChart.setData(barData);
        FitnessTrackerBarChartMarkerView chartMarkerView = new FitnessTrackerBarChartMarkerView(context, R.layout.mpchart_chartvalueselectedview, entryDatesList, activity, numOfDays, barChart);
        barChart.setMarker(chartMarkerView);
        barChart.getLegend().setEnabled(false);
        barChart.invalidate();
    }

    private void setSummaryData(Map<String, Object> activitySummaryMap, int numOfDays) {
        if (inputType.equals(getString(R.string.fitbit_camel_case))) {
            Log.i(TAG, activitySummaryMap.toString());
            statsRecyclerView.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);
            meansAndSumItemAdapter.clear();
            for (Map.Entry<String, Object> activity : activitySummaryMap.entrySet()) {
                if (activity.getValue() instanceof Long) {
                    Long total = (Long) activity.getValue();
                    Long means = total / numOfDays;
                    meansAndSumItemAdapter.add(new MeansAndSumItem(activity.getKey(), means, total));
                } else {
                    Double total = (Double) activity.getValue();
                    Double means = total / numOfDays;
                    meansAndSumItemAdapter.add(new MeansAndSumItem(activity.getKey(), means, total));
                }
            }
        } else if (inputType.equals(getString(R.string.googlefit_camel_case))) {
            Log.i(TAG, activitySummaryMap.toString());
            statsRecyclerView.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);
            meansAndSumItemAdapter.clear();
            for (Map.Entry<String, Object> activity : activitySummaryMap.entrySet()) {
                if (activity.getKey().equals(getString(R.string.activity_active_minutes_camel_case)) || activity.getKey().equals(getString(R.string.activity_sedentary_minutes_camel_case))) {
                    Double total = (Double) activity.getValue();
                    Long totalLong = total.longValue();
                    totalLong = TimeUnit.MILLISECONDS.toMinutes(totalLong);
                    Long means = totalLong / numOfDays;
                    meansAndSumItemAdapter.add(new MeansAndSumItem(activity.getKey(), means, totalLong));
                } else if (activity.getKey().equals(getString(R.string.activity_distance_camel_case))) {
                    Double total = (Double) activity.getValue() / 1000;
                    Double means = total / numOfDays;
                    meansAndSumItemAdapter.add(new MeansAndSumItem(activity.getKey(), means, total));
                } else {
                    Double total = (Double) activity.getValue();
                    Double means = total / numOfDays;
                    meansAndSumItemAdapter.add(new MeansAndSumItem(activity.getKey(), means, total));
                }
            }
        } else if (inputType.equals(getString(R.string.moabi_tracker_camel_case))) {
            Log.i(TAG, activitySummaryMap.toString());
            statsRecyclerView.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);
            meansAndSumItemAdapter.clear();
            for (Map.Entry<String, Object> activity : activitySummaryMap.entrySet()) {
                if (activity.getValue() instanceof Long) {
                    Long total = (Long) activity.getValue();
                    Long means = total / numOfDays;
                    meansAndSumItemAdapter.add(new MeansAndSumItem(activity.getKey(), means, total));
                } else {
                    Double total = (Double) activity.getValue();
                    Double means = total / numOfDays;
                    meansAndSumItemAdapter.add(new MeansAndSumItem(activity.getKey(), means, total));
                }
            }
        } else if (inputType.equals(getString(R.string.weather_camel_case))) {
            Log.i(TAG, activitySummaryMap.toString());
            statsRecyclerView.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);
            meansAndSumItemAdapter.clear();
            for (Map.Entry<String, Object> activity : activitySummaryMap.entrySet()) {
                if (activity.getValue() instanceof Long) {
                    Long total = (Long) activity.getValue();
                    Long means = total / numOfDays;
                    meansAndSumItemAdapter.add(new MeansAndSumItem(activity.getKey(), means, total));
                } else {
                    Double total = (Double) activity.getValue();
                    Double means = total / numOfDays;
                    meansAndSumItemAdapter.add(new MeansAndSumItem(activity.getKey(), means, total));
                }
            }
        }
        if (inputType.equals(getString(R.string.timer_camel_case))) {
            Log.i(TAG, activitySummaryMap.toString());
            statsRecyclerView.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);
            meansAndSumItemAdapter.clear();
            for (Map.Entry<String, Object> activity : activitySummaryMap.entrySet()) {
                if (activity.getValue() instanceof Long) {
                    Long total = (Long) activity.getValue();
                    total = TimeUnit.MILLISECONDS.toMinutes(total);
                    Long means = total / numOfDays;
                    meansAndSumItemAdapter.add(new MeansAndSumItem(activity.getKey(), means, total));
                } else {
                    Double total = (Double) activity.getValue();
                    Double means = total / numOfDays;
                    meansAndSumItemAdapter.add(new MeansAndSumItem(activity.getKey(), means, total));
                }
            }
        }
    }

    private String setUpDatesForToday() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("MM-dd", Locale.US);
        return mdformat.format(calendar.getTime());
    }

    private String convertToTimeString(float timeInFloat) {
        long timeInMiliSecs = (long) timeInFloat;
        long minute = (timeInMiliSecs / (1000 * 60)) % 60;
        long hour = (timeInMiliSecs / (1000 * 60 * 60));
        //Log.i(TAG, timeInMiliSecs + " converted to " + hour);
        if (hour < 1) {
            if (minute < 1) {
                return minute + " m";
            } else {
                return minute + " m";
            }
        } else if (hour == 1) {
            return hour + " h";
        } else {
            return hour + " h";
        }
    }

    private void styleBarChart() {
        statsRecyclerView.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE);
        barChart.setScaleEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getDescription().setEnabled(false);
        barChart.setNoDataTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkComplt));
        barChart.setNoDataText(getString(R.string.chart_no_entry));
        barChart.setNoDataTextTypeface(tf);
        //lineChart.animateXY(1000, 1000);
        barChart.setTouchEnabled(true);
        //lineChart.setDragEnabled(false);
        barChart.setPinchZoom(true);
        barChart.setDoubleTapToZoomEnabled(true);
        barChart.setExtraOffsets(8, 0, 8, 8);
    }

    @Override
    public void onResume() {
        super.onResume();
        weekButton.setChecked(true);
        setSpinner(7);
        //TODO - the gradient doesn't display when a bar chart is set to be displayed on create.

    }
}
