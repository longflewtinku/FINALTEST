package com.linkly.payment.printing.receipts.livegroup;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.printing.PrintReceipt;

public class LiveGroupCashoutReceipt extends LiveGroupBaseReceipt {

    @Override
    public PrintReceipt generateReceipt( Object obj ) {
        TransRec trans = ( TransRec ) obj;
        PrintReceipt receipt = super.generateReceipt( trans ); // generate standard header

        return super.populateTransactionLines( receipt, trans );
    }
}
