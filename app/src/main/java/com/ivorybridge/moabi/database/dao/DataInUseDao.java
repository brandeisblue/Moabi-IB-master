package com.ivorybridge.moabi.database.dao;

import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.InputInUse;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface DataInUseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(InputInUse inputInUse);

    @Query("DELETE FROM input_in_use_table")
    void deleteAllInputsInUse();

    @Query("DELETE from input_in_use_table WHERE lower(name) = lower(:name)")
    void deleteInputInUse(String name);

    @Query("SELECT * FROM input_in_use_table")
    LiveData<List<InputInUse>> getAllInputsInUse();

    @Query("SELECT * FROM input_in_use_table")
    List<InputInUse> getAllInputsInUseNow();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ConnectedService connectedService);

    @Query("DELETE FROM connected_service_table")
    void deleteAllConnectedServices();

    @Query("DELETE from connected_service_table WHERE lower(name) = lower(:name)")
    void deleteConnectedService(String name);

    @Query("SELECT * FROM connected_service_table")
    LiveData<List<ConnectedService>> getAllConnectedServices();

    @Query("SELECT * FROM connected_service_table")
    List<ConnectedService> getAllConnectedServicesNow();
}
