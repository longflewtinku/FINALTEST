package com.linkly.libengine.action.DB;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.reporting.ShiftTotalsReport;

public class DBUpdateShiftTotals extends IAction {

    @Override
    public String getName() {
        return "DBUpdateShiftTotals";
    }

    @Override
    public void run() {
        if( trans.isFinalised() ) {
            ShiftTotalsReport shiftTotalsReport = new ShiftTotalsReport(context);
            shiftTotalsReport.addTransactionToTotals(trans);
        }
    }
}
