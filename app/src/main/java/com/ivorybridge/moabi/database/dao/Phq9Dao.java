package com.ivorybridge.moabi.database.dao;


import com.ivorybridge.moabi.database.entity.depression.DailyPhq9;
import com.ivorybridge.moabi.database.entity.depression.MonthlyPhq9;
import com.ivorybridge.moabi.database.entity.depression.Phq9;
import com.ivorybridge.moabi.database.entity.depression.WeeklyPhq9;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface Phq9Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Phq9 score);

    @Query("DELETE FROM phq9_table")
    void deleteAll();

    @Query("SELECT * FROM phq9_table WHERE dateInLong = :dateInLong limit 1")
    LiveData<Phq9> get(Long dateInLong);

    @Query("SELECT * from phq9_table")
    LiveData<List<Phq9>> getAll();

    @Query("SELECT * FROM phq9_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<Phq9>> getAll(Long start, Long end);

    @Query("SELECT * FROM phq9_table WHERE dateInLong BETWEEN :start AND :end")
    List<Phq9> getAllNow(Long start, Long end);

    @Query("SELECT * FROM phq9_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<Phq9>> getAllByTimeOfEntry(Long start, Long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyPhq9 dailyPhq9);

    @Query("DELETE FROM daily_phq9_table")
    void deleteAllDailyEntries();

    @Query("SELECT * FROM daily_phq9_table WHERE date = :date limit 1")
    LiveData<DailyPhq9> getDailyEntry(String date);

    @Query("SELECT * from daily_phq9_table")
    LiveData<List<DailyPhq9>> getAllDailyEntries();

    @Query("SELECT * FROM daily_phq9_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<DailyPhq9>> getAllDailyEntries(Long start, Long end);

    @Query("SELECT * FROM daily_phq9_table WHERE dateInLong BETWEEN :start AND :end")
    List<DailyPhq9> getAllDailyEntriesNow(Long start, Long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeeklyPhq9 weeklyPhq9);

    @Query("DELETE FROM weekly_phq9_table")
    void deleteAllWeeklyEntries();

    @Query("SELECT * FROM weekly_phq9_table WHERE YYYYW = :dateInYYYYW limit 1")
    LiveData<WeeklyPhq9> getWeeklyEntry(String dateInYYYYW);

    @Query("SELECT * from weekly_phq9_table")
    LiveData<List<WeeklyPhq9>> getAllWeeklyEntries();

    @Query("SELECT * FROM weekly_phq9_table WHERE startDateInLong BETWEEN :startOfFirstWeek AND :startOfLastWeek")
    LiveData<List<WeeklyPhq9>> getAllWeeklyEntries(Long startOfFirstWeek, Long startOfLastWeek);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MonthlyPhq9 monthlyPhq9);

    @Query("DELETE FROM monthly_phq9_table")
    void deleteAllMonthlyEntries();

    @Query("SELECT * FROM monthly_phq9_table WHERE YYYYMM = :dateInYYYYMM limit 1")
    LiveData<MonthlyPhq9> getMonthlyEntry(String dateInYYYYMM);

    @Query("SELECT * from monthly_phq9_table")
    LiveData<List<MonthlyPhq9>> getAllMonthlyEntries();

    @Query("SELECT * FROM monthly_phq9_table WHERE startDateInLong BETWEEN :startOfFirstWeek AND :startOfLastWeek")
    LiveData<List<MonthlyPhq9>> getAllMonthlyEntries(Long startOfFirstWeek, Long startOfLastWeek);
}
