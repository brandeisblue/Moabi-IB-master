package com.ivorybridge.moabi.ui.util;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TimeChartMarkerView extends MarkerView {

    private static final String TAG = TimeChartMarkerView.class.getSimpleName();
    private TextView topText;
    private TextView bottomText;
    private MPPointF mOffset;
    private Map<String, Long> AppUsageNameRankMap = new LinkedHashMap<>();
    private FormattedTime formattedTime;
    private List<String> entryDatesList;
    private int numOfDays;
    private Context context;

    public TimeChartMarkerView(Context context, int layoutResource, Map<String, Long> AppUsageNameRankMap) {
        super(context, layoutResource);
        this.context = context;
        this.AppUsageNameRankMap = AppUsageNameRankMap;
        topText = (TextView) findViewById(R.id.mpchart_chartvalueselectedview_top_textview);
        bottomText = (TextView) findViewById(R.id.mpchart_chartvalueselectedview_bottom_textview);
        this.formattedTime = new FormattedTime();
    }

    public TimeChartMarkerView(Context context, int layoutResource, List<String> entryDatesList, int numOfDays, Chart chart) {
        super(context, layoutResource);
        topText = (TextView) findViewById(R.id.mpchart_chartvalueselectedview_top_textview);
        bottomText = (TextView) findViewById(R.id.mpchart_chartvalueselectedview_bottom_textview);
        this.formattedTime = new FormattedTime();
        this.entryDatesList = entryDatesList;
        this.numOfDays = numOfDays;
        setChartView(chart);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String time = convertToTimeString(e.getY());
        bottomText.setText(time);
        Log.i(TAG, e.getX() + ", " + e.getY() + "");
        //Log.i(TAG, time);
        if (entryDatesList != null) {
            float x = e.getX();
            if (x > 0) {
                int index = (int) x;
                String date = "";
                if (index < numOfDays) {
                    //Log.i(TAG, value + ", " + (int) value + ", " + entryDatesList.get(index));
                    date = entryDatesList.get(index);
                    Log.i(TAG, date);
                }
                if (numOfDays == 180) {
                    int year = Integer.parseInt(date.substring(0, 4));
                    int week = Integer.parseInt(date.substring(5));
                    Long start = formattedTime.getStartTimeOfYYYYW(week, year);
                    Long end = formattedTime.getEndTimeOfYYYYW(week, year);
                    String newDate = formattedTime.convertLongToMMMD(start) + " - " + formattedTime.convertLongToMMMD(end);
                    topText.setText(newDate);
                } else if (numOfDays == 395) {
                    String newDate = formattedTime.convertStringYYYYMMToMMMYYYY(date);
                    topText.setText(newDate);
                } else {
                    topText.setText(formattedTime.convertStringYYYYMMDDToMMMD(date));
                }
            }
        } else {
            String appName = "";
            for (Map.Entry<String, Long> appSummary : AppUsageNameRankMap.entrySet()) {
                if (appSummary.getValue() == 1f * e.getX()) {
                    appName = appSummary.getKey();
                }
            }
            topText.setText(appName);
            bottomText.setText(time);
            Log.i(TAG, appName + ": " + time);
        }
        super.refreshContent(e, highlight);// set the entry-value  s the display text
    }

    @Override
    public MPPointF getOffset() {
        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }
        return mOffset;
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        super.draw(canvas, posX, posY);
        getOffsetForDrawingAtPoint(posX, posY);
    }


    private String convertToTimeString(float timeInFloat) {
        long minute = Math.round((timeInFloat / (1000 * 60)) % 60);
        long hour = Math.round(timeInFloat / (1000 * 60 * 60));
        String timeElapsed = "";
        if (hour == 0) {
            if (minute < 2) {
                timeElapsed = minute + context.getString(R.string.unit_time_sing);
            } else {
                timeElapsed = minute + context.getString(R.string.unit_time_plur);
            }
        } else if (hour == 1) {
            if (minute < 2) {
                timeElapsed = hour + context.getString(R.string.unit_hour_sing) + " " +
                        + minute + context.getString(R.string.unit_time_sing);
            } else {
                timeElapsed = hour + context.getString(R.string.unit_hour_sing) + " " +
                        + minute + context.getString(R.string.unit_time_plur);
            }
        } else {
            if (minute < 2) {
                timeElapsed = hour + context.getString(R.string.unit_hour_plur) + " " +
                        + minute + context.getString(R.string.unit_time_sing);
            } else {
                timeElapsed = hour + context.getString(R.string.unit_hour_plur) + " " +
                        + minute + context.getString(R.string.unit_time_plur);
            }
        }
        return timeElapsed;
    }
}
