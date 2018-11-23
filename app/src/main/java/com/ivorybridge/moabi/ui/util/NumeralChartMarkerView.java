package com.ivorybridge.moabi.ui.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.util.FormattedTime;

import java.util.List;
import java.util.Locale;

public class NumeralChartMarkerView extends MarkerView {

    private static final String TAG = NumeralChartMarkerView.class.getSimpleName();
    private TextView topText;
    private TextView bottomText;
    private List<String> entryDatesList;
    private int numOfDays;
    private MPPointF mOffset;
    private FormattedTime formattedTime;
    private String inputType;
    private SharedPreferences unitSharedPreferences;
    private String unit;

    public NumeralChartMarkerView(Context context, int layoutResource, List<String> entryDatesList,
                                  int numOfDays, Chart chart, String inputType) {
        super(context, layoutResource);
        // this markerview only displays a textview
        topText = (TextView) findViewById(R.id.mpchart_chartvalueselectedview_top_textview);
        bottomText = (TextView) findViewById(R.id.mpchart_chartvalueselectedview_bottom_textview);
        formattedTime = new FormattedTime();
        this.entryDatesList = entryDatesList;
        this.numOfDays = numOfDays;
        this.inputType = inputType;
        this.unitSharedPreferences = getContext().getSharedPreferences(getContext().getString(
                R.string.com_ivorybridge_moabi_UNIT_SHARED_PREFERENCE_KEY),
                Context.MODE_PRIVATE);
        this.unit = unitSharedPreferences.getString(getContext()
                        .getString(R.string.com_ivorybridge_mobai_UNIT_KEY),
                getContext().getString(R.string.preference_unit_si_title));
        setChartView(chart);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        //String time = convertToTimeString(e.getX());
        Log.i(TAG, e.toString());
        Log.i(TAG, highlight.toString());
        /*
        if (highlight.getDataSetIndex() == 0) {
            topText.setText(getString(R.string.mood_camel_case));
        } else {
            topText.setText(getString(R.string.energy_camel_case));
        }*/
        float x = e.getX();
        String score = String.format(Locale.US, "%.2f",e.getY());
        if (inputType.equals(getContext().getString(R.string.mood_camel_case)) ||
                inputType.equals(getContext().getString(R.string.energy_camel_case))) {
            score = String.format(Locale.US, "%.0f",((e.getY() - 1f) / 2f) * 100) + "%";
        } else if (inputType.equals(getContext().getString(R.string.stress_camel_case))) {
            score = String.format(Locale.US, "%.0f",(e.getY() / 10f) * 100) + "%";
        }  else if (inputType.equals(getContext().getString(R.string.daily_review_camel_case))) {
            score = String.format(Locale.US, "%.0f",((e.getY() - 1f) / 4f) * 100) + "%";
        } else if (inputType.equals(getContext().getString(R.string.depression_phq9_camel_case))) {
            score = String.format(Locale.US, "%.0f",(e.getY() / 27f) * 100) + "%";
        } else if (inputType.equals(getContext().getString(R.string.anxiety_gad7_camel_case))) {
            score = String.format(Locale.US, "%.0f",(e.getY() / 21f) * 100) + "%";
        } else if (inputType.equals(getContext().getString(R.string.activity_steps_title))) {
            if (e.getY() > 1) {
                score = String.format(Locale.US, "%.0f", (e.getY())) + " " + getContext().getString(R.string.unit_step_plur);
            } else {
                score = String.format(Locale.US, "%.0f", (e.getY())) + " " + getContext().getString(R.string.unit_step_sing);
            }
        } else if (inputType.contains("Minutes") || inputType.equals(getContext().getString(R.string.activity_sleep_title)) ||
                inputType.contains(getContext().getString(R.string.phone_usage_camel_case))) {
            score = String.format(Locale.US, "%.0f", (e.getY())) + " " + getContext().getString(R.string.unit_time_sing);
        } else if (inputType.equals(getContext().getString(R.string.activity_distance_title))) {
            if (unit.equals(getContext().getString(R.string.preference_unit_si_title))) {
                score = String.format(Locale.US, "%.2f", (e.getY())) + " " + getContext().getString(R.string.unit_distance_si);
            } else {
                score = String.format(Locale.US, "%.2f", (e.getY())) + " " + getContext().getString(R.string.unit_distance_usc);
            }
        } else if (inputType.equals(getContext().getString(R.string.activity_calories_title))) {
            score = String.format(Locale.US, "%.0f", (e.getY())) + " " + getContext().getString(R.string.unit_calories);
        } else if (inputType.equals(getContext().getString(R.string.activity_floors_title))) {
            if (e.getY() > 1) {
                score = String.format(Locale.US, "%.0f", (e.getY())) + " " + getContext().getString(R.string.unit_floor_plur);
            } else {
                score = String.format(Locale.US, "%.0f", (e.getY())) + " " + getContext().getString(R.string.unit_floor_sing);
            }
        } else if (inputType.equals(getContext().getString(R.string.timer_camel_case))) {
            score = String.format(Locale.US, "%.0f", (e.getY())) + " " + getContext().getString(R.string.unit_time_sing);
        } else if (inputType.equals(getContext().getString(R.string.baactivity_camel_case))) {
            if (e.getY() > 1) {
                score = String.format(Locale.US, "%.0f", (e.getY())) + " " + getContext().getString(R.string.unit_activity_plur);
            } else {
                score = String.format(Locale.US, "%.0f", (e.getY())) + " " + getContext().getString(R.string.unit_activity_sing);
            }
        }
        if (x >= 0) {
            int index = (int) x;
            String date = "";
            if (index < numOfDays) {
                //Log.i(TAG, value + ", " + (int) value + ", " + entryDatesList.get(index));
                date = entryDatesList.get(index);
            }
            if (numOfDays == 180) {
                int year = Integer.parseInt(date.substring(0, 4));
                int week = Integer.parseInt(date.substring(5));
                Long start = formattedTime.getStartTimeOfYYYYW(week, year);
                Long end = formattedTime.getEndTimeOfYYYYW(week, year);
                String newDate = formattedTime.convertLongToMMMD(start) + " - " + formattedTime.convertLongToMMMD(end);
                topText.setText(newDate);
                bottomText.setText(score);
            } else if (numOfDays == 395) {
                String newDate = formattedTime.convertStringYYYYMMToMMMYYYY(date);
                topText.setText(newDate);
                bottomText.setText(score);
            } else if (numOfDays == 1) {
                date = entryDatesList.get(index);
                topText.setText(date);
                bottomText.setText(score);
            } else {
                topText.setText(formattedTime.convertStringYYYYMMDDToMMMD(date));
                bottomText.setText(score);
            }
        } else {
            topText.setVisibility(View.GONE);
            bottomText.setText(score);
        }
        //valueSelectedTextView.setText("" + e.getY());
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
