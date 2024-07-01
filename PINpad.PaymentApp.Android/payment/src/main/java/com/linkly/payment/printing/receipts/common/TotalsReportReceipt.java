package com.linkly.payment.printing.receipts.common;

import static com.linkly.libui.IUIDisplay.String_id.STR_COUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_FULL_VERSION;
import static com.linkly.libui.IUIDisplay.String_id.STR_SHORT_VERSION;
import static com.linkly.libui.IUIDisplay.String_id.STR_SUB_TOTAL;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.reporting.Totals;
import com.linkly.libengine.engine.reporting.TotalsReport;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay.String_id;

public class TotalsReportReceipt extends Receipt {

    @SuppressWarnings("static")
    @Override
    public PrintReceipt generateReceipt(Object obj) {
        TotalsReport report = (TotalsReport) obj;
        PrintReceipt receipt = new PrintReceipt();
        PrintReceipt.PrintLine line;
        PayCfg paycfg = d.getPayCfg();
        long rptCount = 0;
        long rptTotal = 0;

        this.addHeader(receipt);
        this.addUserDetails(receipt);
        this.addPosIdAndReceiptNumber(receipt, null);
        this.addVersion(receipt);
        this.addMID(receipt);
        this.addSpaceLine(receipt);

        this.addSpaceLine(receipt);

        this.addLineCentered(receipt, getText(STR_TOTALS), LARGE_FONT);
        if (report.isFullReport()) {
            this.addLineCentered(receipt, getText(STR_FULL_VERSION), LARGE_FONT);
        } else {
            this.addLineCentered(receipt, getText(STR_SHORT_VERSION), LARGE_FONT);
        }

        this.addSpaceLine(receipt);


        receipt.getLines().add(new PrintReceipt.PrintFillLine('.'));
        this.addLineCentered(receipt, paycfg.getCurrencyCode(), LARGE_FONT);

        receipt.getLines().add(new PrintReceipt.PrintFillLine('.'));

        /*Report  Content*/
        for (int i = 0; i < report.getTotalsDataItems().size(); i++) {
            long grpCount = 0;
            long grpTotal = 0;
            this.addSpaceLines(receipt, 2);
            TotalsReport.TotalsGroup group = report.getTotalsDataItems().get(i);
            /*Get all Transactions totals for this card */
            line = new PrintReceipt.PrintLine(group.getCardName(), SMALL_FONT);
            receipt.getLines().add(line);
            receipt.getLines().add(new PrintReceipt.PrintTableLine(" ", getText(STR_COUNT), getText(String_id.STR_AMOUNT_CAPS)));
            for (Totals data : group.getTransTotals()) {
                if (data.getTransType().includeInReconciliation) {
                    receipt.getLines().add(new PrintReceipt.PrintTableLine(data.getTransType().getDisplayName(), "(" + data.getNetCount() + ")", d.getFramework().getCurrency().formatAmount(data.getNetAmount() + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
                    grpCount += data.getNetCount();
                    grpTotal += data.getNetAmount();

                    if (data.isGroupTotal()) {
                        rptCount += data.getNetCount();
                        rptTotal += data.getNetAmount();
                    }
                }
            }

            receipt.getLines().add(new PrintReceipt.PrintFillLine('.'));
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SUB_TOTAL), "(" + grpCount + ")", d.getFramework().getCurrency().formatAmount(grpTotal + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            this.addSpaceLine(receipt);


        }

        receipt.getLines().add(new PrintReceipt.PrintFillLine('.'));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_TOTALS), "(" + rptCount + ")", d.getFramework().getCurrency().formatAmount(rptTotal + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
        this.addSpaceLine(receipt);

        /*Date and time*/
        addDateTimeLine(receipt);

        this.addSpaceLine(receipt);

        return receipt;
    }
}
