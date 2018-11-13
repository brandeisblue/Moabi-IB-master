package com.ivorybridge.moabi.ui.adapter;

import android.content.Context;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.fragment.InsightBodyFragment;
import com.ivorybridge.moabi.ui.fragment.InsightMindFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class InsightFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = InsightFragmentPagerAdapter.class.getSimpleName();
    private Context context;


    public InsightFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new InsightMindFragment();
        } else {
            return new InsightBodyFragment();
        }
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return 2;
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.mind_insight_title);
        } else {
            return context.getString(R.string.body_insight_title);
        }
    }
}