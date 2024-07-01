package com.linkly.libengine.engine.reporting;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TotalsDao {
    @Query("SELECT * FROM totals")
    List<Totals> getAll();

    @Query("SELECT * FROM totals WHERE transType = :transType AND cardName = :cardName")
    List<Totals> findByTransTypeAndCardName(int transType, String cardName);

    @Query("SELECT uid, cardName, transType, "+
            "sum(netAmount) as netAmount, sum(netCount) as netCount, "+
            "sum(debitAmount) as debitAmount, sum(debitCount) as debitCount, "+
            "sum(creditAmount) as creditAmount, sum(creditCount) as creditCount, "+
            "isGroupTotal, firstTranID, lastTranId " +
            " FROM totals GROUP BY cardName")
    List<Totals> groupByCardName();

    @Query("SELECT uid, cardName, transType, "+
            "sum(netAmount) as netAmount, sum(netCount) as netCount, "+
            "sum(debitAmount) as debitAmount, sum(debitCount) as debitCount, "+
            "sum(creditAmount) as creditAmount, sum(creditCount) as creditCount, "+
            "isGroupTotal, firstTranID, lastTranId " +
            " FROM totals GROUP BY transType")
    List<Totals> groupByTransType();

    @Query("SELECT * FROM totals ORDER BY uid DESC LIMIT 1")
    Totals getLatest();

    @Insert
    void insert( Totals Totals );

    @Update
    void update( Totals Totals );

    @Delete
    void delete(Totals Totals);

    @Query("DELETE FROM totals")
    void deleteAll();
}
