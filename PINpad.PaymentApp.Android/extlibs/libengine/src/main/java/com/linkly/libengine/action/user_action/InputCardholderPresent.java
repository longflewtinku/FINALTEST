package com.linkly.libengine.action.user_action;

import static com.linkly.libui.UIScreenDef.IS_CARDHOLDER_PRESENT;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.helpers.UIHelpers;

public class InputCardholderPresent extends IAction {

    @Override
    public String getName() {
        return "InputCardholderPresent";
    }

    @Override
    public void run() {
        TransRec trans = d.getCurrentTransaction();
        UIHelpers.YNQuestion resp =  UIHelpers.uiYesNoCancelQuestion(d, IS_CARDHOLDER_PRESENT, null);
        if (resp == UIHelpers.YNQuestion.YES || resp == UIHelpers.YNQuestion.NO ) {
            trans.getCard().setCardholderPresent(resp == UIHelpers.YNQuestion.YES );
        } else if(resp == UIHelpers.YNQuestion.CANCEL) {
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }
    }

}
