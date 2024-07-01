package com.linkly.launcher;

public interface POSConnectionStatusHost {
    // Tri-state where null indicates uninitialised state.
    Boolean isPOSConnected();
    void setPOSConnected(boolean isConnected);
}
