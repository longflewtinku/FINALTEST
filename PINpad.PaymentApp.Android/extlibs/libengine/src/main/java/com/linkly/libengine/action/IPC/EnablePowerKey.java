package com.linkly.libengine.action.IPC;

import com.linkly.libengine.action.IAction;

public class EnablePowerKey extends IAction {
    @Override
    public String getName() {
        return "EnablePowerKey";
    }

    @Override
    public void run() {
        mal.getHardware().enablePowerKey(true);
    }
}
