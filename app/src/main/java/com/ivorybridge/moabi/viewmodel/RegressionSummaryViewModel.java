package com.ivorybridge.moabi.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;
import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;
import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;
import com.ivorybridge.moabi.database.entity.moodandenergy.AverageMood;
import com.ivorybridge.moabi.database.entity.stats.SimpleRegressionSummary;
import com.ivorybridge.moabi.repository.statistics.PredictionsRepository;

import java.util.List;
import java.util.Map;

public class RegressionSummaryViewModel extends AndroidViewModel {

    private PredictionsRepository mRepository;

    public RegressionSummaryViewModel(Application application) {
        super(application);
        this.mRepository = new PredictionsRepository(application);
    }

    public LiveData<List<SimpleRegressionSummary>> getAllMindSummaries(Long start, Long end, int duration) {
        return mRepository.getAllMindSummaries(start, end, duration);
    }

    public LiveData<List<SimpleRegressionSummary>> getAllBodySummaries(Long start, Long end, int duration) {
        return mRepository.getAllBodySummaries(start, end, duration);
    }

    public void processMoodAndEnergyWithGoogleFit(Map<String, AverageMood> moodsAndEnergyLevelMap, List<GoogleFitSummary> googleFitSummaries, int duration) {
        this.mRepository.processMoodAndEnergyWithGoogleFit(moodsAndEnergyLevelMap, googleFitSummaries, duration);
    }

    public void processData(double depVarForPrediction, long depVarType, String indepVar, String depVar, double[] indepVars, double[] depVars) {
        this.mRepository.processData(depVarForPrediction, depVarType, indepVar, depVar, indepVars, depVars);
    }

    public void processMoodAndEnergyWithFitbit(Map<String, AverageMood> moodsAndEnergyLevelMap, List<FitbitDailySummary> fitbitDailySummaries, int duration) {
        this.mRepository.processMoodAndEnergyWithFitbit(moodsAndEnergyLevelMap, fitbitDailySummaries, duration);
    }

    public void processMoodAndEnergyWithAppUsage(Map<String, AverageMood> moodsAndEnergyLevelMap, List<AppUsageSummary> appUsageSummaries, int duration) {
        this.mRepository.processMoodAndEnergyWithAppUsage(moodsAndEnergyLevelMap, appUsageSummaries, duration);
    }
}
