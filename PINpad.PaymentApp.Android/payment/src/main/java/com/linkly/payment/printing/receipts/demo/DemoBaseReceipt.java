package com.linkly.payment.printing.receipts.demo;

import static com.linkly.libui.IUIDisplay.String_id.STR_RECEIPT_DEMO_MODE;

import com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.payment.printing.receipts.common.BaseReceipt;

import timber.log.Timber;

public class DemoBaseReceipt extends BaseReceipt {

    @Override
    // common header for all Demo receipts
    public PrintReceipt generateReceipt(Object obj) {
        TransRec trans = (TransRec) obj;
        PrintReceipt receipt = new PrintReceipt();

        addDemoModeWarning(receipt);
        /* Disclaimer */
        addDisclaimer(receipt);
        addHeader(receipt, trans);
        return receipt;
    }

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
        addDemoModeWarning(receipt);

        return receipt;
    }


    void addDemoModeWarning(PrintReceipt receipt) {
        addLineCentered(receipt, getText(STR_RECEIPT_DEMO_MODE), LARGE_FONT, true);
    }

}
