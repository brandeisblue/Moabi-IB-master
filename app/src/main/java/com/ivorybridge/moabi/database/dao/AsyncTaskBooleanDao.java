package com.ivorybridge.moabi.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ivorybridge.moabi.database.entity.util.AsyncTaskBoolean;

import java.util.List;

@Dao
public interface AsyncTaskBooleanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AsyncTaskBoolean AsyncTaskBoolean);

    @Query("DELETE FROM async_task_table")
    void deleteAll();

    @Query("DELETE from async_task_table WHERE lower(task_name) = lower(:taskName)")
    void delete(String taskName);

    @Query("SELECT * from async_task_table")
    LiveData<List<AsyncTaskBoolean>> getAll();

    @Query("SELECT * FROM async_task_table WHERE lower(task_name) = lower(:taskName)")
    LiveData<AsyncTaskBoolean> get(String taskName);
}
