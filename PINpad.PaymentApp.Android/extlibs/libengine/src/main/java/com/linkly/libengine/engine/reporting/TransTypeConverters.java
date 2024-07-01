package com.linkly.libengine.engine.reporting;

import androidx.room.TypeConverter;

import com.linkly.libengine.engine.EngineManager;

public class TransTypeConverters {
    @TypeConverter
    public EngineManager.TransType intToEnum(int input ) {
        return EngineManager.TransType.values()[input];
    }

    @TypeConverter
    public int enumToInt( EngineManager.TransType value ) {
        return value.ordinal();
    }
}
