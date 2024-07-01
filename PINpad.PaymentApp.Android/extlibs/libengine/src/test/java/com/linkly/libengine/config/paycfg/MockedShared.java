package com.linkly.libengine.config.paycfg;


import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MockedShared implements SharedPreferences {

    private Map<String, Object> data = new HashMap<>();

    private Editor editor = new MockedEditor(data);

    public static class MockedEditor implements Editor {

        private Map<String, Object> data = new HashMap<>();

        public MockedEditor(Map<String, Object> data) {
            this.data = data;
        }

        @Override
        public Editor putString(String key, @Nullable String value) {
            data.put(key, value);
            return this;
        }

        @Override
        public Editor putStringSet(String key, @Nullable Set<String> values) {
            data.put(key, values);
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            data.put(key, value);
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            data.put(key, value);
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            data.put(key, value);
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            data.put(key, value);
            return this;
        }

        @Override
        public Editor remove(String key) {
            data.remove(key);
            return this;
        }

        @Override
        public Editor clear() {
            data.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return true;
        }

        @Override
        public void apply() {

        }
    }

    @Override
    public Map<String, ?> getAll() {
        return data;
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return contains(key) ? (String) data.get(key) : defValue;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return null;
    }

    @Override
    public int getInt(String key, int defValue) {
        return contains(key) ? (int) data.get(key) : defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        return contains(key) ? (long) data.get(key) : defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        return contains(key) ? (float) data.get(key) : defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return contains(key) ? (boolean) data.get(key) : defValue;
    }

    @Override
    public boolean contains(String key) {
        return data.containsKey(key);
    }

    @Override
    public Editor edit() {
        return editor;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

    }
}