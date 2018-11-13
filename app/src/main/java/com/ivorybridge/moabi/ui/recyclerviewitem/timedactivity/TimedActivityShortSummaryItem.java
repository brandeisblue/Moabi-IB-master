package com.ivorybridge.moabi.ui.recyclerviewitem.timedactivity;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.util.FormattedTime;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimedActivityShortSummaryItem extends AbstractItem<TimedActivityShortSummaryItem, TimedActivityShortSummaryItem.ViewHolder> {

    private static final String TAG = TimedActivityShortSummaryItem.class.getSimpleName();
    private TimedActivitySummary timedActivitySummary;


    public TimedActivityShortSummaryItem(TimedActivitySummary timedActivitySummary) {
        this.timedActivitySummary = timedActivitySummary;
    }

    @Override
    public int getType() {
        return R.id.timedactivity_short_rv_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_timedactivity_short_rv_item;
    }

    @NonNull
    @Override
    public TimedActivityShortSummaryItem.ViewHolder getViewHolder(View v) {
        return new TimedActivityShortSummaryItem.ViewHolder(v);
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<TimedActivityShortSummaryItem> {

        @BindView(R.id.rv_item_timedactivity_short_rv_item_name_textview)
        TextView nameTextView;
        @BindView(R.id.rv_item_timedactivity_short_rv_item_duration_textview)
        TextView durationTextView;
        @BindView(R.id.rv_item_timedactivity_short_rv_item_entrytime_textview)
        TextView entryTextView;
        private FormattedTime formattedTime;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final TimedActivityShortSummaryItem item, List<Object> payloads) {
            formattedTime = new FormattedTime();
            nameTextView.setText(item.timedActivitySummary.getInputName());
            long time = item.timedActivitySummary.getDuration();
            int h = (int) (time / 3600000);
            int m = (int) (time - h * 3600000) / 60000;
            int s = (int) (time - h * 3600000 - m * 60000) / 1000;
            durationTextView.setText(String.format(Locale.US, "%02d:%02d:%02d", h, m, s));
            //durationTextView.setText(formattedTime.convertLongToHHMMSS(item.timedActivitySummary.getDuration()));
            entryTextView.setText(formattedTime.convertLongToHHMM(item.timedActivitySummary.getDateInLong()));
        }

        @Override
        public void unbindView(TimedActivityShortSummaryItem item) {
            //personalCustomUserInputsInUseRef.removeEventListener(valueEventListener);
        }
    }
}

