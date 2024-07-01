package com.linkly.libengine.action.HostActions;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.DEFERRED_AUTH;
import static com.linkly.libui.IUIDisplay.String_id.STR_DEFER_AUTH;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionDeferred;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libui.IUIDisplay;

public class CheckDeferredAuth extends IAction {
    @Override
    public String getName() {
        return "CheckDeferredAuth";
    }

    @Override
    public void run() {
        if (!d.getPayCfg().isDeferredAuthEnabled())
            return;

        //If the hostResponse is NO_RESPONSE, it will ask user if they want to defer the Auth
        if (trans.getProtocol().getHostResult() == HostResult.NO_RESPONSE || trans.getProtocol().getHostResult() == HostResult.CONNECT_FAILED ) {
            int maxDeferredAuthValue = d.getPayCfg().getMaxDeferredAuthValue();
            int maxDeferredAuthCount = d.getPayCfg().getMaxDeferredAuthCount();
            int deferredAuthCount = getDeferredAuthCount();
            //If they select yes then mark the msgStatus as a new value called “DEFERRED_AUTH”
            // We must also check the configs to check we haven't done too many offline etc
            if (trans.getAmounts().getTotalAmount() <= maxDeferredAuthValue && deferredAuthCount < maxDeferredAuthCount) {
                if (UIHelpers.uiYesNoQuestion(d, trans.getTransType().getDisplayName(), d.getPrompt(STR_DEFER_AUTH) + "?", IUIDisplay.LONG_TIMEOUT)) {
                    d.getWorkflowEngine().setNextAction(TransactionDeferred.class);
                }
            } else {
                return;
            }

        }
    }

    private int getDeferredAuthCount() {
        //db lookup to do deferred transaction
        return  TransRecManager.getInstance().getTransRecDao().countDeferredCount(DEFERRED_AUTH.ordinal());
    }
}
