package com.ivorybridge.moabi.ui.recyclerviewitem.insight;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.StackingBehavior;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.stats.SimpleRegressionSummary;
import com.ivorybridge.moabi.database.entity.util.UserGoal;
import com.ivorybridge.moabi.repository.UserGoalRepository;
import com.ivorybridge.moabi.service.UserGoalJob;
import com.ivorybridge.moabi.service.UserGoalPeriodicJob;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.UserGoalViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.refactor.library.SmoothCheckBox;

public class InsightRecommendationItem extends AbstractItem<InsightRecommendationItem, InsightRecommendationItem.ViewHolder> {

    private SimpleRegressionSummary simpleRegressionSummary;
    private Fragment fragment;

    public InsightRecommendationItem(SimpleRegressionSummary simpleRegressionSummary, Fragment fragment) {
        this.simpleRegressionSummary = simpleRegressionSummary;
        this.fragment = fragment;
    }

    @Override
    public int getType() {
        return R.id.insight_recommendations_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_insight_recommendations_item;
    }

    @NonNull
    @Override
    public InsightRecommendationItem.ViewHolder getViewHolder(View v) {
        return new InsightRecommendationItem.ViewHolder(v);
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<InsightRecommendationItem> {

        private static final String TAG = InsightRecommendationItem.class.getSimpleName();
        @BindView(R.id.rv_item_insight_recommendations_item_emptyview_textview)
        TextView emptyTextView;
        @BindView(R.id.rv_item_insight_recommendations_item_nonemptyview_relativelayout)
        RelativeLayout nonEmptyLayout;
        @BindView(R.id.rv_item_insight_recommendations_item_depvar_imageview)
        ImageView depVarImageView;
        @BindView(R.id.rv_item_insight_recommendations_item_indepvar_imageview)
        ImageView indepVarImageView;
        @BindView(R.id.rv_item_insight_recommendations_item_depvar_textview)
        TextView depVarTextView;
        @BindView(R.id.rv_item_insight_recommendations_item_improveorworsen_textview)
        TextView depVarDirectionTextView;
        @BindView(R.id.rv_item_insight_recommendations_item_indepvar_textview)
        TextView indepVarTextView;
        @BindView(R.id.rv_item_insight_recommendations_item_moreorless_textview)
        TextView indepVarDirectionTextView;
        @BindView(R.id.rv_item_insight_recommendations_item_numdays_textview)
        TextView numDaysTextView;
        @BindView(R.id.rv_item_insight_recommendations_item_currentgoal_textview)
        TextView currentGoalTextView;
        @BindView(R.id.rv_item_insight_recommendations_item_recommendedgoal_textview)
        TextView recommendedGoalTextView;
        @BindView(R.id.rv_item_insight_recommendations_item_correlation_textview)
        TextView correlationTextView;
        @BindView(R.id.rv_item_insight_recommendations_item_when_subject_textview)
        TextView whenSubjectTextView;
        @BindView(R.id.rv_item_insight_recommendations_item_recommendedgoal_arrow_imageview)
        ImageView recommendedGoalArrowImageView;
        @BindView(R.id.rv_item_insight_recommendations_item_applychange_button)
        SmoothCheckBox checkBox;
        private FormattedTime formattedTime;
        private UserGoalRepository userGoalRepository;
        private UserGoalViewModel userGoalViewModel;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(InsightRecommendationItem item, List<Object> payloads) {

            if (item.simpleRegressionSummary == null) {
                emptyTextView.setVisibility(View.VISIBLE);
                nonEmptyLayout.setVisibility(View.GONE);
                return;
            }

            formattedTime = new FormattedTime();
            userGoalRepository = new UserGoalRepository(item.fragment.getActivity().getApplication());
            SimpleRegressionSummary summary = item.simpleRegressionSummary;
            userGoalViewModel = ViewModelProviders.of(item.fragment).get(UserGoalViewModel.class);
            userGoalViewModel.getGoal(0).observe(item.fragment, new Observer<UserGoal>() {
                @Override
                public void onChanged(UserGoal userGoal) {
                    if (userGoal != null) {
                        Log.i(TAG, userGoal.getGoalName() + ", " + userGoal.getGoalType() + ", " + userGoal.getGoal());
                        Log.i(TAG, summary.getIndepVar() + ", " + summary.getIndepVarType() + ", " + summary.getRecommendedActivityLevel());
                        if (userGoal.getGoalName().equals(summary.getIndepVar()) &&
                                userGoal.getGoalType().equals(summary.getIndepVarType())
                                && userGoal.getGoal().equals(summary.getRecommendedActivityLevel())) {
                            checkBox.setChecked(true);
                        } else {
                            checkBox.setChecked(false);
                        }
                    }
                }
            });
            emptyTextView.setVisibility(View.INVISIBLE);
            nonEmptyLayout.setVisibility(View.VISIBLE);
            Long numOfDays = summary.getNumOfData();
            //iterate through each construct such as mood and energy level
            String service = summary.getIndepVarType();
            String depVar = summary.getDepVar();
            depVarTextView.setText(depVar);
            String independentVar = summary.getIndepVar();
            String depVarType = summary.getDepVarTypeString();
            whenSubjectTextView.setText("when you ");

            if (depVar.equals(itemView.getContext().getString(R.string.mood_camel_case))) {
                depVarImageView.setImageResource(R.drawable.ic_emotion);
                depVarTextView.setText("Your mood");
                depVarDirectionTextView.setText("improves");
            } else if (depVar.equals(itemView.getContext().getString(R.string.energy_camel_case))) {
                depVarImageView.setImageResource(R.drawable.ic_emotion);
                depVarTextView.setText("Your energy");
                depVarDirectionTextView.setText("improves");
            } else if (depVarType.equals(itemView.getContext().getString(R.string.depression_phq9_camel_case))) {
                depVarImageView.setImageResource(R.drawable.ic_depression_rain_black);
                depVarTextView.setText("Depression severity");
                depVarDirectionTextView.setText("decreases");
            } else if (depVarType.equals(itemView.getContext().getString(R.string.anxiety_gad7_camel_case))) {
                depVarTextView.setText("Anxiety severity");
                depVarDirectionTextView.setText("decreases");
                depVarImageView.setImageResource(R.drawable.ic_anxiety_insomnia_black);
            } else if (depVarType.equals(itemView.getContext().getString(R.string.stress_camel_case))) {
                depVarTextView.setText("Stress level");
                depVarDirectionTextView.setText("decreases");
                depVarImageView.setImageResource(R.drawable.ic_stress);
            } else if (depVarType.equals(itemView.getContext().getString(R.string.daily_review_camel_case))) {
                depVarTextView.setText("Your day");
                depVarDirectionTextView.setText("improves");
                depVarImageView.setImageResource(R.drawable.ic_review_black);
            } 

            int drawable = R.drawable.ic_logo_monogram_colored;
            if (service.equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                drawable = R.drawable.ic_fitbit_logo;
            } else if (service.equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                drawable = R.drawable.ic_appusage;
            } else if (service.equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                drawable = R.drawable.ic_googlefit;
            } else if (service.equals(itemView.getContext().getString(R.string.moabi_tracker_camel_case))) {
                drawable = R.drawable.ic_logo_monogram_colored;
            } else if (service.equals(itemView.getContext().getString(R.string.weather_camel_case))) {
                drawable = R.drawable.ic_partly_cloudy;
            } else if (service.equals(itemView.getContext().getString(R.string.timer_camel_case))) {
                drawable = R.drawable.ic_stopwatch;
            } else if (service.equals(itemView.getContext().getString(R.string.baactivity_camel_case))) {
                drawable = R.drawable.ic_physical_activity_black;
            } 
            final int finalDrawable = drawable;
            indepVarImageView.setImageResource(finalDrawable);
            Double correlation = summary.getCorrelation();
            correlationTextView.setText(String.format(Locale.US, "%.0f", Math.abs(correlation) * 100) + " %");
            String recommendedGoal = "";
            String averageActivityLevel = "";
            final Double recommendedGoalDouble = summary.getRecommendedActivityLevel();
            Double averageActivityLevelDouble = summary.getAverageActivityLevel();
            nonEmptyLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(itemView.getContext())
                            .title(summary.getIndepVarType())
                            .content("Do you want to track the recommendation?")
                            .positiveText("Yes")
                            .negativeText("No")
                            .limitIconToDefaultSize()
                            .iconRes(finalDrawable)
                            .stackingBehavior(StackingBehavior.ADAPTIVE)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    UserGoal userGoal = new UserGoal();
                                    userGoal.setGoalName(summary.getIndepVar());
                                    userGoal.setGoalType(summary.getIndepVarType());
                                    userGoal.setPriority(0);
                                    userGoal.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                                    userGoal.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                    userGoal.setGoal(summary.getRecommendedActivityLevel());
                                    userGoalRepository.insert(userGoal);
                                                /*
                                                Intent serviceIntent = new Intent(itemView.getContext(), UserGoalService.class);
                                                itemView.getContext().startService(serviceIntent);*/
                                    UserGoalJob.runJobImmediately();
                                    UserGoalPeriodicJob.schedulePeriodicJob();
                                    dialog.dismiss();
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });
            numDaysTextView.setText(numOfDays + " day average: ");
            if (service.contains(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                recommendedGoal = String.format(Locale.US, "%.0f", recommendedGoalDouble);
                averageActivityLevel = String.format(Locale.US, "%.0f", averageActivityLevelDouble);
                if (independentVar.contains(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                    recommendedGoal = recommendedGoal + " steps";
                    averageActivityLevel = averageActivityLevel + " steps";
                    indepVarTextView.setText("walk");
                } else if (independentVar.contains("sleep")) {
                    recommendedGoal = recommendedGoal + " mins";
                    averageActivityLevel = averageActivityLevel + " mins";
                    indepVarTextView.setText("sleep");
                } else if (independentVar.contains("active")) {
                    recommendedGoal = recommendedGoal + " mins";
                    averageActivityLevel = averageActivityLevel + " mins";
                    indepVarTextView.setText("stay active");
                } else if (independentVar.contains("sedentary")) {
                    recommendedGoal = recommendedGoal + " mins";
                    averageActivityLevel = averageActivityLevel + " mins";
                    indepVarTextView.setText("stay sedentary");
                } else if (independentVar.contains(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                    recommendedGoal = recommendedGoal + " Cal";
                    averageActivityLevel = averageActivityLevel + " Cal";
                    indepVarTextView.setText("burn calories");
                } else if (independentVar.contains(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                    recommendedGoal = String.format(Locale.US, "%.2f", recommendedGoalDouble);
                    averageActivityLevel = String.format(Locale.US, "%.2f", averageActivityLevelDouble);
                    recommendedGoal = recommendedGoal + " km";
                    averageActivityLevel = averageActivityLevel + " km";
                    indepVarTextView.setText("travel");
                } else if (independentVar.contains("floor")) {
                    recommendedGoal = String.format(Locale.US, "%.0f", recommendedGoalDouble);
                    averageActivityLevel = String.format(Locale.US, "%.0f", averageActivityLevelDouble);
                    recommendedGoal = recommendedGoal + " floors";
                    averageActivityLevel = averageActivityLevel + " floors";
                    indepVarTextView.setText("climb");
                }
                recommendedGoalTextView.setText(recommendedGoal);
                currentGoalTextView.setText(averageActivityLevel);

                if (Double.compare(recommendedGoalDouble, averageActivityLevelDouble) < 0) {
                    recommendedGoalArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                    recommendedGoalArrowImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red));
                    indepVarDirectionTextView.setText("less");
                    indepVarDirectionTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red));
                } else if (Double.compare(recommendedGoalDouble, averageActivityLevelDouble) > 0) {
                    recommendedGoalArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                    recommendedGoalArrowImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                    indepVarDirectionTextView.setText("more");
                    indepVarDirectionTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                } else {
                    recommendedGoalArrowImageView.setVisibility(View.INVISIBLE);
                    indepVarDirectionTextView.setText("the exact amount now!\nGreat Job!");
                }
            } else if (service.contains(itemView.getContext().getString(R.string.googlefit_camel_case)) || service.contains(itemView.getContext().getString(R.string.moabi_tracker_camel_case))) {
                recommendedGoal = String.format(Locale.US, "%.0f", recommendedGoalDouble);
                averageActivityLevel = String.format(Locale.US, "%.0f", averageActivityLevelDouble);
                long acitveMinutesInMinutes = 0;
                if (independentVar.contains(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                    recommendedGoal = recommendedGoal + " steps";
                    averageActivityLevel = averageActivityLevel + " steps";
                    indepVarTextView.setText("walk");
                } else if (independentVar.contains("sleep")) {
                    recommendedGoal = recommendedGoal + " mins";
                    averageActivityLevel = averageActivityLevel + " mins";
                    indepVarTextView.setText("sleep");
                } else if (independentVar.contains("active")) {
                    acitveMinutesInMinutes = TimeUnit.MILLISECONDS.toMinutes(recommendedGoalDouble.longValue());
                    recommendedGoal = acitveMinutesInMinutes + " mins";
                    acitveMinutesInMinutes = TimeUnit.MILLISECONDS.toMinutes(averageActivityLevelDouble.longValue());
                    averageActivityLevel = acitveMinutesInMinutes + " mins";
                    indepVarTextView.setText("stay active");
                } else if (independentVar.contains(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                    recommendedGoal = recommendedGoal + " Cal";
                    averageActivityLevel = averageActivityLevel + " Cal";
                    indepVarTextView.setText("burn calories");
                } else if (independentVar.contains("sedentary")) {
                    acitveMinutesInMinutes = TimeUnit.MILLISECONDS.toMinutes(recommendedGoalDouble.longValue());
                    recommendedGoal = acitveMinutesInMinutes + " mins";
                    acitveMinutesInMinutes = TimeUnit.MILLISECONDS.toMinutes(averageActivityLevelDouble.longValue());
                    averageActivityLevel = acitveMinutesInMinutes + " mins";
                    indepVarTextView.setText("stay sedentary");
                } else if (independentVar.contains(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                    recommendedGoal = String.format(Locale.US, "%.2f", recommendedGoalDouble / 1000);
                    averageActivityLevel = String.format(Locale.US, "%.2f", averageActivityLevelDouble / 1000);
                    recommendedGoal = recommendedGoal + " km";
                    averageActivityLevel = averageActivityLevel + " km";
                    indepVarTextView.setText("travel");
                } else if (independentVar.contains("floors")) {
                    recommendedGoal = String.format(Locale.US, "%.1f", recommendedGoalDouble);
                    averageActivityLevel = String.format(Locale.US, "%.1f", averageActivityLevelDouble);
                    recommendedGoal = recommendedGoal + " floors";
                    averageActivityLevel = averageActivityLevel + " floors";
                    indepVarTextView.setText("climb");
                }
                recommendedGoalTextView.setText(recommendedGoal);
                currentGoalTextView.setText(averageActivityLevel);

                if (Double.compare(recommendedGoalDouble, averageActivityLevelDouble) < 0) {
                    recommendedGoalArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                    recommendedGoalArrowImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red));
                    indepVarDirectionTextView.setText("less");
                    indepVarDirectionTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red));
                } else if (Double.compare(recommendedGoalDouble, averageActivityLevelDouble) > 0) {
                    recommendedGoalArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                    recommendedGoalArrowImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                    indepVarDirectionTextView.setText("more");
                    indepVarDirectionTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                } else {
                    recommendedGoalArrowImageView.setVisibility(View.INVISIBLE);
                    indepVarDirectionTextView.setText("the exact amount now!\nGreat Job!");
                }
            } else if (service.contains(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                recommendedGoal = TimeUnit.MILLISECONDS.toMinutes(recommendedGoalDouble.longValue()) + " mins";
                averageActivityLevel = TimeUnit.MILLISECONDS.toMinutes(averageActivityLevelDouble.longValue()) + " mins";
                indepVarTextView.setText("use " + independentVar);
                recommendedGoalTextView.setText(recommendedGoal);
                currentGoalTextView.setText(averageActivityLevel);
                if (independentVar.contains("Total")) {
                    indepVarTextView.setText("use phone");
                }
                if (Double.compare(recommendedGoalDouble, averageActivityLevelDouble) < 0) {
                    recommendedGoalArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                    recommendedGoalArrowImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red));
                    indepVarDirectionTextView.setText("less");
                    indepVarDirectionTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red));
                } else if (Double.compare(recommendedGoalDouble, averageActivityLevelDouble) > 0) {
                    recommendedGoalArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                    recommendedGoalArrowImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                    indepVarDirectionTextView.setText("more");
                    indepVarDirectionTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                } else {
                    recommendedGoalArrowImageView.setVisibility(View.INVISIBLE);
                    indepVarDirectionTextView.setText("the exact amount now!\nGreat Job!");
                }
            } else if (service.contains(itemView.getContext().getString(R.string.timer_camel_case))) {
                recommendedGoal = TimeUnit.MILLISECONDS.toMinutes(recommendedGoalDouble.longValue()) + " mins";
                averageActivityLevel = TimeUnit.MILLISECONDS.toMinutes(averageActivityLevelDouble.longValue()) + " mins";
                whenSubjectTextView.setText("when you do ");
                indepVarTextView.setText(independentVar);
                recommendedGoalTextView.setText(recommendedGoal);
                currentGoalTextView.setText(averageActivityLevel);
                if (Double.compare(recommendedGoalDouble, averageActivityLevelDouble) < 0) {
                    recommendedGoalArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                    recommendedGoalArrowImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red));
                    indepVarDirectionTextView.setText("less");
                    indepVarDirectionTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red));
                } else if (Double.compare(recommendedGoalDouble, averageActivityLevelDouble) > 0) {
                    recommendedGoalArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                    recommendedGoalArrowImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                    indepVarDirectionTextView.setText("more");
                    indepVarDirectionTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                } else {
                    recommendedGoalArrowImageView.setVisibility(View.INVISIBLE);
                    indepVarDirectionTextView.setText("the exact amount now!\nGreat Job!");
                }
            } else if (service.contains(itemView.getContext().getString(R.string.baactivity_camel_case))) {
                recommendedGoal = recommendedGoalDouble.longValue() + " times";
                averageActivityLevel = averageActivityLevelDouble.longValue() + " times";
                indepVarTextView.setText("do " + independentVar);
                recommendedGoalTextView.setText(recommendedGoal);
                currentGoalTextView.setText(averageActivityLevel);
                if (Double.compare(recommendedGoalDouble, averageActivityLevelDouble) < 0) {
                    recommendedGoalArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                    recommendedGoalArrowImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red));
                    indepVarDirectionTextView.setText("less");
                    indepVarDirectionTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red));
                } else if (Double.compare(recommendedGoalDouble, averageActivityLevelDouble) > 0) {
                    recommendedGoalArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                    recommendedGoalArrowImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                    indepVarDirectionTextView.setText("more");
                    indepVarDirectionTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                } else {
                    recommendedGoalArrowImageView.setVisibility(View.INVISIBLE);
                    indepVarDirectionTextView.setText("the exact amount now!\nGreat Job!");
                }
            } else if (service.contains(itemView.getContext().getString(R.string.weather_camel_case))) {
                whenSubjectTextView.setText("when ");
                recommendedGoal = String.format(Locale.US, "%.2f", recommendedGoalDouble);
                averageActivityLevel = String.format(Locale.US, "%.2f", averageActivityLevelDouble);
                if (independentVar.contains("humidity")) {
                    recommendedGoal = recommendedGoal + " %";
                    averageActivityLevel = averageActivityLevel + " %";
                    indepVarTextView.setText("the humidity is");
                } else if (independentVar.contains("precipitation")) {
                    recommendedGoal = recommendedGoal + " mm";
                    averageActivityLevel = averageActivityLevel + " mm";
                    indepVarTextView.setText("the precipitation is");
                } else if (independentVar.contains("temperature")) {
                    recommendedGoal = recommendedGoal + " °C";
                    averageActivityLevel = averageActivityLevel + " °C";
                    indepVarTextView.setText("the temperature is");
                }
                recommendedGoalTextView.setText(recommendedGoal);
                currentGoalTextView.setText(averageActivityLevel);
                if (Double.compare(recommendedGoalDouble, averageActivityLevelDouble) < 0) {
                    recommendedGoalArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                    recommendedGoalArrowImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red));
                    indepVarDirectionTextView.setText("less");
                    indepVarDirectionTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red));
                } else if (Double.compare(recommendedGoalDouble, averageActivityLevelDouble) > 0) {
                    recommendedGoalArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                    recommendedGoalArrowImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                    indepVarDirectionTextView.setText("more");
                    indepVarDirectionTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                } else {
                    recommendedGoalArrowImageView.setVisibility(View.INVISIBLE);
                    indepVarDirectionTextView.setText("the exact amount now!\nGreat Job!");
                }
            }

        }

        @Override
        public void unbindView(InsightRecommendationItem item) {

        }
    }
}
