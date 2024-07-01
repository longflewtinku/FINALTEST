package com.linkly.payment.printing.receipts.demo;

import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;

import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libpositive.messages.IMessages;

@SuppressWarnings("java:S110") // ("This class has 6 parents..") - suppressing as its existing architecture
public class DemoReconciliationReceipt extends DemoReportReceipt {

    public DemoReconciliationReceipt(IMessages.ReportType reportType) {
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

