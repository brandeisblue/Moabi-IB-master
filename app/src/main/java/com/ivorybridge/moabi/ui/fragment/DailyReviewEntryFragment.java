package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.dailyreview.DailyReview;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.ivorybridge.moabi.viewmodel.DailyReviewViewModel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DailyReviewEntryFragment extends Fragment {

    private static final String TAG = DailyReviewEntryFragment.class.getSimpleName();
    @BindView(R.id.fragment_daily_review_entry_terrible_linearlayout)
    LinearLayout terribleLayout;
    @BindView(R.id.fragment_daily_review_entry_bad_linearlayout)
    LinearLayout badLayout;
    @BindView(R.id.fragment_daily_review_entry_okay_linearlayout)
    LinearLayout okayLayout;
    @BindView(R.id.fragment_daily_review_entry_good_linearlayout)
    LinearLayout goodLayout;
    @BindView(R.id.fragment_daily_review_entry_excellent_linearlayout)
    LinearLayout excellentLayout;
    @BindView(R.id.fragment_daily_review_entry_terrible_textview)
    TextView terribleTextView;
    @BindView(R.id.fragment_daily_review_entry_bad_textview)
    TextView badTextView;
    @BindView(R.id.fragment_daily_review_entry_okay_textview)
    TextView okayTextView;
    @BindView(R.id.fragment_daily_review_entry_good_textview)
    TextView goodTextView;
    @BindView(R.id.fragment_daily_review_entry_excellent_textview)
    TextView excellentTextView;
    @BindView(R.id.fragment_daily_review_entry_submitbutton)
    Button submitButton;
    private float previousProgress;
    private FirebaseManager firebaseManager;
    private FormattedTime formattedTime;
    private Set<String> selectionSet;
    private View lastView;
    private String lastSelection;
    private SharedPreferences overallEntrySharedPreferences;
    private SharedPreferences.Editor overallEntrySPEditor;
    private List<String> userInputsInUseList;
    private DataInUseViewModel dataInUseViewModel;
    private DailyReviewViewModel dailyReviewViewModel;
    private DailyReview dailyReview;
    private Long score;

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
        dailyReview = new DailyReview();
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
        View mView = inflater.inflate(R.layout.fragment_daily_review_entry, container, false);
        ButterKnife.bind(this, mView);
        overallEntrySharedPreferences = getContext().getSharedPreferences(
                getString(R.string.com_ivorybridge_moabi_DAILY_REVIEW_SHARED_PREFERENCE_KEY)
                , Context.MODE_PRIVATE);
        overallEntrySPEditor = overallEntrySharedPreferences.edit();
        if (getActivity() != null) {
            dailyReviewViewModel = ViewModelProviders.of(getActivity()).get(DailyReviewViewModel.class);
        }
        selectionSet = new LinkedHashSet<>();
        //overallLevelTextView.setVisibility(View.INVISIBLE);
        terribleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastSelection != null) {
                    if (!lastSelection.equals(terribleTextView.getText().toString())) {
                        deselectAll();
                        terribleLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        terribleTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                        score = 1L;
                        lastSelection = terribleTextView.getText().toString();
                    } else {
                        deselectAll();
                        score = null;
                        lastSelection = null;
                    }
                } else {
                    deselectAll();
                    terribleLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    terribleTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    lastSelection = terribleTextView.getText().toString();
                    score = 1L;
                }
            }
        });
        badLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastSelection != null) {
                    if (!lastSelection.equals(badTextView.getText().toString())) {
                        deselectAll();
                        badLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        badTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                        lastSelection = badTextView.getText().toString();
                        score = 2L;
                    } else {
                        deselectAll();
                        lastSelection = null;
                        score = null;
                    }
                } else {
                    deselectAll();
                    badLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    badTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    lastSelection = badTextView.getText().toString();
                    score = 2L;
                }
            }
        });
        okayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastSelection != null) {
                    if (!lastSelection.equals(okayTextView.getText().toString())) {
                        deselectAll();
                        okayLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        okayTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                        lastSelection = okayTextView.getText().toString();
                        score = 3L;
                    } else {
                        deselectAll();
                        lastSelection = null;
                        score = null;
                    }
                } else {
                    deselectAll();
                    okayLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    okayTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    lastSelection = okayTextView.getText().toString();
                    score = 3L;
                }
            }
        });
        goodLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastSelection != null) {
                    if (!lastSelection.equals(goodTextView.getText().toString())) {
                        deselectAll();
                        goodLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        goodTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                        lastSelection = goodTextView.getText().toString();
                        score = 4L;
                    } else {
                        deselectAll();
                        lastSelection = null;
                        score = null;
                    }
                } else {
                    deselectAll();
                    goodLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    goodTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    lastSelection = goodTextView.getText().toString();
                    score = 4L;
                }
            }
        });
        excellentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastSelection != null) {
                    if (!lastSelection.equals(excellentTextView.getText().toString())) {
                        deselectAll();
                        excellentLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        excellentTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                        lastSelection = excellentTextView.getText().toString();
                        score = 5L;
                    } else {
                        deselectAll();
                        lastSelection = null;
                        score = null;
                    }
                } else {
                    deselectAll();
                    excellentLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    excellentTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    lastSelection = excellentTextView.getText().toString();
                    score = 5L;
                }
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
                            if (score != null) {
                                dailyReview.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                dailyReview.setDailyReview(score);
                                dailyReview.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                    firebaseManager.getDaysWithDataTodayRef().child(getString(R.string.daily_review_camel_case)).setValue(true);
                                    firebaseManager.getDailyReviewRef().child(formattedTime.getCurrentDateAsYYYYMMDD()).child(formattedTime.getCurrentTimeAsHHMM())
                                            .setValue(score);
                                    firebaseManager.getDailyReviewLast30DaysRef().child(formattedTime.getCurrentDateAsYYYYMMDD())
                                            .child(formattedTime.getCurrentTimeAsHHMM()).setValue(score);
                                }
                                dailyReviewViewModel.insert(dailyReview, formattedTime.getCurrentDateAsYYYYMMDD());
                                deselectAll();
                                dailyReviewViewModel.getEntries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), 365),
                                        formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(getViewLifecycleOwner(), new Observer<List<DailyReview>>() {
                                    @Override
                                    public void onChanged(@Nullable List<DailyReview> dailyReviews) {
                                        if (dailyReviews != null) {
                                            if (dailyReviews.size() > 0) {
                                                dailyReviewViewModel.processDailyReview((dailyReviews));
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
    

    private void deselectAll() {
        lastSelection = null;
        score = null;
        terribleLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        terribleTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        badLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        badTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        okayLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        okayTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        goodLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        goodTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        excellentLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        excellentTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
    }
}
