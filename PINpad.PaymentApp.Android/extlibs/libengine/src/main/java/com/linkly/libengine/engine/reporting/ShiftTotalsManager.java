package com.linkly.libengine.engine.reporting;

import android.content.Context;

import androidx.room.Room;

public class ShiftTotalsManager {
    private static ShiftTotalsDao shiftTotalsDao = null;

    private ShiftTotalsManager() { /* empty */ }

    public static ShiftTotalsDao getShiftTotalsDao(Context context) {
        if (shiftTotalsDao == null) {
            // load/create new database
            ShiftTotalsDatabase shiftTotalsDatabase;
            shiftTotalsDatabase = Room.databaseBuilder( context, ShiftTotalsDatabase.class, "ShiftTotals.db" ).allowMainThreadQueries().build();
            shiftTotalsDao = shiftTotalsDatabase.shiftTotalsDao();
        }
        return shiftTotalsDao;
    }
}
