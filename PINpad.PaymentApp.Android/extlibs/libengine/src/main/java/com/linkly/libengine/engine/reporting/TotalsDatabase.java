package com.linkly.libengine.engine.reporting;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Totals.class}, version = 1)
@TypeConverters({TransTypeConverters.class})
public abstract class TotalsDatabase extends RoomDatabase {
    public abstract TotalsDao totalsDao();
}
