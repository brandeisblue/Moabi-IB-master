package com.ivorybridge.moabi.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ivorybridge.moabi.database.entity.fitbit.FitbitDailySummary;

import java.util.List;

@Dao
public interface FitbitDailySummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FitbitDailySummary fitbitDailySummary);

    @Query("DELETE FROM fitbit_daily_summary_table")
    void deleteAll();

    @Query("SELECT * from fitbit_daily_summary_table")
    LiveData<List<FitbitDailySummary>> getAll();

    @Query("SELECT * FROM fitbit_daily_summary_table WHERE lower(date) = lower(:date) limit 1")
    LiveData<FitbitDailySummary> get(String date);

    @Query("SELECT * FROM fitbit_daily_summary_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<FitbitDailySummary>> getAll(Long start, Long end);

    @Query("SELECT * FROM fitbit_daily_summary_table WHERE dateInLong BETWEEN :start AND :end")
    List<FitbitDailySummary> getAllNow(Long start, Long end);

    @Query("SELECT * FROM fitbit_daily_summary_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<FitbitDailySummary>> getAllByTimeOfEntry(Long start, Long end);

    @Update
    void updateDailySummaries(FitbitDailySummary... dailySummaries);
    /*
    @Query("SELECT * from fitbit_activity_summary_table")
    List<FitbitActivitySummaryRoom> getFitbitActivitySummary;*/
}
