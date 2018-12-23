package com.ivorybridge.moabi.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.stats.SimpleRegressionSummary;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.repository.statistics.PredictionsRepository;
import com.ivorybridge.moabi.ui.adapter.IconSpinnerAdapter;
import com.ivorybridge.moabi.ui.recyclerviewitem.insight.InsightRecommendationItem;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DailyBriefingActivity extends AppCompatActivity {

    private static final String TAG = DailyBriefingActivity.class.getSimpleName();
    @BindView(R.id.activity_daily_briefing_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_daily_briefing_spinner)
    Spinner spinner;
    @BindView(R.id.activity_daily_briefing_greetings_textview)
    TextView greetingsTextView;
    @BindView(R.id.activity_daily_briefing_explanation_textview)
    TextView explanationTextView;
    @BindView(R.id.activity_daily_briefing_recommendation_recyclerview)
    RecyclerView recRecyclerView;
    private DataInUseViewModel dataInUseViewModel;
    private SharedPreferences dailyBriefingPreferences;
    private FastAdapter<IItem> recyclerAdapter;
    private ItemAdapter<InsightRecommendationItem> recommendationItemItemAdapter;
    private PredictionsRepository predictionsRepository;
    private FormattedTime formattedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_briefing);
        ButterKnife.bind(this);
        formattedTime = new FormattedTime();
        predictionsRepository = new PredictionsRepository(getApplication());
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (getSupportActionBar() != null) {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat sf2 = new SimpleDateFormat("MMM d, EEEE", Locale.US);
            Date convertedDate = new Date();
            try {
                convertedDate = sf.parse(formattedTime.getCurrentDateAsYYYYMMDD());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String formattedDate = sf2.format(convertedDate);
            getSupportActionBar().setTitle(formattedDate);
        }

        if (formattedTime.getCurrentHour() < 12) {
            greetingsTextView.setText(getString(R.string.cheers_good_morning_msg));
        } else if (formattedTime.getCurrentHour() < 17) {
            greetingsTextView.setText(getString(R.string.cheers_good_afternoon_msg));
        } else if (formattedTime.getCurrentHour() < 20) {
            greetingsTextView.setText(getString(R.string.cheers_good_evening_msg));
        } else if (formattedTime.getCurrentHour() < 24) {
            greetingsTextView.setText(getString(R.string.cheers_good_night_msg));
        }
        recyclerAdapter = new FastAdapter<>();
        recommendationItemItemAdapter = new ItemAdapter<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerAdapter = FastAdapter.with(Arrays.asList(recommendationItemItemAdapter));
        recRecyclerView.setLayoutManager(layoutManager);
        recRecyclerView.setItemAnimator(new DefaultItemAnimator());
        recRecyclerView.setAdapter(recyclerAdapter);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        recRecyclerView.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(recRecyclerView);
        dailyBriefingPreferences = getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_DAILY_BRIEFING_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);
        dataInUseViewModel.getAllInputsInUse().observe(this, new Observer<List<InputInUse>>() {
            @Override
            public void onChanged(@Nullable List<InputInUse> inputInUses) {
                if (inputInUses != null && inputInUses.size() > 0) {
                    Set<String> activitiesSet = new TreeSet<>();
                    for (InputInUse inputInUse : inputInUses) {
                        if (inputInUse.isInUse()) {
                            /*
                            if (inputInUse.getName().equals(getString(R.string.fitbit_camel_case))) {
                                activitiesSet.add("32 Fitbit");
                            } else if (inputInUse.getName().equals(getString(R.string.googlefit_camel_case))) {
                                activitiesSet.add("34 Google Fit");
                            } else if (inputInUse.getName().equals(getString(R.string.phone_usage_camel_case))) {
                                activitiesSet.add("36 App Usage");
                            } else if (inputInUse.getName().equals(getString(R.string.weather_camel_case))) {
                                activitiesSet.add("38 Weather");
                            } else */
                            if (inputInUse.getName().equals(getString(R.string.daily_review_camel_case))) {
                                activitiesSet.add("10 Daily Review");
                            } else if (inputInUse.getName().equals(getString(R.string.mood_and_energy_camel_case))) {
                                activitiesSet.add("12 Mood");
                                activitiesSet.add("14 Energy");
                            } else if (inputInUse.getName().equals(getString(R.string.stress_camel_case))) {
                                activitiesSet.add("16 Stress");
                            } /*else if (inputInUse.getName().equals(getString(R.string.baactivity_camel_case))) {
                                activitiesSet.add("20 Activity");
                            } else if (inputInUse.getName().equals(getString(R.string.moabi_tracker_camel_case))) {
                                activitiesSet.add("30 Moabi");
                            } else if (inputInUse.getName().equals(getString(R.string.timer_camel_case))) {
                                activitiesSet.add("42 Timer");
                            } */ else if (inputInUse.getName().equals(getString(R.string.depression_phq9_camel_case))) {
                                activitiesSet.add("17 Depression");
                            } else if (inputInUse.getName().equals(getString(R.string.anxiety_gad7_camel_case))) {
                                activitiesSet.add("18 Anxiety");
                            }
                        }
                    }
                    Log.i(TAG, activitiesSet.toString());
                    if (activitiesSet.size() > 0) {
                        String[] activitiesArray = activitiesSet.toArray(new String[0]);
                        int[] imagesArray = new int[activitiesSet.size()];
                        for (int i = 0; i < activitiesArray.length; i++) {
                            if (activitiesArray[i].equals("12 Mood")) {
                                activitiesArray[i] = getString(R.string.mood_title);
                                imagesArray[i] = R.drawable.ic_emotion;
                            } else if (activitiesArray[i].equals("14 Energy")) {
                                activitiesArray[i] = getString(R.string.energy_title);
                                imagesArray[i] = R.drawable.ic_emotion;
                            } else if (activitiesArray[i].equals("16 Stress")) {
                                activitiesArray[i] = getString(R.string.stress_title);
                                imagesArray[i] = R.drawable.ic_stress;
                            } else if (activitiesArray[i].equals("10 Daily Review")) {
                                activitiesArray[i] = getString(R.string.daily_review_title);
                                imagesArray[i] = R.drawable.ic_review_black;
                            } else if (activitiesArray[i].equals("20 Activity")) {
                                activitiesArray[i] = getString(R.string.baactivity_title);
                                imagesArray[i] = R.drawable.ic_physical_activity_black;
                            } else if (activitiesArray[i].equals("32 Fitbit")) {
                                activitiesArray[i] = getString(R.string.fitbit_title);
                                imagesArray[i] = R.drawable.ic_fitbit_logo;
                            } else if (activitiesArray[i].equals("34 Google Fit")) {
                                activitiesArray[i] = getString(R.string.googlefit_title);
                                imagesArray[i] = R.drawable.ic_googlefit;
                            } else if (activitiesArray[i].equals("36 App Usage")) {
                                activitiesArray[i] = getString(R.string.phone_usage_title);
                                imagesArray[i] = R.drawable.ic_appusage;
                            } else if (activitiesArray[i].equals("38 Weather")) {
                                activitiesArray[i] = getString(R.string.weather_title);
                                imagesArray[i] = R.drawable.ic_partly_cloudy;
                            } else if (activitiesArray[i].equals("30 Moabi")) {
                                activitiesArray[i] = getString(R.string.moabi_tracker_title);
                                imagesArray[i] = R.drawable.ic_monogram_colored;
                            } else if (activitiesArray[i].equals("42 Timer")) {
                                activitiesArray[i] = getString(R.string.timer_title);
                                imagesArray[i] = R.drawable.ic_stopwatch;
                            } else if (activitiesArray[i].equals("17 Depression")) {
                                activitiesArray[i] = getString(R.string.depression_phq9_title);
                                imagesArray[i] = R.drawable.ic_depression_rain_black;
                            } else if (activitiesArray[i].equals("18 Anxiety")) {
                                activitiesArray[i] = getString(R.string.anxiety_gad7_title);
                                imagesArray[i] = R.drawable.ic_anxiety_insomnia_black;
                            }
                        }
                        if (activitiesArray != null) {
                            int spinnerSelection = dailyBriefingPreferences.getInt("daily_briefing_spinner_selection", 0);
                            if (spinnerSelection >= activitiesArray.length) {
                                spinnerSelection = 0;
                            }
                            IconSpinnerAdapter iconSpinnerAdapter = new IconSpinnerAdapter(DailyBriefingActivity.this, activitiesArray, imagesArray, false);
                            spinner.setAdapter(iconSpinnerAdapter);
                            spinner.setSelection(spinnerSelection);
                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    Log.i(TAG, activitiesArray[position] + " is selected");
                                    SharedPreferences.Editor editor = dailyBriefingPreferences.edit();
                                    editor.putInt("daily_briefing_spinner_selection", position);
                                    editor.apply();
                                    recommendationItemItemAdapter.clear();
                                    if (activitiesArray[position].equals(getString(R.string.mood_title))) {
                                        loadRegressionRecommendations(activitiesArray[position]);
                                    } else if (activitiesArray[position].equals(getString(R.string.energy_title))) {
                                        loadRegressionRecommendations(activitiesArray[position]);
                                    } else if (activitiesArray[position].equals(getString(R.string.stress_title))) {
                                        loadRegressionRecommendations(activitiesArray[position]);
                                    } else if (activitiesArray[position].equals(getString(R.string.depression_phq9_title))) {
                                        loadRegressionRecommendations(activitiesArray[position]);
                                    } else if (activitiesArray[position].equals(getString(R.string.anxiety_gad7_title))) {
                                        loadRegressionRecommendations(activitiesArray[position]);
                                    } else if (activitiesArray[position].equals(getString(R.string.daily_review_title))) {
                                        loadRegressionRecommendations(activitiesArray[position]);
                                    } else if (activitiesArray[position].equals(getString(R.string.fitbit_title))) {
                                        //inputType = TIMER;
                                        //configureChipGroups();
                                    } else if (activitiesArray[position].equals(getString(R.string.googlefit_title))) {
                                        //inputType = TIMER;
                                        //configureChipGroups();
                                    } else if (activitiesArray[position].equals(getString(R.string.moabi_tracker_title))) {
                                        //inputType = TIMER;
                                        //configureChipGroups();
                                    } else if (activitiesArray[position].equals(getString(R.string.phone_usage_title))) {
                                        //inputType = TIMER;
                                        //configureChipGroups();
                                    } else if (activitiesArray[position].equals(getString(R.string.weather_title))) {
                                        //inputType = TIMER;
                                        //configureChipGroups();
                                    } else if (activitiesArray[position].equals(getString(R.string.timer_title))) {
                                        //inputType = TIMER;
                                        //configureChipGroups();
                                    } else if (activitiesArray[position].equals(getString(R.string.baactivity_title))) {
                                        //inputType = TIMER;
                                        //configureChipGroups();
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void loadRegressionRecommendations(String depVar) {
        Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<SimpleRegressionSummary> regressionSummaries =
                        predictionsRepository.getAllMindSummariesNow(
                                formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(
                                        formattedTime.getCurrentDateAsYYYYMMDD(),
                                        28 - 1),
                                formattedTime.getCurrentTimeInMilliSecs(), 28);
                Collections.sort(regressionSummaries, new SimpleRegressionSummary.BestFitComparator());
                List<SimpleRegressionSummary> simpleRegressionSummaryList = new ArrayList<>();
                if (regressionSummaries.size() > 0) {
                    if (depVar.equals(getString(R.string.mood_title))) {
                        for (SimpleRegressionSummary simpleRegressionSummary : regressionSummaries) {
                            if (simpleRegressionSummary.getDepVar().equals(getString(R.string.mood_camel_case))) {
                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                    if (simpleRegressionSummary.getIndepVar().equals(getString(R.string.phone_usage_total_title))) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                    }
                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                }/* else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                        if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 1) {
                                            sortedList.add(simpleRegressionSummary);
                                        }
                                    }*/ else {
                                    simpleRegressionSummaryList.add(simpleRegressionSummary);
                                }
                            }
                            //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                        }
                    } else if (depVar.equals(getString(R.string.energy_title))) {
                        for (SimpleRegressionSummary simpleRegressionSummary : regressionSummaries) {
                            if (simpleRegressionSummary.getDepVar().equals(getString(R.string.energy_camel_case))) {
                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                    if (simpleRegressionSummary.getIndepVar().equals(getString(R.string.phone_usage_total_title))) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                    }
                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                }/* else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                        if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 1) {
                                            sortedList.add(simpleRegressionSummary);
                                        }
                                    }*/ else {
                                    simpleRegressionSummaryList.add(simpleRegressionSummary);
                                }
                            }
                            //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                        }
                    } else if (depVar.equals(getString(R.string.daily_review_title))) {
                        for (SimpleRegressionSummary simpleRegressionSummary : regressionSummaries) {
                            if (simpleRegressionSummary.getDepVar().equals(getString(R.string.daily_review_camel_case))) {
                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                    if (simpleRegressionSummary.getIndepVar().equals(getString(R.string.phone_usage_total_title))) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                    }
                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                }/* else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                        if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 1) {
                                            sortedList.add(simpleRegressionSummary);
                                        }
                                    }*/ else {
                                    simpleRegressionSummaryList.add(simpleRegressionSummary);
                                }
                            }
                            //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                        }
                    } else if (depVar.equals(getString(R.string.stress_title))) {
                        for (SimpleRegressionSummary simpleRegressionSummary : regressionSummaries) {
                            if (simpleRegressionSummary.getDepVar().equals(getString(R.string.stress_camel_case))) {
                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                    if (simpleRegressionSummary.getIndepVar().equals(getString(R.string.phone_usage_total_title))) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                    }
                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                }/* else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                        if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 1) {
                                            sortedList.add(simpleRegressionSummary);
                                        }
                                    }*/ else {
                                    simpleRegressionSummaryList.add(simpleRegressionSummary);
                                }
                            }
                            //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                        }
                    } else if (depVar.equals(getString(R.string.depression_phq9_title))) {
                        for (SimpleRegressionSummary simpleRegressionSummary : regressionSummaries) {
                            if (simpleRegressionSummary.getDepVar().equals(getString(R.string.depression_phq9_camel_case))) {
                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                    if (simpleRegressionSummary.getIndepVar().equals(getString(R.string.phone_usage_total_title))) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                    }
                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                }/* else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                        if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 1) {
                                            sortedList.add(simpleRegressionSummary);
                                        }
                                    }*/ else {
                                    simpleRegressionSummaryList.add(simpleRegressionSummary);
                                }
                            }
                            //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                        }
                    } else if (depVar.equals(getString(R.string.anxiety_gad7_title))) {
                        for (SimpleRegressionSummary simpleRegressionSummary : regressionSummaries) {
                            if (simpleRegressionSummary.getDepVar().equals(getString(R.string.anxiety_gad7_camel_case))) {
                                if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.phone_usage_camel_case))) {
                                    if (simpleRegressionSummary.getIndepVar().equals(getString(R.string.phone_usage_total_title))) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                    } else if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                        //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()));
                                    }
                                } else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.timer_camel_case))) {
                                    if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 5) {
                                        simpleRegressionSummaryList.add(simpleRegressionSummary);
                                    }
                                }/* else if (simpleRegressionSummary.getIndepVarType().equals(getString(R.string.baactivity_camel_case))) {
                                        if (TimeUnit.MILLISECONDS.toMinutes(simpleRegressionSummary.getRecommendedActivityLevel().longValue()) >= 1) {
                                            sortedList.add(simpleRegressionSummary);
                                        }
                                    }*/ else {
                                    simpleRegressionSummaryList.add(simpleRegressionSummary);
                                }
                            }
                            //Log.i(TAG, simpleRegressionSummary.getDepXIndepVars() + ": " + simpleRegressionSummary.getCoefOfDetermination() + " - " + simpleRegressionSummary.getRecommendedActivityLevel());
                        }
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            greetingsTextView.setVisibility(View.VISIBLE);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, 0, 0, 0);
                            explanationTextView.setLayoutParams(params);
                            explanationTextView.setText(getString(R.string.activity_daily_briefing_recommendations_ready_msg));
                            for (int i = 0; i < simpleRegressionSummaryList.size(); i++) {
                                InsightRecommendationItem insightRecommendationItem = new InsightRecommendationItem(simpleRegressionSummaryList.get(i), i, DailyBriefingActivity.this);
                                Log.i(TAG, simpleRegressionSummaryList.get(i).getDepXIndepVars()
                                        + ": " + simpleRegressionSummaryList.get(i).getCoefOfDetermination()
                                        + " - " + simpleRegressionSummaryList.get(i).getRecommendedActivityLevel());
                                recommendationItemItemAdapter.add(insightRecommendationItem);
                            }
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            greetingsTextView.setVisibility(View.GONE);
                            int sizeInDP = 24;
                            int marginInDp = (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP, sizeInDP, getResources()
                                            .getDisplayMetrics());
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, marginInDp, 0, 0);
                            explanationTextView.setLayoutParams(params);
                            explanationTextView.setText(getString(R.string.empty_insight_analysis_body));
                        }
                    });

                }
            }
        }).start();
    }
}
