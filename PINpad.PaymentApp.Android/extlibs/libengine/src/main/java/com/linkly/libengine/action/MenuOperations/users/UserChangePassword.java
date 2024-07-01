package com.linkly.libengine.action.MenuOperations.users;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.users.UserManager;

public class UserChangePassword extends IAction {

    @Override
    public String getName() {
        return "UserChangePassword";
    }

    @Override
    public void run() {

        UserManager.getInstance().changeUserPassword(null);
        ui.displayMainMenuScreen();
    }
}
