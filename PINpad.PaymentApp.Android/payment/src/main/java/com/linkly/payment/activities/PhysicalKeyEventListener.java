package com.linkly.payment.activities;

import android.view.KeyEvent;

/*
Exists because Fragments don't have innate means to listen to key events, they must do it through
their parent Activity.
Use PhysicalKeyEventDispatcher to register and unregister a PhysicalKeyEventListener.
 */
public interface PhysicalKeyEventListener {
    void onKeyEvent(int keyCode, KeyEvent event);
}
