package com.linkly.libengine.action.MenuOperations.tms;

import com.linkly.libengine.action.IAction;

public class Update extends IAction {
    @Override
    public String getName() {
        return "Update";
    }

    @Override
    public void run() {
        d.getFramework().postUINotification("TMS", "Soft Update");
        d.getMessages().sendDownloadRequest(context, false);
        d.getAppCallbacks().exitApplication();
    }
}
