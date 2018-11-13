package com.ivorybridge.moabi.database.dao;

import com.ivorybridge.moabi.database.entity.util.Credential;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface CredentialDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Credential inputInUse);

    @Query("DELETE FROM credential_table")
    void deleteAll();

    @Query("DELETE from credential_table WHERE lower(serviceName) = lower(:name)")
    void deleteCredential(String name);

    @Query("SELECT * FROM credential_table")
    LiveData<List<Credential>> getAll();

    @Query("SELECT * FROM credential_table")
    List<Credential> getAllNow();

    @Query("SELECT * FROM credential_table WHERE lower(serviceName) = lower(:name)")
    LiveData<Credential> get(String name);

    @Query("SELECT * FROM credential_table WHERE lower(serviceName) = lower(:name)")
    Credential getNow(String name);
    
}
