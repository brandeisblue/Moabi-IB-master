package com.ivorybridge.moabi.ui.activity;

import android.os.Bundle;
import android.view.View;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.adapter.FragmentAdapter;
import com.ivorybridge.moabi.ui.adapter.NoSwipePager;
import com.ivorybridge.moabi.ui.fragment.NotificationSettingsFragment;
import com.ivorybridge.moabi.ui.fragment.ProfileFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AdvancedSettingsActivity extends AppCompatActivity {

    @BindView(R.id.activity_settings_viewpager)
    NoSwipePager viewPager;
    @BindView(R.id.activity_settings_toolbar)
    Toolbar toolbar;
    private FragmentAdapter pagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        viewPager.setPagingEnabled(false);
        pagerAdapter = new FragmentAdapter(getSupportFragmentManager());
        if (getIntent().getStringExtra("settings_type") != null &&
                getIntent().getStringExtra("settings_type").equals(getString(R.string.settings_notifications_title))) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.settings_notifications_title));
            }
            NotificationSettingsFragment notificationSettingsFragment = new NotificationSettingsFragment();
            pagerAdapter.addFragments(notificationSettingsFragment);
        } else if (getIntent().getStringExtra("settings_type") != null &&
                getIntent().getStringExtra("settings_type").equals(getString(R.string.settings_profile_title))) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.settings_profile_title));
            }

            ProfileFragment profileFragment = new ProfileFragment();
            pagerAdapter.addFragments(profileFragment);
        }

        //SettingsFragment settingsFrag = new SettingsFragment();
        //DummyFragment2 dummyFragment2 = new DummyFragment2();
        //activityFrag.setArguments(bundle);
        //pagerAdapter.addFragments(settingsFrag);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(pagerAdapter);

    }
}
