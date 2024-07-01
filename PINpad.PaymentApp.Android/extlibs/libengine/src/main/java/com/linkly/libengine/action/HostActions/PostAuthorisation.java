package com.linkly.libengine.action.HostActions;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.protocol.IProto;

public class PostAuthorisation extends IAction {
    @Override
    public String getName() {
        return "PostAuthorisation";
    }

    @Override
    public void run() {

        IProto iproto = d.getProtocol();
        iproto.postAuthorize(trans);
    }
}
