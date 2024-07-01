package com.linkly.libengine.action.user_action;

import com.linkly.libengine.action.IAction;

/*
AUTO MOTO Txs (aka integration mode triggered MOTO Txs) assumed that ActIdle was behind in the
stack, however Linky Launcher was appearing sometimes (probably due to some other change seeing
ActIdle hidden (startupParam hooks most likely). So decouple dependence on other parts of the system
by ensuring the necessary components for the Workflow are in place (running|foregrounded) when needed.
 */
public class EnsureDisplayActIdle extends IAction {
    @Override
    public String getName() {
        return "EnsureDisplayActIdle";
    }

    @Override
    public void run() {
        if (d.getAppCallbacks() != null) {
            d.getAppCallbacks().runPleaseWaitScreen();
        }
    }
}
