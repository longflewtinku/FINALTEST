package com.linkly.payment.printing.receipts.generic;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.printing.PrintReceipt;

public class RefundReceipt extends GenericBaseReceipt {
    @Override
    public PrintReceipt generateReceipt( Object obj ) {
        TransRec trans = ( TransRec ) obj;
        PrintReceipt receipt = super.generateReceipt( trans );

        return super.populateTransactionLines( receipt, trans );
    }
}