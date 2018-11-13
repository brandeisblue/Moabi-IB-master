package com.ivorybridge.moabi.ui.recyclerviewitem.activitytracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.ivorybridge.moabi.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ActivityTrackerItem extends AbstractItem<ActivityTrackerItem, ActivityTrackerItem.ViewHolder> {

    private static final String TAG = ActivityTrackerItem.class.getSimpleName();
    private Context mContext;
    private AppCompatActivity mActivity;
    private Fragment mFragment;
    private Map<String, Boolean> mIsUsedMap;
    private List<String> inputMethodsWithDataList;
    private String mDate;
    private String unit;


    public ActivityTrackerItem(Context context, AppCompatActivity activity, Fragment fragment,
                               List<String> inputMethodsWithDataList, String today, String unit) {
        mFragment = fragment;
        mActivity = activity;
        mContext = context;
        this.inputMethodsWithDataList = inputMethodsWithDataList;
        mDate = today;
        this.unit = unit;
    }

    @Override
    public int getType() {
        return R.id.activitytracker_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_activitytracker;
    }

    @NonNull
    @Override
    public ActivityTrackerItem.ViewHolder getViewHolder(View v) {
        return new ActivityTrackerItem.ViewHolder(v);
    }

    // Manually create the ViewHolder class
    protected static class ViewHolder extends FastAdapter.ViewHolder<ActivityTrackerItem>
            implements View.OnClickListener {

        @BindView(R.id.rv_item_activitytracker_relativelayout)
        RelativeLayout activityTrackerRL;
        @BindView(R.id.rv_item_activitytracker_horizontal_rv)
        RecyclerView horizontalRecyclerView;
        @BindView(R.id.rv_item_activitytracker_rightarrow_indicator)
        ImageButton rightButton;
        @BindView(R.id.rv_item_activitytracker_leftarrow_indicator)
        ImageButton leftButton;
        @BindView(R.id.rv_item_activitytracker_menu_imagebutton)
        ImageButton menuButton;
        private FastAdapter<IItem> mRecyclerAdapter;
        private ItemAdapter<FitbitItem> mFitbitAdapter;
        private ItemAdapter<GoogleFitItem> mGoogleFitAdapter;
        private ItemAdapter<BuiltInTrackerItem> builtInTrackerItemItemAdapter;
        private LinearLayoutManager horizontalLayoutManager;
        private SharedPreferences unitSharedPreferences;
        private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final ActivityTrackerItem item, List<Object> payloads) {

            //set up Fitbit Adapter
            mFitbitAdapter = new ItemAdapter<>();
            mGoogleFitAdapter = new ItemAdapter<>();
            builtInTrackerItemItemAdapter = new ItemAdapter<>();
            // populate carousel adapter with adapters for different views
            mRecyclerAdapter = FastAdapter.with(Arrays.asList(builtInTrackerItemItemAdapter, mFitbitAdapter, mGoogleFitAdapter));
            //mRecyclerAdapter = FastAdapter.with(Arrays.asList(mEmptyAdapter));
            //mRecyclerAdapter = FastAdapter.with(Arrays.asList(mGoogleFitAdapter));
            //mRecyclerAdapter.withSelectable(true);

            // set up carousel
            horizontalLayoutManager = new LinearLayoutManager(item.mContext,
                    LinearLayoutManager.HORIZONTAL, false);
            horizontalRecyclerView.setLayoutManager(horizontalLayoutManager);
            horizontalRecyclerView.setAdapter(mRecyclerAdapter);
            //recyclerViewIndicator.attachToRecyclerView(horizontalRecyclerView);

            // add pager behavior
            final PagerSnapHelper snapHelper = new PagerSnapHelper();
            horizontalRecyclerView.setOnFlingListener(null);
            snapHelper.attachToRecyclerView(horizontalRecyclerView);

            Log.i(TAG, item.inputMethodsWithDataList.toString());
            if (item.inputMethodsWithDataList != null) {
                Log.i(TAG, item.inputMethodsWithDataList.toString());
                for (String service : item.inputMethodsWithDataList) {
                    if (service.equals(itemView.getContext().getString(R.string.moabi_tracker_camel_case))) {
                        if (item.mFragment != null) {
                            builtInTrackerItemItemAdapter.add(new BuiltInTrackerItem(item.mFragment, item.mDate, item.unit));
                        }
                    }
                    if (service.equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                        if (item.mFragment != null) {
                            mFitbitAdapter.add(new FitbitItem(item.mFragment, item.mDate, item.unit));
                        }
                    }
                    if (service.equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                        if (item.mFragment != null) {
                            mGoogleFitAdapter.add(new GoogleFitItem(item.mFragment, item.mDate, item.unit));
                        }
                    }

                }
            }

            //Log.i(TAG, "Item count: " + horizontalLayoutManager.getInitialPrefetchItemCount());
            Log.i(TAG, "Item count: " + horizontalLayoutManager.getItemCount());
            if (horizontalLayoutManager.getItemCount() > 1) {
                leftButton.setVisibility(View.INVISIBLE);
                rightButton.setVisibility(View.VISIBLE);
            } else {
                rightButton.setVisibility(View.INVISIBLE);
                leftButton.setVisibility(View.INVISIBLE);
            }

            horizontalRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        rightButton.setVisibility(View.GONE);
                        leftButton.setVisibility(View.GONE);
                    } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        Log.i(TAG, "RecyclerView position: " + horizontalLayoutManager.findFirstVisibleItemPosition());
                        int position = horizontalLayoutManager.findFirstVisibleItemPosition();
                        if (horizontalLayoutManager.getItemCount() > 1) {
                            if (position == horizontalLayoutManager.getItemCount() - 1) {
                                rightButton.setVisibility(View.GONE);
                                leftButton.setVisibility(View.VISIBLE);
                            } else if (position == 0) {
                                rightButton.setVisibility(View.VISIBLE);
                                leftButton.setVisibility(View.GONE);
                            } else {
                                rightButton.setVisibility(View.VISIBLE);
                                leftButton.setVisibility(View.VISIBLE);
                            }
                        }
                        //setUpSettingButton(position, settingButton, activitySelectionViewModel);
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });


            //setUpSettingButton(0, settingButton, activitySelectionViewModel);
            leftButton.setOnClickListener(this);
            rightButton.setOnClickListener(this);

            /*
            leftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Right button clicked");
                    horizontalRecyclerView.smoothScrollToPosition(horizontalLayoutManager.findLastVisibleItemPosition() + 1);
                }
            });*/


            //mRecyclerAdapter.notifyAdapterDataSetChanged();
            //recyclerViewIndicator.setRecyclerView(horizontalRecyclerView);
            //recyclerViewIndicator.attachTo(horizontalRecyclerView);
        }

        @Override
        public void unbindView(ActivityTrackerItem item) {
            activityTrackerRL.setOnClickListener(null);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rv_item_activitytracker_leftarrow_indicator:
                    Log.i(TAG, "Left button clicked");
                    if (horizontalLayoutManager.findFirstVisibleItemPosition() > 0) {
                        horizontalRecyclerView.smoothScrollToPosition(horizontalLayoutManager.findFirstVisibleItemPosition() - 1);
                        leftButton.setVisibility(View.INVISIBLE);
                    } else {
                        horizontalRecyclerView.smoothScrollToPosition(0);
                        leftButton.setVisibility(View.INVISIBLE);
                    }
                    break;
                case R.id.rv_item_activitytracker_rightarrow_indicator:
                    Log.i(TAG, "Right button clicked");
                    rightButton.setVisibility(View.INVISIBLE);
                    horizontalRecyclerView.smoothScrollToPosition(horizontalLayoutManager.findLastVisibleItemPosition() + 1);
                    break;
            }
        }
    }
}
