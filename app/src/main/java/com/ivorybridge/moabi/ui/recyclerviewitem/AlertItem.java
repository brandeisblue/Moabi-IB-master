package com.ivorybridge.moabi.ui.recyclerviewitem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.activity.ConnectServicesActivity;
import com.ivorybridge.moabi.ui.activity.EditSurveyItemsActivity;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AlertItem extends AbstractItem<AlertItem, AlertItem.ViewHolder> {

    private static final String TAG = AlertItem.class.getSimpleName();

    public AlertItem() {

    }

    @Override
    public int getType() {
        return R.id.alert_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_alert;
    }

    @NonNull
    @Override
    public AlertItem.ViewHolder getViewHolder(View v) {
        return new AlertItem.ViewHolder(v);
    }

    // Manually create the ViewHolder class
    protected static class ViewHolder extends FastAdapter.ViewHolder<AlertItem> {

        @BindView(R.id.rv_item_alert_action_1_button)
        MaterialButton button1;
        @BindView(R.id.rv_item_alert_action_2_button)
        MaterialButton button2;
        @BindView(R.id.rv_item_alert_action_3_button)
        MaterialButton button3;
        SharedPreferences todayFragSharedPreferences;
        SharedPreferences.Editor todayFragSPEditor;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final AlertItem item, List<Object> payloads) {
            todayFragSharedPreferences = itemView.getContext().getSharedPreferences(
                    itemView.getContext().getString(R.string.com_ivorybridge_mobai_TODAY_FRAG_SHARED_PREFERENCE),
                    Context.MODE_PRIVATE);
            todayFragSPEditor = todayFragSharedPreferences.edit();
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(), ConnectServicesActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(), EditSurveyItemsActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });
            button3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    todayFragSPEditor.putBoolean(itemView.getContext().getString(R.string.tut_lets_get_started_boolean), true);
                    todayFragSPEditor.commit();
                }
            });
        }

        @Override
        public void unbindView(AlertItem item) {
        }
    }
}
