package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.adapter.InsightFragmentPagerAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class InsightFragment extends Fragment {

    @BindView(R.id.fragment_insight_sliding_tabs)
    TabLayout tabLayout;
    @BindView(R.id.fragment_insight_viewpager)
    ViewPager viewPager;
    private Boolean isStarted = false;
    private Boolean isVisible = false;

    private static final String TAG = InsightFragment.class.getSimpleName();
    private Context mContext;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        /*
        isStarted = true;
        if (isVisible && isStarted) {
            loadFragments();
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
        //isStarted = false;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        /*
        isVisible= isVisibleToUser;
        if (isStarted && isVisible) {
            loadFragments();
        }*/
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_insight, container, false);
        ButterKnife.bind(this, mView);
        loadFragments();
        return mView;
    }

    private void loadFragments() {
        InsightFragmentPagerAdapter fragmentPagerAdapter = new InsightFragmentPagerAdapter(getContext(), getChildFragmentManager());
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        tabLayout.setupWithViewPager(viewPager);

    }
}