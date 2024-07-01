package com.linkly.payment.printing.receipts.demo;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libmal.global.printing.PrintReceipt;

public class DemoReversalReceipt extends DemoBaseReceipt {

    @Override
    public PrintReceipt generateReceipt(Object obj) {
        TransRec reversalTran = (TransRec) obj;
        TransRec originalTran = getOriginalTransaction(reversalTran);

        if (originalTran == null) {
            return this.generateInvalidReceipt();
        }

        PrintReceipt receipt = super.generateReceipt(reversalTran); // generate standard header

        return super.populateTransactionLines(receipt, reversalTran);
    }

    @Override
    public void addStan(PrintReceipt receipt, TransRec tran) {
        // STAN of original transaction to be shown as per requirement
        super.addStan(receipt, getOriginalTransaction(tran));
    }

    @Override
    protected void addDateTime(PrintReceipt receipt, TransRec tran) {
        // DateTime of original transaction to be used
        super.addDateTime(receipt, getOriginalTransaction(tran));
    }

    private TransRec getOriginalTransaction(TransRec reversalTran) {
        return TransRecManager.getInstance().getTransRecDao().getByReceiptNumber(
                reversalTran.getAudit().getReversalReceiptNumber());
    }
}
