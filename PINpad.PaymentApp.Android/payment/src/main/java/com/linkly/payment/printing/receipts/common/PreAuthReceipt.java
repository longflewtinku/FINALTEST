package com.linkly.payment.printing.receipts.common;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUIDisplay.String_id;

public class PreAuthReceipt extends Receipt {

    @SuppressWarnings("static")
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
        this.addTransName(receipt, getText(String_id.STR_PRE_AUTHORISATION), null);
        this.addSpaceLine(receipt);

        //TODO  DCC Receipting Not Included ATM
        this.addAmountFields(d, receipt, tran);
        this.addSpaceLine(receipt);

        if (tran.isApproved()) {
            this.addLineCentered(receipt, getText(String_id.STR_PLS_AUTHORISE), LARGE_FONT);
            this.addLineCentered(receipt, getText(String_id.STR_MY_ACCOUNT), LARGE_FONT);
            this.addSpaceLine(receipt);
        }

        //TODO Check condition
        this.addVerificationDetails(receipt, tran, SMALL_FONT);

        this.addAuthCode(receipt, tran);

        if (tran.isApproved()) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(String_id.STR_PREAUTH_DECLARATION)));
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(String_id.STR_FUND_AUTH_TEXT)));
        }

        this.addAuthenticationResult(receipt, tran);

        //Customer reference
        this.addCustomerReference(receipt, tran);

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
