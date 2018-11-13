package com.ivorybridge.moabi.ui.fragment;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.AsyncCallsMasterRepository;
import com.ivorybridge.moabi.ui.activity.AdvancedSettingsActivity;
import com.ivorybridge.moabi.ui.activity.ConnectServicesActivity;
import com.ivorybridge.moabi.ui.activity.EditActivitiesActivity;
import com.ivorybridge.moabi.ui.activity.EditSurveyItemsActivity;
import com.ivorybridge.moabi.util.FormattedTime;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class SettingsFragment extends PreferenceFragmentCompat implements
        TimePickerDialog.OnTimeSetListener,
        Preference.OnPreferenceClickListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private SharedPreferences notificationPreference;
    private static final String NOTIFICATION_TOKEN_PREFERENCES = "NotifTokenPrefs";
    private Context mContext;
    private AsyncCallsMasterRepository asyncCallsMasterRepository;
    private FirebaseManager firebaseManager;
    private FormattedTime formattedTime;
    private SharedPreferences unitSharedPreferences;
    private SharedPreferences.Editor unitSPEditor;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.settings_preference);
        firebaseManager = new FirebaseManager();
        formattedTime = new FormattedTime();
        if (getActivity() != null) {
            asyncCallsMasterRepository = new AsyncCallsMasterRepository((AppCompatActivity) getActivity(), formattedTime.getCurrentDateAsYYYYMMDD());
        }

        String unit = getString(R.string.preference_unit_si_title);
        if (getContext() != null) {
            unitSharedPreferences = getContext().getSharedPreferences(
                    getString(R.string.com_ivorybridge_moabi_UNIT_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
            unitSPEditor = unitSharedPreferences.edit();
            unit = unitSharedPreferences.getString(getString(R.string.com_ivorybridge_mobai_UNIT_KEY),
                    getString(R.string.preference_unit_si_title));
        }

        ListPreference unitPreference = (ListPreference) findPreference("preference_unit");
        unitPreference.setTitle(getString(R.string.preference_unit_title));
        if (unit != null) {
            if (unit.equals(getString(R.string.preference_unit_si_title))) {
                unitPreference.setValueIndex(0);
                //unitPreference.setSummary(getString(R.string.preference_unit_si_summary));
            } else {
                unitPreference.setValueIndex(1);
               // unitPreference.setTitle(getString(R.string.preference_unit_title));
                //unitPreference.setSummary(getString(R.string.preference_unit_usc_summary));
            }
        }

        findPreference("preference_unit").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String newUnit = (String) o;
                Log.i(TAG, newUnit);
                if (newUnit.equals(getString(R.string.preference_unit_si_summary))) {
                    unitPreference.setValueIndex(0);
                    unitPreference.setTitle(getString(R.string.preference_unit_title));
                    //unitPreference.setSummary(getString(R.string.preference_unit_si_summary));
                    unitSPEditor.putString(getString(R.string.com_ivorybridge_mobai_UNIT_KEY),
                            getString(R.string.preference_unit_si_title));
                } else {
                    unitPreference.setValueIndex(1);
                    unitPreference.setTitle(getString(R.string.preference_unit_title));
                    //unitPreference.setSummary(getString(R.string.preference_unit_usc_summary));
                    unitSPEditor.putString(getString(R.string.com_ivorybridge_mobai_UNIT_KEY),
                            getString(R.string.preference_unit_usc_title));
                }
                unitSPEditor.apply();
                return false;
            }
        });

        findPreference("preference_profile_screen").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), AdvancedSettingsActivity.class);
                intent.putExtra("settings_type", getString(R.string.settings_profile_title));
                getActivity().startActivity(intent);
                return false;
            }
        });

        findPreference("preference_notifications_screen").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), AdvancedSettingsActivity.class);
                intent.putExtra("settings_type", getString(R.string.settings_notifications_title));
                getActivity().startActivity(intent);
                return false;
            }
        });
        findPreference("preference_apps_and_services").setOnPreferenceClickListener(this);
        findPreference("preference_surveys").setOnPreferenceClickListener(this);
        findPreference("preference_edit_activities").setOnPreferenceClickListener(this);
        findPreference("preference_sync").setOnPreferenceClickListener(this);
        final SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) findPreference("preference_phone_usage_permission");
        if (isUsagePermissionGranted(mContext)) {
            switchPreference.setChecked(true);
        } else {
            switchPreference.setChecked(false);
        }
        //TODO - add a way to revoke access when the switch is turned off.

        switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!switchPreference.isChecked()) {
                    if (!isUsagePermissionGranted(mContext)) {
                        mContext.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                    } else {
                        Toast.makeText(mContext, "Data usage access is already granted", Toast.LENGTH_SHORT).show();
                        firebaseManager.getIsConnectedRef().child(getString(R.string.phone_usage_camel_case)).setValue(true);
                        firebaseManager.getInputsInUseRef().child(getString(R.string.phone_usage_camel_case)).setValue(true);
                        switchPreference.setChecked(true);
                    }
                } else {
                    switchPreference.setChecked(false);
                    firebaseManager.getIsConnectedRef().child(getString(R.string.phone_usage_camel_case)).setValue(false);
                    firebaseManager.getInputsInUseRef().child(getString(R.string.phone_usage_camel_case)).setValue(false);
                }
                return false;
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        notificationPreference
                = mContext.getSharedPreferences(NOTIFICATION_TOKEN_PREFERENCES, Context.MODE_PRIVATE);
    }


    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        SharedPreferences.Editor editor = notificationPreference.edit();
        editor.putInt("survey_notif_hour", hourOfDay);
        editor.putInt("survey_notif_min", minute);
        editor.apply();
        editor.commit();
        scheduleNotification();
    }

    private void scheduleNotification() {
        /*
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, notificationPreference.getInt("survey_notif_hour", 22));
        calendar.set(Calendar.MINUTE, notificationPreference.getInt("survey_notif_min", 0));
        calendar.set(Calendar.SECOND, 0);
        Intent intent1 = new Intent(mContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);*/
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onPreferenceClick(Preference preference) {

        String key = preference.getKey();
        Log.i("TAG", key);
        if (key.equals("key4")) {
            Log.i("TAG", "time picker was clicked");
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    SettingsFragment.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false);
            tpd.setVersion(TimePickerDialog.Version.VERSION_1);
            if (getContext() != null) {
                tpd.setAccentColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            }
            if (getActivity() != null) {
                // todo - deprecated method below... tpd.show() requires old fragment manager.
                //tpd.show(getFragmentManager(), "Timepickerdialog");
            }
        }
        if (key.equals("preference_apps_and_services")) {
            Intent connectDevicesIntent = new Intent(mContext, ConnectServicesActivity.class);
            startActivity(connectDevicesIntent);

        } if (key.equals("preference_surveys")) {
            Intent userInputsIntent = new Intent(mContext, EditSurveyItemsActivity.class);
            startActivity(userInputsIntent);
        } if (key.equals("preference_edit_activities")) {
            Intent editActivityIntent = new Intent(mContext, EditActivitiesActivity.class);
            startActivity(editActivityIntent);
        } if (key.equals("preference_sync")) {
            asyncCallsMasterRepository.sync();
        }
        return false;
    }

    private boolean isUsagePermissionGranted(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = -1;
        try {
            if (appOps != null) {
                mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), context.getPackageName());
            }
            return mode == MODE_ALLOWED;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}