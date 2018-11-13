package com.ivorybridge.moabi.database.entity.stats;

public class ActivitySummary {

    public String name;
    public Double value;

    public ActivitySummary() {

    }

    public ActivitySummary(String n, Double v) {
        this.name = n;
        this.value = v;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "[" + this.name + ": " + this.value + "]";
    }
}
