package com.ivorybridge.moabi.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.ui.adapter.StatsFragmentPagerAdapter;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StatsFragment extends Fragment {

    @BindView(R.id.fragment_stats_sliding_tabs)
    TabLayout tabLayout;
    @BindView(R.id.fragment_stats_viewpager)
    ViewPager viewPager;
    private DataInUseViewModel dataInUseViewModel;
    private Boolean isStarted = false;
    private Boolean isVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_stats, container, false);
        ButterKnife.bind(this, mView);
        loadFragments();
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void loadFragments() {
        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);
        dataInUseViewModel.getAllInputsInUse().observe(this, new Observer<List<InputInUse>>() {
            @Override
            public void onChanged(@Nullable List<InputInUse> inputInUses) {
                if (inputInUses != null) {
                    List<String> inputsInUseList = new ArrayList<>();
                    for (InputInUse inputInUse : inputInUses) {
                        if (inputInUse.isInUse()) {
                            if (inputInUse.getName().equals(getString(R.string.mood_and_energy_camel_case))) {
                                inputsInUseList.add("201" + getString(R.string.mood_camel_case));
                                inputsInUseList.add("202" + getString(R.string.energy_camel_case));
                            } else if (inputInUse.getName().equals(getString(R.string.baactivity_camel_case))) {
                                inputsInUseList.add("207" + getString(R.string.baactivity_camel_case));
                            } else if (inputInUse.getName().equals(getString(R.string.phone_usage_camel_case))) {
                                inputsInUseList.add("210" + getString(R.string.phone_usage_camel_case));
                            } else if (inputInUse.getName().equals(getString(R.string.fitbit_camel_case))) {
                                inputsInUseList.add("100" + getString(R.string.fitbit_camel_case));
                            } else if (inputInUse.getName().equals(getString(R.string.googlefit_camel_case))) {
                                inputsInUseList.add("111" + getString(R.string.googlefit_camel_case));
                            } else if (inputInUse.getName().equals(getString(R.string.timer_camel_case))) {
                                inputsInUseList.add("208" + getString(R.string.timer_camel_case));
                            } else if (inputInUse.getName().equals(getString(R.string.stress_camel_case))) {
                                inputsInUseList.add("204" + getString(R.string.stress_camel_case));
                            } /*else if (inputInUse.getName().equals(getString(R.string.pain_camel_case))) {
                                inputsInUseList.add("205 Pain");
                            } */else if (inputInUse.getName().equals(getString(R.string.daily_review_camel_case))) {
                                inputsInUseList.add("200" + getString(R.string.daily_review_camel_case));
                            } else if (inputInUse.getName().equals(getString(R.string.moabi_tracker_camel_case))) {
                                inputsInUseList.add("000" + getString(R.string.moabi_tracker_camel_case));
                            } else if (inputInUse.getName().equals(getString(R.string.weather_camel_case))) {
                                inputsInUseList.add("999" + getString(R.string.weather_camel_case));
                            } else if (inputInUse.getName().equals(getString(R.string.depression_phq9_camel_case))) {
                                inputsInUseList.add("205" + getString(R.string.depression_phq9_camel_case));
                            } else if (inputInUse.getName().equals(getString(R.string.anxiety_gad7_camel_case))) {
                                inputsInUseList.add("206" + getString(R.string.anxiety_gad7_camel_case));
                            }
                        }
                    }
                    Collections.sort(inputsInUseList);
                    StatsFragmentPagerAdapter fragmentPagerAdapter = new StatsFragmentPagerAdapter(getChildFragmentManager(), inputsInUseList, getContext());
                    viewPager.setAdapter(fragmentPagerAdapter);
                    viewPager.setOffscreenPageLimit(1);
                    tabLayout.setupWithViewPager(viewPager);
                }
            }
        });
    }
}
