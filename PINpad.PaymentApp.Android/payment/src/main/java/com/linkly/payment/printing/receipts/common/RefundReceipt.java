package com.linkly.payment.printing.receipts.common;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;

public class RefundReceipt extends Receipt {
    @Override
    public PrintReceipt generateReceipt(Object obj) {
        TransRec tran = (TransRec) obj;
        PrintReceipt receipt = new PrintReceipt();

        this.addHeader(receipt);
        this.addUserDetails(receipt);
        this.addPosIdAndReceiptNumber(receipt, tran.getAudit().getReceiptNumber());
        this.addVersion(receipt);
        this.addMID(receipt);
        this.addSpaceLine(receipt);

        // Add the Card holder details
        this.addCardDetails(receipt, tran);
        this.addCardHolder(receipt, tran);

        this.addSpaceLine(receipt);
        this.addTransName(receipt, "REFUND", null);
        this.addSpaceLine(receipt);

        this.addAmountFields(d, receipt, tran);

        this.addSpaceLine(receipt);

        this.addDebitOrCreditText(receipt, false, tran);
        ;

        this.addVerificationDetails(receipt, tran, SMALL_FONT);

        this.addAuthCode(receipt, tran);

        this.addAuthenticationResult(receipt, tran);

        /*Date and time*/
        this.addDateTimeLine(receipt, tran);

        this.addTransFooter(receipt, tran);

        this.addSpaceLine(receipt);

        // Any Promotional / Additional data not related to the transaction
        this.addPromoLines(receipt);
        this.addHelpLines(receipt);

        this.addFooter(receipt);

        if (isMerchantCopy()) {
            addIccDiags(receipt, tran);
        }

        this.addDigitalSignatureSection(receipt, tran, MEDIUM_FONT);
        return receipt;
    }
}
