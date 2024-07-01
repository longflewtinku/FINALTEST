package com.linkly.libengine.action.MenuOperations.users;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.users.UserManager;

public class UserReport extends IAction {

    @Override
    public String getName() {
        return "UserReport";
    }

    @Override
    public void run() {
        UserManager.getInstance().userReport(mal);
        ui.displayMainMenuScreen();
    }
}
