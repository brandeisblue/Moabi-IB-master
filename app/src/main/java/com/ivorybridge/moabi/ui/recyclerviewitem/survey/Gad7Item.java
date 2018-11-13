package com.ivorybridge.moabi.ui.recyclerviewitem.survey;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.anxiety.DailyGad7;
import com.ivorybridge.moabi.database.entity.anxiety.Gad7;
import com.ivorybridge.moabi.ui.util.NumeralChartMarkerView;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.AnxietyViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

public class Gad7Item extends AbstractItem<Gad7Item, Gad7Item.ViewHolder> {

    private static final String TAG = Gad7Item.class.getSimpleName();
    private Fragment mFragment;
    private String mDate;

    public Gad7Item(Fragment fragment, String date) {
        mFragment = fragment;
        mDate = date;
    }

    @NonNull
    @Override
    public Gad7Item.ViewHolder getViewHolder(View v) {
        return new Gad7Item.ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.anxiety_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_mood;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<Gad7Item> {

        @BindView(R.id.rv_item_mood_linechart)
        LineChart lineChart;
        @BindView(R.id.rv_item_mood_description_textview)
        TextView titleTextView;
        @BindView(R.id.rv_item_mood_description_imageview)
        ImageView titleImageView;
        @BindView(R.id.rv_item_mood_radiogroup)
        SegmentedGroup radioGroup;
        @BindView(R.id.rv_item_mood_today_button)
        RadioButton todayButton;
        @BindView(R.id.rv_item_mood_this_week_button)
        RadioButton thisWeekButton;
        @BindView(R.id.rv_item_mood_this_month_button)
        RadioButton thisMonthButton;
        private AnxietyViewModel itemViewModel;
        private SharedPreferences itemSharedPreferences;
        private SharedPreferences.Editor itemSPEditor;
        private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;
        private FormattedTime formattedTime;
        private Typeface tf;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final Gad7Item item, List<Object> payloads) {
            Log.i(TAG, "Gad7Item is created");
            itemSharedPreferences = itemView.getContext().getSharedPreferences(
                    itemView.getContext().getString(R.string.com_ivorybridge_moabi_GAD7_SHARED_PREFERENCE_KEY)
                    , Context.MODE_PRIVATE);
            itemSPEditor = itemSharedPreferences.edit();
            formattedTime = new FormattedTime();
            if (item.mFragment != null) {
                itemViewModel = ViewModelProviders.of(item.mFragment).get(AnxietyViewModel.class);
            }
            titleTextView.setText(itemView.getContext().getString(R.string.anxiety_gad7_title));
            titleImageView.setImageResource(R.drawable.ic_anxiety_insomnia_black);
            tf = ResourcesCompat.getFont(itemView.getContext(), R.font.source_sans_pro);
            styleLineChart();
            radioGroup.setTintColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary), Color.WHITE);
            radioGroup.setUnCheckedTintColor(Color.WHITE, Color.BLACK);
            String checked = itemSharedPreferences.getString(itemView.getContext()
                    .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY), itemView.getContext().getString(R.string.today));
            if (checked.equals(itemView.getContext().getString(R.string.today))) {
                todayButton.setChecked(true);
                configureData(item, 1);
            } else if (checked.equals(itemView.getContext().getString(R.string.this_week))) {
                thisWeekButton.setChecked(true);
                configureData(item, 7);
            } else {
                thisMonthButton.setChecked(true);
                configureData(item, 31);
            }

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // This will get the radiobutton that has changed in its check state
                    RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
                    // This puts the value (true/false) into the variable
                    boolean isChecked = checkedRadioButton.isChecked();
                    // If the radiobutton that has changed in check state is now checked...
                    if (isChecked) {
                        if (checkedId == R.id.rv_item_mood_today_button) {
                            itemSPEditor.putString(itemView.getContext()
                                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                                    itemView.getContext().getString(R.string.today));
                            itemSPEditor.commit();
                            configureData(item, 1);
                        } else if (checkedId == R.id.rv_item_mood_this_week_button) {
                            itemSPEditor.putString(itemView.getContext()
                                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                                    itemView.getContext().getString(R.string.this_week));
                            itemSPEditor.commit();
                            configureData(item, 7);
                        } else {
                            itemSPEditor.putString(itemView.getContext()
                                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                                    itemView.getContext().getString(R.string.this_week));
                            itemSPEditor.commit();
                            configureData(item, 31);
                        }
                    }
                }
            });

        }

        private void configureData(Gad7Item item, int numOfDay) {
            if (numOfDay == 1) {
                configureChartForOneDay(item);
            } else {
                configureChartForExtendedTimeRange(item, numOfDay);
            }
        }

        @Override
        public void unbindView(Gad7Item item) {
        }

        private void configureChartForOneDay(Gad7Item item) {
            itemViewModel.getEntries(formattedTime.getStartOfDay(item.mDate), formattedTime
                    .getEndOfDay(item.mDate)).observe(item.mFragment, new Observer<List<Gad7>>() {
                @Override
                public void onChanged(List<Gad7> itemList) {
                    if (itemList != null) {
                        if (itemList.size() == 0) {
                            lineChart.isEmpty();
                            lineChart.clear();
                            lineChart.invalidate();
                        } else {
                            Handler handler = new Handler();
                            lineChart.setVisibility(View.VISIBLE);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, itemList.toString());
                                    List<Entry> itemLineEntries = new ArrayList<>();
                                    List<String> entryDatesList = new ArrayList<>();
                                    Long total = 0L;
                                    for (Gad7 item : itemList) {
                                        Long timeOfEntry = item.getDateInLong();
                                        Long entry = item.getScore();
                                        total += entry;
                                        String timeOfEntryInString = formattedTime.convertLongToHHMM(timeOfEntry);
                                        Log.i(TAG, timeOfEntryInString);
                                        String[] times = timeOfEntryInString.split(":");
                                        int hour = Integer.parseInt(times[0].trim());
                                        int minute = Integer.parseInt(times[1].trim());
                                        Float timeOfDayInMinutes = hour * 60f + minute;
                                        itemLineEntries.add(new Entry(timeOfDayInMinutes, entry.floatValue()));
                                    }
                                    for (int i = 0; i < 1441; i++) {
                                        String time = i / 60 + ":" + i % 60;
                                        entryDatesList.add(formattedTime.convertStringHMToHMMAA(time));
                                    }
                                    float average = total.floatValue() / itemList.size();
                                    Log.i(TAG, "Average is " + average);
                                    Collections.sort(itemLineEntries, new EntryXComparator());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            drawLineChart(itemLineEntries, entryDatesList, 1, average);
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                }
            });
        }

        private void configureChartForExtendedTimeRange(Gad7Item item, int numOfDays) {
            itemViewModel.getDailyGad7s(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(
                    formattedTime.getCurrentDateAsYYYYMMDD(), numOfDays - 1),
                    formattedTime.getEndOfDay(
                            formattedTime.getCurrentDateAsYYYYMMDD()))
                    .observe(item.mFragment, new Observer<List<DailyGad7>>() {
                @Override
                public void onChanged(@Nullable List<DailyGad7> dailyEntries) {
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
                                            entries.put(date, dailyEntries.get(j).getAverageScore().floatValue());
                                        }
                                    }
                                }
                                float total = 0;
                                float validEntryCounter = 0;
                                int j = 0;
                                for (Map.Entry<String, Float> entry : entries.entrySet()) {
                                    if (!entry.getValue().equals(-200f)) {
                                        lineEntries.add(new Entry(j, entry.getValue()));
                                        j++;
                                        total += entry.getValue();
                                        validEntryCounter++;
                                    } else {
                                        lineEntries.add(new Entry(j, -200));
                                        j++;
                                    }
                                }

                                if (validEntryCounter != 0) {
                                    final float average = total / validEntryCounter;
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

        private String convertToTimeString(float timeInFloat) {
            int hours = (int) Math.floor(timeInFloat / 60);
            int minutes = (int) Math.floor(timeInFloat % 60);
            if (hours < 0) {
                return "00:00";
            }
            //Log.i(TAG, "Original time value is " + timeInFloat);
            //Log.i(TAG, "Converted time is " + hours + ":" + minutes);
            String timeOfRecord = String.format(Locale.getDefault(), "%02d", hours) + ":" + String.format(Locale.getDefault(), "%02d", minutes);
            return timeOfRecord;
        }

        private void styleLineChart() {
            lineChart.setVisibility(View.VISIBLE);
            lineChart.setScaleEnabled(false);
            lineChart.setDrawGridBackground(false);
            lineChart.getDescription().setEnabled(false);
            lineChart.setNoDataText(itemView.getContext().getString(R.string.chart_no_entry));
            lineChart.setNoDataTextTypeface(tf);
            lineChart.setNoDataTextColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.colorPrimaryDarkComplt));
            //lineChart.animateXY(1000, 1000);
            lineChart.setTouchEnabled(true);
            //lineChart.setDragEnabled(false);
            lineChart.setPinchZoom(true);
            lineChart.setDoubleTapToZoomEnabled(true);
            lineChart.setExtraOffsets(8, 0, 8, 8);
            lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        }

        private void drawLineChart(List<Entry> lineEntries, List<String> entryDatesList, int numOfDays, float average) {

            lineChart.clear();

            List<String> formattedEntryDatesList = new ArrayList<>();
            Log.i(TAG, entryDatesList.toString());
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
            Log.i(TAG, formattedEntryDatesList.toString());

            // set up x-axis
            if (entryDatesList.size() > 0) {
                if (numOfDays == 1) {
                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setTextColor(Color.DKGRAY);
                    xAxis.setTypeface(tf);
                    xAxis.setDrawGridLines(false);
                    xAxis.setAxisMaximum(1440);
                    xAxis.setAxisMinimum(-0.5f);
                    xAxis.setLabelCount(4, true);
                    xAxis.setDrawAxisLine(true);
                    lineChart.setDragEnabled(false);
                    xAxis.setGranularity(1f);
                    xAxis.setGranularityEnabled(true);
                    xAxis.setCenterAxisLabels(false);
                    xAxis.setValueFormatter(new IAxisValueFormatter() {
                        @Override
                        public String getFormattedValue(float value, AxisBase axis) {
                            return convertToTimeString(value);
                        }
                    });
                } else {
                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setTextColor(Color.DKGRAY);
                    xAxis.setTypeface(tf);
                    xAxis.setTypeface(tf);
                    xAxis.setAxisMaximum(numOfDays);
                    xAxis.setDrawGridLines(false);
                    lineChart.setDragEnabled(false);
                    xAxis.setDrawAxisLine(true);
                    xAxis.setTextSize(12);
                    if (itemView.getContext() != null) {
                        xAxis.setAxisLineColor(ContextCompat.getColor(itemView.getContext(), R.color.transparent_gray));
                    }
                    xAxis.setCenterAxisLabels(false);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    if (numOfDays == 7) {
                        xAxis.setGranularity(1);
                        xAxis.setGranularityEnabled(true);
                        xAxis.setLabelCount(7);
                    } else {
                        xAxis.setLabelCount(6);
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

            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setAxisMaximum(22f);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setTypeface(tf);
            leftAxis.setTextSize(12);
            leftAxis.setGranularity(1.0f);
            leftAxis.setTextColor(Color.DKGRAY);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setLabelCount(23, true);
            leftAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    Log.i(TAG, "Y-axis value is: " + value);
                    /*if (value == 4f) {
                        return "Min";
                    }*/
                    if (value == 9f) {
                        return itemView.getContext().getString(R.string.chart_mild_abbr);
                    } else if (value == 14f) {
                        return itemView.getContext().getString(R.string.chart_moderate_abbr);
                    } else if (value == 21f) {
                        return itemView.getContext().getString(R.string.chart_severe_abbr);
                    } else {
                        return "";
                    }
                }
            });

            leftAxis.removeAllLimitLines();
            LimitLine averageLL = new LimitLine(average, itemView.getContext().getString(R.string.chart_avg_abbr));
            averageLL.setLineWidth(1f);
            averageLL.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
            averageLL.setTypeface(tf);
            averageLL.setTextStyle(Paint.Style.FILL);
            averageLL.enableDashedLine(4, 8, 1);
            averageLL.setTextColor(Color.DKGRAY);
            averageLL.setTextSize(12);
            averageLL.setLineColor(ContextCompat.getColor(itemView.getContext(), R.color.transparent_gray));
            leftAxis.addLimitLine(averageLL);
            LimitLine poor = new LimitLine(1f, "");
            poor.setLineWidth(0.5f);
            poor.setTypeface(tf);
            poor.enableDashedLine(4, 8, 1);
            poor.disableDashedLine();
            poor.setTextSize(12);
            LimitLine good = new LimitLine(3f, "");
            good.setLineWidth(0.5f);
            good.setTypeface(tf);
            good.enableDashedLine(4, 8, 1);
            good.setTextSize(12);
            good.setLineColor(ContextCompat.getColor(itemView.getContext(), R.color.transparent_gray));

            rightAxis.setDrawGridLines(false);
            rightAxis.setDrawLabels(false);
            rightAxis.setDrawAxisLine(false);
            rightAxis.removeAllLimitLines();
            rightAxis.setEnabled(false);
            NumeralChartMarkerView numeralChartMarker = new NumeralChartMarkerView(
                    itemView.getContext(), R.layout.mpchart_chartvalueselectedview,
                    entryDatesList, numOfDays, lineChart,
                    itemView.getContext().getString(R.string.anxiety_gad7_camel_case));
            lineChart.setMarker(numeralChartMarker);

            LineDataSet set = new LineDataSet(lineEntries, "");
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
            set.setLineWidth(1f);
            set.setDrawValues(false);
            set.setCircleColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
            set.setCircleHoleColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
            set.setCircleHoleRadius(2f);
            set.setCircleRadius(2f);
            set.setDrawFilled(true);
            Drawable drawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_white_to_primary);
            set.setFillDrawable(drawable);
            set.setDrawHorizontalHighlightIndicator(false);
            set.setDrawVerticalHighlightIndicator(false);
            List<ILineDataSet> lineDataSets = new ArrayList<>();
            lineDataSets.add(set);
            LineData lineData = new LineData(lineDataSets);
            lineChart.setData(lineData);
            lineChart.getLegend().setEnabled(false);
            lineChart.invalidate();
        }
    }
}

