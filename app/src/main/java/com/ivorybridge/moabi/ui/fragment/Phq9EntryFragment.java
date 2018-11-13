package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.depression.Phq9;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.ui.recyclerviewitem.surveyquestionitem.Phq9QuestionItem;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.ivorybridge.moabi.viewmodel.DepressionViewModel;
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

public class Phq9EntryFragment extends Fragment {

    private static final String TAG = Phq9EntryFragment.class.getSimpleName();
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
    private ItemAdapter<Phq9QuestionItem> phq9QuestionItemItemAdapter;
    private SharedPreferences phq9SharedPreference;
    private SharedPreferences.Editor phq9SPEditor;
    private SharedPreferences.OnSharedPreferenceChangeListener phq9SPChangeListener;
    private LinearLayoutManager layoutManager;
    private DepressionViewModel depressionViewModel;


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
        phq9QuestionItemItemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(Arrays.asList(phq9QuestionItemItemAdapter));
        fastAdapter.withSelectable(true);
        phq9SharedPreference = getContext().getSharedPreferences(getContext()
                        .getString(R.string.com_ivorybridge_moabi_PHQ9_SHARED_PREFERENCE_KEY),
                Context.MODE_PRIVATE);
        phq9SPEditor = phq9SharedPreference.edit();
        depressionViewModel = ViewModelProviders.of(this).get(DepressionViewModel.class);

        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        SnapHelper snapHelper = new LinearSnapHelper();
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(fastAdapter);
        snapHelper.attachToRecyclerView(recyclerView);

        phq9QuestionItemItemAdapter.add(new Phq9QuestionItem(1));
        phq9QuestionItemItemAdapter.add(new Phq9QuestionItem(2));
        phq9QuestionItemItemAdapter.add(new Phq9QuestionItem(3));
        phq9QuestionItemItemAdapter.add(new Phq9QuestionItem(4));
        phq9QuestionItemItemAdapter.add(new Phq9QuestionItem(5));
        phq9QuestionItemItemAdapter.add(new Phq9QuestionItem(6));
        phq9QuestionItemItemAdapter.add(new Phq9QuestionItem(7));
        phq9QuestionItemItemAdapter.add(new Phq9QuestionItem(8));
        phq9QuestionItemItemAdapter.add(new Phq9QuestionItem(9));

        backButton.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
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
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        phq9SharedPreference.registerOnSharedPreferenceChangeListener(phq9SPChangeListener);
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
                Long suicidality = 0L;
                for (int i = 1; i < 10; i++) {
                    Long choice = phq9SharedPreference.getLong("question_" + i + "_choice", 0);
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
                    if (i == 9) {
                        if (choice == 0) {
                        } else if (choice == 1) {
                            suicidality += 0L;
                        } else if (choice == 2) {
                            suicidality += 1L;
                        } else if (choice == 3) {
                            suicidality += 2L;
                        } else if (choice == 4) {
                            suicidality += 3L;
                        }
                    }
                }
                final Long finalScore = score;
                final Long finalSuicidality = suicidality;
                DataInUseViewModel dataInUseViewModel = ViewModelProviders.of(getActivity()).get(DataInUseViewModel.class);
                dataInUseViewModel.getAllInputsInUse().observe(getActivity(), new Observer<List<InputInUse>>() {
                    @Override
                    public void onChanged(List<InputInUse> inputInUses) {
                        if (inputInUses != null) {
                            List<String> userInputsInUseList = new ArrayList<>();
                            Log.i(TAG, inputInUses.toString());
                            for (InputInUse inUse : inputInUses) {
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
                                Spanned phoneNumber = Html.fromHtml(getString(R.string.survey_phq9_suicidality_hotline_nationality_us_number));
                                SpannableString s = new SpannableString(phoneNumber);
                                Linkify.addLinks(s, Linkify.ALL);
                                if (finalScore >= 0 && finalScore < 5) {
                                    if (finalSuicidality > 0) {
                                        Spanned text = Html.fromHtml(getString(R.string.survey_phq9_your_depression_level) + " " + getString(R.string.survey_phq9_level_min)
                                                + "<br>" + getString(R.string.survey_phq9_minimal_suggestion) + "<br>" + "<br>" +
                                                getString(R.string.survey_phq9_suicidality_cheer) + "<br>" +
                                                getString(R.string.survey_phq9_suicidality_hotline_nationality_us) + "<br>" + "<br>"
                                                + getString(R.string.survey_phq9_suicidality_hotline_nationality_us_number));
                                        SpannableString str = new SpannableString(text);
                                        Linkify.addLinks(str, Linkify.PHONE_NUMBERS);
                                        builder.setMessage(str);
                                    } else {
                                        builder.setMessage(getString(R.string.survey_phq9_your_depression_level) + " " + getString(R.string.survey_phq9_level_min)
                                        + "\n" + getString(R.string.survey_phq9_minimal_suggestion));
                                    }
                                } else if (finalScore >= 5 && finalScore < 10) {
                                    if (finalSuicidality > 0) {
                                        Spanned text = Html.fromHtml(getString(R.string.survey_phq9_your_depression_level) + " " + getString(R.string.survey_phq9_level_mild)
                                                + "<br>" + getString(R.string.survey_phq9_mild_suggestion) + "<br>" + "<br>" +
                                                getString(R.string.survey_phq9_suicidality_cheer) + "<br>" +
                                                getString(R.string.survey_phq9_suicidality_hotline_nationality_us) + "<br>" + "<br>"
                                                + getString(R.string.survey_phq9_suicidality_hotline_nationality_us_number));
                                        SpannableString str = new SpannableString(text);
                                        Linkify.addLinks(str, Linkify.PHONE_NUMBERS);
                                        builder.setMessage(str);
                                    } else {
                                        builder.setMessage(getString(R.string.survey_phq9_your_depression_level) + " " + getString(R.string.survey_phq9_level_mild)
                                                + "\n" + getString(R.string.survey_phq9_mild_suggestion));
                                    }
                                } else if (finalScore >= 10 && finalScore < 15) {
                                    if (finalSuicidality > 0) {
                                        Spanned text = Html.fromHtml(getString(R.string.survey_phq9_your_depression_level) + " " + getString(R.string.survey_phq9_level_moderate)
                                                + "<br>" + getString(R.string.survey_phq9_moderate_suggestion) + "<br>" + "<br>" +
                                                getString(R.string.survey_phq9_suicidality_cheer) + "<br>" +
                                                getString(R.string.survey_phq9_suicidality_hotline_nationality_us) + "<br>" + "<br>"
                                                + getString(R.string.survey_phq9_suicidality_hotline_nationality_us_number));
                                        SpannableString str = new SpannableString(text);
                                        Linkify.addLinks(str, Linkify.PHONE_NUMBERS);
                                        builder.setMessage(str);
                                    } else {
                                        builder.setMessage(getString(R.string.survey_phq9_your_depression_level) + " " + getString(R.string.survey_phq9_level_moderate)
                                                + "\n" + getString(R.string.survey_phq9_moderate_suggestion));
                                    }
                                } else if (finalScore >= 15 && finalScore < 20) {
                                    if (finalSuicidality > 0) {
                                        Spanned text = Html.fromHtml(getString(R.string.survey_phq9_your_depression_level) + " " + getString(R.string.survey_phq9_level_moderately_severe)
                                                + "<br>" + getString(R.string.survey_phq9_moderately_severe_suggestion) + "<br>" + "<br>" +
                                                getString(R.string.survey_phq9_suicidality_cheer) + "<br>" +
                                                getString(R.string.survey_phq9_suicidality_hotline_nationality_us) + "<br>" + "<br>"
                                                + getString(R.string.survey_phq9_suicidality_hotline_nationality_us_number));
                                        SpannableString str = new SpannableString(text);
                                        Linkify.addLinks(str, Linkify.PHONE_NUMBERS);
                                        builder.setMessage(str);
                                    } else {
                                        builder.setMessage(getString(R.string.survey_phq9_your_depression_level) + " " + getString(R.string.survey_phq9_level_moderately_severe)
                                                + "\n" + getString(R.string.survey_phq9_moderately_severe_suggestion));
                                    }
                                } else {
                                    if (finalSuicidality > 0) {
                                        Spanned text = Html.fromHtml(getString(R.string.survey_phq9_your_depression_level) + " " + getString(R.string.survey_phq9_level_severe)
                                                + "<br>" + getString(R.string.survey_phq9_severe_suggestion) + "<br>" + "<br>" +
                                                getString(R.string.survey_phq9_suicidality_cheer) + "<br>" +
                                                getString(R.string.survey_phq9_suicidality_hotline_nationality_us) + "<br>" + "<br>"
                                                + getString(R.string.survey_phq9_suicidality_hotline_nationality_us_number));
                                        SpannableString str = new SpannableString(text);
                                        Linkify.addLinks(str, Linkify.PHONE_NUMBERS);
                                        builder.setMessage(str);
                                    } else {
                                        builder.setMessage(getString(R.string.survey_phq9_your_depression_level) + " " + getString(R.string.survey_phq9_level_severe)
                                                + "\n" + getString(R.string.survey_phq9_severe_suggestion));
                                    }
                                }
                                builder.setPositiveButton(getString(R.string.survey_okay_prompt), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Phq9 phq9 = new Phq9();
                                        phq9.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                        phq9.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                        phq9.setScore(finalScore);
                                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                            firebaseManager.getPhq9DayWithDataRef().child(formattedTime.getCurrentDateAsYYYYMMDD()).setValue(true);
                                            firebaseManager.getPhq9Ref().child(formattedTime.getCurrentDateAsYYYYMMDD()).child(formattedTime.getCurrentTimeAsHHMM())
                                                    .setValue(finalScore);
                                            firebaseManager.getPhq9Last30DaysRef().child(formattedTime.getCurrentDateAsYYYYMMDD()).child(formattedTime.getCurrentTimeAsHHMM())
                                                    .setValue(finalScore);
                                        }
                                        depressionViewModel.insert(phq9, formattedTime.getCurrentDateAsYYYYMMDD());
                                        depressionViewModel.getEntries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), 365),
                                                formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(getViewLifecycleOwner(), new Observer<List<Phq9>>() {
                                            @Override
                                            public void onChanged(@Nullable List<Phq9> entries) {
                                                if (entries != null) {
                                                    if (entries.size() > 0) {
                                                        depressionViewModel.processPhq9(entries);
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
                                ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

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
        Log.i(TAG, "RecyclerView position: " + layoutManager.findFirstVisibleItemPosition());
        nextButton.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        resetSelections();
    }

    private void resetSelections() {
        phq9SPEditor.putLong("question_1_choice", 0);
        phq9SPEditor.putLong("question_2_choice", 0);
        phq9SPEditor.putLong("question_3_choice", 0);
        phq9SPEditor.putLong("question_4_choice", 0);
        phq9SPEditor.putLong("question_5_choice", 0);
        phq9SPEditor.putLong("question_6_choice", 0);
        phq9SPEditor.putLong("question_7_choice", 0);
        phq9SPEditor.putLong("question_8_choice", 0);
        phq9SPEditor.putLong("question_9_choice", 0);
        phq9SPEditor.commit();
    }
}