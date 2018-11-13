package com.ivorybridge.moabi.ui.recyclerviewitem.activitytracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitActivitySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDeviceStatusSummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitSleepSummary;
import com.ivorybridge.moabi.ui.views.SeekArc;
import com.ivorybridge.moabi.viewmodel.FitbitViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FitbitItem extends AbstractItem<FitbitItem, FitbitItem.ViewHolder> {

    private String mDate;
    private Fragment mFragment;
    private String unit;

    public FitbitItem(Fragment fragment, String date, String unit) {
        mFragment = fragment;
        mDate = date;
        this.unit = unit;
    }

    @Override
    public int getType() {
        return R.id.fitbit_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_activitytracker_rv_item;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View v) {
        return new FitbitItem.ViewHolder(v);
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<FitbitItem> {

        private static final String TAG = FitbitItem.class.getSimpleName();
        @BindView(R.id.rv_item_activitytracker_item_device_description_imageview)
        ImageView deviceImageView;
        @BindView(R.id.rv_item_activitytracker_item_device_description_textview)
        TextView deviceTextView;
        @BindView(R.id.rv_item_activitytracker_item_first_activity_progressbar)
        SeekArc firstActProgressBar;
        @BindView(R.id.rv_item_activitytracker_item_first_activity_status_textview)
        TextView firstActStatusTextView;
        @BindView(R.id.rv_item_activitytracker_item_first_activity_goal_textview)
        TextView firstActGoalTextView;
        @BindView(R.id.rv_item_activitytracker_item_first_activity_name_textview)
        TextView firstActNameTextView;
        @BindView(R.id.rv_item_activitytracker_item_second_activity_status_textview)
        TextView secondActStatusTextView;
        @BindView(R.id.rv_item_activitytracker_item_second_activity_unit_textview)
        TextView secondActUnitTextView;
        @BindView(R.id.rv_item_activitytracker_item_third_activity_status_textview)
        TextView thirdActStatusTextView;
        @BindView(R.id.rv_item_activitytracker_item_third_activity_unit_textview)
        TextView thirdActUnitTextView;
        @BindView(R.id.rv_item_activitytracker_item_fourth_activity_status_textview)
        TextView fourthActStatusTextView;
        @BindView(R.id.rv_item_activitytracker_item_fourth_activity_unit_textview)
        TextView fourthActUnitTextView;
        @BindView(R.id.rv_item_activitytracker_item_first_activity_relativelayout)
        RelativeLayout firstActRL;
        @BindView(R.id.rv_item_activitytracker_item_second_activity_linearlayout)
        LinearLayout secondActRL;
        @BindView(R.id.rv_item_activitytracker_item_third_activity_linearlayout)
        LinearLayout thirdActRL;
        @BindView(R.id.rv_item_activitytracker_item_fourth_activity_linearlayout)
        LinearLayout fourthActRL;
        @BindView(R.id.rv_item_activitytracker_item_lastsynctime_refresh_image)
        ImageView refreshImageView;
        @BindView(R.id.rv_item_activitytracker_item_lastsynctime_textview)
        TextView lastSyncTextTimeView;
        @BindView(R.id.rv_item_activitytracker_item_lastsynctime_linearlayout)
        LinearLayout lastSyncTimeLL;
        private SharedPreferences fitbitSharedPreferences;
        private SharedPreferences.Editor fitbitSPEditor;
        private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;
        FitbitViewModel viewModel;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(@NonNull FitbitItem item, @NonNull List<Object> payloads) {

            Animation rotatingAnimation = new RotateAnimation(0.0f, 360.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            rotatingAnimation.setRepeatCount(Animation.INFINITE);
            rotatingAnimation.setDuration(2000);
            fitbitSharedPreferences = itemView.getContext().getSharedPreferences(
                    itemView.getContext().getString(R.string.com_ivorybridge_moabi_FITBIT_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
            fitbitSPEditor = fitbitSharedPreferences.edit();
            configureOnClickEvents();

            viewModel = ViewModelProviders.of(item.mFragment).get(FitbitViewModel.class);
            viewModel.getAll().observe(item.mFragment, new Observer<List<FitbitDailySummary>>() {
                @Override
                public void onChanged(@Nullable List<FitbitDailySummary> fitbitDailySummaries) {
                    if (fitbitDailySummaries != null) {
                        Map<String, Object> activitySummaryMap = new LinkedHashMap<>();
                        Map<String, Object> goalMap = new LinkedHashMap<>();
                        for (FitbitDailySummary dailySummary : fitbitDailySummaries) {
                            if (dailySummary != null && dailySummary.getDate().equals(item.mDate)) {
                                FitbitActivitySummary activitySummary = dailySummary.getActivitySummary();
                                FitbitSleepSummary sleepSummary = dailySummary.getSleepSummary();
                                FitbitDeviceStatusSummary deviceSummary = dailySummary.getDeviceStatusSummary();
                                if (activitySummary != null) {
                                    Log.i(TAG, activitySummary.toString());
                                    if (activitySummary.getSummary().getSteps() != null) {
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_steps_title), activitySummary.getSummary().getSteps());
                                    } else {
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_steps_title), 0L);
                                    }
                                    if (activitySummary.getSummary().getCaloriesOut() != null) {
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_calories_title), activitySummary.getSummary().getCaloriesOut());
                                    } else {
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_calories_title), 0L);
                                    }
                                    if (activitySummary.getSummary().getFloors() != null) {
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_floors_title), activitySummary.getSummary().getFloors());
                                    } else {
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_floors_title), 0L);
                                    }
                                    if (activitySummary.getSummary().getSedentaryMinutes() != null) {
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_sedentary_minutes_title), activitySummary.getSummary().getSedentaryMinutes());
                                    } else {
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_sedentary_minutes_title), 0L);
                                    }
                                    if (activitySummary.getSummary().getDistances().get(0) != null && activitySummary.getSummary().getDistances().get(0).getDistance() != null) {
                                        Double distance = activitySummary.getSummary().getDistances().get(0).getDistance();
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_distance_title), distance);
                                    } else {
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_distance_title), 0f);
                                    }
                                    if (sleepSummary != null && sleepSummary.getSummary().getTotalMinutesAsleep() != null) {
                                        Log.i(TAG, sleepSummary.toString());
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_sleep_title), sleepSummary.getSummary().getTotalMinutesAsleep());
                                    } else {
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_sleep_title), 0L);
                                    }
                                    if (activitySummary.getSummary().getFairlyActiveMinutes() != null && activitySummary.getSummary().getVeryActiveMinutes() != null) {
                                        Long activeMins = activitySummary.getSummary().getFairlyActiveMinutes() + activitySummary.getSummary().getVeryActiveMinutes();
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_active_minutes_title), activeMins);
                                    } else {
                                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_active_minutes_title), 0L);
                                    }
                                    //Log.i(TAG, activitySummaryMap.toString());
                                    if (activitySummary.getGoals().getSteps() != null) {
                                        goalMap.put(itemView.getContext().getString(R.string.activity_steps_title), activitySummary.getGoals().getSteps());
                                    } else {
                                        goalMap.put(itemView.getContext().getString(R.string.activity_steps_title), 10000L);
                                    }
                                    if (activitySummary.getGoals().getCaloriesOut() != null) {
                                        goalMap.put(itemView.getContext().getString(R.string.activity_calories_title), activitySummary.getGoals().getCaloriesOut());
                                    } else {
                                        goalMap.put(itemView.getContext().getString(R.string.activity_calories_title), 2000L);
                                    }
                                    if (activitySummary.getGoals().getFloors() != null) {
                                        goalMap.put(itemView.getContext().getString(R.string.activity_floors_title), activitySummary.getGoals().getFloors());
                                    } else {
                                        goalMap.put(itemView.getContext().getString(R.string.activity_floors_title), 10L);
                                    }
                                    goalMap.put(itemView.getContext().getString(R.string.activity_sedentary_minutes_title), 720L);
                                    if (activitySummary.getGoals().getDistance() != null) {
                                        Double distance = activitySummary.getGoals().getDistance();
                                        goalMap.put(itemView.getContext().getString(R.string.activity_distance_title), distance);
                                    } else {
                                        goalMap.put(itemView.getContext().getString(R.string.activity_distance_title), 5d);
                                    }
                                    if (activitySummary.getGoals().getActiveMinutes() != null) {
                                        Long activeMins = activitySummary.getGoals().getActiveMinutes();
                                        goalMap.put(itemView.getContext().getString(R.string.activity_active_minutes_title), activeMins);
                                    } else {
                                        goalMap.put(itemView.getContext().getString(R.string.activity_active_minutes_title), 65L);
                                    }
                                    goalMap.put(itemView.getContext().getString(R.string.activity_sleep_title), 420L);
                                    //Log.i(TAG, goalMap.toString());
                                    if (deviceSummary != null) {
                                        Log.i(TAG, deviceSummary.toString());
                                        //Log.i(TAG, item.mDate + ": " + deviceSummary.toString());
                                        String deviceVer = null;
                                        String lastSyncTime;
                                        if (deviceSummary.getDeviceVersion() != null) {
                                            deviceVer = deviceSummary.getDeviceVersion();
                                        }
                                        deviceImageView.setImageResource(R.drawable.ic_fitbit_logo);
                                        String lastSyncTimeHHMM = "";
                                        deviceTextView.setText(deviceVer);
                                        if (item.mDate.equals(setUpDatesForToday())) {
                                            if (deviceSummary.getLastSyncTime() != null) {
                                                lastSyncTime = deviceSummary.getLastSyncTime();
                                                if (lastSyncTime != null) {
                                                    lastSyncTimeHHMM = lastSyncTime.substring(11, lastSyncTime.length() - 7);
                                                }
                                                SimpleDateFormat truncatedDF = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                                                SimpleDateFormat timeOnlyDF = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
                                                try {
                                                    Date truncatedDate = truncatedDF.parse(lastSyncTimeHHMM);
                                                    String timeOnly = timeOnlyDF.format(truncatedDate);
                                                    lastSyncTextTimeView.setText(timeOnly);
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        } else {
                                            lastSyncTextTimeView.setText(item.mDate);
                                        }
                                    } else {
                                        lastSyncTextTimeView.setText(item.mDate);
                                        deviceImageView.setImageResource(R.drawable.ic_fitbit_logo);
                                    }
                                }
                            }
                        }


                        displayFitbitDataFromLocalDatabase(activitySummaryMap, goalMap, item.unit);
                        onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                            @Override
                            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                                displayFitbitDataFromLocalDatabase(activitySummaryMap, goalMap, item.unit);
                            }
                        };
                        fitbitSharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
                    }
                }
            });
        }

        private void displayFitbitDataFromLocalDatabase(Map<String, Object> summaryMap, Map<String, Object> goalMap, String unit) {
            Log.i(TAG, summaryMap.toString());
            Log.i(TAG, goalMap.toString());
            List<String> selectedActivitiesInOrder = new ArrayList<>();
            Log.i(TAG, "Displaying fitbit data from local database");
            String defaultFirstValue = itemView.getContext().getString(R.string.activity_steps_title);
            String defaultSecondValue = itemView.getContext().getString(R.string.activity_active_minutes_title);
            String defaultThirdValue = itemView.getContext().getString(R.string.activity_distance_title);
            String defaultFourthValue = itemView.getContext().getString(R.string.activity_sleep_title);
            selectedActivitiesInOrder.add(fitbitSharedPreferences.getString(itemView.getContext().getString(
                    R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), defaultFirstValue));
            selectedActivitiesInOrder.add(fitbitSharedPreferences.getString(itemView.getContext().getString(
                    R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY), defaultSecondValue));
            selectedActivitiesInOrder.add(fitbitSharedPreferences.getString(itemView.getContext().getString(
                    R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY), defaultThirdValue));
            selectedActivitiesInOrder.add(fitbitSharedPreferences.getString(itemView.getContext().getString(
                    R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY), defaultFourthValue));

            long activityStatusLong, activityGoalLong;
            double activityStatusDouble, activityGoalDouble;
            float activityProgress;
            int activityPercent;
            String activityStatusString = "";
            String activityGoalString = "";
            String activityUnitString = "";

            int i = 0;
            for (String selectedActivity : selectedActivitiesInOrder) {
                Log.i(TAG, selectedActivity);
                if (summaryMap.get(selectedActivity) != null) {
                    if (selectedActivity.equals(itemView.getContext().getString(R.string.activity_distance_title))) {
                        if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                            activityStatusDouble = (double) summaryMap.get(selectedActivity);
                            activityGoalDouble = (double) goalMap.get(selectedActivity);
                            activityProgress = (float) activityStatusDouble / (float) activityGoalDouble * 10000;
                            activityStatusString = String.format(Locale.US, "%.2f", activityStatusDouble);
                            activityGoalString = String.format(Locale.US, "%.2f", activityGoalDouble) + " " + itemView.getContext().getString(R.string.unit_distance_si);
                            activityUnitString = " " + itemView.getContext().getString(R.string.unit_distance_si);
                        } else {
                            activityStatusDouble = (double) summaryMap.get(selectedActivity) * 0.62137119;
                            activityGoalDouble = (double) goalMap.get(selectedActivity) * 0.62137119;
                            activityProgress = (float) activityStatusDouble / (float) activityGoalDouble * 10000;
                            activityStatusString = String.format(Locale.US, "%.2f", activityStatusDouble);
                            activityGoalString = String.format(Locale.US, "%.2f", activityGoalDouble) + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                            activityUnitString = " " + itemView.getContext().getString(R.string.unit_distance_usc);
                        }
                    } else {
                        activityStatusLong = (long) summaryMap.get(selectedActivity);
                        activityGoalLong = (long) goalMap.get(selectedActivity);
                        activityProgress = (float) activityStatusLong / (float) activityGoalLong * 10000;
                        activityStatusString = "" + activityStatusLong;
                        activityGoalString = "" + activityGoalLong;

                        if (selectedActivity.equals(itemView.getContext().getString(R.string.activity_sleep_title))) {
                            if (activityStatusLong > 1) {
                                activityUnitString = " " + itemView.getContext().getString(R.string.unit_time_plur);
                            } else {
                                activityUnitString = " " + itemView.getContext().getString(R.string.unit_time_sing);
                            }
                        } else if (selectedActivity.equals(itemView.getContext().getString(R.string.activity_active_minutes_title))) {
                            if (activityStatusLong > 1) {
                                activityUnitString = " " + itemView.getContext().getString(R.string.unit_time_plur);
                            } else {
                                activityUnitString = " " + itemView.getContext().getString(R.string.unit_time_sing);
                            }
                        } else if (selectedActivity.equals(itemView.getContext().getString(R.string.activity_sedentary_minutes_title))) {
                            if (activityStatusLong > 1) {
                                activityUnitString = " " + itemView.getContext().getString(R.string.unit_time_plur);
                            } else {
                                activityUnitString = " " + itemView.getContext().getString(R.string.unit_time_sing);
                            }
                        } else if (selectedActivity.equals(itemView.getContext().getString(R.string.activity_steps_title))) {
                            if (activityStatusLong > 1) {
                                activityUnitString = " " + itemView.getContext().getString(R.string.unit_step_plur);
                            } else {
                                activityUnitString = " " + itemView.getContext().getString(R.string.unit_step_sing);
                            }
                        } else if (selectedActivity.equals(itemView.getContext().getString(R.string.activity_calories_title))) {
                            activityUnitString = " " + itemView.getContext().getString(R.string.unit_calories);
                        } else if (selectedActivity.equals(itemView.getContext().getString(R.string.activity_floors_title))) {
                            if (activityStatusLong > 1) {
                                activityUnitString = " " + itemView.getContext().getString(R.string.unit_floor_plur);
                            } else {
                                activityUnitString = " " + itemView.getContext().getString(R.string.unit_floor_sing);
                            }
                        }
                    }
                    activityPercent = (int) Math.ceil(activityProgress);

                    if (i == 0) {
                        String goalToDisplay = activityGoalString + activityUnitString;
                        firstActStatusTextView.setText(activityStatusString);
                        firstActGoalTextView.setText(goalToDisplay);
                        firstActNameTextView.setText(selectedActivity);
                        firstActProgressBar.setProgress(activityPercent);
                        /*
                        ObjectAnimator firstActAnimation = ObjectAnimator.ofInt(firstActProgressBar, "Progress", 0, activityPercent);
                        firstActAnimation.end();
                        firstActAnimation.setStartDelay(500);
                        firstActAnimation.setDuration(2000);
                        firstActAnimation.start();*/
                    } else {
                        switch (i) {
                            case 1:
                                secondActStatusTextView.setText(activityStatusString);
                                secondActUnitTextView.setText(activityUnitString);
                                break;
                            case 2:
                                thirdActStatusTextView.setText(activityStatusString);
                                thirdActUnitTextView.setText(activityUnitString);
                                break;
                            case 3:
                                fourthActStatusTextView.setText(activityStatusString);
                                fourthActUnitTextView.setText(activityUnitString);
                                break;
                        }
                    }
                }
                i++;
            }
        }

        private void configureOnClickEvents() {
            firstActRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(itemView.getContext())
                            .title(itemView.getContext().getString(R.string.tracker_choose_four_activities_prompt))
                            .items(R.array.fitbit_activities)
                            .itemsCallbackMultiChoice(
                                    new Integer[]{},
                                    (dialog, which, text) -> {
                                        boolean allowSelectionChange =
                                                which.length
                                                        <= 4;
                                        return allowSelectionChange;
                                    })
                            .onNegative((dialog, which) -> dialog.dismiss())
                            .onPositive((dialog, which) -> {
                                if (dialog.getSelectedIndices() != null) {
                                    if (dialog.getSelectedIndices().length == 4) {
                                        Log.i(TAG, "Selected indices: " + Arrays.toString(dialog.getSelectedIndices()));
                                        ArrayList<CharSequence> selectionList = dialog.getItems();
                                        ArrayList<String> selectionStringList = new ArrayList<>();
                                        for (Integer selectedIndex : dialog.getSelectedIndices()) {
                                            if (selectionList != null) {
                                                selectionStringList.add(selectionList.get(selectedIndex).toString());
                                            }
                                        }
                                        fitbitSPEditor.putString(itemView.getContext().getString(
                                                R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),
                                                selectionStringList.get(0));
                                        //fitbitSPEditor.apply();
                                        fitbitSPEditor.putString(itemView.getContext().getString(
                                                R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY),
                                                selectionStringList.get(1));
                                        //fitbitSPEditor.apply();
                                        fitbitSPEditor.putString(itemView.getContext().getString(
                                                R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY),
                                                selectionStringList.get(2));
                                        //fitbitSPEditor.apply();
                                        fitbitSPEditor.putString(itemView.getContext().getString(
                                                R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY),
                                                selectionStringList.get(3));
                                        //fitbitSPEditor.apply();
                                        fitbitSPEditor.commit();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(itemView.getContext(), itemView.getContext().getString(R.string.tracker_please_choose_four_activities_prompt), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .autoDismiss(false)
                            .positiveText(itemView.getContext().getString(R.string.done_title))
                            .negativeText(itemView.getContext().getString(R.string.cancel_title))
                            .alwaysCallMultiChoiceCallback() // the callback will always be called, to check if (un)selection is still allowed
                            .show();
                }
            });
            String defaultFirstValue = itemView.getContext().getString(R.string.activity_steps_title);
            String defaultSecondValue = itemView.getContext().getString(R.string.activity_active_minutes_title);
            String defaultThirdValue = itemView.getContext().getString(R.string.activity_distance_title);
            String defaultFourthValue = itemView.getContext().getString(R.string.activity_sleep_title);
            secondActRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstActivity = fitbitSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), defaultFirstValue);
                    String secondActivity = fitbitSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY), defaultSecondValue);
                    fitbitSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),
                            secondActivity);
                    fitbitSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY),
                            firstActivity);
                    fitbitSPEditor.commit();
                }
            });
            thirdActRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstActivity = fitbitSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), defaultFirstValue);
                    String thirdActivity = fitbitSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY), defaultThirdValue);
                    fitbitSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),
                            thirdActivity);
                    fitbitSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY),
                            firstActivity);
                    fitbitSPEditor.commit();
                }
            });
            fourthActRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstActivity = fitbitSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), defaultFirstValue);
                    String fourthActivity = fitbitSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY), defaultFourthValue);
                    fitbitSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),
                            fourthActivity);
                    fitbitSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY),
                            firstActivity);
                    fitbitSPEditor.commit();
                }
            });
        }

        @Override
        public void unbindView(@NonNull FitbitItem item) {
            //fitbitTodayRef.removeEventListener(valueEventListener);
        }

        private String setUpDatesForToday() {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mDFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return mDFormat.format(calendar.getTime());
        }
    }
}
