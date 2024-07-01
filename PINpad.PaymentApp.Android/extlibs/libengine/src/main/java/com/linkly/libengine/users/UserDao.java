package com.linkly.libengine.users;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    List<User> getAll();

    @Query("SELECT * FROM users WHERE uid IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    User findByUserId(String userId);

    @Insert
    void insertAll(User... users);

    @Insert
    void insert( User user );

    @Update
    void update( User user );

    @Query( "DELETE FROM users WHERE userId = :userId" )
    void deleteUserId(String userId);

    @Delete
    void delete(User user);
}
