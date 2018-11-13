package com.ivorybridge.moabi.database.dao;

import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInProfile;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface BuiltInFitnessDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BuiltInActivitySummary builtInActivitySummary);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BuiltInProfile builtInProfile);

    @Query("DELETE FROM built_in_activity_summary_table")
    void deleteAll();

    @Query("SELECT * from built_in_activity_summary_table")
    LiveData<List<BuiltInActivitySummary>> getAll();

    @Query("SELECT * FROM built_in_activity_summary_table WHERE lower(date) = lower(:date) limit 1")
    LiveData<BuiltInActivitySummary> get(String date);

    @Query("SELECT * FROM built_in_activity_summary_table WHERE lower(date) = lower(:date) limit 1")
    BuiltInActivitySummary getNow(String date);

    @Query("SELECT * FROM built_in_activity_summary_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<BuiltInActivitySummary>> getAll(Long start, Long end);

    @Query("SELECT * FROM built_in_activity_summary_table WHERE dateInLong BETWEEN :start AND :end")
    List<BuiltInActivitySummary> getAllNow(Long start, Long end);

    @Query("SELECT * FROM built_in_activity_summary_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<BuiltInActivitySummary>> getAllByTimeOfEntry(Long start, Long end);

    @Query("SELECT * from built_in_profile_table")
    LiveData<List<BuiltInProfile>> getProfile();

    @Query("SELECT * from built_in_profile_table")
    List<BuiltInProfile> getProfileNow();

    @Query("DELETE FROM built_in_profile_table")
    void deleteAllUserProfiles();
}
