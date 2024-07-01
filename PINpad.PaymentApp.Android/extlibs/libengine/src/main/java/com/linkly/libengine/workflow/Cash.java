package com.linkly.libengine.workflow;

import com.linkly.libengine.action.user_action.InputCashback;

public class Cash extends CommonPayment {
    public Cash() {
        //this is where the differences will be added in respect to the common payment
        //card processing changes
        this.removeAction(InputCashback.class);
    }
}
