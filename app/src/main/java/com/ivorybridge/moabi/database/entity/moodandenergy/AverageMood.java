package com.ivorybridge.moabi.database.entity.moodandenergy;

public class AverageMood {

    private Double averageMood;
    private Double averageEnergyLevel;

    public AverageMood() {
    }

    public AverageMood(Double am, Double ael) {
        this.averageMood = am;
        this.averageEnergyLevel = ael;
    }

    public Double getAverageMood() {
        return averageMood;
    }

    public Double getAverageEnergyLevel() {
        return averageEnergyLevel;
    }
}
