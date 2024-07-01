package com.linkly.payment.workflows.suncorp;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.reporting.DailyBatch;
import com.linkly.libengine.engine.reporting.IDailyBatch;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.config.EnvCfg;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ActSuncorpCheckReconciliation extends IAction {
    @Override
    public String getName() {
        return "ActSuncorpCheckReconciliation";
    }

    @Override
    public void run() {
        this.checkReconciliation(trans);
    }

    private void checkReconciliation(TransRec reconciliationTrans) {
        IDailyBatch dailyBatch = new DailyBatch();

        // calculate totals, without marking trans records as reconciled
        Reconciliation reconciliationTotals = dailyBatch.generateDailyBatch(false, d);

        reconciliationTrans.setReconciliation(reconciliationTotals);
        Date date = new Date();
        // TODO: Need to implement Locale here or use a string resource
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String dateString = sdf.format(date);

        EnvCfg.getInstance().storeValue("apay.recDate", dateString);
    }
}
