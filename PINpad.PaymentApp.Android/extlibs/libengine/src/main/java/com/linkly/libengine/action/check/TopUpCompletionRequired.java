package com.linkly.libengine.action.check;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;

public class TopUpCompletionRequired extends IAction {
    @Override
    public String getName() {
        return "TopUpCompletionRequired";
    }

    @Override
    public void run() {
        if (trans.getTransType() == EngineManager.TransType.TOPUPPREAUTH) {
            TransRec newTrans = new TransRec(EngineManager.TransType.TOPUPCOMPLETION, d);
            d.resetCurrentTransaction(newTrans);
            newTrans.getProtocol().setAuthCode(trans.getProtocol().getAuthCode());
            newTrans.getProtocol().setAccountType(trans.getProtocol().getAccountType());
            newTrans.setCard(trans.getCard());
            newTrans.setAmounts((trans.getAmounts()));
            newTrans.setAudit(trans.getAudit());
            newTrans.setSecurity(trans.getSecurity());
            newTrans.getAmounts().setTopupAmount(0);
            /* cardReset secure data */
            newTrans.getSecurity().clearForTopupCompletions();
            d.getWorkflowEngine().setNextAction(InitialProcessing.class);
        }
    }
}
