package com.ivorybridge.moabi.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.adapter.FragmentAdapter;
import com.ivorybridge.moabi.ui.fragment.IntroFragment;
import com.ivorybridge.moabi.ui.fragment.SettingsFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class IntroActivity extends AppCompatActivity {

    @BindView(R.id.activity_intro_viewpager)
    ViewPager viewPager;
    @BindView(R.id.activity_intro_indicator)
    ScrollingPagerIndicator indicator;
    @BindView(R.id.activity_intro_continue_as_guest_button)
    MaterialButton guestButton;
    @BindView(R.id.activity_intro_sign_in_button)
    MaterialButton signInButton;
    private FragmentAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);
        pagerAdapter = new FragmentAdapter(getSupportFragmentManager());
        SettingsFragment settingsFrag = new SettingsFragment();
        IntroFragment introFragment = new IntroFragment();
        //pagerAdapter.addFragments(settingsFrag);
        pagerAdapter.addFragments(introFragment);
        viewPager.setAdapter(pagerAdapter);
        indicator.attachToPager(viewPager);

        guestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }

}
