package com.ivorybridge.moabi.ui.recyclerviewitem.baactivity;

import android.app.Application;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.dao.BAActivityEntryDao;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityInLibrary;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.BAActivityRepository;
import com.ivorybridge.moabi.ui.views.SquareLayout;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BAActivityEditActivityGridItem extends AbstractItem<BAActivityEditActivityGridItem, BAActivityEditActivityGridItem.ViewHolder> {

    private static final String TAG = BAActivityEditActivityGridItem.class.getSimpleName();

    private BAActivityInLibrary activityInLibrary;
    private Boolean isFavorited = false;
    private Application application;
    private String name;

    public BAActivityEditActivityGridItem(BAActivityInLibrary data, Application application) {
        this.activityInLibrary = data;
        this.application = application;
        this.name = data.getName();
    }

    public BAActivityEditActivityGridItem(BAActivityInLibrary data, Boolean isFavorited, Application application) {
        this.activityInLibrary = data;
        this.isFavorited = isFavorited;
        this.application = application;
        this.name = data.getName();
    }

    public BAActivityEditActivityGridItem() {
    }


    public String getName() {
        return this.name;
    }

    public void setFavorited(boolean isFavorited) {
        this.isFavorited = isFavorited;
    }

    public Boolean isFavorited() {
        return this.isFavorited;
    }

    public void setFavorited(Boolean favorited) {
        isFavorited = favorited;
    }

    public BAActivityInLibrary getActivityInLibrary() {
        return this.activityInLibrary;
    }


    @Override
    public int getType() {
        return R.id.baactivity_rv_griditem;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_baactivity_rv_griditem;
    }

    @Override
    public String toString() {
        return this.getName() + ": " + this.isFavorited;
    }

    @NonNull
    @Override
    public BAActivityEditActivityGridItem.ViewHolder getViewHolder(View v) {
        return new BAActivityEditActivityGridItem.ViewHolder(v);
    }

    public static class ViewHolder extends FastAdapter.ViewHolder<BAActivityEditActivityGridItem> {

        @BindView(R.id.rv_item_baactivity_rv_griditem_squarelayout)
        SquareLayout container;
        @BindView(R.id.rv_item_baactivity_rv_griditem_textview)
        TextView name;
        @BindView(R.id.rv_item_baactivity_rv_griditem_imageview)
        ImageView image;
        FirebaseManager firebaseManager;
        private BAActivityEntryDao mDao;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }



        @Override
        public void bindView(final BAActivityEditActivityGridItem item, List<Object> payloads) {
            firebaseManager = new FirebaseManager();
            BAActivityRepository activityRepository = new BAActivityRepository(item.application);

            image.setImageDrawable(null);
            
            if (item.activityInLibrary != null) {
                name.setText(item.activityInLibrary.getName());
                //Log.i(TAG, "Activity id: " + item.activityInLibrary.getResourceID());
                //Drawable drawable = ContextCompat.getDrawable(itemView.getContext(), item.activityInLibrary.getResourceID());
                //image.setImageDrawable(drawable);
                loadImage(item.activityInLibrary.getResourceID(), image);


                if (item.isFavorited) {
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
                        if (item.isFavorited) {
                            item.isFavorited = false;
                            v.setSelected(false);
                            name.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
                            v.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                            //activityRepository.deleteFavoritedActivity(item.activityInLibrary.getName());
                        } else {
                            item.isFavorited = true;
                            v.setSelected(true);
                            v.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                            name.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                        }
                    }
                });
            }
        }

        @Override
        public void unbindView(BAActivityEditActivityGridItem item) {
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

