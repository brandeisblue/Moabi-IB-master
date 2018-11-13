package com.ivorybridge.moabi.ui.util;

import android.content.Context;
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
