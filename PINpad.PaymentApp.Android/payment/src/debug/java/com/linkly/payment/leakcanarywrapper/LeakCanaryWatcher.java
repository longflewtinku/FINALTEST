package com.linkly.payment.leakcanarywrapper;

import leakcanary.AppWatcher;

public class LeakCanaryWatcher implements ILeakCanaryWrapper {
    /**
     * {@link leakcanary.LeakCanary} leak canary watcher.
     * Will watch the object for memory leaks
     * @param objectToWatch {@link Object} that is being monitored */

    @SuppressWarnings("deprecation")
    @Override
    public void addWatcher( Object objectToWatch ) {
        AppWatcher.INSTANCE.getObjectWatcher().watch( objectToWatch,
                objectToWatch.getClass().getSimpleName() + " is being watched" );
    }
}
