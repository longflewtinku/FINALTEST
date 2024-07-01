package com.linkly.libengine.action.MenuOperations.users;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.users.User;
import com.linkly.libengine.users.UserManager;

import timber.log.Timber;

public class UserLogin extends IAction {

    @Override
    public String getName() {
        return "UserLogin";
    }

    @Override
    public void run() {
        Timber.d("run[UserLogin]...");
        User u = UserManager.getInstance().userLoginRegular();
        if (u != null) {
            UserManager.getInstance().setActiveUser(u);
            ui.displayMainMenuScreen();
        }
    }

    // Allow this action to be cancelled.
    @Override
    public boolean cancellableAction() {
        return true;
    }
}
