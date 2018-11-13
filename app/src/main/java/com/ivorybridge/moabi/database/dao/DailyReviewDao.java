package com.ivorybridge.moabi.database.dao;


import com.ivorybridge.moabi.database.entity.dailyreview.DailyDailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.MonthlyDailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.DailyReview;
import com.ivorybridge.moabi.database.entity.dailyreview.WeeklyDailyReview;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface DailyReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyReview dailyReview);

    @Query("DELETE FROM daily_review_table")
    void deleteAll();

    @Query("SELECT * FROM daily_review_table WHERE dateInLong = :dateInLong limit 1")
    LiveData<DailyReview> get(Long dateInLong);

    @Query("SELECT * from daily_review_table")
    LiveData<List<DailyReview>> getAll();

    @Query("SELECT * FROM daily_review_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<DailyReview>> getAll(Long start, Long end);

    @Query("SELECT * FROM daily_review_table WHERE dateInLong BETWEEN :start AND :end")
    List<DailyReview> getAllNow(Long start, Long end);

    @Query("SELECT * FROM daily_review_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<DailyReview>> getAllByTimeOfEntry(Long start, Long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyDailyReview dailyDailyReview);

    @Query("DELETE FROM daily_daily_review_table")
    void deleteAllDailyEntries();

    @Query("SELECT * FROM daily_daily_review_table WHERE date = :date limit 1")
    LiveData<DailyDailyReview> getDailyEntry(String date);

    @Query("SELECT * from daily_daily_review_table")
    LiveData<List<DailyDailyReview>> getAllDailyEntries();

    @Query("SELECT * FROM daily_daily_review_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<DailyDailyReview>> getAllDailyEntries(Long start, Long end);

    @Query("SELECT * FROM daily_daily_review_table WHERE dateInLong BETWEEN :start AND :end")
    List<DailyDailyReview> getAllDailyEntriesNow(Long start, Long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeeklyDailyReview weeklyDailyReview);

    @Query("DELETE FROM weekly_daily_review_table")
    void deleteAllWeeklyEntries();

    @Query("SELECT * FROM weekly_daily_review_table WHERE YYYYW = :dateInYYYYW limit 1")
    LiveData<WeeklyDailyReview> getWeeklyEntry(String dateInYYYYW);

    @Query("SELECT * from weekly_daily_review_table")
    LiveData<List<WeeklyDailyReview>> getAllWeeklyEntries();

    @Query("SELECT * FROM weekly_daily_review_table WHERE startDateInLong BETWEEN :startOfFirstWeek AND :startOfLastWeek")
    LiveData<List<WeeklyDailyReview>> getAllWeeklyEntries(Long startOfFirstWeek, Long startOfLastWeek);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MonthlyDailyReview monthlyDailyReview);

    @Query("DELETE FROM monthly_daily_review_table")
    void deleteAllMonthlyEntries();

    @Query("SELECT * FROM monthly_daily_review_table WHERE YYYYMM = :dateInYYYYMM limit 1")
    LiveData<MonthlyDailyReview> getMonthlyEntry(String dateInYYYYMM);

    @Query("SELECT * from monthly_daily_review_table")
    LiveData<List<MonthlyDailyReview>> getAllMonthlyEntries();

    @Query("SELECT * FROM monthly_daily_review_table WHERE startDateInLong BETWEEN :startOfFirstWeek AND :startOfLastWeek")
    LiveData<List<MonthlyDailyReview>> getAllMonthlyEntries(Long startOfFirstWeek, Long startOfLastWeek);
}
