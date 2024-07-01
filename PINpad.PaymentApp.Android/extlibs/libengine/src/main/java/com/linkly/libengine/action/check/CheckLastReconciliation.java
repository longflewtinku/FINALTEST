package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.reporting.ReconciliationManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;

public class CheckLastReconciliation extends IAction {
    @Override
    public String getName() {
        return "CheckReconciliation";
    }

    @Override
    public void run() {
        this.checkLastReconciliation(trans);
    }

    private void checkLastReconciliation(TransRec reconciliationTrans) {
        ReconciliationManager.getInstance(); // create instance if not already done

        // find newest reconciliation
        TransRec latest = TransRecManager.getInstance().getTransRecDao().getLatestByTransType(RECONCILIATION);

        if (latest != null) {
            // Echo host result from the reconciliation transaction to show the appropriate result in the receipt
            reconciliationTrans.getProtocol().setHostResult(latest.getProtocol().getHostResult());
            reconciliationTrans.setApproved(latest.isApproved());  // Last settlement approved flag should be same as the last "settlement" transction approved flag
            // set reconciliation data to transaction from the reconciliation record
            reconciliationTrans.setReconciliation(reconciliationDao.findByTransId(latest.getUid()));
        }
    }
}
