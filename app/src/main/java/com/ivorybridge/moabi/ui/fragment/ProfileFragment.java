package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInProfile;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.BuiltInFitnessViewModel;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileFragment extends Fragment {

    private BuiltInFitnessViewModel builtInFitnessViewModel;

    private static final String TAG = ProfileFragment.class.getSimpleName();
    @BindView(R.id.fragment_profile_user_imageview)
    ImageView profileImageView;
    @BindView(R.id.fragment_profile_user_name)
    TextView nameTextView;
    @BindView(R.id.fragment_profile_user_email_or_id)
    TextView emailOrIdTextView;
    @BindView(R.id.fragment_profile_gender_text)
    TextView genderTextView;
    @BindView(R.id.fragment_profile_dob_text)
    TextView dobTextView;
    @BindView(R.id.fragment_profile_height_text)
    TextView heightTextView;
    @BindView(R.id.fragment_profile_weight_text)
    TextView weightTextView;
    @BindView(R.id.fragment_profile_gender_layout)
    RelativeLayout genderLayout;
    @BindView(R.id.fragment_profile_dob_layout)
    RelativeLayout dobLayout;
    @BindView(R.id.fragment_profile_height_layout)
    RelativeLayout heightLayout;
    @BindView(R.id.fragment_profile_weight_layout)
    RelativeLayout weightLayout;
    private BuiltInFitnessRepository builtInFitnessRepository;
    private FormattedTime formattedTime;
    private SharedPreferences unitSharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener unitPrefChangeListener;
    private NumberPicker.OnValueChangeListener picker1ValueChangeListener;
    private NumberPicker.OnValueChangeListener picker2ValueChangeListener;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, mView);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (getContext() != null) {
            unitSharedPreferences = getContext().
                    getSharedPreferences(
                            getString(R.string.com_ivorybridge_moabi_UNIT_SHARED_PREFERENCE_KEY),
                            Context.MODE_PRIVATE);
            if (currentUser != null) {
                Picasso.get()
                        .load(currentUser.getPhotoUrl())
                        .placeholder(R.drawable.ic_profile_outline_black)
                        .error(R.drawable.ic_profile_outline_black)
                        .into(profileImageView);
                nameTextView.setText(currentUser.getDisplayName());
                if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                    emailOrIdTextView.setText(currentUser.getEmail());
                } else if (currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().isEmpty()) {
                    emailOrIdTextView.setText(currentUser.getPhoneNumber());
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {
                                builtInFitnessRepository = new BuiltInFitnessRepository(getActivity().getApplication());
                                List<BuiltInProfile> builtInProfiles = builtInFitnessRepository.getUserProfileNow();
                                BuiltInProfile profile = builtInProfiles.get(0);
                                emailOrIdTextView.setText(profile.getUniqueID());
                            }
                        }
                    }).start();
                }
            } else {
                profileImageView.setImageResource(R.drawable.ic_profile_outline_black);
                nameTextView.setVisibility(View.GONE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            builtInFitnessRepository = new BuiltInFitnessRepository(getActivity().getApplication());
                            List<BuiltInProfile> builtInProfiles = builtInFitnessRepository.getUserProfileNow();
                            BuiltInProfile profile = builtInProfiles.get(0);
                            emailOrIdTextView.setText(profile.getUniqueID());
                        }
                    }
                }).start();
            }
            builtInFitnessViewModel = ViewModelProviders.of(this).get(BuiltInFitnessViewModel.class);
            if (getContext() != null) {
                builtInFitnessViewModel.getUserProfile().observe(this, new Observer<List<BuiltInProfile>>() {
                    @Override
                    public void onChanged(List<BuiltInProfile> builtInProfiles) {
                        if (builtInProfiles != null && builtInProfiles.size() > 0) {
                            unitSharedPreferences = getContext().getSharedPreferences(getString(R.string.com_ivorybridge_moabi_UNIT_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
                            String unit = unitSharedPreferences.getString(getString(R.string.com_ivorybridge_mobai_UNIT_KEY), getContext().getString(R.string.preference_unit_si_title));
                            BuiltInProfile profile = builtInProfiles.get(0);
                            if (currentUser == null) {
                                emailOrIdTextView.setText(profile.getUniqueID());
                            }
                            String gender = profile.getGender();
                            if (gender == null || gender.isEmpty()) {
                                gender = "";
                            }
                            genderTextView.setText(gender);
                            genderLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    final String[] array = getResources().getStringArray(R.array.sex_array);
                                    builder.setTitle(getString(R.string.profile_sex_title))
                                            .setItems(array, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (getActivity() != null) {
                                                                builtInFitnessRepository = new BuiltInFitnessRepository(getActivity().getApplication());
                                                                List<BuiltInProfile> builtInProfiles = builtInFitnessRepository.getUserProfileNow();
                                                                BuiltInProfile profile = builtInProfiles.get(0);
                                                                profile.setGender(array[which]);
                                                                builtInFitnessRepository.insert(profile);
                                                            }
                                                        }
                                                    }).start();
                                                }
                                            });
                                    builder.create().show();
                                }
                            });
                            String dob = profile.getDateOfBirth();
                            if (dob == null || dob.isEmpty()) {
                                dob = "";
                            }
                            dobTextView.setText(dob);
                            dobLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Calendar now = Calendar.getInstance();
                                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                                            new DatePickerDialog.OnDateSetListener() {
                                                @Override
                                                public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                                    final String birthday = year + "/" + monthOfYear + 1+ "/" + dayOfMonth;
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (getActivity() != null) {
                                                                builtInFitnessRepository = new BuiltInFitnessRepository(getActivity().getApplication());
                                                                List<BuiltInProfile> builtInProfiles = builtInFitnessRepository.getUserProfileNow();
                                                                BuiltInProfile profile = builtInProfiles.get(0);
                                                                profile.setDateOfBirth(birthday);
                                                                builtInFitnessRepository.insert(profile);
                                                            }
                                                        }
                                                    }).start();
                                                }
                                            },
                                            now.get(Calendar.YEAR), // Initial year selection
                                            now.get(Calendar.MONTH), // Initial month selection
                                            now.get(Calendar.DAY_OF_MONTH) // Inital day selection
                                    );
                                    dpd.show(getChildFragmentManager(), "Date picker dialog");
                                }
                            });
                            if (unit.equals("SI")) {
                                String height = String.format(Locale.US, "%.0f", profile.getHeight());
                                final int numFirstNumPicker = (int) (profile.getHeight() / 100d);
                                final int numSecondNumPicker = (int) Math.round(profile.getHeight() % 100d);
                                heightTextView.setText(height + " cm");
                                heightLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        configureDoubleNumPicksLayout("Height", unit, numFirstNumPicker, numSecondNumPicker);
                                    }
                                });
                            } else {
                                final int numFirstNumPicker = (int) (profile.getHeight() / 30.48);
                                final int numSecondNumPicker = (int) Math.round((profile.getHeight() % 30.48) / 2.54);
                                heightTextView.setText(numFirstNumPicker + " ft " + numSecondNumPicker + " in");
                                heightLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        configureDoubleNumPicksLayout("Height", unit, numFirstNumPicker, numSecondNumPicker);
                                    }
                                });
                            }
                            if (unit.equals("SI")) {
                                final int numFirstNumPicker = (int) (profile.getWeight() / 100);
                                final int numSecondNumPicker = (int) Math.round(profile.getWeight() % 100);
                                weightTextView.setText(Math.round(profile.getWeight()) + " kg");
                                weightLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        configureDoubleNumPicksLayout("Weight", unit, numFirstNumPicker, numSecondNumPicker);
                                    }
                                });
                            } else {
                                final int numFirstNumPicker = (int) (profile.getWeight() * 2.20462262185 / 100d);
                                final int numSecondNumPicker = (int) Math.round((profile.getWeight() * 2.20462262185) % 100);
                                weightTextView.setText(Math.round(profile.getWeight() * 2.20462262185) + " lbs");
                                weightLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        configureDoubleNumPicksLayout("Weight", unit, numFirstNumPicker, numSecondNumPicker);
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }
        return mView;
    }

    private void configureDoubleNumPicksLayout(String type, String unit, int firstNum, int secondNum) {

        LinearLayout LL = new LinearLayout(getContext());
        LL.setOrientation(LinearLayout.HORIZONTAL);

        NumberPicker numPicker1 = new NumberPicker(getContext());
        NumberPicker numPicker2 = new NumberPicker(getContext());
        final TextView firstUnit = new TextView(getContext());
        final TextView secondUnit = new TextView(getContext());
        if (unit.equals("SI")) {
            if (type.equals("Height")) {
                numPicker1.setMaxValue(2);
                numPicker1.setMinValue(0);
                numPicker2.setMaxValue(99);
                numPicker2.setMinValue(0);
                firstUnit.setText("m");
                firstUnit.setGravity(Gravity.CENTER);
                secondUnit.setText("cm");
                secondUnit.setGravity(Gravity.CENTER);
                numPicker1.setValue(firstNum);
                numPicker2.setValue(secondNum);
            } else if (type.equals("Weight")) {
                final String[] weights = new String[7];
                for (int i = 0; i < 7; i++) {
                    weights[i] = Integer.toString(i * 100);
                }
                numPicker1.setMaxValue(weights.length - 1);
                numPicker1.setMinValue(0);
                numPicker1.setDisplayedValues(weights);
                numPicker2.setMaxValue(99);
                numPicker2.setMinValue(0);
                firstUnit.setText("");
                firstUnit.setGravity(Gravity.CENTER);
                secondUnit.setText("kg");
                secondUnit.setGravity(Gravity.CENTER);
                numPicker1.setValue(firstNum);
                numPicker2.setValue(secondNum);
            }
        } else {
            if (type.equals("Height")) {
                numPicker1.setMaxValue(7);
                numPicker1.setMinValue(0);
                numPicker2.setMaxValue(11);
                numPicker2.setMinValue(0);
                firstUnit.setText("ft");
                firstUnit.setGravity(Gravity.CENTER);
                secondUnit.setText("in");
                secondUnit.setGravity(Gravity.CENTER);
                numPicker1.setValue(firstNum);
                numPicker2.setValue(secondNum);
            } else if (type.equals("Weight")) {
                final String[] weights = new String[15];
                for (int i = 0; i < 15; i++) {
                    weights[i] = Integer.toString(i * 100);
                }
                numPicker1.setMaxValue(weights.length - 1);
                numPicker1.setMinValue(0);
                numPicker1.setDisplayedValues(weights);
                numPicker2.setMaxValue(99);
                numPicker2.setMinValue(0);
                firstUnit.setText("");
                firstUnit.setGravity(Gravity.CENTER);
                secondUnit.setText("lbs");
                secondUnit.setGravity(Gravity.CENTER);
                numPicker1.setValue(firstNum);
                numPicker2.setValue(secondNum);
            }
        }
        final TextView emptyView = new TextView(getContext());
        final TextView emptyView2 = new TextView(getContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50);
        params.setMargins(24, 24, 16, 16);
        params.gravity = Gravity.CENTER;

        LinearLayout.LayoutParams numPicker1Params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        numPicker1Params.weight = 2;

        LinearLayout.LayoutParams firstUnitParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        firstUnitParams.weight = 1;

        LinearLayout.LayoutParams numPicker2Params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        numPicker2Params.weight = 2;

        LinearLayout.LayoutParams secondUnitParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        secondUnitParams.weight = 1;

        LinearLayout.LayoutParams emptyViewParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        emptyViewParams.weight = 1;

        LinearLayout.LayoutParams emptyView2Params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        emptyView2Params.weight = 1;

        LL.setLayoutParams(params);
        LL.addView(emptyView, emptyViewParams);
        LL.addView(numPicker1, numPicker1Params);
        LL.addView(firstUnit, firstUnitParams);
        LL.addView(numPicker2, numPicker2Params);
        LL.addView(secondUnit, secondUnitParams);
        LL.addView(emptyView2, emptyView2Params);

        if (type.equals("Height")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.profile_height_title))
                    .setView(LL)
                    .setPositiveButton(getString(R.string.survey_okay_prompt), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, numPicker1.getValue() + ", " + numPicker2.getValue());
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (getActivity() != null) {
                                        builtInFitnessRepository = new BuiltInFitnessRepository(getActivity().getApplication());
                                        List<BuiltInProfile> builtInProfiles = builtInFitnessRepository.getUserProfileNow();
                                        BuiltInProfile profile = builtInProfiles.get(0);
                                        if (unit.equals("SI")) {
                                            profile.setHeight(numPicker1.getValue() * 100d + numPicker2.getValue());
                                        } else {
                                            profile.setHeight(numPicker1.getValue() * 12 * 2.54 + (numPicker2.getValue() * 2.54));
                                        }
                                        builtInFitnessRepository.insert(profile);
                                    }
                                }
                            }).start();
                        }
                    });
            builder.create().show();
        } else if (type.equals("Weight")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.profile_weight_title))
                    .setView(LL)
                    .setPositiveButton(getString(R.string.survey_okay_prompt), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, numPicker1.getValue() + ", " + numPicker2.getValue());
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (getActivity() != null) {
                                        builtInFitnessRepository = new BuiltInFitnessRepository(getActivity().getApplication());
                                        List<BuiltInProfile> builtInProfiles = builtInFitnessRepository.getUserProfileNow();
                                        BuiltInProfile profile = builtInProfiles.get(0);
                                        if (unit.equals("SI")) {
                                            profile.setWeight(0d + (numPicker1.getValue() * 100) + numPicker2.getValue());
                                        } else {
                                            profile.setWeight((numPicker1.getValue() * 100 * 0.45359237) + numPicker2.getValue() * 0.45359237);
                                        }
                                        builtInFitnessRepository.insert(profile);
                                    }
                                }
                            }).start();
                        }
                    });
            builder.create().show();
        }
    }
}