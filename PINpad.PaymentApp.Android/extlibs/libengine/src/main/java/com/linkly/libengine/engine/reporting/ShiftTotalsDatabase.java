package com.linkly.libengine.engine.reporting;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ShiftTotals.class}, version = 1)
public abstract class ShiftTotalsDatabase extends RoomDatabase {
    public abstract ShiftTotalsDao shiftTotalsDao();
}
