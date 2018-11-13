package com.ivorybridge.moabi.database.dao;

import com.ivorybridge.moabi.database.entity.appusage.AppUsageSummary;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface AppUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppUsageSummary appUsageSummary);

    @Query("DELETE FROM app_usage_table")
    void deleteAll();

    @Query("SELECT * from app_usage_table")
    LiveData<List<AppUsageSummary>> getAll();

    @Query("SELECT * FROM app_usage_table WHERE lower(date) = lower(:date) limit 1")
    LiveData<AppUsageSummary> get(String date);

    @Query("SELECT * FROM app_usage_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<AppUsageSummary>> getAll(Long start, Long end);

    @Query("SELECT * FROM app_usage_table WHERE dateInLong BETWEEN :start AND :end")
    List<AppUsageSummary> getAllNow(Long start, Long end);

    @Query("SELECT * FROM app_usage_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<AppUsageSummary>> getAllByTimeOfEntry(Long start, Long end);
}
