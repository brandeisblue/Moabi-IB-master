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

public class FitnessTrackerBarChartMarkerView extends MarkerView {

    private static final String TAG = FitnessTrackerBarChartMarkerView.class.getSimpleName();
    private TextView topText;
    private TextView bottomText;
    private MPPointF mOffset;
    private Map<String, Long> FitnessTrackerNameRankMap = new LinkedHashMap<>();
    private FormattedTime formattedTime;
    private List<String> entryDatesList;
    private int numOfDays;
    private String activity;
    private Context context;

    public FitnessTrackerBarChartMarkerView(Context context, int layoutResource, List<String> entryDatesList, String activity, int numOfDays,  Chart chart) {
        super(context, layoutResource);
        this.context = context;
        topText = (TextView) findViewById(R.id.mpchart_chartvalueselectedview_top_textview);
        bottomText = (TextView) findViewById(R.id.mpchart_chartvalueselectedview_bottom_textview);
        this.formattedTime = new FormattedTime();
        this.entryDatesList = entryDatesList;
        this.numOfDays = numOfDays;
        this.activity = activity;
        setChartView(chart);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String history = "" + e.getY();
        if (activity.equals(context.getString(R.string.activity_steps_camel_case))) {
            history = "" + (long) e.getY();
            history += " steps";
        } else if (activity.equals(context.getString(R.string.activity_active_minutes_camel_case)) || activity.equals(context.getString(R.string.activity_sedentary_minutes_camel_case)) || activity.equals(context.getString(R.string.activity_sleep_camel_case))) {
            history = "" + (long) e.getY();
            history += " mins";
        } else if (activity.equals(context.getString(R.string.activity_distance_camel_case))) {
            history += " km";
        } else if (activity.equals(context.getString(R.string.activity_calories_camel_case))) {
            history = "" + (long) e.getY();
            history += " Cal";
        } else if (activity.equals(context.getString(R.string.activity_floors_camel_case))) {
            history = "" + (long) e.getY();
            history += " floors";
        }
        bottomText.setText(history);
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
}
