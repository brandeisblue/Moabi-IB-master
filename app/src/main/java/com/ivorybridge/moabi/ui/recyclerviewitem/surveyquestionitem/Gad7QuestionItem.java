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

public class Gad7QuestionItem extends AbstractItem<Gad7QuestionItem, Gad7QuestionItem.ViewHolder> {

    private static final String TAG = Gad7QuestionItem.class.getSimpleName();
    private int questionNum;

    public Gad7QuestionItem(int questionNum) {
        this.questionNum = questionNum;
    }

    @Override
    public int getType() {
        return R.id.gad7_question_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_survey_question_item;
    }

    @NonNull
    @Override
    public Gad7QuestionItem.ViewHolder getViewHolder(View v) {
        return new Gad7QuestionItem.ViewHolder(v);
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<Gad7QuestionItem> {

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
        private SharedPreferences gad7SharedPreference;
        private SharedPreferences.Editor gad7SPEditor;
        private SharedPreferences.OnSharedPreferenceChangeListener gad7SPChangeListener;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final Gad7QuestionItem item, List<Object> payloads) {
            gad7SharedPreference = itemView.getContext().getSharedPreferences(itemView.getContext()
                            .getString(R.string.com_ivorybridge_moabi_GAD7_SHARED_PREFERENCE_KEY),
                    Context.MODE_PRIVATE);
            gad7SPEditor = gad7SharedPreference.edit();
            Long selection = gad7SharedPreference.getLong("question_" + item.questionNum + "_choice", 0);
            int questionNum = item.questionNum;
            Log.i(TAG, "Question " + item.questionNum + ": " + selection);
            if (questionNum == 1) {
                questionTextView.setText(itemView.getContext().getString(R.string.gad7_question_1));
            } else if (questionNum == 2) {
                questionTextView.setText(itemView.getContext().getString(R.string.gad7_question_2));
            } else if (questionNum == 3) {
                questionTextView.setText(itemView.getContext().getString(R.string.gad7_question_3));
            } else if (questionNum == 4) {
                questionTextView.setText(itemView.getContext().getString(R.string.gad7_question_4));
            } else if (questionNum == 5) {
                questionTextView.setText(itemView.getContext().getString(R.string.gad7_question_5));
            } else if (questionNum == 6) {
                questionTextView.setText(itemView.getContext().getString(R.string.gad7_question_6));
            } else if (questionNum == 7) {
                questionTextView.setText(itemView.getContext().getString(R.string.gad7_question_7));
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
                            gad7SPEditor.putBoolean("has_made_selection", true);
                            gad7SPEditor.putLong("question_" + item.questionNum + "_choice", 1L);
                            gad7SPEditor.commit();
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
                        gad7SPEditor.putBoolean("has_made_selection", true);
                        gad7SPEditor.putLong("question_" + item.questionNum + "_choice", 1L);
                        gad7SPEditor.commit();
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
                            gad7SPEditor.putLong("question_" + item.questionNum + "_choice", 2L);
                            gad7SPEditor.putBoolean("has_made_selection", true);
                            gad7SPEditor.commit();
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
                        gad7SPEditor.putLong("question_" + item.questionNum + "_choice", 2L);
                        gad7SPEditor.putBoolean("has_made_selection", true);
                        gad7SPEditor.commit();
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
                            gad7SPEditor.putLong("question_" + item.questionNum + "_choice", 3L);
                            gad7SPEditor.putBoolean("has_made_selection", true);
                            gad7SPEditor.commit();
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
                        gad7SPEditor.putLong("question_" + item.questionNum + "_choice", 3L);
                        gad7SPEditor.putBoolean("has_made_selection", true);
                        gad7SPEditor.commit();
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
                            gad7SPEditor.putLong("question_" + item.questionNum + "_choice", 4L);
                            gad7SPEditor.putBoolean("has_made_selection", true);
                            gad7SPEditor.commit();
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
                        gad7SPEditor.putLong("question_" + item.questionNum + "_choice", 4L);
                        gad7SPEditor.putBoolean("has_made_selection", true);
                        gad7SPEditor.commit();
                    }
                }
            });
        }

        @Override
        public void unbindView(Gad7QuestionItem item) {
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
            gad7SPEditor.putLong("question_" + questionNum + "_choice", 0);
            gad7SPEditor.commit();
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
