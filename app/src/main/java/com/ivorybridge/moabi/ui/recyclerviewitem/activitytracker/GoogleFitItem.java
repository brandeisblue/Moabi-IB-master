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
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitGoal;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.ui.views.SeekArc;
import com.ivorybridge.moabi.viewmodel.GoogleFitViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;

public class GoogleFitItem extends AbstractItem<GoogleFitItem, GoogleFitItem.ViewHolder> {

    private Fragment mFragment;
    private String mDate;
    private String unit;

    public GoogleFitItem(Fragment fragment, String date, String unit) {
        mFragment = fragment;
        mDate = date;
        this.unit = unit;
    }

    @Override
    public int getType() {
        return R.id.googlefit_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_activitytracker_rv_item;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View v) {
        return new GoogleFitItem.ViewHolder(v);
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<GoogleFitItem> {

        private static final String TAG = GoogleFitItem.class.getSimpleName();
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
        LinearLayout secondActLL;
        @BindView(R.id.rv_item_activitytracker_item_third_activity_linearlayout)
        LinearLayout thirdActLL;
        @BindView(R.id.rv_item_activitytracker_item_fourth_activity_linearlayout)
        LinearLayout fourthActLL;
        @BindView(R.id.rv_item_activitytracker_item_lastsynctime_refresh_image)
        ImageView refreshImageView;
        @BindView(R.id.rv_item_activitytracker_item_lastsynctime_textview)
        TextView lastSyncTextTimeView;
        @BindView(R.id.rv_item_activitytracker_item_lastsynctime_linearlayout)
        LinearLayout lastSyncTimeLL;
        private SharedPreferences googleFitSharedPreferences;
        private SharedPreferences.Editor googleFitSPEditor;
        private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final GoogleFitItem item, List<Object> payloads) {

            deviceImageView.setImageResource(R.drawable.ic_googlefit);
            deviceTextView.setText(R.string.googlefit_title);
            firstActProgressBar.setProgressColor(ContextCompat.getColor(itemView.getContext(), R.color.reduction_orange));

            googleFitSharedPreferences = itemView.getContext().getSharedPreferences(
                    itemView.getContext().getString(R.string.com_ivorybridge_moabi_GOOGLEFIT_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
            googleFitSPEditor = googleFitSharedPreferences.edit();

            configureOnClickEvents();

            final Animation rotatingAnimation = new RotateAnimation(0.0f, 360.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            rotatingAnimation.setRepeatCount(Animation.INFINITE);
            rotatingAnimation.setDuration(2000);

            GoogleFitViewModel viewModel = ViewModelProviders.of(item.mFragment).get(GoogleFitViewModel.class);
            viewModel.get(item.mDate).observe(item.mFragment, new Observer<GoogleFitSummary>() {
                @Override
                public void onChanged(@Nullable GoogleFitSummary googleFitSummary) {
                    if (googleFitSummary != null) {
                        Log.i(TAG, googleFitSummary.toString());
                        List<GoogleFitSummary.Summary> summaries = new ArrayList<>();
                        List<GoogleFitGoal> goals = new ArrayList<>();
                        Map<String, Object> activitySummaryMap = new LinkedHashMap<>();
                        Map<String, Object> goalMap = new LinkedHashMap<>();
                        if (googleFitSummary.getSummaries() != null) {
                            summaries = googleFitSummary.getSummaries();
                        }
                        if (googleFitSummary.getGoals() != null) {
                            goals = googleFitSummary.getGoals();
                        }
                        if (googleFitSummary.getLastSyncTime() != null && item.mDate.equals(setUpDatesForToday())) {
                            lastSyncTextTimeView.setText(googleFitSummary.getLastSyncTime());
                        } else {
                            lastSyncTextTimeView.setText(item.mDate);
                        }
                        Long steps = 0L;
                        Double stepsGoal = 5000d;
                        Double calories = 0d;
                        Double caloriesGoal = 2000d;
                        Double distance = 0d;
                        Double distanceGoal = 5000d;
                        Long sedentaryMinutes = 0L;
                        Long sedentaryMinutesGoal = 43200000L;
                        Long activeMinutes = 0L;
                        Long activeMinutesGoal = 3600000L;
                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_steps_title), steps);
                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_distance_title), distance / 1000.0);
                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_calories_title), calories.longValue());
                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_active_minutes_title), TimeUnit.MILLISECONDS.toMinutes(activeMinutes));
                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_sedentary_minutes_title), TimeUnit.MILLISECONDS.toMinutes(sedentaryMinutes));
                        if (summaries.size() > 0) {
                            for (GoogleFitSummary.Summary summary : summaries) {
                                if (summary.getName().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                                    steps = summary.getValue().longValue();
                                    activitySummaryMap.put(itemView.getContext().getString(R.string.activity_steps_title), steps);
                                } else if (summary.getName().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                                    distance = summary.getValue();
                                    activitySummaryMap.put(itemView.getContext().getString(R.string.activity_distance_title), distance / 1000.0);
                                } else if (summary.getName().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                                    calories = summary.getValue();
                                    activitySummaryMap.put(itemView.getContext().getString(R.string.activity_calories_title), calories.longValue());
                                } else if (summary.getName().equals(itemView.getContext().getString(R.string.activity_active_minutes_camel_case))) {
                                    activeMinutes = summary.getValue().longValue();
                                    long activeMinsInMins = TimeUnit.MILLISECONDS.toMinutes(activeMinutes);
                                    activitySummaryMap.put(itemView.getContext().getString(R.string.activity_active_minutes_title), activeMinsInMins);
                                } else if (summary.getName().equals(itemView.getContext().getString(R.string.activity_sedentary_minutes_camel_case))) {
                                    sedentaryMinutes = summary.getValue().longValue();
                                    long sedentaryMinsInMins = TimeUnit.MILLISECONDS.toMinutes(sedentaryMinutes);
                                    activitySummaryMap.put(itemView.getContext().getString(R.string.activity_sedentary_minutes_title), sedentaryMinsInMins);
                                }
                            }
                        }
                        goalMap.put(itemView.getContext().getString(R.string.activity_steps_title), stepsGoal.longValue());
                        goalMap.put(itemView.getContext().getString(R.string.activity_distance_title), distanceGoal / 1000.0);
                        goalMap.put(itemView.getContext().getString(R.string.activity_calories_title), caloriesGoal.longValue());
                        goalMap.put(itemView.getContext().getString(R.string.activity_active_minutes_title), TimeUnit.MILLISECONDS.toMinutes(activeMinutesGoal));
                        goalMap.put(itemView.getContext().getString(R.string.activity_sedentary_minutes_title), TimeUnit.MILLISECONDS.toMinutes(sedentaryMinutesGoal));
                        if (goals.size() > 0) {
                            for (GoogleFitGoal goal : goals) {
                                String goalString = "";
                                if (goal.getName().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                                    goalString = goal.getGoal();
                                    stepsGoal = Double.parseDouble(goalString);
                                    goalMap.put(itemView.getContext().getString(R.string.activity_steps_title), stepsGoal.longValue());
                                } else if (goal.getName().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                                    goalString = goal.getGoal();
                                    distanceGoal = Double.parseDouble(goalString);
                                    goalMap.put(itemView.getContext().getString(R.string.activity_distance_title), distanceGoal / 1000.0);
                                } else if (goal.getName().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                                    goalString = goal.getGoal();
                                    caloriesGoal = Double.parseDouble(goalString);
                                    goalMap.put(itemView.getContext().getString(R.string.activity_calories_title), caloriesGoal.longValue());
                                } else if (goal.getName().equals(itemView.getContext().getString(R.string.activity_active_minutes_camel_case))) {
                                    goalString = goal.getGoal();
                                    activeMinutesGoal = Long.parseLong(goalString);
                                    long activeMinsInMins = TimeUnit.MILLISECONDS.toMinutes(activeMinutesGoal);
                                    goalMap.put(itemView.getContext().getString(R.string.activity_active_minutes_title), activeMinsInMins);
                                } else if (goal.getName().equals(itemView.getContext().getString(R.string.activity_sedentary_minutes_camel_case))) {
                                    goalString = goal.getGoal();
                                    activeMinutesGoal = Long.parseLong(goalString);
                                    long sedentaryMinsInMins = TimeUnit.MILLISECONDS.toMinutes(activeMinutesGoal);
                                    goalMap.put(itemView.getContext().getString(R.string.activity_sedentary_minutes_title), sedentaryMinsInMins);
                                }
                            }
                        }


                        displayGoogleFitDataFromLocalDatabase(activitySummaryMap, goalMap, item.unit);
                        onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                            @Override
                            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                                displayGoogleFitDataFromLocalDatabase(activitySummaryMap, goalMap, item.unit);
                            }
                        };
                        googleFitSharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
                    }
                }
            });
        }

        private void displayGoogleFitDataFromLocalDatabase(final Map<String, Object> summaryMap,
                                                           final Map<String, Object> goalMap, String unit) {

            Log.i(TAG, summaryMap.toString());
            Log.i(TAG, goalMap.toString());
            List<String> selectedActivitiesInOrder = new ArrayList<>();
            Log.i(TAG, "Displaying googleFit data from local database");
            String defaultFirstValue = itemView.getContext().getString(R.string.activity_steps_title);
            String defaultSecondValue = itemView.getContext().getString(R.string.activity_active_minutes_title);
            String defaultThirdValue = itemView.getContext().getString(R.string.activity_distance_title);
            String defaultFourthValue = itemView.getContext().getString(R.string.activity_calories_title);
            selectedActivitiesInOrder.add(googleFitSharedPreferences.getString(itemView.getContext().getString(
                    R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), defaultFirstValue));
            selectedActivitiesInOrder.add(googleFitSharedPreferences.getString(itemView.getContext().getString(
                    R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY), defaultSecondValue));
            selectedActivitiesInOrder.add(googleFitSharedPreferences.getString(itemView.getContext().getString(
                    R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY), defaultThirdValue));
            selectedActivitiesInOrder.add(googleFitSharedPreferences.getString(itemView.getContext().getString(
                    R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY), defaultFourthValue));

            long activityStatusLong = 0;
            double activityStatusDouble = 0;
            long activityGoalLong = 0;
            double activityGoalDouble = 0;
            float activityProgress = 0;
            int activityPercent = 0;
            String activityStatusString;
            String activityGoalString;
            String activityUnitString = "";

            int i = 0;
            for (String selectedActivity : selectedActivitiesInOrder) {
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
                            .items(R.array.googlefit_activities)
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
                                        googleFitSPEditor.putString(itemView.getContext().getString(
                                                R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),
                                                selectionStringList.get(0));
                                        //googleFitSPEditor.apply();
                                        googleFitSPEditor.putString(itemView.getContext().getString(
                                                R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY),
                                                selectionStringList.get(1));
                                        //googleFitSPEditor.apply();
                                        googleFitSPEditor.putString(itemView.getContext().getString(
                                                R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY),
                                                selectionStringList.get(2));
                                        //googleFitSPEditor.apply();
                                        googleFitSPEditor.putString(itemView.getContext().getString(
                                                R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY),
                                                selectionStringList.get(3));
                                        //googleFitSPEditor.apply();
                                        googleFitSPEditor.commit();
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
            String defaultFourthValue = itemView.getContext().getString(R.string.activity_calories_title);

            secondActLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstActivity = googleFitSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), defaultFirstValue);
                    String secondActivity = googleFitSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY), defaultSecondValue);
                    googleFitSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),
                            secondActivity);
                    googleFitSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY),
                            firstActivity);
                    googleFitSPEditor.commit();
                }
            });

            thirdActLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstActivity = googleFitSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), defaultFirstValue);
                    String thirdActivity = googleFitSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY), defaultThirdValue);
                    googleFitSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),
                            thirdActivity);
                    googleFitSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY),
                            firstActivity);
                    googleFitSPEditor.commit();
                }
            });

            fourthActLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstActivity = googleFitSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), defaultFirstValue);
                    String fourthActivity = googleFitSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY), defaultFourthValue);
                    googleFitSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),
                            fourthActivity);
                    googleFitSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY),
                            firstActivity);
                    googleFitSPEditor.commit();
                }
            });
        }

        @Override
        public void unbindView(@NonNull GoogleFitItem item) {
            //GoogleFitTodayRef.removeEventListener(valueEventListener);
        }

        private String setUpDatesForToday() {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return mdformat.format(calendar.getTime());
        }
    }
}
