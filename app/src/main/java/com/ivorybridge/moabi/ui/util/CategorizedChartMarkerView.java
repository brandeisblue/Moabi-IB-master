package com.ivorybridge.moabi.ui.util;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.ivorybridge.moabi.R;

import java.util.Locale;

public class CategorizedChartMarkerView extends MarkerView {

    private static final String TAG = CategorizedChartMarkerView.class.getSimpleName();
    private TextView topText;
    private TextView bottomText;
    private MPPointF mOffset;
    private Context context;

    public CategorizedChartMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        this.context = context;
        // this markerview only displays a textview
        topText = (TextView) findViewById(R.id.mpchart_chartvalueselectedview_top_textview);
        bottomText = (TextView) findViewById(R.id.mpchart_chartvalueselectedview_bottom_textview);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String time = convertToDateString(e.getX());
        String value =  context.getString(R.string.chart_label_poor);
        if (e.getY() == 1) {
            value =  context.getString(R.string.chart_label_poor);
        } else if (e.getY() == 3){
            value =  context.getString(R.string.chart_label_good);
        }
        Log.i(TAG, highlight.toString());
        if (highlight.getDataSetIndex() == 0) {
            topText.setText( context.getString(R.string.mood_camel_case));
            bottomText.setText(value);
        } else {
            topText.setText( context.getString(R.string.energy_camel_case));
            bottomText.setText(value);
        }
        super.refreshContent(e, highlight);// set the entry-value  s the display text
    }

    @Override
    public MPPointF getOffset() {
        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), - getHeight());
        }
        return mOffset;
    }

    private String convertToDateString(float timeInFloat) {
        int hours = (int) Math.floor(timeInFloat / 60);
        int minutes = (int) Math.floor(timeInFloat % 60);
        String timeOfRecord = String.format(Locale.getDefault(), "%02d", hours) + ":" + String.format(Locale.getDefault(), "%02d", minutes);
        return timeOfRecord;
    }
}
