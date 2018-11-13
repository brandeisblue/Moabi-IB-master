package com.ivorybridge.moabi.database.dao;

import com.ivorybridge.moabi.database.entity.timedactivity.TimedActivitySummary;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

// TODO - create an interface for all Daos
@Dao
public interface TimedActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TimedActivitySummary timedActivitySummary);

    @Query("DELETE FROM timed_activity_table")
    void deleteAll();

    @Query("SELECT * FROM timed_activity_table WHERE lower(date) = lower(:date)")
    LiveData<List<TimedActivitySummary>> getAll(String date);

    @Query("SELECT * from timed_activity_table")
    LiveData<List<TimedActivitySummary>> getAll();

    @Query("SELECT date from timed_activity_table")
    LiveData<List<String>> getDates();

    @Query("SELECT * FROM timed_activity_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<TimedActivitySummary>> getAll(Long start, Long end);

    @Query("SELECT * FROM timed_activity_table WHERE dateInLong BETWEEN :start AND :end")
    List<TimedActivitySummary> getAllNow(Long start, Long end);

    @Query("SELECT * FROM timed_activity_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<TimedActivitySummary>> getAllByTimeOfEntry(Long start, Long end);
}
