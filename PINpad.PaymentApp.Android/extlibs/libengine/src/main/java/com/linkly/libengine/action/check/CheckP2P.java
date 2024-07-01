package com.linkly.libengine.action.check;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.Engine;
import com.linkly.libsecapp.P2PLib;

public class CheckP2P extends IAction {
    @Override
    public String getName() {
        return "CheckP2P";
    }

    // If our P2Pe/secapp has issues we need to reinitialise the connection.
    // This also ends up creating a new object (even though it is an instance)
    @Override
    public void run() {
        if(d.getAppCallbacks().initialiseP2Pe(context)) {
            // We need to initialise config for this to work.
            Engine.initialiseP2PeConfig(d.getPayCfg(), d.getConfig().getEmvCfg(), d.getConfig().getCtlsCfg());
            // Set the dependencies value
            d.setP2PLib(P2PLib.getInstance());
        }
    }
}