package com.linkly.payment.printing.receipts.common;

import static com.linkly.libui.IUIDisplay.String_id.STR_TOTAL;
import static com.linkly.libui.IUIDisplay.String_id.STR_USER_ID;
import static com.linkly.libui.IUIDisplay.String_id.STR_USER_REPORT;

import com.linkly.libengine.engine.reporting.UserReport;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUICurrency;

public class UserReportReceipt extends Receipt {


    @Override
    public PrintReceipt generateReceipt(Object obj) {
        UserReport report = (UserReport) obj;
        PrintReceipt receipt = new PrintReceipt();

        this.addHeader(receipt);
        this.addUserDetails(receipt);
        this.addPosIdAndReceiptNumber(receipt, null);
        this.addVersion(receipt);
        this.addMID(receipt);
        this.addSpaceLine(receipt);

        addLineCentered(receipt, "----"+getText(STR_USER_REPORT)+"----", SMALL_FONT);

        this.addSpaceLine(receipt);

        //Report Content
        int i;
        for (i = 0; i < report.getUserDataItems().size(); i++) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_USER_ID).toUpperCase()+":", report.getUserDataItems().get(i).getUserId(), report.getUserDataItems().get(i).getUserName()));
            receipt.getLines().add(new PrintReceipt.PrintFillLine('.'));
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_TOTAL).toUpperCase()+":", "(" + report.getUserDataItems().get(i).getCount() + ")", d.getFramework().getCurrency().formatAmount(report.getUserDataItems().get(i).getAmount() + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            receipt.getLines().add(new PrintReceipt.PrintFillLine('.'));

            this.addSpaceLine(receipt);
        }

        //Date and time
        addDateTimeLine(receipt);

        this.addSpaceLine(receipt);


        return receipt;
    }
}
