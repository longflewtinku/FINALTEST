package com.linkly.libengine.users;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {User.class}, version = 5)
@TypeConverters({User.class})
public abstract class UserDatabase extends RoomDatabase {
    public abstract UserDao userDao();

}
