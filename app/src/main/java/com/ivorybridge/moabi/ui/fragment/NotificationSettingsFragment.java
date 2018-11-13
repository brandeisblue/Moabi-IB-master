package com.ivorybridge.moabi.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.service.MotionSensorService;
import com.ivorybridge.moabi.service.UserGoalJob;
import com.ivorybridge.moabi.service.UserGoalPeriodicJob;

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

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        setPreferencesFromResource(R.xml.settings_preference_notifications, s);
        boolean isTimerNotifEnabled = notificationSharedPreferences.getBoolean(
                getString(R.string.preference_timer_notification), false);
        boolean isFitnessNotifEnabled = notificationSharedPreferences.getBoolean(
                getString(R.string.preference_fitness_tracker_notification), false);
        boolean isPersonalGoalNotifEnabled = notificationSharedPreferences.getBoolean(
                getString(R.string.preference_personal_goal_notification), false);
        boolean isCheckInNotifEnabled = notificationSharedPreferences.getBoolean(
                getString(R.string.preference_daily_check_in_notification), false);
        boolean isDailyCheersNotifEnabled = notificationSharedPreferences.getBoolean(
                getString(R.string.preference_daily_cheers_notification), false);

        final SwitchPreference timerSwitchPref = (SwitchPreference)
                findPreference("timer_preference");
        final SwitchPreference fitnessSwitchPref = (SwitchPreference)
                findPreference("fitness_tracker_preference");
        final SwitchPreference personalGoalSwitchPref = (SwitchPreference)
                findPreference("personal_goal_preference");
        final SwitchPreference dailyCheckInSwitchPref = (SwitchPreference)
                findPreference("daily_check_in_preference");
        final SwitchPreference dailyCheersSwitchPref = (SwitchPreference)
                findPreference("daily_cheers_preference");

        timerSwitchPref.setChecked(isTimerNotifEnabled);
        fitnessSwitchPref.setChecked(isFitnessNotifEnabled);
        personalGoalSwitchPref.setChecked(isPersonalGoalNotifEnabled);
        dailyCheckInSwitchPref.setChecked(isCheckInNotifEnabled);
        dailyCheersSwitchPref.setChecked(isDailyCheersNotifEnabled);

        timerSwitchPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean isChecked = (Boolean) newValue;
                timerSwitchPref.setChecked(isChecked);
                notificationSPEditor.putBoolean(getString(R.string.preference_timer_notification), isChecked);
                notificationSPEditor.commit();
                return false;
            }
        });

        fitnessSwitchPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean isChecked = (Boolean) newValue;
                notificationSPEditor.putBoolean(getString(R.string.preference_fitness_tracker_notification), isChecked);
                notificationSPEditor.commit();
                fitnessSwitchPref.setChecked(isChecked);
                MotionSensorService motionSensorService = new MotionSensorService();
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
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        notificationSharedPreferences = context.getSharedPreferences(
                getString(R.string.com_ivorybridge_mobai_NOTIFICATION_SHARED_PREFERENCE),
                Context.MODE_PRIVATE);
        notificationSPEditor = notificationSharedPreferences.edit();
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
                if (preference instanceof PreferenceCategory)
                    setZeroPaddingToLayoutChildren(holder.itemView);
                else {
                    View iconFrame = holder.itemView.findViewById(R.id.icon_frame);
                    if (iconFrame != null) {
                        iconFrame.setVisibility(preference.getIcon() == null ? View.GONE : View.VISIBLE);
                    }
                }
            }
        };
    }
}
