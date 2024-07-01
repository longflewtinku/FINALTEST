package com.linkly.libengine.engine.transactions;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {TransRec.class}, version = 15)
@TypeConverters({TransRecConverters.class})
public abstract class TransRecDatabase extends RoomDatabase {
    public abstract TransRecDao transRecDao();
}


