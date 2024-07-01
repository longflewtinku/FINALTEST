package com.linkly.payment.printing.receipts.generic;

import com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsUtils;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.printing.PrintReceipt;

import timber.log.Timber;

public class GenericBaseReceipt extends com.linkly.payment.printing.receipts.common.BaseReceipt {



    /**
     * Populates the {@link PrintReceipt} object with transaction fields
     * This will get complicated if I have fields that needs to sit in between the below lines
     *
     * @param receipt  object to be populated
     * @param transRec transaction to be used
     * @return {@link PrintReceipt} populated object
     */
    protected PrintReceipt populateTransactionLines( PrintReceipt receipt, TransRec transRec ) {
       String procCode = "00";
        try {
            procCode = As2805WoolworthsUtils.packProcCode(transRec).getTranType();
        } catch (Exception e) {
            Timber.w(e);
        }
        addBatch(receipt, transRec);
        addStan(receipt, transRec);
        addRRN(receipt, transRec);
        addAuthCode(receipt, transRec);
        addCardData( receipt, transRec );
        addAmounts( receipt, transRec ,procCode);
        addAuthResult( receipt, transRec );
        addMOTOType(receipt, transRec);
        addDateTime(receipt, transRec);
        addVerificationDetails( receipt, transRec );
        addFooter( receipt );
        addEmvDiagnosticData( receipt, transRec );

        return receipt;
    }

}
