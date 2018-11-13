package com.ivorybridge.moabi.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ivorybridge.moabi.database.entity.baactivity.BAActivityInLibrary;

import java.util.List;

@Dao
public interface BAActivityInLibraryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BAActivityInLibrary activity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BAActivityInLibrary> activities);

    @Query("DELETE FROM ba_activity_library_table")
    void deleteAll();

    @Query("DELETE FROM ba_activity_library_table where lower(name) = lower(:name)")
    void delete(String name);

    @Query("SELECT * FROM ba_activity_library_table WHERE lower(name) = lower(:name) limit 1")
    LiveData<BAActivityInLibrary> get(String name);

    @Query("SELECT * from ba_activity_library_table")
    LiveData<List<BAActivityInLibrary>> getAll();

    @Query("SELECT * FROM ba_activity_library_table limit 1")
    BAActivityInLibrary[] getAny();

    @Update
    void updateSingleRecord(BAActivityInLibrary activity);
}
