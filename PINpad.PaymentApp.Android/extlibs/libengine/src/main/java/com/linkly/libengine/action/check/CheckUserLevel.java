package com.linkly.libengine.action.check;

import static com.linkly.libengine.users.User.Privileges.SUPERVISOR;
import static com.linkly.libengine.users.User.Privileges.USER;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.UIScreenDef.PERMISSIONS_ERROR_SUPERVISOR_LOGIN;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.users.User;
import com.linkly.libengine.users.UserManager;
import com.linkly.libui.IUIDisplay;

import timber.log.Timber;

public class CheckUserLevel extends IAction {

    @Override
    public String getName() {
        return "CheckUserLevel";
    }

    @Override
    public void run() {

        if (trans.isSupervisorRequired(d.getPayCfg()) || d.getPayCfg().getPasswordRequiredForAllCards(trans, trans.getCard().getCaptureMethod()))  {
            runUserLogin(d, ui);
        }
    }

    public static boolean runUserLogin(IDependency d, IUIDisplay ui) {
        // TODO implement supervisor users
        User.Privileges userLevel = UserManager.getActiveUser().getPrivileges();
        Timber.i( "Need to login the supervisor if he isn't already");
        if ((userLevel == USER) && (d.getUsrMgr().getUpgradedUser() == null)) {
            User newUser = UserManager.upgradeUserLevel(SUPERVISOR, ui);
            if (newUser == null) {
                ui.showScreen(PERMISSIONS_ERROR_SUPERVISOR_LOGIN);
                ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                return false;
            }
        }
        return true;
    }
}
