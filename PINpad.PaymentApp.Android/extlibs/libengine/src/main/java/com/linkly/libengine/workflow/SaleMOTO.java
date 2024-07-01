package com.linkly.libengine.workflow;

import com.linkly.libengine.action.cardprocessing.GetCard;
import com.linkly.libengine.action.cardprocessing.GetMOTO;
import com.linkly.libengine.action.cardprocessing.ManualProcessing;
import com.linkly.libengine.action.check.CheckPassword;
import com.linkly.libengine.action.check.CheckUserLevel;
import com.linkly.libengine.action.user_action.ConfirmSurcharge;
import com.linkly.libengine.action.user_action.InputTip;
import com.linkly.libengine.action.user_action.UiProcessing;

public class SaleMOTO extends CommonPayment {
    public SaleMOTO() {
        //this is where the differences will be added in respect to the common payment

        // Restrict input to only allow moto
        this.removeAction(InputTip.class);
        this.insertAfterAction(new CheckPassword(), CheckUserLevel.class);
        this.insertAfterAction(new ManualProcessing(), CheckPassword.class);
        this.removeAction(GetCard.class);
        this.insertAfterAction(new UiProcessing(), ManualProcessing.class);
        this.insertAfterAction(new GetMOTO(), UiProcessing.class);
        this.insertAfterAction( new ConfirmSurcharge(), GetMOTO.class );
    }
}
