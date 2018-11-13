package com.ivorybridge.moabi.database.dao;

import com.ivorybridge.moabi.database.entity.anxiety.DailyGad7;
import com.ivorybridge.moabi.database.entity.anxiety.Gad7;
import com.ivorybridge.moabi.database.entity.anxiety.MonthlyGad7;
import com.ivorybridge.moabi.database.entity.anxiety.WeeklyGad7;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface Gad7Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Gad7 score);

    @Query("DELETE FROM gad7_table")
    void deleteAll();

    @Query("SELECT * FROM gad7_table WHERE dateInLong = :dateInLong limit 1")
    LiveData<Gad7> get(Long dateInLong);

    @Query("SELECT * from gad7_table")
    LiveData<List<Gad7>> getAll();

    @Query("SELECT * FROM gad7_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<Gad7>> getAll(Long start, Long end);

    @Query("SELECT * FROM gad7_table WHERE dateInLong BETWEEN :start AND :end")
    List<Gad7> getAllNow(Long start, Long end);

    @Query("SELECT * FROM gad7_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<Gad7>> getAllByTimeOfEntry(Long start, Long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyGad7 dailyGad7);

    @Query("DELETE FROM daily_gad7_table")
    void deleteAllDailyEntries();

    @Query("SELECT * FROM daily_gad7_table WHERE date = :date limit 1")
    LiveData<DailyGad7> getDailyEntry(String date);

    @Query("SELECT * from daily_gad7_table")
    LiveData<List<DailyGad7>> getAllDailyEntries();

    @Query("SELECT * FROM daily_gad7_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<DailyGad7>> getAllDailyEntries(Long start, Long end);

    @Query("SELECT * FROM daily_gad7_table WHERE dateInLong BETWEEN :start AND :end")
    List<DailyGad7> getAllDailyEntriesNow(Long start, Long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeeklyGad7 weeklyGad7);

    @Query("DELETE FROM weekly_gad7_table")
    void deleteAllWeeklyEntries();

    @Query("SELECT * FROM weekly_gad7_table WHERE YYYYW = :dateInYYYYW limit 1")
    LiveData<WeeklyGad7> getWeeklyEntry(String dateInYYYYW);

    @Query("SELECT * from weekly_gad7_table")
    LiveData<List<WeeklyGad7>> getAllWeeklyEntries();

    @Query("SELECT * FROM weekly_gad7_table WHERE startDateInLong BETWEEN :startOfFirstWeek AND :startOfLastWeek")
    LiveData<List<WeeklyGad7>> getAllWeeklyEntries(Long startOfFirstWeek, Long startOfLastWeek);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MonthlyGad7 monthlyGad7);

    @Query("DELETE FROM monthly_gad7_table")
    void deleteAllMonthlyEntries();

    @Query("SELECT * FROM monthly_gad7_table WHERE YYYYMM = :dateInYYYYMM limit 1")
    LiveData<MonthlyGad7> getMonthlyEntry(String dateInYYYYMM);

    @Query("SELECT * from monthly_gad7_table")
    LiveData<List<MonthlyGad7>> getAllMonthlyEntries();

    @Query("SELECT * FROM monthly_gad7_table WHERE startDateInLong BETWEEN :startOfFirstWeek AND :startOfLastWeek")
    LiveData<List<MonthlyGad7>> getAllMonthlyEntries(Long startOfFirstWeek, Long startOfLastWeek);
}
