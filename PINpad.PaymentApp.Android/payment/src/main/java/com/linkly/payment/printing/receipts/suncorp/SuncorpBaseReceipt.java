package com.linkly.payment.printing.receipts.suncorp;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;

public class SuncorpBaseReceipt extends Receipt {

    @Override
    // common header for all suncorp receipts
    public PrintReceipt generateReceipt(Object obj) {
        TransRec trans = (TransRec) obj;
        PrintReceipt receipt = new PrintReceipt();

        this.addHeader(receipt);
        this.addDateTimeLineLeft(receipt, trans, MEDIUM_FONT);
        this.addMaskedMID(receipt, trans, MEDIUM_FONT);
        this.addMaskedTID(receipt, trans, MEDIUM_FONT);
        this.addStan(receipt,trans,MEDIUM_FONT);
        return receipt;
    }
}
