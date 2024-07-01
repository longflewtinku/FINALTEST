package com.linkly.libengine.workflow;

import static com.linkly.libpositive.PosIntegrate.ResultResponse.RES_INVALID_REPORT_TYPE;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libpositive.events.PositiveTransEvent;

/**
 * Action to be used for unsupported auto txn types. This will send "B2|Unsupported Function" to POS
 */
public class NotSupportedAutoReport extends IAction {

    @Override
    public String getName() {
        return "NotSupportedAutoReport";
    }

    @Override
    public void run() {
        PositiveTransEvent transEvent = null;
        if (d.getCurrentTransaction() != null) {
            transEvent = d.getCurrentTransaction().getTransEvent();
        }
        ECRHelpers.ipcSendNullReportResponse(d, transEvent, RES_INVALID_REPORT_TYPE, context);
    }
}
