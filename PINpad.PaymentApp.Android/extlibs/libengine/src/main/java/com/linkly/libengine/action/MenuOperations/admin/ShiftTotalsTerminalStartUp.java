package com.linkly.libengine.action.MenuOperations.admin;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.reporting.ShiftTotalsReport;

public class ShiftTotalsTerminalStartUp extends IAction {

    @Override
    public String getName() {
        return "ShiftTotalsTerminalStartUp";
    }

    @Override
    public void run() {
        ShiftTotalsReport shiftTotalsReport = new ShiftTotalsReport(context);
        shiftTotalsReport.terminalStartUp();
    }
}
