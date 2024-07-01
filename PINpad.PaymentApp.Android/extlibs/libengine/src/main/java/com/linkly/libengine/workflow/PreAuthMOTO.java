package com.linkly.libengine.workflow;

import com.linkly.libengine.action.HostActions.PreAuthorisation;
import com.linkly.libengine.action.IPC.CheckExistingPreAuth;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.cardprocessing.DCCProcessing;
import com.linkly.libengine.action.cardprocessing.GetCard;
import com.linkly.libengine.action.cardprocessing.GetMOTO;
import com.linkly.libengine.action.cardprocessing.ManualProcessing;
import com.linkly.libengine.action.check.CheckPassword;
import com.linkly.libengine.action.check.CheckPostCommsReferral;
import com.linkly.libengine.action.check.CheckUserLevel;
import com.linkly.libengine.action.user_action.ConfirmSurcharge;
import com.linkly.libengine.action.user_action.InputCashback;

public class PreAuthMOTO extends CommonPayment {
    public PreAuthMOTO() {
        //card processing changes
        this.removeAction(InputCashback.class);
        this.removeAction(DCCProcessing.class);

        this.insertBeforeAction(new CheckExistingPreAuth(), PreAuthorisation.class);

        this.insertAfterAction( new CheckPassword(), CheckUserLevel.class );
        this.insertAfterAction(new ManualProcessing(), CheckPassword.class);
        this.removeAction(GetCard.class);
        this.insertAfterAction(new GetMOTO(), ManualProcessing.class);
        this.insertAfterAction( new ConfirmSurcharge(), GetMOTO.class );

        //declined part
        this.insertAfterAction(new CheckPostCommsReferral(), TransactionDecliner.class);
    }
}
