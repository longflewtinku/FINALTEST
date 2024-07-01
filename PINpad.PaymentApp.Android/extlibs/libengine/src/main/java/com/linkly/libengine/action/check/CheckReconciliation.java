package com.linkly.libengine.action.check;

import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_SETTLEMENT_STARTED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.reporting.DailyBatch;
import com.linkly.libengine.engine.reporting.IDailyBatch;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.config.EnvCfg;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckReconciliation extends IAction {
    @Override
    public String getName() {
        return "CheckReconciliation";
    }

    @Override
    public void run() {

        this.checkCup(trans);
    }

    private void checkCup(TransRec trans) {
        this.checkReconciliation(trans);
    }

    private void checkReconciliation(TransRec reconciliationTrans) {
        IDailyBatch dailyBatch = null;

        d.getStatusReporter().reportStatusEvent(STATUS_SETTLEMENT_STARTED , trans.isSuppressPosDialog());

        dailyBatch = new DailyBatch();

        Reconciliation reconciliationTotals = dailyBatch.generateDailyBatch(true, d);
        reconciliationTrans.setReconciliation(reconciliationTotals);

        Date date = new Date();
        // TODO: Need to implement Locale here or use a string resource
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String dateString = sdf.format(date);

        EnvCfg.getInstance().storeValue("apay.recDate", dateString);
    }
}
