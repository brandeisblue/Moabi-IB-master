package com.ivorybridge.moabi.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ivorybridge.moabi.database.entity.baactivity.BAActivityEntry;

import java.util.List;

@Dao
public interface BAActivityEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BAActivityEntry baActivityEntry);

    @Query("DELETE FROM ba_activity_entry_table")
    void deleteAll();

    @Query("DELETE from ba_activity_entry_table WHERE dateInLong = :dateInLong AND lower(name) = lower(:name)")
    void delete(String dateInLong, String name);

    @Query("SELECT * from ba_activity_entry_table")
    LiveData<List<BAActivityEntry>> getAll();

    @Query("SELECT * FROM ba_activity_entry_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<BAActivityEntry>> getAll(Long start, Long end);

    @Query("SELECT * FROM ba_activity_entry_table WHERE dateInLong BETWEEN :start AND :end")
    List<BAActivityEntry> getAllNow(Long start, Long end);

    @Query("SELECT * FROM ba_activity_entry_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<BAActivityEntry>> getAllByTimeOfEntry(Long start, Long end);

    @Query("SELECT * FROM ba_activity_entry_table WHERE dateInLong BETWEEN :start AND :end AND lower(name) = lower(:name)")
    LiveData<List<BAActivityEntry>> getAll(Long start, Long end, String name);

    @Query("SELECT * FROM ba_activity_entry_table WHERE dateInLong BETWEEN :start AND :end AND activityType = :activityType")
    LiveData<List<BAActivityEntry>> getAll(Long start, Long end, Long activityType);

    @Query("SELECT * FROM ba_activity_entry_table WHERE activityType = :activityType")
    LiveData<List<BAActivityEntry>> getAll(Long activityType);

    @Query("SELECT * FROM ba_activity_entry_table WHERE lower(name) = lower(:name)")
    LiveData<List<BAActivityEntry>> getAll(String name);
}
