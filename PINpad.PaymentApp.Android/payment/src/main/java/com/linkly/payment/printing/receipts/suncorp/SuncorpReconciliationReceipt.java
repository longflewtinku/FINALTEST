package com.linkly.payment.printing.receipts.suncorp;

import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;

import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.printing.PrintReceipt;

public class SuncorpReconciliationReceipt extends SuncorpReportReceipt {

    public SuncorpReconciliationReceipt(boolean zReport, boolean full) {
        super(zReport, full);
    }

    public PrintReceipt generateReceipt(Object obj) {
        TransRec trans = (TransRec)obj;
        Reconciliation rec = reconciliationDao.findByTransId( trans.getUid() );
        if( rec != null ) {
            return super.generateReceipt(rec, trans);
        } else {
            return null;
        }
    }
}

