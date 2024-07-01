package com.linkly.libengine.env;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {EnvVar.class}, version = 1)
public abstract class EnvVarDatabase extends RoomDatabase {
    public abstract EnvVarDao envVarDao();

}

