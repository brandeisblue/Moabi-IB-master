package com.ivorybridge.moabi.database.entity.googlefit;

public class GoogleFitDataTypeClassifier {

    private String dataTypeRaw;

    public GoogleFitDataTypeClassifier(String d) {
        this.dataTypeRaw = d;
    }

    public String getDataType() {
        if (this.dataTypeRaw != null) {
            switch (dataTypeRaw) {
                case "com.google.activity.summary":
                    return "activity";
                case "com.google.activity.segment":
                    return "activity";
                case "com.google.calories.expended":
                    return "calories";
                case "com.google.nutrition.summary":
                    return "nutrition";
                case "com.google.cycling.pedaling.cadence":
                    return "cadence";
                case "com.google.cycling.wheel_revolution.rpm":
                    return "rpm";
                case "com.google.distance.delta":
                    return "distance";
                case "com.google.heart_rate.bpm":
                    return "heartRate";
                case "com.google.height":
                    return "height";
                case "com.google.location.sample":
                    return "location";
                case "com.google.nutrition":
                    return "nutrition";
                case "com.google.power.sample":
                    return "power";
                case "com.google.speed":
                    return "speed";
                case "com.google.step_count.cadence":
                    return "stepsCadence";
                case "com.google.step_count.delta":
                    return "steps";
                case "com.google.weight":
                    return "weight";
                case "com.google.activity.exercise":
                    return "exercise";
                default:
                    return "unknown";
            }
        } else {
            return "unknown";
        }
    }
}

