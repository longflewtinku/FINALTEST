package com.linkly.payment.printing.receipts.common;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUIDisplay.String_id;

public class CashReceipt extends Receipt {
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

        //TODO GET THIS A STRING
        this.addTransName(receipt, getText(String_id.STR_CASH).toUpperCase(), null);
        this.addSpaceLine(receipt);

        //TODO  DCC Receipting Not Included ATM
        this.addAmountFields(d, receipt, tran);
        this.addSpaceLine(receipt);

        this.addDebitOrCreditText(receipt, true, tran);

        //TODO Check condition
        this.addVerificationDetails(receipt, tran, SMALL_FONT);

        this.addAuthCode(receipt, tran);

        //TODO This is all a bit suspect -
        this.addAuthenticationResult(receipt, tran);

        //Customer reference
        this.addCustomerReference(receipt, tran);

        // Add the account name
        this.addAccount(receipt, tran);

        /*Date and time*/
        this.addDateTimeLine(receipt, tran);

        // Add the UTI RRN and account type
        this.addTransFooter(receipt, tran);

        if (tran.getCard().isIccCaptured()) {
            addIccFooter(receipt, tran);
        }

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
