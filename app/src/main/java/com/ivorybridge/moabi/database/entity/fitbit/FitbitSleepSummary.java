package com.ivorybridge.moabi.database.entity.fitbit;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.annotation.Keep;

@Keep
@Entity(foreignKeys = @ForeignKey(entity = FitbitDailySummary.class, parentColumns = "date", childColumns = "summary"))
public class FitbitSleepSummary {

    /*
    @ColumnInfo(name="sleep_id")
    public int sleepId;*/
    @Embedded
    public Summary summary;

    public FitbitSleepSummary() {
    }

    @Ignore
    public FitbitSleepSummary(Summary summary) {
        this.summary = summary;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "Sleep: " + getSummary().toString();
    }

    @Keep
    public static class Summary {
        /*
        @ColumnInfo(name="sleep_summary_id")
        public int sleepSummaryId;*/
        @ColumnInfo(name = "total_minutes_asleep")
        public Long totalMinutesAsleep;
        @ColumnInfo(name = "total_sleep_records")
        public Long totalSleepRecords;
        @ColumnInfo(name = "total_time_in_bed")
        public Long totalTimeInBed;

        public Summary() {

        }

        @Ignore
        public Summary(Long totalMinutesAsleep, Long totalSleepRecords, Long totalTimeInBed) {
            this.totalMinutesAsleep = totalMinutesAsleep;
            this.totalSleepRecords = totalSleepRecords;
            this.totalTimeInBed = totalTimeInBed;
        }

        public Long getTotalMinutesAsleep() {
            return totalMinutesAsleep;
        }

        public void setTotalMinutesAsleep(Long totalMinutesAsleep) {
            this.totalMinutesAsleep = totalMinutesAsleep;
        }

        public Long getTotalSleepRecords() {
            return totalSleepRecords;
        }

        public void setTotalSleepRecords(Long totalSleepRecords) {
            this.totalSleepRecords = totalSleepRecords;
        }

        public Long getTotalTimeInBed() {
            return totalTimeInBed;
        }

        public void setTotalTimeInBed(Long totalTimeInBed) {
            this.totalTimeInBed = totalTimeInBed;
        }

        @Override
        public String toString() {
            return "totalMinutesAsleep: " + getTotalMinutesAsleep();
        }
    }

}
