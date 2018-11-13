package com.ivorybridge.moabi.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.DataInUseRepository;
import com.ivorybridge.moabi.service.TimerService;
import com.ivorybridge.moabi.ui.recyclerviewitem.timedactivity.TimedActivityShortSummaryItem;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.TimedActivityViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TimedActivityActivity extends AppCompatActivity implements PropertyChangeListener {

    private static final String TAG = TimedActivityActivity.class.getSimpleName();
    @BindView(R.id.activity_time_tracker_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_time_tracker_time_textview)
    TextView timeTextView;
    @BindView(R.id.activity_time_tracker_edittext)
    EditText editText;
    /*
    @BindView(R.id.activity_time_tracker_initial_play_button)
    Button initialPlayButton;*/
    @BindView(R.id.activity_time_tracker_play_button)
    Button playButton;
    @BindView(R.id.activity_time_tracker_reset_button)
    Button resetButton;
    @BindView(R.id.activity_time_tracker_save_button)
    Button saveButton;
    @BindView(R.id.activity_time_tracker_edit_imagebutton)
    ImageButton editButton;
    @BindView(R.id.activity_time_tracker_nowplaying_linearlayout)
    LinearLayout nowPlayingLL;
    @BindView(R.id.activity_time_tracker_savedactivities_recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.activity_time_tracker_time_display_relativelayout)
    RelativeLayout displayRL;
    @BindView(R.id.activity_time_tracker_recyclerview_header_linearlayout)
    LinearLayout headerLL;
    @BindView(R.id.activity_time_tracker_recyclerview_border)
    View recyclerViewBorder;
    @BindView(R.id.activity_time_tracker_container)
    CoordinatorLayout container;
    private TimedActivityViewModel timedActivityViewModel;
    private FastAdapter<IItem> recyclerAdapter;
    private ItemAdapter<TimedActivityShortSummaryItem> timedActivityItemAdapter;
    private DataInUseRepository dataInUseRepository;
    private Timer timer;
    private Handler timerHandler;
    private FirebaseManager firebaseManager;
    private SharedPreferences timedActivitySharedPreferences;
    private SharedPreferences.Editor timedActivitySPEditor;
    private SharedPreferences.OnSharedPreferenceChangeListener changeListener;
    private Date lastTypeTime = null;
    private final Runnable updateTextRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimeText();
        }
    };


    /*
    @BindView(R.id.activity_time_tracker_edittext_check_imageview)
    ImageButton checkImage;*/
    private long lastStopTime = 0;
    private long intervalOnPause = 0;
    private long intervalActive = 0;
    private FormattedTime formattedTime;
    private TimerService timerService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_tracker);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Track Activity");
        }


        formattedTime = new FormattedTime();
        timerHandler = new Handler();
        firebaseManager = new FirebaseManager();
        timedActivitySharedPreferences = getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_TIMED_ACTIVITY_SHARED_PREFERENCE_KEY),
                Context.MODE_PRIVATE);
        timedActivitySPEditor = timedActivitySharedPreferences.edit();
        changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                String savedString = timedActivitySharedPreferences.getString("current_activity", null);
                if (savedString == null) {
                    editText.setVisibility(View.INVISIBLE);
                    editButton.setVisibility(View.VISIBLE);
                } else {
                    editButton.setVisibility(View.INVISIBLE);
                    editText.setVisibility(View.VISIBLE);
                    editText.setText(savedString);
                }
            }
        };
        timedActivitySharedPreferences.registerOnSharedPreferenceChangeListener(changeListener);
        String savedString = timedActivitySharedPreferences.getString("current_activity", null);
        if (savedString != null) {
            editButton.setVisibility(View.INVISIBLE);
            editText.setVisibility(View.VISIBLE);
            editText.setText(savedString);
        }
        recyclerAdapter = new FastAdapter<>();
        timedActivityItemAdapter = new ItemAdapter<>();
        recyclerAdapter = FastAdapter.with(timedActivityItemAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(recyclerAdapter);

        timedActivityViewModel = ViewModelProviders.of(TimedActivityActivity.this).get(TimedActivityViewModel.class);
        dataInUseRepository = new DataInUseRepository(this.getApplication());
        timedActivityViewModel.getAll(formattedTime.getCurrentDateAsYYYYMMDD()).observe(this, new Observer<List<TimedActivitySummary>>() {
            @Override
            public void onChanged(@Nullable List<TimedActivitySummary> timedActivities) {
                if (timedActivities != null && timedActivities.size() > 0) {
                    Log.i(TAG, "Has timed activity entry");
                    if (recyclerView.getVisibility() == View.GONE) {
                        displayRL.animate().translationY(-406);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerViewBorder.setVisibility(View.VISIBLE);
                        //headerCard.setVisibility(View.VISIBLE);
                        //headerLL.setVisibility(View.VISIBLE);
                    }
                    timedActivityItemAdapter.clear();
                    Collections.reverse(timedActivities);
                    for (TimedActivitySummary timedActivitySummary : timedActivities) {
                        timedActivityItemAdapter.add(new TimedActivityShortSummaryItem(timedActivitySummary));
                        //Log.i(TAG, timedActivitySummary.toString());
                    }
                }
            }
        });

        formattedTime = new FormattedTime();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lastTypeTime = new Date();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // dispatch after done typing (1 sec after)
                Timer t = new Timer();
                Handler handler = new Handler();
                TimerTask tt = new TimerTask() {
                    @Override
                    public void run() {
                        Date myRunTime = new Date();
                        if ((lastTypeTime.getTime() + 1000) <= myRunTime.getTime()) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!editText.getText().toString().isEmpty()) {
                                        editText.clearFocus();
                                        timedActivitySPEditor.putString("current_activity", editText.getText().toString().trim());
                                        timedActivitySPEditor.apply();
                                        hideKeyboard(TimedActivityActivity.this);
                                    } else {
                                        editText.clearFocus();
                                        editText.setVisibility(View.INVISIBLE);
                                        editButton.setVisibility(View.VISIBLE);
                                        timedActivitySPEditor.putString("current_activity", null);
                                        timedActivitySPEditor.apply();
                                        hideKeyboard(TimedActivityActivity.this);
                                    }
                                    Log.i(TAG, "typing finished!!!");
                                }
                            });
                        } else {
                            Log.i(TAG, "Canceled");
                        }
                    }
                };
                t.schedule(tt, 1000);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().length() == 0) {
                    editText.setVisibility(View.VISIBLE);
                    editButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (TimerService.TimeContainer.getInstance().getCurrentState() == TimerService.TimeContainer.STATE_RUNNING
                || TimerService.TimeContainer.getInstance().getCurrentState() == TimerService.TimeContainer.STATE_PAUSED) {

        } else {
            stopService(new Intent(this, TimerService.class));
        }

    }

    private void save() {

        String name = timedActivitySharedPreferences.getString("current_activity", null);
        if (name == null || name.isEmpty()) {
            name = "Unknown";
        }
        intervalActive = TimerService.TimeContainer.getInstance().getElapsedTime();
        /*
        intervalActive += SystemClock.elapsedRealtime() - chronometer.getBase();
        if (playButton.getText().equals("start")) {
            intervalActive = 0;
        }*/
        if (name.equals("Unknown") && intervalActive == 0) {
            Toast.makeText(this, "No activity was recorded", Toast.LENGTH_SHORT).show();
            return;
        }
        InputInUse inputInUse = new InputInUse();
        inputInUse.setType("tracker");
        inputInUse.setName("timedActivitySummary");
        inputInUse.setInUse(true);
        dataInUseRepository.insert(inputInUse);
        Log.i(TAG, name);
        TimedActivitySummary timedActivitySummary = new TimedActivitySummary();
        timedActivitySummary.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
        timedActivitySummary.setInputName(name);
        timedActivitySummary.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
        timedActivitySummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
        timedActivitySummary.setDuration(intervalActive);
        //timedActivitySummary.setDuration(SystemClock.elapsedRealtime() - chronometer.getBase() - intervalOnPause);
        timedActivityViewModel.insert(timedActivitySummary, formattedTime.getCurrentDateAsYYYYMMDD());
        firebaseManager.getUserInputsRef().child("timedActivitySummary")
                .child(formattedTime.getCurrentDateAsYYYYMMDD())
                .child(formattedTime.convertLongToHHMM(timedActivitySummary.getDateInLong()))
                .child(name).setValue(timedActivitySummary.getDuration());
        firebaseManager.getDaysWithDataTodayRef().child("timedActivitySummary").setValue(true);
        TimerService.TimeContainer.getInstance().stopAndReset();
        //playButton.setText("start");
        //reset(resetButton);
    }

    /*
    private void displayChronometer() {
        chronometer.setText("00:00:00");
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;*/
                /*
                String hh = h < 10 ? "0"+h: h+"";
                String mm = m < 10 ? "0"+m: m+"";
                String ss = s < 10 ? "0"+s: s+"";*/
                /*cArg.setText(String.format(Locale.US, "%02d:%02d:%02d", h, m, s));
            }
        });
    }*/

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        TimerService.TimeContainer.getInstance().removeObserver(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkServiceRunning();
        TimerService.TimeContainer t = TimerService.TimeContainer.getInstance();
        if (t.getCurrentState() == TimerService.TimeContainer.STATE_RUNNING) {
            playButton.setText("pause");
            startUpdateTimer();
        } else {
            playButton.setText("start");
            updateTimeText();
        }
        TimerService.TimeContainer.getInstance().addObserver(this);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if (playButton.getVisibility() == View.VISIBLE)
    }

    private void checkServiceRunning() {
        if (!TimerService.TimeContainer.getInstance().isServiceRunning.get()) {
            startService(new Intent(this, TimerService.class));
        }
    }

    public void changeState(View v) {
        checkServiceRunning();
        TimerService.TimeContainer tc = TimerService.TimeContainer.getInstance();
        if (tc.getCurrentState() == TimerService.TimeContainer.STATE_RUNNING) {
            TimerService.TimeContainer.getInstance().pause();
            playButton.setText("start");
        } else {
            TimerService.TimeContainer.getInstance().start();
            startUpdateTimer();
            playButton.setText("pause");
        }
    }

    public void reset(View v) {
        TimerService.TimeContainer.getInstance().stopAndReset();
        editText.setText(null);
        editText.setVisibility(View.INVISIBLE);
        editButton.setVisibility(View.VISIBLE);
        timedActivitySPEditor.putString("current_activity", null);
        timedActivitySPEditor.apply();
        updateTimeText();
        stopService(new Intent(this, TimerService.class));
    }

    private void updateTimeText() {
        timeTextView.setText(getTimeString(TimerService.TimeContainer.getInstance().getElapsedTime()));
    }

    public void startUpdateTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerHandler.post(updateTextRunnable);
            }
        }, 0, 16);
        if (editText.getText().toString().isEmpty()) {
            TimerService.TimeContainer.getInstance().setName("Unknown");
        } else {
            TimerService.TimeContainer.getInstance().setName(editText.getText().toString().trim());
        }
    }

    private String getTimeString(long ms) {
        if (ms == 0) {
            return "00:00:00";
        } else {
            long millis = (ms % 1000) / 10;
            long seconds = (ms / 1000) % 60;
            long minutes = (ms / 1000) / 60;
            long hours = minutes / 60;

            StringBuilder sb = new StringBuilder();
            if (hours > 0) {
                sb.append(hours);
                sb.append(':');
            }
            if (minutes > 0) {
                minutes = minutes % 60;
                if (minutes >= 10) {
                    sb.append(minutes);
                } else {
                    sb.append(0);
                    sb.append(minutes);
                }
            } else {
                sb.append('0');
                sb.append('0');
            }
            sb.append(':');
            if (seconds > 0) {
                if (seconds >= 10) {
                    sb.append(seconds);
                } else {
                    sb.append(0);
                    sb.append(seconds);
                }
            } else {
                sb.append('0');
                sb.append('0');
            }
            sb.append(':');
            if (millis > 0) {
                if (millis >= 10) {
                    sb.append(millis);
                } else {
                    sb.append(0);
                    sb.append(millis);
                }
            } else {
                sb.append('0');
                sb.append('0');
            }
            return sb.toString();
        }
    }


    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName() == TimerService.TimeContainer.STATE_CHANGED) {
            TimerService.TimeContainer t = TimerService.TimeContainer.getInstance();
            if (t.getCurrentState() == TimerService.TimeContainer.STATE_RUNNING) {
                playButton.setText("pause");
                startUpdateTimer();
            } else {
                playButton.setText("start");
                updateTimeText();
            }
            //checkServiceRunning();
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
