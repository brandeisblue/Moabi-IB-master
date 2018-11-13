package com.ivorybridge.moabi.ui.recyclerviewitem.appusage;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.appusage.AppUsage;
import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.ui.util.TimeChartMarkerView;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.AppUsageViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.android.segmented.SegmentedGroup;

public class AppUsageItem extends AbstractItem<AppUsageItem, AppUsageItem.ViewHolder> {

    private static final String TAG = AppUsageItem.class.getSimpleName();
    private String mDate;
    private Fragment mFragment;

    public AppUsageItem(Fragment fragment, String date) {
        mDate = date;
        this.mFragment = fragment;
    }

    @NonNull
    @Override
    public AppUsageItem.ViewHolder getViewHolder(View v) {
        return new AppUsageItem.ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.appusage_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_appusage;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<AppUsageItem> {

        @BindView(R.id.rv_item_appusage_barchart)
        BarChart barChart;
        @BindView(R.id.rv_item_appusage_radiogroup)
        SegmentedGroup radioGroup;
        @BindView(R.id.rv_item_appusage_today_button)
        RadioButton todayButton;
        @BindView(R.id.rv_item_appusage_this_week_button)
        RadioButton thisWeekButton;
        @BindView(R.id.rv_item_appusage_this_month_button)
        RadioButton thisMonthButton;
        private SharedPreferences appUsageSharedPreferences;
        private SharedPreferences.Editor appUsageSPEditor;
        private AppUsageViewModel appUsageViewModel;
        IMarker marker;
        private FormattedTime formattedTime;
        private Typeface tf;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final AppUsageItem item, List<Object> payloads) {
            Log.i(TAG, "AppUsageItem is created");
            appUsageSharedPreferences = itemView.getContext().getSharedPreferences(itemView.getContext()
                            .getString(R.string.com_ivorybridge_moabi_APP_USAGE_SHARED_PREFERENCE_KEY),
                    Context.MODE_PRIVATE);
            appUsageSPEditor = appUsageSharedPreferences.edit();
            formattedTime = new FormattedTime();
            tf = ResourcesCompat.getFont(itemView.getContext(), R.font.source_sans_pro);
            if (item.mFragment != null) {
                appUsageViewModel = ViewModelProviders.of(item.mFragment).get(AppUsageViewModel.class);
            }
            radioGroup.setTintColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary), Color.WHITE);
            radioGroup.setUnCheckedTintColor(Color.WHITE, Color.BLACK);
            String checked = appUsageSharedPreferences.getString(itemView.getContext()
                    .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY), itemView.getContext().getString(R.string.today));
            if (checked.equals(itemView.getContext().getString(R.string.today))) {
                todayButton.setChecked(true);
            } else if (checked.equals(itemView.getContext().getString(R.string.this_week))) {
                thisWeekButton.setChecked(true);
            } else {
                thisMonthButton.setChecked(true);
            }
            configureBarChart();
            configureData(item);

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
                        if (checkedId == R.id.rv_item_appusage_today_button) {
                            appUsageSPEditor.putString(itemView.getContext()
                                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                                    itemView.getContext().getString(R.string.today));
                            appUsageSPEditor.commit();
                            configureData(item);
                        } else if (checkedId == R.id.rv_item_appusage_this_week_button) {
                            appUsageSPEditor.putString(itemView.getContext()
                                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                                    itemView.getContext().getString(R.string.this_week));
                            appUsageSPEditor.commit();
                            configureData(item);
                        } else {
                            appUsageSPEditor.putString(itemView.getContext()
                                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                                    itemView.getContext().getString(R.string.this_month));
                            appUsageSPEditor.commit();
                            configureData(item);
                        }
                    }
                }
            });
        }

        private void configureData(AppUsageItem item) {

            String defaultTimeRange = appUsageSharedPreferences.getString(itemView.getContext()
                    .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY), itemView.getContext().getString(R.string.today));

            if (defaultTimeRange.equals(itemView.getContext().getString(R.string.today))) {
                //dayButton.setChecked(true);
                //dayButton.setPressed(true);
                //weekButton.setPressed(false);
                //monthButton.setPressed(false);
                appUsageViewModel.get(item.mDate).observe(item.mFragment, new Observer<AppUsageSummary>() {
                    @Override
                    public void onChanged(@Nullable AppUsageSummary appUsageSummary) {
                        if (appUsageSummary != null) {
                            configureChartForOneDay(appUsageSummary);
                            Log.i(TAG, "App usage data is updated");

                        }
                    }
                });
            } else if (defaultTimeRange.equals(itemView.getContext().getString(R.string.this_week))) {
                appUsageViewModel.getAll(formattedTime.getStartOfWeek(item.mDate), formattedTime.getEndOfDay(item.mDate)).observe(item.mFragment, new Observer<List<AppUsageSummary>>() {
                    @Override
                    public void onChanged(@Nullable List<AppUsageSummary> appUsageSummaries) {
                        if (appUsageSummaries != null) {
                            configureChartForExtendedTimeRange(appUsageSummaries, 7);
                        }
                    }
                });
            } else if (defaultTimeRange.equals(itemView.getContext().getString(R.string.this_month))) {
                appUsageViewModel.getAll(formattedTime.getStartOfWeek(item.mDate), formattedTime.getEndOfDay(item.mDate)).observe(item.mFragment, new Observer<List<AppUsageSummary>>() {
                    @Override
                    public void onChanged(@Nullable List<AppUsageSummary> appUsageSummaries) {
                        if (appUsageSummaries != null) {
                            configureChartForExtendedTimeRange(appUsageSummaries, 31);
                        }
                    }
                });
            }
        }

        private void configureChartForOneDay(AppUsageSummary appUsageSummary) {

            Map<String, Long> AppUsageNameRankMap = new LinkedHashMap<>();
            Map<Long, Long> AppUsageRankTimeMap = new LinkedHashMap<>();
            List<BarEntry> AppUsageBarEntries = new ArrayList<>();

            Log.i(TAG, appUsageSummary.getActivities().toString());
            List<AppUsage> activities = appUsageSummary.getActivities();

            if (activities.size() == 0) {
                barChart.isEmpty();
                barChart.clear();
                barChart.invalidate();
                return;
            }

            for (AppUsage activity : activities) {
                AppUsageNameRankMap.put(activity.getAppName(), activity.getUsageRank());
                AppUsageRankTimeMap.put(activity.getUsageRank(), activity.getTotalTime());
            }

            int i = 0;
            for (Map.Entry<Long, Long> entry : AppUsageRankTimeMap.entrySet()) {
                if (entry.getValue() != null && entry.getValue() != 0L) {
                    AppUsageBarEntries.add(new BarEntry(entry.getKey(), entry.getValue()));
                    i++;
                }
            }

            // set up x-axis
            XAxis xAxis = barChart.getXAxis();
            xAxis.setTextColor(Color.DKGRAY);
            xAxis.setDrawGridLines(false);
            xAxis.setAxisMaximum(6.5f);
            xAxis.setAxisMinimum(0.5f);
            xAxis.setTextSize(10f);
            xAxis.setTypeface(tf);
            //xAxis.setLabelCount(5, true);
            xAxis.setDrawAxisLine(true);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelRotationAngle(0f);
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    for (Map.Entry<String, Long> appSummary : AppUsageNameRankMap.entrySet()) {
                        //Log.i(TAG, "Rank: " + appSummary.getValue() + " Axis Value: " + value);
                        if (appSummary.getValue() == 1f * value) {
                            if (appSummary.getKey().length() > 5) {
                                return appSummary.getKey().substring(0, 5) + "...";
                            } else {
                                return appSummary.getKey();
                            }
                        }
                    }
                    return "";
                }
            });


            marker = new TimeChartMarkerView(itemView.getContext(), R.layout.mpchart_chartvalueselectedview, AppUsageNameRankMap);
            barChart.setMarker(marker);
            Collections.sort(AppUsageBarEntries, new EntryXComparator());
            BarDataSet AppUsageDataSet = new BarDataSet(AppUsageBarEntries, "Digital BAActivityFavorited");
            AppUsageDataSet.setDrawValues(false);
            AppUsageDataSet.setColor(itemView.getContext().getColor(R.color.colorPrimary));
            //AppUsageDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            AppUsageDataSet.setValueFormatter(new IValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    return convertToTimeString(value);
                }
            });
            BarData barData = new BarData(AppUsageDataSet);
            barData.setValueTextSize(12f);
            barData.setBarWidth(0.4f);
            barChart.setFitBars(false);
            //barChart.animateY(1000, Easing.EasingOption.Linear);
            barChart.setData(barData);
            barChart.invalidate();
        }

        private void configureChartForExtendedTimeRange(List<AppUsageSummary> appUsageSummaries, int numOfDays) {

            Map<String, Long> appUsageTotalSummaryMap = new LinkedHashMap<>();
            Map<Long, String> appUsageSortedMap = new TreeMap<>(Collections.reverseOrder());
            Map<String, Long> AppUsageNameRankMap = new LinkedHashMap<>();
            Map<Long, Long> AppUsageRankTimeMap = new LinkedHashMap<>();
            List<BarEntry> AppUsageBarEntries = new ArrayList<>();
            List<AppUsageSummary> reverseOrderedList = appUsageSummaries;
            Collections.reverse(reverseOrderedList);

            if (reverseOrderedList.size() == 0) {
                barChart.isEmpty();
                barChart.clear();
                barChart.invalidate();
                return;
            }

            int i = 0;
            for (AppUsageSummary appUsageSummary : reverseOrderedList) {
                if (i < numOfDays) {
                    List<AppUsage> activities = appUsageSummary.getActivities();
                    Log.i(TAG, appUsageSummary.getDate());
                    for (AppUsage activity : activities) {
                        if (appUsageTotalSummaryMap.get(activity.getAppName()) != null) {
                            String appName = activity.getAppName();
                            long usageTime = appUsageTotalSummaryMap.get(appName);
                            usageTime += activity.getTotalTime();
                            appUsageTotalSummaryMap.put(appName, usageTime);
                        } else {
                            appUsageTotalSummaryMap.put(activity.getAppName(), activity.getTotalTime());
                        }
                    }
                } else {
                    break;
                }
                i++;
            }

            for (Map.Entry<String, Long> entry : appUsageTotalSummaryMap.entrySet()) {
                appUsageSortedMap.put(entry.getValue(), entry.getKey());
            }
            Log.i(TAG, appUsageTotalSummaryMap.toString());
            Log.i(TAG, appUsageSortedMap.toString());

            long k = 1;
            for (Map.Entry<Long, String> entry : appUsageSortedMap.entrySet()) {
                AppUsageNameRankMap.put(entry.getValue(), k);
                AppUsageRankTimeMap.put(k, entry.getKey());
                k++;
            }


            i = 0;
            for (Map.Entry<Long, Long> entry : AppUsageRankTimeMap.entrySet()) {
                if (entry.getValue() != null && entry.getValue() != 0L) {
                    AppUsageBarEntries.add(new BarEntry(entry.getKey(), entry.getValue()));
                    i++;
                }
            }

            marker = new TimeChartMarkerView(itemView.getContext(), R.layout.mpchart_chartvalueselectedview, AppUsageNameRankMap);
            barChart.setMarker(marker);

            // set up x-axis
            XAxis xAxis = barChart.getXAxis();
            xAxis.setDrawGridLines(false);
            xAxis.setTextColor(Color.DKGRAY);
            xAxis.setTypeface(tf);
            xAxis.setAxisMaximum(6.5f);
            xAxis.setAxisMinimum(0.5f);
            xAxis.setTextSize(10f);
            //xAxis.setLabelCount(5, true);
            xAxis.setDrawAxisLine(true);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelRotationAngle(0f);
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    for (Map.Entry<String, Long> appSummary : AppUsageNameRankMap.entrySet()) {
                        //Log.i(TAG, "Rank: " + appSummary.getValue() + " Axis Value: " + value);
                        if (appSummary.getValue() == 1f * value) {
                            if (appSummary.getKey().length() > 5) {
                                return appSummary.getKey().substring(0, 5) + "...";
                            } else {
                                return appSummary.getKey();
                            }
                        }
                    }
                    return "";
                }
            });


            //Log.i(TAG, AppUsageNameRankMap.toString());
            //Log.i(TAG, AppUsageRankTimeMap.toString());
            Log.i(TAG, "Number of entries: " + AppUsageBarEntries.size());
            Collections.sort(AppUsageBarEntries, new EntryXComparator());
            BarDataSet AppUsageDataSet = new BarDataSet(AppUsageBarEntries, "Digital BAActivityFavorited");
            AppUsageDataSet.setDrawValues(false);
            AppUsageDataSet.setColor(itemView.getContext().getColor(R.color.colorPrimary));
            BarData barData = new BarData(AppUsageDataSet);
            barData.setValueTextSize(12f);
            barData.setBarWidth(0.4f);
            barChart.setFitBars(false);
            barChart.setData(barData);
            barChart.invalidate();

        }


        private void configureBarChart() {
            // set up the bar chart
            barChart.setScaleEnabled(true);
            barChart.setDrawGridBackground(false);
            barChart.getDescription().setEnabled(false);
            barChart.setNoDataText(itemView.getContext().getString(R.string.chart_no_entry));
            barChart.setTouchEnabled(true);
            barChart.setPinchZoom(false);
            barChart.setDoubleTapToZoomEnabled(false);
            barChart.setScaleEnabled(false);
            barChart.setDragEnabled(false);
            barChart.setAutoScaleMinMaxEnabled(true);
            barChart.setDrawBarShadow(false);
            barChart.setDrawValueAboveBar(false);
            //barChart.setMaxVisibleValueCount(7);
            barChart.getLegend().setEnabled(false);

            // set up y-axis
            YAxis leftAxis = barChart.getAxisLeft();
            YAxis rightAxis = barChart.getAxisRight();
            leftAxis.setTypeface(tf);
            leftAxis.setTextColor(Color.DKGRAY);
            leftAxis.setEnabled(true);
            leftAxis.setDrawGridLines(false);
            //leftAxis.setAxisMaximum(3);
            leftAxis.setAxisMinimum(0);
            leftAxis.setTextSize(10f);
            //leftAxis.setGranularity(1f);
            //leftAxis.setGranularityEnabled(true);
            leftAxis.setLabelCount(3, true);
            leftAxis.setDrawAxisLine(true);
            leftAxis.setSpaceMax(40f);
            //leftAxis.setSpaceMin(10f);
            leftAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return convertToTimeString(value);
                }
            });


            rightAxis.setAxisMinimum(0);
            rightAxis.setEnabled(false);
            rightAxis.setDrawGridLines(true);
            rightAxis.setLabelCount(3, true);
            rightAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return convertToTimeString(value);
                }
            });
            rightAxis.setDrawLabels(false);
            rightAxis.setDrawAxisLine(false);
            rightAxis.setAxisMinimum(0);
            rightAxis.setSpaceMax(40f);
        }

        @Override
        public void unbindView(AppUsageItem item) {
        }

        private String convertToTimeString(float timeInFloat) {
            long timeInMiliSecs = (long) timeInFloat;
            long minute = Math.round((timeInMiliSecs / (1000 * 60)) % 60);
            long hour = Math.round((timeInMiliSecs / (1000 * 60 * 60)));
            if (hour < 1) {
                return minute + itemView.getContext().getString(R.string.unit_time_sing);
            } else {
                return hour + itemView.getContext().getString(R.string.unit_hour_sing);
            }
        }
    }
}
