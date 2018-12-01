package com.ivorybridge.moabi.ui.recyclerviewitem.insight;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ivorybridge.moabi.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

public class InsightBodyAverageItem extends AbstractItem<InsightBodyAverageItem, InsightBodyAverageItem.ViewHolder> {

    private static final String TAG = InsightBodyAverageItem.class.getSimpleName();
    private String inputType;
    private Map<String, Double> activitySummaryMap;
    private int numOfDays;
    private double lowest;
    private double highest;
    private double average;

    public InsightBodyAverageItem(String inputType, Map<String, Double> activitySummaryMap, int numOfDays) {
        this.inputType = inputType;
        this.activitySummaryMap = activitySummaryMap;
        this.numOfDays = numOfDays;
    }

    public InsightBodyAverageItem(String inputType, double lowest, double highest, double average) {
        this.inputType = inputType;
        this.lowest = lowest;
        this.highest = highest;
        this.average = average;
    }


    @Override
    public int getType() {
        return R.id.insight_body_average_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_insight_body_average_item;
    }

    @NonNull
    @Override
    public InsightBodyAverageItem.ViewHolder getViewHolder(View v) {
        return new InsightBodyAverageItem.ViewHolder(v);
    }

    // Manually create the ViewHolder class
    protected static class ViewHolder extends FastAdapter.ViewHolder<InsightBodyAverageItem> {

        @BindView(R.id.rv_item_insight_body_average_item_title_textview)
        TextView textView;
        @BindView(R.id.rv_item_insight_body_average_item_seekbar)
        IndicatorSeekBar seekBar;
        private SharedPreferences unitSharedPreferences;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final InsightBodyAverageItem item, List<Object> payloads) {
            unitSharedPreferences = itemView.getContext().getSharedPreferences(
                    itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_UNIT_SHARED_PREFERENCE_KEY),
                    Context.MODE_PRIVATE);
            String unit = unitSharedPreferences.getString(itemView.getContext()
                            .getString(R.string.com_ivorybridge_mobai_UNIT_KEY),
                    itemView.getContext().getString(R.string.preference_unit_si_title));
            seekBar.setMax(0);
            seekBar.setMin(0);
            seekBar.setProgress(0);
            seekBar.setDecimalScale(2);
            Log.i(TAG, item.average + ", " + item.lowest + ", " + item.highest);
            String unitString = itemView.getContext().getString(R.string.unit_time_sing);
            if (item.inputType.equals(itemView.getContext().getString(R.string.activity_steps_title))) {
                seekBar.setMax((float) item.highest);
                seekBar.setMin((float) item.lowest);
                seekBar.setProgress((float) item.average);
                if (item.average <= 1) {
                    unitString = itemView.getContext().getString(R.string.unit_step_sing);
                } else {
                    unitString = itemView.getContext().getString(R.string.unit_step_plur);
                }
            } else if (item.inputType.equals(itemView.getContext().getString(R.string.activity_active_minutes_title)) ||
                    item.inputType.equals(itemView.getContext().getString(R.string.activity_sleep_title)) ||
                    item.inputType.contains(itemView.getContext().getString(R.string.phone_usage_camel_case)) ||
                    item.inputType.equals(itemView.getContext().getString(R.string.activity_sedentary_minutes_title))) {
                seekBar.setMax((float) item.highest);
                seekBar.setMin((float) item.lowest);
                seekBar.setProgress((float) item.average);
                unitString = itemView.getContext().getString(R.string.unit_time_sing);
            } else if (item.inputType.equals(itemView.getContext().getString(R.string.activity_distance_title))) {
                seekBar.setMax((float) item.highest);
                seekBar.setMin((float) item.lowest);
                seekBar.setProgress((float) item.average);
                if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                    unitString = itemView.getContext().getString(R.string.unit_distance_si);
                } else {
                    unitString = itemView.getContext().getString(R.string.unit_distance_usc);
                    /*
                    seekBar.setMax((float) item.highest * 0.62137f);
                    seekBar.setMin((float) item.lowest * 0.62137f);
                    seekBar.setProgress((float) item.average * 0.62137f);*/
                }
            } else if (item.inputType.equals(itemView.getContext().getString(R.string.activity_calories_title))) {
                seekBar.setMax((float) item.highest);
                seekBar.setMin((float) item.lowest);
                seekBar.setProgress((float) item.average);
                unitString = itemView.getContext().getString(R.string.unit_calories);
            } else if (item.inputType.equals(itemView.getContext().getString(R.string.activity_floors_title))) {
                seekBar.setMax((float) item.highest);
                seekBar.setMin((float) item.lowest);
                seekBar.setProgress((float) item.average);
                if (item.average <= 1) {
                    unitString = itemView.getContext().getString(R.string.unit_floor_sing);
                } else {
                    unitString = itemView.getContext().getString(R.string.unit_floor_plur);
                }
            } else if (item.inputType.equals(itemView.getContext().getString(R.string.timer_camel_case))) {
                seekBar.setMax((float) item.highest);
                seekBar.setMin((float) item.lowest);
                seekBar.setProgress((float) item.average);
                unitString = itemView.getContext().getString(R.string.unit_time_sing);
            } else if (item.inputType.equals(itemView.getContext().getString(R.string.baactivity_camel_case))) {
                seekBar.setMax((float) item.highest);
                seekBar.setMin((float) item.lowest);
                seekBar.setProgress((float) item.average);
                if (item.average <= 1) {
                    unitString = itemView.getContext().getString(R.string.unit_activity_sing);
                } else {
                    unitString = itemView.getContext().getString(R.string.unit_activity_plur);
                }
            }
            seekBar.setIndicatorTextFormat("${PROGRESS} " + unitString);
        }

        @Override
        public void unbindView(InsightBodyAverageItem item) {
        }
    }
}

