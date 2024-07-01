package com.linkly.libengine.workflow;

import com.linkly.libengine.action.IPC.PopulateTransaction;
import com.linkly.libengine.action.IPC.TempLogin;
import com.linkly.libengine.action.IPC.TempLogout;
import com.linkly.libengine.action.IPC.TransResponse;
import com.linkly.libengine.action.IPC.UserUndoUpgrade;
import com.linkly.libengine.action.cardprocessing.DCCProcessing;
import com.linkly.libengine.action.cardprocessing.FallbackRemoveCard;
import com.linkly.libengine.action.check.TimeNeedsFixing;
import com.linkly.libengine.action.user_action.InputAmount;
import com.linkly.libengine.action.user_action.InputCashback;
import com.linkly.libengine.action.user_action.InputReference;
import com.linkly.libengine.action.user_action.MainMenu;

public class AutoCash extends CommonPayment {

    public AutoCash() {

        // initial processing changes
        this.removeAction(InputAmount .class);
        this.removeAction(InputCashback .class);
        //this.removeAction(InputTip.class); // Commented out now in auto sale as a POS can request a Tip new flag passed in Trans event request.
        this.removeAction(InputReference .class);

        this.insertAfterAction(new PopulateTransaction(), FallbackRemoveCard.class);
        this.insertAfterAction(new TempLogin(), PopulateTransaction.class);

        //card processing changes
        this.removeAction(DCCProcessing .class);

        //finishing state changes
        this.removeAction(UserUndoUpgrade .class);
        this.removeAction(TimeNeedsFixing .class);
        this.removeAction(MainMenu .class);

        this.insertAfterAction(new TempLogout(), TransResponse.class);
    }

}
