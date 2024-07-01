package com.linkly.libengine.engine.reporting;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReconciliationDao {
    @Query("SELECT * FROM recs")
    List<Reconciliation> getAll();

    @Query("SELECT * FROM recs WHERE transID = :transId LIMIT 1")
    Reconciliation findByTransId(Integer transId);

    @Query("SELECT * FROM recs ORDER BY uid DESC LIMIT 1")
    Reconciliation getLatest();

    @Insert
    long insert( Reconciliation Reconciliation );

    @Update
    void update( Reconciliation Reconciliation );

    @Delete
    void delete(Reconciliation Reconciliation);

    @Query("DELETE FROM recs WHERE uid < :uid")
    int deleteTxnsBeforeUid( int uid );

    @Query("DELETE FROM recs")
    void deleteAll();
}
