package com.linkly.libengine.workflow;

import com.linkly.libengine.action.HostActions.PreAuthorisation;
import com.linkly.libengine.action.IPC.CheckExistingPreAuth;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.cardprocessing.DCCProcessing;
import com.linkly.libengine.action.check.CheckPostCommsReferral;
import com.linkly.libengine.action.user_action.InputCashback;

public class PreAuth extends CommonPayment {
    public PreAuth() {
        //card processing changes
        this.removeAction(InputCashback.class);
        this.removeAction(DCCProcessing.class);

        this.insertBeforeAction(new CheckExistingPreAuth(), PreAuthorisation.class);

        //declined part
        this.insertAfterAction(new CheckPostCommsReferral(), TransactionDecliner.class);
    }
}
