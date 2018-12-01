package com.ivorybridge.moabi.ui.recyclerviewitem.insight;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.activity.ConnectServicesActivity;
import com.ivorybridge.moabi.ui.activity.MakeEntryActivity;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

public class InsightEmptyViewItem extends AbstractItem<InsightEmptyViewItem, InsightEmptyViewItem.ViewHolder> {

    private static final String TAG = InsightEmptyViewItem.class.getSimpleName();
    private String inputType;

    public InsightEmptyViewItem() {
    }

    public InsightEmptyViewItem(String inputType) {
        this.inputType = inputType;
    }



    @Override
    public int getType() {
        return R.id.insight_empty_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_insight_emptyview_item;
    }

    @NonNull
    @Override
    public InsightEmptyViewItem.ViewHolder getViewHolder(View v) {
        return new InsightEmptyViewItem.ViewHolder(v);
    }

    // Manually create the ViewHolder class
    protected static class ViewHolder extends FastAdapter.ViewHolder<InsightEmptyViewItem> {

        @BindView(R.id.rv_item_insight_emptyview_item_imageview)
        ImageView imageView;
        @BindView(R.id.rv_item_insight_emptyview_item_title_textview)
        TextView titleTextView;
        @BindView(R.id.rv_item_insight_emptyview_item_body_textview)
        TextView bodyTextView;
        @BindView(R.id.rv_item_insight_emptyview_item_button)
        Button button;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final InsightEmptyViewItem item, List<Object> payloads) {
            if (item.inputType.equals(itemView.getContext().getString(R.string.recommendation_camel_case))) {
                imageView.setImageResource(R.drawable.ic_analyzing);
                titleTextView.setText(itemView.getContext().getString(R.string.empty_insight_analysis_title));
                bodyTextView.setText(itemView.getContext().getString(R.string.empty_insight_analysis_body));
                button.setVisibility(View.INVISIBLE);
            } else if (item.inputType.equals(itemView.getContext().getString(R.string.survey_camel_case))){
                imageView.setImageResource(R.drawable.ic_make_entry);
                titleTextView.setText(itemView.getContext().getString(R.string.empty_insight_mind_title));
                bodyTextView.setText(itemView.getContext().getString(R.string.empty_insight_mind_body));
                button.setVisibility(View.VISIBLE);
                button.setText(itemView.getContext().getString(R.string.empty_view_survey_button));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(itemView.getContext(), MakeEntryActivity.class);
                        itemView.getContext().startActivity(intent);
                    }
                });
            } else if (item.inputType.equals(itemView.getContext().getString(R.string.apps_and_services_camel_case))){
                imageView.setImageResource(R.drawable.ic_connecting);
                titleTextView.setText(itemView.getContext().getString(R.string.empty_insight_body_title));
                bodyTextView.setText(itemView.getContext().getString(R.string.empty_insight_body_body));
                button.setVisibility(View.VISIBLE);
                button.setText(itemView.getContext().getString(R.string.empty_view_apps_and_services_button));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(itemView.getContext(), ConnectServicesActivity.class);
                        itemView.getContext().startActivity(intent);
                    }
                });
            }
        }

        @Override
        public void unbindView(InsightEmptyViewItem item) {
        }
    }
}


