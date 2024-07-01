package com.linkly.payment.printing.receipts.generic;

import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;

import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libpositive.messages.IMessages;

public class ReconciliationReceipt extends ReportReceipt {

    public ReconciliationReceipt(IMessages.ReportType reportType) {
        super(reportType);
    }

    @Override
    public PrintReceipt generateReceipt(Object obj) {
        TransRec trans = (TransRec) obj;
        Reconciliation rec;
        // Settlement
        switch (reportType) {
            case ZReport:
                rec = reconciliationDao.findByTransId(trans.getUid());
                break;
            case LastReconciliationReport:
                rec = reconciliationDao.findByTransId(trans.getReconciliation().getTransID());
                break;
            case XReport:
            default:
                rec = trans.getReconciliation();
                break;
        }
        if (rec != null) {
            return super.generateReceipt(rec, trans);
        } else {
            return null;
        }
    }
}

