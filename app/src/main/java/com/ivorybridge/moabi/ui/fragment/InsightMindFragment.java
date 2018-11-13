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
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.anxiety.DailyGad7;
import com.ivorybridge.moabi.database.entity.dailyreview.DailyDailyReview;
import com.ivorybridge.moabi.database.entity.depression.DailyPhq9;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.entity.moodandenergy.DailyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.DailyMood;
import com.ivorybridge.moabi.database.entity.stats.InsightSummaryAnxietyMediatorLiveData;
import com.ivorybridge.moabi.database.entity.stats.InsightSummaryDepressionMediatorLiveData;
import com.ivorybridge.moabi.database.entity.stats.InsightSummaryEnergyMediatorLiveData;
import com.ivorybridge.moabi.database.entity.stats.InsightSummaryMoodMediatorLiveData;
import com.ivorybridge.moabi.database.entity.stats.InsightSummaryDailyReviewMediatorLiveData;
import com.ivorybridge.moabi.database.entity.stats.InsightSummaryStressMediatorLiveData;
import com.ivorybridge.moabi.database.entity.stats.SimpleRegressionSummary;
import com.ivorybridge.moabi.database.entity.stress.DailyStress;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.ui.recyclerviewitem.insight.InsightBestAndWorstItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.insight.InsightMindAverageItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.insight.InsightRecommendationItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.insight.InsightTopThreeItem;
import com.ivorybridge.moabi.ui.adapter.IconSpinnerAdapter;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.AnxietyViewModel;
import com.ivorybridge.moabi.viewmodel.AppUsageViewModel;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.ivorybridge.moabi.viewmodel.DepressionViewModel;
import com.ivorybridge.moabi.viewmodel.FitbitViewModel;
import com.ivorybridge.moabi.viewmodel.GoogleFitViewModel;
import com.ivorybridge.moabi.viewmodel.MoodAndEnergyViewModel;
import com.ivorybridge.moabi.viewmodel.DailyReviewViewModel;
import com.ivorybridge.moabi.viewmodel.RegressionSummaryViewModel;
import com.ivorybridge.moabi.viewmodel.StressViewModel;
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

public class InsightMindFragment extends Fragment {

    private static final String TAG = InsightMindFragment.class.getSimpleName();
    private static final int MOOD = 0;
    private static final int ENERGY = 1;
    private static final int DEPRESSION = 7;
    private static final int ANXIETY = 8;
    private static final int STRESS = 5;
    private static final int DAILYREVIEW = 6;
    private static final int MINDXFITBIT = 2;
    private static final int MINDXGOOGLEFIT = 3;
    private static final int MINDXAPPUSAGE = 4;
    private static final int MINDXMOABI = 10;
    private static final int MINDXWEATHER = 11;
    private static final int MINDXTIMER = 12;
    private static final int MINDXACTIVITY = 13;
    @BindView(R.id.fragment_insight_mind_recyclerview)
    RecyclerView recyclerview;
    @BindView(R.id.fragment_insight_mind_spinner)
    Spinner spinner;
    @BindView(R.id.fragment_insight_mind_radiogroup)
    SegmentedGroup radioGroup;
    @BindView(R.id.fragment_insight_mind_week_button)
    RadioButton weekButton;
    @BindView(R.id.fragment_insight_mind_month_button)
    RadioButton monthButton;
    @BindView(R.id.fragment_insight_mind_sixmonths_button)
    RadioButton sixMonthsButton;
    @BindView(R.id.fragment_insight_mind_year_button)
    RadioButton yearButton;
    private MoodAndEnergyViewModel moodAndEnergyViewModel;
    private StressViewModel stressViewModel;
    private DailyReviewViewModel dailyReviewViewModel;
    private DepressionViewModel depressionViewModel;
    private AnxietyViewModel anxietyViewModel;
    private GoogleFitViewModel googleFitViewModel;
    private RegressionSummaryViewModel regressionSummaryViewModel;
    private DataInUseViewModel dataInUseViewModel;
    private FitbitViewModel fitbitViewModel;
    private AppUsageViewModel appUsageViewModel;
    private FormattedTime formattedTime;
    private Long startOfMonth;
    private Long now;
    private Long yesterday;
    private String todaysDate;
    private Long depVarType = 0L;
    private SharedPreferences mindInsightPreferences;
    private FastAdapter<IItem> recyclerAdapter;
    private ItemAdapter<InsightMindAverageItem> averageItemItemAdapter;
    private ItemAdapter<InsightBestAndWorstItem> bestAndAverageItemItemAdapter;
    private ItemAdapter<InsightRecommendationItem> recommendationItemItemAdapter;
    private ItemAdapter<InsightTopThreeItem> topThreeItemItemAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_insight_mind, container, false);
        ButterKnife.bind(this, mView);
        radioGroup.setTintColor(ContextCompat.getColor(getContext(), R.color.colorPrimary), Color.WHITE);
        radioGroup.setUnCheckedTintColor(Color.WHITE, Color.BLACK);
        formattedTime = new FormattedTime();
        todaysDate = formattedTime.getCurrentDateAsYYYYMMDD();
        startOfMonth = formattedTime.getStartOfMonth(todaysDate);
        now = formattedTime.getCurrentTimeInMilliSecs();
        mindInsightPreferences = getContext().getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_MIND_INSIGHT_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        yesterday = formattedTime.convertStringYYYYMMDDToLong(formattedTime.getYesterdaysDateAsYYYYMMDD());
        moodAndEnergyViewModel = ViewModelProviders.of(this).get(MoodAndEnergyViewModel.class);
        stressViewModel = ViewModelProviders.of(this).get(StressViewModel.class);
        dailyReviewViewModel = ViewModelProviders.of(this).get(DailyReviewViewModel.class);
        googleFitViewModel = ViewModelProviders.of(this).get(GoogleFitViewModel.class);
        appUsageViewModel = ViewModelProviders.of(this).get(AppUsageViewModel.class);
        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);
        regressionSummaryViewModel = ViewModelProviders.of(this).get(RegressionSummaryViewModel.class);
        fitbitViewModel = ViewModelProviders.of(this).get(FitbitViewModel.class);
        depressionViewModel = ViewModelProviders.of(this).get(DepressionViewModel.class);
        anxietyViewModel = ViewModelProviders.of(this).get(AnxietyViewModel.class);
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
        //calculateMoodAndEnergyLevelRegression(7);
        //calculateMoodAndEnergyLevelRegression(28);
        //calculateMoodAndEnergyLevelRegression(392);
        //calculateMoodAndEnergyLevelRegression(182);
        setSpinner(7);
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
                    if (checkedId == R.id.fragment_insight_mind_week_button) {
                        Log.i(TAG, getContext().getString(R.string.week));
                        setSpinner(7);
                    } else if (checkedId == R.id.fragment_insight_mind_month_button) {
                        Log.i(TAG, getContext().getString(R.string.month));
                        setSpinner(28);
                    } else if (checkedId == R.id.fragment_insight_mind_year_button) {
                        Log.i(TAG, "year");
                        setSpinner(392);
                    } else {
                        Log.i(TAG, "6 months");
                        setSpinner(182);
                    }
                }
            }
        });
        //calculateMoodAndEnergyLevelRegression();
        //calculateMoodAndGoogleFitRegression();
        return mView;
    }

    private void setSpinner(int numberOfDays) {
        dataInUseViewModel.getAllInputsInUse().observe(getViewLifecycleOwner(), new Observer<List<InputInUse>>() {
            @Override
            public void onChanged(@Nullable List<InputInUse> inputInUses) {
                if (inputInUses != null && inputInUses.size() > 0) {
                    Log.i(TAG, inputInUses.toString());
                    Set<String> activitiesSet = new TreeSet<>();
                    for (InputInUse inputInUse : inputInUses) {
                        if (inputInUse.isInUse()) {
                            if (inputInUse.getName().equals(getString(R.string.fitbit_camel_case))) {
                                activitiesSet.add("32 Fitbit");
                            } else if (inputInUse.getName().equals(getString(R.string.googlefit_camel_case))) {
                                activitiesSet.add("34 Google Fit");
                            } else if (inputInUse.getName().equals(getString(R.string.phone_usage_camel_case))) {
                                activitiesSet.add("36 App Usage");
                            } else if (inputInUse.getName().equals(getString(R.string.weather_camel_case))) {
                                activitiesSet.add("38 Weather");
                            } else if (inputInUse.getName().equals(getString(R.string.daily_review_camel_case))) {
                                activitiesSet.add("10 Daily Review");
                            } else if (inputInUse.getName().equals(getString(R.string.mood_and_energy_camel_case))) {
                                activitiesSet.add("12 Mood");
                                activitiesSet.add("14 Energy");
                            } else if (inputInUse.getName().equals(getString(R.string.stress_camel_case))) {
                                activitiesSet.add("16 Stress");
                            } else if (inputInUse.getName().equals(getString(R.string.baactivity_camel_case))) {
                                activitiesSet.add("20 Activity");
                            } else if (inputInUse.getName().equals(getString(R.string.moabi_tracker_camel_case))) {
                                activitiesSet.add("30 Moabi");
                            } else if (inputInUse.getName().equals(getString(R.string.timer_camel_case))) {
                                activitiesSet.add("42 Timer");
                            } else if (inputInUse.getName().equals(getString(R.string.depression_phq9_camel_case))) {
                                activitiesSet.add("17 Depression");
                            } else if (inputInUse.getName().equals(getString(R.string.anxiety_gad7_camel_case))) {
                                activitiesSet.add("18 Anxiety");
                            }
                        }
                    }
                    Log.i(TAG, activitiesSet.toString());
                    if (activitiesSet.size() > 0) {
                        String[] activitiesArray = activitiesSet.toArray(new String[0]);
                        int[] imagesArray = new int[activitiesSet.size()];
                        for (int i = 0; i < activitiesArray.length; i++) {
                            if (activitiesArray[i].equals("12 Mood")) {
                                activitiesArray[i] = getString(R.string.insight_summary_mood);
                                imagesArray[i] = R.drawable.ic_emotion;
                            } else if (activitiesArray[i].equals("14 Energy")) {
                                activitiesArray[i] = getString(R.string.insight_summary_energy);
                                imagesArray[i] = R.drawable.ic_emotion;
                            } else if (activitiesArray[i].equals("16 Stress")) {
                                activitiesArray[i] = getString(R.string.insight_summary_stress);
                                imagesArray[i] = R.drawable.ic_stress;
                            } else if (activitiesArray[i].equals("10 Daily Review")) {
                                activitiesArray[i] = getString(R.string.insight_summary_daily_review);
                                imagesArray[i] = R.drawable.ic_review_black;
                            } else if (activitiesArray[i].equals("20 Activity")) {
                                activitiesArray[i] = getString(R.string.insight_mind_x_activity);
                                imagesArray[i] = R.drawable.ic_physical_activity_black;
                            } else if (activitiesArray[i].equals("32 Fitbit")) {
                                activitiesArray[i] = getString(R.string.insight_mind_x_fitbit);
                                imagesArray[i] = R.drawable.ic_fitbit_logo;
                            } else if (activitiesArray[i].equals("34 Google Fit")) {
                                activitiesArray[i] = getString(R.string.insight_mind_x_googlefit);
                                imagesArray[i] = R.drawable.ic_googlefit;
                            } else if (activitiesArray[i].equals("36 App Usage")) {
                                activitiesArray[i] = getString(R.string.insight_mind_x_phone_usage);
                                imagesArray[i] = R.drawable.ic_appusage;
                            } else if (activitiesArray[i].equals("38 Weather")) {
                                activitiesArray[i] = getString(R.string.insight_mind_x_weather);
                                imagesArray[i] = R.drawable.ic_partly_cloudy;
                            } else if (activitiesArray[i].equals("30 Moabi")) {
                                activitiesArray[i] = getString(R.string.insight_mind_x_moabi);
                                imagesArray[i] = R.drawable.ic_logo_monogram_colored;
                            } else if (activitiesArray[i].equals("42 Timer")) {
                                activitiesArray[i] = getString(R.string.insight_mind_x_timer);
                                imagesArray[i] = R.drawable.ic_stopwatch;
                            } else if (activitiesArray[i].equals("17 Depression")) {
                                activitiesArray[i] = getString(R.string.insight_summary_depression);
                                imagesArray[i] = R.drawable.ic_depression_rain_black;
                            } else if (activitiesArray[i].equals("18 Anxiety")) {
                                activitiesArray[i] = getString(R.string.insight_summary_anxiety);
                                imagesArray[i] = R.drawable.ic_anxiety_insomnia_black;
                            }
                        }
                        if (activitiesArray != null && getContext() != null) {
                            int spinnerSelection = mindInsightPreferences.getInt("mind_insight_spinner_selection", 0);
                            IconSpinnerAdapter iconSpinnerAdapter = new IconSpinnerAdapter(getContext(), activitiesArray, imagesArray);
                            spinner.setAdapter(iconSpinnerAdapter);
                            spinner.setSelection(spinnerSelection);
                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    Log.i(TAG, activitiesArray[position] + " is selected");
                                    SharedPreferences.Editor editor = mindInsightPreferences.edit();
                                    editor.putInt("mind_insight_spinner_selection", position);
                                    editor.apply();
                                    if (activitiesArray[position].equals(getString(R.string.insight_summary_mood))) {
                                        configureSummary(MOOD, numberOfDays);
                                        //displayAllInsights(MOOD);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_summary_energy))) {
                                        configureSummary(ENERGY, numberOfDays);
                                        //displayAllInsights(ENERGY);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_summary_stress))) {
                                        configureSummary(STRESS, numberOfDays);
                                        //displayAllInsights(ENERGY);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_summary_daily_review))) {
                                        configureSummary(DAILYREVIEW, numberOfDays);
                                        //displayAllInsights(ENERGY);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_summary_depression))) {
                                        configureSummary(DEPRESSION, numberOfDays);
                                        //displayAllInsights(ENERGY);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_summary_anxiety))) {
                                        configureSummary(ANXIETY, numberOfDays);
                                        //displayAllInsights(ENERGY);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_mind_x_fitbit))) {
                                        //configureSummary(ENERGY, numberOfDays);
                                        displayAllInsights(MINDXFITBIT, numberOfDays);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_mind_x_googlefit))) {
                                        //configureSummary(ENERGY, numberOfDays);
                                        displayAllInsights(MINDXGOOGLEFIT, numberOfDays);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_mind_x_phone_usage))) {
                                        //configureSummary(ENERGY, numberOfDays);
                                        displayAllInsights(MINDXAPPUSAGE, numberOfDays);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_mind_x_weather))) {
                                        //configureSummary(ENERGY, numberOfDays);
                                        displayAllInsights(MINDXWEATHER, numberOfDays);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_mind_x_moabi))) {
                                        //configureSummary(ENERGY, numberOfDays);
                                        displayAllInsights(MINDXMOABI, numberOfDays);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_mind_x_activity))) {
                                        //configureSummary(ENERGY, numberOfDays);
                                        displayAllInsights(MINDXACTIVITY, numberOfDays);
                                    } else if (activitiesArray[position].equals(getString(R.string.insight_mind_x_timer))) {
                                        //configureSummary(ENERGY, numberOfDays);
                                        displayAllInsights(MINDXTIMER, numberOfDays);
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

    private void configureSummary(int itemType, int numOfDays) {
        if (itemType == MOOD) {
            InsightSummaryMoodMediatorLiveData insightMoodMediatorLiveData =
                    new InsightSummaryMoodMediatorLiveData(moodAndEnergyViewModel.getDailyMoods(
                            formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                            formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())),
                            regressionSummaryViewModel.getAllMindSummaries(
                                    formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                                    now, numOfDays));
            insightMoodMediatorLiveData.observe(getViewLifecycleOwner(), new Observer<Pair<List<DailyMood>, List<SimpleRegressionSummary>>>() {
                @Override
                public void onChanged(@Nullable Pair<List<DailyMood>, List<SimpleRegressionSummary>> listListPair) {
                    if (listListPair != null && listListPair.first != null && listListPair.second != null) {
                        if (listListPair.first.size() > 0) {
                            Handler handler = new Handler();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    double total = 0;
                                    final List<String> entryDatesList = new ArrayList<>();
                                    final List<BarEntry> barEntries = new ArrayList<>();
                                    Map<String, Double> dataByDayMap = new LinkedHashMap<>();
                                    Map<String, Long> countByDayMap = new LinkedHashMap<>();
                                    String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                    for (int i = 1; i <= numOfDays; i++) {
                                        String date = formattedTime.getDateBeforeSpecifiedNumberOfDaysAsEEE(lastEntryDate, numOfDays - i);
                                        Log.i(TAG, date);
                                        dataByDayMap.put(date, 0d);
                                        countByDayMap.put(date, 0L);
                                    }
                                    Log.i(TAG, dataByDayMap.toString());
                                    Log.i(TAG, countByDayMap.toString());

                                    for (DailyMood dailyMood : listListPair.first) {
                                        // find matching record
                                        total += dailyMood.getAverageMood();
                                        String formattedDate = formattedTime.convertStringYYYYMMDDToEEE(dailyMood.getDate());
                                        Long oldCount = countByDayMap.get(formattedDate);
                                        countByDayMap.put(formattedDate, oldCount + 1L);
                                        Double oldData = dataByDayMap.get(formattedDate);
                                        dataByDayMap.put(formattedDate, oldData + dailyMood.getAverageMood());
                                    }
                                    int i = 0;
                                    float bestValue = 0;
                                    for (Map.Entry<String, Double> entry : dataByDayMap.entrySet()) {
                                        float entryValue = 0;
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
                                    //displayAllInsights(itemType);
                                    Log.i(TAG, "Average is " + average);
                                    final List<SimpleRegressionSummary> simpleRegressionSummaries = new ArrayList<>();
                                    final List<SimpleRegressionSummary> sortedList = new ArrayList<>();
                                    if (listListPair.second.size() > 0) {
                                        simpleRegressionSummaries.addAll(listListPair.second);
                                        Collections.sort(simpleRegressionSummaries, new SimpleRegressionSummary.BestFitComparator());
                                        for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                            if (simpleRegressionSummary.getDepVar().equals(getString(R.string.mood_camel_case))) {
                                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                                    if (simpleRegressionSummary.getIndepVar().equals("Total")) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    }
                                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                    }
                                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 1) {
                                                        sortedList.add(simpleRegressionSummary);
                                                    }
                                                } else {
                                                    sortedList.add(simpleRegressionSummary);
                                                }
                                                Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                            }
                                        }
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            averageItemItemAdapter.clear();
                                            bestAndAverageItemItemAdapter.clear();
                                            topThreeItemItemAdapter.clear();
                                            recommendationItemItemAdapter.clear();
                                            InsightMindAverageItem moodAverageItem = new InsightMindAverageItem(getString(R.string.mood_camel_case), average);
                                            InsightBestAndWorstItem bestAndWorstItem = new InsightBestAndWorstItem(barEntries, entryDatesList, average, finalValueToPass);
                                            averageItemItemAdapter.add(moodAverageItem);
                                            bestAndAverageItemItemAdapter.add(bestAndWorstItem);
                                            if (sortedList.size() > 0) {
                                                if (getActivity() != null) {
                                                    topThreeItemItemAdapter.add(new InsightTopThreeItem(InsightMindFragment.this, getString(R.string.mood_camel_case), sortedList));
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
        } else if (itemType == ENERGY) {
            InsightSummaryEnergyMediatorLiveData insightEnergyMediatorLiveData =
                    new InsightSummaryEnergyMediatorLiveData(moodAndEnergyViewModel.getDailyEnergies(
                            formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                            formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())),
                            regressionSummaryViewModel.getAllMindSummaries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                                    now, numOfDays));
            insightEnergyMediatorLiveData.observe(getViewLifecycleOwner(), new Observer<Pair<List<DailyEnergy>, List<SimpleRegressionSummary>>>() {
                @Override
                public void onChanged(@Nullable Pair<List<DailyEnergy>, List<SimpleRegressionSummary>> listListPair) {
                    if (listListPair != null && listListPair.first != null && listListPair.second != null) {
                        if (listListPair.first.size() > 0) {
                            Handler handler = new Handler();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    double total = 0;
                                    final List<String> entryDatesList = new ArrayList<>();
                                    final List<BarEntry> barEntries = new ArrayList<>();
                                    Map<String, Double> dataByDayMap = new LinkedHashMap<>();
                                    Map<String, Long> countByDayMap = new LinkedHashMap<>();
                                    String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                    for (int i = 1; i <= numOfDays; i++) {
                                        String date = formattedTime.getDateBeforeSpecifiedNumberOfDaysAsEEE(lastEntryDate, numOfDays - i);
                                        Log.i(TAG, date);
                                        dataByDayMap.put(date, 0d);
                                        countByDayMap.put(date, 0L);
                                    }
                                    Log.i(TAG, dataByDayMap.toString());
                                    Log.i(TAG, countByDayMap.toString());

                                    for (DailyEnergy dailyEnergy : listListPair.first) {
                                        // find matching record
                                        total += dailyEnergy.getAverageEnergy();
                                        String formattedDate = formattedTime.convertStringYYYYMMDDToEEE(dailyEnergy.getDate());
                                        Long oldCount = countByDayMap.get(formattedDate);
                                        countByDayMap.put(formattedDate, oldCount + 1L);
                                        Double oldData = dataByDayMap.get(formattedDate);
                                        dataByDayMap.put(formattedDate, oldData + dailyEnergy.getAverageEnergy());
                                    }
                                    int i = 0;
                                    float bestValue = 0;
                                    for (Map.Entry<String, Double> entry : dataByDayMap.entrySet()) {
                                        float entryValue = 0;
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
                                    //displayAllInsights(itemType);
                                    Log.i(TAG, "Average is " + average);
                                    final List<SimpleRegressionSummary> simpleRegressionSummaries = new ArrayList<>();
                                    final List<SimpleRegressionSummary> sortedList = new ArrayList<>();
                                    if (listListPair.second.size() > 0) {
                                        simpleRegressionSummaries.addAll(listListPair.second);
                                        Collections.sort(simpleRegressionSummaries, new SimpleRegressionSummary.BestFitComparator());
                                        for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                            if (simpleRegressionSummary.getDepVar().equals(getString(R.string.energy_camel_case))) {
                                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                                    if (simpleRegressionSummary.getIndepVar().equals("Total")) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    }
                                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                    }
                                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 1) {
                                                        sortedList.add(simpleRegressionSummary);
                                                    }
                                                } else {
                                                    sortedList.add(simpleRegressionSummary);
                                                }
                                                Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                            }
                                        }
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            averageItemItemAdapter.clear();
                                            bestAndAverageItemItemAdapter.clear();
                                            topThreeItemItemAdapter.clear();
                                            recommendationItemItemAdapter.clear();
                                            InsightMindAverageItem moodAverageItem = new InsightMindAverageItem(getString(R.string.energy_camel_case), average);
                                            InsightBestAndWorstItem bestAndWorstItem = new InsightBestAndWorstItem(barEntries, entryDatesList, average, finalValueToPass);
                                            averageItemItemAdapter.add(moodAverageItem);
                                            bestAndAverageItemItemAdapter.add(bestAndWorstItem);
                                            if (sortedList.size() > 0) {
                                                if (getActivity() != null) {
                                                    topThreeItemItemAdapter.add(new InsightTopThreeItem(InsightMindFragment.this, getString(R.string.energy_camel_case), sortedList));
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
        } else if (itemType == STRESS) {
            InsightSummaryStressMediatorLiveData insightEnergyMediatorLiveData =
                    new InsightSummaryStressMediatorLiveData(stressViewModel.getDailyStress(
                            formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                            formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())),
                            regressionSummaryViewModel.getAllMindSummaries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                                    now, numOfDays));
            insightEnergyMediatorLiveData.observe(getViewLifecycleOwner(), new Observer<Pair<List<DailyStress>, List<SimpleRegressionSummary>>>() {
                @Override
                public void onChanged(@Nullable Pair<List<DailyStress>, List<SimpleRegressionSummary>> listListPair) {
                    if (listListPair != null && listListPair.first != null && listListPair.second != null) {
                        if (listListPair.first.size() > 0) {
                            Handler handler = new Handler();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    double total = 0;
                                    final List<String> entryDatesList = new ArrayList<>();
                                    final List<BarEntry> barEntries = new ArrayList<>();
                                    Map<String, Double> dataByDayMap = new LinkedHashMap<>();
                                    Map<String, Long> countByDayMap = new LinkedHashMap<>();
                                    String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                    for (int i = 1; i <= numOfDays; i++) {
                                        String date = formattedTime.getDateBeforeSpecifiedNumberOfDaysAsEEE(lastEntryDate, numOfDays - i);
                                        Log.i(TAG, date);
                                        dataByDayMap.put(date, 0d);
                                        countByDayMap.put(date, 0L);
                                    }
                                    Log.i(TAG, dataByDayMap.toString());
                                    Log.i(TAG, countByDayMap.toString());

                                    for (DailyStress dailyEnergy : listListPair.first) {
                                        // find matching record
                                        total += dailyEnergy.getAverageStress();
                                        String formattedDate = formattedTime.convertStringYYYYMMDDToEEE(dailyEnergy.getDate());
                                        Long oldCount = countByDayMap.get(formattedDate);
                                        countByDayMap.put(formattedDate, oldCount + 1L);
                                        Double oldData = dataByDayMap.get(formattedDate);
                                        dataByDayMap.put(formattedDate, oldData + dailyEnergy.getAverageStress());
                                    }
                                    int i = 0;
                                    float bestValue = 0;
                                    for (Map.Entry<String, Double> entry : dataByDayMap.entrySet()) {
                                        float entryValue = 0;
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
                                    //displayAllInsights(itemType);
                                    Log.i(TAG, "Average is " + average);
                                    final List<SimpleRegressionSummary> simpleRegressionSummaries = new ArrayList<>();
                                    final List<SimpleRegressionSummary> sortedList = new ArrayList<>();
                                    if (listListPair.second.size() > 0) {
                                        simpleRegressionSummaries.addAll(listListPair.second);
                                        Collections.sort(simpleRegressionSummaries, new SimpleRegressionSummary.BestFitComparator());
                                        for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                            if (simpleRegressionSummary.getDepVar().equals(getString(R.string.stress_camel_case))) {
                                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                                    if (simpleRegressionSummary.getIndepVar().equals("Total")) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    }
                                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                    }
                                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 1) {
                                                        sortedList.add(simpleRegressionSummary);
                                                    }
                                                } else {
                                                    sortedList.add(simpleRegressionSummary);
                                                }
                                                Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                            }
                                        }
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            averageItemItemAdapter.clear();
                                            bestAndAverageItemItemAdapter.clear();
                                            topThreeItemItemAdapter.clear();
                                            recommendationItemItemAdapter.clear();
                                            InsightMindAverageItem moodAverageItem = new InsightMindAverageItem(getString(R.string.stress_camel_case), average);
                                            InsightBestAndWorstItem bestAndWorstItem = new InsightBestAndWorstItem(barEntries, entryDatesList, average, finalValueToPass);
                                            averageItemItemAdapter.add(moodAverageItem);
                                            bestAndAverageItemItemAdapter.add(bestAndWorstItem);
                                            if (sortedList.size() > 0) {
                                                if (getActivity() != null) {
                                                    topThreeItemItemAdapter.add(new InsightTopThreeItem(InsightMindFragment.this, getString(R.string.stress_camel_case), sortedList));
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
        } else if (itemType == DAILYREVIEW) {
            InsightSummaryDailyReviewMediatorLiveData insightSummaryDailyReviewMediatorLiveData =
                    new InsightSummaryDailyReviewMediatorLiveData(dailyReviewViewModel.getDailyDailyReviews(
                            formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                            formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())),
                            regressionSummaryViewModel.getAllMindSummaries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                                    now, numOfDays));
            insightSummaryDailyReviewMediatorLiveData.observe(getViewLifecycleOwner(), new Observer<Pair<List<DailyDailyReview>, List<SimpleRegressionSummary>>>() {
                @Override
                public void onChanged(@Nullable Pair<List<DailyDailyReview>, List<SimpleRegressionSummary>> listListPair) {
                    if (listListPair != null && listListPair.first != null && listListPair.second != null) {
                        if (listListPair.first.size() > 0) {
                            Handler handler = new Handler();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    double total = 0;
                                    final List<String> entryDatesList = new ArrayList<>();
                                    final List<BarEntry> barEntries = new ArrayList<>();
                                    Map<String, Double> dataByDayMap = new LinkedHashMap<>();
                                    Map<String, Long> countByDayMap = new LinkedHashMap<>();
                                    String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                    for (int i = 1; i <= numOfDays; i++) {
                                        String date = formattedTime.getDateBeforeSpecifiedNumberOfDaysAsEEE(lastEntryDate, numOfDays - i);
                                        Log.i(TAG, date);
                                        dataByDayMap.put(date, 0d);
                                        countByDayMap.put(date, 0L);
                                    }
                                    Log.i(TAG, dataByDayMap.toString());
                                    Log.i(TAG, countByDayMap.toString());

                                    for (DailyDailyReview dailyEnergy : listListPair.first) {
                                        // find matching record
                                        total += dailyEnergy.getAverageDailyReview();
                                        String formattedDate = formattedTime.convertStringYYYYMMDDToEEE(dailyEnergy.getDate());
                                        Long oldCount = countByDayMap.get(formattedDate);
                                        countByDayMap.put(formattedDate, oldCount + 1L);
                                        Double oldData = dataByDayMap.get(formattedDate);
                                        dataByDayMap.put(formattedDate, oldData + dailyEnergy.getAverageDailyReview());
                                    }
                                    int i = 0;
                                    float bestValue = 0;
                                    for (Map.Entry<String, Double> entry : dataByDayMap.entrySet()) {
                                        float entryValue = 0;
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
                                    //displayAllInsights(itemType);
                                    Log.i(TAG, "Average is " + average);
                                    final List<SimpleRegressionSummary> simpleRegressionSummaries = new ArrayList<>();
                                    final List<SimpleRegressionSummary> sortedList = new ArrayList<>();
                                    if (listListPair.second.size() > 0) {
                                        simpleRegressionSummaries.addAll(listListPair.second);
                                        Collections.sort(simpleRegressionSummaries, new SimpleRegressionSummary.BestFitComparator());
                                        for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                            if (simpleRegressionSummary.getDepVar().equals(getString(R.string.daily_review_camel_case))) {
                                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                                    if (simpleRegressionSummary.getIndepVar().equals("Total")) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    }
                                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                    }
                                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 1) {
                                                        sortedList.add(simpleRegressionSummary);
                                                    }
                                                } else {
                                                    sortedList.add(simpleRegressionSummary);
                                                }
                                                Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                            }
                                        }
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            averageItemItemAdapter.clear();
                                            bestAndAverageItemItemAdapter.clear();
                                            topThreeItemItemAdapter.clear();
                                            recommendationItemItemAdapter.clear();
                                            InsightMindAverageItem moodAverageItem = new InsightMindAverageItem(getString(R.string.daily_review_camel_case), average);
                                            InsightBestAndWorstItem bestAndWorstItem = new InsightBestAndWorstItem(barEntries, entryDatesList, average, finalValueToPass);
                                            averageItemItemAdapter.add(moodAverageItem);
                                            bestAndAverageItemItemAdapter.add(bestAndWorstItem);
                                            if (sortedList.size() > 0) {
                                                if (getActivity() != null) {
                                                    topThreeItemItemAdapter.add(new InsightTopThreeItem(InsightMindFragment.this, getString(R.string.daily_review_camel_case), sortedList));
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
        } else if (itemType == DEPRESSION) {
            InsightSummaryDepressionMediatorLiveData insightDepressionMediatorLiveData =
                    new InsightSummaryDepressionMediatorLiveData(depressionViewModel.getDailyPhq9s(
                            formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                            formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())),
                            regressionSummaryViewModel.getAllMindSummaries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                                    now, numOfDays));
            insightDepressionMediatorLiveData.observe(getViewLifecycleOwner(), new Observer<Pair<List<DailyPhq9>, List<SimpleRegressionSummary>>>() {
                @Override
                public void onChanged(@Nullable Pair<List<DailyPhq9>, List<SimpleRegressionSummary>> listListPair) {
                    if (listListPair != null && listListPair.first != null && listListPair.second != null) {
                        if (listListPair.first.size() > 0) {
                            Handler handler = new Handler();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    double total = 0;
                                    final List<String> entryDatesList = new ArrayList<>();
                                    final List<BarEntry> barEntries = new ArrayList<>();
                                    Map<String, Double> dataByDayMap = new LinkedHashMap<>();
                                    Map<String, Long> countByDayMap = new LinkedHashMap<>();
                                    String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                    for (int i = 1; i <= numOfDays; i++) {
                                        String date = formattedTime.getDateBeforeSpecifiedNumberOfDaysAsEEE(lastEntryDate, numOfDays - i);
                                        Log.i(TAG, date);
                                        dataByDayMap.put(date, 0d);
                                        countByDayMap.put(date, 0L);
                                    }
                                    Log.i(TAG, dataByDayMap.toString());
                                    Log.i(TAG, countByDayMap.toString());

                                    for (DailyPhq9 dailyEntry : listListPair.first) {
                                        // find matching record
                                        total += dailyEntry.getAverageScore();
                                        String formattedDate = formattedTime.convertStringYYYYMMDDToEEE(dailyEntry.getDate());
                                        Long oldCount = countByDayMap.get(formattedDate);
                                        countByDayMap.put(formattedDate, oldCount + 1L);
                                        Double oldData = dataByDayMap.get(formattedDate);
                                        dataByDayMap.put(formattedDate, oldData + dailyEntry.getAverageScore());
                                    }
                                    int i = 0;
                                    float bestValue = 0;
                                    for (Map.Entry<String, Double> entry : dataByDayMap.entrySet()) {
                                        float entryValue = 0;
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
                                    //displayAllInsights(itemType);
                                    Log.i(TAG, "Average is " + average);
                                    final List<SimpleRegressionSummary> simpleRegressionSummaries = new ArrayList<>();
                                    final List<SimpleRegressionSummary> sortedList = new ArrayList<>();
                                    if (listListPair.second.size() > 0) {
                                        simpleRegressionSummaries.addAll(listListPair.second);
                                        Collections.sort(simpleRegressionSummaries, new SimpleRegressionSummary.BestFitComparator());
                                        for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                            if (simpleRegressionSummary.getDepVar().equals(getString(R.string.depression_phq9_camel_case))) {
                                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                                    if (simpleRegressionSummary.getIndepVar().equals("Total")) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    }
                                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                    }
                                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 1) {
                                                        sortedList.add(simpleRegressionSummary);
                                                    }
                                                } else {
                                                    sortedList.add(simpleRegressionSummary);
                                                }
                                                Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                            }
                                        }
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            averageItemItemAdapter.clear();
                                            bestAndAverageItemItemAdapter.clear();
                                            topThreeItemItemAdapter.clear();
                                            recommendationItemItemAdapter.clear();
                                            InsightMindAverageItem moodAverageItem = new InsightMindAverageItem(getString(R.string.depression_phq9_camel_case), average);
                                            InsightBestAndWorstItem bestAndWorstItem = new InsightBestAndWorstItem(barEntries, entryDatesList, average, finalValueToPass);
                                            averageItemItemAdapter.add(moodAverageItem);
                                            bestAndAverageItemItemAdapter.add(bestAndWorstItem);
                                            if (sortedList.size() > 0) {
                                                if (getActivity() != null) {
                                                    topThreeItemItemAdapter.add(new InsightTopThreeItem(InsightMindFragment.this, getString(R.string.depression_phq9_camel_case), sortedList));
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
        } else if (itemType == ANXIETY) {
            InsightSummaryAnxietyMediatorLiveData insightAnxietyMediatorLiveData =
                    new InsightSummaryAnxietyMediatorLiveData(anxietyViewModel.getDailyGad7s(
                            formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                            formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())),
                            regressionSummaryViewModel.getAllMindSummaries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                                    now, numOfDays));
            insightAnxietyMediatorLiveData.observe(getViewLifecycleOwner(), new Observer<Pair<List<DailyGad7>, List<SimpleRegressionSummary>>>() {
                @Override
                public void onChanged(@Nullable Pair<List<DailyGad7>, List<SimpleRegressionSummary>> listListPair) {
                    if (listListPair != null && listListPair.first != null && listListPair.second != null) {
                        if (listListPair.first.size() > 0) {
                            Handler handler = new Handler();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    double total = 0;
                                    final List<String> entryDatesList = new ArrayList<>();
                                    final List<BarEntry> barEntries = new ArrayList<>();
                                    Map<String, Double> dataByDayMap = new LinkedHashMap<>();
                                    Map<String, Long> countByDayMap = new LinkedHashMap<>();
                                    String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                    for (int i = 1; i <= numOfDays; i++) {
                                        String date = formattedTime.getDateBeforeSpecifiedNumberOfDaysAsEEE(lastEntryDate, numOfDays - i);
                                        Log.i(TAG, date);
                                        dataByDayMap.put(date, 0d);
                                        countByDayMap.put(date, 0L);
                                    }
                                    Log.i(TAG, dataByDayMap.toString());
                                    Log.i(TAG, countByDayMap.toString());

                                    for (DailyGad7 dailyEntry : listListPair.first) {
                                        // find matching record
                                        total += dailyEntry.getAverageScore();
                                        String formattedDate = formattedTime.convertStringYYYYMMDDToEEE(dailyEntry.getDate());
                                        Long oldCount = countByDayMap.get(formattedDate);
                                        countByDayMap.put(formattedDate, oldCount + 1L);
                                        Double oldData = dataByDayMap.get(formattedDate);
                                        dataByDayMap.put(formattedDate, oldData + dailyEntry.getAverageScore());
                                    }
                                    int i = 0;
                                    float bestValue = 0;
                                    for (Map.Entry<String, Double> entry : dataByDayMap.entrySet()) {
                                        float entryValue = 0;
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
                                    //displayAllInsights(itemType);
                                    Log.i(TAG, "Average is " + average);
                                    final List<SimpleRegressionSummary> simpleRegressionSummaries = new ArrayList<>();
                                    final List<SimpleRegressionSummary> sortedList = new ArrayList<>();
                                    if (listListPair.second.size() > 0) {
                                        simpleRegressionSummaries.addAll(listListPair.second);
                                        Collections.sort(simpleRegressionSummaries, new SimpleRegressionSummary.BestFitComparator());
                                        for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                            if (simpleRegressionSummary.getDepVar().equals(getString(R.string.anxiety_gad7_camel_case))) {
                                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                                    if (simpleRegressionSummary.getIndepVar().equals("Total")) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                                    }
                                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                                        sortedList.add(simpleRegressionSummary);
                                                    }
                                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 1) {
                                                        sortedList.add(simpleRegressionSummary);
                                                    }
                                                } else {
                                                    sortedList.add(simpleRegressionSummary);
                                                }
                                                Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                            }
                                        }
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            averageItemItemAdapter.clear();
                                            bestAndAverageItemItemAdapter.clear();
                                            topThreeItemItemAdapter.clear();
                                            recommendationItemItemAdapter.clear();
                                            InsightMindAverageItem moodAverageItem = new InsightMindAverageItem(getString(R.string.anxiety_gad7_camel_case), average);
                                            InsightBestAndWorstItem bestAndWorstItem = new InsightBestAndWorstItem(barEntries, entryDatesList, average, finalValueToPass);
                                            averageItemItemAdapter.add(moodAverageItem);
                                            bestAndAverageItemItemAdapter.add(bestAndWorstItem);
                                            if (sortedList.size() > 0) {
                                                if (getActivity() != null) {
                                                    topThreeItemItemAdapter.add(new InsightTopThreeItem(InsightMindFragment.this, getString(R.string.anxiety_gad7_camel_case), sortedList));
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


    private void displayAllInsights(final int itemType, int numOfDays) {
        regressionSummaryViewModel.getAllMindSummaries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                now, numOfDays).observe(getViewLifecycleOwner(), new Observer<List<SimpleRegressionSummary>>() {
            @Override
            public void onChanged(@Nullable List<SimpleRegressionSummary> simpleRegressionSummaries) {
                if (simpleRegressionSummaries != null && simpleRegressionSummaries.size() > 0) {
                    averageItemItemAdapter.clear();
                    bestAndAverageItemItemAdapter.clear();
                    topThreeItemItemAdapter.clear();
                    recommendationItemItemAdapter.clear();
                    Handler handler = new Handler();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Collections.sort(simpleRegressionSummaries, new SimpleRegressionSummary.BestFitComparator());
                            List<SimpleRegressionSummary> simpleRegressionSummaryList = new ArrayList<>();
                            if (itemType == MINDXFITBIT) {
                                for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                    if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.fitbit_camel_case))) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                    //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                }
                            } else if (itemType == MINDXGOOGLEFIT) {
                                for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                    if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.googlefit_camel_case))) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                    //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                }
                            } else if (itemType == MINDXAPPUSAGE) {
                                for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                    if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                        if (simpleRegressionSummary.getRecommendedActivityLevel() > 300000)
                                            simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                    //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                }
                            } else if (itemType == MINDXMOABI) {
                                for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                    if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.moabi_tracker_camel_case))) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                    //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                }
                            } else if (itemType == MINDXWEATHER) {
                                for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                    if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.weather_camel_case))) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                    //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                }
                            } else if (itemType == MINDXTIMER) {
                                for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                    if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                        if (simpleRegressionSummary.getRecommendedActivityLevel() > 300000)
                                            simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                    //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                }
                            } else if (itemType == MINDXACTIVITY) {
                                for (SimpleRegressionSummary simpleRegressionSummary : simpleRegressionSummaries) {
                                    if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                        if (simpleRegressionSummary.getRecommendedActivityLevel() >= 1)
                                            simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                    //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                                }
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (simpleRegressionSummaryList.size() == 0) {
                                        if (getActivity() != null) {
                                            InsightRecommendationItem insightRecommendationItem = new InsightRecommendationItem(null, InsightMindFragment.this);
                                            recommendationItemItemAdapter.add(insightRecommendationItem);
                                        }
                                    } else {
                                        for (int i = 0; i < simpleRegressionSummaryList.size(); i++) {
                                            if (getActivity() != null) {
                                                InsightRecommendationItem insightRecommendationItem = new InsightRecommendationItem(simpleRegressionSummaryList.get(i), InsightMindFragment.this);
                                                Log.i(TAG, simpleRegressionSummaryList.get(i).getDepXIndepVars()
                                                        + ": " + simpleRegressionSummaryList.get(i).getCoefOfDetermination()
                                                        + " - " + simpleRegressionSummaryList.get(i).getRecommendedActivityLevel());
                                                recommendationItemItemAdapter.add(insightRecommendationItem);
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }).start();
                } else {
                    averageItemItemAdapter.clear();
                    bestAndAverageItemItemAdapter.clear();
                    topThreeItemItemAdapter.clear();
                    recommendationItemItemAdapter.clear();
                    if (getActivity() != null) {
                        InsightRecommendationItem insightRecommendationItem = new InsightRecommendationItem(null, InsightMindFragment.this);
                        recommendationItemItemAdapter.add(insightRecommendationItem);
                    }
                }
            }
        });
    }

    private void calculateMoodAndGoogleFitRegression() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                moodAndEnergyViewModel.getDailyMoods(startOfMonth, now).observe(getViewLifecycleOwner(), new Observer<List<DailyMood>>() {
                    @Override
                    public void onChanged(@Nullable List<DailyMood> dailyMoods) {
                        if (dailyMoods != null) {
                            if (dailyMoods.size() > 0) {
                                googleFitViewModel.getAll(startOfMonth, now).observe(getViewLifecycleOwner(), new Observer<List<GoogleFitSummary>>() {
                                    @Override
                                    public void onChanged(@Nullable List<GoogleFitSummary> googleFitSummaries) {
                                        if (googleFitSummaries != null) {
                                            if (googleFitSummaries.size() > 0) {
                                                List<Double> moods = new ArrayList<>();
                                                List<Double> steps = new ArrayList<>();
                                                List<Double> activeMins = new ArrayList<>();
                                                List<Double> calories = new ArrayList<>();
                                                List<Double> sedentaryMins = new ArrayList<>();
                                                List<Double> distances = new ArrayList<>();
                                                List<String> dates = new ArrayList<>();
                                                double averageMood = 0;
                                                double averageSteps = 0;
                                                double averageCalories = 0;
                                                double averageSedentaryMins = 0;
                                                double averageDistances = 0;
                                                double averageActiveMins = 0;
                                                int validEntryCounter = 0;
                                                for (DailyMood mood : dailyMoods) {
                                                    for (GoogleFitSummary googleFitSummary : googleFitSummaries) {
                                                        if (mood.getDate().equals(googleFitSummary.getDate())) {
                                                            averageMood += mood.getAverageMood();
                                                            moods.add(mood.getAverageMood());
                                                            validEntryCounter++;
                                                            dates.add(mood.getDate());
                                                            List<GoogleFitSummary.Summary> summaries = googleFitSummary.getSummaries();
                                                            for (GoogleFitSummary.Summary summary : summaries) {
                                                                if (summary.getName().equals(getString(R.string.activity_steps_camel_case))) {
                                                                    steps.add(summary.getValue());
                                                                    averageSteps += summary.getValue();
                                                                } else if (summary.getName().equals(getString(R.string.activity_active_minutes_camel_case))) {
                                                                    activeMins.add(summary.getValue());
                                                                    averageActiveMins += summary.getValue();
                                                                } else if (summary.getName().equals(getString(R.string.activity_distance_camel_case))) {
                                                                    distances.add(summary.getValue());
                                                                    averageDistances += summary.getValue();
                                                                } else if (summary.getName().equals(getString(R.string.activity_sedentary_minutes_camel_case))) {
                                                                    sedentaryMins.add(summary.getValue());
                                                                    averageSedentaryMins += summary.getValue();
                                                                } else if (summary.getName().equals(getString(R.string.activity_calories_camel_case))) {
                                                                    calories.add(summary.getValue());
                                                                    averageSedentaryMins += summary.getValue();
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                averageMood = averageMood / validEntryCounter;
                                                averageActiveMins = averageActiveMins / validEntryCounter;
                                                averageCalories = averageCalories / validEntryCounter;
                                                averageDistances = averageDistances / validEntryCounter;
                                                averageSedentaryMins = averageSedentaryMins / validEntryCounter;
                                                averageSteps = averageSteps / validEntryCounter;
                                                Log.i(TAG, dates.toString());
                                                double[] moodsArray = toDoubleArray(moods);
                                                Log.i(TAG, averageMood + ", " + Arrays.toString(moodsArray));
                                                double[] stepsArray = toDoubleArray(steps);
                                                Log.i(TAG, averageSteps + ", " + Arrays.toString(stepsArray));
                                                double[] activeMinsArray = toDoubleArray(activeMins);
                                                double[] distancesArray = toDoubleArray(distances);
                                                double[] sedentaryMinsArray = toDoubleArray(sedentaryMins);
                                                double[] caloriesArray = toDoubleArray(calories);
                                                /*
                                                OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
                                                double[][] regressors = new double[5][];
                                                regressors[0] = stepsArray;
                                                regressors[1] = activeMinsArray;
                                                regressors[2] = caloriesArray;
                                                regressors[3] = distancesArray;
                                                regressors[4] = sedentaryMinsArray;
                                                regression.newSampleData(moodsArray, regressors);
                                                Log.i(TAG, "Beta: " + Arrays.toString(regression.estimateRegressionParameters()));
                                                Log.i(TAG, "rSquared: " + regression.calculateRSquared());
                                                Log.i(TAG, "Parameters Variance: " + Arrays.toString(regression.estimateRegressionParametersVariance()));*/
                                                regressionSummaryViewModel.processData(
                                                        averageSteps,
                                                        depVarType, getString(R.string.mood_camel_case), "Google Fit Steps",
                                                        moodsArray, stepsArray);
                                                regressionSummaryViewModel.processData(
                                                        averageActiveMins,
                                                        depVarType, getString(R.string.mood_camel_case), "Google Fit Active Minutes",
                                                        moodsArray, activeMinsArray);
                                                regressionSummaryViewModel.processData(
                                                        averageCalories,
                                                        depVarType, getString(R.string.mood_camel_case), "Google Fit Calories",
                                                        moodsArray, caloriesArray);
                                                regressionSummaryViewModel.processData(
                                                        averageDistances,
                                                        depVarType, getString(R.string.mood_camel_case), "Google Fit Distances",
                                                        moodsArray, distancesArray);
                                                regressionSummaryViewModel.processData(
                                                        averageSedentaryMins,
                                                        depVarType, getString(R.string.mood_camel_case), "Google Fit Sedentary Minutes",
                                                        moodsArray, sedentaryMinsArray);

                                                /*
                                                List<Double> testX = new ArrayList<>();
                                                testX.add(3d);
                                                testX.add(4d);
                                                testX.add(10d);
                                                testX.add(120d);
                                                testX.add(2000d);
                                                double[] testXArray = toDoubleArray(testX);
                                                List<Double> testY = new ArrayList<>();
                                                testY.add(2d);
                                                testY.add(3d);
                                                testY.add(9d);
                                                testY.add(119d);
                                                testY.add(1999d);
                                                double[] testYArray = toDoubleArray(testY);
                                                regressionSummaryViewModel.processData(18d, 3, "TestX", "TestY", testXArray, testYArray);
                                                List<Double> testX1= new ArrayList<>();
                                                testX1.add(1d);
                                                testX1.add(2d);
                                                testX1.add(3d);
                                                testX1.add(4d);
                                                testX1.add(5d);
                                                double[] testX1Array = toDoubleArray(testX1);
                                                List<Double> testY1 = new ArrayList<>();
                                                testY1.add(3d);
                                                testY1.add(6d);
                                                testY1.add(11d);
                                                testY1.add(18d);
                                                testY1.add(27d);
                                                double[] testY1Array = toDoubleArray(testY1);
                                                regressionSummaryViewModel.processData(12d, 3, "TestX1", "TestY1", testX1Array, testY1Array);*/
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private double[] toDoubleArray(List<Double> list) {
        double[] ret = new double[list.size()];
        int i = 0;
        for (Double e : list)
            ret[i++] = e;
        return ret;
    }
}

