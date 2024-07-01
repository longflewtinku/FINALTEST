package com.linkly.payment.activities;

/*
Interface facilitating a parent Activity that registers and unregisters listeners to which it
 dispatches events.
 */
public interface PhysicalKeyEventDispatcher {
    void registerListener(PhysicalKeyEventListener listener);
    void unregisterListener(PhysicalKeyEventListener listener);
}
