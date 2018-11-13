package com.ivorybridge.moabi.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ivorybridge.moabi.database.entity.moodandenergy.DailyMood;
import com.ivorybridge.moabi.database.entity.moodandenergy.MonthlyMood;
import com.ivorybridge.moabi.database.entity.moodandenergy.WeeklyMood;

import java.util.List;

@Dao
public interface MoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyMood dailyMood);

    @Query("DELETE FROM daily_mood_table")
    void deleteAllDailyEntries();

    @Query("SELECT * FROM daily_mood_table WHERE date = :date limit 1")
    LiveData<DailyMood> getDailyEntry(String date);

    @Query("SELECT * from daily_mood_table")
    LiveData<List<DailyMood>> getAllDailyEntries();

    @Query("SELECT * FROM daily_mood_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<DailyMood>> getAllDailyEntries(Long start, Long end);

    @Query("SELECT * FROM daily_mood_table WHERE dateInLong BETWEEN :start AND :end")
    List<DailyMood> getAllDailyEntriesNow(Long start, Long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeeklyMood weeklyMood);

    @Query("DELETE FROM weekly_mood_table")
    void deleteAllWeeklyEntries();

    @Query("SELECT * FROM weekly_mood_table WHERE YYYYW = :dateInYYYYW limit 1")
    LiveData<WeeklyMood> getWeeklyEntry(String dateInYYYYW);

    @Query("SELECT * from weekly_mood_table")
    LiveData<List<WeeklyMood>> getAllWeeklyEntries();

    @Query("SELECT * FROM weekly_mood_table WHERE startDateInLong BETWEEN :startOfFirstWeek AND :startOfLastWeek")
    LiveData<List<WeeklyMood>> getAllWeeklyEntries(Long startOfFirstWeek, Long startOfLastWeek);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MonthlyMood monthlyMood);

    @Query("DELETE FROM monthly_mood_table")
    void deleteAllMonthlyEntries();

    @Query("SELECT * FROM monthly_mood_table WHERE YYYYMM = :dateInYYYYMM limit 1")
    LiveData<MonthlyMood> getMonthlyEntry(String dateInYYYYMM);

    @Query("SELECT * from monthly_mood_table")
    LiveData<List<MonthlyMood>> getAllMonthlyEntries();

    @Query("SELECT * FROM monthly_mood_table WHERE startDateInLong BETWEEN :startOfFirstWeek AND :startOfLastWeek")
    LiveData<List<MonthlyMood>> getAllMonthlyEntries(Long startOfFirstWeek, Long startOfLastWeek);
}
