package com.ivorybridge.moabi.database.dao;


import com.ivorybridge.moabi.database.entity.stress.DailyStress;
import com.ivorybridge.moabi.database.entity.stress.MonthlyStress;
import com.ivorybridge.moabi.database.entity.stress.Stress;
import com.ivorybridge.moabi.database.entity.stress.WeeklyStress;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface StressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Stress stress);

    @Query("DELETE FROM stress_table")
    void deleteAll();

    @Query("SELECT * FROM stress_table WHERE dateInLong = :dateInLong limit 1")
    LiveData<Stress> get(Long dateInLong);

    @Query("SELECT * from stress_table")
    LiveData<List<Stress>> getAll();

    @Query("SELECT * FROM stress_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<Stress>> getAll(Long start, Long end);

    @Query("SELECT * FROM stress_table WHERE dateInLong BETWEEN :start AND :end")
    List<Stress> getAllNow(Long start, Long end);

    @Query("SELECT * FROM stress_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<Stress>> getAllByTimeOfEntry(Long start, Long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyStress dailyStress);

    @Query("DELETE FROM daily_stress_table")
    void deleteAllDailyEntries();

    @Query("SELECT * FROM daily_stress_table WHERE date = :date limit 1")
    LiveData<DailyStress> getDailyEntry(String date);

    @Query("SELECT * from daily_stress_table")
    LiveData<List<DailyStress>> getAllDailyEntries();

    @Query("SELECT * FROM daily_stress_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<DailyStress>> getAllDailyEntries(Long start, Long end);

    @Query("SELECT * FROM daily_stress_table WHERE dateInLong BETWEEN :start AND :end")
    List<DailyStress> getAllDailyEntriesNow(Long start, Long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeeklyStress weeklyStress);

    @Query("DELETE FROM weekly_stress_table")
    void deleteAllWeeklyEntries();

    @Query("SELECT * FROM weekly_stress_table WHERE YYYYW = :dateInYYYYW limit 1")
    LiveData<WeeklyStress> getWeeklyEntry(String dateInYYYYW);

    @Query("SELECT * from weekly_stress_table")
    LiveData<List<WeeklyStress>> getAllWeeklyEntries();

    @Query("SELECT * FROM weekly_stress_table WHERE startDateInLong BETWEEN :startOfFirstWeek AND :startOfLastWeek")
    LiveData<List<WeeklyStress>> getAllWeeklyEntries(Long startOfFirstWeek, Long startOfLastWeek);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MonthlyStress monthlyStress);

    @Query("DELETE FROM monthly_stress_table")
    void deleteAllMonthlyEntries();

    @Query("SELECT * FROM monthly_stress_table WHERE YYYYMM = :dateInYYYYMM limit 1")
    LiveData<MonthlyStress> getMonthlyEntry(String dateInYYYYMM);

    @Query("SELECT * from monthly_stress_table")
    LiveData<List<MonthlyStress>> getAllMonthlyEntries();

    @Query("SELECT * FROM monthly_stress_table WHERE startDateInLong BETWEEN :startOfFirstWeek AND :startOfLastWeek")
    LiveData<List<MonthlyStress>> getAllMonthlyEntries(Long startOfFirstWeek, Long startOfLastWeek);
}
