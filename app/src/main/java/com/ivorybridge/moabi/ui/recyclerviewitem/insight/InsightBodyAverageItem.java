package com.ivorybridge.moabi.ui.recyclerviewitem.insight;

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
    private double average;
    private Map<String, Double> activitySummaryMap;

    public InsightBodyAverageItem() {

    }

    public InsightBodyAverageItem(String inputType, double average) {
        this.inputType = inputType;
        this.average = average;
    }

    public InsightBodyAverageItem(String inputType, Map<String, Double> activitySummaryMap) {
        this.inputType = inputType;
        this.activitySummaryMap = activitySummaryMap;
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

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final InsightBodyAverageItem item, List<Object> payloads) {
            if (item.activitySummaryMap != null) {
                double lowest = 0;
                double highest = 0;
                double total = 0;
                int i = 0;
                for (Map.Entry<String, Double> entry: item.activitySummaryMap.entrySet()) {
                    if (i == 0) {
                        lowest = entry.getValue();
                        highest = entry.getValue();
                    } else {
                        if (entry.getValue() < lowest) {
                            lowest = entry.getValue();
                        }
                        if (entry.getValue() > highest) {
                            highest = entry.getValue();
                        }
                    }
                    total += entry.getValue();
                    i++;
                }
                seekBar.setMin((float) lowest);
                seekBar.setMax((float) highest);
                seekBar.setProgress((float) total / item.activitySummaryMap.size());
                if (item.inputType.equals(itemView.getContext().getString(R.string.activity_steps_title))) {
                    seekBar.setIndicatorTextFormat("${PROGRESS} steps");
                } else if (item.inputType.contains("Minutes") || item.inputType.equals(itemView.getContext().getString(R.string.activity_sleep_title))) {
                    seekBar.setIndicatorTextFormat("${PROGRESS} mins");
                } else if (item.inputType.equals(itemView.getContext().getString(R.string.activity_distance_title))) {
                    seekBar.setIndicatorTextFormat("${PROGRESS} km");
                } else if (item.inputType.equals(itemView.getContext().getString(R.string.activity_calories_title))) {
                    seekBar.setIndicatorTextFormat("${PROGRESS} Cal");
                } else if (item.inputType.equals(itemView.getContext().getString(R.string.activity_floors_title))) {
                    seekBar.setIndicatorTextFormat("${PROGRESS} floors");
                }

            } else {
                seekBar.setProgress((float) item.average);
            }
        }

        @Override
        public void unbindView(InsightBodyAverageItem item) {
        }
    }
}

