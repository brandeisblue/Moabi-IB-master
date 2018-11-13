package com.ivorybridge.moabi.ui.recyclerviewitem.surveyquestionitem;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ivorybridge.moabi.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

public class Phq9QuestionItem extends AbstractItem<Phq9QuestionItem, Phq9QuestionItem.ViewHolder> {

    private static final String TAG = Phq9QuestionItem.class.getSimpleName();
    private int questionNum;

    public Phq9QuestionItem(int questionNum) {
        this.questionNum = questionNum;
    }

    @Override
    public int getType() {
        return R.id.phq9_question_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_survey_question_item;
    }

    @NonNull
    @Override
    public Phq9QuestionItem.ViewHolder getViewHolder(View v) {
        return new Phq9QuestionItem.ViewHolder(v);
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<Phq9QuestionItem> {

        @BindView(R.id.rv_item_survey_question_item_question_title_textview)
        TextView questionTextView;
        @BindView(R.id.rv_item_survey_question_item_option1_textview)
        TextView option1TextView;
        @BindView(R.id.rv_item_survey_question_item_option2_textview)
        TextView option2TextView;
        @BindView(R.id.rv_item_survey_question_item_option3_textview)
        TextView option3TextView;
        @BindView(R.id.rv_item_survey_question_item_option4_textview)
        TextView option4TextView;
        private String lastSelection;
        private SharedPreferences phq9SharedPreference;
        private SharedPreferences.Editor phq9SPEditor;
        private SharedPreferences.OnSharedPreferenceChangeListener phq9SPChangeListener;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final Phq9QuestionItem item, List<Object> payloads) {
            phq9SharedPreference = itemView.getContext().getSharedPreferences(itemView.getContext()
                            .getString(R.string.com_ivorybridge_moabi_PHQ9_SHARED_PREFERENCE_KEY),
                    Context.MODE_PRIVATE);
            phq9SPEditor = phq9SharedPreference.edit();
            Long selection = phq9SharedPreference.getLong("question_" + item.questionNum + "_choice", 0);
            int questionNum = item.questionNum;
            Log.i(TAG, "Question " + item.questionNum + ": " + selection);
            if (questionNum == 1) {
                questionTextView.setText(itemView.getContext().getString(R.string.phq9_question_1));
            } else if (questionNum == 2) {
                questionTextView.setText(itemView.getContext().getString(R.string.phq9_question_2));
            } else if (questionNum == 3) {
                questionTextView.setText(itemView.getContext().getString(R.string.phq9_question_3));
            } else if (questionNum == 4) {
                questionTextView.setText(itemView.getContext().getString(R.string.phq9_question_4));
            } else if (questionNum == 5) {
                questionTextView.setText(itemView.getContext().getString(R.string.phq9_question_5));
            } else if (questionNum == 6) {
                questionTextView.setText(itemView.getContext().getString(R.string.phq9_question_6));
            } else if (questionNum == 7) {
                questionTextView.setText(itemView.getContext().getString(R.string.phq9_question_7));
            } else if (questionNum == 8) {
                questionTextView.setText(itemView.getContext().getString(R.string.phq9_question_8));
            } else if (questionNum == 9) {
                questionTextView.setText(itemView.getContext().getString(R.string.phq9_question_9));
            }
            if (selection == 0) {
                resetViews();
            } else if (selection == 1) {
                resetViews();
                option1TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                option1TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else if (selection == 2) {
                resetViews();
                option2TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                option2TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else if (selection == 3) {
                resetViews();
                option3TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                option3TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else if (selection == 4) {
                resetViews();
                option4TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                option4TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            }

            phq9SharedPreference.registerOnSharedPreferenceChangeListener(phq9SPChangeListener);
            option1TextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (lastSelection != null) {
                        if (!lastSelection.equals(option1TextView.getText().toString())) {
                            // selecting this item.
                            deselectAll(item.questionNum);
                            option1TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                            option1TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                            lastSelection = option1TextView.getText().toString();
                            phq9SPEditor.putBoolean("has_made_selection", true);
                            phq9SPEditor.putLong("question_" + item.questionNum + "_choice", 1L);
                            phq9SPEditor.commit();
                        } else {
                            // deselecting this item
                            deselectAll(item.questionNum);
                        }
                    } else {
                        // selecting this item
                        deselectAll(item.questionNum);
                        option1TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                        option1TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                        lastSelection = option1TextView.getText().toString();
                        phq9SPEditor.putBoolean("has_made_selection", true);
                        phq9SPEditor.putLong("question_" + item.questionNum + "_choice", 1L);
                        phq9SPEditor.commit();
                    }
                }
            });
            option2TextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (lastSelection != null) {
                        if (!lastSelection.equals(option2TextView.getText().toString())) {
                            // selecting this item.
                            deselectAll(item.questionNum);
                            option2TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                            option2TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                            lastSelection = option2TextView.getText().toString();
                            phq9SPEditor.putLong("question_" + item.questionNum + "_choice", 2L);
                            phq9SPEditor.putBoolean("has_made_selection", true);
                            phq9SPEditor.commit();
                        } else {
                            // deselecting this item
                            deselectAll(item.questionNum);
                        }
                    } else {
                        // selecting this item
                        deselectAll(item.questionNum);
                        option2TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                        option2TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                        lastSelection = option2TextView.getText().toString();
                        phq9SPEditor.putLong("question_" + item.questionNum + "_choice", 2L);
                        phq9SPEditor.putBoolean("has_made_selection", true);
                        phq9SPEditor.commit();
                    }
                }
            });
            option3TextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (lastSelection != null) {
                        if (!lastSelection.equals(option3TextView.getText().toString())) {
                            // selecting this item.
                            deselectAll(item.questionNum);
                            option3TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                            option3TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                            lastSelection = option3TextView.getText().toString();
                            phq9SPEditor.putLong("question_" + item.questionNum + "_choice", 3L);
                            phq9SPEditor.putBoolean("has_made_selection", true);
                            phq9SPEditor.commit();
                        } else {
                            // deselecting this item
                            deselectAll(item.questionNum);
                        }
                    } else {
                        // selecting this item
                        deselectAll(item.questionNum);
                        option3TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                        option3TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                        lastSelection = option3TextView.getText().toString();
                        phq9SPEditor.putLong("question_" + item.questionNum + "_choice", 3L);
                        phq9SPEditor.putBoolean("has_made_selection", true);
                        phq9SPEditor.commit();
                    }
                }
            });
            option4TextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (lastSelection != null) {
                        if (!lastSelection.equals(option4TextView.getText().toString())) {
                            // selecting this item.
                            deselectAll(item.questionNum);
                            option4TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                            option4TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                            lastSelection = option4TextView.getText().toString();
                            phq9SPEditor.putLong("question_" + item.questionNum + "_choice", 4L);
                            phq9SPEditor.putBoolean("has_made_selection", true);
                            phq9SPEditor.commit();
                        } else {
                            // deselecting this item
                            deselectAll(item.questionNum);
                        }
                    } else {
                        // selecting this item
                        deselectAll(item.questionNum);
                        option4TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                        option4TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                        lastSelection = option4TextView.getText().toString();
                        phq9SPEditor.putLong("question_" + item.questionNum + "_choice", 4L);
                        phq9SPEditor.putBoolean("has_made_selection", true);
                        phq9SPEditor.commit();
                    }
                }
            });
        }

        @Override
        public void unbindView(Phq9QuestionItem item) {
        }

        private void resetViews() {
            option1TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            option1TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            option2TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            option2TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            option3TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            option3TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            option4TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            option4TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
        }

        private void deselectAll(int questionNum) {
            lastSelection = null;
            phq9SPEditor.putLong("question_" + questionNum + "_choice", 0);
            phq9SPEditor.commit();
            option1TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            option1TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            option2TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            option2TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            option3TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            option3TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            option4TextView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            option4TextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
        }
    }
}
