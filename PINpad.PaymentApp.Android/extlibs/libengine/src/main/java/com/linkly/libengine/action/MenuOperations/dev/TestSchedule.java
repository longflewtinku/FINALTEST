package com.linkly.libengine.action.MenuOperations.dev;

import com.linkly.libengine.action.IAction;

public class TestSchedule extends IAction {
    
    @Override
    public String getName() {
        return "TestSchedule";
    }

    @Override
    public void run() {
        d.getMessages().sendAutoRebootRequest(context);
    }
}
