package com.linkly.libengine.engine.reporting;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.env.ReceiptNumber;

import java.util.Collections;
import java.util.List;

public class History {

    public Reconciliation generateHistory() {
        boolean last = true;   // true when we are searching for the last (most recent) transaction that goes to the reconciliation

        // create Reconciliation for TransRec
        Reconciliation history = new Reconciliation();
        history.setReceiptNumber(ReceiptNumber.getNewValue());

        List<TransRec> allTrans = TransRec.getLastXTransactionsList( null, "cancelled = 0" );

        if (allTrans == null) {
            /*No Data */
            return history;
        }

        Collections.reverse(allTrans);  // reverse the order so the last  would come first

        // calculate totals (we exit the loop if we find previous reconciliation)
        for (TransRec trans : allTrans) {

            // update the last transID
            if (last) {
                history.setEndTran(trans.getProtocol().getStan());
                history.setEndReceiptTran(trans.getAudit().getReceiptNumber());
                last = false;
            }

            // update the first transID
            history.setStartTran(trans.getProtocol().getStan());
            history.setStartReceiptTran(trans.getAudit().getReceiptNumber());

            // add transaction to the list for receipt
            history.getRecTransList().add(trans);
        }

        return history;
    }


}
