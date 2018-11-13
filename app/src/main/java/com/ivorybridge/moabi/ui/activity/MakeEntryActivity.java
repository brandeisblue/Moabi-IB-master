package com.ivorybridge.moabi.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.tabs.TabLayout;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.ui.adapter.MakeEntryFragmentPagerAdapter;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MakeEntryActivity extends AppCompatActivity {

    private static final String TAG = MakeEntryActivity.class.getSimpleName();
    @BindView(R.id.activity_make_entry_sliding_tabs)
    TabLayout tabLayout;
    @BindView(R.id.activity_make_entry_viewpager)
    ViewPager viewPager;
    @BindView(R.id.activity_make_entry_toolbar_backbutton)
    ImageButton backButton;
    @BindView(R.id.activity_make_entry_toolbar_checkbutton)
    ImageButton checkButton;
    private MakeEntryFragmentPagerAdapter fragmentPagerAdapter;
    private DataInUseViewModel dataInUseViewModel;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_entry);
        ButterKnife.bind(this);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MakeEntryActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);
        dataInUseViewModel.getAllInputsInUse().observe(this, new Observer<List<InputInUse>>() {
            @Override
            public void onChanged(List<InputInUse> inputInUses) {
                if (inputInUses != null) {
                    List<String> userInputsInUseList = new ArrayList<>();
                    Log.i(TAG, inputInUses.toString());
                    for (InputInUse inUse: inputInUses) {
                        if (inUse.getName().equals(getString(R.string.mood_and_energy_camel_case))
                                || inUse.getName().equals(getString(R.string.baactivity_camel_case))
                                || inUse.getName().equals(getString(R.string.stress_camel_case))
                                || inUse.getName().equals(getString(R.string.pain_camel_case))
                                || inUse.getName().equals(getString(R.string.daily_review_camel_case))
                                || inUse.getName().equals(getString(R.string.depression_phq9_camel_case))
                                || inUse.getName().equals(getString(R.string.anxiety_gad7_camel_case))) {
                            Boolean isChecked = (Boolean) inUse.isInUse();
                            if (isChecked != null) {
                                if (isChecked) {
                                    userInputsInUseList.add(inUse.getName());
                                }
                            }
                        }
                    }
                    Log.i(TAG, userInputsInUseList.toString());
                    Collections.sort(userInputsInUseList);
                    if (userInputsInUseList.size() < 1) {
                        Intent intent = new Intent(MakeEntryActivity.this, EditSurveyItemsActivity.class);
                        startActivity(intent);
                    } else {
                        fragmentPagerAdapter = new MakeEntryFragmentPagerAdapter(getApplicationContext(), getSupportFragmentManager(), userInputsInUseList);
                        viewPager.setAdapter(fragmentPagerAdapter);
                        tabLayout.setupWithViewPager(viewPager);
                    }
                }
            }
        });
        //fragmentPagerAdapter = new MakeEntryFragmentPagerAdapter(getApplicationContext(), getSupportFragmentManager(), );
        //viewPager.setAdapter(fragmentPagerAdapter);
        //tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private int fetchColor(@ColorRes int color) {
        return ContextCompat.getColor(this, color);
    }
}
