package com.ivorybridge.moabi.ui.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class AppUsageXAxisLabelFormatter implements IAxisValueFormatter {

    private final String[] mLabels;

    public AppUsageXAxisLabelFormatter(String[] lables) {
        this.mLabels = lables;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return null;
    }
}
