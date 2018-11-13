package com.ivorybridge.moabi.ui.recyclerviewitem.insight;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ivorybridge.moabi.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.List;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

public class InsightMindAverageItem extends AbstractItem<InsightMindAverageItem, InsightMindAverageItem.ViewHolder> {

    private static final String TAG = InsightMindAverageItem.class.getSimpleName();
    private String inputType;
    private double average;

    public InsightMindAverageItem() {
    }

    public InsightMindAverageItem(String inputType, double average) {
        this.inputType = inputType;
        this.average = average;
    }


    @Override
    public int getType() {
        return R.id.insight_mind_average_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_insight_mind_average_item;
    }

    @NonNull
    @Override
    public InsightMindAverageItem.ViewHolder getViewHolder(View v) {
        return new InsightMindAverageItem.ViewHolder(v);
    }

    // Manually create the ViewHolder class
    protected static class ViewHolder extends FastAdapter.ViewHolder<InsightMindAverageItem> {

        @BindView(R.id.rv_item_insight_mind_average_item_title_textview)
        TextView textView;
        @BindView(R.id.rv_item_insight_mind_average_item_seekbar)
        IndicatorSeekBar seekBar;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final InsightMindAverageItem item, List<Object> payloads) {
            if (item.inputType.equals(itemView.getContext().getString(R.string.mood_camel_case)) || item.inputType.equals(itemView.getContext().getString(R.string.energy_camel_case))) {
                String[] tickTexts = itemView.getContext().getResources().getStringArray(R.array.moodandenergy_seekbar_array);
                seekBar.setTickCount(2);
                seekBar.customTickTexts(tickTexts);
                seekBar.setMin(1);
                seekBar.setMax(3);
                seekBar.setProgress((float) item.average);
            } else if (item.inputType.equals(itemView.getContext().getString(R.string.stress_camel_case))) {
                String[] tickTexts = itemView.getContext().getResources().getStringArray(R.array.stress_seekbar_array);
                seekBar.setTickCount(2);
                seekBar.customTickTexts(tickTexts);
                seekBar.setMin(0);
                seekBar.setMax(10);
                seekBar.setProgress((float) item.average);
            } else if (item.inputType.equals(itemView.getContext().getString(R.string.daily_review_camel_case))) {
                String[] tickTexts = itemView.getContext().getResources().getStringArray(R.array.overall_review_seekbar_array);
                seekBar.setTickCount(2);
                seekBar.customTickTexts(tickTexts);
                seekBar.setMin(1);
                seekBar.setMax(5);
                seekBar.setProgress((float) item.average);
            } else if (item.inputType.equals(itemView.getContext().getString(R.string.depression_phq9_camel_case))) {
                String[] tickTexts = itemView.getContext().getResources().getStringArray(R.array.depression_seekbar_array);
                seekBar.setTickCount(28);
                seekBar.customTickTexts(tickTexts);
                seekBar.setMin(0);
                seekBar.setMax(27);
                seekBar.setProgress((float) item.average);
            } else if (item.inputType.equals(itemView.getContext().getString(R.string.anxiety_gad7_camel_case))) {
                String[] tickTexts = itemView.getContext().getResources().getStringArray(R.array.anxiety_seekbar_array);
                seekBar.setTickCount(22);
                seekBar.customTickTexts(tickTexts);
                seekBar.setMin(0);
                seekBar.setMax(21);
                seekBar.setProgress((float) item.average);
            }
            Log.i(TAG, "Progress is " + (float) item.average);
            Log.i(TAG, "Seekbar is at " + seekBar.getProgressFloat());
        }

        @Override
        public void unbindView(InsightMindAverageItem item) {
        }
    }
}
