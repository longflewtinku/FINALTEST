package com.linkly.libengine.action.check;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.reporting.ShiftTotalsReport;
import com.linkly.libengine.engine.transactions.TransRec;

public class CheckSubTotals extends IAction {
    @Override
    public String getName() {
        return "CheckSubTotals";
    }

    @Override
    public void run() {
        this.checkSubTotals(trans);
    }

    private void checkSubTotals(TransRec trans) {

        ShiftTotalsReport shiftTotalsReport = new ShiftTotalsReport(context);
        trans.setShiftTotals( shiftTotalsReport.getSubTotalsRecord() );

        if( trans.getShiftTotals() == null ) {
            // Sub Totals not found
            d.getProtocol().setInternalRejectReason( trans, IProto.RejectReasonType.TRANSACTION_NOT_FOUND );
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
        }
    }

}