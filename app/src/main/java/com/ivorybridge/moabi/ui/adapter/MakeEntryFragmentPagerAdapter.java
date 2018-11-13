package com.ivorybridge.moabi.ui.adapter;

import android.content.Context;
import android.util.Log;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.fragment.BAActivityEntryFragment;
import com.ivorybridge.moabi.ui.fragment.Gad7EntryFragment;
import com.ivorybridge.moabi.ui.fragment.MoodEntryFragment;
import com.ivorybridge.moabi.ui.fragment.DailyReviewEntryFragment;
import com.ivorybridge.moabi.ui.fragment.PainEntryFragment;
import com.ivorybridge.moabi.ui.fragment.Phq9EntryFragment;
import com.ivorybridge.moabi.ui.fragment.StressEntryFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MakeEntryFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = MakeEntryFragmentPagerAdapter.class.getSimpleName();
    private List<String> selectedSurveys = new ArrayList<>();
    private List<Fragment> fragmentsToShow;
    private Context context;


    public MakeEntryFragmentPagerAdapter(Context context, FragmentManager fm, List<String> surveyList) {
        super(fm);
        this.context = context;
        selectedSurveys = surveyList;
        fragmentsToShow = new ArrayList<>();
        Log.i(TAG, selectedSurveys.toString());
        for (String survey: selectedSurveys) {
            if (survey.equals(context.getString(R.string.baactivity_camel_case))) {
                fragmentsToShow.add(new BAActivityEntryFragment());
            } else if (survey.equals(context.getString(R.string.mood_and_energy_camel_case))) {
                fragmentsToShow.add(new MoodEntryFragment());
            } else if (survey.equals(context.getString(R.string.stress_camel_case))) {
                fragmentsToShow.add(new StressEntryFragment());
            } else if (survey.equals(context.getString(R.string.pain_camel_case))) {
                fragmentsToShow.add(new PainEntryFragment());
            } else if (survey.equals(context.getString(R.string.daily_review_camel_case))) {
                fragmentsToShow.add(new DailyReviewEntryFragment());
            } else if (survey.equals(context.getString(R.string.depression_phq9_camel_case))) {
                fragmentsToShow.add(new Phq9EntryFragment());
            } else if (survey.equals(context.getString(R.string.anxiety_gad7_camel_case))) {
                fragmentsToShow.add(new Gad7EntryFragment());
            }
        }
        Log.i(TAG, "# of fragments: " + fragmentsToShow.size());
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        if (fragmentsToShow.size() > 0) {
            return fragmentsToShow.get(position);
        } else {
            return null;
        }
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return selectedSurveys.size();
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        if (selectedSurveys.size() > 0) {
            if (selectedSurveys.get(position).equals(context.getString(R.string.baactivity_camel_case))) {
                return context.getString(R.string.baactivity_title);
            } else if (selectedSurveys.get(position).equals(context.getString(R.string.mood_and_energy_camel_case))) {
                return context.getString(R.string.mood_and_energy_title);
            } else if (selectedSurveys.get(position).equals(context.getString(R.string.stress_camel_case))) {
                return context.getString(R.string.stress_title);
            } else if (selectedSurveys.get(position).equals(context.getString(R.string.pain_camel_case))) {
                return context.getString(R.string.pain_title);
            } else if (selectedSurveys.get(position).equals(context.getString(R.string.daily_review_camel_case))) {
                return context.getString(R.string.daily_review_title);
            } else if (selectedSurveys.get(position).equals(context.getString(R.string.depression_phq9_camel_case))) {
                return context.getString(R.string.depression_phq9_title);
            } else if (selectedSurveys.get(position).equals(context.getString(R.string.anxiety_gad7_camel_case))) {
                return context.getString(R.string.anxiety_gad7_title);
            } else {
                return "";
            }
        } else {
            return "";
        }
    }
}
