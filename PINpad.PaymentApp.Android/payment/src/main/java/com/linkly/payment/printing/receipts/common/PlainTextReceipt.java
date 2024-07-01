package com.linkly.payment.printing.receipts.common;

import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;

public class PlainTextReceipt extends Receipt {


    @Override
    public PrintReceipt generateReceipt(Object obj) {
        String text = (String) obj;
        PrintReceipt receipt = new PrintReceipt();

        this.addHeader(receipt);
        this.addUserDetails(receipt);
        this.addPosIdAndReceiptNumber(receipt, null);
        this.addVersion(receipt);
        this.addMID(receipt);
        this.addSpaceLine(receipt);

        this.addTransName(receipt, "INFO", null);
        this.addSpaceLine(receipt);

        /*TODO - Add all the Transaction Details */
        receipt.getLines().add(new PrintReceipt.PrintLine(text));
        this.addSpaceLine(receipt);

        /*Date and time*/
        this.addDateTimeLine(receipt);


        return receipt;

    }

}
