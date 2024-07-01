package com.linkly.libengine.action.MenuOperations.users;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.users.UserManager;

public class UserAdd extends IAction {

    @Override
    public String getName() {
        return "UserAdd";
    }

    @Override
    public void run() {
        UserManager.getInstance().addUser(mal);
        ui.displayMainMenuScreen();
    }
}
