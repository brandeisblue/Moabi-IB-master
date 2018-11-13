package com.ivorybridge.moabi.database.entity.stats;


import com.ivorybridge.moabi.database.entity.dailyreview.DailyDailyReview;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class InsightSummaryDailyReviewMediatorLiveData extends MediatorLiveData<Pair<List<DailyDailyReview>,
        List<SimpleRegressionSummary>>> {

    public InsightSummaryDailyReviewMediatorLiveData(LiveData<List<DailyDailyReview>> dailyDailyReviews,
                                                     LiveData<List<SimpleRegressionSummary>> simpleRegressionSummaryList) {
        addSource(dailyDailyReviews, new Observer<List<DailyDailyReview>>() {
            @Override
            public void onChanged(@Nullable List<DailyDailyReview> dailyDailyReviewList) {
                setValue(Pair.create(dailyDailyReviewList, simpleRegressionSummaryList.getValue()));
            }
        });
        addSource(simpleRegressionSummaryList, new Observer<List<SimpleRegressionSummary>>() {
            @Override
            public void onChanged(@Nullable List<SimpleRegressionSummary> simpleRegressionSummaries) {
                setValue(Pair.create(dailyDailyReviews.getValue(), simpleRegressionSummaries));
            }
        });
    }
}
