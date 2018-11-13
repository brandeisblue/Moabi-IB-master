package com.ivorybridge.moabi.ui.recyclerviewitem.timedactivity;

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
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.ui.util.TimeChartMarkerView;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.TimedActivityViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

public class TimedActivityItem extends AbstractItem<TimedActivityItem, TimedActivityItem.ViewHolder> {

    private static final String TAG = TimedActivityItem.class.getSimpleName();
    private String mDate;
    private Fragment mFragment;

    public TimedActivityItem(Fragment fragment, String date) {
        mDate = date;
        this.mFragment = fragment;
    }

    @NonNull
    @Override
    public TimedActivityItem.ViewHolder getViewHolder(View v) {
        return new TimedActivityItem.ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.timedactivity_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_timedactivity;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<TimedActivityItem> {

        @BindView(R.id.rv_item_timedactivity_barchart)
        BarChart barChart;
        @BindView(R.id.rv_item_timedactivity_radiogroup)
        SegmentedGroup radioGroup;
        @BindView(R.id.rv_item_timedactivity_today_button)
        RadioButton todayButton;
        @BindView(R.id.rv_item_timedactivity_this_week_button)
        RadioButton thisWeekButton;
        @BindView(R.id.rv_item_timedactivity_this_month_button)
        RadioButton thisMonthButton;
        private SharedPreferences timedActivitySharedPreferences;
        private SharedPreferences.Editor timedActivitySPEditor;
        private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;
        private TimedActivityViewModel timedActivityViewModel;
        IMarker marker;
        private FormattedTime formattedTime;
        private Typeface tf;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final TimedActivityItem item, List<Object> payloads) {
            Log.i(TAG, "TimedActivityItem is created");
            //lastSyncTimeTextView.setText(date);*/
            timedActivitySharedPreferences = itemView.getContext().getSharedPreferences(itemView.getContext()
                            .getString(R.string.com_ivorybridge_moabi_TIMED_ACTIVITY_SHARED_PREFERENCE_KEY),
                    Context.MODE_PRIVATE);
            timedActivitySPEditor = timedActivitySharedPreferences.edit();
            formattedTime = new FormattedTime();
            tf = ResourcesCompat.getFont(itemView.getContext(), R.font.source_sans_pro);
            if (item.mFragment != null) {
                timedActivityViewModel = ViewModelProviders.of(item.mFragment).get(TimedActivityViewModel.class);
            }
            radioGroup.setTintColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary), Color.WHITE);
            radioGroup.setUnCheckedTintColor(Color.WHITE, Color.BLACK);
            String checked = timedActivitySharedPreferences.getString(itemView.getContext()
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
                        if (checkedId == R.id.rv_item_timedactivity_today_button) {
                            timedActivitySPEditor.putString(itemView.getContext()
                                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                                    itemView.getContext().getString(R.string.today));
                            timedActivitySPEditor.commit();
                            configureData(item);
                        } else if (checkedId == R.id.rv_item_timedactivity_this_week_button) {
                            timedActivitySPEditor.putString(itemView.getContext()
                                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                                    itemView.getContext().getString(R.string.this_week));
                            timedActivitySPEditor.commit();
                            configureData(item);
                        } else {
                            timedActivitySPEditor.putString(itemView.getContext()
                                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                                    itemView.getContext().getString(R.string.this_month));
                            timedActivitySPEditor.commit();
                            configureData(item);
                        }
                    }
                }
            });
        }

        private void configureData(TimedActivityItem item) {

            String defaultTimeRange = timedActivitySharedPreferences.getString(itemView.getContext()
                    .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY), itemView.getContext().getString(R.string.today));

            if (defaultTimeRange.equals(itemView.getContext().getString(R.string.today))) {
                configureChartForOneDay(item);
            } else if (defaultTimeRange.equals(itemView.getContext().getString(R.string.this_week))) {
                configureChartForExtendedTimeRange(item, 7);
            } else if (defaultTimeRange.equals(itemView.getContext().getString(R.string.this_month))) {
                configureChartForExtendedTimeRange(item, 31);
            }
        }

        private void configureChartForOneDay(TimedActivityItem item) {

            timedActivityViewModel.getAll(item.mDate).observe(item.mFragment, new Observer<List<TimedActivitySummary>>() {
                @Override
                public void onChanged(@Nullable List<TimedActivitySummary> timedActivities) {
                    if (timedActivities != null) {

                        if (timedActivities.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        Map<String, Long> timedActivityNameTimeMap = new LinkedHashMap<>();
                        Map<String, Long> timedActivityNameRankMap = new LinkedHashMap<>();
                        Map<Long, Long> timedActivityRankTimeMap = new LinkedHashMap<>();
                        List<BarEntry> timedActivityBarEntries = new ArrayList<>();

                        List<Long> durationList = new ArrayList<>();
                        Log.i(TAG, timedActivities.toString());

                        for (TimedActivitySummary activity : timedActivities) {
                            if (timedActivityNameTimeMap.get(activity.getInputName()) != null) {
                                Long duration = timedActivityNameTimeMap.get(activity.getInputName());
                                timedActivityNameTimeMap.put(activity.getInputName(), duration 
                                        + activity.getDuration());
                            } else {
                                timedActivityNameTimeMap.put(activity.getInputName(), activity.getDuration());
                            }
                            //Log.i(TAG, activity.getInputName());
                        }
                        for (Map.Entry<String, Long> entry : timedActivityNameTimeMap.entrySet()) {
                            if (entry.getValue() != null && entry.getValue() != 0L) {
                                durationList.add(entry.getValue());
                                //Log.i(TAG, entry.getKey() + ": " + entry.getValue());
                            }
                        }
                        Collections.sort(durationList, Collections.reverseOrder());
                        for (Map.Entry<String, Long> entry : timedActivityNameTimeMap.entrySet()) {
                            if (entry.getValue() != null && entry.getValue() != 0L) {
                                for (int i = 0; i < durationList.size(); i++) {
                                    if (durationList.get(i).equals(entry.getValue())) {
                                        timedActivityRankTimeMap.put((long) i, durationList.get(i));
                                        timedActivityNameRankMap.put(entry.getKey(), (long) i);
                                    }
                                }
                            }
                        }

                        int i = 0;
                        for (Map.Entry<Long, Long> entry : timedActivityRankTimeMap.entrySet()) {
                            if (entry.getValue() != null && entry.getValue() != 0L) {
                                timedActivityBarEntries.add(new BarEntry(entry.getKey(), entry.getValue()));
                                i++;
                            }
                        }
                        Log.i(TAG, timedActivityBarEntries.toString());
                        Log.i(TAG, timedActivityNameRankMap.toString());
                        Log.i(TAG, timedActivityRankTimeMap.toString());

                        // set up x-axis
                        XAxis xAxis = barChart.getXAxis();
                        xAxis.setDrawGridLines(false);
                        xAxis.setAxisMaximum(6.5f);
                        xAxis.setAxisMinimum(-0.5f);
                        xAxis.setTextSize(10f);
                        xAxis.setTypeface(tf);
                        //xAxis.setLabelCount(6, true);
                        xAxis.setDrawAxisLine(true);
                        xAxis.setGranularity(1f);
                        xAxis.setGranularityEnabled(true);
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setLabelRotationAngle(0f);
                        xAxis.setValueFormatter(new IAxisValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                for (Map.Entry<String, Long> appSummary : timedActivityNameRankMap.entrySet()) {
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


                        marker = new TimeChartMarkerView(itemView.getContext(), R.layout.mpchart_chartvalueselectedview, timedActivityNameRankMap);
                        barChart.setMarker(marker);
                        Collections.sort(timedActivityBarEntries, new EntryXComparator());
                        BarDataSet timedActivityDataSet = new BarDataSet(timedActivityBarEntries, "TimedActivities");
                        timedActivityDataSet.setDrawValues(false);
                        timedActivityDataSet.setColor(itemView.getContext().getColor(R.color.colorPrimary));
                        //timedActivityDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                        timedActivityDataSet.setValueFormatter(new IValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                                return convertToTimeString(value);
                            }
                        });
                        BarData barData = new BarData(timedActivityDataSet);
                        barData.setValueTextSize(12f);
                        barData.setBarWidth(0.4f);
                        xAxis.setSpaceMin(barData.getBarWidth() / 2f);
                        xAxis.setSpaceMax(barData.getBarWidth() / 2f);
                        //barChart.setFitBars(true);
                        //barChart.animateY(1000, Easing.EasingOption.Linear);
                        barChart.setData(barData);
                        barChart.invalidate();
                    } else {
                        if (timedActivities.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }
                    }
                }
            }); 
        }
        
        private void configureChartForExtendedTimeRange(TimedActivityItem item, int numOfDays) {

            timedActivityViewModel.getAll(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(item.mDate, numOfDays),
                    formattedTime.getEndOfDay(item.mDate)).observe(item.mFragment, new Observer<List<TimedActivitySummary>>() {
                @Override
                public void onChanged(@Nullable List<TimedActivitySummary> timedActivities) {
                    if (timedActivities != null) {
                        if (timedActivities.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        Map<String, Long> timedActivityNameTimeMap = new LinkedHashMap<>();
                        Map<String, Long> timedActivityNameRankMap = new LinkedHashMap<>();
                        Map<Long, Long> timedActivityRankTimeMap = new LinkedHashMap<>();
                        List<BarEntry> timedActivityBarEntries = new ArrayList<>();

                        List<Long> durationList = new ArrayList<>();
                        Log.i(TAG, timedActivities.toString());

                        if (timedActivities.size() == 0) {
                            barChart.isEmpty();
                            barChart.clear();
                            barChart.invalidate();
                            return;
                        }

                        for (TimedActivitySummary activity : timedActivities) {
                            if (timedActivityNameTimeMap.get(activity.getInputName()) != null) {
                                Long duration = timedActivityNameTimeMap.get(activity.getInputName());
                                timedActivityNameTimeMap.put(activity.getInputName(), duration + activity.getDuration());
                            } else {
                                timedActivityNameTimeMap.put(activity.getInputName(), activity.getDuration());
                            }
                            //Log.i(TAG, activity.getInputName());
                        }

                        for (Map.Entry<String, Long> entry : timedActivityNameTimeMap.entrySet()) {
                            if (entry.getValue() != null && entry.getValue() != 0L) {
                                durationList.add(entry.getValue());
                                //Log.i(TAG, entry.getKey() + ": " + entry.getValue());
                            }
                        }

                        Collections.sort(durationList, Collections.reverseOrder());

                        for (Map.Entry<String, Long> entry : timedActivityNameTimeMap.entrySet()) {
                            if (entry.getValue() != null && entry.getValue() != 0L) {
                                for (int i = 0; i < durationList.size(); i++) {
                                    if (durationList.get(i).equals(entry.getValue())) {
                                        timedActivityRankTimeMap.put((long) i, durationList.get(i));
                                        timedActivityNameRankMap.put(entry.getKey(), (long) i);
                                    }
                                }
                            }
                        }

                        int i = 0;
                        for (Map.Entry<Long, Long> entry : timedActivityRankTimeMap.entrySet()) {
                            if (entry.getValue() != null && entry.getValue() != 0L) {
                                timedActivityBarEntries.add(new BarEntry(entry.getKey(), entry.getValue()));
                                i++;
                            }
                        }
                        Log.i(TAG, timedActivityBarEntries.toString());
                        Log.i(TAG, timedActivityNameRankMap.toString());
                        Log.i(TAG, timedActivityRankTimeMap.toString());

                        // set up x-axis
                        XAxis xAxis = barChart.getXAxis();
                        xAxis.setDrawGridLines(false);
                        xAxis.setAxisMaximum(6.5f);
                        xAxis.setAxisMinimum(-0.5f);
                        xAxis.setTextSize(10f);
                        xAxis.setTypeface(tf);
                        xAxis.setTextColor(Color.DKGRAY);
                        //xAxis.setLabelCount(6, true);
                        xAxis.setDrawAxisLine(true);
                        xAxis.setGranularity(1f);
                        xAxis.setGranularityEnabled(true);
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setLabelRotationAngle(0f);
                        xAxis.setValueFormatter(new IAxisValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                for (Map.Entry<String, Long> appSummary : timedActivityNameRankMap.entrySet()) {
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


                        marker = new TimeChartMarkerView(itemView.getContext(), R.layout.mpchart_chartvalueselectedview, timedActivityNameRankMap);
                        barChart.setMarker(marker);

                        //Log.i(TAG, timedActivityNameRankMap.toString());
                        //Log.i(TAG, timedActivityRankTimeMap.toString());
                        //Log.i(TAG, "Number of entries: " + timedActivityBarEntries.size());
                        Collections.sort(timedActivityBarEntries, new EntryXComparator());
                        BarDataSet timedActivityDataSet = new BarDataSet(timedActivityBarEntries, "Digital BAActivityFavorited");
                        timedActivityDataSet.setDrawValues(false);
                        timedActivityDataSet.setColor(itemView.getContext().getColor(R.color.colorPrimary));
                        //timedActivityDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                        timedActivityDataSet.setValueFormatter(new IValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                                return convertToTimeString(value);
                            }
                        });
                        BarData barData = new BarData(timedActivityDataSet);
                        barData.setValueTextSize(12f);
                        barData.setBarWidth(0.4f);
                        xAxis.setSpaceMin(barData.getBarWidth() / 2f);
                        xAxis.setSpaceMax(barData.getBarWidth() / 2f);
                        //barChart.setFitBars(true);
                        //barChart.animateY(1000, Easing.EasingOption.Linear);
                        barChart.setData(barData);
                        barChart.invalidate();
                    }
                }
            });
        }

        private void configureBarChart() {
            // set up the bar chart
            barChart.setScaleEnabled(true);
            //barChart.setNoDataTextTypeface();
            //Typeface typeface = itemView.getResources().getFont(R.font.roboto);
            barChart.setDrawGridBackground(false);
            //Description description = new Description();
            //description.setText(getTodaysDate());
            //lineChart.setDescription(description);
            barChart.getDescription().setEnabled(false);
            barChart.setNoDataText(itemView.getContext().getString(R.string.chart_no_entry));
            //barChart.animateXY(3000, 3000);
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
            //rightAxis.setSpaceMin(20f);
        }

        @Override
        public void unbindView(TimedActivityItem item) {

        }

        private String convertToTimeString(float timeInFloat) {
            long timeInMiliSecs = (long) timeInFloat;
            long minute = Math.round((timeInMiliSecs / (1000 * 60)) % 60);
            long hour = Math.round((timeInMiliSecs / (1000 * 60 * 60)));
            //Log.i(TAG, timeInMiliSecs + " converted to " + hour);
            if (hour < 1) {
                return minute + itemView.getContext().getString(R.string.unit_time_sing);
            } else {
                return hour + itemView.getContext().getString(R.string.unit_hour_sing);
            }
        }
    }
}
