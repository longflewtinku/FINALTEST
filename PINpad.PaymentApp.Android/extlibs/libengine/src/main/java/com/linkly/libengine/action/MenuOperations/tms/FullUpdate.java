package com.linkly.libengine.action.MenuOperations.tms;

import com.linkly.libengine.action.IAction;

public class FullUpdate extends IAction {
    @Override
    public String getName() {
        return "FullUpdate";
    }

    @Override
    public void run() {
        d.getFramework().postUINotification("TMS", "Full Update");
        d.getMessages().sendDownloadRequest(context, true);
        d.getAppCallbacks().exitApplication();
    }
}
