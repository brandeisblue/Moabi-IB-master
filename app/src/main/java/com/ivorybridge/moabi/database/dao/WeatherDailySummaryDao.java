package com.ivorybridge.moabi.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ivorybridge.moabi.database.entity.weather.WeatherDailySummary;
import java.util.List;

@Dao
public interface WeatherDailySummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeatherDailySummary weatherDailySummary);

    @Query("DELETE FROM weather_daily_summary_table")
    void deleteAll();

    @Query("SELECT * from weather_daily_summary_table")
    LiveData<List<WeatherDailySummary>> getAll();

    @Query("SELECT * FROM weather_daily_summary_table WHERE lower(date) = lower(:date) limit 1")
    LiveData<WeatherDailySummary> get(String date);

    @Query("SELECT * FROM weather_daily_summary_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<WeatherDailySummary>> getAll(Long start, Long end);

    @Query("SELECT * FROM weather_daily_summary_table WHERE dateInLong BETWEEN :start AND :end")
    List<WeatherDailySummary> getAllNow(Long start, Long end);
}
