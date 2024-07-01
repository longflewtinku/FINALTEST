package com.linkly.payment.printing.receipts.till;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libpositive.messages.IMessages;

public class TillReconciliationReceipt extends TillReportReceipt {

    public TillReconciliationReceipt(IMessages.ReportType reportType) {
        super(reportType);
    }

    @Override
    public PrintReceipt generateReceipt(Object obj) {
        TransRec trans = (TransRec) obj;

        return super.generateReceipt(trans);
    }
}

