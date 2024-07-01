package com.linkly.libengine.action.IPC;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.users.UserManager;
public class TempLogout extends IAction {
    @Override
    public String getName() {
        return "TempLogout";
    }

    @Override
    public void run() {

        this.ipcTempLogout(trans);
    }
    private void ipcTempLogout(TransRec trans) {
        UserManager.undoAutoLoginUser(d);
    }
}
