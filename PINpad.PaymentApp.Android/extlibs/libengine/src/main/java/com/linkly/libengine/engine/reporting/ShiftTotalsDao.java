package com.linkly.libengine.engine.reporting;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ShiftTotalsDao {
    @Query("SELECT * FROM shiftTotals")
    List<ShiftTotals> getAll();

    @Query("SELECT * FROM shiftTotals ORDER BY id DESC LIMIT 1")
    ShiftTotals getLatest();

    @Insert
    long insert( ShiftTotals shiftTotals );

    @Update
    void update( ShiftTotals shiftTotals );

    @Query("SELECT * FROM shiftTotals ORDER BY id DESC LIMIT 1 OFFSET 1")
    ShiftTotals getPrevious();

    @Query("DELETE FROM shiftTotals WHERE id < :id")
    void deleteBeforeId(int id);
}
