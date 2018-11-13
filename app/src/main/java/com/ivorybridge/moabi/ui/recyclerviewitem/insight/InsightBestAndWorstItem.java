package com.ivorybridge.moabi.ui.recyclerviewitem.insight;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
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

    public InsightBestAndWorstItem() {

    }

    public InsightBestAndWorstItem(List<BarEntry> barEntries, List<String> entryDatesList, double average, float bestValue) {
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
            drawBarChart(item.barEntries, item.entryDatesList, item.average, item.bestValue);
        }

        @Override
        public void unbindView(InsightBestAndWorstItem item) {
        }

        private void drawBarChart(List<BarEntry> barEntries, List<String> entryDatesList, double average, float bestValue) {
            if (barEntries.size() == 0) {
                barChart.isEmpty();
                barChart.clear();
                barChart.invalidate();
                return;
            }

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
            xAxis.setDrawAxisLine(true);
            xAxis.setSpaceMin(0.5f);
            xAxis.setSpaceMax(0.5f);
            xAxis.setTextSize(12);
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
            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawAxisLine(false);
            //leftAxis.setAxisMaximum(3f);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setTypeface(tf);
            leftAxis.setTextSize(12);
            leftAxis.setGranularity(1f);
            leftAxis.setTextColor(Color.DKGRAY);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setLabelCount(5, true);
            leftAxis.setDrawLabels(false);
            rightAxis.setAxisMinimum(0);
            rightAxis.setEnabled(false);
            rightAxis.setDrawGridLines(false);
            rightAxis.setDrawLabels(false);
            rightAxis.setDrawAxisLine(false);
            rightAxis.setEnabled(false);

            BarDataSet set = new BarDataSet(barEntries, "");
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));

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

            int height = barChart.getHeight();
            Paint paint = barChart.getRenderer().getPaintRender();
            int[] colors = new int[]{ContextCompat.getColor(itemView.getContext(), R.color.colorPrimaryDark), ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary),
                    ContextCompat.getColor(itemView.getContext(), R.color.white)};
            float[] positions = new float[]{0, 0.5f, 1};

            LinearGradient linearGradient = new LinearGradient(0, 0, 0, height,
                    colors, positions,
                    Shader.TileMode.REPEAT);
            paint.setShader(linearGradient);

            Bitmap bestBitmap = BitmapFactory.decodeResource(itemView.getResources(), R.drawable.ic_star_filled);
            barChart.setRenderer(new ImageBarChartRenderer(barChart, barChart.getAnimator(), barChart.getViewPortHandler(), bestBitmap, bestValue));
            NumeralChartMarkerView chartMarkerView = new NumeralChartMarkerView(itemView.getContext(),
                    R.layout.mpchart_chartvalueselectedview, entryDatesList, numOfDays, barChart, "");
            barChart.setMarker(chartMarkerView);
            barChart.getLegend().setEnabled(false);
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
            barChart.setExtraOffsets(8, 0, 8, 8);
        }
    }
}
