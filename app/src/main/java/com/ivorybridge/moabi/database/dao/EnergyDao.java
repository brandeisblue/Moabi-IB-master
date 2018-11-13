package com.ivorybridge.moabi.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ivorybridge.moabi.database.entity.moodandenergy.DailyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.MonthlyEnergy;
import com.ivorybridge.moabi.database.entity.moodandenergy.WeeklyEnergy;

import java.util.List;

@Dao
public interface EnergyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyEnergy dailyEnergy);

    @Query("DELETE FROM daily_energy_table")
    void deleteAllDailyEntries();

    @Query("SELECT * FROM daily_energy_table WHERE date = :date limit 1")
    LiveData<DailyEnergy> getDailyEntry(String date);

    @Query("SELECT * from daily_energy_table")
    LiveData<List<DailyEnergy>> getAllDailyEntries();

    @Query("SELECT * FROM daily_energy_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<DailyEnergy>> getAllDailyEntries (Long start, Long end);

    @Query("SELECT * FROM daily_energy_table WHERE dateInLong BETWEEN :start AND :end")
    List<DailyEnergy> getAllDailyEntriesNow (Long start, Long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeeklyEnergy weeklyEnergy);

    @Query("DELETE FROM weekly_energy_table")
    void deleteAllWeeklyEntries();

    @Query("SELECT * FROM weekly_energy_table WHERE YYYYW = :dateInYYYYW limit 1")
    LiveData<WeeklyEnergy> getWeeklyEntry(String dateInYYYYW);

    @Query("SELECT * from weekly_energy_table")
    LiveData<List<WeeklyEnergy>> getAllWeeklyEntries();

    @Query("SELECT * FROM weekly_energy_table WHERE startDateInLong BETWEEN :startOfFirstWeek AND :startOfLastWeek")
    LiveData<List<WeeklyEnergy>> getAllWeeklyEntries(Long startOfFirstWeek, Long startOfLastWeek);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MonthlyEnergy monthlyEnergy);

    @Query("DELETE FROM monthly_energy_table")
    void deleteAllMonthlyEntries();

    @Query("SELECT * FROM monthly_energy_table WHERE YYYYMM = :dateInYYYYMM limit 1")
    LiveData<MonthlyEnergy> getMonthlyEntry(String dateInYYYYMM);

    @Query("SELECT * from monthly_energy_table")
    LiveData<List<MonthlyEnergy>> getAllMonthlyEntries();

    @Query("SELECT * FROM monthly_energy_table WHERE startDateInLong BETWEEN :startOfFirstWeek AND :startOfLastWeek")
    LiveData<List<MonthlyEnergy>> getAllMonthlyEntries(Long startOfFirstWeek, Long startOfLastWeek);
}
