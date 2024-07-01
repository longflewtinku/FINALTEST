package com.linkly.payment.printing.receipts.common;

import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUIDisplay.String_id;

public class ReversalReceipt extends Receipt {

    @SuppressWarnings("static")
    @Override
    public PrintReceipt generateReceipt(Object obj) {
        // Todo: currently basing an assumption that we are passing in a valid object. Maybe some protective prog?
        TransRec reversalTran = (TransRec) obj;
        // Anything from a DB can pass back null.
        TransRec originalTran = TransRecManager.getInstance().getTransRecDao().getByReceiptNumber(reversalTran.getAudit().getReversalReceiptNumber());
        PrintReceipt receipt = new PrintReceipt();

        // Invalid data has been passed in will generate an invalid receipt (Maybe something more detailed later?)
        if (originalTran == null) {
            return this.generateInvalidReceipt();
        }

        this.addHeader(receipt);
        this.addUserDetails(receipt);
        this.addPosIdAndReceiptNumber(receipt, reversalTran.getAudit().getReceiptNumber());
        this.addVersion(receipt);
        this.addMID(receipt);
        this.addVirtualDetails(receipt, reversalTran);
        this.addSpaceLine(receipt);

        // Add the Card holder details
        this.addCardDetails(receipt, originalTran);
        this.addCardHolder(receipt, originalTran);

        this.addSpaceLine(receipt);

        if (originalTran.getTransType() == EngineManager.TransType.DEPOSIT) {
            this.addLineCentered(receipt, getText(String_id.STR_DEPOSIT_REVERSAL), LARGE_FONT);
        } else if (originalTran.getTransType() == EngineManager.TransType.REFUND) {
            this.addLineCentered(receipt, getText(String_id.STR_REFUND_REVERSAL), LARGE_FONT);
        } else {
            this.addLineCentered(receipt, getText(String_id.STR_REVERSAL).toUpperCase(), LARGE_FONT);
        }

        this.addReceiptNumber(receipt, originalTran);
        this.addSpaceLine(receipt);

        this.addAmountFields(d, receipt, originalTran);

        this.addSpaceLine(receipt);

        if (originalTran.getTransType() == EngineManager.TransType.DEPOSIT) {
            this.addDebitOrCreditText(receipt, true, originalTran);
        } else if (originalTran.getTransType() == EngineManager.TransType.REFUND) {
            this.addDebitOrCreditText(receipt, true, originalTran);
        } else {
            this.addDebitOrCreditText(receipt, false, originalTran);
        }

        this.addSpaceLine(receipt);

        this.addAuthenticationResult(receipt, reversalTran);

        /*Date and time*/
        addDateTimeLine(receipt, reversalTran, SMALL_FONT, false);

        if (d.getPayCfg().isIncludedOrginalStandInRec()) {
            this.addRRN(receipt, reversalTran, MEDIUM_FONT);
        } else {
            this.addRRN(receipt, originalTran, MEDIUM_FONT);
        }

        this.addAccountType(receipt, originalTran);

        this.addSpaceLine(receipt);

        // Any Promotional / Additional data not related to the transaction
        this.addPromoLines(receipt);
        this.addHelpLines(receipt);

        this.addFooter(receipt);

        return receipt;
    }
}
