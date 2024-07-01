package com.linkly.libengine.workflow;

import com.linkly.libengine.action.cardprocessing.GetCard;
import com.linkly.libengine.action.check.CheckOfflineLimits;

public class Sale extends CommonPayment {
    public Sale() {

        this.insertBeforeAction(new CheckOfflineLimits(), GetCard.class);
        //this is where the differences will be added in respect to the common payment
    }
}
