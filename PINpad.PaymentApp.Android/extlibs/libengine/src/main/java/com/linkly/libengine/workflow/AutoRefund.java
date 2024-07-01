package com.linkly.libengine.workflow;

import com.linkly.libengine.action.ConstrainedAction;
import com.linkly.libengine.action.IPC.PopulateTransaction;
import com.linkly.libengine.action.IPC.TempLogin;
import com.linkly.libengine.action.IPC.TempLogout;
import com.linkly.libengine.action.IPC.TransResponse;
import com.linkly.libengine.action.IPC.UserUndoUpgrade;
import com.linkly.libengine.action.InterfaceSelected;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.cardprocessing.DCCProcessing;
import com.linkly.libengine.action.cardprocessing.FallbackRemoveCard;
import com.linkly.libengine.action.cardprocessing.GetCard;
import com.linkly.libengine.action.check.CheckBINRange;
import com.linkly.libengine.action.check.CheckCUP;
import com.linkly.libengine.action.check.CheckOfflineAllowed;
import com.linkly.libengine.action.check.CheckOfflineLimits;
import com.linkly.libengine.action.check.CheckPassword;
import com.linkly.libengine.action.check.CheckPostCommsReferral;
import com.linkly.libengine.action.check.CheckRefundLimits;
import com.linkly.libengine.action.check.CheckUserLevel;
import com.linkly.libengine.action.check.TimeNeedsFixing;
import com.linkly.libengine.action.user_action.InputAmount;
import com.linkly.libengine.action.user_action.InputCashback;
import com.linkly.libengine.action.user_action.InputReference;
import com.linkly.libengine.action.user_action.InputTip;
import com.linkly.libengine.action.user_action.MainMenu;
import com.linkly.libengine.engine.transactions.properties.TCard;

public class AutoRefund extends CommonPayment {
    public AutoRefund() {

        // initial processing changes

        this.removeAction(InputAmount.class);
        this.removeAction(InputTip.class);
        this.removeAction(InputReference.class);
        this.insertAfterAction(new CheckPassword(), CheckUserLevel.class);

        this.insertAfterAction(new PopulateTransaction(), FallbackRemoveCard.class);
        this.insertAfterAction(new TempLogin(), PopulateTransaction.class);
        this.insertBeforeAction(new CheckOfflineLimits(), GetCard.class);
        this.insertBeforeAction( new CheckRefundLimits(), GetCard.class );

        //card processing changes
        this.removeAction(InputCashback.class);
        this.removeAction(DCCProcessing.class);

        this.insertAfterAction(new CheckBINRange(), CheckCUP.class);
        this.insertAfterAction(new ConstrainedAction(new CheckOfflineAllowed(), new ConstrainedAction.IsCaptureMethod(TCard.CaptureMethod.ICC)), InterfaceSelected.class);

        //declined part
        this.insertAfterAction(new CheckPostCommsReferral(), TransactionDecliner.class);

        //finishig state changes
        this.removeAction(UserUndoUpgrade.class);
        this.removeAction(TimeNeedsFixing.class);
        this.removeAction(MainMenu.class);
        this.insertAfterAction(new TempLogout(), TransResponse.class);
    }
}
