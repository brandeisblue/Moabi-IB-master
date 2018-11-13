package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.anxiety.DailyGad7;
import com.ivorybridge.moabi.database.entity.anxiety.Gad7;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.ui.recyclerviewitem.surveyquestionitem.Gad7QuestionItem;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.AnxietyViewModel;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class Gad7EntryFragment extends Fragment {

    private static final String TAG = Gad7EntryFragment.class.getSimpleName();
    @BindView(R.id.fragment_survey_entry_question_prompt_textview)
    TextView questionPromptTextView;
    @BindView(R.id.fragment_survey_entry_question_recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_survey_entry_submitbutton)
    Button submitButton;
    @BindView(R.id.fragment_survey_entry_next_button)
    Button nextButton;
    @BindView(R.id.fragment_survey_entry_back_button)
    Button backButton;
    private FirebaseManager firebaseManager;
    private FormattedTime formattedTime;
    private FastAdapter fastAdapter;
    private ItemAdapter<Gad7QuestionItem> Gad7QuestionItemItemAdapter;
    private SharedPreferences gad7SharedPreference;
    private SharedPreferences.Editor gad7SPEditor;
    private SharedPreferences.OnSharedPreferenceChangeListener gad7SPChangeListener;
    private LinearLayoutManager layoutManager;
    private AnxietyViewModel anxietyViewModel;


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
        View mView = inflater.inflate(R.layout.fragment_survey_entry, container, false);
        ButterKnife.bind(this, mView);
        // populate fast adapter with item adapters
        Gad7QuestionItemItemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(Arrays.asList(Gad7QuestionItemItemAdapter));
        fastAdapter.withSelectable(true);
        gad7SharedPreference = getContext().getSharedPreferences(getContext()
                        .getString(R.string.com_ivorybridge_moabi_GAD7_SHARED_PREFERENCE_KEY),
                Context.MODE_PRIVATE);
        gad7SPEditor = gad7SharedPreference.edit();
        anxietyViewModel = ViewModelProviders.of(this).get(AnxietyViewModel.class);

        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        SnapHelper snapHelper = new LinearSnapHelper();
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(fastAdapter);
        snapHelper.attachToRecyclerView(recyclerView);

        Gad7QuestionItemItemAdapter.add(new Gad7QuestionItem(1));
        Gad7QuestionItemItemAdapter.add(new Gad7QuestionItem(2));
        Gad7QuestionItemItemAdapter.add(new Gad7QuestionItem(3));
        Gad7QuestionItemItemAdapter.add(new Gad7QuestionItem(4));
        Gad7QuestionItemItemAdapter.add(new Gad7QuestionItem(5));
        Gad7QuestionItemItemAdapter.add(new Gad7QuestionItem(6));
        Gad7QuestionItemItemAdapter.add(new Gad7QuestionItem(7));

        anxietyViewModel.getEntries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), 365),
                formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(getViewLifecycleOwner(), new Observer<List<Gad7>>() {
            @Override
            public void onChanged(@Nullable List<Gad7> entries) {
                if (entries != null) {
                    if (entries.size() > 0) {
                        Log.i(TAG, entries.toString());
                    }
                }
            }
        });

        anxietyViewModel.getDailyGad7s(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), 365),
                formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(getViewLifecycleOwner(), new Observer<List<DailyGad7>>() {
            @Override
            public void onChanged(@Nullable List<DailyGad7> entries) {
                if (entries != null) {
                    if (entries.size() > 0) {
                        Log.i(TAG, entries.toString());
                    }
                }
            }
        });

        enableBackButton();
        //disableNextButton();
        enableNextButton();
        setSubmitButton();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    nextButton.setVisibility(View.GONE);
                    backButton.setVisibility(View.GONE);
                    submitButton.setVisibility(View.GONE);
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.i(TAG, "RecyclerView position: " + layoutManager.findFirstVisibleItemPosition());
                    int position = layoutManager.findFirstVisibleItemPosition();
                    if (layoutManager.getItemCount() > 1) {
                        if (position == layoutManager.getItemCount() - 1) {
                            nextButton.setVisibility(View.GONE);
                            backButton.setVisibility(View.VISIBLE);
                            submitButton.setVisibility(View.VISIBLE);
                        } else if (position == 0) {
                            nextButton.setVisibility(View.VISIBLE);
                            backButton.setVisibility(View.GONE);
                            submitButton.setVisibility(View.GONE);
                        } else {
                            nextButton.setVisibility(View.VISIBLE);
                            backButton.setVisibility(View.VISIBLE);
                            submitButton.setVisibility(View.GONE);
                        }
                    }
                    //setUpSettingButton(position, settingButton, activitySelectionViewModel);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        gad7SharedPreference.registerOnSharedPreferenceChangeListener(gad7SPChangeListener);

        return mView;
    }

    private void disableNextButton() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Please choose an option", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableNextButton() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "@ " + layoutManager.findLastVisibleItemPosition() + "/" + layoutManager.getItemCount());
                //nextButton.setVisibility(View.INVISIBLE);
                recyclerView.smoothScrollToPosition(layoutManager.findLastVisibleItemPosition() + 1);
            }
        });
    }

    private void enableBackButton() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "@ " + layoutManager.findFirstVisibleItemPosition() + "/" + layoutManager.getItemCount());
                if (layoutManager.findFirstVisibleItemPosition() > 0) {
                    recyclerView.smoothScrollToPosition(layoutManager.findFirstVisibleItemPosition() - 1);
                } else {
                    recyclerView.smoothScrollToPosition(0);
                    //backButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void setSubmitButton() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<Integer> missingChoiceSet = new LinkedHashSet<>();
                Long score = 0L;
                for (int i = 1; i < 8; i++) {
                    Long choice = gad7SharedPreference.getLong("question_" + i + "_choice", 0);
                    if (choice == 0) {
                        missingChoiceSet.add(i);
                    } else if (choice == 1) {
                        score += 0L;
                    } else if (choice == 2) {
                        score += 1L;
                    } else if (choice == 3) {
                        score += 2L;
                    } else if (choice == 4) {
                        score += 3L;
                    }
                }
                final Long finalScore = score;
                DataInUseViewModel dataInUseViewModel = ViewModelProviders.of(getActivity()).get(DataInUseViewModel.class);
                dataInUseViewModel.getAllInputsInUse().observe(getViewLifecycleOwner(), new Observer<List<InputInUse>>() {
                    @Override
                    public void onChanged(List<InputInUse> inputInUses) {
                        if (inputInUses != null) {
                            List<String> userInputsInUseList = new ArrayList<>();
                            Log.i(TAG, inputInUses.toString());
                            for (InputInUse inUse : inputInUses) {
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
                            if (missingChoiceSet.size() > 0) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                String missingChoice = "";
                                missingChoice = Arrays.toString(missingChoiceSet.toArray());
                                missingChoice = missingChoice.replaceAll("[\\\\[\\\\](){}]", "");
                                if (missingChoiceSet.size() > 1) {
                                    missingChoice = getString(R.string.survey_incomplete_questions) + " " + missingChoice;
                                } else {
                                    missingChoice = getString(R.string.survey_incomplete_questions) + " " + missingChoice;
                                }
                                builder.setTitle(getString(R.string.survey_incomplete_survey_prompt))
                                        .setMessage(getString(R.string.survey_missing_questions) + " " + missingChoice)
                                        .setPositiveButton(getString(R.string.survey_ignore_prompt), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
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
                                        })
                                        .setNegativeButton(getString(R.string.survey_okay_prompt), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle(getString(R.string.survey_result_prompt));
                                if (finalScore >= 0 && finalScore < 5) {
                                    builder.setMessage(getString(R.string.survey_gad7_your_anxiety_level) + " " + getString(R.string.survey_gad7_level_min)
                                            + "\n" + getString(R.string.survey_gad7_minimal_suggestion));
                                } else if (finalScore >= 5 && finalScore < 10) {

                                    builder.setMessage(getString(R.string.survey_gad7_your_anxiety_level) + " " + getString(R.string.survey_gad7_level_mild)
                                            + "\n" + getString(R.string.survey_gad7_mild_suggestion));
                                } else if (finalScore >= 10 && finalScore < 15) {

                                    builder.setMessage(getString(R.string.survey_gad7_your_anxiety_level) + " " + getString(R.string.survey_gad7_level_moderate)
                                            + "\n" + getString(R.string.survey_gad7_moderate_suggestion));
                                } else if (finalScore >= 15) {
                                    builder.setMessage(getString(R.string.survey_gad7_your_anxiety_level) + " " + getString(R.string.survey_gad7_level_severe)
                                            + "\n" + getString(R.string.survey_gad7_severe_suggestion));
                                }
                                builder.setPositiveButton(getString(R.string.survey_okay_prompt), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Gad7 gad7 = new Gad7();
                                        gad7.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                        gad7.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                        gad7.setScore(finalScore);
                                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                            firebaseManager.getGad7DayWithDataRef().child(formattedTime.getCurrentDateAsYYYYMMDD()).setValue(true);
                                            firebaseManager.getGad7Ref().child(formattedTime.getCurrentDateAsYYYYMMDD()).child(formattedTime.getCurrentTimeAsHHMM())
                                                    .setValue(finalScore);
                                            firebaseManager.getGad7Last30DaysRef().child(formattedTime.getCurrentDateAsYYYYMMDD()).child(formattedTime.getCurrentTimeAsHHMM())
                                                    .setValue(finalScore);
                                        }
                                        anxietyViewModel.insert(gad7, formattedTime.getCurrentDateAsYYYYMMDD());
                                        anxietyViewModel.getEntries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), 365),
                                                formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(getViewLifecycleOwner(), new Observer<List<Gad7>>() {
                                            @Override
                                            public void onChanged(@Nullable List<Gad7> entries) {
                                                if (entries != null) {
                                                    if (entries.size() > 0) {
                                                        Log.i(TAG, entries.toString());
                                                        anxietyViewModel.processGad7(entries);
                                                    }
                                                }
                                            }
                                        });
                                        resetSelections();
                                        recyclerView.smoothScrollToPosition(0);
                                        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.activity_make_entry_viewpager);
                                        int currentItem = viewPager.getCurrentItem();
                                        Log.i(TAG, "# of fragments: " + userInputsInUseList.size() + ", " + "current position: " + currentItem);
                                        if (userInputsInUseList.size() == 1 || currentItem == userInputsInUseList.size() - 1) {
                                            Intent intent = new Intent(getActivity(), MainActivity.class);
                                            startActivity(intent);
                                        } else {
                                            viewPager.setCurrentItem(currentItem + 1, true);
                                        }
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                // Make the dialog's TextView clickable
                                dialog.show();
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerView.smoothScrollToPosition(0);
        nextButton.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        /*
        Log.i(TAG, "RecyclerView position: " + layoutManager.findFirstVisibleItemPosition());
        int position = layoutManager.findFirstVisibleItemPosition();
        if (layoutManager.getItemCount() > 1) {
            if (position == layoutManager.getItemCount() - 1) {
                nextButton.setVisibility(View.GONE);
                backButton.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.VISIBLE);
            } else if (position == 0) {
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
            } else {
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.GONE);
            }
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        resetSelections();
    }

    private void resetSelections() {
        gad7SPEditor.putLong("question_1_choice", 0);
        gad7SPEditor.putLong("question_2_choice", 0);
        gad7SPEditor.putLong("question_3_choice", 0);
        gad7SPEditor.putLong("question_4_choice", 0);
        gad7SPEditor.putLong("question_5_choice", 0);
        gad7SPEditor.putLong("question_6_choice", 0);
        gad7SPEditor.putLong("question_7_choice", 0);
        gad7SPEditor.commit();
    }
}
