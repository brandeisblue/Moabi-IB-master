package com.ivorybridge.moabi.ui.recyclerviewitem.insight;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.StackingBehavior;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.stats.SimpleRegressionSummary;
import com.ivorybridge.moabi.database.entity.util.UserGoal;
import com.ivorybridge.moabi.repository.UserGoalRepository;
import com.ivorybridge.moabi.service.UserGoalJob;
import com.ivorybridge.moabi.service.UserGoalPeriodicJob;
import com.ivorybridge.moabi.ui.fragment.InsightMindFragment;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.UserGoalViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.refactor.library.SmoothCheckBox;

public class InsightTopThreeItem extends AbstractItem<InsightTopThreeItem, InsightTopThreeItem.ViewHolder> {

    private static final String TAG = InsightTopThreeItem.class.getSimpleName();
    private List<SimpleRegressionSummary> data;
    private String insightType;
    private Fragment fragment;
    private Activity activity;

    public InsightTopThreeItem() {
    }

    public InsightTopThreeItem(Fragment fragment, Activity activity, String insightType, List<SimpleRegressionSummary> data) {
        this.fragment = fragment;
        this.insightType = insightType;
        this.data = data;
        this.activity = activity;
    }


    @Override
    public int getType() {
        return R.id.insight_topthree_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_insight_topthree_item;
    }

    @NonNull
    @Override
    public InsightTopThreeItem.ViewHolder getViewHolder(View v) {
        return new InsightTopThreeItem.ViewHolder(v);
    }

    // Manually create the ViewHolder class
    protected static class ViewHolder extends FastAdapter.ViewHolder<InsightTopThreeItem> {

        @BindView(R.id.rv_item_insight_topthree_item_title_textview)
        TextView titleTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top1_imageview)
        ImageView top1ImageView;
        @BindView(R.id.rv_item_insight_topthree_item_top1_average_textview)
        TextView top1AvgTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top1_average_name_textview)
        TextView top1AvgNameTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top1_average_proposition_textview)
        TextView top1AvgPropTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top1_layout)
        RelativeLayout top1Layout;
        @BindView(R.id.rv_item_insight_topthree_item_top1_recommendation_prompt_textview)
        TextView top1RecPromptTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top1_recommendation_textview)
        TextView top1RecTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top1_recommendation_arrow_imageview)
        ImageView top1ArrowImageView;
        @BindView(R.id.rv_item_insight_topthree_item_top1_recommendation_checkbox)
        SmoothCheckBox top1CheckBox;
        @BindView(R.id.rv_item_insight_topthree_item_top2_imageview)
        ImageView top2ImageView;
        @BindView(R.id.rv_item_insight_topthree_item_top2_average_textview)
        TextView top2AvgTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top2_average_name_textview)
        TextView top2AvgNameTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top2_average_proposition_textview)
        TextView top2AvgPropTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top2_layout)
        RelativeLayout top2Layout;
        @BindView(R.id.rv_item_insight_topthree_item_top2_recommendation_prompt_textview)
        TextView top2RecPromptTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top2_recommendation_textview)
        TextView top2RecTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top2_recommendation_arrow_imageview)
        ImageView top2ArrowImageView;
        @BindView(R.id.rv_item_insight_topthree_item_top2_recommendation_checkbox)
        SmoothCheckBox top2CheckBox;
        @BindView(R.id.rv_item_insight_topthree_item_top3_imageview)
        ImageView top3ImageView;
        @BindView(R.id.rv_item_insight_topthree_item_top3_average_textview)
        TextView top3AvgTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top3_average_name_textview)
        TextView top3AvgNameTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top3_average_proposition_textview)
        TextView top3AvgPropTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top3_layout)
        RelativeLayout top3Layout;
        @BindView(R.id.rv_item_insight_topthree_item_top3_recommendation_prompt_textview)
        TextView top3RecPromptTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top3_recommendation_textview)
        TextView top3RecTextView;
        @BindView(R.id.rv_item_insight_topthree_item_top3_recommendation_arrow_imageview)
        ImageView top3ArrowImageView;
        @BindView(R.id.rv_item_insight_topthree_item_top3_recommendation_checkbox)
        SmoothCheckBox top3CheckBox;
        private UserGoalRepository userGoalRepository;
        private FormattedTime formattedTime;
        private SharedPreferences userGoalSharedPreferences;
        private SharedPreferences.Editor userGoalSPEditor;
        private UserGoalViewModel userGoalViewModel;
        private SharedPreferences unitSharedPreferences;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final InsightTopThreeItem item, List<Object> payloads) {
            unitSharedPreferences = itemView.getContext().getSharedPreferences(
                    itemView.getContext().getString(
                            R.string.com_ivorybridge_moabi_UNIT_SHARED_PREFERENCE_KEY),
                    Context.MODE_PRIVATE);
            String unit = unitSharedPreferences.getString(itemView.getContext()
                            .getString(R.string.com_ivorybridge_mobai_UNIT_KEY),
                    itemView.getContext().getString(R.string.preference_unit_si_title));
            userGoalRepository = new UserGoalRepository(item.fragment.getActivity().getApplication());
            formattedTime = new FormattedTime();
            userGoalSharedPreferences = itemView.getContext().getSharedPreferences(
                    itemView.getContext().getString(R.string.com_ivorybridge_moabi_USER_GOAL_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
            userGoalSPEditor = userGoalSharedPreferences.edit();
            if (item.data.size() == 1) {
                titleTextView.setText(itemView.getContext().getString(R.string.insight_top1_title));
                SimpleRegressionSummary summary = item.data.get(0);
                Log.i(TAG, summary.getDepVar() + " x " + summary.getIndepVarType() + " " + summary.getIndepVar()
                        + ": " + summary.getCoefOfDetermination()
                        + " - " + summary.getRecommendedActivityLevel());
                int drawable = R.drawable.ic_appusage;
                String indepVarType = "";
                if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                    drawable = R.drawable.ic_appusage;
                    indepVarType = itemView.getContext().getString(R.string.phone_usage_title);
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                    drawable = R.drawable.ic_fitbit_logo;
                    indepVarType = itemView.getContext().getString(R.string.fitbit_title);
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                    drawable = R.drawable.ic_googlefit;
                    indepVarType = itemView.getContext().getString(R.string.googlefit_title);
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.weather_camel_case))) {
                    drawable = R.drawable.ic_partly_cloudy;
                    indepVarType = itemView.getContext().getString(R.string.weather_title);
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.moabi_tracker_camel_case))) {
                    drawable = R.drawable.ic_monogram_colored;
                    indepVarType = itemView.getContext().getString(R.string.moabi_tracker_title);
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.timer_camel_case))) {
                    drawable = R.drawable.ic_stopwatch;
                    indepVarType = itemView.getContext().getString(R.string.timer_title);
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.baactivity_camel_case))) {
                    drawable = R.drawable.ic_physical_activity_black;
                    indepVarType = itemView.getContext().getString(R.string.baactivity_title);
                }
                final int finalDrawable = drawable;
                String indepVarAction = "";
                String averageIndepVarLevel = "";
                String recommendedIndepVarLevel = "";
                String avgProposition = "";

                final String indepVarTypeName = indepVarType;
                final int index = 0;
                userGoalViewModel = ViewModelProviders.of(item.fragment).get(UserGoalViewModel.class);
                userGoalViewModel.getGoal(0).observe(item.fragment, new Observer<UserGoal>() {
                    @Override
                    public void onChanged(UserGoal userGoal) {
                        if (userGoal != null) {
                            if (index == 0) {
                                //Log.i(TAG, userGoal.getGoalName() + ", " + userGoal.getGoalType() + ", " + userGoal.getGoal());
                                //Log.i(TAG, summary.getIndepVar() + ", " + summary.getIndepVarType() + ", " + summary.getRecommendedActivityLevel());
                                if (userGoal.getGoalName().equals(summary.getIndepVar()) &&
                                        userGoal.getGoalType().equals(summary.getIndepVarType())
                                        && userGoal.getGoal().equals(summary.getRecommendedActivityLevel())) {
                                    top1CheckBox.setChecked(true);
                                } else {
                                    top1CheckBox.setChecked(false);
                                }
                            }
                        }
                    }
                });
                if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                    //top1ImageView.setImageResource(R.drawable.ic_appusage);
                    if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.phone_usage_total_title))) {
                        indepVarAction = "Using phone";
                    } else {
                        indepVarAction = "Using " + summary.getIndepVar();
                    }
                    avgProposition = " for ";
                    averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " " + itemView.getContext().getString(R.string.unit_time_sing);
                    recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) + " " + itemView.getContext().getString(R.string.unit_time_sing);
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                    //top1ImageView.setImageResource(R.drawable.ic_fitbit_logo);
                    if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                        //top1AvgNameTextView.setText("Moving");
                        indepVarAction = "Traveling";
                        avgProposition = " ";
                        if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_distance_si);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_distance_si);
                        } else {
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getAverageActivityLevel() * 0.62417f))
                                    + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getRecommendedActivityLevel() * 0.62417f))
                                    + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                        }
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                        indepVarAction = "Walking up";
                        avgProposition = " ";
                        averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_floor_plur);
                        recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_floor_plur);
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                        indepVarAction = "Taking";
                        avgProposition = " ";
                        averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_step_plur);
                        recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_step_plur);
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_active_minutes_camel_case))) {
                        indepVarAction = "Staying active";
                        avgProposition = " for ";
                        averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                    } else if (summary.getIndepVar().contains(itemView.getContext().getString(R.string.activity_sleep_camel_case))) {
                        indepVarAction = "Sleeping";
                        avgProposition = " for ";
                        averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                    } else if (summary.getIndepVar().contains(itemView.getContext().getString(R.string.activity_sedentary_minutes_camel_case))) {
                        indepVarAction = "Staying sedentary";
                        avgProposition = " for ";
                        averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                        indepVarAction = "Burning";
                        avgProposition = " ";
                        averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_calories);
                        recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_calories);
                    }
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case))
                        || summary.getIndepVarType().equals(itemView.getContext().getString(R.string.moabi_tracker_camel_case))) {
                    //top1ImageView.setImageResource(R.drawable.ic_googlefit);
                    if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                        //top1AvgNameTextView.setText("Moving");
                        indepVarAction = "Traveling";
                        avgProposition = " ";
                        if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel() / 1000)
                                    + " " + itemView.getContext().getString(R.string.unit_distance_si);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel() / 1000)
                                    + " " + itemView.getContext().getString(R.string.unit_distance_si);
                        } else {
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getAverageActivityLevel() / 1000 * 0.62417f))
                                    + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getRecommendedActivityLevel() / 1000 * 0.62417f))
                                    + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                        }
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                        indepVarAction = "Walking up";
                        avgProposition = " ";
                        averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_floor_plur);
                        recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_floor_plur);
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                        indepVarAction = "Taking";
                        avgProposition = " ";
                        averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_step_plur);
                        recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_step_plur);
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_active_minutes_camel_case))) {
                        indepVarAction = "Staying active";
                        avgProposition = " for ";
                        averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                    } else if (summary.getIndepVar().contains(itemView.getContext().getString(R.string.activity_sleep_camel_case))) {
                        indepVarAction = "Sleeping";
                        avgProposition = " for ";
                        averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                    } else if (summary.getIndepVar().contains(itemView.getContext().getString(R.string.activity_sedentary_minutes_camel_case))) {
                        indepVarAction = "Staying sedentary";
                        avgProposition = " for ";
                        averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                        indepVarAction = "Burning";
                        avgProposition = " ";
                        averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_calories);
                        recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_calories);
                    }
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.weather_camel_case))) {
                    if (summary.getIndepVar().contains("humidity")) {
                        indepVarAction = "Humidity";
                        avgProposition = " of ";
                        averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " %";
                        recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " %";
                    } else if (summary.getIndepVar().contains("temperature")) {
                        indepVarAction = "Temperature";
                        avgProposition = " of ";
                        if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_temp_si);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_temp_si);
                        } else {
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getAverageActivityLevel() * (5 / 9) + 32))
                                    + " " + itemView.getContext().getString(R.string.unit_temp_usc);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getRecommendedActivityLevel() * (5 / 9) + 32))
                                    + " " + itemView.getContext().getString(R.string.unit_temp_usc);
                        }
                    }
                    if (summary.getIndepVar().contains("precipitation")) {
                        indepVarAction = "Precipitation";
                        avgProposition = " of ";
                        if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                            averageIndepVarLevel = String.format(Locale.US, "%.2f",
                                    summary.getAverageActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_precip_si);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f",
                                    summary.getRecommendedActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_precip_si);
                        } else {
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getAverageActivityLevel() / 25.4))
                                    + " " + itemView.getContext().getString(R.string.unit_precip_usc);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getRecommendedActivityLevel() / 25.4))
                                    + " " + itemView.getContext().getString(R.string.unit_precip_usc);
                        }
                    }
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.timer_camel_case))) {
                    indepVarAction = summary.getIndepVar();
                    avgProposition = " for ";
                    averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue())
                            + " " + itemView.getContext().getString(R.string.unit_time_sing);
                    recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue())
                            + " " + itemView.getContext().getString(R.string.unit_time_sing);
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.baactivity_camel_case))) {
                    indepVarAction = summary.getIndepVar();
                    avgProposition = " ";
                    averageIndepVarLevel = String.format("%.2f", summary.getAverageActivityLevel())
                            + " " + itemView.getContext().getString(R.string.unit_activity_plur);
                    recommendedIndepVarLevel = String.format("%.2f", summary.getRecommendedActivityLevel())
                            + " " + itemView.getContext().getString(R.string.unit_activity_plur);
                }
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(itemView.getContext());
                SharedPreferences.Editor e = getPrefs.edit();
                boolean tutInsightComplete = getPrefs.getBoolean("tut_insight_complete", false);
                getPrefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        boolean tutInsightComplete = getPrefs.getBoolean("tut_insight_complete", false);
                        if (tutInsightComplete) {
                            top1Layout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.i(TAG, "Top 1 layout clicked");
                                    new MaterialDialog.Builder(itemView.getContext())
                                            .title(indepVarTypeName)
                                            .content("Do you want to track the recommendation?")
                                            .positiveText("Yes")
                                            .negativeText("No")
                                            .limitIconToDefaultSize()
                                            .iconRes(finalDrawable)
                                            .stackingBehavior(StackingBehavior.ADAPTIVE)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                    userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                    userGoalSPEditor.commit();
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
                        } else {
                            TapTargetView.showFor(item.activity, TapTarget.forView(itemView.getRootView().findViewById(R.id.rv_item_insight_topthree_item_top1_layout),
                                    itemView.getContext().getString(R.string.tutorial_insight_title),
                                    "")
                                            .outerCircleColor(R.color.colorPrimary)
                                            .outerCircleAlpha(0.7f)
                                            .targetCircleColor(R.color.white)
                                            .titleTextSize(16)
                                            //.titleTextColor(R.color.colorPrimary)      // Specify the color of the title text
                                            .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                            //.descriptionTextColor(R.color.white)  // Specify the color of the description text
                                            .textColor(R.color.white)
                                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                            .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                                            .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                            .tintTarget(false)                   // Whether to tint the target view's color
                                            .transparentTarget(true)          // Specify whether the target is transparent (displays the content underneath)
                                            //.icon(ContextCompat.getDrawable(this, R.drawable.bg_rectangle_rounded_white))           // Specify a custom drawable to draw as the target
                                            .targetRadius(72),                  // Specify the target radius (in dp)
                                    new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                                        @Override
                                        public void onTargetClick(TapTargetView view) {
                                            super.onTargetClick(view);      // This call is optional
                                            Log.i(TAG, "Top 1 layout clicked");
                                            new MaterialDialog.Builder(itemView.getContext())
                                                    .title(indepVarTypeName)
                                                    .content("Do you want to track the recommendation?")
                                                    .positiveText("Yes")
                                                    .negativeText("No")
                                                    .limitIconToDefaultSize()
                                                    .iconRes(finalDrawable)
                                                    .stackingBehavior(StackingBehavior.ADAPTIVE)
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            e.putBoolean("tut_insight_complete", true);
                                                            e.commit();
                                                            userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                            userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                            userGoalSPEditor.commit();
                                                            UserGoal userGoal = new UserGoal();
                                                            userGoal.setGoalName(summary.getIndepVar());
                                                            userGoal.setGoalType(summary.getIndepVarType());
                                                            userGoal.setPriority(0);
                                                            userGoal.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                                                            userGoal.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                                            userGoal.setGoal(summary.getRecommendedActivityLevel());
                                                            userGoalRepository.insert(userGoal);
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
                        }
                    }
                });
                if (!tutInsightComplete && item.fragment instanceof InsightMindFragment) {
                    TapTargetView.showFor(item.activity, TapTarget.forView(itemView.getRootView().findViewById(R.id.rv_item_insight_topthree_item_top1_layout),
                            itemView.getContext().getString(R.string.tutorial_insight_title),
                            "")
                                    .outerCircleColor(R.color.colorPrimary)
                                    .outerCircleAlpha(0.7f)
                                    .targetCircleColor(R.color.white)
                                    .titleTextSize(16)
                                    //.titleTextColor(R.color.colorPrimary)      // Specify the color of the title text
                                    .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                    //.descriptionTextColor(R.color.white)  // Specify the color of the description text
                                    .textColor(R.color.white)
                                    .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                    .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                    .drawShadow(true)                   // Whether to draw a drop shadow or not
                                    .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                    .tintTarget(false)                   // Whether to tint the target view's color
                                    .transparentTarget(true)          // Specify whether the target is transparent (displays the content underneath)
                                    //.icon(ContextCompat.getDrawable(this, R.drawable.bg_rectangle_rounded_white))           // Specify a custom drawable to draw as the target
                                    .targetRadius(72),                  // Specify the target radius (in dp)
                            new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                                @Override
                                public void onTargetClick(TapTargetView view) {
                                    super.onTargetClick(view);      // This call is optional
                                    Log.i(TAG, "Top 1 layout clicked");
                                    new MaterialDialog.Builder(itemView.getContext())
                                            .title(indepVarTypeName)
                                            .content("Do you want to track the recommendation?")
                                            .positiveText("Yes")
                                            .negativeText("No")
                                            .limitIconToDefaultSize()
                                            .iconRes(finalDrawable)
                                            .stackingBehavior(StackingBehavior.ADAPTIVE)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    e.putBoolean("tut_insight_complete", true);
                                                    e.commit();
                                                    userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                    userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                    userGoalSPEditor.commit();
                                                    UserGoal userGoal = new UserGoal();
                                                    userGoal.setGoalName(summary.getIndepVar());
                                                    userGoal.setGoalType(summary.getIndepVarType());
                                                    userGoal.setPriority(0);
                                                    userGoal.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                                                    userGoal.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                                    userGoal.setGoal(summary.getRecommendedActivityLevel());
                                                    userGoalRepository.insert(userGoal);
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
                } else {
                    top1Layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i(TAG, "Top 1 layout clicked");
                            new MaterialDialog.Builder(itemView.getContext())
                                    .title(indepVarTypeName)
                                    .content("Do you want to track the recommendation?")
                                    .positiveText("Yes")
                                    .negativeText("No")
                                    .limitIconToDefaultSize()
                                    .iconRes(finalDrawable)
                                    .stackingBehavior(StackingBehavior.ADAPTIVE)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                            userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                            userGoalSPEditor.commit();
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
                }
                if (summary.getRecommendedActivityLevel() > summary.getAverageActivityLevel()) {
                    top1ArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                    ImageViewCompat.setImageTintList(top1ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary)));
                } else {
                    top1ArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                    ImageViewCompat.setImageTintList(top1ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red)));
                }
                top1ImageView.setImageResource(finalDrawable);
                top1AvgNameTextView.setText(indepVarAction);
                top1AvgPropTextView.setText(avgProposition);
                top1AvgTextView.setText(averageIndepVarLevel);
                top1RecTextView.setText(recommendedIndepVarLevel);
            } else if (item.data.size() == 2) {
                titleTextView.setText(itemView.getContext().getString(R.string.insight_top2_title));
                for (int i = 0; i < 2; i++) {
                    SimpleRegressionSummary summary = item.data.get(i);
                    Log.i(TAG, summary.getDepVar() + " x " + summary.getIndepVarType() + " " + summary.getIndepVar()
                            + ": " + summary.getCoefOfDetermination()
                            + " - " + summary.getRecommendedActivityLevel());
                    int drawable = R.drawable.ic_appusage;
                    String indepVarType = "";
                    if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                        drawable = R.drawable.ic_appusage;
                        indepVarType = itemView.getContext().getString(R.string.phone_usage_title);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                        drawable = R.drawable.ic_fitbit_logo;
                        indepVarType = itemView.getContext().getString(R.string.fitbit_title);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                        drawable = R.drawable.ic_googlefit;
                        indepVarType = itemView.getContext().getString(R.string.googlefit_title);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.weather_camel_case))) {
                        drawable = R.drawable.ic_partly_cloudy;
                        indepVarType = itemView.getContext().getString(R.string.weather_title);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.moabi_tracker_camel_case))) {
                        drawable = R.drawable.ic_monogram_colored;
                        indepVarType = itemView.getContext().getString(R.string.moabi_tracker_title);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.timer_camel_case))) {
                        drawable = R.drawable.ic_stopwatch;
                        indepVarType = itemView.getContext().getString(R.string.timer_title);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.baactivity_camel_case))) {
                        drawable = R.drawable.ic_physical_activity_black;
                        indepVarType = itemView.getContext().getString(R.string.baactivity_title);
                    }
                    final int finalDrawable = drawable;
                    String indepVarAction = "";
                    String averageIndepVarLevel = "";
                    String recommendedIndepVarLevel = "";
                    String avgProposition = "";

                    final String indepVarTypeName = indepVarType;
                    final int index = i;
                    userGoalViewModel = ViewModelProviders.of(item.fragment).get(UserGoalViewModel.class);
                    userGoalViewModel.getGoal(0).observe(item.fragment, new Observer<UserGoal>() {
                        @Override
                        public void onChanged(UserGoal userGoal) {
                            if (userGoal != null) {
                                if (index == 0) {
                                    //Log.i(TAG, userGoal.getGoalName() + ", " + userGoal.getGoalType() + ", " + userGoal.getGoal());
                                    //Log.i(TAG, summary.getIndepVar() + ", " + summary.getIndepVarType() + ", " + summary.getRecommendedActivityLevel());
                                    if (userGoal.getGoalName().equals(summary.getIndepVar()) &&
                                            userGoal.getGoalType().equals(summary.getIndepVarType())
                                            && userGoal.getGoal().equals(summary.getRecommendedActivityLevel())) {
                                        top1CheckBox.setChecked(true);
                                    } else {
                                        top1CheckBox.setChecked(false);
                                    }
                                } else if (index == 1) {
                                    //Log.i(TAG, userGoal.getGoalName() + ", " + userGoal.getGoalType() + ", " + userGoal.getGoal());
                                    //Log.i(TAG, summary.getIndepVar() + ", " + summary.getIndepVarType() + ", " + summary.getRecommendedActivityLevel());
                                    if (userGoal.getGoalName().equals(summary.getIndepVar()) &&
                                            userGoal.getGoalType().equals(summary.getIndepVarType())
                                            && userGoal.getGoal().equals(summary.getRecommendedActivityLevel())) {
                                        top2CheckBox.setChecked(true);
                                    } else {
                                        top2CheckBox.setChecked(false);
                                    }
                                }
                            }
                        }
                    });
                    if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                        //top1ImageView.setImageResource(R.drawable.ic_appusage);
                        if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.phone_usage_total_title))) {
                            indepVarAction = "Using phone";
                        } else {
                            indepVarAction = "Using " + summary.getIndepVar();
                        }
                        avgProposition = " for ";
                        averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) + " " + itemView.getContext().getString(R.string.unit_time_sing);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                        //top1ImageView.setImageResource(R.drawable.ic_fitbit_logo);
                        if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                            //top1AvgNameTextView.setText("Moving");
                            indepVarAction = "Traveling";
                            avgProposition = " ";
                            if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_distance_si);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_distance_si);
                            } else {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getAverageActivityLevel() * 0.62417f))
                                        + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getRecommendedActivityLevel() * 0.62417f))
                                        + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                            }
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                            indepVarAction = "Walking up";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_floor_plur);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_floor_plur);
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                            indepVarAction = "Taking";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_step_plur);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_step_plur);
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_active_minutes_camel_case))) {
                            indepVarAction = "Staying active";
                            avgProposition = " for ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        } else if (summary.getIndepVar().contains(itemView.getContext().getString(R.string.activity_sleep_camel_case))) {
                            indepVarAction = "Sleeping";
                            avgProposition = " for ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        } else if (summary.getIndepVar().contains(itemView.getContext().getString(R.string.activity_sedentary_minutes_camel_case))) {
                            indepVarAction = "Staying sedentary";
                            avgProposition = " for ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                            indepVarAction = "Burning";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_calories);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_calories);
                        }
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case))
                            || summary.getIndepVarType().equals(itemView.getContext().getString(R.string.moabi_tracker_camel_case))) {
                        //top1ImageView.setImageResource(R.drawable.ic_googlefit);
                        if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                            //top1AvgNameTextView.setText("Moving");
                            indepVarAction = "Traveling";
                            avgProposition = " ";
                            if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel() / 1000)
                                        + " " + itemView.getContext().getString(R.string.unit_distance_si);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel() / 1000)
                                        + " " + itemView.getContext().getString(R.string.unit_distance_si);
                            } else {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getAverageActivityLevel() / 1000 * 0.62417f))
                                        + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getRecommendedActivityLevel() / 1000 * 0.62417f))
                                        + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                            }
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                            indepVarAction = "Walking up";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_floor_plur);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_floor_plur);
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                            indepVarAction = "Taking";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_step_plur);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_step_plur);
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_active_minutes_camel_case))) {
                            indepVarAction = "Staying active";
                            avgProposition = " for ";
                            averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                            recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        } else if (summary.getIndepVar().contains(itemView.getContext().getString(R.string.activity_sleep_camel_case))) {
                            indepVarAction = "Sleeping";
                            avgProposition = " for ";
                            averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                            recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        } else if (summary.getIndepVar().contains(itemView.getContext().getString(R.string.activity_sedentary_minutes_camel_case))) {
                            indepVarAction = "Staying sedentary";
                            avgProposition = " for ";
                            averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                            recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                            indepVarAction = "Burning";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_calories);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_calories);
                        }
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.weather_camel_case))) {
                        if (summary.getIndepVar().contains("humidity")) {
                            indepVarAction = "Humidity";
                            avgProposition = " of ";
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " %";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " %";
                        } else if (summary.getIndepVar().contains("temperature")) {
                            indepVarAction = "Temperature";
                            avgProposition = " of ";
                            if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_temp_si);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_temp_si);
                            } else {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getAverageActivityLevel() * (5 / 9) + 32))
                                        + " " + itemView.getContext().getString(R.string.unit_temp_usc);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getRecommendedActivityLevel() * (5 / 9) + 32))
                                        + " " + itemView.getContext().getString(R.string.unit_temp_usc);
                            }
                        }
                        if (summary.getIndepVar().contains("precipitation")) {
                            indepVarAction = "Precipitation";
                            avgProposition = " of ";
                            if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f",
                                        summary.getAverageActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_precip_si);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f",
                                        summary.getRecommendedActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_precip_si);
                            } else {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getAverageActivityLevel() / 25.4))
                                        + " " + itemView.getContext().getString(R.string.unit_precip_usc);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getRecommendedActivityLevel() / 25.4))
                                        + " " + itemView.getContext().getString(R.string.unit_precip_usc);
                            }
                        }
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.timer_camel_case))) {
                        indepVarAction = summary.getIndepVar();
                        avgProposition = " for ";
                        averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.baactivity_camel_case))) {
                        indepVarAction = summary.getIndepVar();
                        avgProposition = " ";
                        averageIndepVarLevel = String.format("%.2f", summary.getAverageActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_activity_plur);
                        recommendedIndepVarLevel = String.format("%.2f", summary.getRecommendedActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_activity_plur);
                    }
                    if (i == 0) {
                        SharedPreferences getPrefs = PreferenceManager
                                .getDefaultSharedPreferences(itemView.getContext());
                        SharedPreferences.Editor e = getPrefs.edit();
                        boolean tutInsightComplete = getPrefs.getBoolean("tut_insight_complete", false);
                        getPrefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                            @Override
                            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                                boolean tutInsightComplete = getPrefs.getBoolean("tut_insight_complete", false);
                                if (tutInsightComplete) {
                                    top1Layout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Log.i(TAG, "Top 1 layout clicked");
                                            new MaterialDialog.Builder(itemView.getContext())
                                                    .title(indepVarTypeName)
                                                    .content("Do you want to track the recommendation?")
                                                    .positiveText("Yes")
                                                    .negativeText("No")
                                                    .limitIconToDefaultSize()
                                                    .iconRes(finalDrawable)
                                                    .stackingBehavior(StackingBehavior.ADAPTIVE)
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                            userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                            userGoalSPEditor.commit();
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
                                } else {
                                    TapTargetView.showFor(item.activity, TapTarget.forView(itemView.getRootView().findViewById(R.id.rv_item_insight_topthree_item_top1_layout),
                                            itemView.getContext().getString(R.string.tutorial_insight_title),
                                            "")
                                                    .outerCircleColor(R.color.colorPrimary)
                                                    .outerCircleAlpha(0.7f)
                                                    .targetCircleColor(R.color.white)
                                                    .titleTextSize(16)
                                                    //.titleTextColor(R.color.colorPrimary)      // Specify the color of the title text
                                                    .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                                    //.descriptionTextColor(R.color.white)  // Specify the color of the description text
                                                    .textColor(R.color.white)
                                                    .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                                    .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                                    .drawShadow(true)                   // Whether to draw a drop shadow or not
                                                    .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                                    .tintTarget(false)                   // Whether to tint the target view's color
                                                    .transparentTarget(true)          // Specify whether the target is transparent (displays the content underneath)
                                                    //.icon(ContextCompat.getDrawable(this, R.drawable.bg_rectangle_rounded_white))           // Specify a custom drawable to draw as the target
                                                    .targetRadius(72),                  // Specify the target radius (in dp)
                                            new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                                                @Override
                                                public void onTargetClick(TapTargetView view) {
                                                    super.onTargetClick(view);      // This call is optional
                                                    Log.i(TAG, "Top 1 layout clicked");
                                                    new MaterialDialog.Builder(itemView.getContext())
                                                            .title(indepVarTypeName)
                                                            .content("Do you want to track the recommendation?")
                                                            .positiveText("Yes")
                                                            .negativeText("No")
                                                            .limitIconToDefaultSize()
                                                            .iconRes(finalDrawable)
                                                            .stackingBehavior(StackingBehavior.ADAPTIVE)
                                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                                @Override
                                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                    e.putBoolean("tut_insight_complete", true);
                                                                    e.commit();
                                                                    userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                                    userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                                    userGoalSPEditor.commit();
                                                                    UserGoal userGoal = new UserGoal();
                                                                    userGoal.setGoalName(summary.getIndepVar());
                                                                    userGoal.setGoalType(summary.getIndepVarType());
                                                                    userGoal.setPriority(0);
                                                                    userGoal.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                                                                    userGoal.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                                                    userGoal.setGoal(summary.getRecommendedActivityLevel());
                                                                    userGoalRepository.insert(userGoal);
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
                                }
                            }
                        });
                        if (!tutInsightComplete && item.fragment instanceof InsightMindFragment) {
                            TapTargetView.showFor(item.activity, TapTarget.forView(itemView.getRootView().findViewById(R.id.rv_item_insight_topthree_item_top1_layout),
                                    itemView.getContext().getString(R.string.tutorial_insight_title),
                                    "")
                                            .outerCircleColor(R.color.colorPrimary)
                                            .outerCircleAlpha(0.7f)
                                            .targetCircleColor(R.color.white)
                                            .titleTextSize(16)
                                            //.titleTextColor(R.color.colorPrimary)      // Specify the color of the title text
                                            .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                            //.descriptionTextColor(R.color.white)  // Specify the color of the description text
                                            .textColor(R.color.white)
                                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                            .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                                            .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                            .tintTarget(false)                   // Whether to tint the target view's color
                                            .transparentTarget(true)          // Specify whether the target is transparent (displays the content underneath)
                                            //.icon(ContextCompat.getDrawable(this, R.drawable.bg_rectangle_rounded_white))           // Specify a custom drawable to draw as the target
                                            .targetRadius(72),                  // Specify the target radius (in dp)
                                    new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                                        @Override
                                        public void onTargetClick(TapTargetView view) {
                                            super.onTargetClick(view);      // This call is optional
                                            Log.i(TAG, "Top 1 layout clicked");
                                            new MaterialDialog.Builder(itemView.getContext())
                                                    .title(indepVarTypeName)
                                                    .content("Do you want to track the recommendation?")
                                                    .positiveText("Yes")
                                                    .negativeText("No")
                                                    .limitIconToDefaultSize()
                                                    .iconRes(finalDrawable)
                                                    .stackingBehavior(StackingBehavior.ADAPTIVE)
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            e.putBoolean("tut_insight_complete", true);
                                                            e.commit();
                                                            userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                            userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                            userGoalSPEditor.commit();
                                                            UserGoal userGoal = new UserGoal();
                                                            userGoal.setGoalName(summary.getIndepVar());
                                                            userGoal.setGoalType(summary.getIndepVarType());
                                                            userGoal.setPriority(0);
                                                            userGoal.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                                                            userGoal.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                                            userGoal.setGoal(summary.getRecommendedActivityLevel());
                                                            userGoalRepository.insert(userGoal);
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
                        } else {
                            top1Layout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.i(TAG, "Top 1 layout clicked");
                                    new MaterialDialog.Builder(itemView.getContext())
                                            .title(indepVarTypeName)
                                            .content("Do you want to track the recommendation?")
                                            .positiveText("Yes")
                                            .negativeText("No")
                                            .limitIconToDefaultSize()
                                            .iconRes(finalDrawable)
                                            .stackingBehavior(StackingBehavior.ADAPTIVE)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                    userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                    userGoalSPEditor.commit();
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
                        }
                        if (summary.getRecommendedActivityLevel() > summary.getAverageActivityLevel()) {
                            top1ArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                            ImageViewCompat.setImageTintList(top1ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary)));
                        } else {
                            top1ArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                            ImageViewCompat.setImageTintList(top1ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red)));
                        }
                        top1ImageView.setImageResource(finalDrawable);
                        top1AvgNameTextView.setText(indepVarAction);
                        top1AvgPropTextView.setText(avgProposition);
                        top1AvgTextView.setText(averageIndepVarLevel);
                        top1RecTextView.setText(recommendedIndepVarLevel);
                    } else if (i == 1) {
                        top2Layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i(TAG, "Top 2 layout clicked");
                                new MaterialDialog.Builder(itemView.getContext())
                                        .title(indepVarTypeName)
                                        .content("Do you want to track the recommendation?")
                                        .positiveText("Yes")
                                        .negativeText("No")
                                        .limitIconToDefaultSize()
                                        .iconRes(finalDrawable)
                                        .stackingBehavior(StackingBehavior.ADAPTIVE)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                userGoalSPEditor.commit();
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
                        if (summary.getRecommendedActivityLevel() > summary.getAverageActivityLevel()) {
                            top2ArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                            ImageViewCompat.setImageTintList(top2ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary)));
                        } else {
                            top2ArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                            ImageViewCompat.setImageTintList(top2ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red)));
                        }
                        top2ImageView.setImageResource(finalDrawable);
                        top2AvgNameTextView.setText(indepVarAction);
                        top2AvgPropTextView.setText(avgProposition);
                        top2AvgTextView.setText(averageIndepVarLevel);
                        top2RecTextView.setText(recommendedIndepVarLevel);
                    }
                }
            } else if (item.data.size() > 2) {
                for (int i = 0; i < 3; i++) {
                    SimpleRegressionSummary summary = item.data.get(i);
                    Log.i(TAG, summary.getDepVar() + " x " + summary.getIndepVarType() + " " + summary.getIndepVar()
                            + ": " + summary.getCoefOfDetermination()
                            + " - " + summary.getRecommendedActivityLevel());
                    int drawable = R.drawable.ic_appusage;
                    String indepVarType = "";
                    if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                        drawable = R.drawable.ic_appusage;
                        indepVarType = itemView.getContext().getString(R.string.phone_usage_title);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                        drawable = R.drawable.ic_fitbit_logo;
                        indepVarType = itemView.getContext().getString(R.string.fitbit_title);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                        drawable = R.drawable.ic_googlefit;
                        indepVarType = itemView.getContext().getString(R.string.googlefit_title);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.weather_camel_case))) {
                        drawable = R.drawable.ic_partly_cloudy;
                        indepVarType = itemView.getContext().getString(R.string.weather_title);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.moabi_tracker_camel_case))) {
                        drawable = R.drawable.ic_monogram_colored;
                        indepVarType = itemView.getContext().getString(R.string.moabi_tracker_title);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.timer_camel_case))) {
                        drawable = R.drawable.ic_stopwatch;
                        indepVarType = itemView.getContext().getString(R.string.timer_title);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.baactivity_camel_case))) {
                        drawable = R.drawable.ic_physical_activity_black;
                        indepVarType = itemView.getContext().getString(R.string.baactivity_title);
                    }
                    final int finalDrawable = drawable;
                    String indepVarAction = "";
                    String averageIndepVarLevel = "";
                    String recommendedIndepVarLevel = "";
                    String avgProposition = "";

                    final String indepVarTypeName = indepVarType;
                    final int index = i;
                    userGoalViewModel = ViewModelProviders.of(item.fragment).get(UserGoalViewModel.class);
                    userGoalViewModel.getGoal(0).observe(item.fragment, new Observer<UserGoal>() {
                        @Override
                        public void onChanged(UserGoal userGoal) {
                            if (userGoal != null) {
                                if (index == 0) {
                                    //Log.i(TAG, userGoal.getGoalName() + ", " + userGoal.getGoalType() + ", " + userGoal.getGoal());
                                    //Log.i(TAG, summary.getIndepVar() + ", " + summary.getIndepVarType() + ", " + summary.getRecommendedActivityLevel());
                                    if (userGoal.getGoalName().equals(summary.getIndepVar()) &&
                                            userGoal.getGoalType().equals(summary.getIndepVarType())
                                            && userGoal.getGoal().equals(summary.getRecommendedActivityLevel())) {
                                        top1CheckBox.setChecked(true);
                                    } else {
                                        top1CheckBox.setChecked(false);
                                    }
                                } else if (index == 1) {
                                    //Log.i(TAG, userGoal.getGoalName() + ", " + userGoal.getGoalType() + ", " + userGoal.getGoal());
                                    //Log.i(TAG, summary.getIndepVar() + ", " + summary.getIndepVarType() + ", " + summary.getRecommendedActivityLevel());
                                    if (userGoal.getGoalName().equals(summary.getIndepVar()) &&
                                            userGoal.getGoalType().equals(summary.getIndepVarType())
                                            && userGoal.getGoal().equals(summary.getRecommendedActivityLevel())) {
                                        top2CheckBox.setChecked(true);
                                    } else {
                                        top2CheckBox.setChecked(false);
                                    }
                                } else {
                                    //Log.i(TAG, userGoal.getGoalName() + ", " + userGoal.getGoalType() + ", " + userGoal.getGoal());
                                    //Log.i(TAG, summary.getIndepVar() + ", " + summary.getIndepVarType() + ", " + summary.getRecommendedActivityLevel());
                                    if (userGoal.getGoalName().equals(summary.getIndepVar()) &&
                                            userGoal.getGoalType().equals(summary.getIndepVarType())
                                            && userGoal.getGoal().equals(summary.getRecommendedActivityLevel())) {
                                        top3CheckBox.setChecked(true);
                                    } else {
                                        top3CheckBox.setChecked(false);
                                    }
                                }
                            }
                        }
                    });
                    if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                        //top1ImageView.setImageResource(R.drawable.ic_appusage);
                        if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.phone_usage_total_title))) {
                            indepVarAction = "Using phone";
                        } else {
                            indepVarAction = "Using " + summary.getIndepVar();
                        }
                        avgProposition = " for ";
                        averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) + " " + itemView.getContext().getString(R.string.unit_time_sing);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                        //top1ImageView.setImageResource(R.drawable.ic_fitbit_logo);
                        if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                            //top1AvgNameTextView.setText("Moving");
                            indepVarAction = "Traveling";
                            avgProposition = " ";
                            if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_distance_si);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_distance_si);
                            } else {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getAverageActivityLevel() * 0.62417f))
                                        + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getRecommendedActivityLevel() * 0.62417f))
                                        + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                            }
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                            indepVarAction = "Walking up";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_floor_plur);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_floor_plur);
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                            indepVarAction = "Taking";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_step_plur);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_step_plur);
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_active_minutes_camel_case))) {
                            indepVarAction = "Staying active";
                            avgProposition = " for ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        } else if (summary.getIndepVar().contains(itemView.getContext().getString(R.string.activity_sleep_camel_case))) {
                            indepVarAction = "Sleeping";
                            avgProposition = " for ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        } else if (summary.getIndepVar().contains(itemView.getContext().getString(R.string.activity_sedentary_minutes_camel_case))) {
                            indepVarAction = "Staying sedentary";
                            avgProposition = " for ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                            indepVarAction = "Burning";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_calories);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_calories);
                        }
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case))
                            || summary.getIndepVarType().equals(itemView.getContext().getString(R.string.moabi_tracker_camel_case))) {
                        //top1ImageView.setImageResource(R.drawable.ic_googlefit);
                        if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                            //top1AvgNameTextView.setText("Moving");
                            indepVarAction = "Traveling";
                            avgProposition = " ";
                            if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel() / 1000)
                                        + " " + itemView.getContext().getString(R.string.unit_distance_si);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel() / 1000)
                                        + " " + itemView.getContext().getString(R.string.unit_distance_si);
                            } else {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getAverageActivityLevel() / 1000 * 0.62417f))
                                        + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getRecommendedActivityLevel() / 1000 * 0.62417f))
                                        + " " + itemView.getContext().getString(R.string.unit_distance_usc);
                            }
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                            indepVarAction = "Walking up";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_floor_plur);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_floor_plur);
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                            indepVarAction = "Taking";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_step_plur);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_step_plur);
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_active_minutes_camel_case))) {
                            indepVarAction = "Staying active";
                            avgProposition = " for ";
                            averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                            recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        } else if (summary.getIndepVar().contains(itemView.getContext().getString(R.string.activity_sleep_camel_case))) {
                            indepVarAction = "Sleeping";
                            avgProposition = " for ";
                            averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                            recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        } else if (summary.getIndepVar().contains(itemView.getContext().getString(R.string.activity_sedentary_minutes_camel_case))) {
                            indepVarAction = "Staying sedentary";
                            avgProposition = " for ";
                            averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                            recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue())
                                    + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                            indepVarAction = "Burning";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_calories);
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel())
                                    + " " + itemView.getContext().getString(R.string.unit_calories);
                        }
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.weather_camel_case))) {
                        if (summary.getIndepVar().contains("humidity")) {
                            indepVarAction = "Humidity";
                            avgProposition = " of ";
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " %";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " %";
                        } else if (summary.getIndepVar().contains("temperature")) {
                            indepVarAction = "Temperature";
                            avgProposition = " of ";
                            if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_temp_si);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_temp_si);
                            } else {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getAverageActivityLevel() * (5 / 9) + 32))
                                        + " " + itemView.getContext().getString(R.string.unit_temp_usc);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getRecommendedActivityLevel() * (5 / 9) + 32))
                                        + " " + itemView.getContext().getString(R.string.unit_temp_usc);
                            }
                        }
                        if (summary.getIndepVar().contains("precipitation")) {
                            indepVarAction = "Precipitation";
                            avgProposition = " of ";
                            if (unit.equals(itemView.getContext().getString(R.string.preference_unit_si_title))) {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f",
                                        summary.getAverageActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_precip_si);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f",
                                        summary.getRecommendedActivityLevel()) + " " + itemView.getContext().getString(R.string.unit_precip_si);
                            } else {
                                averageIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getAverageActivityLevel() / 25.4))
                                        + " " + itemView.getContext().getString(R.string.unit_precip_usc);
                                recommendedIndepVarLevel = String.format(Locale.US, "%.2f", (summary.getRecommendedActivityLevel() / 25.4))
                                        + " " + itemView.getContext().getString(R.string.unit_precip_usc);
                            }
                        }
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.timer_camel_case))) {
                        indepVarAction = summary.getIndepVar();
                        avgProposition = " for ";
                        averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                        recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue())
                                + " " + itemView.getContext().getString(R.string.unit_time_sing);
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.baactivity_camel_case))) {
                        indepVarAction = summary.getIndepVar();
                        avgProposition = " ";
                        averageIndepVarLevel = String.format("%.2f", summary.getAverageActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_activity_plur);
                        recommendedIndepVarLevel = String.format("%.2f", summary.getRecommendedActivityLevel())
                                + " " + itemView.getContext().getString(R.string.unit_activity_plur);
                    }
                    if (i == 0) {
                        SharedPreferences getPrefs = PreferenceManager
                                .getDefaultSharedPreferences(itemView.getContext());
                        SharedPreferences.Editor e = getPrefs.edit();
                        boolean tutInsightComplete = getPrefs.getBoolean("tut_insight_complete", false);
                        getPrefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                            @Override
                            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                                boolean tutInsightComplete = getPrefs.getBoolean("tut_insight_complete", false);
                                if (tutInsightComplete) {
                                    top1Layout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Log.i(TAG, "Top 1 layout clicked");
                                            new MaterialDialog.Builder(itemView.getContext())
                                                    .title(indepVarTypeName)
                                                    .content("Do you want to track the recommendation?")
                                                    .positiveText("Yes")
                                                    .negativeText("No")
                                                    .limitIconToDefaultSize()
                                                    .iconRes(finalDrawable)
                                                    .stackingBehavior(StackingBehavior.ADAPTIVE)
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                            userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                            userGoalSPEditor.commit();
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
                                } else {
                                    TapTargetView.showFor(item.activity, TapTarget.forView(itemView.getRootView().findViewById(R.id.rv_item_insight_topthree_item_top1_layout),
                                            itemView.getContext().getString(R.string.tutorial_insight_title),
                                            "")
                                                    .outerCircleColor(R.color.colorPrimary)
                                                    .outerCircleAlpha(0.7f)
                                                    .targetCircleColor(R.color.white)
                                                    .titleTextSize(16)
                                                    //.titleTextColor(R.color.colorPrimary)      // Specify the color of the title text
                                                    .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                                    //.descriptionTextColor(R.color.white)  // Specify the color of the description text
                                                    .textColor(R.color.white)
                                                    .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                                    .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                                    .drawShadow(true)                   // Whether to draw a drop shadow or not
                                                    .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                                    .tintTarget(false)                   // Whether to tint the target view's color
                                                    .transparentTarget(true)          // Specify whether the target is transparent (displays the content underneath)
                                                    //.icon(ContextCompat.getDrawable(this, R.drawable.bg_rectangle_rounded_white))           // Specify a custom drawable to draw as the target
                                                    .targetRadius(72),                  // Specify the target radius (in dp)
                                            new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                                                @Override
                                                public void onTargetClick(TapTargetView view) {
                                                    super.onTargetClick(view);      // This call is optional
                                                    Log.i(TAG, "Top 1 layout clicked");
                                                    new MaterialDialog.Builder(itemView.getContext())
                                                            .title(indepVarTypeName)
                                                            .content("Do you want to track the recommendation?")
                                                            .positiveText("Yes")
                                                            .negativeText("No")
                                                            .limitIconToDefaultSize()
                                                            .iconRes(finalDrawable)
                                                            .stackingBehavior(StackingBehavior.ADAPTIVE)
                                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                                @Override
                                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                    e.putBoolean("tut_insight_complete", true);
                                                                    e.commit();
                                                                    userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                                    userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                                    userGoalSPEditor.commit();
                                                                    UserGoal userGoal = new UserGoal();
                                                                    userGoal.setGoalName(summary.getIndepVar());
                                                                    userGoal.setGoalType(summary.getIndepVarType());
                                                                    userGoal.setPriority(0);
                                                                    userGoal.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                                                                    userGoal.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                                                    userGoal.setGoal(summary.getRecommendedActivityLevel());
                                                                    userGoalRepository.insert(userGoal);
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
                                }
                            }
                        });
                        if (!tutInsightComplete && item.fragment instanceof InsightMindFragment) {
                            TapTargetView.showFor(item.activity, TapTarget.forView(itemView.getRootView().findViewById(R.id.rv_item_insight_topthree_item_top1_layout),
                                    itemView.getContext().getString(R.string.tutorial_insight_title),
                                    "")
                                            .outerCircleColor(R.color.colorPrimary)
                                            .outerCircleAlpha(0.7f)
                                            .targetCircleColor(R.color.white)
                                            .titleTextSize(16)
                                            //.titleTextColor(R.color.colorPrimary)      // Specify the color of the title text
                                            .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                            //.descriptionTextColor(R.color.white)  // Specify the color of the description text
                                            .textColor(R.color.white)
                                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                            .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                                            .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                            .tintTarget(false)                   // Whether to tint the target view's color
                                            .transparentTarget(true)          // Specify whether the target is transparent (displays the content underneath)
                                            //.icon(ContextCompat.getDrawable(this, R.drawable.bg_rectangle_rounded_white))           // Specify a custom drawable to draw as the target
                                            .targetRadius(72),                  // Specify the target radius (in dp)
                                    new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                                        @Override
                                        public void onTargetClick(TapTargetView view) {
                                            super.onTargetClick(view);      // This call is optional
                                            Log.i(TAG, "Top 1 layout clicked");
                                            new MaterialDialog.Builder(itemView.getContext())
                                                    .title(indepVarTypeName)
                                                    .content("Do you want to track the recommendation?")
                                                    .positiveText("Yes")
                                                    .negativeText("No")
                                                    .limitIconToDefaultSize()
                                                    .iconRes(finalDrawable)
                                                    .stackingBehavior(StackingBehavior.ADAPTIVE)
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            e.putBoolean("tut_insight_complete", true);
                                                            e.commit();
                                                            userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                            userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                            userGoalSPEditor.commit();
                                                            UserGoal userGoal = new UserGoal();
                                                            userGoal.setGoalName(summary.getIndepVar());
                                                            userGoal.setGoalType(summary.getIndepVarType());
                                                            userGoal.setPriority(0);
                                                            userGoal.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                                                            userGoal.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                                            userGoal.setGoal(summary.getRecommendedActivityLevel());
                                                            userGoalRepository.insert(userGoal);
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
                        } else {
                            top1Layout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.i(TAG, "Top 1 layout clicked");
                                    new MaterialDialog.Builder(itemView.getContext())
                                            .title(indepVarTypeName)
                                            .content("Do you want to track the recommendation?")
                                            .positiveText("Yes")
                                            .negativeText("No")
                                            .limitIconToDefaultSize()
                                            .iconRes(finalDrawable)
                                            .stackingBehavior(StackingBehavior.ADAPTIVE)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                    userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                    userGoalSPEditor.commit();
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
                        }
                        if (summary.getRecommendedActivityLevel() > summary.getAverageActivityLevel()) {
                            top1ArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                            ImageViewCompat.setImageTintList(top1ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary)));
                        } else {
                            top1ArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                            ImageViewCompat.setImageTintList(top1ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red)));
                        }
                        top1ImageView.setImageResource(finalDrawable);
                        top1AvgNameTextView.setText(indepVarAction);
                        top1AvgPropTextView.setText(avgProposition);
                        top1AvgTextView.setText(averageIndepVarLevel);
                        top1RecTextView.setText(recommendedIndepVarLevel);
                    } else if (i == 1) {
                        top2Layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i(TAG, "Top 2 layout clicked");
                                new MaterialDialog.Builder(itemView.getContext())
                                        .title(indepVarTypeName)
                                        .content("Do you want to track the recommendation?")
                                        .positiveText("Yes")
                                        .negativeText("No")
                                        .limitIconToDefaultSize()
                                        .iconRes(finalDrawable)
                                        .stackingBehavior(StackingBehavior.ADAPTIVE)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                userGoalSPEditor.commit();
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
                        if (summary.getRecommendedActivityLevel() > summary.getAverageActivityLevel()) {
                            top2ArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                            ImageViewCompat.setImageTintList(top2ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary)));
                        } else {
                            top2ArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                            ImageViewCompat.setImageTintList(top2ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red)));
                        }
                        top2ImageView.setImageResource(finalDrawable);
                        top2AvgNameTextView.setText(indepVarAction);
                        top2AvgPropTextView.setText(avgProposition);
                        top2AvgTextView.setText(averageIndepVarLevel);
                        top2RecTextView.setText(recommendedIndepVarLevel);
                    } else {
                        top3Layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i(TAG, "Top 3 layout clicked");
                                new MaterialDialog.Builder(itemView.getContext())
                                        .title(indepVarTypeName)
                                        .content("Do you want to track the recommendation?")
                                        .positiveText("Yes")
                                        .negativeText("No")
                                        .limitIconToDefaultSize()
                                        .iconRes(finalDrawable)
                                        .stackingBehavior(StackingBehavior.ADAPTIVE)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                userGoalSPEditor.putString("inputType", summary.getIndepVarType());
                                                userGoalSPEditor.putString("activityName", summary.getIndepVar());
                                                userGoalSPEditor.commit();
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
                        if (summary.getRecommendedActivityLevel() > summary.getAverageActivityLevel()) {
                            top3ArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                            ImageViewCompat.setImageTintList(top3ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary)));
                        } else {
                            top3ArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                            ImageViewCompat.setImageTintList(top3ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red)));
                        }
                        top3ImageView.setImageResource(finalDrawable);
                        top3AvgNameTextView.setText(indepVarAction);
                        top3AvgPropTextView.setText(avgProposition);
                        top3AvgTextView.setText(averageIndepVarLevel);
                        top3RecTextView.setText(recommendedIndepVarLevel);
                    }
                }
            }
        }

        @Override
        public void unbindView(InsightTopThreeItem item) {
        }
    }
}

