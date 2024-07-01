package com.linkly.payment.workflows.generic;

import static com.linkly.libengine.engine.EngineManager.TransType.AUTOSETTLEMENT;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION_AUTO;

import com.linkly.libengine.action.IAction;

import timber.log.Timber;

public class ActCheckAutoSettlement extends IAction {
    @Override
    public String getName() { return "ActCheckAutoSettlement"; }

    @Override
    public void run() {
        if (trans.getTransType() == AUTOSETTLEMENT) {
            Timber.e("AutoSettlement started");
            trans.setReconciliationOriginalTransType(trans.getTransType());
            trans.setPrintTransactionListing(d.getPayCfg().isAutoSettlementPrintTransactionListing());
            try {
                trans.setAutoSettlementRetryCount(Integer.parseInt(d.getPayCfg().getAutoSettlementRetryCount()));
            } catch (NumberFormatException ex) {
                Timber.e("format error");
                trans.setAutoSettlementRetryCount(0);
            }
            trans.setTransType(RECONCILIATION);
        } else if(trans.getTransType() == RECONCILIATION || trans.getTransType() == RECONCILIATION_AUTO) {
            Timber.e("Manual Settlement started");
        }
    }
}
