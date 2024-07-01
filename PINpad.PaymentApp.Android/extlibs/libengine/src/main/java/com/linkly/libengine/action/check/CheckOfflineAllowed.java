package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.RTIME_OVER_FLOOR;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.UIScreenDef.OFFLINE_TRANS_NOT_ALLOW;

import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libui.IUIDisplay;

import timber.log.Timber;

public class CheckOfflineAllowed extends IAction {
    @Override
    public String getName() {
        return "CheckOfflineAllowed";
    }

    @Override
    public void run() {
        checkOfflineAllowed();
    }

    // assume you are going online
    // so we go offline if allowed
    // if there is anything insisting we go online then we throw an error if its not possible
    // set a flag to say it is an authOffline transaction
    private void checkOfflineAllowed() {
        TProtocol protocol = trans.getProtocol();
        CardProductCfg cardsConfig = trans.getCard().getCardsConfig(d.getPayCfg());

        /* always default to going online */
        protocol.setCanAuthOffline(false);

        /* if its an online service code but an offline transaction then we cant do it */
        if (trans.isCompletion()) {
            Timber.i( "Completion trans type, skipping service code check");
        } else {
            if (trans.getCard().isOnlineSC()) {
                trans.getAudit().setReasonOnlineCode(TAudit.ReasonOnlineCode.RTIME_FORCED_CARD_ISSUER);
                if (trans.isOfflineDependency(d.getPayCfg())) {
                    ui.showScreen(OFFLINE_TRANS_NOT_ALLOW);
                    ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    return;
                }
            }

            /* specific error for sales that are forced offline and not allowed */
            if (trans.isSale() && cardsConfig.isForceOffline() && trans.isForceOnlineRequired(d.getPayCfg())) {
                ui.showScreen(OFFLINE_TRANS_NOT_ALLOW);
                ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            }

            /* some cvms must go online so we cant allow them offline */
            if (trans.isForceOnlineRequired(d.getPayCfg())) {
                return;
            }
        }

        /* lastly if its an offline transaction we allow it offline */
        if (trans.isOfflineTransaction(d.getPayCfg())) {
            Timber.i( "isOfflineTransaction=true, setting setCanAuthOffline=true");
            protocol.setCanAuthOffline(true);
        } else {
            /* bit dangerous for now so going to disable whilst developing until someone really wants it */
            if (checkBelowFloorLimits()) {
                protocol.setCanAuthOffline(false);
            } else {
                trans.getAudit().setReasonOnlineCode(RTIME_OVER_FLOOR);
            }
        }
    }
    private boolean checkBelowFloorLimits() {

        TCard cardinfo = trans.getCard();
        CardProductCfg cardsCfg = cardinfo.getCardsConfig(d.getPayCfg());
        int floor = cardsCfg.getLimits().getFloor();

        if (trans.isCash()) {
            floor = cardsCfg.getLimits().getCashFloor();
        }

        if (floor == 0) {
            return false;
        }

        return trans.getAmounts().getTotalAmount() <= (floor * TAmounts.getAmountCurExMul(d.getPayCfg().getCurrencyNum()));
    }
}
