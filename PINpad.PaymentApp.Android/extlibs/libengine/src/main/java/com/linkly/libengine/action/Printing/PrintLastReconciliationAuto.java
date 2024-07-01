package com.linkly.libengine.action.Printing;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.transactions.TransRec;

public class PrintLastReconciliationAuto extends IAction {

    @Override
    public String getName() {
        return "PrintLastReconciliationAuto";
    }

    @Override
    public void run() {
        // print receipt
        printReconciliation(trans);
    }

    private void printReconciliation(TransRec trans) {
        if (trans.getReconciliation() != null) {
            if (trans.getTransEvent() != null && trans.getTransEvent().isPosPrintingSync()) {
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
