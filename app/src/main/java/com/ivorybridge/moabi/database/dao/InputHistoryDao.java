package com.ivorybridge.moabi.database.dao;

import com.ivorybridge.moabi.database.entity.util.InputDate;
import com.ivorybridge.moabi.database.entity.util.InputHistory;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface InputHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInputHistory(InputHistory inputHistory);

    @Query("DELETE FROM input_history_table")
    void deleteAllInputHistory();

    @Query("SELECT * FROM input_history_table WHERE lower(date) = lower(:date)")
    LiveData<List<InputHistory>> getAll(String date);

    @Query("SELECT * from input_history_table")
    LiveData<List<InputHistory>> getAll();

    @Query("SELECT date from input_history_table")
    LiveData<List<String>> getDates();

    @Query("SELECT inputType FROM input_history_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<String>> getAllInputTypesByTimeOfEntry(Long start, Long end);

    @Query("SELECT inputType FROM input_history_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<String>> getAllInputTypesByDate(Long start, Long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInputDate(InputDate inputDate);

    @Query("DELETE FROM input_date_table")
    void deleteAllInputDates();

    @Query("SELECT * from input_date_table")
    LiveData<List<InputDate>> getInputDates();

    @Query("SELECT * from input_date_table")
    List<InputDate> getInputDatesNow();
}
