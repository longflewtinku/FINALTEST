package com.linkly.libengine.action.check;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.reporting.ShiftTotalsReport;
import com.linkly.libengine.engine.transactions.TransRec;

public class CheckReprintShiftTotals extends IAction {
    @Override
    public String getName() {
        return "CheckReprintShiftTotals";
    }

    @Override
    public void run() {
        this.checkReprintShiftTotals(trans);
    }

    private void checkReprintShiftTotals(TransRec trans) {

        ShiftTotalsReport shiftTotalsReport = new ShiftTotalsReport(context);
        trans.setShiftTotals( shiftTotalsReport.getPreviousShiftTotalsRecord() );

        if( trans.getShiftTotals() == null ) {
            // Sub Totals not found
            d.getProtocol().setInternalRejectReason( trans, IProto.RejectReasonType.TRANSACTION_NOT_FOUND );
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
        }
    }

}