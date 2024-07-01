package com.linkly.payment.activities;

import com.google.android.material.tabs.TabLayout;

/*
Interface for child Fragments to communicate with parent Activity that hosts an AppBar containing:
- a "header" (hard-coded as BrandingHeader at the moment),
- a TabLayout.
 */
public interface AppBarHost {
    void showTabs(boolean shouldShow);
    void showHeader(boolean shouldShow);
    TabLayout getTabLayout();
}
