package com.linkly.libengine.action.MenuOperations.admin;

import com.linkly.libengine.action.IAction;

public class Exit extends IAction {
    @Override
    public String getName() {
        return "Exit";
    }

    @Override
    public void run() {
        d.getAppCallbacks().exitApplication();
    }
}
