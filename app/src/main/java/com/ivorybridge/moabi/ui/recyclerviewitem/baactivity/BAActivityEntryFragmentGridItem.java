package com.ivorybridge.moabi.ui.recyclerviewitem.baactivity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityFavorited;
import com.ivorybridge.moabi.ui.activity.EditActivitiesActivity;
import com.ivorybridge.moabi.ui.views.SquareLayout;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BAActivityEntryFragmentGridItem extends AbstractItem<BAActivityEntryFragmentGridItem, BAActivityEntryFragmentGridItem.ViewHolder> {

    private static final String TAG = BAActivityEntryFragmentGridItem.class.getSimpleName();

    private BAActivityFavorited activityFavorited;
    private Boolean isEntered = false;
    private String name;
    private Long activityType;


    public BAActivityEntryFragmentGridItem(BAActivityFavorited data) {
        this.activityFavorited = data;
        this.name = data.getName();
        this.activityType = data.getActivtyType();
    }

    public BAActivityEntryFragmentGridItem() {
    }

    public String getName() {
        return this.name;
    }

    public Boolean isEntered() {
        return this.isEntered;
    }

    public BAActivityFavorited getActivityFavorited() {
        return this.activityFavorited;
    }

    public Long getActivityType() {
        return activityType;
    }

    @Override
    public int getType() {
        return R.id.baactivity_rv_griditem;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_baactivity_rv_griditem;
    }

    @NonNull
    @Override
    public BAActivityEntryFragmentGridItem.ViewHolder getViewHolder(View v) {
        return new BAActivityEntryFragmentGridItem.ViewHolder(v);
    }

    public static class ViewHolder extends FastAdapter.ViewHolder<BAActivityEntryFragmentGridItem> {

        @BindView(R.id.rv_item_baactivity_rv_griditem_squarelayout)
        SquareLayout container;
        @BindView(R.id.rv_item_baactivity_rv_griditem_textview)
        TextView name;
        @BindView(R.id.rv_item_baactivity_rv_griditem_imageview)
        ImageView image;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }



        @Override
        public void bindView(final BAActivityEntryFragmentGridItem item, List<Object> payloads) {
            image.setImageDrawable(null);
            if (item.activityFavorited != null) {
                name.setText(item.activityFavorited.getName());
                Log.i(TAG, " " + item.activityFavorited.getResourceID());
                //Drawable drawable = ContextCompat.getDrawable(itemView.getContext(), item.activityFavorited.getResourceID());
                //image.setImageResource(item.activityInDB.getResourceID());
                //image.setImageDrawable(drawable);
                loadImage(item.activityFavorited.getResourceID(), image);

                if (item.activityFavorited.getName().equals(itemView.getContext().getString(R.string.edit_title))) {
                    container.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(itemView.getContext(), EditActivitiesActivity.class);
                            itemView.getContext().startActivity(intent);
                        }
                    });
                } else {
                    if (item.isEntered) {
                        //Log.i(TAG, item.activityInLibrary.getName() + " is checked");
                        //container.setSelected(true);
                        container.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                        name.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                        //Log.i(TAG, item.name + " is selected");
                    } else {
                        container.setPressed(false);
                        name.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
                        container.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                    }

                    container.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (item.isEntered) {
                                item.isEntered = false;
                                v.setSelected(false);
                                name.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
                                v.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                                //activityRepository.deleteFavoritedActivity(item.activityInLibrary.getName());
                            } else {
                                item.isEntered = true;
                                v.setSelected(true);
                                name.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                                v.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                                BAActivityFavorited activityInUse = new BAActivityFavorited();
                            }
                        }
                    });
                    /*
                    container.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (item.isEntered) {
                                item.isEntered = false;
                                container.setSelected(false);
                                container.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.white));
                                firebaseManager.getActivityTodayRef().child("" + item.currentTime).child(item.activityFavorited.getName()).setValue(0L);
                                firebaseManager.getActivityLast30DaysTodayRef().child("" + item.currentTime).child(item.activityFavorited.getName()).setValue(0L);
                                activityRepository.deleteActivityEntry(item.currentTime, item.activityFavorited.getName());
                            } else {
                                item.isEntered = true;
                                container.setSelected(true);
                                container.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.transparent_gray));
                                firebaseManager.getActivityTodayRef().child("" + item.currentTime).child(item.activityFavorited.getName()).setValue(1L);
                                firebaseManager.getActivityLast30DaysTodayRef().child("" + item.currentTime).child(item.activityFavorited.getName()).setValue(1L);
                                activityRepository.insertActivityEntry(
                                        new BAActivityEntry(item.activityFavorited.getName(),
                                                item.currentTime,
                                                item.activityFavorited.getActivtyType()));
                            }
                        }
                    });*/
                }
            }

        }

        @Override
        public void unbindView(BAActivityEntryFragmentGridItem item) {
            //personalCustomUserInputsInUseRef.removeEventListener(valueEventListener);
        }

        private void loadImage(String mImageName, ImageView mImageIcon){
            int resID = itemView.getContext().getResources().getIdentifier(mImageName , "drawable", itemView.getContext().getPackageName());
            if(resID!=0) {//The associated resource identifier. Returns 0 if no such resource was found. (0 is not a valid resource ID.)
                mImageIcon.setImageResource(resID);
            }
        }
    }
}
