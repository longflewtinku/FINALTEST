package com.linkly.libengine.workflow;

import com.linkly.libengine.action.user_action.InputAmount;
import com.linkly.libengine.action.user_action.InputPurchasePlusCashAmounts;
import com.linkly.libengine.action.user_action.InputTip;

public class PurchasePlusCashback extends CommonPayment {
    public PurchasePlusCashback() {
        //this is where the differences will be added in respect to the common payment
        this.removeAction(InputAmount.class);
        //MD : Before tip, Purchase+cash amount need to be entered
        this.insertBeforeAction(new InputPurchasePlusCashAmounts(), InputTip.class);
    }
}
