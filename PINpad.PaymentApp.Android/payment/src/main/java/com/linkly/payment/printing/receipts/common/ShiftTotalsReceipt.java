package com.linkly.payment.printing.receipts.common;

import static com.linkly.libui.IUIDisplay.String_id.STR_CARD_SCHEME_NET_TOTALS;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASH;
import static com.linkly.libui.IUIDisplay.String_id.STR_COMPLETION;
import static com.linkly.libui.IUIDisplay.String_id.STR_EXCLUDING_SURCHARGE;
import static com.linkly.libui.IUIDisplay.String_id.STR_GRATUITY;
import static com.linkly.libui.IUIDisplay.String_id.STR_INCLUSIVE_OF;
import static com.linkly.libui.IUIDisplay.String_id.STR_NET_TOTAL;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRE_AUTH;
import static com.linkly.libui.IUIDisplay.String_id.STR_REFUND;
import static com.linkly.libui.IUIDisplay.String_id.STR_REPRINT_SHIFT_TOTALS;
import static com.linkly.libui.IUIDisplay.String_id.STR_SALE;
import static com.linkly.libui.IUIDisplay.String_id.STR_SHIFT_TOTALS;
import static com.linkly.libui.IUIDisplay.String_id.STR_SUB_TOTALS;
import static com.linkly.libui.IUIDisplay.String_id.STR_SURCHARGE;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTAL;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_NOT_RESET;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_REPRINT;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_RESET;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.reporting.ShiftTotals;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libui.IUICurrency;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import timber.log.Timber;

public class ShiftTotalsReceipt extends BaseReceipt {

    public final IMessages.ReportType reportType;

    public ShiftTotalsReceipt(IMessages.ReportType reportType) { this.reportType = reportType; }

    @Override
    public PrintReceipt generateReceipt( Object obj ) {

        TransRec trans = ( TransRec ) obj;

        if (trans == null) {
            return null;
        }

        ShiftTotals shiftTotals = trans.getShiftTotals();
        PrintReceipt receipt = super.generateReceipt(trans); // generate standard header

        addFromToLines(receipt, shiftTotals);

        this.addSpaceLines(receipt, 2);

        switch (reportType) {
            case SubShiftTotalsReport:
                Receipt.addLineCentered(receipt, getText(STR_SUB_TOTALS), LARGE_FONT);
                break;
            case ShiftTotalsReport:
                Receipt.addLineCentered(receipt, getText(STR_SHIFT_TOTALS), LARGE_FONT);
                break;
            case ReprintShiftTotalsReport:
                Receipt.addLineCentered(receipt, getText(STR_REPRINT_SHIFT_TOTALS), LARGE_FONT);
                break;
            default:
                // unhandled report type
                Timber.i("%s report type is not handled", reportType.toString());
        }

        this.addSpaceLines(receipt, 2);

        addTotalsLines(receipt, shiftTotals);

        addTipsSurchargeTotalsLines(receipt, shiftTotals);

        addSchemeDataLines(receipt,shiftTotals);

        switch (reportType) {
            case SubShiftTotalsReport:
                Receipt.addLineCentered(receipt, getText(STR_TOTALS_NOT_RESET));
                break;
            case ShiftTotalsReport:
                addBoxedText(receipt, new String[]{getText(STR_TOTALS_RESET)});
                break;
            case ReprintShiftTotalsReport:
                addBoxedText(receipt, new String[]{getText(STR_TOTALS_REPRINT)});
                break;
            default:
                // unhandled report type
                Timber.i("%s report type is not handled", reportType.toString());
        }

        return receipt;
    }

    private void addFromToLines(PrintReceipt receipt, ShiftTotals shiftTotals) {

        String formattedFrom = shiftTotals.getTotalsFrom()>0 ? formatDateTime(new Date(shiftTotals.getTotalsFrom()), "dd/MM/yy HH:mm zzz") : "Not Set";
        String formattedTo = formatDateTime(new Date( shiftTotals.getTotalsTo()>0 ? shiftTotals.getTotalsTo(): System.currentTimeMillis()), "dd/MM/yy HH:mm zzz");
        receipt.getLines().add(new PrintReceipt.PrintLine("TOTALS FROM:", MEDIUM_FONT));
        receipt.getLines().add(new PrintReceipt.PrintLine(formattedFrom, MEDIUM_FONT));
        receipt.getLines().add(new PrintReceipt.PrintLine("TO:", MEDIUM_FONT));
        receipt.getLines().add(new PrintReceipt.PrintLine(formattedTo, MEDIUM_FONT));
    }

    private void addTotalsLines(PrintReceipt receipt, ShiftTotals shiftTotals) {

        this.addCountAndAmount(receipt, STR_SALE, shiftTotals.getSaleCount(), shiftTotals.getSaleAmount());
        this.addCountAndAmount(receipt, STR_CASH, shiftTotals.getCashCount(), shiftTotals.getCashAmount());
        this.addCountAndAmount(receipt, STR_REFUND, shiftTotals.getRefundCount(), shiftTotals.getRefundAmount(), shiftTotals.getRefundAmount() > 0);
        receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_PRE_AUTH).toUpperCase(), MEDIUM_FONT));
        this.addCountAndAmount(receipt, STR_COMPLETION, shiftTotals.getCompletionCount(), shiftTotals.getCompletionAmount());
        this.addCountAndAmount(receipt, STR_NET_TOTAL, shiftTotals.getTotalCount(), shiftTotals.getTotalAmount());
        this.addSpaceLines(receipt, 2);
    }

    private void addTipsSurchargeTotalsLines(PrintReceipt receipt, ShiftTotals shiftTotals) {

        if (d.getPayCfg() != null && d.getCustomer() != null ) {
            boolean tipsAllowed = ( d.getCustomer().supportTipsOnReports() && d.getPayCfg().isTipAllowed() );
            boolean surchargeAllowed = d.getPayCfg().isSurchargeSupported();

            if (tipsAllowed || shiftTotals.getTipCount()>0 || surchargeAllowed || shiftTotals.getSurchargeCount()>0 ) {

                receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_INCLUSIVE_OF), MEDIUM_FONT));

                if (tipsAllowed || shiftTotals.getTipCount() > 0) {
                    this.addCountAndAmount(receipt, STR_GRATUITY, shiftTotals.getTipCount(), shiftTotals.getTipAmount());
                }

                if (surchargeAllowed || shiftTotals.getSurchargeCount() > 0) {
                    this.addCountAndAmount(receipt, getText(STR_SURCHARGE), shiftTotals.getSurchargeCount(), shiftTotals.getSurchargeAmount());
                }

                this.addSpaceLines(receipt, 1);
            }
        }
        this.addCountAndAmount(receipt, getText(STR_TOTAL).toUpperCase(), shiftTotals.getTotalCount(), shiftTotals.getTotalAmount()-shiftTotals.getSurchargeAmount());
        if( shiftTotals.getSurchargeCount()>0 ) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_EXCLUDING_SURCHARGE), MEDIUM_FONT));
        }
        this.addSpaceLines(receipt, 2);
    }

    private void addSchemeDataLines(PrintReceipt receipt, ShiftTotals shiftTotals) {

        final int NAME_MAX_LENGTH = 12;
        final int LINE_MAX_LENGTH = 26;

        Gson gson = new Gson();
        Type cardSchemeTotalsType = new TypeToken<ArrayList<Reconciliation.CardSchemeTotals>>(){}.getType();
        ArrayList<Reconciliation.CardSchemeTotals> schemeTotals =  gson.fromJson(shiftTotals.getSchemeTotals(), cardSchemeTotalsType);

        if (receipt == null || (schemeTotals == null || schemeTotals.isEmpty()))
            return;

        receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_CARD_SCHEME_NET_TOTALS), MEDIUM_FONT));

        boolean addSpaceLines = false;
        for (Reconciliation.CardSchemeTotals total : schemeTotals) {
            String amount = d.getFramework().getCurrency().formatAmount("" + total.totalAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());

            if (total.name.length() > NAME_MAX_LENGTH || total.name.length() + amount.length() > LINE_MAX_LENGTH) {
                receipt.getLines().add(new PrintReceipt.PrintLine(total.name.toUpperCase() + ":", MEDIUM_FONT));
                this.addCountAndAmount(receipt, "", total.totalCount, total.totalAmount);
            } else {
                this.addCountAndAmount(receipt, total.name, total.totalCount, total.totalAmount);
            }

            addSpaceLines = true;
        }
        if (addSpaceLines) {
            this.addSpaceLines(receipt, 2);
        }
    }

    private void addBoxedText(PrintReceipt receipt, String[] prompts) {

        receipt.getLines().add(new PrintReceipt.PrintFillLine('*'));

        for (String prompt : prompts) {
            String[] lines = prompt.split("\n");
            for (String line : lines) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine("*", line, "*"));
            }
        }

        receipt.getLines().add(new PrintReceipt.PrintFillLine('*'));
    }

}

