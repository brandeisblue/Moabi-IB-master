package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;
import com.ivorybridge.moabi.database.entity.util.InputDate;
import com.ivorybridge.moabi.database.entity.util.InputHistory;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;
import com.ivorybridge.moabi.repository.AsyncCallsMasterRepository;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.ui.activity.SettingsActivity;
import com.ivorybridge.moabi.ui.recyclerviewitem.EmptyItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.activitytracker.ActivityTrackerItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.appusage.AppUsageItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.baactivity.BAActivityItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.survey.DailyReviewItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.survey.EnergyItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.survey.Gad7Item;
import com.ivorybridge.moabi.ui.recyclerviewitem.survey.MoodItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.survey.Phq9Item;
import com.ivorybridge.moabi.ui.recyclerviewitem.survey.StressItem;
import com.ivorybridge.moabi.ui.recyclerviewitem.timedactivity.TimedActivityItem;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.AsyncTaskBooleanViewModel;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.ivorybridge.moabi.viewmodel.InputHistoryViewModel;
import com.ivorybridge.moabi.viewmodel.WeatherViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import pub.devrel.easypermissions.EasyPermissions;

public class TodayFragment extends Fragment implements
        EasyPermissions.PermissionCallbacks,
        DatePickerDialog.OnDateSetListener {

    private String TAG = TodayFragment.class.getSimpleName();
    private Context mContext;
    @BindView(R.id.fragment_main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.fragment_main_vertical_recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_main_appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.fragment_main_sync_progressbar)
    MaterialProgressBar progressBar;
    @BindView(R.id.fragment_main_weather_layout)
    LinearLayout weatherLayout;
    @BindView(R.id.fragment_main_weather_imageview)
    ImageView weatherImageView;
    @BindView(R.id.fragment_main_weather_textview)
    TextView weatherTextView;
    private Unbinder unbinder;
    private String mDate;
    private AsyncTaskBooleanViewModel asyncTaskBooleanViewModel;
    private AsyncCallsMasterRepository asyncCallsMasterRepository;
    private InputHistoryViewModel inputHistoryViewModel;
    private DataInUseViewModel dataInUseViewModel;

    // adapters
    private FastAdapter<IItem> recyclerAdapter;
    private ItemAdapter<ActivityTrackerItem> activityTrackerItemItemAdapter;
    private ItemAdapter<AppUsageItem> appUsageItemItemAdapter;
    private ItemAdapter<MoodItem> moodItemAdapter;
    private ItemAdapter<EnergyItem> energyItemAdapter;
    private ItemAdapter<StressItem> stressItemAdapter;
    private ItemAdapter<DailyReviewItem> dailyReviewItemAdapter;
    private ItemAdapter<Phq9Item> phq9ItemAdapter;
    private ItemAdapter<Gad7Item> gad7ItemAdapter;
    private ItemAdapter<TimedActivityItem> timedActivityItemAdapter;
    private ItemAdapter<EmptyItem> mEmptyAdapter;
    private ItemAdapter<BAActivityItem> bAActivityItemAdapter;
    private SharedPreferences unitSharedPreferences;
    private FormattedTime formattedTime;
    private WeatherViewModel weatherViewModel;

    public TodayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach");
        mContext = context;
        mDate = setUpDatesForToday();
        Bundle bundle = this.getArguments();
        if (bundle != null && bundle.getString("date") != null) {
            mDate = bundle.getString("date");
            Log.i(TAG, mDate);
        }
        formattedTime = new FormattedTime();
        asyncTaskBooleanViewModel =
                ViewModelProviders.of(this).get(AsyncTaskBooleanViewModel.class);
        inputHistoryViewModel = ViewModelProviders.of(this).get(InputHistoryViewModel.class);
        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);
        weatherViewModel = ViewModelProviders.of(this).get(WeatherViewModel.class);
        if (getActivity() != null) {
            asyncCallsMasterRepository = new AsyncCallsMasterRepository((AppCompatActivity) getActivity(), mDate);
            if (isOnline()) {
                Log.i(TAG, "Making network calls");
                //asyncCallsMasterRepository.makeCallsToConnectedServices();
            } else {
                Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mDate = setUpDatesForToday();
        Bundle bundle = this.getArguments();
        if (bundle != null && bundle.getString("date") != null) {
            mDate = bundle.getString("date");
        }
        if (getContext() != null) {
            unitSharedPreferences = getContext().getSharedPreferences(getString(R.string.com_ivorybridge_moabi_UNIT_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
            String unit = unitSharedPreferences.getString(getString(R.string.com_ivorybridge_mobai_UNIT_KEY), getContext().getString(R.string.preference_unit_si_title));
            setItemsToRecyclerView(unit);
            configureWeather(unit);
        }
        Log.i(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        recyclerView.setAdapter(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        Log.i(TAG, "onCreateView");
        return view;
    }

    private void configureWeather(String unit) {
        if (mDate.equals(formattedTime.getCurrentDateAsYYYYMMDD())) {
            weatherViewModel.getAll(formattedTime.getStartOfDay(mDate), formattedTime.getEndOfDay(mDate))
                    .observe(this, new Observer<List<WeatherDailySummary>>() {
                        @Override
                        public void onChanged(List<WeatherDailySummary> weatherDailySummaries) {
                            if (weatherDailySummaries != null && weatherDailySummaries.size() > 0) {
                                WeatherDailySummary dailySummary = weatherDailySummaries.get(0);
                                String weatherConditionImageUrl = dailySummary.getImageUrl();
                                double avgTempC = dailySummary.getAvgTempC();
                                double avgTempF = dailySummary.getAvgTempF();
                                DecimalFormat decimalFormat = new DecimalFormat("#");
                                if (unit.equals(getString(R.string.preference_unit_si_title))) {
                                    configureWeatherUI(unit, weatherConditionImageUrl, decimalFormat.format(avgTempC));
                                } else {
                                    configureWeatherUI(unit, weatherConditionImageUrl, decimalFormat.format(avgTempF));
                                }
                            } else {
                                weatherImageView.setVisibility(View.GONE);
                                weatherTextView.setText(getString(R.string.chart_no_entry));
                            }
                        }
                    });
        } else {
            weatherViewModel.getAll(formattedTime.getStartOfDay(mDate), formattedTime.getEndOfDay(mDate))
                    .observe(getViewLifecycleOwner(), new Observer<List<WeatherDailySummary>>() {
                        @Override
                        public void onChanged(List<WeatherDailySummary> weatherDailySummaries) {
                            if (weatherDailySummaries != null && weatherDailySummaries.size() > 0) {
                                WeatherDailySummary dailySummary = weatherDailySummaries.get(0);
                                String weatherConditionImageUrl = dailySummary.getImageUrl();
                                double minTempC = dailySummary.getMinTempC();
                                double maxTempC = dailySummary.getMaxTempC();
                                double minTempF = dailySummary.getMinTempF();
                                double maxTempF = dailySummary.getMaxTempF();
                                double avgTempC = dailySummary.getAvgTempC();
                                double avgTempF = dailySummary.getAvgTempF();
                                if (unit.equals(getString(R.string.preference_unit_si_title))) {
                                    configureWeatherHistoryUI(unit, weatherConditionImageUrl, avgTempC, minTempC, maxTempC);
                                } else {
                                    configureWeatherHistoryUI(unit, weatherConditionImageUrl, avgTempF, minTempF, maxTempF);
                                }
                            } else {
                                weatherImageView.setVisibility(View.GONE);
                                weatherTextView.setText(getString(R.string.chart_no_entry));
                            }
                        }
                    });
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.fragment_main_menuitems, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.fragment_main_menuitems_refresh:
                if (getActivity() != null) {
                    if (isOnline()) {
                        Log.i(TAG, "Making network calls");
                        asyncCallsMasterRepository.makeCallsToConnectedServices();
                    }
                } else {
                    Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.fragment_main_menuitems_menu:
                if (getActivity() != null) {
                    View popUpView = getView().findViewById(R.id.fragment_main_menuitems_menu);
                    PopupMenu popupMenu = new PopupMenu(getActivity(), popUpView);
                    popupMenu.getMenuInflater().inflate(R.menu.fragment_main_menuitems_popupmenu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu_settings:
                                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                                    getActivity().startActivity(intent);
                                    break;
                                case R.id.menu_calendar:
                                    inputHistoryViewModel.getAllInputDates().observe(getActivity(), new Observer<List<InputDate>>() {
                                        @Override
                                        public void onChanged(@Nullable List<InputDate> inputDates) {
                                            if (inputDates != null) {
                                                Handler handler = new Handler();
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                                                        // 1. Convert list of strings to an array of calendars.
                                                        // 2. Set the days to the date picker.
                                                        // 3. Set up listener for the date picker.
                                                        Calendar[] selectableDays = new Calendar[inputDates.size()];
                                                        for (int i = 0; i < inputDates.size(); i++) {
                                                            Calendar c = Calendar.getInstance();
                                                            try {
                                                                Date d = sdf.parse(inputDates.get(i).getDate());
                                                                c.setTime(d);
                                                                selectableDays[i] = c;
                                                            } catch (ParseException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        Calendar now = Calendar.getInstance();
                                                        DatePickerDialog dpd = DatePickerDialog.newInstance(
                                                                TodayFragment.this,
                                                                now.get(Calendar.YEAR),
                                                                now.get(Calendar.MONTH),
                                                                now.get(Calendar.DAY_OF_MONTH)
                                                        );
                                                        dpd.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                                                            @Override
                                                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                                                String d = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                                                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                                                                SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                                                                Date convertedDate = new Date();
                                                                try {
                                                                    convertedDate = sf.parse(d);
                                                                } catch (ParseException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                String date = sf2.format(convertedDate);
                                                                getActivity().finish();
                                                                Intent restartActivity = new Intent(getActivity(), MainActivity.class);
                                                                restartActivity.putExtra("date", date);
                                                                startActivity(restartActivity);
                                                            }
                                                        });
                                                        dpd.setSelectableDays(selectableDays);
                                                        dpd.setHighlightedDays(selectableDays);
                                                        //dpd.show(getActivity().getFragmentManager(), "Hello");
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (getFragmentManager() != null) {
                                                                    dpd.show(getFragmentManager(), "Calendar");
                                                                }
                                                            }
                                                        });
                                                    }
                                                }).start();


                                            }
                                        }
                                    });
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated");
        setToolbar();
        asyncTaskBooleanViewModel.getAll().observe(this, new Observer<List<AsyncTaskBoolean>>() {
            @Override
            public void onChanged(@Nullable List<AsyncTaskBoolean> asyncTaskBooleans) {
                if (!isOnline()) {
                    progressBar.setVisibility(View.GONE);
                }
                if (asyncTaskBooleans == null) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    Boolean isLoading = false;
                    for (AsyncTaskBoolean asyncTask : asyncTaskBooleans) {
                        if (!asyncTask.getResult()) {
                            isLoading = true;
                        }
                        //Log.i(TAG, asyncTask.getTaskName() + ": " + asyncTask.getResult());
                    }
                    if (isLoading) {
                        if (progressBar.getVisibility() != View.VISIBLE) {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });
        recyclerAdapter = new FastAdapter<>();
        activityTrackerItemItemAdapter = new ItemAdapter<>();
        appUsageItemItemAdapter = new ItemAdapter<>();
        moodItemAdapter = new ItemAdapter<>();
        energyItemAdapter = new ItemAdapter<>();
        stressItemAdapter = new ItemAdapter<>();
        dailyReviewItemAdapter = new ItemAdapter<>();
        phq9ItemAdapter = new ItemAdapter<>();
        gad7ItemAdapter = new ItemAdapter<>();
        mEmptyAdapter = new ItemAdapter<>();
        bAActivityItemAdapter = new ItemAdapter<>();
        timedActivityItemAdapter = new ItemAdapter<>();
        recyclerAdapter.withSelectable(true);
        recyclerAdapter = FastAdapter.with(Arrays.asList(activityTrackerItemItemAdapter,
                mEmptyAdapter, appUsageItemItemAdapter, timedActivityItemAdapter, dailyReviewItemAdapter,
                moodItemAdapter, energyItemAdapter, stressItemAdapter, phq9ItemAdapter, gad7ItemAdapter,
                bAActivityItemAdapter));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext,
                RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (getActivity() != null) {
                    if (dy < 0) {
                        ((MainActivity) getActivity()).setNavigationVisibility(false);
                    } else if (dy > 0) {
                        ((MainActivity) getActivity()).setNavigationVisibility(true);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        //inputHistoryViewModel.getInputHistory(mDate).removeObservers(this);
        //inputHistoryViewModel.getAllInputDates().removeObservers(this);
        Log.i(TAG, "onViewCreated");
    }

    private void setToolbar() {
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mDate = bundle.getString("date");
        }
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat sf2 = new SimpleDateFormat("MMM d, EEEE", Locale.US);
        Date convertedDate = new Date();
        try {
            convertedDate = sf.parse(mDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = sf2.format(convertedDate);
        toolbar.setTitle(formattedDate);
    }

    private void setItemsToRecyclerView(String unit) {
        dataInUseViewModel.getAllInputsInUse().observe(this, new Observer<List<InputInUse>>() {
            @Override
            public void onChanged(@Nullable List<InputInUse> inputInUseList) {
                activityTrackerItemItemAdapter.clear();
                appUsageItemItemAdapter.clear();
                moodItemAdapter.clear();
                energyItemAdapter.clear();
                stressItemAdapter.clear();
                phq9ItemAdapter.clear();
                gad7ItemAdapter.clear();
                dailyReviewItemAdapter.clear();
                bAActivityItemAdapter.clear();
                timedActivityItemAdapter.clear();
                mEmptyAdapter.clear();
                if (inputInUseList != null) {
                    Log.i(TAG, inputInUseList.toString());
                    Set<String> inputTypes = new LinkedHashSet<>();
                    List<String> inputMethodsWithDataList = new ArrayList<>();
                    for (InputInUse inputInUse : inputInUseList) {
                        if (inputInUse.isInUse()) {
                            if (inputInUse.getName().equals(getString(R.string.fitbit_camel_case))) {
                                inputMethodsWithDataList.add(getString(R.string.fitbit_camel_case));
                            } else if (inputInUse.getName().equals(getString(R.string.googlefit_camel_case))) {
                                inputMethodsWithDataList.add(getString(R.string.googlefit_camel_case));
                            } else if (inputInUse.getName().equals(getString(R.string.moabi_tracker_camel_case))) {
                                inputMethodsWithDataList.add(getString(R.string.moabi_tracker_camel_case));
                            } else {
                                inputTypes.add(inputInUse.getName());
                            }
                        }
                    }
                    if (inputMethodsWithDataList.size() > 0) {
                        if (getContext() != null && getActivity() != null) {
                            activityTrackerItemItemAdapter.clear();
                            activityTrackerItemItemAdapter.add(new ActivityTrackerItem(
                                    getContext(), (AppCompatActivity) getActivity(), TodayFragment.this,
                                    inputMethodsWithDataList, mDate, unit));
                        }
                    }
                    if (inputTypes.contains(getString(R.string.phone_usage_camel_case))) {
                        if (getContext() != null) {
                            appUsageItemItemAdapter.clear();
                            appUsageItemItemAdapter.add(new AppUsageItem(TodayFragment.this, mDate));
                        }
                    }
                    if (inputTypes.contains(getString(R.string.mood_and_energy_camel_case))) {
                        moodItemAdapter.clear();
                        moodItemAdapter.add(new MoodItem(
                                TodayFragment.this, mDate));
                        energyItemAdapter.clear();
                        energyItemAdapter.add(new EnergyItem(
                                TodayFragment.this, mDate));
                    }
                    if (inputTypes.contains(getString(R.string.stress_camel_case))) {
                        stressItemAdapter.clear();
                        stressItemAdapter.add(new StressItem(
                                TodayFragment.this, mDate));
                    }
                    if (inputTypes.contains(getString(R.string.daily_review_camel_case))) {
                        dailyReviewItemAdapter.clear();
                        dailyReviewItemAdapter.add(new DailyReviewItem(
                                TodayFragment.this, mDate));
                    }
                    if (inputTypes.contains(getString(R.string.depression_phq9_camel_case))) {
                        phq9ItemAdapter.clear();
                        phq9ItemAdapter.add(new Phq9Item(
                                TodayFragment.this, mDate));
                    }
                    if (inputTypes.contains(getString(R.string.anxiety_gad7_camel_case))) {
                        gad7ItemAdapter.clear();
                        gad7ItemAdapter.add(new Gad7Item(
                                TodayFragment.this, mDate));
                    }
                    if (inputTypes.contains(getString(R.string.baactivity_camel_case))) {
                        bAActivityItemAdapter.clear();
                        bAActivityItemAdapter.add(new BAActivityItem(TodayFragment.this, mDate));
                    }
                    if (inputTypes.contains(getString(R.string.timer_camel_case))) {
                        timedActivityItemAdapter.clear();
                        timedActivityItemAdapter.add(new TimedActivityItem(TodayFragment.this, mDate));
                    }
                    if (inputTypes.contains(getString(R.string.weather_camel_case))) {
                        weatherLayout.setVisibility(View.VISIBLE);
                    }
                    Log.i(TAG, inputTypes.toString());
                }
                if (inputInUseList == null || inputInUseList.size() == 0) {
                    mEmptyAdapter.clear();
                    mEmptyAdapter.add(new EmptyItem());
                }
            }
        });
        inputHistoryViewModel.getInputHistory(mDate).observe(this, new Observer<List<InputHistory>>() {
            @Override
            public void onChanged(@Nullable List<InputHistory> inputHistories) {
                if (inputHistories != null) {
                    Set<String> inputTypes = new LinkedHashSet<>();
                    for (InputHistory inputHistory : inputHistories) {
                        inputTypes.add(inputHistory.getInputType());
                    }
                    if (inputTypes.contains(getString(R.string.timer_camel_case))) {
                        timedActivityItemAdapter.clear();
                        timedActivityItemAdapter.add(new TimedActivityItem(TodayFragment.this, mDate));
                    }
                    Log.i(TAG, inputTypes.toString());
                }
                if ((inputHistories == null || inputHistories.size() == 0) && activityTrackerItemItemAdapter.getAdapterItemCount() == 0 &&
                        appUsageItemItemAdapter.getAdapterItemCount() == 0 &&
                        moodItemAdapter.getAdapterItemCount() == 0 &&
                        bAActivityItemAdapter.getAdapterItemCount() == 0 &&
                        timedActivityItemAdapter.getAdapterItemCount() == 0
                        ) {
                    mEmptyAdapter.clear();
                    mEmptyAdapter.add(new EmptyItem());
                }
            }
        });
    }

    private void configureWeatherUI(String unit, String imageUrl, String tempNow) {
        if (imageUrl != null) {
            weatherImageView.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_clear)
                    .error(R.drawable.ic_error_outline_black)
                    .into(weatherImageView);
        } else {
            weatherImageView.setImageResource(R.drawable.ic_not_found_outline_black);
        }

        if (tempNow != null) {
            weatherTextView.setVisibility(View.VISIBLE);
            if (unit.equals(getString(R.string.preference_unit_si_title))) {
                weatherTextView.setText(tempNow + "°C");
            } else {
                weatherTextView.setText(tempNow + "°F");
            }
        } else {
            weatherTextView.setVisibility(View.GONE);
        }
    }

    private void configureWeatherHistoryUI(String unit, String imageUrl, double tempAvg, double tempMin, double tempMax) {
        if (imageUrl != null) {
            weatherImageView.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_clear)
                    .error(R.drawable.ic_error_outline_black)
                    .into(weatherImageView);
        } else {
            weatherImageView.setImageResource(R.drawable.ic_not_found_outline_black);
        }
        if (unit.equals(getString(R.string.preference_unit_si_title))) {
            weatherTextView.setText(Math.round(tempMax) + "°C" + " / " + Math.round(tempMin) + "°C");
        } else {
            weatherTextView.setText(Math.round(tempMax) + "°F" + " / " + Math.round(tempMin) + "°F");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //requestLocationUpdates();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String d = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date convertedDate = new Date();
        try {
            convertedDate = sf.parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String date = sf2.format(convertedDate);
        if (getActivity() != null) {
            getActivity().finish();
        }
        Intent restartActivity = new Intent(getActivity(), MainActivity.class);
        restartActivity.putExtra("date", date);
        startActivity(restartActivity);
        /*

        //Toast.makeText(getContext(), date, Toast.LENGTH_LONG).show();*/
    }

    private String setUpDatesForToday() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return mdformat.format(calendar.getTime());
    }

    public boolean isOnline() {
        if (getActivity() != null) {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                return netInfo != null && netInfo.isConnected();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
