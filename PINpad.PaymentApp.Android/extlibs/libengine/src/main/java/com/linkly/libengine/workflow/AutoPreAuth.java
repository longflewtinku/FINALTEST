package com.linkly.libengine.workflow;

import com.linkly.libengine.action.HostActions.PreAuthorisation;
import com.linkly.libengine.action.IPC.CheckExistingPreAuth;
import com.linkly.libengine.action.IPC.PopulateTransaction;
import com.linkly.libengine.action.IPC.TempLogin;
import com.linkly.libengine.action.IPC.TempLogout;
import com.linkly.libengine.action.IPC.TransResponse;
import com.linkly.libengine.action.cardprocessing.DCCProcessing;
import com.linkly.libengine.action.cardprocessing.FallbackRemoveCard;
import com.linkly.libengine.action.check.TimeNeedsFixing;
import com.linkly.libengine.action.user_action.InputAmount;
import com.linkly.libengine.action.user_action.InputCashback;
import com.linkly.libengine.action.user_action.InputReference;
import com.linkly.libengine.action.user_action.InputTip;
import com.linkly.libengine.action.user_action.MainMenu;

public class AutoPreAuth extends CommonPayment {
    public AutoPreAuth() {

        // initial processing changes

        this.removeAction(InputAmount.class);
        this.removeAction(InputTip.class);
        this.removeAction(InputReference.class);

        this.insertAfterAction(new PopulateTransaction(), FallbackRemoveCard.class);
        this.insertAfterAction(new TempLogin(), PopulateTransaction.class);

        //card processing changes
        this.removeAction(InputCashback.class);
        this.removeAction(DCCProcessing.class);

        this.insertBeforeAction(new CheckExistingPreAuth(), PreAuthorisation.class);

        //finishig state changes
        this.removeAction(TimeNeedsFixing.class);
        this.removeAction(MainMenu.class);
        this.insertAfterAction(new TempLogout(), TransResponse.class);
    }
}
