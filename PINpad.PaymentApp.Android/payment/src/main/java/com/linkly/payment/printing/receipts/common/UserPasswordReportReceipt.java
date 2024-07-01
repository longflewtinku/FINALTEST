package com.linkly.payment.printing.receipts.common;

import static com.linkly.libui.IUIDisplay.String_id.STR_PASSWORD_RESET;
import static com.linkly.libui.IUIDisplay.String_id.STR_USER_ID;
import static com.linkly.libui.IUIDisplay.String_id.STR_USER_INITIAL_PASSCODE;
import static com.linkly.libui.IUIDisplay.String_id.STR_USER_NAME;

import com.linkly.libengine.engine.reporting.UserPasswordData;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;

public class UserPasswordReportReceipt extends Receipt {

    @Override
    public PrintReceipt generateReceipt(Object obj) {
        UserPasswordData report = (UserPasswordData) obj;
        PrintReceipt receipt = new PrintReceipt();

        this.addHeader(receipt);
        this.addUserDetails(receipt);
        this.addPosIdAndReceiptNumber(receipt, null);
        this.addVersion(receipt);
        this.addMID(receipt);
        this.addSpaceLine(receipt);

        addLineCentered(receipt, "----"+getText(STR_PASSWORD_RESET)+"----", SMALL_FONT);
        this.addSpaceLine(receipt);

        //Report Content
        receipt.getLines().add(new PrintReceipt.PrintFillLine('.'));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_USER_ID).toUpperCase() +":" ,"", report.getUserId()));
        this.addSpaceLine(receipt);
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_USER_NAME).toUpperCase(),"" ,report.getUserName()));
        this.addSpaceLine(receipt);
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_USER_INITIAL_PASSCODE).toUpperCase()+":","", report.getPassword()));
        this.addSpaceLine(receipt);

        receipt.getLines().add(new PrintReceipt.PrintFillLine('.'));


        //Date and time
        addDateTimeLine(receipt);

        this.addSpaceLine(receipt);


        return receipt;
    }
}
