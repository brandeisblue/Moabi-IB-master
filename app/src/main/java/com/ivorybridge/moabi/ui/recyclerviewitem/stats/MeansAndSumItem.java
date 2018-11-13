package com.ivorybridge.moabi.ui.recyclerviewitem.stats;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.ivorybridge.moabi.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MeansAndSumItem extends AbstractItem<MeansAndSumItem, MeansAndSumItem.ViewHolder> {

    private static final String TAG = MeansAndSumItem.class.getSimpleName();
    private String name;
    private Double meansDouble;
    private Double totalDouble;
    private Long meansLong;
    private Long totalLong;

    public MeansAndSumItem(String name, Double means, Double total) {
        this.name = name;
        this.meansDouble = means;
        this.totalDouble = total;
    }

    public MeansAndSumItem(String name, Long means, Long total) {
        this.name = name;
        this.meansLong = means;
        this.totalLong = total;
    }

    @Override
    public int getType() {
        return R.id.meansandsum_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_stats_meansandsum_item;
    }

    @NonNull
    @Override
    public MeansAndSumItem.ViewHolder getViewHolder(View v) {
        return new MeansAndSumItem.ViewHolder(v);
    }

    // Manually create the ViewHolder class
    protected static class ViewHolder extends FastAdapter.ViewHolder<MeansAndSumItem> {

        @BindView(R.id.rv_item_stats_meansandsum_item_activitytype_textview)
        TextView activityTypeTextView;
        @BindView(R.id.rv_item_stats_meansandsum_item_means_textview)
        TextView meansTextView;
        @BindView(R.id.rv_item_stats_meansandsum_item_sum_textview)
        TextView sumTextView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final MeansAndSumItem item, List<Object> payloads) {
            activityTypeTextView.setText(item.name);
            if (item.meansLong != null) {
                meansTextView.setText("" + item.meansLong);
                sumTextView.setText("" + item.totalLong);
            } else {
                meansTextView.setText(String.format(Locale.US, "%.2f", item.meansDouble));
                sumTextView.setText(String.format(Locale.US, "%.2f", item.totalDouble));
            }
        }

        @Override
        public void unbindView(MeansAndSumItem item) {
        }
    }
}
