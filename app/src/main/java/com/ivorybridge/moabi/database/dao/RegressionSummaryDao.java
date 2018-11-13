package com.ivorybridge.moabi.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ivorybridge.moabi.database.entity.stats.RegressionSummary;
import com.ivorybridge.moabi.database.entity.stats.SimpleRegressionSummary;

import java.util.List;

@Dao
public interface RegressionSummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RegressionSummary regressionSummary);

    /*
    @Query("DELETE FROM regression_summary_table")
    void deleteAll();

    @Query("SELECT * from regression_summary_table")
    LiveData<List<RegressionSummary>> getAll();

    @Query("SELECT * from regression_summary_table WHERE type = 0")
    LiveData<List<RegressionSummary>> getAllMindSummaries();

    @Query("SELECT * from regression_summary_table WHERE type = 1")
    LiveData<List<RegressionSummary>> getAllBodySummaries();

    @Query("SELECT * from regression_summary_table WHERE type = 0 AND dateInLong BETWEEN :start AND :end")
    LiveData<List<RegressionSummary>> getAllMindSummaries(Long start, Long end);

    @Query("SELECT * from regression_summary_table WHERE type = 0 AND lower(date) = lower(:date)")
    LiveData<List<RegressionSummary>> getAllMindSummaries(String date);

    @Query("SELECT * from regression_summary_table WHERE type = 1 AND dateInLong BETWEEN :start AND :end")
    LiveData<List<RegressionSummary>> getAllBodySummaries(Long start, Long end);

    @Query("SELECT * from regression_summary_table WHERE type = 1 AND lower(date) = lower(:date)")
    LiveData<List<RegressionSummary>> getAllBodySummaries(String date);

    @Query("SELECT * FROM regression_summary_table WHERE lower(date) = lower(:date) limit 1")
    LiveData<RegressionSummary> get(String date);

    @Query("SELECT * FROM regression_summary_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<RegressionSummary>> getAll(Long start, Long end);

    @Query("SELECT * FROM regression_summary_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<RegressionSummary>> getAllByTimeOfEntry(Long start, Long end);*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SimpleRegressionSummary simpleRegressionSummary);

    @Query("DELETE FROM simple_regression_summary_table")
    void deleteAll();

    @Query("SELECT * from simple_regression_summary_table")
    LiveData<List<SimpleRegressionSummary>> getAll();

    @Query("SELECT * from simple_regression_summary_table WHERE depVarType = 0")
    LiveData<List<SimpleRegressionSummary>> getAllMindSummaries();

    @Query("SELECT * from simple_regression_summary_table WHERE depVarType = 1")
    LiveData<List<SimpleRegressionSummary>> getAllBodySummaries();

    @Query("SELECT * from simple_regression_summary_table WHERE depVarType = 0 AND duration = :duration AND dateInLong BETWEEN :start AND :end")
    LiveData<List<SimpleRegressionSummary>> getAllMindSummaries(Long start, Long end, int duration);

    @Query("SELECT * from simple_regression_summary_table WHERE depVarType = 0 AND duration = :duration AND dateInLong BETWEEN :start AND :end")
    List<SimpleRegressionSummary> getAllMindSummariesNow(Long start, Long end, int duration);

    @Query("SELECT * from simple_regression_summary_table WHERE depVarType = 0 AND lower(date) = lower(:date)")
    LiveData<List<SimpleRegressionSummary>> getAllMindSummaries(String date);

    @Query("SELECT * from simple_regression_summary_table WHERE depVarType = 1 AND duration = :duration AND dateInLong BETWEEN :start AND :end")
    LiveData<List<SimpleRegressionSummary>> getAllBodySummaries(Long start, Long end, int duration);

    @Query("SELECT * from simple_regression_summary_table WHERE depVarType = 1 AND duration = :duration AND dateInLong BETWEEN :start AND :end")
    List<SimpleRegressionSummary> getAllBodySummariesNow(Long start, Long end, int duration);

    @Query("SELECT * from simple_regression_summary_table WHERE depVarType = 1 AND lower(date) = lower(:date)")
    LiveData<List<SimpleRegressionSummary>> getAllBodySummaries(String date);

    @Query("SELECT * FROM simple_regression_summary_table WHERE lower(date) = lower(:date) limit 1")
    LiveData<SimpleRegressionSummary> get(String date);

    @Query("SELECT * FROM simple_regression_summary_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<SimpleRegressionSummary>> getAll(Long start, Long end);

    @Query("SELECT * FROM simple_regression_summary_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<SimpleRegressionSummary>> getAllByTimeOfEntry(Long start, Long end);
}
