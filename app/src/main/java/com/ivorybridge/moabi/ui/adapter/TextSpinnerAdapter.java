package com.ivorybridge.moabi.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ivorybridge.moabi.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TextSpinnerAdapter extends ArrayAdapter<String> {

    String[] spinnerTitles;
    Context mContext;

    public TextSpinnerAdapter(@NonNull Context context, String[] titles) {
        super(context, R.layout.spinner_item_with_icon);
        this.spinnerTitles = titles;
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
            convertView = mInflater.inflate(R.layout.spinner_item_without_icon, parent, false);
            mViewHolder.name = (TextView) convertView.findViewById(R.id.spinner_item_without_icon_textview);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.name.setText(spinnerTitles[position]);

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
    }
}
