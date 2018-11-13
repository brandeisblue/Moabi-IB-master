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
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.ui.views.SeekArc;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.BuiltInFitnessViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BuiltInTrackerItem extends AbstractItem<BuiltInTrackerItem, BuiltInTrackerItem.ViewHolder> {

    private String mDate;
    private Fragment mFragment;
    private String unit;

    public BuiltInTrackerItem(Fragment fragment, String date, String unit) {
        mFragment = fragment;
        mDate = date;
        this.unit = unit;
    }

    @Override
    public int getType() {
        return R.id.built_in_fitness_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_activitytracker_rv_item;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View v) {
        return new BuiltInTrackerItem.ViewHolder(v);
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<BuiltInTrackerItem> {

        private static final String TAG = BuiltInTrackerItem.class.getSimpleName();
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
        private SharedPreferences builtInFitnessSharedPreferences;
        private SharedPreferences.Editor builtInFitnessSPEditor;
        private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;
        private BuiltInFitnessViewModel viewModel;
        private FormattedTime formattedTime;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(@NonNull BuiltInTrackerItem item, @NonNull List<Object> payloads) {

            Animation rotatingAnimation = new RotateAnimation(0.0f, 360.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            rotatingAnimation.setRepeatCount(Animation.INFINITE);
            rotatingAnimation.setDuration(2000);
            builtInFitnessSharedPreferences = itemView.getContext().getSharedPreferences(
                    itemView.getContext().getString(R.string.com_ivorybridge_moabi_BUILT_IN_FITNESS_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
            builtInFitnessSPEditor = builtInFitnessSharedPreferences.edit();
            formattedTime = new FormattedTime();
            configureOnClickEvents();

            viewModel = ViewModelProviders.of(item.mFragment).get(BuiltInFitnessViewModel.class);
            viewModel.get(item.mDate).observe(item.mFragment, new Observer<BuiltInActivitySummary>() {
                @Override
                public void onChanged(@Nullable BuiltInActivitySummary builtInActivitySummary) {
                    if (builtInActivitySummary != null) {
                        Map<String, Object> activitySummaryMap = new LinkedHashMap<>();
                        Map<String, Object> goalMap = new LinkedHashMap<>();
                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_steps_title), builtInActivitySummary.getSteps());
                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_calories_title), builtInActivitySummary.getCalories().longValue());
                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_sedentary_minutes_title), TimeUnit.MILLISECONDS.toMinutes(builtInActivitySummary.getSedentaryMinutes()));
                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_active_minutes_title), TimeUnit.MILLISECONDS.toMinutes(builtInActivitySummary.getActiveMinutes()));
                        activitySummaryMap.put(itemView.getContext().getString(R.string.activity_distance_title), builtInActivitySummary.getDistance());
                        goalMap.put(itemView.getContext().getString(R.string.activity_steps_title), 10000L);
                        goalMap.put(itemView.getContext().getString(R.string.activity_calories_title), 2000L);
                        goalMap.put(itemView.getContext().getString(R.string.activity_distance_title), 5d);
                        goalMap.put(itemView.getContext().getString(R.string.activity_active_minutes_title), 65L);
                        goalMap.put(itemView.getContext().getString(R.string.activity_sedentary_minutes_title), 720L);
                        deviceImageView.setImageResource(R.drawable.ic_logo_monogram_colored);
                        String lastSyncTimeHHMM = "";
                        deviceTextView.setText(itemView.getContext().getString(R.string.moabi_tracker_title));
                        if (item.mDate.equals(formattedTime.getCurrentDateAsYYYYMMDD())) {
                            lastSyncTextTimeView.setText(formattedTime.convertLongToHHMMaa(builtInActivitySummary.getTimeOfEntry()));
                        } else {
                            lastSyncTextTimeView.setText(item.mDate);
                        }
                        displayDataFromLocalDatabase(activitySummaryMap, goalMap, item.unit);
                        onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                            @Override
                            public void onSharedPreferenceChanged(SharedPreferences
                                                                          sharedPreferences, String key) {
                                displayDataFromLocalDatabase(activitySummaryMap, goalMap, item.unit);
                            }
                        };
                        builtInFitnessSharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
                    }
                }
            });
        }
 

        private void displayDataFromLocalDatabase(Map<String, Object> summaryMap, Map<String, Object> goalMap, String unit) {

            List<String> selectedActivitiesInOrder = new ArrayList<>();
            Log.i(TAG, "Displaying built in fitness data from local database");
            String defaultFirstValue = itemView.getContext().getString(R.string.activity_steps_title);
            String defaultSecondValue = itemView.getContext().getString(R.string.activity_active_minutes_title);
            String defaultThirdValue = itemView.getContext().getString(R.string.activity_distance_title);
            String defaultFourthValue = itemView.getContext().getString(R.string.activity_calories_title);
            selectedActivitiesInOrder.add(builtInFitnessSharedPreferences.getString(itemView.getContext().getString(
                    R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), defaultFirstValue));
            selectedActivitiesInOrder.add(builtInFitnessSharedPreferences.getString(itemView.getContext().getString(
                    R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY), defaultSecondValue));
            selectedActivitiesInOrder.add(builtInFitnessSharedPreferences.getString(itemView.getContext().getString(
                    R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY), defaultThirdValue));
            selectedActivitiesInOrder.add(builtInFitnessSharedPreferences.getString(itemView.getContext().getString(
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
                            activityStatusDouble = (double) summaryMap.get(selectedActivity) / 1000;
                            activityGoalDouble = (double) goalMap.get(selectedActivity);
                            activityProgress = (float) activityStatusDouble / (float) activityGoalDouble * 10000;
                            activityStatusString = String.format(Locale.US, "%.2f", activityStatusDouble);
                            activityGoalString = String.format(Locale.US, "%.2f", activityGoalDouble) + " " + itemView.getContext().getString(R.string.unit_distance_si);
                            activityUnitString = " " + itemView.getContext().getString(R.string.unit_distance_si);
                        } else {
                            activityStatusDouble = (double) summaryMap.get(selectedActivity) / 1000 * 0.62137119;
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
                            .items(R.array.built_in_fitness_activities)
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
                                        builtInFitnessSPEditor.putString(itemView.getContext().getString(
                                                R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),
                                                selectionStringList.get(0));
                                        //builtInFitnessSPEditor.apply();
                                        builtInFitnessSPEditor.putString(itemView.getContext().getString(
                                                R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY),
                                                selectionStringList.get(1));
                                        //builtInFitnessSPEditor.apply();
                                        builtInFitnessSPEditor.putString(itemView.getContext().getString(
                                                R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY),
                                                selectionStringList.get(2));
                                        //builtInFitnessSPEditor.apply();
                                        builtInFitnessSPEditor.putString(itemView.getContext().getString(
                                                R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY),
                                                selectionStringList.get(3));
                                        //builtInFitnessSPEditor.apply();
                                        builtInFitnessSPEditor.commit();
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
            secondActRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstActivity = builtInFitnessSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), defaultFirstValue);
                    String secondActivity = builtInFitnessSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY), defaultSecondValue);
                    builtInFitnessSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),
                            secondActivity);
                    builtInFitnessSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_SECOND_ACTIVITY_TO_DISPLAY_KEY),
                            firstActivity);
                    builtInFitnessSPEditor.commit();
                }
            });
            thirdActRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstActivity = builtInFitnessSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), defaultFirstValue);
                    String thirdActivity = builtInFitnessSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY), defaultThirdValue);
                    builtInFitnessSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),
                            thirdActivity);
                    builtInFitnessSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_THIRD_ACTIVITY_TO_DISPLAY_KEY),
                            firstActivity);
                    builtInFitnessSPEditor.commit();
                }
            });
            fourthActRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstActivity = builtInFitnessSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY), defaultFirstValue);
                    String fourthActivity = builtInFitnessSharedPreferences.getString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY), defaultFourthValue);
                    builtInFitnessSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FIRST_ACTIVITY_TO_DISPLAY_KEY),
                            fourthActivity);
                    builtInFitnessSPEditor.putString(itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_FOURTH_ACTIVITY_TO_DISPLAY_KEY),
                            firstActivity);
                    builtInFitnessSPEditor.commit();
                }
            });
        }

        @Override
        public void unbindView(@NonNull BuiltInTrackerItem item) {
            //fitbitTodayRef.removeEventListener(valueEventListener);
        }
    }
}

