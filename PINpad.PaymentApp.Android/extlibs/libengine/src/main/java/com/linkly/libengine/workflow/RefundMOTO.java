package com.linkly.libengine.workflow;

import com.linkly.libengine.action.ConstrainedAction;
import com.linkly.libengine.action.InterfaceSelected;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.cardprocessing.DCCProcessing;
import com.linkly.libengine.action.cardprocessing.GetCard;
import com.linkly.libengine.action.cardprocessing.GetMOTO;
import com.linkly.libengine.action.cardprocessing.ManualProcessing;
import com.linkly.libengine.action.check.CheckBINRange;
import com.linkly.libengine.action.check.CheckCUP;
import com.linkly.libengine.action.check.CheckOfflineAllowed;
import com.linkly.libengine.action.check.CheckPassword;
import com.linkly.libengine.action.check.CheckPostCommsReferral;
import com.linkly.libengine.action.check.CheckRefundLimits;
import com.linkly.libengine.action.check.CheckUserLevel;
import com.linkly.libengine.action.user_action.InputCashback;
import com.linkly.libengine.action.user_action.InputReference;
import com.linkly.libengine.action.user_action.InputTip;
import com.linkly.libengine.action.user_action.UiProcessing;
import com.linkly.libengine.engine.transactions.properties.TCard;


public class RefundMOTO extends CommonPayment {
    public RefundMOTO() {
        //initial state changes

        this.removeAction(InputTip.class);
        this.removeAction(InputReference.class);

        // Restrict input to only allow moto
        this.insertAfterAction( new CheckPassword(), CheckUserLevel.class );
        this.insertAfterAction(new ManualProcessing(), CheckPassword.class);
        this.removeAction(GetCard.class);
        this.insertBeforeAction( new CheckRefundLimits(), CheckPassword.class );
        this.insertAfterAction(new UiProcessing(), ManualProcessing.class);
        this.insertAfterAction(new GetMOTO(), UiProcessing.class);

        // card processing changes
        this.removeAction(InputCashback.class);
        this.removeAction(DCCProcessing.class);
        this.insertAfterAction(new CheckBINRange(), CheckCUP.class);
        this.insertAfterAction(new ConstrainedAction(new CheckOfflineAllowed(), new ConstrainedAction.IsCaptureMethod(TCard.CaptureMethod.ICC)), InterfaceSelected.class);

        // declined part changes
        this.insertAfterAction(new CheckPostCommsReferral(), TransactionDecliner.class);
    }

}
