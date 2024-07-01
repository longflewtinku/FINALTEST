package com.linkly.libengine.engine.reporting;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.linkly.libengine.engine.transactions.properties.TReconciliationFigures;

@Database(entities = {Reconciliation.class}, version = 2)
@TypeConverters({TReconciliationFigures.class, Amounts.class})
public abstract class ReconciliationDatabase extends RoomDatabase {
    public abstract ReconciliationDao reconciliationDao();
}
