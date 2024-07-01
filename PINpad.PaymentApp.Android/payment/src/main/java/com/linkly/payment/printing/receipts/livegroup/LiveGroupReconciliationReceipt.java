package com.linkly.payment.printing.receipts.livegroup;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.payment.printing.receipts.generic.ReconciliationReceipt;

public class LiveGroupReconciliationReceipt extends ReconciliationReceipt {

    public LiveGroupReconciliationReceipt(IMessages.ReportType reportType) {
        super(reportType);
    }

    @Override
    public void addSettlementDate(PrintReceipt receipt, TransRec tran, PrintReceipt.FONT font) {
        LiveGroupBaseReceipt.addBatch(receipt, tran);
    }
}

