package com.linkly.libengine.action.check;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionReferral;
import com.linkly.libengine.overrides.CoreOverrides;
public class CheckPostCommsReferral extends IAction {
    @Override
    public String getName() {
        return "CheckPostCommsReferral";
    }

    @Override
    public void run() {

        checkPostCommsReferralRequired();
    }

    private void checkPostCommsReferralRequired() {

        if (CoreOverrides.get().isSpoofReferral()) {
            d.getWorkflowEngine().setNextAction(TransactionReferral.class);
        }
    }
}
