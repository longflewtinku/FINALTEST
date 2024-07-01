package com.linkly.libengine.action.MenuOperations.users;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.users.UserManager;

public class UserResetPassword extends IAction {

    @Override
    public String getName() {
        return "UserResetPassword";
    }

    @Override
    public void run() {
        UserManager.getInstance().resetUserPassword(mal);
        ui.displayMainMenuScreen();
    }
}
