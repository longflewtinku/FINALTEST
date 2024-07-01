package com.linkly.payment.workflows.generic;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.Printing.PrintFirst;
import com.linkly.libengine.engine.transactions.TransRec;

public class ActPrintSummary extends IAction {

    private void printReconciliation(TransRec trans) {
        if(trans.getTransEvent() != null && trans.getTransEvent().isPosPrintingSync()) {
            PrintFirst.buildAndBroadcastReceipt(d, trans, PrintFirst.ReceiptType.SETTLEMENT, false, context, mal);

            if(trans.isPrintOnTerminal()) {
                trans.print(d, true, false, mal);
            }
        } else {
            trans.print(d, true, false, mal);
        }
    }

    @Override
    public String getName() {
        return "ActPrintSummary";
    }

    @Override
    public void run() {
        // print receipt
        printReconciliation(trans);
    }
}
