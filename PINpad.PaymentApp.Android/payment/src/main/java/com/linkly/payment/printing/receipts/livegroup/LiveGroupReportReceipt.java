package com.linkly.payment.printing.receipts.livegroup;

import com.linkly.libpositive.messages.IMessages;
import com.linkly.payment.printing.receipts.generic.ReportReceipt;

public class LiveGroupReportReceipt extends ReportReceipt {

    public LiveGroupReportReceipt(IMessages.ReportType reportType) {
        super(reportType);
    }
}
