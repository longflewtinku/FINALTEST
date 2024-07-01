package com.linkly.payment.printing.receipts.common;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;

public class CashBackReceipt extends Receipt {
    @Override
    public PrintReceipt generateReceipt(Object obj) {
        TransRec tran = (TransRec) obj;

        PrintReceipt receipt = new PrintReceipt();
        PrintReceipt.PrintLine line;

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
        this.addTransName(receipt, "PURCHASE WITH", "CASHBACK");
        this.addSpaceLine(receipt);

        //TODO  DCC Receipting Not Included ATM
        this.addAmountFields(d, receipt, tran);
        this.addSpaceLine(receipt);

        this.addDebitOrCreditText(receipt, true, tran);

        //TODO Check condition
        this.addVerificationDetails(receipt, tran, SMALL_FONT);

        if (tran.isApproved()) {
            //TODO Auth Code
            receipt.getLines().add(new PrintReceipt.PrintLine("AUTH CODE: " + tran.getProtocol().getAuthCode()));
            this.addSpaceLine(receipt);
        }

        //TODO This is all a bit suspect -
        this.addAuthenticationResult(receipt, tran);

        this.addCustomerReference(receipt, tran);

        // Add the account data
        this.addAccount(receipt, tran);

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
