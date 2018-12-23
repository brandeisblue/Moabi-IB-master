package com.ivorybridge.moabi.ui.activity;

import android.animation.Animator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.repository.AsyncCallsMasterRepository;
import com.ivorybridge.moabi.service.MotionSensorService;
import com.ivorybridge.moabi.ui.adapter.FragmentAdapter;
import com.ivorybridge.moabi.ui.fragment.InsightFragment;
import com.ivorybridge.moabi.ui.fragment.StatsFragment;
import com.ivorybridge.moabi.ui.fragment.TodayFragment;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProviders;
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
    private DataInUseViewModel dataInUseViewModel;
    private TapTargetView tapTargetView;
    private TapTargetSequence tapTargetSequence;

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.i(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        // Log and toast
                        Log.i(TAG, token);
                        //Toast.makeText(MainActivity.this, "Token is : " + token, Toast.LENGTH_SHORT).show();
                    }
                });
        //  Declare a new thread to do a preference check
        //  Make a new preferences editor
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor e = getPrefs.edit();
        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);
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
        if (tapTargetView != null) {
            tapTargetView.dismiss(true);
        }
        if (tapTargetSequence != null) {
            tapTargetSequence.cancel();
        }
        //closeFABMenu();
    }

    public void onClickMakeEntry(View v) {
        Intent intent = new Intent(MainActivity.this, MakeEntryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void onClickTrackActivity(View v) {
        Intent intent = new Intent(MainActivity.this, TimerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }


    public void onClickEditEntryTypes(View v) {
        Intent intent = new Intent(MainActivity.this, EditSurveyItemsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
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
        //startService(new Intent(this, DailyBriefingNotifService.class));
        SharedPreferences notificationSharedPreferences = getSharedPreferences(
                getString(R.string.com_ivorybridge_mobai_NOTIFICATION_SHARED_PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor notificationSPEditor = notificationSharedPreferences.edit();
        /*
        DataInUseMediatorLiveData dataInUseMediatorLiveData = new DataInUseMediatorLiveData(
                dataInUseViewModel.getAllInputsInUse(), dataInUseViewModel.getAllConnectedServices());
        dataInUseMediatorLiveData.observe(this, new Observer<Pair<List<InputInUse>, List<ConnectedService>>>() {
            @Override
            public void onChanged(Pair<List<InputInUse>, List<ConnectedService>> listListPair) {
                Set<String> activitiesSet = new TreeSet<>();
                if (listListPair.first != null && listListPair.first.size() > 0 &&
                        listListPair.second != null && listListPair.second.size() > 0) {
                    for (InputInUse inputInUse : listListPair.first) {
                        if (inputInUse.isInUse()) {
                            for (ConnectedService connectedService : listListPair.second) {
                                if (connectedService.getName().equals(inputInUse.getName()) &&
                                        connectedService.isConnected()) {
                                    if (connectedService.getName().equals(getString(R.string.fitbit_camel_case))) {
                                        activitiesSet.add("3" + connectedService.getName());
                                        notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_source_notification),
                                                getString(R.string.fitbit_title));
                                        notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_activity_type_notification),
                                                getString(R.string.activity_steps_title));
                                        notificationSPEditor.commit();
                                    } else if (connectedService.getName().equals(getString(R.string.googlefit_camel_case))) {
                                        activitiesSet.add("2" + connectedService.getName());
                                        notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_source_notification),
                                                getString(R.string.googlefit_title));
                                        notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_activity_type_notification),
                                                getString(R.string.activity_steps_title));
                                        notificationSPEditor.commit();
                                    } else if (connectedService.getName().equals(getString(R.string.moabi_tracker_camel_case))) {
                                        activitiesSet.add("0" + connectedService.getName());
                                        notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_source_notification),
                                                getString(R.string.moabi_tracker_title));
                                        notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_activity_type_notification),
                                                getString(R.string.activity_steps_title));
                                        notificationSPEditor.commit();
                                    }
                                }
                            }
                        }
                    }
                }
                String[] activitiesArray = activitiesSet.toArray(new String[0]);
                if (activitiesArray.length < 1) {
                    notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_source_notification),
                            null);
                    notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_activity_type_notification),
                            null);
                    notificationSPEditor.commit();
                    Intent intent = new Intent(MainActivity.this, MotionSensorService.class);
                    stopService(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, MotionSensorService.class);
                    startService(intent);
                }
            }
        });*/
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor e = getPrefs.edit();
        boolean tut1Complete = getPrefs.getBoolean("tut_1_complete", false);
        boolean tut2Complete = getPrefs.getBoolean("tut_2_complete", false);
        boolean tut3Complete = getPrefs.getBoolean("tut_3_complete", false);
        boolean tut4Complete = getPrefs.getBoolean("tut_4_complete", false);
        Log.i(TAG, "Tutorial 1 - " + tut1Complete + " Tutorial 2 - " + tut2Complete
        + " Tutorial 3 - " + tut3Complete + " Tutorial 4 - " + tut4Complete);
        if (!tut1Complete || !tut2Complete || !tut3Complete) {
            dataInUseViewModel.deleteAllInputs();
            e.putBoolean("tut_1_complete", false);
            e.putBoolean("tut_2_complete", false);
            e.putBoolean("tut_3_complete", false);
            e.commit();
            e.apply();
        }
        tut1Complete = getPrefs.getBoolean("tut_1_complete", false);
        if (!tut1Complete) {
            showFABMenu();
            tapTargetView = TapTargetView.showFor(this, TapTarget.forView(findViewById(R.id.activity_main_make_entry_linearlayout1),
                    getString(R.string.tutorial_main_welcome_msg),
                    getString(R.string.tutorial_main_click_fab))
                            .outerCircleColor(R.color.colorPrimary)
                            .outerCircleAlpha(0.7f)
                            .targetCircleColor(R.color.white)
                            .titleTextSize(16)
                            //.titleTextColor(R.color.colorPrimary)      // Specify the color of the title text
                            .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                            //.descriptionTextColor(R.color.white)  // Specify the color of the description text
                            .textColor(R.color.white)
                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                            .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                            .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(false)                   // Whether to tint the target view's color
                            .transparentTarget(true)   // Specify whether the target is transparent (displays the content underneath)
                            //.icon(ContextCompat.getDrawable(this, R.drawable.bg_rectangle_rounded_white))           // Specify a custom drawable to draw as the target
                            .targetRadius(72),                  // Specify the target radius (in dp)
                    new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);// This call is optional
                            e.putBoolean("tut_1_complete", true);
                            e.commit();
                            onClickMakeEntry(findViewById(R.id.activity_main_make_entry_linearlayout1));
                        }
                    });
        } else {
            //showFABMenu();
            int width = this.getResources().getDisplayMetrics().widthPixels;
            int height = this.getResources().getDisplayMetrics().heightPixels;
            final Rect target = new Rect( width - convertDptoPixels(44), convertDptoPixels(40), width - convertDptoPixels(20), convertDptoPixels(64));
            TapTarget tapTarget = TapTarget.forBounds(target, getString(R.string.tutorial_wrap_up_title));
            //target.offset(display.getWidth() / 2, display.getHeight() / 2);
            if (!tut4Complete) {
                tapTargetView = TapTargetView.showFor(this, tapTarget
                                .outerCircleColor(R.color.colorPrimary)
                                .outerCircleAlpha(0.7f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(16)
                                //.titleTextColor(R.color.colorPrimary)      // Specify the color of the title text
                                .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                //.descriptionTextColor(R.color.white)  // Specify the color of the description text
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(false)                   // Whether to tint the target view's color
                                .transparentTarget(true)   // Specify whether the target is transparent (displays the content underneath)
                                //.icon(ContextCompat.getDrawable(this, R.drawable.bg_rectangle_rounded_white))           // Specify a custom drawable to draw as the target
                                .targetRadius(24),                  // Specify the target radius (in dp)
                        new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                            @Override
                            public void onTargetClick(TapTargetView view) {
                                super.onTargetClick(view);// This call is optional
                                e.putBoolean("tut_4_complete", true);
                                e.commit();
                            }
                        });
                /*
                tapTargetSequence = new TapTargetSequence(this)
                        .target(TapTarget.forBounds(target, getString(R.string.tutorial_wrap_up_title),
                                getString(R.string.tutorial_main_click_fab))
                                .outerCircleColor(R.color.colorPrimary)
                                .outerCircleAlpha(0.7f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(16)
                                //.icon(logo)
                                .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(false)                   // Whether to tint the target view's color
                                .transparentTarget(true)
                                .targetRadius(24));
                tapTargetSequence.start();
                tapTargetSequence.listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        e.putBoolean("tut_4_complete", true);
                        e.commit();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                    }
                });*/
            }
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
            if (intent.getStringExtra("redirected_from") != null &&
                    intent.getStringExtra("redirected_from").equals("insight_notif")) {
                Log.i(TAG, "redirected from insight notif");
                currentFragment = 1;
            }
            Log.i(TAG, "current fragment - " + currentFragment);
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
            if (mDate.equals(formattedTime.getCurrentDateAsYYYYMMDD())) {
                asyncCallsMasterRepository = new AsyncCallsMasterRepository(this, mDate);
                asyncCallsMasterRepository.makeCallsToConnectedServices();
            }
            mAuth = FirebaseAuth.getInstance();
        }
    }

    private void loadFragment(Fragment fragment) {
        Log.i(TAG, "Loading fragment");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_main_framelayout, fragment);
        //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showFABMenu() {
        isFABOpen = true;
        fab1LL.setVisibility(View.VISIBLE);
        fab2LL.setVisibility(View.VISIBLE);
        //fab3LL.setVisibility(View.VISIBLE);
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
        SharedPreferences getPrefs = androidx.preference.PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean tut4Complete = getPrefs.getBoolean("tut_4_complete", false);
        boolean tut1Complete = getPrefs.getBoolean("tut_1_complete", false);
        SharedPreferences.Editor e = getPrefs.edit();
        if (!tut4Complete && tut1Complete) {
            e.putBoolean("tut_3_complete", false);
            //dataInUseViewModel.deleteAllInputs();
            dataInUseViewModel.deleteAllConnectedServices();
            e.commit();
            Intent intent = new Intent(this, ConnectServicesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else {
            finishAffinity();
        }
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

    public float convertPixelsToDp(float px){
        return px / ((float) getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public int convertDptoPixels(float dp){
        float density = getResources().getDisplayMetrics().density;
        return (int) Math.ceil(dp * density);
    }
}
