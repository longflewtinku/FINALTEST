package com.linkly.libengine.action.IPC;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libpositive.messages.IMessages;

public class SendReportResponse extends IAction {
    @Override
    public String getName() {
        return "SendReportResponse";
    }

    @Override
    public void run() {
        String reportType;

        switch(trans.getTransType())
        {
            case SUMMARY_AUTO:
                reportType = IMessages.ReportType.XReport.toString();
                break;
            case LAST_RECONCILIATION_AUTO:
                reportType = IMessages.ReportType.LastReconciliationReport.toString();
                break;
            case RECONCILIATION_AUTO:
            case SUB_TOTALS_AUTO:
            case SHIFT_TOTALS_AUTO:
            default:
                reportType = IMessages.ReportType.ZReport.toString();
                break;
        }

        ECRHelpers.ipcSendReportResponse(d, trans, trans.getReconciliation(), reportType, context);
    }
}
