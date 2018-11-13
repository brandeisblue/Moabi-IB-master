package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.appusage.AppUsage;
import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitActivitySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitSleepSummary;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.entity.stats.InsightSummaryBuiltInFitnessMediatorLiveData;
import com.ivorybridge.moabi.database.entity.stats.InsightSummaryFitbitMediatorLiveData;
import com.ivorybridge.moabi.database.entity.stats.InsightSummaryGoogleFitMediatorLiveData;
import com.ivorybridge.moabi.database.entity.stats.SimpleRegressionSummary;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.ui.adapter.IconSpinnerAdapter;
import com.ivorybridge.moabi.ui.recyclerviewitem.insight.InsightBestAndWorstItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.insight.InsightBodyAverageItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.insight.InsightRecommendationItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.insight.InsightTopThreeItem;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.AppUsageViewModel;
import com.ivorybridge.moabi.viewmodel.BuiltInFitnessViewModel;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.ivorybridge.moabi.viewmodel.FitbitViewModel;
import com.ivorybridge.moabi.viewmodel.GoogleFitViewModel;
import com.ivorybridge.moabi.viewmodel.MoodAndEnergyViewModel;
import com.ivorybridge.moabi.viewmodel.RegressionSummaryViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.android.segmented.SegmentedGroup;

public class InsightBodyFragment extends Fragment {

    private static final String TAG = InsightBodyFragment.class.getSimpleName();
    @BindView(R.id.fragment_insight_body_recyclerview)
    RecyclerView recyclerview;
    @BindView(R.id.fragment_insight_body_spinner)
    Spinner spinner;
    @BindView(R.id.fragment_insight_body_radiogroup)
    SegmentedGroup radioGroup;
    @BindView(R.id.fragment_insight_body_week_button)
    RadioButton weekButton;
    @BindView(R.id.fragment_insight_body_month_button)
    RadioButton monthButton;
    @BindView(R.id.fragment_insight_body_sixmonths_button)
    RadioButton sixMonthsButton;
    @BindView(R.id.fragment_insight_body_year_button)
    RadioButton yearButton;
    @BindView(R.id.fragment_insight_body_chipgroup)
    ChipGroup chipGroup;
    private MoodAndEnergyViewModel moodAndEnergyViewModel;
    private GoogleFitViewModel googleFitViewModel;
    private RegressionSummaryViewModel regressionSummaryViewModel;
    private DataInUseViewModel dataInUseViewModel;
    private BuiltInFitnessViewModel builtInFitnessViewModel;
    private FitbitViewModel fitbitViewModel;
    private AppUsageViewModel appUsageViewModel;
    private FormattedTime formattedTime;
    private Long startOfMonth;
    private Long now;
    private Long yesterday;
    private String todaysDate;
    private Long inputType;
    private Long FITBIT = 5L;
    private Long GOOGLEFIT = 6L;
    private Long APPUSAGE = 7L;
    private Long MOABI = 4L;
    private Long ACTIVITY = 8L;
    private int numOfDays = 7;
    private SharedPreferences bodyInsightPreferences;
    private FastAdapter<IItem> recyclerAdapter;
    private ItemAdapter<InsightBodyAverageItem> averageItemItemAdapter;
    private ItemAdapter<InsightBestAndWorstItem> bestAndAverageItemItemAdapter;
    private ItemAdapter<InsightRecommendationItem> recommendationItemItemAdapter;
    private ItemAdapter<InsightTopThreeItem> topThreeItemItemAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_insight_body, container, false);
        ButterKnife.bind(this, mView);
        radioGroup.setTintColor(ContextCompat.getColor(getContext(), R.color.colorPrimary), Color.WHITE);
        radioGroup.setUnCheckedTintColor(Color.WHITE, Color.BLACK);
        formattedTime = new FormattedTime();
        todaysDate = formattedTime.getCurrentDateAsYYYYMMDD();
        startOfMonth = formattedTime.getStartOfMonth(todaysDate);
        now = formattedTime.getCurrentTimeInMilliSecs();
        bodyInsightPreferences = getContext().getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_BODY_INSIGHT_SHARED_PREFERENCE_KEY),
                Context.MODE_PRIVATE);
        yesterday = formattedTime.convertStringYYYYMMDDToLong(formattedTime.getYesterdaysDateAsYYYYMMDD());
        moodAndEnergyViewModel = ViewModelProviders.of(this).get(MoodAndEnergyViewModel.class);
        googleFitViewModel = ViewModelProviders.of(this).get(GoogleFitViewModel.class);
        appUsageViewModel = ViewModelProviders.of(this).get(AppUsageViewModel.class);
        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);
        regressionSummaryViewModel = ViewModelProviders.of(this).get(RegressionSummaryViewModel.class);
        fitbitViewModel = ViewModelProviders.of(this).get(FitbitViewModel.class);
        builtInFitnessViewModel = ViewModelProviders.of(this).get(BuiltInFitnessViewModel.class);
        recyclerAdapter = new FastAdapter<>();
        averageItemItemAdapter = new ItemAdapter<>();
        bestAndAverageItemItemAdapter = new ItemAdapter<>();
        recommendationItemItemAdapter = new ItemAdapter<>();
        topThreeItemItemAdapter = new ItemAdapter<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerAdapter = FastAdapter.with(Arrays.asList(averageItemItemAdapter, bestAndAverageItemItemAdapter, topThreeItemItemAdapter, recommendationItemItemAdapter));
        recyclerview.setLayoutManager(layoutManager);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(recyclerAdapter);
        weekButton.setChecked(true);
        setSpinner(numOfDays);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    if (checkedId == R.id.fragment_insight_body_week_button) {
                        Log.i(TAG, getContext().getString(R.string.week));
                        Log.i(TAG, "week");
                        SharedPreferences.Editor editor = bodyInsightPreferences.edit();
                        numOfDays = 7;
                        setSpinner(7);
                    } else if (checkedId == R.id.fragment_insight_body_month_button) {
                        Log.i(TAG, getContext().getString(R.string.month));
                        Log.i(TAG, "month");
                        numOfDays = 28;
                        setSpinner(28);
                    } else if (checkedId == R.id.fragment_insight_body_year_button) {
                        Log.i(TAG, "year");
                        numOfDays = 392;
                        setSpinner(392);
                    } else {
                        Log.i(TAG, "6 months");
                        numOfDays = 182;
                        setSpinner(182);
                    }
                }
            }
        });
        return mView;
    }

    private void setSpinner(int numberOfDays) {
        dataInUseViewModel.getAllInputsInUse().observe(getViewLifecycleOwner(), new Observer<List<InputInUse>>() {
            @Override
            public void onChanged(@Nullable List<InputInUse> inputInUses) {
                if (inputInUses != null && inputInUses.size() > 0) {
                    Set<String> activitiesSet = new TreeSet<>();
                    for (InputInUse inputInUse : inputInUses) {
                        if (inputInUse.getName().equals(getString(R.string.fitbit_camel_case))) {
                            activitiesSet.add("5 Fitbit");
                        } else if (inputInUse.getName().equals(getString(R.string.googlefit_camel_case))) {
                            activitiesSet.add("6 Google Fit");
                        } else if (inputInUse.getName().equals(getString(R.string.phone_usage_camel_case))) {
                            activitiesSet.add("7 App Usage");
                        } else if (inputInUse.getName().equals(getString(R.string.moabi_tracker_camel_case))) {
                            activitiesSet.add("4 Moabi");
                        } /*else if (inputInUse.getName().equals(getString(R.string.baactivity_camel_case))) {
                            activitiesSet.add("8 Activity");
                        }*/
                    }
                    Log.i(TAG, activitiesSet.toString());
                    if (activitiesSet.size() > 0) {
                        String[] activitiesArray = activitiesSet.toArray(new String[0]);
                        int[] imagesArray = new int[activitiesSet.size()];
                        for (int i = 0; i < activitiesArray.length; i++) {
                            if (activitiesArray[i].equals("5 Fitbit")) {
                                activitiesArray[i] = getString(R.string.insight_summary_fitbit);
                                imagesArray[i] = R.drawable.ic_fitbit_logo;
                            } else if (activitiesArray[i].equals("6 Google Fit")) {
                                activitiesArray[i] = getString(R.string.insight_summary_googlefit);
                                imagesArray[i] = R.drawable.ic_googlefit;
                            } else if (activitiesArray[i].equals("7 App Usage")) {
                                activitiesArray[i] = "Summary (App Usage)";
                                imagesArray[i] = R.drawable.ic_appusage;
                            } else if (activitiesArray[i].equals("4 Moabi")) {
                                activitiesArray[i] = getString(R.string.insight_summary_moabi);
                                imagesArray[i] = R.drawable.ic_logo_monogram_colored;
                            } /*else if (activitiesArray[i].equals("8 Activity")) {
                                activitiesArray[i] = "Summary (Activity)";
                                imagesArray[i] = R.drawable.ic_logo_monogram_colored;
                            }*/
                        }
                        if (activitiesArray != null && getContext() != null) {
                            int spinnerSelection = bodyInsightPreferences.getInt("body_insight_spinner_selection", 0);
                            IconSpinnerAdapter iconSpinnerAdapter = new IconSpinnerAdapter(getContext(), activitiesArray, imagesArray);
                            spinner.setAdapter(iconSpinnerAdapter);
                            spinner.setSelection(spinnerSelection);
                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    Log.i(TAG, activitiesArray[position] + " is selected");
                                    SharedPreferences.Editor editor = bodyInsightPreferences.edit();
                                    editor.putInt("body_insight_spinner_selection", position);
                                    editor.apply();
                                    if (activitiesArray[position].equals(getString(R.string.insight_summary_fitbit))) {
                                        inputType = FITBIT;
                                        configureChipGroups();
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_summary_googlefit))) {
                                        inputType = GOOGLEFIT;
                                        configureChipGroups();
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_summary_phone_usage))) {
                                        inputType = APPUSAGE;
                                        configureChipGroups();
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_summary_moabi))) {
                                        inputType = MOABI;
                                        configureChipGroups();
                                        //configureSummary(ENERGY, numberOfDays);
                                        //displayAllInsights(MINDXFITBIT, numberOfDays);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_summary_activity))) {
                                        inputType = ACTIVITY;
                                        configureChipGroups();
                                        //configureSummary(ENERGY, numberOfDays);
                                        //displayAllInsights(MINDXFITBIT, numberOfDays);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void configureChipGroups() {
        if (inputType.equals(FITBIT)) {
            chipGroup.removeAllViews();
            Chip step = new Chip(getContext());
            step.setText(getString(R.string.activity_steps_title));
            step.setTextAppearanceResource(R.style.ChipTextStyle);
            step.setCheckable(true);
            step.setCheckedIconVisible(false);
            Chip activeMins = new Chip(getContext());
            activeMins.setText(getString(R.string.activity_active_minutes_title));
            activeMins.setTextAppearanceResource(R.style.ChipTextStyle);
            activeMins.setCheckable(true);
            activeMins.setCheckedIconVisible(false);
            Chip calories = new Chip(getContext());
            calories.setText(getString(R.string.activity_calories_title));
            calories.setTextAppearanceResource(R.style.ChipTextStyle);
            calories.setCheckable(true);
            calories.setCheckedIconVisible(false);
            Chip distance = new Chip(getContext());
            distance.setText(getString(R.string.activity_distance_title));
            distance.setTextAppearanceResource(R.style.ChipTextStyle);
            distance.setCheckable(true);
            distance.setCheckedIconVisible(false);
            Chip floors = new Chip(getContext());
            floors.setText(getString(R.string.activity_floors_title));
            floors.setTextAppearanceResource(R.style.ChipTextStyle);
            floors.setCheckable(true);
            floors.setCheckedIconVisible(false);
            Chip sedentaryMins = new Chip(getContext());
            sedentaryMins.setText(getString(R.string.activity_sedentary_minutes_title));
            sedentaryMins.setTextAppearanceResource(R.style.ChipTextStyle);
            sedentaryMins.setCheckable(true);
            sedentaryMins.setCheckedIconVisible(false);
            Chip sleep = new Chip(getContext());
            sleep.setText(getString(R.string.activity_sleep_title));
            sleep.setTextAppearanceResource(R.style.ChipTextStyle);
            sleep.setCheckable(true);
            sleep.setCheckedIconVisible(false);
            chipGroup.addView(activeMins);
            chipGroup.addView(calories);
            chipGroup.addView(distance);
            chipGroup.addView(floors);
            chipGroup.addView(sedentaryMins);
            chipGroup.addView(sleep);
            chipGroup.addView(step);
            activeMins.setChecked(true);
            configureSummary(getString(R.string.activity_active_minutes_title), numOfDays);
            chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(ChipGroup chipGroup, int i) {
                    Chip checkedChip = chipGroup.findViewById(chipGroup.getCheckedChipId());
                    if (checkedChip != null) {
                        Boolean isChecked = checkedChip.isChecked();
                        if (isChecked) {
                            configureSummary(checkedChip.getText().toString(), numOfDays);
                        }
                    }
                }
            });
        } else if (inputType.equals(GOOGLEFIT)) {
            chipGroup.removeAllViews();
            Chip step = new Chip(getContext());
            step.setText(getString(R.string.activity_steps_title));
            //step.setChipIconResource(R.drawable.ic_steps_black);
            step.setTextAppearanceResource(R.style.ChipTextStyle);
            //step.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_white));
            step.setCheckable(true);
            step.setCheckedIconVisible(false);
            Chip activeMins = new Chip(getContext());
            activeMins.setText(getString(R.string.activity_active_minutes_title));
            //activeMins.setChipIconResource(R.drawable.ic_active_black);
            activeMins.setTextAppearanceResource(R.style.ChipTextStyle);
            activeMins.setCheckable(true);
            //activeMins.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
            activeMins.setCheckedIconVisible(false);
            Chip calories = new Chip(getContext());
            calories.setText(getString(R.string.activity_calories_title));
            //calories.setChipIconResource(R.drawable.ic_calories_black);
            calories.setTextAppearanceResource(R.style.ChipTextStyle);
            calories.setCheckable(true);
            calories.setCheckedIconVisible(false);
            Chip distance = new Chip(getContext());
            distance.setText(getString(R.string.activity_distance_title));
            //distance.setChipIconResource(R.drawable.ic_distance_black);
            distance.setTextAppearanceResource(R.style.ChipTextStyle);
            distance.setCheckable(true);
            distance.setCheckedIconVisible(false);
            Chip sedentaryMins = new Chip(getContext());
            sedentaryMins.setText(getString(R.string.activity_sedentary_minutes_title));
            //sedentaryMins.setChipIconResource(R.drawable.ic_sedentary_black);
            sedentaryMins.setTextAppearanceResource(R.style.ChipTextStyle);
            sedentaryMins.setCheckable(true);
            sedentaryMins.setCheckedIconVisible(false);
            chipGroup.addView(activeMins);
            chipGroup.addView(calories);
            chipGroup.addView(distance);
            chipGroup.addView(sedentaryMins);
            chipGroup.addView(step);
            activeMins.setChecked(true);
            configureSummary(getString(R.string.activity_active_minutes_title), numOfDays);
            chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(ChipGroup chipGroup, int i) {
                    Chip checkedChip = chipGroup.findViewById(chipGroup.getCheckedChipId());
                    if (checkedChip != null) {
                        Boolean isChecked = checkedChip.isChecked();
                        if (isChecked) {
                            configureSummary(checkedChip.getText().toString(), numOfDays);
                        }
                    }
                }
            });
        } else if (inputType.equals(MOABI)) {
            chipGroup.removeAllViews();
            Chip step = new Chip(getContext());
            step.setText(getString(R.string.activity_steps_title));
            //step.setChipIconResource(R.drawable.ic_steps_black);
            step.setTextAppearanceResource(R.style.ChipTextStyle);
            //step.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_white));
            step.setCheckable(true);
            step.setCheckedIconVisible(false);
            Chip activeMins = new Chip(getContext());
            activeMins.setText(getString(R.string.activity_active_minutes_title));
            //activeMins.setChipIconResource(R.drawable.ic_active_black);
            activeMins.setTextAppearanceResource(R.style.ChipTextStyle);
            activeMins.setCheckable(true);
            //activeMins.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
            activeMins.setCheckedIconVisible(false);
            Chip calories = new Chip(getContext());
            calories.setText(getString(R.string.activity_calories_title));
            //calories.setChipIconResource(R.drawable.ic_calories_black);
            calories.setTextAppearanceResource(R.style.ChipTextStyle);
            calories.setCheckable(true);
            calories.setCheckedIconVisible(false);
            Chip distance = new Chip(getContext());
            distance.setText(getString(R.string.activity_distance_title));
            //distance.setChipIconResource(R.drawable.ic_distance_black);
            distance.setTextAppearanceResource(R.style.ChipTextStyle);
            distance.setCheckable(true);
            distance.setCheckedIconVisible(false);
            Chip sedentaryMins = new Chip(getContext());
            sedentaryMins.setText(getString(R.string.activity_sedentary_minutes_title));
            //sedentaryMins.setChipIconResource(R.drawable.ic_sedentary_black);
            sedentaryMins.setTextAppearanceResource(R.style.ChipTextStyle);
            sedentaryMins.setCheckable(true);
            sedentaryMins.setCheckedIconVisible(false);
            chipGroup.addView(activeMins);
            chipGroup.addView(calories);
            chipGroup.addView(distance);
            chipGroup.addView(sedentaryMins);
            chipGroup.addView(step);
            activeMins.setChecked(true);
            configureSummary(getString(R.string.activity_active_minutes_title), numOfDays);
            chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(ChipGroup chipGroup, int i) {
                    Chip checkedChip = chipGroup.findViewById(chipGroup.getCheckedChipId());
                    if (checkedChip != null) {
                        Boolean isChecked = checkedChip.isChecked();
                        if (isChecked) {
                            configureSummary(checkedChip.getText().toString(), numOfDays);
                        }
                    }
                }
            });
        } else if (inputType.equals(ACTIVITY)) {
            chipGroup.removeAllViews();
            Chip step = new Chip(getContext());
            step.setText(getString(R.string.activity_steps_title));
            step.setTextAppearanceResource(R.style.ChipTextStyle);
            step.setCheckable(true);
            step.setCheckedIconVisible(false);
            Chip activeMins = new Chip(getContext());
            activeMins.setText(getString(R.string.activity_active_minutes_title));
            activeMins.setTextAppearanceResource(R.style.ChipTextStyle);
            activeMins.setCheckable(true);
            activeMins.setCheckedIconVisible(false);
            Chip calories = new Chip(getContext());
            calories.setText(getString(R.string.activity_calories_title));
            calories.setTextAppearanceResource(R.style.ChipTextStyle);
            calories.setCheckable(true);
            calories.setCheckedIconVisible(false);
            Chip distance = new Chip(getContext());
            distance.setText(getString(R.string.activity_distance_title));
            distance.setTextAppearanceResource(R.style.ChipTextStyle);
            distance.setCheckable(true);
            distance.setCheckedIconVisible(false);
            Chip sedentaryMins = new Chip(getContext());
            sedentaryMins.setText(getString(R.string.activity_sedentary_minutes_title));
            sedentaryMins.setTextAppearanceResource(R.style.ChipTextStyle);
            sedentaryMins.setCheckable(true);
            sedentaryMins.setCheckedIconVisible(false);
            chipGroup.addView(activeMins);
            chipGroup.addView(calories);
            chipGroup.addView(distance);
            chipGroup.addView(sedentaryMins);
            chipGroup.addView(step);
            activeMins.setChecked(true);
            configureSummary(getString(R.string.activity_active_minutes_title), numOfDays);
            chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(ChipGroup chipGroup, int i) {
                    Chip checkedChip = chipGroup.findViewById(chipGroup.getCheckedChipId());
                    if (checkedChip != null) {
                        Boolean isChecked = checkedChip.isChecked();
                        if (isChecked) {
                            configureSummary(checkedChip.getText().toString(), numOfDays);
                        }
                    }
                }
            });
        } else if (inputType.equals(APPUSAGE)) {
            appUsageViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(
                    formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD()))
                    .observe(getViewLifecycleOwner(), new Observer<List<AppUsageSummary>>() {
                        @Override
                        public void onChanged(List<AppUsageSummary> appUsageSummaries) {
                            if (appUsageSummaries != null && appUsageSummaries.size() > 0) {
                                chipGroup.removeAllViews();
                                Set<String> apps = new TreeSet<>();
                                for (AppUsageSummary appUsageSummary : appUsageSummaries) {
                                    List<AppUsage> appUsages = appUsageSummary.getActivities();
                                    for (AppUsage appUsage : appUsages) {
                                        if (TimeUnit.MINUTES.toMillis(appUsage.getTotalTime()) > 10) {
                                            apps.add(appUsage.getAppName());
                                        }
                                    }
                                }
                                for (String name : apps) {
                                    Chip chip = new Chip(getContext());
                                    chip.setText(name);
                                    chip.setTextAppearanceResource(R.style.ChipTextStyle);
                                    chip.setCheckable(true);
                                    chip.setCheckedIconVisible(false);
                                    chipGroup.addView(chip);
                                }
                            }
                        }
                    });
        }
    }

    private void configureSummary(String activity, int numOfDays) {
        if (inputType == FITBIT) {
            InsightSummaryFitbitMediatorLiveData insightSummaryFitbitMediatorLiveData =
                    new InsightSummaryFitbitMediatorLiveData(fitbitViewModel.getAll(
                            formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                            formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())),
                            regressionSummaryViewModel.getAllBodySummaries(
                                    formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                                    now, numOfDays));
            insightSummaryFitbitMediatorLiveData.observe(getViewLifecycleOwner(), new Observer<Pair<List<FitbitDailySummary>, List<SimpleRegressionSummary>>>() {
                @Override
                public void onChanged(@Nullable Pair<List<FitbitDailySummary>, List<SimpleRegressionSummary>> listListPair) {
                    if (listListPair != null && listListPair.first != null && listListPair.second != null) {
                        if (listListPair.first.size() > 0) {
                            Handler handler = new Handler();
                            Map<String, Double> activitySummaryMap = new LinkedHashMap<>();
                            final List<String> entryDatesList = new ArrayList<>();
                            final List<BarEntry> barEntries = new ArrayList<>();
                            Map<String, Double> dataByDayMap = new LinkedHashMap<>();
                            Map<String, Long> countByDayMap = new LinkedHashMap<>();
                            final List<SimpleRegressionSummary> simpleRegressionSummaries = new ArrayList<>();
                            final List<SimpleRegressionSummary> sortedList = new ArrayList<>();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (listListPair.second.size() > 0) {
                                        simpleRegressionSummaries.addAll(listListPair.second);
                                        Collections.sort(simpleRegressionSummaries, new SimpleRegressionSummary.BestFitComparator());
                                        for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                            if (simpleRegressionSummary.getDepVarTypeString().equals(getString(R.string.fitbit_camel_case)) && activity.replace(" ", "").toLowerCase().trim().equals(simpleRegressionSummary.getDepVar().toLowerCase())) {
                                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                                    if (simpleRegressionSummary.getIndepVar().equals("Total")) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) > 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    }
                                                } else {
                                                    sortedList.add(simpleRegressionSummary);
                                                }
                                                Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                            }
                                        }
                                    }
                                    for (int i = 1; i <= numOfDays; i++) {
                                        String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                        String date = formattedTime.getDateBeforeSpecifiedNumberOfDaysAsEEE(lastEntryDate, numOfDays - i);
                                        Log.i(TAG, date);
                                        dataByDayMap.put(date, 0d);
                                        countByDayMap.put(date, 0L);
                                    }
                                    double total = 0;
                                    for (FitbitDailySummary dailySummary : listListPair.first) {
                                        FitbitActivitySummary activitySummary = dailySummary.getActivitySummary();
                                        FitbitSleepSummary sleepSummary = dailySummary.getSleepSummary();
                                        String formattedDate = formattedTime.convertStringYYYYMMDDToEEE(dailySummary.getDate());
                                        if (activity.equals(getString(R.string.activity_active_minutes_title))) {
                                            activitySummaryMap.put(dailySummary.getDate(), activitySummary.getSummary().getFairlyActiveMinutes().doubleValue() + activitySummary.getSummary().getVeryActiveMinutes().doubleValue());
                                            Long oldCount = countByDayMap.get(formattedDate);
                                            countByDayMap.put(formattedDate, oldCount + 1L);
                                            Double oldData = dataByDayMap.get(formattedDate);
                                            dataByDayMap.put(formattedDate, oldData + activitySummary.getSummary().getFairlyActiveMinutes().doubleValue() + activitySummary.getSummary().getVeryActiveMinutes().doubleValue());
                                        } else if (activity.equals(getString(R.string.activity_calories_title))) {
                                            activitySummaryMap.put(dailySummary.getDate(), activitySummary.getSummary().getCaloriesOut().doubleValue());
                                            Long oldCount = countByDayMap.get(formattedDate);
                                            countByDayMap.put(formattedDate, oldCount + 1L);
                                            Double oldData = dataByDayMap.get(formattedDate);
                                            dataByDayMap.put(formattedDate, oldData + activitySummary.getSummary().getCaloriesOut().doubleValue());
                                        } else if (activity.equals(getString(R.string.activity_distance_title))) {
                                            activitySummaryMap.put(dailySummary.getDate(), activitySummary.getSummary().getDistances().get(0).getDistance());
                                            Long oldCount = countByDayMap.get(formattedDate);
                                            countByDayMap.put(formattedDate, oldCount + 1L);
                                            Double oldData = dataByDayMap.get(formattedDate);
                                            dataByDayMap.put(formattedDate, oldData + activitySummary.getSummary().getDistances().get(0).getDistance());
                                        } else if (activity.equals(getString(R.string.activity_floors_title))) {
                                            activitySummaryMap.put(dailySummary.getDate(), activitySummary.getSummary().getFloors().doubleValue());
                                            Long oldCount = countByDayMap.get(formattedDate);
                                            countByDayMap.put(formattedDate, oldCount + 1L);
                                            Double oldData = dataByDayMap.get(formattedDate);
                                            dataByDayMap.put(formattedDate, oldData + activitySummary.getSummary().getFloors().doubleValue());
                                        } else if (activity.equals(getString(R.string.activity_sedentary_minutes_title))) {
                                            activitySummaryMap.put(dailySummary.getDate(), activitySummary.getSummary().getSedentaryMinutes().doubleValue());
                                            Long oldCount = countByDayMap.get(formattedDate);
                                            countByDayMap.put(formattedDate, oldCount + 1L);
                                            Double oldData = dataByDayMap.get(formattedDate);
                                            dataByDayMap.put(formattedDate, oldData + activitySummary.getSummary().getSedentaryMinutes().doubleValue());
                                        } else if (activity.equals(getString(R.string.activity_sleep_title))) {
                                            activitySummaryMap.put(dailySummary.getDate(), sleepSummary.getSummary().getTotalMinutesAsleep().doubleValue());
                                            Long oldCount = countByDayMap.get(formattedDate);
                                            countByDayMap.put(formattedDate, oldCount + 1L);
                                            Double oldData = dataByDayMap.get(formattedDate);
                                            dataByDayMap.put(formattedDate, oldData + sleepSummary.getSummary().getTotalMinutesAsleep().doubleValue());
                                        } else if (activity.equals(getString(R.string.activity_steps_title))) {
                                            activitySummaryMap.put(dailySummary.getDate(), activitySummary.getSummary().getSteps().doubleValue());
                                            Long oldCount = countByDayMap.get(formattedDate);
                                            countByDayMap.put(formattedDate, oldCount + 1L);
                                            Double oldData = dataByDayMap.get(formattedDate);
                                            dataByDayMap.put(formattedDate, oldData + activitySummary.getSummary().getSteps().doubleValue());
                                        }
                                    }

                                    int i = 0;
                                    float bestValue = 0;
                                    for (Map.Entry<String, Double> entry : dataByDayMap.entrySet()) {
                                        float entryValue = 0;
                                        total += entry.getValue();
                                        if (countByDayMap.get(entry.getKey()) != null && countByDayMap.get(entry.getKey()) != 0L) {
                                            entryValue = entry.getValue().floatValue() / countByDayMap.get(entry.getKey());
                                        }
                                        if (entryValue > bestValue) {
                                            bestValue = entryValue;
                                        }
                                        barEntries.add(new BarEntry(i, entryValue));
                                        entryDatesList.add(entry.getKey());
                                        i++;
                                    }

                                    Log.i(TAG, barEntries.toString());
                                    Log.i(TAG, dataByDayMap.toString());
                                    Log.i(TAG, countByDayMap.toString());
                                    double average = total / listListPair.first.size();
                                    final float finalValueToPass = bestValue;

                                    Log.i(TAG, activitySummaryMap.toString());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            averageItemItemAdapter.clear();
                                            bestAndAverageItemItemAdapter.clear();
                                            topThreeItemItemAdapter.clear();
                                            recommendationItemItemAdapter.clear();
                                            InsightBodyAverageItem averageItem = new InsightBodyAverageItem(activity, activitySummaryMap);
                                            averageItemItemAdapter.add(averageItem);
                                            InsightBestAndWorstItem bestAndWorstItem = new InsightBestAndWorstItem(barEntries, entryDatesList, average, finalValueToPass);
                                            bestAndAverageItemItemAdapter.add(bestAndWorstItem);
                                            if (sortedList.size() > 0) {
                                                if (getActivity() != null) {
                                                    topThreeItemItemAdapter.add(new InsightTopThreeItem(InsightBodyFragment.this, getString(R.string.mood_camel_case), sortedList));
                                                }
                                            }
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                }
            });
        } else if (inputType == GOOGLEFIT) {
            InsightSummaryGoogleFitMediatorLiveData insightSummaryGoogleFitMediatorLiveData =
                    new InsightSummaryGoogleFitMediatorLiveData(googleFitViewModel.getAll(
                            formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                            formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())),
                            regressionSummaryViewModel.getAllBodySummaries(
                                    formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                                    now, numOfDays));
            insightSummaryGoogleFitMediatorLiveData.observe(getViewLifecycleOwner(), new Observer<Pair<List<GoogleFitSummary>, List<SimpleRegressionSummary>>>() {
                @Override
                public void onChanged(@Nullable Pair<List<GoogleFitSummary>, List<SimpleRegressionSummary>> listListPair) {
                    if (listListPair != null && listListPair.first != null && listListPair.second != null) {
                        if (listListPair.first.size() > 0) {
                            Handler handler = new Handler();
                            Map<String, Double> activitySummaryMap = new LinkedHashMap<>();
                            final List<String> entryDatesList = new ArrayList<>();
                            final List<BarEntry> barEntries = new ArrayList<>();
                            Map<String, Double> dataByDayMap = new LinkedHashMap<>();
                            Map<String, Long> countByDayMap = new LinkedHashMap<>();
                            final List<SimpleRegressionSummary> simpleRegressionSummaries = new ArrayList<>();
                            final List<SimpleRegressionSummary> sortedList = new ArrayList<>();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (listListPair.second.size() > 0) {
                                        simpleRegressionSummaries.addAll(listListPair.second);
                                        Collections.sort(simpleRegressionSummaries, new SimpleRegressionSummary.BestFitComparator());
                                        for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                            if (simpleRegressionSummary.getDepVarTypeString().equals(getString(R.string.googlefit_camel_case)) && activity.replace(" ", "").toLowerCase().trim().equals(simpleRegressionSummary.getDepVar().toLowerCase())) {
                                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                                    if (simpleRegressionSummary.getIndepVar().equals("Total")) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) > 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    }
                                                } else {
                                                    sortedList.add(simpleRegressionSummary);
                                                }
                                                Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                            }
                                        }
                                    }
                                    for (int i = 1; i <= numOfDays; i++) {
                                        String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                        String date = formattedTime.getDateBeforeSpecifiedNumberOfDaysAsEEE(lastEntryDate, numOfDays - i);
                                        Log.i(TAG, date);
                                        dataByDayMap.put(date, 0d);
                                        countByDayMap.put(date, 0L);
                                    }
                                    double total = 0;
                                    for (GoogleFitSummary dailySummary : listListPair.first) {
                                        List<GoogleFitSummary.Summary> summaries = dailySummary.getSummaries();
                                        String formattedDate = formattedTime.convertStringYYYYMMDDToEEE(dailySummary.getDate());
                                        for (GoogleFitSummary.Summary summary : summaries) {
                                            String name = summary.getName();
                                            if (activity.equals(getString(R.string.activity_active_minutes_title))) {
                                                if (name.equals(getString(R.string.activity_active_minutes_camel_case))) {
                                                    Long minutes = TimeUnit.MILLISECONDS.toMinutes(summary.getValue().longValue());
                                                    activitySummaryMap.put(dailySummary.getDate(), minutes.doubleValue());
                                                    Long oldCount = countByDayMap.get(formattedDate);
                                                    countByDayMap.put(formattedDate, oldCount + 1L);
                                                    Double oldData = dataByDayMap.get(formattedDate);
                                                    dataByDayMap.put(formattedDate, oldData + minutes.doubleValue());
                                                }
                                            } else if (activity.equals(getString(R.string.activity_calories_title))) {
                                                if (name.equals(getString(R.string.activity_calories_camel_case))) {
                                                    activitySummaryMap.put(dailySummary.getDate(), summary.getValue());
                                                    Long oldCount = countByDayMap.get(formattedDate);
                                                    countByDayMap.put(formattedDate, oldCount + 1L);
                                                    Double oldData = dataByDayMap.get(formattedDate);
                                                    dataByDayMap.put(formattedDate, oldData + summary.getValue());
                                                }
                                            } else if (activity.equals(getString(R.string.activity_distance_title))) {
                                                if (name.equals(getString(R.string.activity_distance_camel_case))) {
                                                    activitySummaryMap.put(dailySummary.getDate(), summary.getValue() / 1000);
                                                    Long oldCount = countByDayMap.get(formattedDate);
                                                    countByDayMap.put(formattedDate, oldCount + 1L);
                                                    Double oldData = dataByDayMap.get(formattedDate);
                                                    dataByDayMap.put(formattedDate, oldData + summary.getValue() / 1000);
                                                }
                                            } else if (activity.equals(getString(R.string.activity_sedentary_minutes_title))) {
                                                if (name.equals(getString(R.string.activity_sedentary_minutes_camel_case))) {
                                                    Long minutes = TimeUnit.MILLISECONDS.toMinutes(summary.getValue().longValue());
                                                    activitySummaryMap.put(dailySummary.getDate(), minutes.doubleValue());
                                                    Long oldCount = countByDayMap.get(formattedDate);
                                                    countByDayMap.put(formattedDate, oldCount + 1L);
                                                    Double oldData = dataByDayMap.get(formattedDate);
                                                    dataByDayMap.put(formattedDate, oldData + minutes.doubleValue());
                                                }
                                            } else if (activity.equals(getString(R.string.activity_steps_title))) {
                                                if (name.equals(getString(R.string.activity_steps_camel_case))) {
                                                    activitySummaryMap.put(dailySummary.getDate(), summary.getValue());
                                                    Long oldCount = countByDayMap.get(formattedDate);
                                                    countByDayMap.put(formattedDate, oldCount + 1L);
                                                    Double oldData = dataByDayMap.get(formattedDate);
                                                    dataByDayMap.put(formattedDate, oldData + summary.getValue());
                                                }
                                            }
                                        }
                                    }

                                    int i = 0;
                                    float bestValue = 0;
                                    for (Map.Entry<String, Double> entry : dataByDayMap.entrySet()) {
                                        float entryValue = 0;
                                        total += entry.getValue();
                                        if (countByDayMap.get(entry.getKey()) != null && countByDayMap.get(entry.getKey()) != 0L) {
                                            entryValue = entry.getValue().floatValue() / countByDayMap.get(entry.getKey());
                                        }
                                        if (entryValue > bestValue) {
                                            bestValue = entryValue;
                                        }
                                        barEntries.add(new BarEntry(i, entryValue));
                                        entryDatesList.add(entry.getKey());
                                        i++;
                                    }

                                    Log.i(TAG, barEntries.toString());
                                    Log.i(TAG, dataByDayMap.toString());
                                    Log.i(TAG, countByDayMap.toString());
                                    double average = total / listListPair.first.size();
                                    final float finalValueToPass = bestValue;

                                    Log.i(TAG, activitySummaryMap.toString());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            averageItemItemAdapter.clear();
                                            bestAndAverageItemItemAdapter.clear();
                                            topThreeItemItemAdapter.clear();
                                            recommendationItemItemAdapter.clear();
                                            InsightBodyAverageItem averageItem = new InsightBodyAverageItem(activity, activitySummaryMap);
                                            averageItemItemAdapter.add(averageItem);
                                            InsightBestAndWorstItem bestAndWorstItem = new InsightBestAndWorstItem(barEntries, entryDatesList, average, finalValueToPass);
                                            bestAndAverageItemItemAdapter.add(bestAndWorstItem);
                                            if (sortedList.size() > 0) {
                                                if (getActivity() != null) {
                                                    topThreeItemItemAdapter.add(new InsightTopThreeItem(InsightBodyFragment.this, getString(R.string.mood_camel_case), sortedList));
                                                }
                                            }
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                }
            });
        } else if (inputType == MOABI) {
            InsightSummaryBuiltInFitnessMediatorLiveData insightSummaryBuiltInFitnessMediatorLiveData =
                    new InsightSummaryBuiltInFitnessMediatorLiveData(builtInFitnessViewModel.getAllSummaries(
                            formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                            formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())),
                            regressionSummaryViewModel.getAllBodySummaries(
                                    formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                                    now, numOfDays));
            insightSummaryBuiltInFitnessMediatorLiveData.observe(getViewLifecycleOwner(), new Observer<Pair<List<BuiltInActivitySummary>, List<SimpleRegressionSummary>>>() {
                @Override
                public void onChanged(@Nullable Pair<List<BuiltInActivitySummary>, List<SimpleRegressionSummary>> listListPair) {
                    if (listListPair != null && listListPair.first != null && listListPair.second != null) {
                        if (listListPair.first.size() > 0) {
                            Handler handler = new Handler();
                            Map<String, Double> activitySummaryMap = new LinkedHashMap<>();
                            final List<String> entryDatesList = new ArrayList<>();
                            final List<BarEntry> barEntries = new ArrayList<>();
                            Map<String, Double> dataByDayMap = new LinkedHashMap<>();
                            Map<String, Long> countByDayMap = new LinkedHashMap<>();
                            final List<SimpleRegressionSummary> simpleRegressionSummaries = new ArrayList<>();
                            final List<SimpleRegressionSummary> sortedList = new ArrayList<>();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (listListPair.second.size() > 0) {
                                        simpleRegressionSummaries.addAll(listListPair.second);
                                        Collections.sort(simpleRegressionSummaries, new SimpleRegressionSummary.BestFitComparator());
                                        for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                            if (simpleRegressionSummary.getDepVarTypeString().equals(getString(R.string.moabi_tracker_camel_case)) && activity.replace(" ", "").toLowerCase().trim().equals(simpleRegressionSummary.getDepVar().toLowerCase())) {
                                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                                    if (simpleRegressionSummary.getIndepVar().equals("Total")) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) > 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    }
                                                } else {
                                                    sortedList.add(simpleRegressionSummary);
                                                }
                                                Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                            }
                                        }
                                    }
                                    for (int i = 1; i <= numOfDays; i++) {
                                        String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                        String date = formattedTime.getDateBeforeSpecifiedNumberOfDaysAsEEE(lastEntryDate, numOfDays - i);
                                        Log.i(TAG, date);
                                        dataByDayMap.put(date, 0d);
                                        countByDayMap.put(date, 0L);
                                    }
                                    double total = 0;
                                    for (BuiltInActivitySummary dailySummary : listListPair.first) {
                                        String formattedDate = formattedTime.convertStringYYYYMMDDToEEE(dailySummary.getDate());
                                        if (activity.equals(getString(R.string.activity_active_minutes_title))) {
                                            activitySummaryMap.put(dailySummary.getDate(), (double) TimeUnit.MILLISECONDS.toMinutes(dailySummary.getActiveMinutes()));
                                            Long oldCount = countByDayMap.get(formattedDate);
                                            countByDayMap.put(formattedDate, oldCount + 1L);
                                            Double oldData = dataByDayMap.get(formattedDate);
                                            dataByDayMap.put(formattedDate, oldData + (double) TimeUnit.MILLISECONDS.toMinutes(dailySummary.getActiveMinutes()));
                                        } else if (activity.equals(getString(R.string.activity_calories_title))) {
                                            activitySummaryMap.put(dailySummary.getDate(), dailySummary.getCalories());
                                            Long oldCount = countByDayMap.get(formattedDate);
                                            countByDayMap.put(formattedDate, oldCount + 1L);
                                            Double oldData = dataByDayMap.get(formattedDate);
                                            dataByDayMap.put(formattedDate, oldData + dailySummary.getCalories());
                                        } else if (activity.equals(getString(R.string.activity_distance_title))) {
                                            activitySummaryMap.put(dailySummary.getDate(), dailySummary.getDistance() / 1000);
                                            Long oldCount = countByDayMap.get(formattedDate);
                                            countByDayMap.put(formattedDate, oldCount + 1L);
                                            Double oldData = dataByDayMap.get(formattedDate);
                                            dataByDayMap.put(formattedDate, oldData + dailySummary.getDistance() / 1000);
                                        } else if (activity.equals(getString(R.string.activity_sedentary_minutes_title))) {
                                            activitySummaryMap.put(dailySummary.getDate(), (double) TimeUnit.MILLISECONDS.toMinutes(dailySummary.getSedentaryMinutes()));
                                            Long oldCount = countByDayMap.get(formattedDate);
                                            countByDayMap.put(formattedDate, oldCount + 1L);
                                            Double oldData = dataByDayMap.get(formattedDate);
                                            dataByDayMap.put(formattedDate, oldData + (double) TimeUnit.MILLISECONDS.toMinutes(dailySummary.getSedentaryMinutes()));
                                        } else if (activity.equals(getString(R.string.activity_steps_title))) {
                                            activitySummaryMap.put(dailySummary.getDate(), dailySummary.getSteps().doubleValue());
                                            Long oldCount = countByDayMap.get(formattedDate);
                                            countByDayMap.put(formattedDate, oldCount + 1L);
                                            Double oldData = dataByDayMap.get(formattedDate);
                                            dataByDayMap.put(formattedDate, oldData + dailySummary.getSteps().doubleValue());
                                        }
                                    }

                                    int i = 0;
                                    float bestValue = 0;
                                    for (Map.Entry<String, Double> entry : dataByDayMap.entrySet()) {
                                        float entryValue = 0;
                                        total += entry.getValue();
                                        if (countByDayMap.get(entry.getKey()) != null && countByDayMap.get(entry.getKey()) != 0L) {
                                            entryValue = entry.getValue().floatValue() / countByDayMap.get(entry.getKey());
                                        }
                                        if (entryValue > bestValue) {
                                            bestValue = entryValue;
                                        }
                                        barEntries.add(new BarEntry(i, entryValue));
                                        entryDatesList.add(entry.getKey());
                                        i++;
                                    }

                                    Log.i(TAG, barEntries.toString());
                                    Log.i(TAG, dataByDayMap.toString());
                                    Log.i(TAG, countByDayMap.toString());
                                    double average = total / listListPair.first.size();
                                    final float finalValueToPass = bestValue;

                                    Log.i(TAG, activitySummaryMap.toString());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            averageItemItemAdapter.clear();
                                            bestAndAverageItemItemAdapter.clear();
                                            topThreeItemItemAdapter.clear();
                                            recommendationItemItemAdapter.clear();
                                            InsightBodyAverageItem averageItem = new InsightBodyAverageItem(activity, activitySummaryMap);
                                            averageItemItemAdapter.add(averageItem);
                                            InsightBestAndWorstItem bestAndWorstItem = new InsightBestAndWorstItem(barEntries, entryDatesList, average, finalValueToPass);
                                            bestAndAverageItemItemAdapter.add(bestAndWorstItem);
                                            if (sortedList.size() > 0) {
                                                if (getActivity() != null) {
                                                    topThreeItemItemAdapter.add(new InsightTopThreeItem(InsightBodyFragment.this, getString(R.string.mood_camel_case), sortedList));
                                                }
                                            }
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                }
            });
        }
    }

    private double[] toDoubleArray(List<Double> list) {
        double[] ret = new double[list.size()];
        int i = 0;
        for (Double e : list)
            ret[i++] = e;
        return ret;
    }
}
