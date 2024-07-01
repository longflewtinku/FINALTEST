package com.linkly.libengine.action.MenuOperations.dev;

import com.linkly.libengine.action.IAction;

import java.util.Calendar;

public class TestAddTime extends IAction {

    @Override
    public String getName() {
        return "TestAddTime";
    }

    @Override
    public void run() {
        Calendar d = Calendar.getInstance();
        d.add(Calendar.HOUR, 1);
        d.getTime();
        mal.getHardware().setSystemDateTime(d.getTime());
    }
}
