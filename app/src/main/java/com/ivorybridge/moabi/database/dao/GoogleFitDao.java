package com.ivorybridge.moabi.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ivorybridge.moabi.database.entity.googlefit.GoogleFitSummary;

import java.util.List;

@Dao
public interface GoogleFitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GoogleFitSummary googleFitSummary);

    @Query("DELETE FROM google_fit_summary_table")
    void deleteAll();

    @Query("SELECT * FROM google_fit_summary_table WHERE lower(date) = lower(:date) limit 1")
    LiveData<GoogleFitSummary> get(String date);

    @Query("SELECT * from google_fit_summary_table")
    LiveData<List<GoogleFitSummary>> getAll();

    @Query("SELECT * FROM google_fit_summary_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<GoogleFitSummary>> getAll(Long start, Long end);

    @Query("SELECT * FROM google_fit_summary_table WHERE dateInLong BETWEEN :start AND :end")
    List<GoogleFitSummary> getAllNow(Long start, Long end);

    @Query("SELECT * FROM google_fit_summary_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<GoogleFitSummary>> getAllByTimeOfEntry(Long start, Long end);

    @Update
    void update(GoogleFitSummary googleFitSummary);

    @Update
    void update(GoogleFitSummary... dailySummaries);
}
