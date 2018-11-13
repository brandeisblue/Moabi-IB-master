package com.ivorybridge.moabi.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ivorybridge.moabi.database.entity.moodandenergy.MoodAndEnergy;

import java.util.List;

@Dao
public interface MoodAndEnergyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MoodAndEnergy moodAndEnergy);

    @Query("DELETE FROM mood_and_energy_entry_table")
    void deleteAll();

    @Query("SELECT * FROM mood_and_energy_entry_table WHERE dateInLong = :dateInLong limit 1")
    LiveData<MoodAndEnergy> get(Long dateInLong);

    @Query("SELECT * from mood_and_energy_entry_table")
    LiveData<List<MoodAndEnergy>> getAll();

    @Query("SELECT * FROM mood_and_energy_entry_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<MoodAndEnergy>> getAll(Long start, Long end);

    @Query("SELECT * FROM mood_and_energy_entry_table WHERE dateInLong BETWEEN :start AND :end")
    List<MoodAndEnergy> getAllNow(Long start, Long end);

    @Query("SELECT * FROM mood_and_energy_entry_table WHERE timeOfEntry BETWEEN :start AND :end")
    LiveData<List<MoodAndEnergy>> getAllByTimeOfEntry(Long start, Long end);
}
