package com.ivorybridge.moabi.ui.recyclerviewitem.baactivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.util.wordcloud.WordCloud;
import com.ivorybridge.moabi.util.wordcloud.WordCloudClick;
import com.ivorybridge.moabi.util.wordcloud.WordCloudEntry;
import com.ivorybridge.moabi.viewmodel.BAActivityViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.android.segmented.SegmentedGroup;


public class BAActivityItem extends AbstractItem<BAActivityItem, BAActivityItem.ViewHolder> {

    private static final String TAG = BAActivityItem.class.getSimpleName();
    private String mDate;
    private Fragment fragment;

    public BAActivityItem(Fragment fragment, String date) {
        this.fragment = fragment;
        mDate = date;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_baactivity;
    }

    @Override
    public int getType() {
        return R.id.baactivity_item;
    }

    @NonNull
    @Override
    public BAActivityItem.ViewHolder getViewHolder(View v) {
        return new BAActivityItem.ViewHolder(v);
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<BAActivityItem> {

        @BindView(R.id.rv_item_baactivity_radiogroup)
        SegmentedGroup radioGroup;
        @BindView(R.id.rv_item_baactivity_today_button)
        RadioButton todayButton;
        @BindView(R.id.rv_item_baactivity_this_week_button)
        RadioButton thisWeekButton;
        @BindView(R.id.rv_item_baactivity_this_month_button)
        RadioButton thisMonthButton;
        @BindView(R.id.rv_item_baactivity_wordcloud)
        WordCloud wordCloud;
        private BAActivityViewModel baActivityViewModel;
        private SharedPreferences baActivitySharedPreferences;
        private SharedPreferences.Editor baActivitySPEditor;
        private FormattedTime formattedTime;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(BAActivityItem item, List<Object> payloads) {
            final Map<String, Integer> activitiesMap = new LinkedHashMap<>();
            baActivitySharedPreferences = itemView.getContext().getSharedPreferences(
                    itemView.getContext().getString(R.string.com_ivorybridge_moabi_BA_ACTIVITY_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
            baActivitySPEditor = baActivitySharedPreferences.edit();
            formattedTime = new FormattedTime();

            radioGroup.setTintColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary), Color.WHITE);
            radioGroup.setUnCheckedTintColor(Color.WHITE, Color.BLACK);
            if (item.fragment != null) {
                baActivityViewModel = ViewModelProviders.of(item.fragment).get(BAActivityViewModel.class);
            }
            String checked = baActivitySharedPreferences.getString(itemView.getContext()
                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                    itemView.getContext().getString(R.string.today));
            if (checked.equals(itemView.getContext().getString(R.string.today))) {
                setWordCloud(item, 0);
                todayButton.setChecked(true);
            } else if (checked.equals(itemView.getContext().getString(R.string.this_week))) {
                setWordCloud(item, 7);
                thisWeekButton.setChecked(true);
            } else if (checked.equals(itemView.getContext().getString(R.string.this_month))) {
                setWordCloud(item, 28);
                thisMonthButton.setChecked(true);
            }

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // This will get the radiobutton that has changed in its check state
                    RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
                    // This puts the value (true/false) into the variable
                    boolean isChecked = checkedRadioButton.isChecked();
                    // If the radiobutton that has changed in check state is now checked...
                    if (isChecked) {
                        if (checkedId == R.id.rv_item_baactivity_today_button) {
                            baActivitySPEditor.putString(itemView.getContext()
                                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                                    itemView.getContext().getString(R.string.today));
                            baActivitySPEditor.commit();
                            setWordCloud(item, 0);
                        } else if (checkedId == R.id.rv_item_baactivity_this_week_button) {
                            baActivitySPEditor.putString(itemView.getContext()
                                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                                    itemView.getContext().getString(R.string.this_week));
                            baActivitySPEditor.commit();
                            setWordCloud(item, 7);
                        } else {
                            baActivitySPEditor.putString(itemView.getContext()
                                            .getString(R.string.com_ivorybridge_mobai_TIME_RANGE_KEY),
                                    itemView.getContext().getString(R.string.this_month));
                            baActivitySPEditor.commit();
                            setWordCloud(item, 31);
                        }
                    }
                }
            });
        }

        private void setWordCloud(BAActivityItem item, int numOfDay) {
            Long start = formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(item.mDate, numOfDay);
            Long end = formattedTime.getEndOfDay(item.mDate);
            Log.i(TAG, formattedTime.convertLongToMDHHMMaa(start) + " - " + formattedTime.convertLongToMDHHMMaa(end));
            baActivityViewModel.getActivityEntries(start, end).observe(item.fragment,
                    new Observer<List<BAActivityEntry>>() {
                @Override
                public void onChanged(@Nullable List<BAActivityEntry> baActivityEntries) {
                    if (baActivityEntries != null) {
                        final Map<String, Long> nameFrequencyMap = new LinkedHashMap<>();
                        final Map<String, Long> nameTypeMap = new LinkedHashMap<>();
                        final Map<String, Set<String>> nameDatesMap = new LinkedHashMap<>();
                        final List<WordCloudEntry> wordCloudEntryList = new ArrayList<>();
                        Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (BAActivityEntry baActivityEntry : baActivityEntries) {
                                    if (nameFrequencyMap.get(baActivityEntry.getName()) != null) {
                                        Long old = nameFrequencyMap.get(baActivityEntry.getName());
                                        nameFrequencyMap.put(baActivityEntry.getName(), old + 1);
                                    } else {
                                        nameFrequencyMap.put(baActivityEntry.getName(), 1L);
                                    }
                                    if (nameDatesMap.get(baActivityEntry.getName()) != null) {
                                        Set<String> old = nameDatesMap.get(baActivityEntry.getName());
                                        old.add(formattedTime.convertLongToYYYYMMDD(baActivityEntry.getDateInLong()));
                                        nameDatesMap.put(baActivityEntry.getName(), old);
                                    } else {
                                        Set<String> dates = new LinkedHashSet<>();
                                        dates.add(formattedTime.convertLongToYYYYMMDD(baActivityEntry.getDateInLong()));
                                        nameDatesMap.put(baActivityEntry.getName(), dates);
                                    }
                                    nameTypeMap.put(baActivityEntry.getName(), baActivityEntry.getActivityType());
                                }

                                for (Map.Entry<String, Long> nameFrequency : nameFrequencyMap.entrySet()) {
                                    long type = nameTypeMap.get(nameFrequency.getKey());
                                    WordCloudEntry worldCloudEntry = new WordCloudEntry(nameFrequency.getKey(),
                                            nameFrequency.getValue(),
                                            type);
                                    wordCloudEntryList.add(worldCloudEntry);
                                }
                                Collections.shuffle(wordCloudEntryList);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        displayWordCloud(wordCloudEntryList, nameDatesMap);
                                        Log.i(TAG, baActivityEntries.toString());
                                    }
                                });
                            }
                        }).start();
                    }
                }
            });
        }

        private void displayWordCloud(List<WordCloudEntry> wordCloudEntryList, Map<String, Set<String>> nameDatesMap) {
            if (wordCloudEntryList.size() != 0) {
                wordCloud.setMinTextSize(40f);
                wordCloud.setTextSize(40f);
                wordCloud.create(wordCloudEntryList);
                wordCloud.setOnWordClickListener(new WordCloudClick() {
                    @Override
                    public void onWordClick(View widget, int index) {
                        Log.i(TAG, "Index is " + index);
                        Toast.makeText(itemView.getContext(), wordCloudEntryList.get(index).getEntryName()
                                + "\n" + nameDatesMap.get(wordCloudEntryList.get(index).getEntryName().toString()), Toast.LENGTH_SHORT).show();
                    }
                });
                //wordCloud.setRandomFonts();
            } else {
                wordCloud.setText(itemView.getContext().getString(R.string.chart_no_entry));
                wordCloud.setTextSize(12f);
                wordCloud.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimaryDarkComplt));
            }
        }

        @Override
        public void unbindView(BAActivityItem item) {

        }
    }
}
