package com.ivorybridge.moabi.repository.statistics;

import org.apache.commons.math3.linear.RealMatrix;

public interface TrendLine {
    public void setValues(double[] y, double[] x); // y ~ f(x)
    public double predict(double x); // get a predicted y for a given x
    public double getAdjRSquared();
    public double getStandardError();
    public RealMatrix getCoefficients();
    //public double predictX(double y); // get a predicted x for a given x
}