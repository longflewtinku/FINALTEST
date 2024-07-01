package com.linkly.libengine.action.HostActions;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.protocol.IProto;

public class Authorise extends IAction {
    @Override
    public String getName() {
        return "Authorise";
    }

    @Override
    public void run() {

        IProto iproto = d.getProtocol();
        iproto.authorize(trans);
    }
}
