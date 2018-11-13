package com.ivorybridge.moabi.ui.recyclerviewitem.insight;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.refactor.library.SmoothCheckBox;

public class InsightTopThreeItem extends AbstractItem<InsightTopThreeItem, InsightTopThreeItem.ViewHolder> {

    private static final String TAG = InsightTopThreeItem.class.getSimpleName();
    private List<SimpleRegressionSummary> data;
    private String insightType;
    private Fragment fragment;

    public InsightTopThreeItem() {
    }

    public InsightTopThreeItem(Fragment fragment, String insightType, List<SimpleRegressionSummary> data) {
        this.fragment = fragment;
        this.insightType = insightType;
        this.data = data;
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


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final InsightTopThreeItem item, List<Object> payloads) {
            userGoalRepository = new UserGoalRepository(item.fragment.getActivity().getApplication());
            formattedTime = new FormattedTime();
            userGoalSharedPreferences = itemView.getContext().getSharedPreferences(
                    itemView.getContext().getString(R.string.com_ivorybridge_moabi_USER_GOAL_SHARED_PREFERENCE_KEY), Context.MODE_PRIVATE);
            userGoalSPEditor = userGoalSharedPreferences.edit();

            if (item.data.size() == 1) {
                titleTextView.setText("Top 1 Factor");
                SimpleRegressionSummary summary = item.data.get(0);
                Log.i(TAG, summary.getDepXIndepVars()
                        + ": " + summary.getCoefOfDetermination()
                        + " - " + summary.getRecommendedActivityLevel());
                top1Layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "Top 1 layout clicked");
                        int drawable = R.drawable.ic_appusage;
                        if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                            drawable = R.drawable.ic_appusage;
                        } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                            drawable = R.drawable.ic_fitbit_logo;
                        }
                        if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                            drawable = R.drawable.ic_googlefit;
                        }
                        new MaterialDialog.Builder(itemView.getContext())
                                .title(summary.getIndepVarType())
                                .content("Do you want to track the recommended goal?")
                                .positiveText("Yes")
                                .negativeText("No")
                                .limitIconToDefaultSize()
                                .iconRes(drawable)
                                .stackingBehavior(StackingBehavior.ADAPTIVE)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
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
                    top1ArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                    ImageViewCompat.setImageTintList(top1ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary)));
                } else {
                    top1ArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                    ImageViewCompat.setImageTintList(top1ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red)));
                }
                if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                    top1ImageView.setImageResource(R.drawable.ic_appusage);
                    if (summary.getIndepVar().equals("Total")) {
                        top1AvgNameTextView.setText("Using phone");
                    } else {
                        top1AvgNameTextView.setText("Using " + summary.getIndepVar());
                    }
                    top1AvgTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                    top1RecTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) + " mins");
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                    top1ImageView.setImageResource(R.drawable.ic_fitbit_logo);
                    if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                        //top1AvgNameTextView.setText("Moving");
                        top1AvgNameTextView.setText("Travelling");
                        top1AvgPropTextView.setText(" ");
                        top1AvgTextView.setText(String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " km");
                        top1RecTextView.setText(String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " km");
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                        top1AvgNameTextView.setText("Walking up");
                        top1AvgPropTextView.setText(" ");
                        top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " floors");
                        top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " floors");
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                        top1AvgNameTextView.setText("Taking");
                        top1AvgPropTextView.setText(" ");
                        top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " steps");
                        top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " steps");
                    } else if (summary.getIndepVar().contains("active")) {
                        top1AvgNameTextView.setText("Staying active");
                        top1AvgPropTextView.setText(" for ");
                        top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins");
                        top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins");
                    } else if (summary.getIndepVar().contains("sleep")) {
                        top1AvgNameTextView.setText("Sleeping");
                        top1AvgPropTextView.setText(" for ");
                        top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins");
                        top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins");
                    } else if (summary.getIndepVar().contains("sedentary")) {
                        top1AvgNameTextView.setText("Staying sedentary");
                        top1AvgPropTextView.setText(" for ");
                        top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins");
                        top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins");
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                        top1AvgNameTextView.setText("Burning");
                        top1AvgPropTextView.setText(" ");
                        top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " Cal");
                        top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " Cal");
                    }
                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                    top1ImageView.setImageResource(R.drawable.ic_googlefit);
                    String capitalized = summary.getIndepVar().substring(0, 1)
                            .toUpperCase() + summary.getIndepVar()
                            .substring(1, summary.getIndepVar().length());
                    top1AvgNameTextView.setText(capitalized);
                    if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                        //top1AvgNameTextView.setText("Moving");
                        top1AvgNameTextView.setText("Travelling");
                        top1AvgPropTextView.setText(" ");
                        top1AvgTextView.setText(String.format(Locale.US, "%.2f", summary.getAverageActivityLevel() / 1000) + " km");
                        top1RecTextView.setText(String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel() / 1000) + " km");
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                        top1AvgNameTextView.setText("Walking up");
                        top1AvgPropTextView.setText(" ");
                        top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " floors");
                        top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " floors");
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                        top1AvgNameTextView.setText("Taking");
                        top1AvgPropTextView.setText(" ");
                        top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " steps");
                        top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " steps");
                    } else if (summary.getIndepVar().contains("active")) {
                        top1AvgNameTextView.setText("Staying active");
                        top1AvgPropTextView.setText(" for ");
                        top1AvgTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                        top1RecTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                    } else if (summary.getIndepVar().contains("sleep")) {
                        top1AvgNameTextView.setText("Sleeping");
                        top1AvgPropTextView.setText(" for ");
                        top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins");
                        top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins");
                    } else if (summary.getIndepVar().contains("sedentary")) {
                        top1AvgNameTextView.setText("Staying sedentary");
                        top1AvgPropTextView.setText(" for ");
                        top1AvgTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                        top1RecTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                    } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                        top1AvgNameTextView.setText("Burning");
                        top1AvgPropTextView.setText(" ");
                        top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " Cal");
                        top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " Cal");
                    }
                }
            } else if (item.data.size() == 2) {
                titleTextView.setText("Top 2 Factors");
                for (int i = 0; i < 2; i++) {
                    SimpleRegressionSummary summary = item.data.get(i);
                    Log.i(TAG, summary.getDepXIndepVars()
                            + ": " + summary.getCoefOfDetermination()
                            + " - " + summary.getRecommendedActivityLevel());
                    if (i == 0) {
                        top1Layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i(TAG, "Top 1 layout clicked");
                                int drawable = R.drawable.ic_appusage;
                                if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                                    drawable = R.drawable.ic_appusage;
                                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                                    drawable = R.drawable.ic_fitbit_logo;
                                }
                                if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                                    drawable = R.drawable.ic_googlefit;
                                }
                                new MaterialDialog.Builder(itemView.getContext())
                                        .title(summary.getIndepVarType())
                                        .content("Do you want to track the recommended goal?")
                                        .positiveText("Yes")
                                        .negativeText("No")
                                        .limitIconToDefaultSize()
                                        .iconRes(drawable)
                                        .stackingBehavior(StackingBehavior.ADAPTIVE)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
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
                            top1ArrowImageView.setImageResource(R.drawable.ic_arrow_upward_black);
                            ImageViewCompat.setImageTintList(top1ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary)));
                        } else {
                            top1ArrowImageView.setImageResource(R.drawable.ic_arrow_downward_black);
                            ImageViewCompat.setImageTintList(top1ArrowImageView, ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.fitbit_red)));
                        }
                        if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                            top1ImageView.setImageResource(R.drawable.ic_appusage);
                            if (summary.getIndepVar().equals("Total")) {
                                top1AvgNameTextView.setText("Using phone");
                            } else {
                                top1AvgNameTextView.setText("Using " + summary.getIndepVar());
                            }
                            top1AvgTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                            top1RecTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) + " mins");
                        } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                            top1ImageView.setImageResource(R.drawable.ic_fitbit_logo);
                            if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                                //top1AvgNameTextView.setText("Moving");
                                top1AvgNameTextView.setText("Travelling");
                                top1AvgPropTextView.setText(" ");
                                top1AvgTextView.setText(String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " km");
                                top1RecTextView.setText(String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " km");
                            } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                                top1AvgNameTextView.setText("Walking up");
                                top1AvgPropTextView.setText(" ");
                                top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " floors");
                                top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " floors");
                            } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                                top1AvgNameTextView.setText("Taking");
                                top1AvgPropTextView.setText(" ");
                                top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " steps");
                                top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " steps");
                            } else if (summary.getIndepVar().contains("active")) {
                                top1AvgNameTextView.setText("Staying active");
                                top1AvgPropTextView.setText(" for ");
                                top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins");
                                top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins");
                            } else if (summary.getIndepVar().contains("sleep")) {
                                top1AvgNameTextView.setText("Sleeping");
                                top1AvgPropTextView.setText(" for ");
                                top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins");
                                top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins");
                            } else if (summary.getIndepVar().contains("sedentary")) {
                                top1AvgNameTextView.setText("Staying sedentary");
                                top1AvgPropTextView.setText(" for ");
                                top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins");
                                top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins");
                            } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                                top1AvgNameTextView.setText("Burning");
                                top1AvgPropTextView.setText(" ");
                                top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " Cal");
                                top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " Cal");
                            }
                        } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                            top1ImageView.setImageResource(R.drawable.ic_googlefit);
                            if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                                //top1AvgNameTextView.setText("Moving");
                                top1AvgNameTextView.setText("Travelling");
                                top1AvgPropTextView.setText(" ");
                                top1AvgTextView.setText(String.format(Locale.US, "%.2f", summary.getAverageActivityLevel() / 1000) + " km");
                                top1RecTextView.setText(String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel() / 1000) + " km");
                            } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                                top1AvgNameTextView.setText("Walking up");
                                top1AvgPropTextView.setText(" ");
                                top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " floors");
                                top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " floors");
                            } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                                top1AvgNameTextView.setText("Taking");
                                top1AvgPropTextView.setText(" ");
                                top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " steps");
                                top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " steps");
                            } else if (summary.getIndepVar().contains("active")) {
                                top1AvgNameTextView.setText("Staying active");
                                top1AvgPropTextView.setText(" for ");
                                top1AvgTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                                top1RecTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                            } else if (summary.getIndepVar().contains("sleep")) {
                                top1AvgNameTextView.setText("Sleeping");
                                top1AvgPropTextView.setText(" for ");
                                top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins");
                                top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins");
                            } else if (summary.getIndepVar().contains("sedentary")) {
                                top1AvgNameTextView.setText("Staying sedentary");
                                top1AvgPropTextView.setText(" for ");
                                top1AvgTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                                top1RecTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                            } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                                top1AvgNameTextView.setText("Burning");
                                top1AvgPropTextView.setText(" ");
                                top1AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " Cal");
                                top1RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " Cal");
                            }
                        }
                    } else {
                        top2Layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i(TAG, "Top 1 layout clicked");
                                int drawable = R.drawable.ic_appusage;
                                if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                                    drawable = R.drawable.ic_appusage;
                                } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                                    drawable = R.drawable.ic_fitbit_logo;
                                }
                                if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                                    drawable = R.drawable.ic_googlefit;
                                }
                                new MaterialDialog.Builder(itemView.getContext())
                                        .title(summary.getIndepVarType())
                                        .content("Do you want to track the recommended goal?")
                                        .positiveText("Yes")
                                        .negativeText("No")
                                        .limitIconToDefaultSize()
                                        .iconRes(drawable)
                                        .stackingBehavior(StackingBehavior.ADAPTIVE)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
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
                        if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                            top2ImageView.setImageResource(R.drawable.ic_appusage);
                            if (summary.getIndepVar().equals("Total")) {
                                top2AvgNameTextView.setText("Using phone");
                            } else {
                                top2AvgNameTextView.setText("Using " + summary.getIndepVar());
                            }
                            top2AvgTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                            top2RecTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) + " mins");
                        } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                            top2ImageView.setImageResource(R.drawable.ic_fitbit_logo);
                            if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                                //top1AvgNameTextView.setText("Moving");
                                top2AvgNameTextView.setText("Travelling");
                                top2AvgPropTextView.setText(" ");
                                top2AvgTextView.setText(String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " km");
                                top2RecTextView.setText(String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " km");
                            } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                                top2AvgNameTextView.setText("Walking up");
                                top2AvgPropTextView.setText(" ");
                                top2AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " floors");
                                top2RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " floors");
                            } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                                top2AvgNameTextView.setText("Taking");
                                top2AvgPropTextView.setText(" ");
                                top2AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " steps");
                                top2RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " steps");
                            } else if (summary.getIndepVar().contains("active")) {
                                top2AvgNameTextView.setText("Staying active");
                                top2AvgPropTextView.setText(" for ");
                                top2AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins");
                                top2RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins");
                            } else if (summary.getIndepVar().contains("sleep")) {
                                top2AvgNameTextView.setText("Sleeping");
                                top2AvgPropTextView.setText(" for ");
                                top2AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins");
                                top2RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins");
                            } else if (summary.getIndepVar().contains("sedentary")) {
                                top2AvgNameTextView.setText("Staying sedentary");
                                top2AvgPropTextView.setText(" for ");
                                top2AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins");
                                top2RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins");
                            } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                                top2AvgNameTextView.setText("Burning");
                                top2AvgPropTextView.setText(" ");
                                top2AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " Cal");
                                top2RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " Cal");
                            }
                        } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                            top2ImageView.setImageResource(R.drawable.ic_googlefit);
                            if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                                //top1AvgNameTextView.setText("Moving");
                                top2AvgNameTextView.setText("Travelling");
                                top2AvgPropTextView.setText(" ");
                                top2AvgTextView.setText(String.format(Locale.US, "%.2f", summary.getAverageActivityLevel() / 1000) + " km");
                                top2RecTextView.setText(String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel() / 1000) + " km");
                            } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                                top2AvgNameTextView.setText("Walking up");
                                top2AvgPropTextView.setText(" ");
                                top2AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " floors");
                                top2RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " floors");
                            } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                                top2AvgNameTextView.setText("Taking");
                                top2AvgPropTextView.setText(" ");
                                top2AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " steps");
                                top2RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " steps");
                            } else if (summary.getIndepVar().contains("active")) {
                                top2AvgNameTextView.setText("Staying active");
                                top2AvgPropTextView.setText(" for ");
                                top2AvgTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                                top2RecTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                            } else if (summary.getIndepVar().contains("sleep")) {
                                top2AvgNameTextView.setText("Sleeping");
                                top2AvgPropTextView.setText(" for ");
                                top2AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins");
                                top2RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins");
                            } else if (summary.getIndepVar().contains("sedentary")) {
                                top2AvgNameTextView.setText("Staying sedentary");
                                top2AvgPropTextView.setText(" for ");
                                top2AvgTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                                top2RecTextView.setText(TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins");
                            } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                                top2AvgNameTextView.setText("Burning");
                                top2AvgPropTextView.setText(" ");
                                top2AvgTextView.setText(String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " Cal");
                                top2RecTextView.setText(String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " Cal");
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    SimpleRegressionSummary summary = item.data.get(i);
                    Log.i(TAG, summary.getDepXIndepVars()
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
                        drawable = R.drawable.ic_logo_monogram_colored;
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
                                    Log.i(TAG, userGoal.getGoalName() + ", " + userGoal.getGoalType() + ", " + userGoal.getGoal());
                                    Log.i(TAG, summary.getIndepVar() + ", " + summary.getIndepVarType() + ", " + summary.getRecommendedActivityLevel());
                                    if (userGoal.getGoalName().equals(summary.getIndepVar()) &&
                                            userGoal.getGoalType().equals(summary.getIndepVarType())
                                            && userGoal.getGoal().equals(summary.getRecommendedActivityLevel())) {
                                        top1CheckBox.setChecked(true);
                                    } else {
                                        top1CheckBox.setChecked(false);
                                    }
                                } else if (index == 1) {
                                    Log.i(TAG, userGoal.getGoalName() + ", " + userGoal.getGoalType() + ", " + userGoal.getGoal());
                                    Log.i(TAG, summary.getIndepVar() + ", " + summary.getIndepVarType() + ", " + summary.getRecommendedActivityLevel());
                                    if (userGoal.getGoalName().equals(summary.getIndepVar()) &&
                                            userGoal.getGoalType().equals(summary.getIndepVarType())
                                            && userGoal.getGoal().equals(summary.getRecommendedActivityLevel())) {
                                        top2CheckBox.setChecked(true);
                                    } else {
                                        top2CheckBox.setChecked(false);
                                    }
                                } else {
                                    Log.i(TAG, userGoal.getGoalName() + ", " + userGoal.getGoalType() + ", " + userGoal.getGoal());
                                    Log.i(TAG, summary.getIndepVar() + ", " + summary.getIndepVarType() + ", " + summary.getRecommendedActivityLevel());
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
                        if (summary.getIndepVar().equals("Total")) {
                            indepVarAction = "Using phone";
                        } else {
                            indepVarAction = "Using " + summary.getIndepVar();
                        }
                        avgProposition = " for ";
                        averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins";
                        recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) + " mins";
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                        //top1ImageView.setImageResource(R.drawable.ic_fitbit_logo);
                        if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                            //top1AvgNameTextView.setText("Moving");
                            indepVarAction = "Travelling";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " km";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " km";
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                            indepVarAction = "Walking up";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " floors";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " floors";
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                            indepVarAction = "Taking";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " steps";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " steps";
                        } else if (summary.getIndepVar().contains("active")) {
                            indepVarAction = "Staying active";
                            avgProposition = " for ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins";
                        } else if (summary.getIndepVar().contains("sleep")) {
                            indepVarAction = "Sleeping";
                            avgProposition = " for ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins";
                        } else if (summary.getIndepVar().contains("sedentary")) {
                            indepVarAction = "Staying sedentary";
                            avgProposition = " for ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins";
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                            indepVarAction = "Burning";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " Cal";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " Cal";
                        }
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.googlefit_camel_case)) || summary.getIndepVarType().equals(itemView.getContext().getString(R.string.moabi_tracker_camel_case))) {
                        //top1ImageView.setImageResource(R.drawable.ic_googlefit);
                        if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_distance_camel_case))) {
                            //top1AvgNameTextView.setText("Moving");
                            indepVarAction = "Travelling";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel() / 1000) + " km";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel() / 1000) + " km";
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_floors_camel_case))) {
                            indepVarAction = "Walking up";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " floors";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " floors";
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_steps_camel_case))) {
                            indepVarAction = "Taking";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " steps";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " steps";
                        } else if (summary.getIndepVar().contains("active")) {
                            indepVarAction = "Staying active";
                            avgProposition = " for ";
                            averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins";
                            recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) + " mins";
                        } else if (summary.getIndepVar().contains("sleep")) {
                            indepVarAction = "Sleeping";
                            avgProposition = " for ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " mins";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " mins";
                        } else if (summary.getIndepVar().contains("sedentary")) {
                            indepVarAction = "Staying sedentary";
                            avgProposition = " for ";
                            averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins";
                            recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) + " mins";
                        } else if (summary.getIndepVar().equals(itemView.getContext().getString(R.string.activity_calories_camel_case))) {
                            indepVarAction = "Burning";
                            avgProposition = " ";
                            averageIndepVarLevel = String.format(Locale.US, "%.0f", summary.getAverageActivityLevel()) + " Cal";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.0f", summary.getRecommendedActivityLevel()) + " Cal";
                        }
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.weather_camel_case))) {
                        if (summary.getIndepVar().contains("humidity")) {
                            indepVarAction = "Humidity";
                            avgProposition = " of ";
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " %";
                            recommendedIndepVarLevel = summary.getRecommendedActivityLevel() + " %";
                        } else if (summary.getIndepVar().contains("temperature")) {
                            indepVarAction = "Temperature";
                            avgProposition = " of ";
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " °C";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " °C";
                        }
                        if (summary.getIndepVar().contains("precipitation")) {
                            indepVarAction = "Precipitation";
                            avgProposition = " of ";
                            averageIndepVarLevel = String.format(Locale.US, "%.2f", summary.getAverageActivityLevel()) + " mm";
                            recommendedIndepVarLevel = String.format(Locale.US, "%.2f", summary.getRecommendedActivityLevel()) + " mm";
                        }
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.timer_camel_case))) {
                        indepVarAction = summary.getIndepVar();
                        avgProposition = " for ";
                        averageIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getAverageActivityLevel().longValue()) + " mins";
                        recommendedIndepVarLevel = TimeUnit.MILLISECONDS.toMinutes(summary.getRecommendedActivityLevel().longValue()) + " mins";
                    } else if (summary.getIndepVarType().equals(itemView.getContext().getString(R.string.baactivity_camel_case))) {
                        indepVarAction = summary.getIndepVar();
                        avgProposition = " ";
                        averageIndepVarLevel = summary.getAverageActivityLevel() + " times";
                        recommendedIndepVarLevel = summary.getRecommendedActivityLevel() + " times";
                    }
                    if (i == 0) {
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

