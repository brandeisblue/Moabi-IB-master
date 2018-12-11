package com.ivorybridge.moabi.ui.recyclerviewitem.insight;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
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
import com.github.mikephil.charting.model.GradientColor;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.util.ImageBarChartRenderer;
import com.ivorybridge.moabi.ui.util.NumeralChartMarkerView;
import com.ivorybridge.moabi.util.FormattedTime;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

public class InsightBestAndWorstItem extends AbstractItem<InsightBestAndWorstItem, InsightBestAndWorstItem.ViewHolder> {

    private static final String TAG = InsightBestAndWorstItem.class.getSimpleName();
    private List<BarEntry> barEntries;
    private List<String> entryDatesList;
    private double average;
    private float bestValue;
    private String inputType;

    public InsightBestAndWorstItem() {

    }

    public InsightBestAndWorstItem(String inputType, List<BarEntry> barEntries, List<String> entryDatesList, double average, float bestValue) {
        this.inputType = inputType;
        this.barEntries = barEntries;
        this.entryDatesList = entryDatesList;
        this.average = average;
        this.bestValue = bestValue;
    }


    @Override
    public int getType() {
        return R.id.insight_bestandworst_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_insight_bestandworst_item;
    }

    @NonNull
    @Override
    public InsightBestAndWorstItem.ViewHolder getViewHolder(View v) {
        return new InsightBestAndWorstItem.ViewHolder(v);
    }

    // Manually create the ViewHolder class
    protected static class ViewHolder extends FastAdapter.ViewHolder<InsightBestAndWorstItem> {

        @BindView(R.id.rv_item_insight_bestandworst_item_barchart)
        BarChart barChart;
        @BindView(R.id.rv_item_insight_bestandworst_item_title_textview)
        TextView titleTextView;
        private FormattedTime formattedTime;
        private Typeface tf;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final InsightBestAndWorstItem item, List<Object> payloads) {
            formattedTime = new FormattedTime();
            tf = ResourcesCompat.getFont(itemView.getContext(), R.font.source_sans_pro);
            styleBarChart();
            drawBarChart(item.barEntries, item.entryDatesList, item.average, item.bestValue, item.inputType);
        }

        @Override
        public void unbindView(InsightBestAndWorstItem item) {
        }

        private void drawBarChart(List<BarEntry> barEntries, List<String> entryDatesList, double average, float bestValue, String inputType) {

            if (barEntries.size() == 0) {
                barChart.isEmpty();
                barChart.clear();
                barChart.invalidate();
                return;
            }

            titleTextView.setText(itemView.getContext().getString(R.string.best_day_title));
            barChart.clear();
            Log.i(TAG, barEntries.toString());
            List<String> formattedEntryDatesList = new ArrayList<>();
            for (int i = 0; i < entryDatesList.size(); i++) {
                String newDate = formattedTime.convertStringEEEToE(entryDatesList.get(i));
                formattedEntryDatesList.add(newDate.substring(0, 1));
            }

            Log.i(TAG, formattedEntryDatesList.toString());
            int numOfDays = 7;
            XAxis xAxis = barChart.getXAxis();
            xAxis.setTypeface(tf);
            xAxis.setDrawGridLines(false);
            barChart.setDragEnabled(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setTextSize(10);
            xAxis.setTextColor(Color.DKGRAY);
            xAxis.setAxisLineColor(ContextCompat.getColor(itemView.getContext(), R.color.transparent_gray));
            xAxis.setCenterAxisLabels(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1);
            xAxis.setGranularityEnabled(true);
            //xAxis.setLabelCount(numOfDays);
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    Log.i(TAG, value + ", " + (int) value);
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

            YAxis leftAxis = barChart.getAxisLeft();
            YAxis rightAxis = barChart.getAxisRight();
            //leftAxis.setLabelCount(3, true);
            leftAxis.setDrawLabels(true);
            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawAxisLine(false);
            rightAxis.setEnabled(false);
            if (inputType.equals(itemView.getContext().getString(R.string.mood_camel_case)) || inputType.equals(itemView.getContext().getString(R.string.energy_camel_case))) {
                // set up y-axis
                leftAxis.setAxisMaximum(3.5f);
                leftAxis.setAxisMinimum(1f);
                leftAxis.setTypeface(tf);
                leftAxis.setTextSize(10);
                leftAxis.setGranularity(0.5f);
                leftAxis.setTextColor(Color.DKGRAY);
                leftAxis.setGranularityEnabled(true);
                leftAxis.setLabelCount(6, true);
                leftAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        Log.i(TAG, "Y-axis value is: " + value);
                        if (value == 1f) {
                            return itemView.getContext().getString(R.string.chart_label_poor);
                        } else if (value == 3f) {
                            return itemView.getContext().getString(R.string.chart_label_good);
                        } else {
                            return "";
                        }
                    }
                });
            } else if (inputType.equals(itemView.getContext().getString(R.string.stress_camel_case))) {
                titleTextView.setText(itemView.getContext().getString(R.string.worst_day_title));
                leftAxis.setAxisMaximum(11f);
                leftAxis.setAxisMinimum(0f);
                leftAxis.setTypeface(tf);
                leftAxis.setTextSize(10);
                leftAxis.setGranularity(1f);
                leftAxis.setLabelCount(12, true);
                leftAxis.setTextColor(Color.DKGRAY);
                leftAxis.setGranularityEnabled(true);
                leftAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        //Log.i(TAG, "Y-axis value is: " + value);
                        if (value == 10f) {
                            return itemView.getContext().getString(R.string.chart_label_max);
                        } else if (value == 0f) {
                            return itemView.getContext().getString(R.string.chart_label_none);
                        } else {
                            return "";
                        }
                    }
                });
            } else if (inputType.equals(itemView.getContext().getString(R.string.daily_review_camel_case))) {
                leftAxis.setAxisMaximum(5.5f);
                leftAxis.setAxisMinimum(1f);
                leftAxis.setTypeface(tf);
                leftAxis.setTextSize(10);
                leftAxis.setGranularity(1f);
                leftAxis.setTextColor(Color.DKGRAY);
                leftAxis.setGranularityEnabled(true);
                leftAxis.setLabelCount(10, true);
                leftAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        //Log.i(TAG, "Y-axis value is: " + value);
                        if (value == 5f) {
                            return itemView.getContext().getString(R.string.chart_label_excellent);
                        } else if (value == 1f) {
                            return itemView.getContext().getString(R.string.chart_label_terrible);
                        } else {
                            return "";
                        }
                    }
                });
            } else if (inputType.equals(itemView.getContext().getString(R.string.depression_phq9_camel_case))) {
                titleTextView.setText(itemView.getContext().getString(R.string.worst_day_title));
                leftAxis.setAxisMaximum(30f);
                leftAxis.setAxisMinimum(0f);
                leftAxis.setTypeface(tf);
                leftAxis.setTextSize(10);
                leftAxis.setGranularity(3);
                leftAxis.setGranularityEnabled(true);
                leftAxis.setTextColor(Color.DKGRAY);
                leftAxis.setLabelCount(11, true);
                leftAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        Log.i(TAG, "Y-axis value is: " + value);
                    /*
                    if (value == 4f) {
                        return "Min";
                    }*/
                        if (value == 9f) {
                            return itemView.getContext().getString(R.string.chart_mild_abbr);
                        } /*else if (value == 19f) {
                        return "M.Sv";
                    } */ else if (value == 27f) {
                            return itemView.getContext().getString(R.string.chart_severe_abbr);
                        } else {
                            return "";
                        }
                    }
                });
            } else if (inputType.equals(itemView.getContext().getString(R.string.anxiety_gad7_camel_case))) {
                titleTextView.setText(itemView.getContext().getString(R.string.worst_day_title));
                leftAxis.setAxisMaximum(24f);
                leftAxis.setAxisMinimum(0f);
                leftAxis.setTypeface(tf);
                leftAxis.setTextSize(10);
                leftAxis.setGranularity(1.0f);
                leftAxis.setTextColor(Color.DKGRAY);
                leftAxis.setGranularityEnabled(true);
                leftAxis.setLabelCount(9, true);
                leftAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        Log.i(TAG, "Y-axis value is: " + value);
                    /*if (value == 4f) {
                        return "Min";
                    }*/
                        if (value == 9f) {
                            return itemView.getContext().getString(R.string.chart_mild_abbr);
                        } else if (value == 21f) {
                            return itemView.getContext().getString(R.string.chart_severe_abbr);
                        } else {
                            return "";
                        }
                    }
                });
            } else if (inputType.equals(itemView.getContext().getString(R.string.phone_usage_camel_case)) ||
                    inputType.equals(itemView.getContext().getString(R.string.activity_active_minutes_title)) ||
                    inputType.equals(itemView.getContext().getString(R.string.activity_sedentary_minutes_title)) ||
                    inputType.equals(itemView.getContext().getString(R.string.activity_sleep_title)) ||
                    inputType.equals(itemView.getContext().getString(R.string.timer_camel_case))) {
                titleTextView.setText(itemView.getContext().getString(R.string.worst_day_title));
                leftAxis.setTypeface(tf);
                leftAxis.setTextColor(Color.DKGRAY);
                //leftAxis.setAxisMaximum(3);
                leftAxis.setAxisMinimum(0);
                leftAxis.setTextSize(10f);
                //leftAxis.setGranularity(1f);
                //leftAxis.setGranularityEnabled(true);
                leftAxis.setLabelCount(3, true);
                leftAxis.setDrawAxisLine(false);
                leftAxis.setSpaceMax(40f);
                //leftAxis.setSpaceMin(10f);
                leftAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return convertToTimeString(value);
                    }
                });
            } else {
                leftAxis.setAxisMinimum(0f);
                leftAxis.setTypeface(tf);
                leftAxis.setTextSize(10);
                leftAxis.setGranularity(1f);
                leftAxis.setTextColor(Color.DKGRAY);
                leftAxis.setGranularityEnabled(true);
                leftAxis.setLabelCount(3, true);
                leftAxis.setValueFormatter(null);
            }

            leftAxis.removeAllLimitLines();
            LimitLine averageLL = new LimitLine((float) average, itemView.getContext().getString(R.string.chart_avg_abbr));
            averageLL.setLineWidth(1f);
            averageLL.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
            averageLL.setTypeface(tf);
            averageLL.setTextStyle(Paint.Style.FILL);
            averageLL.enableDashedLine(4, 8, 1);
            averageLL.setTextColor(Color.DKGRAY);
            averageLL.setTextSize(10);
            //averageLL.setLineColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            averageLL.setLineColor(ContextCompat.getColor(itemView.getContext(), R.color.transparent_gray));
            leftAxis.addLimitLine(averageLL);

            BarDataSet set = new BarDataSet(barEntries, "");
            int startColor1 = ContextCompat.getColor(itemView.getContext(), R.color.white20);
            int startColor2 = ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary);
            List<GradientColor> gradientColors = new ArrayList<>();
            gradientColors.add(new GradientColor(startColor1, startColor2));
            set.setGradientColors(gradientColors);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            //set.setColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));

            //set.setLineWidth(1f);
            //Log.i(TAG, set.toString());
            BarData barData = new BarData(set);
            barData.setDrawValues(false);
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
            barChart.resetViewPortOffsets();

            Bitmap bestBitmap = BitmapFactory.decodeResource(itemView.getResources(), R.drawable.ic_star_filled);
            barChart.setRenderer(new ImageBarChartRenderer(barChart, barChart.getAnimator(), barChart.getViewPortHandler(), bestBitmap, bestValue));
            NumeralChartMarkerView chartMarkerView = new NumeralChartMarkerView(itemView.getContext(),
                    R.layout.mpchart_chartvalueselectedview, entryDatesList, numOfDays, barChart, inputType);
            barChart.setMarker(chartMarkerView);
            barChart.getLegend().setEnabled(false);
            barChart.setMinOffset(0);
            barChart.setExtraBottomOffset(8);
            barChart.setExtraTopOffset(8);
            barChart.invalidate();
        }

        private void styleBarChart() {
            barChart.setVisibility(View.VISIBLE);
            barChart.setScaleEnabled(false);
            barChart.setDrawGridBackground(false);
            barChart.getDescription().setEnabled(false);
            barChart.setNoDataTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimaryDarkComplt));
            barChart.setNoDataText(itemView.getContext().getString(R.string.chart_no_entry));
            barChart.setNoDataTextTypeface(tf);
            //lineChart.animateXY(1000, 1000);
            barChart.setTouchEnabled(true);
            //lineChart.setDragEnabled(false);
            barChart.setPinchZoom(true);
            barChart.setDoubleTapToZoomEnabled(true);
        }

        private String convertToTimeString(float timeInFloat) {
            long minute = (long) timeInFloat % 60;
            long hour = (long) timeInFloat / 60;
            if (hour < 1) {
                return minute + " " + itemView.getContext().getString(R.string.unit_time_sing);
            } else {
                return hour + " " + itemView.getContext().getString(R.string.unit_hour_sing);
            }
        }
    }
}
