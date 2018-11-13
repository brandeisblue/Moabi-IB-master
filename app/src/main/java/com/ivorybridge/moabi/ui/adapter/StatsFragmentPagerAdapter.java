package com.ivorybridge.moabi.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.fragment.StatsNonTrackerFragment;
import com.ivorybridge.moabi.ui.fragment.StatsTrackerFragment;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class StatsFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = StatsFragmentPagerAdapter.class.getSimpleName();
    private List<String> inputsInUseList;
    private Context context;


    public StatsFragmentPagerAdapter(FragmentManager fm, List<String> inputsInUseList, Context context) {
        super(fm);
        this.inputsInUseList = inputsInUseList;
        this.context = context;
        Log.i(TAG, inputsInUseList.toString());
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        String string = inputsInUseList.get(position).substring(3);
        string = string.trim();
        bundle.putString("inputType", string);
        if (inputsInUseList.get(position).equals("100" + context.getString(R.string.fitbit_camel_case)) ||
                inputsInUseList.get(position).equals("111" + context.getString(R.string.googlefit_camel_case)) ||
                inputsInUseList.get(position).equals("000" + context.getString(R.string.moabi_tracker_camel_case)) ||
                inputsInUseList.get(position).equals("999" + context.getString(R.string.weather_camel_case)) ||
                inputsInUseList.get(position).equals("208" + context.getString(R.string.timer_camel_case))) {
            StatsTrackerFragment fragment = new StatsTrackerFragment();
            fragment.setArguments(bundle);
            return fragment;
        } else {
            StatsNonTrackerFragment fragment = new StatsNonTrackerFragment();
            fragment.setArguments(bundle);
            return fragment;
        }
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return inputsInUseList.size();
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        if (inputsInUseList.size() == 0) {
            return "Start Tracking To View Stats";
        }
        if (inputsInUseList.get(position).equals("210" + context.getString(R.string.phone_usage_camel_case))) {
            return context.getString(R.string.phone_usage_title);
        } else if (inputsInUseList.get(position).equals("100" + context.getString(R.string.fitbit_camel_case))) {
            return context.getString(R.string.fitbit_title);
        } else if (inputsInUseList.get(position).equals("111" + context.getString(R.string.googlefit_camel_case))) {
            return context.getString(R.string.googlefit_title);
        } else if (inputsInUseList.get(position).equals("201" + context.getString(R.string.mood_camel_case))) {
            return context.getString(R.string.mood_title);
        } else if (inputsInUseList.get(position).equals("202" + context.getString(R.string.energy_camel_case))) {
            return context.getString(R.string.energy_title);
        } else if (inputsInUseList.get(position).equals("207" + context.getString(R.string.baactivity_camel_case))) {
            return context.getString(R.string.baactivity_title);
        } else if (inputsInUseList.get(position).equals("208" + context.getString(R.string.timer_camel_case))) {
            return context.getString(R.string.timer_title);
        } else if (inputsInUseList.get(position).equals("204" + context.getString(R.string.stress_camel_case))) {
            return context.getString(R.string.stress_title);
        } else if (inputsInUseList.get(position).equals("205" + context.getString(R.string.pain_camel_case))) {
            return context.getString(R.string.pain_title);
        } else if (inputsInUseList.get(position).equals("200" + context.getString(R.string.daily_review_camel_case))) {
            return context.getString(R.string.daily_review_title);
        } else if (inputsInUseList.get(position).equals("000" + context.getString(R.string.moabi_tracker_camel_case))) {
            return context.getString(R.string.moabi_tracker_title);
        } else if (inputsInUseList.get(position).equals("999" + context.getString(R.string.weather_camel_case))) {
            return context.getString(R.string.weather_title);
        } else if (inputsInUseList.get(position).equals("205" + context.getString(R.string.depression_phq9_camel_case))) {
            return context.getString(R.string.depression_phq9_title);
        } else if (inputsInUseList.get(position).equals("206" + context.getString(R.string.anxiety_gad7_camel_case))) {
            return context.getString(R.string.anxiety_gad7_title);
        } else {
            return "";
        }
    }
}
