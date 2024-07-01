package com.linkly.libengine.workflow;

import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.cardprocessing.FallbackRemoveCard;
import com.linkly.libengine.action.cardprocessing.GetCard;
import com.linkly.libengine.action.cardprocessing.ManualProcessing;
import com.linkly.libengine.action.check.CheckPostCommsReferral;
import com.linkly.libengine.action.user_action.DisplayCNP;
import com.linkly.libengine.action.user_action.DisplaySummary;
import com.linkly.libengine.action.user_action.InputAmount;
import com.linkly.libengine.action.user_action.InputCashback;
import com.linkly.libengine.action.user_action.InputReference;
import com.linkly.libengine.action.user_action.InputTip;

public class CNPSale extends CommonPayment {
    public CNPSale() {
        //this is where the differences will be added in respect to the common payment


        this.removeAction(FallbackRemoveCard.class);

        this.removeAction(InputAmount.class);
        this.removeAction(InputTip.class);
        this.removeAction(InputReference.class);
        this.removeAction(ManualProcessing.class);
        this.insertAfterAction(new DisplayCNP(), GetCard.class);
        this.removeAction(GetCard.class);

        this.removeAction(InputCashback.class);
        this.removeAction(DisplaySummary.class);

        this.insertAfterAction(new CheckPostCommsReferral(), TransactionDecliner.class);

    }
}
