package com.ivorybridge.moabi.ui.recyclerviewitem;

import android.content.Intent;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.activity.ConnectServicesActivity;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EmptyItem extends AbstractItem<EmptyItem, EmptyItem.ViewHolder> {

    private static final String TAG = EmptyItem.class.getSimpleName();

    public EmptyItem() {

    }

    @Override
    public int getType() {
        return R.id.empty_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_emptyview;
    }

    @NonNull
    @Override
    public EmptyItem.ViewHolder getViewHolder(View v) {
        return new EmptyItem.ViewHolder(v);
    }

    // Manually create the ViewHolder class
    protected static class ViewHolder extends FastAdapter.ViewHolder<EmptyItem> {

        @BindView(R.id.rv_item_emptyview_button)
        Button clickToStartButton;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final EmptyItem item, List<Object> payloads) {
            clickToStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(), ConnectServicesActivity.class);
                    intent.putExtra("redirected_from", "empty_view");
                    itemView.getContext().startActivity(intent);
                }
            });

        }

        @Override
        public void unbindView(EmptyItem item) {
        }
    }
}
