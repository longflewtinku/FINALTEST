package com.linkly.libengine.workflow;

import com.linkly.libengine.action.DB.DBSave;
import com.linkly.libengine.action.Printing.PrintSecond;
import com.linkly.libengine.action.cardprocessing.DCCProcessing;
import com.linkly.libengine.action.check.TopUpCompletionRequired;
import com.linkly.libengine.action.user_action.InputCashback;

public class TopUp extends CommonPayment {
    public TopUp() {
        //card processing changes
        this.removeAction(InputCashback.class);
        this.removeAction(DCCProcessing.class);


        //approved
        this.removeAction(DBSave.class);
        this.insertAfterAction(new TopUpCompletionRequired(), PrintSecond.class);
        this.addAction(new PrintSecond());

    }
}
