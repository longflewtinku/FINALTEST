package com.linkly.libengine.action.user_action;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.check.CheckAmounts;

public class DisplaySummary extends IAction {
    @Override
    public String getName() {
        return "DisplaySummary";
    }

    @Override
    public void run() {

        {
            //check if amounts are valid before displaying summary
            CheckAmounts checkAmounts = new CheckAmounts();
            checkAmounts.run(d, mal, context);
            if (d.getWorkflowEngine().isJumping()) {
                return;
            }
        }
        //todo not implemented yet
        //should display a summary of all amounts incl DCC / CASHBACK / TIP
        //use should be able to cancel the transaction at this point
    }
}
