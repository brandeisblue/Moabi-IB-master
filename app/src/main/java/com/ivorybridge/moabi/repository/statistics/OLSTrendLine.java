package com.ivorybridge.moabi.repository.statistics;

import android.util.Log;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.util.Arrays;

public abstract class OLSTrendLine implements TrendLine {

    private static final String TAG = OLSTrendLine.class.getSimpleName();
    RealMatrix coef = null; // will hold prediction coefs once we get values
    double adjRSquared = 0;
    boolean isNinetyPercentConfident;
    boolean isNinetyFivePercentConfident;
    double standardError;
    double[][] xData;
    double[] yData;
    private double TSS;

    protected abstract double[] xVector(double x); // create vector of values from x
    protected abstract boolean logY(); // set true to predict log of y (note: y must be positive)

    @Override
    public void setValues(double[] y, double[] x) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("The numbers of y and x values must be equal (%d != %d)", y.length, x.length));
        }
        xData = new double[x.length][];
        yData = Arrays.copyOf(y, y.length);
        for (int i = 0; i < x.length; i++) {
            // the implementation determines how to produce a vector of predictors from a single x
            xData[i] = xVector(x[i]);
        }
        if (logY()) { // in some models we are predicting ln y, so we replace each y with ln y
            yData = Arrays.copyOf(y, y.length); // user might not be finished with the array we were given
            for (int i = 0; i < x.length; i++) {
                yData[i] = Math.log(y[i]);
            }
        }
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.setNoIntercept(true); // let the implementation include a constant in xVector if desired
        //ols.newSampleData(yData, xData); // provide the data to the model
        try {
            ols.newSampleData(yData, xData); // provide the data to the model
            Log.i(TAG, Arrays.toString(ols.estimateRegressionParameters()));
            coef = MatrixUtils.createColumnRealMatrix(ols.estimateRegressionParameters()); // get our coefs
            adjRSquared = ols.calculateRSquared();
            standardError = ols.estimateRegressionStandardError();
            TSS = ols.calculateTotalSumOfSquares();
        } catch (MathIllegalArgumentException e) {
            Log.i(TAG, e. getMessage());
            Log.i(TAG, "IO" + e);
        }
    }

    @Override
    public double predict(double x) {
        if (coef == null) {
            return 0.0;
        }
        double yhat = coef.preMultiply(xVector(x))[0]; // apply coefs to xVector
        if (logY()) yhat = (Math.exp(yhat)); // if we predicted ln y, we still need to get y
        return yhat;
    }

    @Override
    public double getAdjRSquared() {
        return adjRSquared;
    }

    @Override
    public double getStandardError() {
        return standardError;
    }

    @Override
    public RealMatrix getCoefficients() {
        return coef;
    }

    public boolean isNinetyPercentConfident() {
        for (int i = 0; i < yData.length; i++) {

        }
        return true;
    }
}

