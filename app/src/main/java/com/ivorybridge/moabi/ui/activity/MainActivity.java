package com.ivorybridge.moabi.ui.activity;

import android.animation.Animator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.repository.AsyncCallsMasterRepository;
import com.ivorybridge.moabi.service.MotionSensorService;
import com.ivorybridge.moabi.ui.adapter.FragmentAdapter;
import com.ivorybridge.moabi.ui.fragment.InsightFragment;
import com.ivorybridge.moabi.ui.fragment.StatsFragment;
import com.ivorybridge.moabi.ui.fragment.TodayFragment;
import com.ivorybridge.moabi.util.FormattedTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.preference.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.activity_main_fabBGLayout)
    View fabBGLayout;
    @BindView(R.id.activity_main_fab)
    FloatingActionButton mFab;
    @BindView(R.id.activity_main_make_entry_fab1)
    FloatingActionButton makeEntryFab1;
    @BindView(R.id.activity_main_track_activity_fab2)
    FloatingActionButton trackActivityFab2;
    @BindView(R.id.activity_main_edit_entry_types_fab3)
    FloatingActionButton editEntryTypesFab3;
    @BindView(R.id.activity_main_make_entry_linearlayout1)
    LinearLayout fab1LL;
    @BindView(R.id.activity_main_track_activity_linearlayout2)
    LinearLayout fab2LL;
    @BindView(R.id.activity_main_edit_entry_types_linearlayout3)
    LinearLayout fab3LL;
    @BindView(R.id.activity_main_bottom_navigation)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.activity_main_framelayout)
    FrameLayout fragContainer;
    Boolean isFABOpen = false;
    private FragmentAdapter pagerAdapter;
    private String mDate;
    private FirebaseAuth mAuth;
    private AsyncCallsMasterRepository asyncCallsMasterRepository;
    private FormattedTime formattedTime;
    private SharedPreferences lastFragmentSharedPreference;
    private SharedPreferences.Editor lastFragEditor;
    private Intent motionServiceIntent;
    private MotionSensorService motionSensorService;
    private int currentFragment = -1;

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //  Declare a new thread to do a preference check
        //  Make a new preferences editor
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor e = getPrefs.edit();
        //  Edit preference to make it false because we don't want this to run again
        e.putBoolean("firstStart", false);
        //  Apply changes
        e.apply();
        lastFragmentSharedPreference = this.getSharedPreferences(
                getString(R.string.com_ivorybridge_mobai_LAST_ACCESSED_FRAGMENT_IN_MAIN_ACTIVITY_SHARED_PREFERENCE_KEY)
                , Context.MODE_PRIVATE);
        lastFragEditor = lastFragmentSharedPreference.edit();
        isFABOpen = false;
        formattedTime = new FormattedTime();
        motionSensorService = new MotionSensorService();
        motionServiceIntent = new Intent(this, MotionSensorService.class);
        if (!isMyServiceRunning(motionSensorService.getClass())) {
            startService(motionServiceIntent);
        }
        fabBGLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFABMenu();
            }
        });
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
            }
        });

        // clear stack.
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
            finishAffinity();
        }
        Log.i(TAG, "onCreate Called");

        Intent dateIntent = getIntent();
        // if a user has selected a date, it will be passed as intent extra.
        if (dateIntent != null) {
            mDate = dateIntent.getStringExtra("date");
        }
        // if a user has not selected a date, pass today's date.
        if (mDate == null) {
            mDate = setUpDatesForToday();
        }

        fragContainer.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.bottom_navigation_today:
                        currentFragment = 0;
                        setFabVisible();
                        Log.i(TAG, "Today fragment selected");
                        TodayFragment todayFragment = new TodayFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("date", mDate);
                        Intent intent = getIntent();
                        if (intent.getStringExtra("redirected_from") != null &&
                                intent.getStringExtra("redirected_from").equals("SplashActivity")) {
                            bundle.putString("redirected_from", "SplashActivity");
                        }
                        todayFragment.setArguments(bundle);
                        loadFragment(todayFragment);
                        return true;
                    case R.id.bottom_navigation_insight:
                        currentFragment = 1;
                        setFabInvisible();
                        Log.i(TAG, "Insight fragment selected");
                        InsightFragment insightFragment = new InsightFragment();
                        loadFragment(insightFragment);
                        return true;
                    case R.id.bottom_navigation_stats:
                        currentFragment = 2;
                        setFabInvisible();
                        Log.i(TAG, "Stats fragment selected");
                        StatsFragment statsFragment = new StatsFragment();
                        loadFragment(statsFragment);
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });
        Log.i(TAG, mDate);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastFragEditor.apply();
        //closeFABMenu();
    }

    public void onClickMakeEntry(View v) {
        Intent intent = new Intent(MainActivity.this, MakeEntryActivity.class);
        startActivity(intent);
    }

    public void onClickTrackActivity(View v) {
        Intent intent = new Intent(MainActivity.this, TimerActivity.class);
        startActivity(intent);
    }


    public void onClickEditEntryTypes(View v) {
        Intent intent = new Intent(MainActivity.this, EditSurveyItemsActivity.class);
        startActivity(intent);

    }

    @Override
    protected void onStop() {
        super.onStop();
        // whenever an app is stopped, revert back to today's date.
        mDate = setUpDatesForToday();
        lastFragEditor.putInt("last_fragment", 0);
        lastFragEditor.apply();
    }

    @Override
    protected void onDestroy() {
        //stopService(motionServiceIntent);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent dateIntent = getIntent();
        // if a user has selected a date, it will be passed as intent extra.
        if (dateIntent != null) {
            mDate = dateIntent.getStringExtra("date");
        }
        // if a user has not selected a date, pass today's date.
        if (mDate == null) {
            mDate = setUpDatesForToday();
        }
        TodayFragment todayFragment = new TodayFragment();
        Bundle bundle = new Bundle();
        bundle.putString("date", mDate);
        Intent intent = getIntent();
        if (intent.getStringExtra("redirected_from") != null &&
                intent.getStringExtra("redirected_from").equals("SplashActivity")) {
            bundle.putString("redirected_from", "SplashActivity");
        }
        todayFragment.setArguments(bundle);
        InsightFragment insightFragment = new InsightFragment();
        StatsFragment statsFragment = new StatsFragment();
        switch (currentFragment) {
            case -1:
                setFabVisible();
                loadFragment(todayFragment);
                break;
            case 0:
                setFabVisible();
                loadFragment(todayFragment);
                break;
            case 1:
                setFabInvisible();
                loadFragment(insightFragment);
                break;
            case 2:
                setFabInvisible();
                loadFragment(statsFragment);
                break;
        }
        // set up viewpager to handle fragments on Resume
        if (isFABOpen) {
            closeFABMenu();
        }
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            // set up date that will be passed to the TodayFragment.

            if (mDate.equals(formattedTime.getCurrentDateAsYYYYMMDD())) {
                asyncCallsMasterRepository = new AsyncCallsMasterRepository(this, mDate);
                asyncCallsMasterRepository.makeCallsToConnectedServices();
            }
            Log.i(TAG, "User is already signed in!");
            //setupViewPager(viewPager);
        }
    }

    private void loadFragment(Fragment fragment) {
        Log.i(TAG, "Loading fragment");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_main_framelayout, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showFABMenu() {
        isFABOpen = true;
        fab1LL.setVisibility(View.VISIBLE);
        fab2LL.setVisibility(View.VISIBLE);
        fab3LL.setVisibility(View.VISIBLE);
        fabBGLayout.setVisibility(View.VISIBLE);
        mFab.animate().rotationBy(135).setDuration(100);
        mFab.setExpanded(true);
        fab1LL.animate().alpha(1)
                //.scaleX(1)
                //.scaleY(1)
                .setDuration(100);
        fab2LL.animate().alpha(1)
                //.scaleX(1)
                //.scaleY(1)
                .setDuration(100);
        fab3LL.animate().alpha(1)
                //.scaleX(1)
                //.scaleY(1)
                .setDuration(100);
        //fab1LL.animate().translationY(-getResources().getDimension(R.dimen.standard_56)).setDuration(100);
        //fab2LL.animate().translationY(-getResources().getDimension(R.dimen.standard_112)).setDuration(100);
        //fab3LL.animate().translationY(-getResources().getDimension(R.dimen.standard_168)).setDuration(100);
        //if (bottomNavigationView.isV)
        /*
        if (bottomNavigation.isHidden()) {
            fab1LL.animate().translationY(-getResources().getDimension(R.dimen.standard_0)).setDuration(500);
            fab2LL.animate().translationY(-getResources().getDimension(R.dimen.standard_56)).setDuration(500);
            fab3LL.animate().translationY(-getResources().getDimension(R.dimen.standard_112)).setDuration(500);
        } else {
            fab1LL.animate().translationY(-getResources().getDimension(R.dimen.standard_56)).setDuration(500);
            fab2LL.animate().translationY(-getResources().getDimension(R.dimen.standard_112)).setDuration(500);
            fab3LL.animate().translationY(-getResources().getDimension(R.dimen.standard_168)).setDuration(500);
        }
        bottomNavigation.setEnabled(false);*/
    }

    private void closeFABMenu() {
        isFABOpen = false;
        //bottomNavigation.setEnabled(true);
        fabBGLayout.setVisibility(View.GONE);
        if (mFab.isExpanded()) {
            mFab.animate().rotationBy(-135).setDuration(100);
            mFab.setExpanded(false);
        }
        fab1LL.animate().alpha(0)
                //.scaleX(0)
                //.scaleY(0)
                .setDuration(100);
        fab2LL.animate().alpha(0)
                //.scaleX(0)
                //.scaleY(0)
                .setDuration(100);
        fab3LL.animate().alpha(0)
                //.scaleX(0)
                //.scaleY(0)
                .setDuration(100)
        .setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isFABOpen) {
                    fab1LL.setVisibility(View.GONE);
                    fab2LL.setVisibility(View.GONE);
                    fab3LL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        //fab1LL.animate().translationY(72).setDuration(100);
        //fab2LL.animate().translationY(72).setDuration(100);
        /*
        fab3LL.animate().translationY(72).setDuration(100).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isFABOpen) {
                    fab1LL.setVisibility(View.GONE);
                    fab2LL.setVisibility(View.GONE);
                    fab3LL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });*/
    }

    private void setFabVisible() {
        mFab.show();
        mFab.setAlpha(0f);
        mFab.setScaleX(0f);
        mFab.setScaleY(0f);
        mFab.animate()
                .alpha(1)
                .scaleX(1)
                .scaleY(1)
                .setDuration(100)
                .setInterpolator(new OvershootInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mFab.animate()
                                .setInterpolator(new LinearOutSlowInInterpolator())
                                .start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }

    private void setFabInvisible() {
        mFab.animate()
                .alpha(0)
                .scaleX(0)
                .scaleY(0)
                .setDuration(100)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mFab.hide();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        //mFab.setVisibility(View.GONE);
                        mFab.hide();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
        closeFABMenu();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    public void setNavigationVisibility(boolean shouldHide) {
        if (shouldHide) {
            //bottomNavigation.hideBottomNavigation(true);
        } else {
            //bottomNavigation.restoreBottomNavigation(true);
        }
    }

    public String getDate() {
        return mDate;
    }

    /*
    private String setUpDatesForToday() {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = DateFormat.getDateInstance();
        return dateFormat.format(new Date());
        //SimpleDateFormat mdformat = new SimpleDateFormat("MMM d, EEEE");
        //mdformat.format(calendar.getTime());
    }*/

    private String setUpDatesForToday() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return mdformat.format(calendar.getTime());
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }
}
