package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.anxiety.DailyGad7;
import com.ivorybridge.moabi.database.entity.anxiety.MonthlyGad7;
import com.ivorybridge.moabi.database.entity.anxiety.WeeklyGad7;
import com.ivorybridge.moabi.database.entity.appusage.AppUsage;
import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;
import com.ivorybridge.moabi.database.entity.dailyreview.MonthlyDailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.WeeklyDailyReview;
import com.ivorybridge.moabi.database.entity.depression.DailyPhq9;
import com.ivorybridge.moabi.database.entity.depression.MonthlyPhq9;
import com.ivorybridge.moabi.database.entity.depression.WeeklyPhq9;
import com.ivorybridge.moabi.database.entity.moodandenergy.DailyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.DailyMood;
import com.ivorybridge.moabi.database.entity.moodandenergy.MonthlyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.MonthlyMood;
import com.ivorybridge.moabi.database.entity.moodandenergy.WeeklyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.WeeklyMood;
import com.ivorybridge.moabi.database.entity.dailyreview.DailyDailyReview;
import com.ivorybridge.moabi.database.entity.stress.DailyStress;
import com.ivorybridge.moabi.database.entity.stress.MonthlyStress;
import com.ivorybridge.moabi.database.entity.stress.WeeklyStress;
import com.ivorybridge.moabi.ui.recyclerviewitem.stats.MeansAndSumItem;
import com.ivorybridge.moabi.ui.util.TimeChartMarkerView;
import com.ivorybridge.moabi.ui.util.NumeralChartMarkerView;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.util.wordcloud.WordCloud;
import com.ivorybridge.moabi.util.wordcloud.WordCloudClick;
import com.ivorybridge.moabi.util.wordcloud.WordCloudEntry;
import com.ivorybridge.moabi.viewmodel.AnxietyViewModel;
import com.ivorybridge.moabi.viewmodel.AppUsageViewModel;
import com.ivorybridge.moabi.viewmodel.BAActivityViewModel;
import com.ivorybridge.moabi.viewmodel.DepressionViewModel;
import com.ivorybridge.moabi.viewmodel.InputHistoryViewModel;
import com.ivorybridge.moabi.viewmodel.MoodAndEnergyViewModel;
import com.ivorybridge.moabi.viewmodel.DailyReviewViewModel;
import com.ivorybridge.moabi.viewmodel.StressViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

public class StatsNonTrackerFragment extends Fragment {

    private static final String TAG = StatsNonTrackerFragment.class.getSimpleName();
    @BindView(R.id.fragment_stats_nontracker_stats_recyclerview)
    RecyclerView statsRecyclerView;
    @BindView(R.id.fragment_stats_nontracker_notabletrends_recyclerview)
    RecyclerView trendsRecyclerView;
    @BindView(R.id.fragment_stats_nontracker_radiogroup)
    SegmentedGroup radioGroup;
    @BindView(R.id.fragment_stats_nontracker_week_button)
    RadioButton weekButton;
    @BindView(R.id.fragment_stats_nontracker_month_button)
    RadioButton monthButton;
    @BindView(R.id.fragment_stats_nontracker_sixmonths_button)
    RadioButton sixMonthsButton;
    @BindView(R.id.fragment_stats_nontracker_year_button)
    RadioButton yearButton;
    @BindView(R.id.fragment_stats_nontracker_linechart)
    LineChart lineChart;
    @BindView(R.id.fragment_stats_nontracker_barchart)
    BarChart barChart;
    @BindView(R.id.fragment_stats_nontracker_wordcloud)
    WordCloud wordCloud;
    private InputHistoryViewModel inputHistoryViewModel;
    private FormattedTime formattedTime;
    private FastAdapter<IItem> statsRecyclerAdapter;
    private ItemAdapter<MeansAndSumItem> meansAndSumItemAdapter;
    private Long startOfWeek;
    private Long endOfWeek;
    private String inputType;
    private Typeface tf;
    private MoodAndEnergyViewModel moodAndEnergyViewModel;
    private AppUsageViewModel appUsageViewModel;
    private BAActivityViewModel activityViewModel;
    private StressViewModel stressViewModel;
    private DailyReviewViewModel dailyReviewViewModel;
    private DepressionViewModel depressionViewModel;
    private AnxietyViewModel anxietyViewModel;

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
        View mView = inflater.inflate(R.layout.fragment_stats_nontracker, container, false);
        ButterKnife.bind(this, mView);
        formattedTime = new FormattedTime();

        String todaysDate = formattedTime.getCurrentDateAsYYYYMMDD();
        startOfWeek = formattedTime.getStartOfWeek(todaysDate);
        endOfWeek = formattedTime.getEndOfDay(todaysDate);
        radioGroup.setTintColor(ContextCompat.getColor(getContext(), R.color.colorPrimary), Color.WHITE);
        radioGroup.setUnCheckedTintColor(Color.WHITE, Color.BLACK);
        inputHistoryViewModel = ViewModelProviders.of(this).get(InputHistoryViewModel.class);
        statsRecyclerAdapter = new FastAdapter<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, false);
        statsRecyclerView.setLayoutManager(layoutManager);
        meansAndSumItemAdapter = new ItemAdapter<>();
        statsRecyclerAdapter = FastAdapter.with(meansAndSumItemAdapter);
        tf = ResourcesCompat.getFont(getContext(), R.font.source_sans_pro);
        moodAndEnergyViewModel = ViewModelProviders.of(this).get(MoodAndEnergyViewModel.class);
        appUsageViewModel = ViewModelProviders.of(this).get(AppUsageViewModel.class);
        activityViewModel = ViewModelProviders.of(this).get(BAActivityViewModel.class);
        stressViewModel = ViewModelProviders.of(this).get(StressViewModel.class);
        dailyReviewViewModel = ViewModelProviders.of(this).get(DailyReviewViewModel.class);
        depressionViewModel = ViewModelProviders.of(this).get(DepressionViewModel.class);
        anxietyViewModel = ViewModelProviders.of(this).get(AnxietyViewModel.class);
        Log.i(TAG, inputType);
        weekButton.setChecked(true);

        if (inputType.equals(getString(R.string.mood_camel_case)) || inputType.equals(getString(R.string.energy_camel_case)) || inputType.equals(getString(R.string.stress_camel_case))
                || inputType.equals(getString(R.string.daily_review_camel_case)) || inputType.equals(getString(R.string.depression_phq9_camel_case))
                || inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
            styleLineChart();
            //setDailyLineChart(7);
        } else if (inputType.equals(getString(R.string.phone_usage_camel_case))) {
            styleBarChart();
            //setDailyBarChart(7);
        } else if (inputType.equals(getString(R.string.baactivity_camel_case))) {
            styleWordCloud();
            //setWordCloud(7);
        }

        if (getContext() != null && getActivity() != null) {
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
                        if (checkedId == R.id.fragment_stats_nontracker_week_button) {
                            Log.i(TAG, getContext().getString(R.string.week));
                            if (inputType.equals(getString(R.string.mood_camel_case)) || inputType.equals(getString(R.string.energy_camel_case)) || inputType.equals(getString(R.string.stress_camel_case))
                                    || inputType.equals(getString(R.string.daily_review_camel_case)) || inputType.equals(getString(R.string.depression_phq9_camel_case))
                                    || inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
                                setDailyLineChart(7);
                            } else if (inputType.equals(getString(R.string.phone_usage_camel_case))) {
                                setDailyBarChart(7);
                            } else if (inputType.equals(getString(R.string.baactivity_camel_case))) {
                                setWordCloud(7);
                            }
                        } else if (checkedId == R.id.fragment_stats_nontracker_month_button) {
                            Log.i(TAG, getContext().getString(R.string.month));
                            if (inputType.equals(getString(R.string.mood_camel_case)) || inputType.equals(getString(R.string.energy_camel_case)) || inputType.equals(getString(R.string.stress_camel_case))
                                    || inputType.equals(getString(R.string.daily_review_camel_case)) || inputType.equals(getString(R.string.depression_phq9_camel_case))
                                    || inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
                                setDailyLineChart(31);
                            } else if (inputType.equals(getString(R.string.phone_usage_camel_case))) {
                                setDailyBarChart(31);
                            } else if (inputType.equals(getString(R.string.baactivity_camel_case))) {
                                setWordCloud(31);
                            }
                        } else if (checkedId == R.id.fragment_stats_nontracker_year_button) {
                            Log.i(TAG, "year");
                            if (inputType.equals(getString(R.string.mood_camel_case)) || inputType.equals(getString(R.string.energy_camel_case)) || inputType.equals(getString(R.string.stress_camel_case))
                                    || inputType.equals(getString(R.string.daily_review_camel_case)) || inputType.equals(getString(R.string.depression_phq9_camel_case))
                                    || inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
                                setMonthlyLineChart(395);
                            } else if (inputType.equals(getString(R.string.phone_usage_camel_case))) {
                                setMonthlyBarChart(395);
                            } else if (inputType.equals(getString(R.string.baactivity_camel_case))) {
                                setWordCloud(395);
                            }
                        } else {
                            Log.i(TAG, "6 months");
                            if (inputType.equals(getString(R.string.mood_camel_case)) || inputType.equals(getString(R.string.energy_camel_case)) || inputType.equals(getString(R.string.stress_camel_case))
                                    || inputType.equals(getString(R.string.daily_review_camel_case)) || inputType.equals(getString(R.string.depression_phq9_camel_case))
                                    || inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
                                setWeeklyLineChart(180);
                            } else if (inputType.equals(getString(R.string.phone_usage_camel_case))) {
                                setWeeklyBarEntries(180);
                            } else if (inputType.equals(getString(R.string.baactivity_camel_case))) {
                                setWordCloud(180);
                            }
                        }
                    }
                }
            });
        }
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null && getActivity() != null) {
            int checkedId = radioGroup.getCheckedRadioButtonId();
            RadioButton checkedRadioButton = (RadioButton) radioGroup.findViewById(checkedId);
            // This puts the value (true/false) into the variable
            boolean isChecked = checkedRadioButton.isChecked();
            // If the radiobutton that has changed in check state is now checked...
            if (isChecked) {
                if (checkedId == R.id.fragment_stats_nontracker_week_button) {
                    Log.i(TAG, getContext().getString(R.string.week));
                    if (inputType.equals(getString(R.string.mood_camel_case)) || inputType.equals(getString(R.string.energy_camel_case)) || inputType.equals(getString(R.string.stress_camel_case))
                            || inputType.equals(getString(R.string.daily_review_camel_case)) || inputType.equals(getString(R.string.depression_phq9_camel_case))
                            || inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
                        setDailyLineChart(7);
                    } else if (inputType.equals(getString(R.string.phone_usage_camel_case))) {
                        setDailyBarChart(7);
                    } else if (inputType.equals(getString(R.string.baactivity_camel_case))) {
                        setWordCloud(7);
                    }
                } else if (checkedId == R.id.fragment_stats_nontracker_month_button) {
                    Log.i(TAG, getContext().getString(R.string.month));
                    if (inputType.equals(getString(R.string.mood_camel_case)) || inputType.equals(getString(R.string.energy_camel_case)) || inputType.equals(getString(R.string.stress_camel_case))
                            || inputType.equals(getString(R.string.daily_review_camel_case)) || inputType.equals(getString(R.string.depression_phq9_camel_case))
                            || inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
                        setDailyLineChart(31);
                    } else if (inputType.equals(getString(R.string.phone_usage_camel_case))) {
                        setDailyBarChart(31);
                    } else if (inputType.equals(getString(R.string.baactivity_camel_case))) {
                        setWordCloud(31);
                    }
                } else if (checkedId == R.id.fragment_stats_nontracker_year_button) {
                    Log.i(TAG, "year");
                    if (inputType.equals(getString(R.string.mood_camel_case)) || inputType.equals(getString(R.string.energy_camel_case)) || inputType.equals(getString(R.string.stress_camel_case))
                            || inputType.equals(getString(R.string.daily_review_camel_case)) || inputType.equals(getString(R.string.depression_phq9_camel_case))
                            || inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
                        setMonthlyLineChart(395);
                    } else if (inputType.equals(getString(R.string.phone_usage_camel_case))) {
                        setMonthlyBarChart(395);
                    } else if (inputType.equals(getString(R.string.baactivity_camel_case))) {
                        setWordCloud(395);
                    }
                } else {
                    Log.i(TAG, "6 months");
                    if (inputType.equals(getString(R.string.mood_camel_case)) || inputType.equals(getString(R.string.energy_camel_case)) || inputType.equals(getString(R.string.stress_camel_case))
                            || inputType.equals(getString(R.string.daily_review_camel_case)) || inputType.equals(getString(R.string.depression_phq9_camel_case))
                            || inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
                        setWeeklyLineChart(180);
                    } else if (inputType.equals(getString(R.string.phone_usage_camel_case))) {
                        setWeeklyBarEntries(180);
                    } else if (inputType.equals(getString(R.string.baactivity_camel_case))) {
                        setWordCloud(180);
                    }
                }
            }
        }
    }

    private void styleLineChart() {
        lineChart.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);
        wordCloud.setVisibility(View.GONE);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setNoDataText(getString(R.string.chart_no_entry));
        lineChart.setNoDataTextTypeface(tf);
        lineChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDarkComplt));
        //lineChart.animateXY(1000, 1000);
        lineChart.setTouchEnabled(true);
        //lineChart.setDragEnabled(false);
        lineChart.setPinchZoom(true);
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.setExtraOffsets(8, 0, 8, 8);
    }

    private void styleBarChart() {
        wordCloud.setVisibility(View.GONE);
        lineChart.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE);
        barChart.setScaleEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getDescription().setEnabled(false);
        barChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDarkComplt));
        barChart.setNoDataText(getString(R.string.chart_no_entry));
        barChart.setNoDataTextTypeface(tf);
        //lineChart.animateXY(1000, 1000);
        barChart.setTouchEnabled(true);
        //lineChart.setDragEnabled(false);
        barChart.setPinchZoom(true);
        barChart.setDoubleTapToZoomEnabled(true);
        barChart.setExtraOffsets(8, 0, 8, 8);
    }

    private void styleWordCloud() {
        lineChart.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        wordCloud.setVisibility(View.VISIBLE);
    }

    private void setWordCloud(int numOfDays) {
        if (inputType.equals(getString(R.string.baactivity_camel_case))) {
            activityViewModel.getActivityEntries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<BAActivityEntry>>() {
                @Override
                public void onChanged(@Nullable List<BAActivityEntry> baActivityEntries) {
                    if (baActivityEntries != null) {

                        final Map<String, Long> nameFrequencyMap = new LinkedHashMap<>();
                        final Map<String, Long> nameTypeMap = new LinkedHashMap<>();
                        final Map<String, Set<String>> nameDatesMap = new LinkedHashMap<>();
                        final List<WordCloudEntry> wordCloudEntryList = new ArrayList<>();
                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (BAActivityEntry baActivityEntry : baActivityEntries) {
                                    if (nameFrequencyMap.get(baActivityEntry.getName()) != null) {
                                        Long old = nameFrequencyMap.get(baActivityEntry.getName());
                                        nameFrequencyMap.put(baActivityEntry.getName(), old + 1);
                                    } else {
                                        nameFrequencyMap.put(baActivityEntry.getName(), 1L);
                                    }
                                    if (nameDatesMap.get(baActivityEntry.getName()) != null) {
                                        Set<String> old = nameDatesMap.get(baActivityEntry.getName());
                                        old.add(formattedTime.convertLongToYYYYMMDD(baActivityEntry.getDateInLong()));
                                        nameDatesMap.put(baActivityEntry.getName(), old);
                                    } else {
                                        Set<String> dates = new LinkedHashSet<>();
                                        dates.add(formattedTime.convertLongToYYYYMMDD(baActivityEntry.getDateInLong()));
                                        nameDatesMap.put(baActivityEntry.getName(), dates);
                                    }
                                    nameTypeMap.put(baActivityEntry.getName(), baActivityEntry.getActivityType());
                                }

                                for (Map.Entry<String, Long> nameFrequency : nameFrequencyMap.entrySet()) {
                                    long type = nameTypeMap.get(nameFrequency.getKey());
                                    WordCloudEntry worldCloudEntry = new WordCloudEntry(nameFrequency.getKey(),
                                            nameFrequency.getValue(),
                                            type);
                                    wordCloudEntryList.add(worldCloudEntry);
                                }
                                Collections.shuffle(wordCloudEntryList);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        displayWordCloud(wordCloudEntryList, nameDatesMap);
                                        Log.i(TAG, baActivityEntries.toString());
                                    }
                                });
                            }
                        }).start();
                    }
                }
            });
        }
    }

    private void displayWordCloud(List<WordCloudEntry> wordCloudEntryList, Map<String, Set<String>> nameDatesMap) {
        if (wordCloudEntryList.size() != 0) {
            wordCloud.setMinTextSize(70f);
            wordCloud.setTextSize(70f);
            wordCloud.create(wordCloudEntryList);
            wordCloud.setOnWordClickListener(new WordCloudClick() {
                @Override
                public void onWordClick(View widget, int index) {
                    Log.i(TAG, "Index is " + index);
                    Toast.makeText(getContext(), wordCloudEntryList.get(index).getEntryName()
                            + "\n" + nameDatesMap.get(wordCloudEntryList.get(index).getEntryName().toString()), Toast.LENGTH_SHORT).show();
                }
            });
            //wordCloud.setRandomFonts();
        } else {
            wordCloud.setText(getString(R.string.chart_no_entry));
            wordCloud.setTextSize(12f);
            wordCloud.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDarkComplt));
        }
    }

    private void setDailyBarChart(int numOfDays) {
        if (inputType.equals(getString(R.string.phone_usage_camel_case))) {
            appUsageViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<AppUsageSummary>>() {
                @Override
                public void onChanged(@Nullable List<AppUsageSummary> appUsageSummaries) {
                    final List<AppUsageSummary> reverseOrderedList = appUsageSummaries;
                    final List<String> entryDatesList = new ArrayList<>();
                    final List<BarEntry> barEntries = new ArrayList<>();
                    final Map<String, Long> entries = new LinkedHashMap<>();
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

                            // 1. iterate through the list and get the list of activities
                            // 2. extract only the total from the list
                            // 3. add each total to a map.

                            int i = 0;
                            int total = 0;
                            int validEntryCounter = 0;

                            // iterate through the entry dates
                            // iterate through the summary list until we find a match in date
                            // if there is a match, do the thing.
                            // if not, put 0 as value and move on.
                            for (String date : entryDatesList) {
                                for (AppUsageSummary appUsageSummary : reverseOrderedList) {
                                    // find matching record
                                    if (appUsageSummary.getDate().equals(date)) {
                                        List<AppUsage> appUsageList = appUsageSummary.getActivities();
                                        for (AppUsage appUsage : appUsageList) {
                                            if (appUsage.getAppName().equals("Total")) {
                                                entries.put(appUsageSummary.getDate(), appUsage.getTotalTime());
                                                barEntries.add(new BarEntry(i, appUsage.getTotalTime()));
                                                total += appUsage.getTotalTime();
                                                validEntryCounter++;
                                                break;
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
                                final int average = total / validEntryCounter;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        drawBarChart(barEntries, entryDatesList, numOfDays, average);
                                    }
                                });
                            } else {
                                final int average = 0;
                            }
                            Log.i(TAG, entries.toString());
                        }
                    }).start();
                }
            });
        }
    }

    private void setWeeklyBarEntries(int numOfDays) {
        if (inputType.equals(getString(R.string.phone_usage_camel_case))) {
            appUsageViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<AppUsageSummary>>() {
                @Override
                public void onChanged(@Nullable List<AppUsageSummary> appUsageSummaries) {

                    if (appUsageSummaries.size() == 0) {
                        barChart.isEmpty();
                        barChart.clear();
                        barChart.invalidate();
                        return;
                    }

                    final List<AppUsageSummary> reverseOrderedList = appUsageSummaries;
                    final List<String> entryDatesList = new ArrayList<>();
                    final List<BarEntry> barEntries = new ArrayList<>();
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

                            // 1. iterate through the list and get the list of activities
                            // 2. extract only the total from the list
                            // 3. add each total to a map.

                            int i = 0;
                            int total = 0;
                            int validEntryCounter = 0;

                            for (String YYYW : entryDatesList) {
                                long weeklyTotal = 0;
                                for (AppUsageSummary appUsageSummary : reverseOrderedList) {
                                    if (YYYW.equals(formattedTime.convertLongToYYYYW(appUsageSummary.getDateInLong()))) {
                                        List<AppUsage> appUsages = appUsageSummary.getActivities();
                                        for (AppUsage appUsage : appUsages) {
                                            if (appUsage.getAppName().equals("Total")) {
                                                weeklyTotal += appUsage.getTotalTime();
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


                            Log.i(TAG, entryDatesList.toString());

                            if (validEntryCounter != 0) {
                                final int average = total / validEntryCounter;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        drawBarChart(barEntries, entryDatesList, numOfDays, average);
                                    }
                                });
                            }
                            Log.i(TAG, barEntries.toString());
                        }
                    }).start();

                }
            });
        }
    }

    private void setMonthlyBarChart(int numOfDays) {
        if (inputType.equals(getString(R.string.phone_usage_camel_case))) {
            appUsageViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<AppUsageSummary>>() {
                @Override
                public void onChanged(@Nullable List<AppUsageSummary> appUsageSummaries) {
                    List<AppUsageSummary> reverseOrderedList = appUsageSummaries;
                    List<String> entryDatesList = new ArrayList<>();
                    List<BarEntry> barEntries = new ArrayList<>();
                    Map<String, Long> entries = new LinkedHashMap<>();
                    Collections.reverse(reverseOrderedList);

                    int numOfMonths = numOfDays / 30;

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
                            for (int i = 1; i <= numOfMonths; i++) {
                                entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i));
                                entries.put(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i), 0L);
                            }

                            // 1. iterate through the list and get the list of activities
                            // 2. extract only the total from the list
                            // 3. add each total to a map.

                            Log.i(TAG, entryDatesList.toString());

                            int i = 0;
                            int total = 0;
                            int validEntryCounter = 0;

                            for (String YYYYMM : entryDatesList) {
                                long monthlyTotal = 0;
                                for (AppUsageSummary appUsageSummary : reverseOrderedList) {
                                    if (YYYYMM.equals(formattedTime.convertLongToYYYYMM(appUsageSummary.getDateInLong()))) {
                                        List<AppUsage> appUsages = appUsageSummary.getActivities();
                                        for (AppUsage appUsage : appUsages) {
                                            if (appUsage.getAppName().equals("Total")) {
                                                monthlyTotal += appUsage.getTotalTime();
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
                                final int average = total / validEntryCounter;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        drawBarChart(barEntries, entryDatesList, numOfDays, average);
                                    }
                                });
                            }
                            Log.i(TAG, entries.toString());
                        }
                    }).start();
                }
            });
        }
    }

    private void drawBarChart(List<BarEntry> barEntries, List<String> entryDatesList, int numOfDays, int average) {
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
            xAxis.setAxisLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
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
        leftAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return convertToTimeString(value);
            }
        });

        rightAxis.setAxisMinimum(0);
        rightAxis.setEnabled(false);

        leftAxis.removeAllLimitLines();
        LimitLine averageLL = new LimitLine(average, getString(R.string.chart_avg_abbr));
        averageLL.setLineWidth(2f);
        averageLL.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        averageLL.setTypeface(tf);
        averageLL.setTextStyle(Paint.Style.FILL);
        averageLL.enableDashedLine(4, 8, 1);
        averageLL.setTextColor(Color.DKGRAY);
        averageLL.setTextSize(12);
        //averageLL.setLineColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        averageLL.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
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

        //set.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

        //set.setLineWidth(1f);
        //Log.i(TAG, set.toString());
        BarData barData = new BarData(set);
        barData.setDrawValues(false);
        //barData.setValueTextSize(12f);

        int height = barChart.getHeight();
        Paint paint = barChart.getRenderer().getPaintRender();
        int[] colors = new int[]{ContextCompat.getColor(getContext(), R.color.colorPrimaryDark), ContextCompat.getColor(getContext(), R.color.colorPrimary),
                ContextCompat.getColor(getContext(), R.color.white)};
        float[] positions = new float[]{0, 0.5f, 1};
        /*
        LinearGradient linearGradient = new LinearGradient(0, 0, 0, height,
                ContextCompat.getColor(getContext(), R.color.colorPrimary), ContextCompat.getColor(getContext(), R.color.white),
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

        barData.setBarWidth(0.5f);

        //barChart.animateY(1000, Easing.EasingOption.Linear);
        barChart.setFitBars(true);
        barChart.setData(barData);
        TimeChartMarkerView chartMarkerView = new TimeChartMarkerView(getContext(), R.layout.mpchart_chartvalueselectedview, entryDatesList, numOfDays, barChart);
        barChart.setMarker(chartMarkerView);
        barChart.getLegend().setEnabled(false);
        barChart.invalidate();
    }


    private void setDailyLineChart(int numOfDays) {
        if (inputType.equals(getString(R.string.mood_camel_case))) {
            Log.i(TAG, "Processing daily mood");
            moodAndEnergyViewModel.getDailyMoods(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<DailyMood>>() {
                @Override
                public void onChanged(@Nullable List<DailyMood> dailyEntries) {
                    if (dailyEntries != null) {
                        lineChart.setVisibility(View.VISIBLE);
                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();

                        if (dailyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfDays; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i), -200f);
                                }
                                for (int j = 0; j < dailyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (dailyEntries.get(j).getDate().equals(date)) {
                                            entries.put(date, dailyEntries.get(j).getAverageMood().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }

                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, dailyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.energy_camel_case))) {
            moodAndEnergyViewModel.getDailyEnergies(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<DailyEnergy>>() {
                @Override
                public void onChanged(@Nullable List<DailyEnergy> dailyEntries) {
                    if (dailyEntries != null) {

                        if (dailyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        lineChart.setVisibility(View.VISIBLE);
                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();
                        String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 1; i <= numOfDays; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i), -200f);
                                }
                                for (int j = 0; j < dailyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (dailyEntries.get(j).getDate().equals(date)) {
                                            entries.put(date, dailyEntries.get(j).getAverageEnergy().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, dailyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.stress_camel_case))) {
            stressViewModel.getDailyStress(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<DailyStress>>() {
                @Override
                public void onChanged(@Nullable List<DailyStress> dailyEntries) {
                    if (dailyEntries != null) {

                        if (dailyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        lineChart.setVisibility(View.VISIBLE);
                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();
                        String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 1; i <= numOfDays; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i), -200f);
                                }
                                for (int j = 0; j < dailyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (dailyEntries.get(j).getDate().equals(date)) {
                                            entries.put(date, dailyEntries.get(j).getAverageStress().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, dailyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.daily_review_camel_case))) {
            dailyReviewViewModel.getDailyDailyReviews(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<DailyDailyReview>>() {
                @Override
                public void onChanged(@Nullable List<DailyDailyReview> dailyEntries) {
                    if (dailyEntries != null) {

                        if (dailyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        lineChart.setVisibility(View.VISIBLE);
                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();
                        String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 1; i <= numOfDays; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i), -200f);
                                }
                                for (int j = 0; j < dailyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (dailyEntries.get(j).getDate().equals(date)) {
                                            entries.put(date, dailyEntries.get(j).getAverageDailyReview().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, dailyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.depression_phq9_camel_case))) {
            depressionViewModel.getDailyPhq9s(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<DailyPhq9>>() {
                @Override
                public void onChanged(@Nullable List<DailyPhq9> dailyEntries) {
                    if (dailyEntries != null) {

                        if (dailyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        lineChart.setVisibility(View.VISIBLE);
                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();
                        String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 1; i <= numOfDays; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i), -200f);
                                }
                                for (int j = 0; j < dailyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (dailyEntries.get(j).getDate().equals(date)) {
                                            entries.put(date, dailyEntries.get(j).getAverageScore().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, dailyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
            anxietyViewModel.getDailyGad7s(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<DailyGad7>>() {
                @Override
                public void onChanged(@Nullable List<DailyGad7> dailyEntries) {
                    if (dailyEntries != null) {

                        if (dailyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        lineChart.setVisibility(View.VISIBLE);
                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();
                        String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 1; i <= numOfDays; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfDaysAsYYYYMMDD(lastEntryDate, numOfDays - i), -200f);
                                }
                                for (int j = 0; j < dailyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (dailyEntries.get(j).getDate().equals(date)) {
                                            entries.put(date, dailyEntries.get(j).getAverageScore().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, dailyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                    }
                }
            });
        }
    }

    private void setWeeklyLineChart(int numOfDays) {
        if (inputType.equals(getString(R.string.mood_camel_case))) {
            moodAndEnergyViewModel.getWeeklyMoods(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<WeeklyMood>>() {
                @Override
                public void onChanged(@Nullable List<WeeklyMood> weeklyEntries) {
                    if (weeklyEntries != null) {

                        if (weeklyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();
                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfWeeks = numOfDays / 7;


                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfWeeks; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i), -200f);
                                }
                                for (int j = 0; j < weeklyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (weeklyEntries.get(j).getYYYYW().equals(date)) {
                                            entries.put(date, weeklyEntries.get(j).getAverageMood().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, weeklyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                        Log.i(TAG, weeklyEntries.toString());
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.energy_camel_case))) {
            moodAndEnergyViewModel.getWeeklyEnergies(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<WeeklyEnergy>>() {
                @Override
                public void onChanged(@Nullable List<WeeklyEnergy> weeklyEntries) {
                    if (weeklyEntries != null) {

                        if (weeklyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfWeeks = numOfDays / 7;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfWeeks; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i), -200f);
                                }
                                for (int j = 0; j < weeklyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (weeklyEntries.get(j).getYYYYW().equals(date)) {
                                            entries.put(date, weeklyEntries.get(j).getAverageEnergy().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, weeklyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();


                    }
                }
            });
        } else if (inputType.equals(getString(R.string.stress_camel_case))) {
            stressViewModel.getWeeklyStress(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<WeeklyStress>>() {
                @Override
                public void onChanged(@Nullable List<WeeklyStress> weeklyEntries) {
                    if (weeklyEntries != null) {

                        if (weeklyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfWeeks = numOfDays / 7;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfWeeks; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i), -200f);
                                }
                                for (int j = 0; j < weeklyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (weeklyEntries.get(j).getYYYYW().equals(date)) {
                                            entries.put(date, weeklyEntries.get(j).getAverageStress().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, weeklyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.daily_review_camel_case))) {
            dailyReviewViewModel.getWeeklyDailyReviews(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<WeeklyDailyReview>>() {
                @Override
                public void onChanged(@Nullable List<WeeklyDailyReview> weeklyEntries) {
                    if (weeklyEntries != null) {

                        if (weeklyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfWeeks = numOfDays / 7;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfWeeks; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i), -200f);
                                }
                                for (int j = 0; j < weeklyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (weeklyEntries.get(j).getYYYYW().equals(date)) {
                                            entries.put(date, weeklyEntries.get(j).getAverageDailyReview().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, weeklyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.depression_phq9_camel_case))) {
            depressionViewModel.getWeeklyPhq9s(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<WeeklyPhq9>>() {
                @Override
                public void onChanged(@Nullable List<WeeklyPhq9> weeklyEntries) {
                    if (weeklyEntries != null) {

                        if (weeklyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfWeeks = numOfDays / 7;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfWeeks; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i), -200f);
                                }
                                for (int j = 0; j < weeklyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (weeklyEntries.get(j).getYYYYW().equals(date)) {
                                            entries.put(date, weeklyEntries.get(j).getAverageScore().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, weeklyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
            anxietyViewModel.getWeeklyGad7s(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<WeeklyGad7>>() {
                @Override
                public void onChanged(@Nullable List<WeeklyGad7> weeklyEntries) {
                    if (weeklyEntries != null) {

                        if (weeklyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfWeeks = numOfDays / 7;

                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfWeeks; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfWeeksAsYYYYW(lastEntryDate, numOfWeeks - i), -200f);
                                }
                                for (int j = 0; j < weeklyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (weeklyEntries.get(j).getYYYYW().equals(date)) {
                                            entries.put(date, weeklyEntries.get(j).getAverageScore().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, weeklyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                    }
                }
            });
        }
    }

    private void setMonthlyLineChart(int numOfDays) {
        if (inputType.equals(getString(R.string.mood_camel_case))) {
            moodAndEnergyViewModel.getMonthlyMoods(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<MonthlyMood>>() {
                @Override
                public void onChanged(@Nullable List<MonthlyMood> monthlyEntries) {
                    if (monthlyEntries != null) {

                        if (monthlyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();
                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfMonths = numOfDays / 30;
                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfMonths; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i), -200f);
                                }
                                for (int j = 0; j < monthlyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (monthlyEntries.get(j).getYYYYMM().equals(date)) {
                                            entries.put(date, monthlyEntries.get(j).getAverageMood().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }

                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, monthlyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                        Log.i(TAG, monthlyEntries.toString());
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.mood_camel_case))) {
            moodAndEnergyViewModel.getMonthlyEnergies(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<MonthlyEnergy>>() {
                @Override
                public void onChanged(@Nullable List<MonthlyEnergy> monthlyEntries) {
                    if (monthlyEntries != null) {

                        if (monthlyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();
                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfMonths = numOfDays / 30;
                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfMonths; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i), -200f);
                                }
                                for (int j = 0; j < monthlyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (monthlyEntries.get(j).getYYYYMM().equals(date)) {
                                            entries.put(date, monthlyEntries.get(j).getAverageEnergy().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }

                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, monthlyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());
                            }
                        }).start();
                        Log.i(TAG, monthlyEntries.toString());
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.stress_camel_case))) {
            stressViewModel.getMonthlyStress(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<MonthlyStress>>() {
                @Override
                public void onChanged(@Nullable List<MonthlyStress> monthlyEntries) {
                    if (monthlyEntries != null) {

                        if (monthlyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfMonths = numOfDays / 30;
                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfMonths; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i), -200f);
                                }
                                for (int j = 0; j < monthlyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (monthlyEntries.get(j).getYYYYMM().equals(date)) {
                                            entries.put(date, monthlyEntries.get(j).getAverageStress().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, monthlyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());

                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.daily_review_camel_case))) {
            dailyReviewViewModel.getMonthlyDailyReviews(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<MonthlyDailyReview>>() {
                @Override
                public void onChanged(@Nullable List<MonthlyDailyReview> monthlyEntries) {
                    if (monthlyEntries != null) {

                        if (monthlyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfMonths = numOfDays / 30;
                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfMonths; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i), -200f);
                                }
                                for (int j = 0; j < monthlyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (monthlyEntries.get(j).getYYYYMM().equals(date)) {
                                            entries.put(date, monthlyEntries.get(j).getAverageDailyReview().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, monthlyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());

                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.depression_phq9_camel_case))) {
            depressionViewModel.getMonthlyPhq9s(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<MonthlyPhq9>>() {
                @Override
                public void onChanged(@Nullable List<MonthlyPhq9> monthlyEntries) {
                    if (monthlyEntries != null) {

                        if (monthlyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfMonths = numOfDays / 30;
                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfMonths; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i), -200f);
                                }
                                for (int j = 0; j < monthlyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (monthlyEntries.get(j).getYYYYMM().equals(date)) {
                                            entries.put(date, monthlyEntries.get(j).getAverageScore().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, monthlyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());

                            }
                        }).start();
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
            anxietyViewModel.getMonthlyGad7s(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays),
                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(this, new Observer<List<MonthlyGad7>>() {
                @Override
                public void onChanged(@Nullable List<MonthlyGad7> monthlyEntries) {
                    if (monthlyEntries != null) {

                        if (monthlyEntries.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                            return;
                        }

                        List<String> entryDatesList = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        Map<String, Float> entries = new LinkedHashMap<>();

                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int numOfMonths = numOfDays / 30;
                                String lastEntryDate = formattedTime.getCurrentDateAsYYYYMMDD();
                                for (int i = 1; i <= numOfMonths; i++) {
                                    entryDatesList.add(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i));
                                    entries.put(formattedTime.getDateBeforeSpecifiedNumberOfMonthsAsYYYYMM(lastEntryDate, numOfMonths - i), -200f);
                                }
                                for (int j = 0; j < monthlyEntries.size(); j++) {
                                    for (String date : entryDatesList) {
                                        if (monthlyEntries.get(j).getYYYYMM().equals(date)) {
                                            entries.put(date, monthlyEntries.get(j).getAverageScore().floatValue());
                                        }
                                    }
                                }
                                float totalMood = 0;
                                float validEntryCounter = 0;
                                //int j = numOfDays - 1;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        totalMood += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }
                                if (validEntryCounter != 0) {
                                    //average = totalMood / lineEntries.size();
                                    final float average = totalMood / validEntryCounter;

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(lineEntries, entryDatesList, numOfDays, average);
                                        }
                                    });
                                }
                                Collections.sort(lineEntries, new EntryXComparator());
                                Log.i(TAG, monthlyEntries.toString());
                                Log.i(TAG, entryDatesList.toString());
                                Log.i(TAG, entries.toString());
                                Log.i(TAG, lineEntries.toString());

                            }
                        }).start();
                    }
                }
            });
        }
    }

    private void drawLineChart(List<Entry> lineEntries, List<String> entryDatesList, int numOfDays, float average) {

        lineChart.clear();

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
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setTypeface(tf);
            xAxis.setDrawGridLines(false);
            //xAxis.setAxisMinimum(0f);
            lineChart.setDragEnabled(false);
            //xAxis.setLabelCount(7, true);
            xAxis.setDrawAxisLine(true);
            //xAxis.setXOffset(0.5f);
            xAxis.setSpaceMin(0.5f);
            xAxis.setSpaceMax(0.5f);
            xAxis.setTextSize(12);
            xAxis.setTextColor(Color.DKGRAY);
            if (getContext() != null) {
                xAxis.setAxisLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            }
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
                        Log.i(TAG, "X-axis - " + value + ", " + (int) value);
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
                        Log.i(TAG, value + ", " + (int) value);
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
                                Log.i(TAG, date + " at " + index);
                            }
                            return date;
                        } else {
                            return "";
                        }
                    }
                });
            }
        }

        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();

        if (inputType.equals(getString(R.string.mood_camel_case)) || inputType.equals(getString(R.string.energy_camel_case))) {
            // set up y-axis
            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setAxisMaximum(3.5f);
            leftAxis.setAxisMinimum(1f);
            leftAxis.setTypeface(tf);
            leftAxis.setTextSize(12);
            leftAxis.setGranularity(0.5f);
            leftAxis.setTextColor(Color.DKGRAY);
            leftAxis.setGranularityEnabled(true);
            //leftAxis.setLabelCount(3, true);
            leftAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    //Log.i(TAG, "Y-axis value is: " + value);
                    if (value == 1f) {
                        return getString(R.string.chart_label_poor);
                    } else if (value == 3f) {
                        return getString(R.string.chart_label_good);
                    } else {
                        return "";
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.stress_camel_case))) {
            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setAxisMaximum(10f);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setTypeface(tf);
            leftAxis.setTextSize(12);
            leftAxis.setGranularity(5f);
            leftAxis.setTextColor(Color.DKGRAY);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    //Log.i(TAG, "Y-axis value is: " + value);
                    if (value == 10f) {
                        return getString(R.string.chart_label_max);
                    } else if (value == 0f) {
                        return getString(R.string.chart_label_none);
                    } else {
                        return "";
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.daily_review_camel_case))) {
            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setAxisMaximum(5f);
            leftAxis.setAxisMinimum(1f);
            leftAxis.setTypeface(tf);
            leftAxis.setTextSize(12);
            leftAxis.setGranularity(1f);
            leftAxis.setTextColor(Color.DKGRAY);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    //Log.i(TAG, "Y-axis value is: " + value);
                    if (value == 5f) {
                        return getString(R.string.chart_label_excellent);
                    } else if (value == 1f) {
                        return getString(R.string.chart_label_terrible);
                    } else {
                        return "";
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.depression_phq9_camel_case))) {
            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setAxisMaximum(28f);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setTypeface(tf);
            leftAxis.setTextSize(12);
            leftAxis.setGranularity(1.0f);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setTextColor(Color.DKGRAY);
            leftAxis.setLabelCount(29, true);
            leftAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    Log.i(TAG, "Y-axis value is: " + value);
                    /*
                    if (value == 4f) {
                        return "Min";
                    } */
                    if (value == 9f) {
                        return getString(R.string.chart_mild_abbr);
                    } else if ((int) value == 14f) {
                        return getString(R.string.chart_moderate_abbr);
                    } /*else if (value == 19f) {
                        return "M.Sv";
                    } */ else if (value == 27f) {
                        return getString(R.string.chart_severe_abbr);
                    } else {
                        return "";
                    }
                }
            });
        } else if (inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setAxisMaximum(21f);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setTypeface(tf);
            leftAxis.setTextSize(12);
            leftAxis.setGranularity(1.0f);
            leftAxis.setTextColor(Color.DKGRAY);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setLabelCount(22, true);
            leftAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    Log.i(TAG, "Y-axis value is: " + value);
                    /*if (value == 4f) {
                        return "Min";
                    }*/
                    if (value == 9f) {
                        return getString(R.string.chart_mild_abbr);
                    } else if (value == 14f) {
                        return getString(R.string.chart_moderate_abbr);
                    } else if (value == 21f) {
                        return getString(R.string.chart_severe_abbr);
                    } else {
                        return "";
                    }
                }
            });
        }

        leftAxis.removeAllLimitLines();
        LimitLine averageLL = new LimitLine(average, getString(R.string.chart_avg_abbr));
        averageLL.setLineWidth(1f);
        averageLL.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        averageLL.setTypeface(tf);
        averageLL.setTextStyle(Paint.Style.FILL);
        averageLL.enableDashedLine(4, 8, 1);
        averageLL.setTextColor(Color.DKGRAY);
        averageLL.setTextSize(12);
        //averageLL.setLineColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        averageLL.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
        leftAxis.addLimitLine(averageLL);
        if (inputType.equals(getString(R.string.mood_camel_case)) || inputType.equals(getString(R.string.energy_camel_case))) {
            LimitLine poor = new LimitLine(1f, "");
            poor.setLineWidth(0.5f);
            poor.setTypeface(tf);
            poor.enableDashedLine(4, 8, 1);
            poor.disableDashedLine();
            //poor.setTextColor(Color.BLACK);
            poor.setTextSize(12);
            //poor.setLineColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            LimitLine good = new LimitLine(3f, "");
            good.setLineWidth(0.5f);
            good.setTypeface(tf);
            good.enableDashedLine(4, 8, 1);
            //good.setTextColor(Color.BLACK);
            good.setTextSize(12);
            //good.disableDashedLine();
            good.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            //good.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            //leftAxis.addLimitLine(poor);
            //leftAxis.addLimitLine(good);
        } else if (inputType.equals(getString(R.string.stress_camel_case))) {
            LimitLine max = new LimitLine(10f, "");
            max.setLineWidth(0.5f);
            max.setTypeface(tf);
            max.enableDashedLine(4, 8, 1);
            //max.disableDashedLine();
            max.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            max.setTextSize(12);
            //leftAxis.addLimitLine(max);
        } else if (inputType.equals(getString(R.string.daily_review_camel_case))) {
            LimitLine max = new LimitLine(5f, "");
            max.setLineWidth(0.5f);
            max.setTypeface(tf);
            max.enableDashedLine(4, 8, 1);
            max.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            //max.disableDashedLine();
            max.setTextSize(12);
            //leftAxis.addLimitLine(max);
        } else if (inputType.equals(getString(R.string.depression_phq9_camel_case))) {
            LimitLine minimal = new LimitLine(4f, "");
            minimal.setLineWidth(1f);
            minimal.setTypeface(tf);
            minimal.enableDashedLine(4, 8, 1);
            minimal.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            //minimal.disableDashedLine();
            minimal.setTextSize(12);
            LimitLine mild = new LimitLine(9f, "");
            mild.setLineWidth(0.5f);
            mild.setTypeface(tf);
            mild.enableDashedLine(4, 8, 1);
            mild.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            //mild.disableDashedLine();
            mild.setTextSize(12);
            LimitLine moderate = new LimitLine(14f, "");
            moderate.setLineWidth(0.5f);
            moderate.setTypeface(tf);
            moderate.enableDashedLine(4, 8, 1);
            moderate.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            //moderate.disableDashedLine();
            moderate.setTextSize(12);
            LimitLine modSevere = new LimitLine(19f, "");
            modSevere.setLineWidth(1f);
            modSevere.setTypeface(tf);
            modSevere.enableDashedLine(4, 8, 1);
            modSevere.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            //modSevere.disableDashedLine();
            modSevere.setTextSize(12);
            LimitLine severe = new LimitLine(27f, "");
            severe.setLineWidth(0.5f);
            severe.setTypeface(tf);
            severe.enableDashedLine(4, 8, 1);
            severe.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            //severe.disableDashedLine();
            severe.setTextSize(12);
            //leftAxis.addLimitLine(minimal);
            //leftAxis.addLimitLine(mild);
            //leftAxis.addLimitLine(moderate);
            //leftAxis.addLimitLine(modSevere);
            //leftAxis.addLimitLine(severe);
        } else if (inputType.equals(getString(R.string.anxiety_gad7_camel_case))) {
            LimitLine minimal = new LimitLine(4f, "");
            minimal.setLineWidth(1f);
            minimal.setTypeface(tf);
            minimal.enableDashedLine(4, 8, 1);
            minimal.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            //minimal.disableDashedLine();
            minimal.setTextSize(12);
            LimitLine mild = new LimitLine(9f, "");
            mild.setLineWidth(0.5f);
            mild.setTypeface(tf);
            mild.enableDashedLine(4, 8, 1);
            mild.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            //mild.disableDashedLine();
            mild.setTextSize(12);
            LimitLine moderate = new LimitLine(14f, "");
            moderate.setLineWidth(0.5f);
            moderate.setTypeface(tf);
            moderate.enableDashedLine(4, 8, 1);
            moderate.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            //moderate.disableDashedLine();
            moderate.setTextSize(12);
            LimitLine severe = new LimitLine(21f, "");
            severe.setLineWidth(0.5f);
            severe.setTypeface(tf);
            severe.enableDashedLine(4, 8, 1);
            severe.setLineColor(ContextCompat.getColor(getContext(), R.color.transparent_gray));
            //severe.disableDashedLine();
            severe.setTextSize(12);
            //leftAxis.addLimitLine(minimal);
            //leftAxis.addLimitLine(mild);
            //leftAxis.addLimitLine(moderate);
            //leftAxis.addLimitLine(severe);
            //leftAxis.addLimitLine(max);
        }
        //leftAxis.setDrawLimitLinesBehindData(true);
        //leftAxis.addLimitLine(goodLL);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.removeAllLimitLines();
        //rightAxis.setDrawLimitLinesBehindData(true);
        rightAxis.setEnabled(false);

        LineDataSet set = new LineDataSet(lineEntries, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        set.setLineWidth(1f);
        set.setDrawValues(false);
        set.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        set.setCircleHoleColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        set.setCircleHoleRadius(2f);
        set.setCircleRadius(2f);
        set.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.bg_white_to_primary);
        set.setFillDrawable(drawable);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawVerticalHighlightIndicator(false);
        if (inputType.equals(getString(R.string.mood_camel_case))) {
            set.setLabel(getString(R.string.mood_camel_case));
        } else if (inputType.equals(getString(R.string.energy_camel_case))) {
            set.setLabel(getString(R.string.energy_camel_case));
        }
        List<ILineDataSet> lineDataSets = new ArrayList<>();
        lineDataSets.add(set);
        LineData lineData = new LineData(lineDataSets);
        NumeralChartMarkerView numeralChartMarker = new NumeralChartMarkerView(getContext(),
                R.layout.mpchart_chartvalueselectedview, entryDatesList, numOfDays, lineChart, inputType);
        lineChart.setMarker(numeralChartMarker);
        lineChart.setData(lineData);
        lineChart.getLegend().setEnabled(false);
        lineChart.invalidate();
    }

    @Override
    public void onStart() {
        super.onStart();

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
}
