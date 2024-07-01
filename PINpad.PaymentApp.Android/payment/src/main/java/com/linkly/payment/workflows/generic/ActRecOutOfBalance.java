package com.linkly.payment.workflows.generic;

import com.linkly.libengine.action.IAction;

public class ActRecOutOfBalance extends IAction {
    @Override
    public String getName() {
        return "ActRecOutOfBalance";
    }

    @Override
    public void run() {
        trans.setApproved(true); // set to approved means record won't be deleted
        trans.setCancelled(false);
        trans.setDeclined(true);
    }
}
