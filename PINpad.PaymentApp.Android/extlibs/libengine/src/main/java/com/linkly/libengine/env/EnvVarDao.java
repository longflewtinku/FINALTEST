package com.linkly.libengine.env;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EnvVarDao {
    @Query("SELECT * FROM envvars")
    List<EnvVar> getAll();

    @Query("SELECT * FROM envvars WHERE name = :name LIMIT 1")
    EnvVar findByName(String name);

    @Insert
    void insert( EnvVar envVar );

    @Update
    void update( EnvVar envVar );

    @Query( "DELETE FROM envvars WHERE name = :name" )
    void deleteByName(String name);

    @Delete
    void delete(EnvVar envVar);

    @Query("DELETE FROM envvars")
    void deleteAll();
}
