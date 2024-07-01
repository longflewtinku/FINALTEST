package com.linkly.launcher.access;

public interface AccessCodeCheckCallbacks {
    public void onAdminMenuGranted();
    public void onExitLauncherGranted();
    public void onEnterUnattendedServiceModeModeGranted();
    void onAccessDenied();
    void onAuthCancellation();
}
