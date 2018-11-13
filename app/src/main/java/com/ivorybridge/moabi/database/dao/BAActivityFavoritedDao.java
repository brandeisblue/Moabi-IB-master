package com.ivorybridge.moabi.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ivorybridge.moabi.database.entity.baactivity.BAActivityFavorited;

import java.util.List;

@Dao
public interface BAActivityFavoritedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BAActivityFavorited activity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BAActivityFavorited> activities);

    @Query("DELETE FROM ba_activity_favorited_table")
    void deleteAll();

    @Query("DELETE FROM ba_activity_favorited_table where lower(name) = lower(:name)")
    void delete(String name);

    @Query("SELECT * FROM ba_activity_favorited_table WHERE lower(name) = lower(:name) limit 1")
    LiveData<BAActivityFavorited> get(String name);

    @Query("SELECT * FROM ba_activity_favorited_table")
    LiveData<List<BAActivityFavorited>> getAll();

    @Update
    void updateSingleRecord(BAActivityFavorited activity);
}
