package com.ivorybridge.moabi.database.dao;

import com.ivorybridge.moabi.database.entity.util.UserGoal;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserGoal userGoal);

    @Query("DELETE FROM user_goal_table")
    void deleteAll();

    @Query("SELECT * FROM user_goal_table WHERE lower(date) = lower(:date)")
    LiveData<List<UserGoal>> getAll(String date);

    @Query("SELECT * from user_goal_table")
    LiveData<List<UserGoal>> getAll();

    @Query("SELECT * FROM user_goal_table WHERE dateInLong BETWEEN :start AND :end")
    LiveData<List<UserGoal>> getAll(Long start, Long end);

    @Query("SELECT * FROM user_goal_table WHERE dateInLong BETWEEN :start AND :end")
    List<UserGoal> getAllNow(Long start, Long end);

    @Query("SELECT * FROM user_goal_table WHERE priority = :priority AND dateInLong BETWEEN :start AND :end")
    LiveData<List<UserGoal>> getAll(int priority, Long start, Long end);

    @Query("SELECT * FROM user_goal_table WHERE priority = :priority")
    LiveData<UserGoal> getGoal(int priority);

    @Query("SELECT * FROM user_goal_table WHERE priority = :priority")
    UserGoal getGoalNow(int priority);

    @Query("SELECT * FROM user_goal_table WHERE lower(goalType) = lower(:goalType) AND dateInLong BETWEEN :start AND :end")
    LiveData<List<UserGoal>> getAll(String goalType, Long start, Long end);

    @Query("SELECT * FROM user_goal_table WHERE lower(goalType) = lower(:goalType) AND dateInLong BETWEEN :start AND :end")
    List<UserGoal> getAllNow(String goalType, Long start, Long end);
}

