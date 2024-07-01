package com.linkly.libengine.workflow;

import static com.linkly.libpositive.PosIntegrate.ResultResponse.RES_TRANSACTION_TYPE_NOT_FOUND;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libpositive.events.PositiveTransEvent;

/**
 * Action to be used for unsupported auto txn types. This will send "B2|Unsupported Function" to POS
 */
public class NotSupportedAutoTxn extends IAction {

    @Override
    public String getName() {
        return "NotSupportedAutoTxn";
    }

    @Override
    public void run() {
        PositiveTransEvent transEvent = null;
        if (d.getCurrentTransaction() != null) {
            transEvent = d.getCurrentTransaction().getTransEvent();
        }
        ECRHelpers.ipcSendNullTransResponse(d, transEvent, RES_TRANSACTION_TYPE_NOT_FOUND, context);
    }
}
