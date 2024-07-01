package com.linkly.libengine.action.MenuOperations.users;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.users.UserManager;

public class UserDelete extends IAction {

    @Override
    public String getName() {
        return "UserDelete";
    }

    @Override
    public void run() {

        UserManager.getInstance().deleteUser();
        ui.displayMainMenuScreen();
    }
}
