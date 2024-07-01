package com.linkly.libengine.action.user_action;

import com.linkly.libengine.action.IAction;

public class MainMenu extends IAction {
    @Override
    public String getName() {
        return "MainMenu";
    }

    @Override
    public void run() {
        mal.getHardware().enablePowerKey(true);
        ui.displayMainMenuScreen();
    }

    @Override
    public boolean cancellableAction() {
        return true;
    }
}
