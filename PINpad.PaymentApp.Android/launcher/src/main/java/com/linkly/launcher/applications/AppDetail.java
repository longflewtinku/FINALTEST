package com.linkly.launcher.applications;

import android.graphics.drawable.Drawable;

@SuppressWarnings("java:S1104") // Make xxx a static final constant or non-public and provide accessors if needed.
public class AppDetail {
    public String label;
    public String packageName;
    public String activityName;
    public Drawable icon;
    public String displayName;
    public boolean enableNavBar;
    public boolean autoStart;
    public int autoStartDelay = 0;
    public int priority = 0;
}