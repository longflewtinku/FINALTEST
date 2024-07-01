package com.linkly.payment.printing.receipts.livegroup;

import com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.payment.printing.receipts.common.BaseReceipt;

import timber.log.Timber;

public class LiveGroupBaseReceipt extends BaseReceipt {
    private static final String TAG = LiveGroupBaseReceipt.class.getSimpleName();

    /**
     * Populates the {@link PrintReceipt} object with transaction fields
     *
     * @param receipt  object to be populated
     * @param transRec transaction to be used
     * @return {@link PrintReceipt} populated object
     */

    protected PrintReceipt populateTransactionLines(PrintReceipt receipt, TransRec transRec) {
        String procCode = "00";
        try {
            procCode = As2805EftexUtils.packProcCode(transRec).getTranType();
        } catch (Exception e) {
            Timber.w(e);
        }
        addBatch(receipt, transRec);
        addStan(receipt, transRec);
        addRRN(receipt, transRec);
        addAuthCode(receipt, transRec);
        addCardData(receipt, transRec);
        addAmounts(receipt, transRec, procCode);
        addAuthResult(receipt, transRec);
        addMOTOType(receipt, transRec);
        addDateTime(receipt, transRec);
        addVerificationDetails(receipt, transRec);
        addReversalText(receipt, transRec);
        addReversalDateTime(receipt, transRec);
        addFooter(receipt);
        addEmvDiagnosticData(receipt, transRec);

        return receipt;
    }

}
