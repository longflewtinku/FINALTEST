package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.SIG;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.UIScreenDef.ATM_ONLY;
import static com.linkly.libui.UIScreenDef.CASH_NOT_ALLOW;
import static com.linkly.libui.UIScreenDef.CASH_ONLY_CARD;
import static com.linkly.libui.UIScreenDef.INSERT_CARD;

import com.linkly.libconfig.ProfileCfg;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libui.IUIDisplay;

import timber.log.Timber;

public class CheckSVCCode extends IAction {
    @Override
    public String getName() {
        return "CheckSVCCode";
    }


    @Override
    public void run() {
        TCard cardinfo = trans.getCard();

        // Warning, this code is never used as part of an action but rather in MagstripeProcessing.java action.
        // Removed any need restriction of presentation type checking (i.e swiped/ICC etc) This code was incorrectly checking presentation type anyway.

        if (ProfileCfg.getInstance().isDemo()) {
            return;
        }

        if (cardinfo.isManual()) {
            Timber.i( "No service code check for manual transactions");
            return;
        }

        try {
            if (!cardinfo.getCardsConfig(d.getPayCfg()).isServiceCodeCheck()) {
                Timber.i( "No service code check for transactions with svc code checking off");
                return;
            }
        } catch (NullPointerException e) {
            return;
        }

        if (!cardinfo.processServiceCodes(d)) {
            Timber.i( "No service code check for cards that don't processs the service code");
            return;
        }

        if (cardinfo.isAtmOnlySC()) {
            ui.showScreen(ATM_ONLY);
            ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }

        Timber.i( "Check Service Codes");
        if (cardinfo.isIccCardSC() && cardinfo.isEmvAllowed() && !CoreOverrides.get().isIgnoreServiceCodes()) {
            cardinfo.setMsrAllowed(false);
            cardinfo.setCtlsAllowed(false);
            cardinfo.setEmvAllowed(true);
            d.getWorkflowEngine().setNextAction(InitialProcessing.class);

            ui.showScreen(INSERT_CARD);
            ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
            cardinfo.invalidateCard();
            d.getWorkflowEngine().setNextAction(InitialProcessing.class);
        }

        if (trans.isSale() && cardinfo.getCardsConfig(d.getPayCfg()).isForceOffline() && !cardinfo.isOnlineSC()) {
            cardinfo.setCvmType(SIG);
        }

        if (trans.isCash()) {
            if (!cardinfo.isCashAllowedSC()) {
                ui.showScreen(CASH_NOT_ALLOW);
                ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            }
        } else {
            if (cardinfo.isCashOnlySC()) {
                ui.showScreen(CASH_ONLY_CARD);
                ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            }
        }
    }
}
