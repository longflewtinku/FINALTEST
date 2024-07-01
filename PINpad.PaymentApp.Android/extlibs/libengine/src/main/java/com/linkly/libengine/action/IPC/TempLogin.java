package com.linkly.libengine.action.IPC;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.users.UserManager;

import timber.log.Timber;

public class TempLogin extends IAction {
    @Override
    public String getName() {
        return "TempLogin";
    }

    @Override
    public void run() {

        this.ipcTempLogin();
    }

    private void ipcTempLogin() {

        if (!UserManager.autoLoginUser(d, trans.getAudit().getIpcUserId(), trans.getAudit().getIpcUserPwd(), trans.getAudit().getIpcDepartmentId(), context)) {
            Timber.i( "User Login Failed:%s", trans.getAudit().getIpcUserId());
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }
    }
}
