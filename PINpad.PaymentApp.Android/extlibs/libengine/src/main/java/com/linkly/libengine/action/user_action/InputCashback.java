package com.linkly.libengine.action.user_action;

import com.linkly.libengine.action.IAction;

public class InputCashback extends IAction {

    @Override
    public String getName() {
        return "InputCashback";
    }

    @Override
    public void run() {
        // WC - IAAS-958. this checks if cashback is allowed for given card and presents 'cashback required?' question to user.
        // Disabling this feature for now in lieu of a pwcb transaction type, and because it's causing issues with POS initiated PWCB transactions
        // keeping code here for when/if we reinstate this feature
        return;


    }

}
