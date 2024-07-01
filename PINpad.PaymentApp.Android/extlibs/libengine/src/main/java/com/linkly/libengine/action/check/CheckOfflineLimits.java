package com.linkly.libengine.action.check;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.OfflineLimitsCheck;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libui.UIScreenDef;

public class CheckOfflineLimits extends IAction {

    @Override
    public String getName() {
        return "CheckOfflineLimits";
    }

    @Override
    public void run() {
        if (trans.isStartedInOfflineMode()) {
            OfflineLimitsCheck limitCheck = new OfflineLimitsCheck(d, trans, new OfflineLimitsCheck.OfflineLimitsCheckCallback() {
                @Override
                public void transCountExceeded() {
                    ui.showScreen(UIScreenDef.OFFLINE_TRANS_COUNT_LIMIT_EXCEEDED);
                    d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.OFFLINE_TRANS_COUNT_LIMIT_EXCEEDED);
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                }

                @Override
                public void transValueExceeded() {
                    ui.showScreen(UIScreenDef.TOTAL_OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED);
                    d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.TOTAL_OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED);
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                }

                @Override
                public void softLimitCountExceeded() {
                    UIHelpers.uiShowDismissableScreen(d, UIScreenDef.OFFLINE_TRANS_CLOSE_TO_LIMIT);
                }

                @Override
                public void softLimitValueExceeded() {
                    UIHelpers.uiShowDismissableScreen(d, UIScreenDef.OFFLINE_TRANS_CLOSE_TO_LIMIT);
                }

                @Override
                public void transactionLimitExceeded() {
                    // ignore this. CheckAmounts does the same check prior to card presentation
                }

            });
            limitCheck.execute();
        }
    }

}
