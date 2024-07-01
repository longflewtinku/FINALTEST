package com.linkly.libengine.action.check;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.user_action.UiApproved;
import com.linkly.libengine.action.user_action.UiDeclined;

public class CheckTestConnectionResult extends IAction {
    @Override
    public String getName() {
        return "CheckTestConnectionResult";
    }

    @Override
    public void run() {

        CheckResult checkResult = new CheckResult();
        checkResult.run(d, mal, context);

        if (trans.isApproved()) {
            UiApproved uiApproved = new UiApproved();
            uiApproved.run(d, mal, context);
        } else if (trans.isDeclined()) {
            UiDeclined uiDeclined = new UiDeclined();
            uiDeclined.run(d, mal, context);
        }
        d.getWorkflowEngine().setNextAction(TransactionFinalizer.class);
    }
}
