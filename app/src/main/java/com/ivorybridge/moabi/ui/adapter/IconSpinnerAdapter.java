package com.ivorybridge.moabi.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivorybridge.moabi.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IconSpinnerAdapter extends ArrayAdapter<String> {

    String[] spinnerTitles;
    int[] spinnerImages;
    String[] spinnerPopulation;
    boolean hasMargin = true;
    Context mContext;

    public IconSpinnerAdapter(@NonNull Context context, String[] titles, int[] images) {
        super(context, R.layout.spinner_item_with_icon);
        this.spinnerTitles = titles;
        this.spinnerImages = images;
        this.mContext = context;
    }

    public IconSpinnerAdapter(@NonNull Context context, String[] titles, int[] images, boolean hasMargin) {
        super(context, R.layout.spinner_item_with_icon);
        this.spinnerTitles = titles;
        this.spinnerImages = images;
        this.mContext = context;
        this.hasMargin = hasMargin;
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
        if (convertView == null && hasMargin) {
            LayoutInflater mInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.spinner_item_with_icon, parent, false);
            mViewHolder.linearLayout = convertView.findViewById(R.id.spinner_item_with_icon_linearlayout);
            mViewHolder.icon = convertView.findViewById(R.id.spinner_item_with_icon_imageview);
            mViewHolder.name = convertView.findViewById(R.id.spinner_item_with_icon_textview);
            convertView.setTag(mViewHolder);
        } else if (convertView == null && !hasMargin) {
            LayoutInflater mInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.spinner_item_with_icon_no_start_margin, parent, false);
            mViewHolder.icon = convertView.findViewById(R.id.spinner_item_with_icon_no_margin_imageview);
            mViewHolder.name = convertView.findViewById(R.id.spinner_item_with_icon_no_margin_textview);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.icon.setImageResource(spinnerImages[position]);
        mViewHolder.name.setText(spinnerTitles[position]);
        return convertView;
    }

    private static class ViewHolder {
        LinearLayout linearLayout;
        ImageView icon;
        TextView name;
    }
}
