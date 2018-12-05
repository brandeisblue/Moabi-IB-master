package com.ivorybridge.moabi.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.DataInUseMediatorLiveData;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.entity.util.UserGoal;
import com.ivorybridge.moabi.service.CheckInDailyJob;
import com.ivorybridge.moabi.service.MotionSensorService;
import com.ivorybridge.moabi.service.StopwatchService;
import com.ivorybridge.moabi.service.UserGoalJob;
import com.ivorybridge.moabi.service.UserGoalPeriodicJob;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.ivorybridge.moabi.viewmodel.UserGoalViewModel;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import androidx.core.util.Pair;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationSettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = NotificationSettingsFragment.class.getSimpleName();
    private SharedPreferences notificationSharedPreferences;
    private SharedPreferences.Editor notificationSPEditor;
    private DataInUseViewModel dataInUseViewModel;
    private SwitchPreference fitnessSwitchPref;
    private ListPreference trackerSourcePref;
    private ListPreference measureListPref;
    private Preference checkInTimePref;
    private Boolean isFitnessNotifEnabled;
    private boolean isTimerNotifEnabled;
    private boolean isCheckInNotifEnabled;
    private boolean isPersonalGoalNotifEnabled;
    private boolean isDailyCheersNotifEnabled;
    private UserGoalViewModel userGoalViewModel;
    private SwitchPreference timerSwitchPref;
    private SwitchPreference personalGoalSwitchPref;
    private SwitchPreference dailyCheckInSwitchPref;
    private SwitchPreference dailyCheersSwitchPref;
    private FormattedTime formattedTime;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        setPreferencesFromResource(R.xml.settings_preference_notifications, s);
        notificationSharedPreferences = getContext().getSharedPreferences(
                getString(R.string.com_ivorybridge_mobai_NOTIFICATION_SHARED_PREFERENCE),
                Context.MODE_PRIVATE);
        notificationSPEditor = notificationSharedPreferences.edit();
        formattedTime = new FormattedTime();
        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);
        userGoalViewModel = ViewModelProviders.of(this).get(UserGoalViewModel.class);
        timerSwitchPref = (SwitchPreference)
                findPreference("timer_preference");
        fitnessSwitchPref = (SwitchPreference)
                findPreference("fitness_tracker_preference");
        personalGoalSwitchPref = (SwitchPreference)
                findPreference("personal_goal_preference");
        dailyCheckInSwitchPref = (SwitchPreference)
                findPreference("daily_check_in_preference");
        dailyCheersSwitchPref = (SwitchPreference)
                findPreference("cheers_preference");
        dailyCheersSwitchPref.setVisible(false);
        trackerSourcePref = (ListPreference)
        findPreference("fitness_tracker_source_preference");
        measureListPref = (ListPreference)
        findPreference("fitness_tracker_measures_preference");
        checkInTimePref = findPreference("daily_check_in_time_preference");

        timerSwitchPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean isChecked = (Boolean) newValue;
                timerSwitchPref.setChecked(isChecked);
                notificationSPEditor.putBoolean(getString(R.string.preference_timer_notification), isChecked);
                notificationSPEditor.commit();
                if (isChecked) {
                    if (StopwatchService.TimeContainer.getInstance().isServiceRunning.get()) {
                        getContext().startService(new Intent(getActivity(), StopwatchService.class));
                    } else {
                        if (getActivity() != null) {
                            getContext().stopService(new Intent(getActivity(), StopwatchService.class));
                        }
                    }
                }
                return false;
            }
        });

        fitnessSwitchPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean isChecked = (Boolean) newValue;
                notificationSPEditor.putBoolean(getString(R.string.preference_fitness_tracker_notification), isChecked);
                notificationSPEditor.commit();
                setTrackerNotif();
                MotionSensorService motionSensorService = new MotionSensorService();
                if (getActivity() != null) {
                    Intent motionServiceIntent = new Intent(getActivity(), MotionSensorService.class);
                    getActivity().startService(motionServiceIntent);
                }
                return false;
            }
        });

        trackerSourcePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String service = (String) newValue;
                if (service != null) {
                    measureListPref.setEnabled(true);
                    notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_source_notification), service);
                    notificationSPEditor.commit();
                    notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_activity_type_notification), getString(R.string.activity_steps_title));
                    notificationSPEditor.commit();
                    measureListPref.setSummary(getString(R.string.activity_steps_title));
                    measureListPref.setValue(getString(R.string.activity_steps_title));
                    if (service.equals(getString(R.string.fitbit_title))) {
                        measureListPref.setEntries(R.array.fitbit_activities);
                        measureListPref.setEntryValues(R.array.fitbit_activities);
                    } else if (service.equals(getString(R.string.googlefit_title))) {
                        measureListPref.setEntries(R.array.googlefit_activities);
                        measureListPref.setEntryValues(R.array.googlefit_activities);
                    } else if (service.equals(getString(R.string.moabi_tracker_title))) {
                        measureListPref.setEntries(R.array.built_in_fitness_activities);
                        measureListPref.setEntryValues(R.array.built_in_fitness_activities);
                    }
                    int index = trackerSourcePref.findIndexOfValue(service);
                    trackerSourcePref.setValueIndex(index);
                    trackerSourcePref.setSummary(service);
                    if (getActivity() != null) {
                        Intent motionServiceIntent = new Intent(getActivity(), MotionSensorService.class);
                        getActivity().startService(motionServiceIntent);
                    }
                }
                return false;
            }
        });

        measureListPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String measure = (String) newValue;
                notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_activity_type_notification), measure);
                notificationSPEditor.commit();
                measureListPref.setValue(measure);
                measureListPref.setSummary(measure);
                if (getActivity() != null) {
                    Intent motionServiceIntent = new Intent(getActivity(), MotionSensorService.class);
                    getActivity().startService(motionServiceIntent);
                }
                return false;
            }
        });

        personalGoalSwitchPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean isChecked = (Boolean) newValue;
                notificationSPEditor.putBoolean(getString(R.string.preference_personal_goal_notification), isChecked);
                notificationSPEditor.commit();
                personalGoalSwitchPref.setChecked(isChecked);
                UserGoalJob.runJobImmediately();
                UserGoalPeriodicJob.schedulePeriodicJob();
                return false;
            }
        });

        userGoalViewModel.getGoal(0).observe(this, new Observer<UserGoal>() {
            @Override
            public void onChanged(UserGoal userGoal) {
                if (userGoal != null) {
                    String indepVarType = userGoal.getGoalType();
                    String goal = userGoal.getGoalName();
                    personalGoalSwitchPref.setSummary(
                            getString(R.string.preference_current_recommended_goal_notification_summary)
                + " - " + goal);
                }
            }
        });

        dailyCheckInSwitchPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean isChecked = (Boolean) newValue;
                dailyCheckInSwitchPref.setChecked(isChecked);
                notificationSPEditor.putBoolean(getString(R.string.preference_daily_check_in_notification), isChecked);
                notificationSPEditor.commit();
                if (isChecked) {
                    checkInTimePref.setEnabled(true);
                    int hour = notificationSharedPreferences.getInt(getString(R.string.preference_daily_check_in_hour), 20);
                    int minute = notificationSharedPreferences.getInt(getString(R.string.preference_daily_check_in_minute), 0);
                    CheckInDailyJob.scheduleJob(hour, minute);
                } else {
                    checkInTimePref.setEnabled(false);
                }
                return false;
            }
        });

        int hour = notificationSharedPreferences.getInt(getString(R.string.preference_daily_check_in_hour), 20);
        int minute = notificationSharedPreferences.getInt(getString(R.string.preference_daily_check_in_minute), 0);
        String time = hour + ":" + minute;
        checkInTimePref.setSummary(formattedTime.convertStringHMToHMMAA(time));

        checkInTimePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                                Log.i(TAG, hourOfDay + ":" + minute);
                                notificationSPEditor.putInt(getString(R.string.preference_daily_check_in_hour), hourOfDay);
                                notificationSPEditor.putInt(getString(R.string.preference_daily_check_in_minute), minute);
                                notificationSPEditor.commit();
                                String time = hourOfDay + ":" + minute;
                                checkInTimePref.setSummary(formattedTime.convertStringHMToHMMAA(time));
                                CheckInDailyJob.scheduleJob(hourOfDay, minute);
                            }
                        },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE), false);
                tpd.show(getChildFragmentManager(), "Timepickerdialog");
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isTimerNotifEnabled = notificationSharedPreferences.getBoolean(
                getString(R.string.preference_timer_notification), true);
        isPersonalGoalNotifEnabled = notificationSharedPreferences.getBoolean(
                getString(R.string.preference_personal_goal_notification), true);
        isCheckInNotifEnabled = notificationSharedPreferences.getBoolean(
                getString(R.string.preference_daily_check_in_notification), true);
        isDailyCheersNotifEnabled = notificationSharedPreferences.getBoolean(
                getString(R.string.preference_daily_cheers_notification), false);
        String tracker = notificationSharedPreferences.getString(getString(R.string.preference_fitness_tracker_source_notification), null);
        String measure = notificationSharedPreferences.getString(getString(R.string.preference_fitness_tracker_activity_type_notification),
                null);

        if (tracker != null && measure != null) {
            trackerSourcePref.setSummary(tracker);
            trackerSourcePref.setValue(tracker);
            measureListPref.setEnabled(true);
            measureListPref.setValue(measure);
            measureListPref.setSummary(measure);
        } else {
            trackerSourcePref.setSummary("");
            trackerSourcePref.setValue(null);
            measureListPref.setValue(null);
            measureListPref.setSummary("");
        }

        if (isCheckInNotifEnabled) {
            checkInTimePref.setEnabled(true);
        } else {
            checkInTimePref.setEnabled(false);
        }

        setTrackerNotif();
        timerSwitchPref.setChecked(isTimerNotifEnabled);
        personalGoalSwitchPref.setChecked(isPersonalGoalNotifEnabled);
        dailyCheckInSwitchPref.setChecked(isCheckInNotifEnabled);
        dailyCheersSwitchPref.setChecked(isDailyCheersNotifEnabled);
    }

    private void setZeroPaddingToLayoutChildren(View view) {
        if (!(view instanceof ViewGroup))
            return;
        ViewGroup viewGroup = (ViewGroup) view;
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            setZeroPaddingToLayoutChildren(viewGroup.getChildAt(i));
            viewGroup.setPaddingRelative(0, viewGroup.getPaddingTop(), viewGroup.getPaddingEnd(), viewGroup.getPaddingBottom());
        }
    }

    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen) {
            @SuppressLint("RestrictedApi")
            @Override
            public void onBindViewHolder(PreferenceViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                Preference preference = getItem(position);
                if (preference instanceof PreferenceCategory) {
                    //setZeroPaddingToLayoutChildren(holder.itemView);
                }
                else {
                    View iconFrame = holder.itemView.findViewById(R.id.icon_frame);
                    if (iconFrame != null) {
                        iconFrame.setVisibility(preference.getIcon() == null ? View.GONE : View.VISIBLE);
                    }
                }
            }
        };
    }

    private void setTrackerNotif() {
        isFitnessNotifEnabled = notificationSharedPreferences.getBoolean(
                getString(R.string.preference_fitness_tracker_notification), false);
        DataInUseMediatorLiveData dataInUseMediatorLiveData = new DataInUseMediatorLiveData(
                dataInUseViewModel.getAllInputsInUse(), dataInUseViewModel.getAllConnectedServices());
        dataInUseMediatorLiveData.observe(this, new Observer<Pair<List<InputInUse>, List<ConnectedService>>>() {
            @Override
            public void onChanged(Pair<List<InputInUse>, List<ConnectedService>> listListPair) {
                Set<String> activitiesSet = new TreeSet<>();
                if (listListPair.first != null && listListPair.first.size() > 0 &&
                        listListPair.second != null && listListPair.second.size() > 0) {
                    for (InputInUse inputInUse: listListPair.first) {
                        if (inputInUse.isInUse()) {
                            for (ConnectedService connectedService: listListPair.second) {
                                if (connectedService.getName().equals(inputInUse.getName()) &&
                                        connectedService.isConnected()) {
                                    if (connectedService.getName().equals(getString(R.string.fitbit_camel_case))) {
                                        activitiesSet.add("3" + connectedService.getName());
                                    } else if (connectedService.getName().equals(getString(R.string.googlefit_camel_case))) {
                                        activitiesSet.add("2" + connectedService.getName());
                                    } else if (connectedService.getName().equals(getString(R.string.moabi_tracker_camel_case))) {
                                        activitiesSet.add("0" + connectedService.getName());
                                    }
                                }
                            }
                        }
                    }
                    String[] activitiesArray = activitiesSet.toArray(new String[0]);
                    if (activitiesArray.length > 0) {
                        fitnessSwitchPref.setEnabled(true);
                        if (!isFitnessNotifEnabled) {
                            fitnessSwitchPref.setChecked(false);
                            trackerSourcePref.setEnabled(false);
                            trackerSourcePref.setSummary("");
                            measureListPref.setEnabled(false);
                            measureListPref.setSummary("");
                        } else {
                            fitnessSwitchPref.setChecked(true);
                            trackerSourcePref.setEnabled(true);
                            measureListPref.setEnabled(true);
                            for (int i = 0; i < activitiesArray.length; i++) {
                                if (activitiesArray[i].equals("3" + getString(R.string.fitbit_camel_case))) {
                                    activitiesArray[i] = getString(R.string.fitbit_title);
                                } else if (activitiesArray[i].equals("2" + getString(R.string.googlefit_camel_case))) {
                                    activitiesArray[i] = getString(R.string.googlefit_title);
                                } else if (activitiesArray[i].equals("0" + getString(R.string.moabi_tracker_camel_case))) {
                                    activitiesArray[i] = getString(R.string.moabi_tracker_title);
                                }
                            }
                            trackerSourcePref.setEntries(activitiesArray);
                            trackerSourcePref.setEntryValues(activitiesArray);
                            setMeasureListPref();
                        }
                    } else {
                        fitnessSwitchPref.setEnabled(false);
                        fitnessSwitchPref.setChecked(false);
                        trackerSourcePref.setEnabled(false);
                        measureListPref.setEnabled(false);
                    }
                }
            }
        });
    }

    private void setMeasureListPref() {
        String tracker = trackerSourcePref.getValue();
        if (tracker != null) {
            measureListPref.setEnabled(true);
            if (tracker.equals(getString(R.string.fitbit_title))) {
                measureListPref.setEntries(R.array.fitbit_activities);
                measureListPref.setEntryValues(R.array.fitbit_activities);
            } else if (tracker.equals(getString(R.string.googlefit_title))) {
                measureListPref.setEntries(R.array.googlefit_activities);
                measureListPref.setEntryValues(R.array.googlefit_activities);
            } else if (tracker.equals(getString(R.string.moabi_tracker_title))) {
                measureListPref.setEntries(R.array.built_in_fitness_activities);
                measureListPref.setEntryValues(R.array.built_in_fitness_activities);
            }
        } else {
            measureListPref.setEnabled(false);
        }
    }
}
