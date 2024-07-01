package com.linkly.payment.workflows.suncorp;

import com.linkly.libengine.action.IAction;

public class ActSuncorpRecOutOfBalance extends IAction {
    @Override
    public String getName() {
        return "ActSuncorpRecOutOfBalance";
    }

    @Override
    public void run() {
        trans.setApproved(true); // set to approved means record won't be deleted
        trans.setCancelled(false);
        trans.setDeclined(true);
    }
}
