package com.linkly.libengine.action.IPC;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.users.UserManager;

public class UserUndoUpgrade extends IAction {
    @Override
    public String getName() {
        return "UserUndoUpgrade";
    }

    @Override
    public void run() {

        UserManager.undoUpgradeUserLevel();
    }
}
