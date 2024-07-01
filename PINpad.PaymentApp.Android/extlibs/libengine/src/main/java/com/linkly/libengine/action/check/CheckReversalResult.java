package com.linkly.libengine.action.check;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.user_action.UiApproved;
import com.linkly.libengine.action.user_action.UiCancelled;
import com.linkly.libengine.action.user_action.UiDeclined;

public class CheckReversalResult extends IAction {
    @Override
    public String getName() {
        return "CheckReversalResult";
    }

    @Override
    public void run() {

        if (trans.isApproved()) {
            UiApproved uiApproved = new UiApproved();
            uiApproved.run(d, mal, context);
            d.getWorkflowEngine().setNextAction(null);
        } else if (trans.isDeclined()) {
            UiDeclined uiDeclined = new UiDeclined();
            uiDeclined.run(d, mal, context);
            d.getWorkflowEngine().setNextAction(null);
        } else if (trans.isCancelled()) {
            UiCancelled uiCancelled = new UiCancelled();
            uiCancelled.run(d, mal, context);
            d.getWorkflowEngine().setNextAction(TransactionFinalizer.class);
        }
    }
}
