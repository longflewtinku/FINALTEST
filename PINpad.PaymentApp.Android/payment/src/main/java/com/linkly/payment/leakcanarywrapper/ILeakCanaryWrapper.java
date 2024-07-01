package com.linkly.payment.leakcanarywrapper;
/**
 * Object watcher wrapper Interface class for LeakCanary.
 * */
public interface ILeakCanaryWrapper {
    /**
     * Adds an object to the watch list to leakCanary
     * Only in Debug builds, is empty in Release builds
     * */
    void addWatcher( Object objectToWatch );
}
