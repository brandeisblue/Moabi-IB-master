package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityFavorited;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.BAActivityRepository;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.ui.recyclerviewitem.baactivity.BAActivityEntryFragmentGridItem;
import com.ivorybridge.moabi.ui.util.GridSpacingItemDecoration;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.BAActivityViewModel;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BAActivityEntryFragment extends Fragment {

    private static final String TAG = BAActivityEntryFragment.class.getSimpleName();
    private Context mContext;

    private FirebaseManager firebaseManager;
    @BindView(R.id.fragment_baactivity_entry_submitbutton)
    Button submitButton;
    @BindView(R.id.fragment_baactivity_entry_recyclerview)
    RecyclerView recyclerView;
    private FastItemAdapter<BAActivityEntryFragmentGridItem> mFastItemAdapter;
    private List<BAActivityEntryFragmentGridItem> entryItems;
    private Long currentTime;
    private BAActivityRepository baActivityRepository;
    private BAActivityViewModel activityViewModel;
    private FormattedTime formattedTime;
    private List<String> userInputsInUseList;
    private DataInUseViewModel dataInUseViewModel;

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
        mContext = context;
        firebaseManager = new FirebaseManager();
        formattedTime = new FormattedTime();

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
        View mView = inflater.inflate(R.layout.fragment_baactivity_entry, container, false);
        ButterKnife.bind(this, mView);
        final FragmentManager fragmentManager = getFragmentManager();
        final List<Fragment> fragments = new ArrayList<>();
        if (fragmentManager != null) {
            fragments.addAll(fragmentManager.getFragments());
        }
        userInputsInUseList = new ArrayList<>();
        Log.i(TAG, fragments.toString());
        entryItems = new ArrayList<>();
        mFastItemAdapter = new FastItemAdapter<>();
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zdt = now.atZone(ZoneId.systemDefault());
        currentTime = zdt.toInstant().toEpochMilli();

        int spanCount = 4; // 5 columns
        int spacing = 20; // 20px
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        //recyclerView.setItemAnimator(new AlphaInAnimator());
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));

        recyclerView.setAdapter(mFastItemAdapter);
        activityViewModel = ViewModelProviders.of(this).get(BAActivityViewModel.class);
        activityViewModel.getAllFavoritedActivities().observe(this, new Observer<List<BAActivityFavorited>>() {
            @Override
            public void onChanged(@Nullable List<BAActivityFavorited> baActivitiesInUse) {
                mFastItemAdapter.clear();
                if (baActivitiesInUse != null && baActivitiesInUse.size() > 0) {
                    Collections.sort(baActivitiesInUse);
                    if (getActivity() != null) {
                        for (BAActivityFavorited activityInUse : baActivitiesInUse) {
                            BAActivityEntryFragmentGridItem entryItem = new BAActivityEntryFragmentGridItem(activityInUse);
                            Log.i(TAG, activityInUse.getName() + ": " + activityInUse.getActivtyType());
                            entryItems.add(entryItem);
                            mFastItemAdapter.add(entryItem);
                        }
                        BAActivityFavorited addItem = new BAActivityFavorited(getString(R.string.edit_title), 6L, 0L, "ic_add_circle_black_24dp");
                        mFastItemAdapter.add(new BAActivityEntryFragmentGridItem(addItem));
                    }
                } else {
                    BAActivityFavorited addItem = new BAActivityFavorited(getString(R.string.edit_title), 6L, 0L, "ic_add_circle_black_24dp");
                    mFastItemAdapter.add(new BAActivityEntryFragmentGridItem(addItem));
                }
            }
        });

        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);
        dataInUseViewModel.getAllInputsInUse().observe(this, new Observer<List<InputInUse>>() {
            @Override
            public void onChanged(List<InputInUse> inputInUses) {
                if (inputInUses != null) {
                    Log.i(TAG, inputInUses.toString());
                    for (InputInUse inUse: inputInUses) {
                        if (inUse.getName().equals(getString(R.string.mood_and_energy_camel_case))
                                || inUse.getName().equals(getString(R.string.baactivity_camel_case))
                                || inUse.getName().equals(getString(R.string.stress_camel_case))
                                || inUse.getName().equals(getString(R.string.pain_camel_case))
                                || inUse.getName().equals(getString(R.string.daily_review_camel_case))
                                || inUse.getName().equals(getString(R.string.depression_phq9_camel_case))
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
                            //TODO - if getActivities for today (start of day till now) isn't null, update daysWithData
                            for (BAActivityEntryFragmentGridItem item : entryItems) {
                                BAActivityFavorited favoritedActivity = item.getActivityFavorited();
                                //Log.i(TAG, item.getName() + ": " + item.isEntered());
                                BAActivityEntry entry = new BAActivityEntry();
                                entry.setActivityType(favoritedActivity.getActivtyType());
                                entry.setName(favoritedActivity.getName());
                                entry.setTimeOfEntry(currentTime);
                                entry.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                if (item.isEntered()) {
                                    activityViewModel.addActivityEntry(entry, formattedTime.getCurrentDateAsYYYYMMDD());
                                    //activityRepository.insertFavoritedActivity(favoritedActivity);
                                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                        firebaseManager.getActivityTodayRef().child(getCurrentTimeInString()).child(favoritedActivity.getName()).child("numEntry").setValue(1L);
                                        firebaseManager.getActivityTodayRef().child(getCurrentTimeInString()).child(favoritedActivity.getName()).child("type").setValue(favoritedActivity.getActivtyType());
                                        firebaseManager.getActivityLast30DaysTodayRef().child(getCurrentTimeInString()).child(favoritedActivity.getName()).child("numEntry").setValue(1L);
                                        firebaseManager.getActivityLast30DaysTodayRef().child(getCurrentTimeInString()).child(favoritedActivity.getName()).child("type").setValue(favoritedActivity.getActivtyType());
                                        firebaseManager.getDaysWithDataTodayRef().child(getString(R.string.baactivity_camel_case)).setValue(true);
                                    }
                                }
                            }
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

        /*
        submitButton.setOnResultEndListener(new SubmitButton.OnResultEndListener() {
            @Override
            public void onResultEnd() {
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.activity_make_entry_viewpager);
                int currentItem = viewPager.getCurrentItem();
                if (fragments.size() == 1 || currentItem == fragments.size() - 1) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                } else {
                    viewPager.setCurrentItem(currentItem + 1, true);
                }
                submitButton.reset();
            }
        });*/

        return mView;
    }


    private String getCurrentTimeInString() {
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return df.format(Calendar.getInstance().getTime());
    }

    private Long getStartOfDay(String date) {

        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .atStartOfDay(ZoneOffset.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    private Long getEndOfDay(String date) {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .atStartOfDay(ZoneOffset.systemDefault())
                .plusDays(1L)
                .minusNanos(1L)
                .toInstant()
                .toEpochMilli();
    }

    private Long getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zdt = now.atZone(ZoneId.systemDefault());
        return currentTime = zdt.toInstant().toEpochMilli();
    }

    private String setUpDatesForToday() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return mdformat.format(calendar.getTime());
    }
}