package com.ivorybridge.moabi.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivorybridge.moabi.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IconSpinnerAdapter extends ArrayAdapter<String> {

    String[] spinnerTitles;
    int[] spinnerImages;
    String[] spinnerPopulation;
    Context mContext;

    public IconSpinnerAdapter(@NonNull Context context, String[] titles, int[] images) {
        super(context, R.layout.spinner_item_with_icon);
        this.spinnerTitles = titles;
        this.spinnerImages = images;
        this.mContext = context;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return spinnerTitles.length;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder mViewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.spinner_item_with_icon, parent, false);
            mViewHolder.icon = (ImageView) convertView.findViewById(R.id.spinner_item_with_icon_imageview);
            mViewHolder.name = (TextView) convertView.findViewById(R.id.spinner_item_with_icon_textview);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.icon.setImageResource(spinnerImages[position]);
        mViewHolder.name.setText(spinnerTitles[position]);

        return convertView;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView name;
    }
}
