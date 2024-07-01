package com.linkly.payment.workflows.till;

import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.reporting.DailyBatch;
import com.linkly.libengine.engine.reporting.IDailyBatch;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;

public class TillCheckReconciliation extends IAction {
    @Override
    public String getName() {
        return "TillCheckReconciliation";
    }

    @Override
    public void run() {
        this.checkReconciliation(trans);
    }

    private void checkReconciliation(TransRec reconciliationTrans) {
        // There is no need to show the correct Totals in request message for Till,
        // use the same calculation for every Settlement type (Settlement, Pre-Settlement, Last Settlement)
        // just to create Reconciliation record
        IDailyBatch dailyBatch = new DailyBatch();
        // calculate totals, without marking trans records as reconciled
        Reconciliation reconciliationTotals = dailyBatch.generateDailyBatch(false, d);
        reconciliationTrans.setReconciliation(reconciliationTotals);
    }
}
