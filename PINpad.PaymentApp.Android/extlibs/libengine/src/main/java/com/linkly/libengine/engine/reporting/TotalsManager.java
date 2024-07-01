package com.linkly.libengine.engine.reporting;

import android.content.Context;

import androidx.room.Room;

import com.linkly.libmal.MalFactory;

public class TotalsManager {
    private static final String TAG = "TotalsManager";
    private static TotalsManager instance = null;
    private static TotalsDatabase totalsDatabase;
    public static TotalsDao totalsDao;

    public static TotalsManager getInstance() {
        if (instance == null) {
            // TODO: Fix this when refactor happens....
            instance = new TotalsManager(MalFactory.getInstance().getMalContext());
        }
        return instance;
    }

    TotalsManager(Context context) {
        // load/create new database
        totalsDatabase = Room.databaseBuilder( context, TotalsDatabase.class, "Totals.db" ).allowMainThreadQueries().build();
        totalsDao = totalsDatabase.totalsDao();
    }

}
