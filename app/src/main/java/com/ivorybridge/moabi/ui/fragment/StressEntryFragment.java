package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.stress.Stress;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.ui.views.SeekArc;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.ivorybridge.moabi.viewmodel.StressViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StressEntryFragment extends Fragment {

    private static final String TAG = StressEntryFragment.class.getSimpleName();
    @BindView(R.id.fragment_stress_entry_seekarc)
    SeekArc seekArc;
    @BindView(R.id.fragment_stress_entry_thumb_imageview)
    ImageView thumbImageView;
    @BindView(R.id.fragment_stress_entry_stresslevel_textview)
    TextView stressLevelTextView;
    @BindView(R.id.fragment_stress_entry_submitbutton)
    Button submitButton;
    private Float lastProgress;
    private FirebaseManager firebaseManager;
    private FormattedTime formattedTime;
    private List<String> userInputsInUseList;
    private DataInUseViewModel dataInUseViewModel;
    private StressViewModel stressViewModel;
    private Stress stress;


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
        formattedTime = new FormattedTime();
        firebaseManager = new FirebaseManager();
        stress = new Stress();
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
        View mView = inflater.inflate(R.layout.fragment_stress_entry, container, false);
        ButterKnife.bind(this, mView);
        //stressLevelTextView.setVisibility(View.INVISIBLE);
        thumbImageView.setVisibility(View.GONE);
        stressLevelTextView.setText("" + 0);
        if (getActivity() != null) {
            stressViewModel = ViewModelProviders.of(getActivity()).get(StressViewModel.class);
        }
        seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                //Log.i(TAG, "Previous progress is " + previousProgress + ", Current Progress is " + progress);
                //float angle = (progress - previousProgress) / 100f * 180;
                //Log.i(TAG, "Rotate by " + angle);
                //previousProgress = progress;
                //thumbImageView.animate().rotationBy(angle);
                lastProgress = (float) progress / 10;
                stressLevelTextView.setText("" + lastProgress);
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });
        userInputsInUseList = new ArrayList<>();
        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);
        dataInUseViewModel.getAllInputsInUse().observe(this, new Observer<List<InputInUse>>() {
            @Override
            public void onChanged(List<InputInUse> inputInUses) {
                if (inputInUses != null) {
                    Log.i(TAG, inputInUses.toString());
                    for (InputInUse inUse: inputInUses) {
                        if (inUse.getName().equals(getString(R.string.mood_and_energy_camel_case)) || inUse.getName().equals(getString(R.string.baactivity_camel_case))
                                || inUse.getName().equals(getString(R.string.stress_camel_case)) || inUse.getName().equals(getString(R.string.pain_camel_case)) ||
                                inUse.getName().equals(getString(R.string.daily_review_camel_case)) || inUse.getName().equals(getString(R.string.depression_phq9_camel_case))
                                || inUse.getName().equals(getString(R.string.anxiety_gad7_camel_case))) {
                            Boolean isChecked = (Boolean) inUse.isInUse();
                            if (isChecked != null) {
                                if (isChecked) {
                                    userInputsInUseList.add(inUse.getName());
                                }
                            }
                        }
                    }
                    Log.i(TAG, userInputsInUseList.toString());
                    submitButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (lastProgress != null) {
                                stress.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                stress.setStress(lastProgress.doubleValue());
                                stress.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                    firebaseManager.getDaysWithDataTodayRef().child(getString(R.string.stress_camel_case)).setValue(true);
                                    firebaseManager.getStressRef().child(formattedTime.getCurrentDateAsYYYYMMDD()).child(formattedTime.getCurrentTimeAsHHMM())
                                            .setValue(lastProgress.doubleValue());
                                    firebaseManager.getStressLast30DaysRef().child(formattedTime.getCurrentDateAsYYYYMMDD())
                                            .child(formattedTime.getCurrentTimeAsHHMM()).setValue(lastProgress.doubleValue());
                                }
                                stressViewModel.insert(stress, formattedTime.getCurrentDateAsYYYYMMDD());
                                lastProgress = null;
                                stressViewModel.getEntries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), 365),
                                        formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(getViewLifecycleOwner(), new Observer<List<Stress>>() {
                                    @Override
                                    public void onChanged(@Nullable List<Stress> stresses) {
                                        if (stresses != null) {
                                            if (stresses.size() > 0) {
                                                stressViewModel.processStress((stresses));
                                            }
                                        }
                                    }
                                });
                            }
                            //TODO - if getActivities for today (start of day till now) isn't null, update daysWithData
                            ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.activity_make_entry_viewpager);
                            int currentItem = viewPager.getCurrentItem();
                            Log.i(TAG, "# of fragments: " + userInputsInUseList.size() + ", " + "current position: " + currentItem);
                            if (userInputsInUseList.size() == 1 || currentItem == userInputsInUseList.size() - 1) {
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                viewPager.setCurrentItem(currentItem + 1, true);
                            }
                        }
                    });
                }
            }
        });
        return mView;
    }
}

