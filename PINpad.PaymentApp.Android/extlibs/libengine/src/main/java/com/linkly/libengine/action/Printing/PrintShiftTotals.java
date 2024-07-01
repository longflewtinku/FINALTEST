package com.linkly.libengine.action.Printing;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.transactions.TransRec;

public class PrintShiftTotals extends IAction {

    @Override
    public String getName() {
        return "PrintShiftTotals";
    }

    @Override
    public void run() {
        // print receipt
        printShiftTotals(trans);
    }

    private void printShiftTotals(TransRec trans) {
        if (trans.getShiftTotals() != null) {
            if (trans.getTransEvent() != null && trans.getTransEvent().isPosPrintingSync() && trans.getTransType().autoTransaction) {
                PrintFirst.buildAndBroadcastReceipt(d, trans, PrintFirst.ReceiptType.SETTLEMENT, false, context, mal);

                if (trans.isPrintOnTerminal()) {
                    trans.print(d, true, false, mal);
                }
            } else {
                trans.print(d, true, false, mal);
            }
        }
    }
}
