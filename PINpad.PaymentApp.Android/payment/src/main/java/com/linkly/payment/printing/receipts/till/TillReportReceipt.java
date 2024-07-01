package com.linkly.payment.printing.receipts.till;

import static com.linkly.libmal.global.printing.PrintReceipt.PrintLine.TextAlignment.RIGHT;
import static com.linkly.libui.IUIDisplay.String_id.STR_APPROVED;
import static com.linkly.libui.IUIDisplay.String_id.STR_CARD_TYPE_TOTALS;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASH_AMOUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASH_CAPS;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASH_COUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_DECLINED;
import static com.linkly.libui.IUIDisplay.String_id.STR_ENQUIRY_FAILED;
import static com.linkly.libui.IUIDisplay.String_id.STR_LAST_SETTLEMENT;
import static com.linkly.libui.IUIDisplay.String_id.STR_NET_AMOUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRE_SETTLEMENT;
import static com.linkly.libui.IUIDisplay.String_id.STR_PURCH;
import static com.linkly.libui.IUIDisplay.String_id.STR_PURCH_AMOUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_PURCH_CAPS;
import static com.linkly.libui.IUIDisplay.String_id.STR_PURCH_COUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_REFUNDS;
import static com.linkly.libui.IUIDisplay.String_id.STR_REFUNDS_CAPS;
import static com.linkly.libui.IUIDisplay.String_id.STR_REFUND_AMOUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_REFUND_COUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTLED;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTLEMENT;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTLEMENT_SUCCESS;
import static com.linkly.libui.IUIDisplay.String_id.STR_SURCHARGE;
import static com.linkly.libui.IUIDisplay.String_id.STR_SURCHARGE_TOTALS;
import static com.linkly.libui.IUIDisplay.String_id.STR_TILL_RECEIPT_ALREADY_SETTLED_MESSAGE;
import static com.linkly.libui.IUIDisplay.String_id.STR_TILL_RECEIPT_INCLUSIVE_OF;
import static com.linkly.libui.IUIDisplay.String_id.STR_TIP_TOTALS;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_IN_BALANCE;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_NOT_RESET;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_RESET;

import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.reporting.SchemeTotals;
import com.linkly.libengine.engine.reporting.TimePeriodTotals;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libui.IUICurrency;
import com.linkly.payment.workflows.till.TillReconciliationUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;


public class TillReportReceipt extends TillBaseReceipt {
    public final IMessages.ReportType reportType;

    private static final Map<String, String> SCHEMES_MESSAGE_MAP = new HashMap<>();

    static {
        SCHEMES_MESSAGE_MAP.put("04", "VISA");
        SCHEMES_MESSAGE_MAP.put("05", "MASTERCARD");
        SCHEMES_MESSAGE_MAP.put("99", "EFTPOS");
        SCHEMES_MESSAGE_MAP.put("03", "AMEX");
        SCHEMES_MESSAGE_MAP.put("33", "UPI");
        SCHEMES_MESSAGE_MAP.put("07", "DINERS");
    }

    public TillReportReceipt(IMessages.ReportType reportType) {
        this.reportType = reportType;
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

    private void printTotalsLines(TransRec trans, PrintReceipt receipt, Reconciliation localReconciliation, boolean tipsAllowed, boolean surchargeAllowed) {
        this.addSectionLine(receipt);
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_PURCH_COUNT), "",
                ""+(trans.getReconciliation().getSale().count - trans.getReconciliation().getSale().reversalCount - trans.getReconciliation().getCash().count)));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_PURCH_AMOUNT), "", d.getFramework().getCurrency().formatUIAmount(
                "" + (trans.getReconciliation().getSale().amount - trans.getReconciliation().getSale().reversalAmount - trans.getReconciliation().getCash().amount), IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()), MEDIUM_FONT));
        this.addSpaceLines(receipt, 1);

        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_CASH_COUNT), "",
                ""+(trans.getReconciliation().getCash().count - trans.getReconciliation().getCash().reversalCount)));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_CASH_AMOUNT), "", d.getFramework().getCurrency().formatUIAmount(
                "" + (trans.getReconciliation().getCash().amount - trans.getReconciliation().getCash().reversalAmount), IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()), MEDIUM_FONT));
        this.addSpaceLines(receipt, 1);

        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_REFUND_COUNT), "",
                ""+(trans.getReconciliation().getRefund().count - trans.getReconciliation().getRefund().reversalCount)));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_REFUND_AMOUNT), "", d.getFramework().getCurrency().formatUIAmount(
                "" + (trans.getReconciliation().getRefund().amount - trans.getReconciliation().getRefund().reversalAmount), IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()), MEDIUM_FONT));
        this.addSectionLine(receipt);
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_NET_AMOUNT), "", d.getFramework().getCurrency().formatUIAmount(
                "" + (trans.getReconciliation().getTotalAmount()), IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()), MEDIUM_FONT));
        this.addSpaceLines(receipt, 1);

        if (trans.getSchemeTotals() != null) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_CARD_TYPE_TOTALS), MEDIUM_FONT));
            for (SchemeTotals schemeTotals : trans.getSchemeTotals()) {
                receipt.getLines().add(new PrintReceipt.PrintLine(SCHEMES_MESSAGE_MAP.get(schemeTotals.getCardNameIndex()), MEDIUM_FONT));
                if ((schemeTotals.getDebitNumber() > 0) || (schemeTotals.getCreditNumber() > 0)) {
                    this.addCountAndAmountWithRawLabel(receipt, getText(STR_PURCH), schemeTotals.getDebitNumber(), schemeTotals.getDebitAmount(), false);
                    this.addCountAndAmountWithRawLabel(receipt, getText(STR_REFUNDS), schemeTotals.getCreditNumber(), schemeTotals.getCreditAmount(), true);
                }
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_NET_AMOUNT), d.getPayCfg().getCountryCode().getAlphaCode(), d.getFramework().getCurrency().formatUIAmount(
                        "" + (schemeTotals.getDebitAmount() - schemeTotals.getCreditAmount()), IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()), MEDIUM_FONT));
                addSchemeSurchargeLine(receipt, SCHEMES_MESSAGE_MAP.get(schemeTotals.getCardNameIndex()), localReconciliation);
                this.addSectionLine(receipt);
            }
        }

        addLineCentered(receipt, getText(STR_TOTALS), MEDIUM_FONT);
        this.addSpaceLines(receipt, 1);
        this.addCountAndAmountWithRawLabel(receipt, getText(STR_PURCH_CAPS), trans.getReconciliation().getSale().count - trans.getReconciliation().getSale().reversalCount,
                trans.getReconciliation().getSale().amount - trans.getReconciliation().getSale().reversalAmount, false);
        this.addCountAndAmountWithRawLabel(receipt, getText(STR_REFUNDS_CAPS), trans.getReconciliation().getRefund().count - trans.getReconciliation().getRefund().reversalCount,
                trans.getReconciliation().getRefund().amount - trans.getReconciliation().getRefund().reversalAmount, true);
        this.addCountAndAmountWithRawLabel(receipt, getText(STR_CASH_CAPS), trans.getReconciliation().getCash().count - trans.getReconciliation().getCash().reversalCount,
                trans.getReconciliation().getCash().amount - trans.getReconciliation().getCash().reversalAmount, false);
        this.addSectionLine(receipt);
        this.addAmountField(receipt, getText(STR_NET_AMOUNT), (trans.getReconciliation().getTotalAmount()), MEDIUM_FONT);

        addTipsSurchargeTotals(receipt, localReconciliation, tipsAllowed, surchargeAllowed);
        this.addSpaceLines(receipt, 1);
    }

    private void addTipsSurchargeTotals(PrintReceipt receipt, Reconciliation localReconciliation, boolean tipsAllowed, boolean surchargeAllowed) {
        if (tipsAllowed || surchargeAllowed) {
            receipt.getLines().add( new PrintReceipt.PrintLine( getText( STR_TILL_RECEIPT_INCLUSIVE_OF ), MEDIUM_FONT ) );
        }

        if (tipsAllowed) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_TIP_TOTALS), "", d.getFramework().getCurrency().formatUIAmount(
                    "" + (localReconciliation.getTips().amount - localReconciliation.getTips().reversalAmount), IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()), MEDIUM_FONT));
        }
        if (surchargeAllowed) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SURCHARGE_TOTALS), "", d.getFramework().getCurrency().formatUIAmount(
                    "" + (localReconciliation.getSurcharge().amount - localReconciliation.getSurcharge().reversalAmount), IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()), MEDIUM_FONT));
        }
    }

    public PrintReceipt generateReceipt(TransRec trans) {
        boolean tipsEnabled = (d.getCustomer().supportTipsOnReports() && d.getPayCfg().isTipAllowed());
        boolean surchargeEnabled = d.getPayCfg().isSurchargeSupported();

        PrintReceipt receipt = super.generateReceipt(trans); // generate standard header

        this.addSpaceLines(receipt, 2);
        addTitle(receipt);

        this.addDateTimeLineAs12HourFormat(receipt, trans, MEDIUM_FONT, false);
        this.addStanJustified(receipt, trans, MEDIUM_FONT, RIGHT);

        if (TillReconciliationUtil.printTotals(trans)) {
            printTotalsLines(trans, receipt, trans.getReconciliation(), tipsEnabled, surchargeEnabled);
        }

        addFooter(trans, receipt);

        if (TillReconciliationUtil.printTotals(trans)) {
            TimePeriodTotals timePeriodTotals = TillReconciliationUtil.generatePeriodTotals(d, trans);

            if (timePeriodTotals.getReportWindowStart() != 0 && timePeriodTotals.getReportWindowEnd() != 0) {
                // Start/end of window. Format TBD
                receipt.getLines().add(new PrintReceipt.PrintLine("Surch. and Tipping Window"));
                receipt.getLines().add(new PrintReceipt.PrintTableLine("start:", formatDateTime(new Date(timePeriodTotals.getReportWindowStart()), "dd/MM/yyyy HH:mm:ss")));
                receipt.getLines().add(new PrintReceipt.PrintTableLine("end:", formatDateTime(new Date(timePeriodTotals.getReportWindowEnd()), "dd/MM/yyyy HH:mm:ss")));
            }
        }
        return receipt;
    }

    private void addTitle(PrintReceipt receipt) {
        switch (reportType) {
            case ZReport:
                Receipt.addLineCentered(receipt, getText(STR_SETTLEMENT), LARGE_FONT);
                break;
            case XReport:
                Receipt.addLineCentered(receipt, getText(STR_PRE_SETTLEMENT), LARGE_FONT);
                break;
            case LastReconciliationReport:
                Receipt.addLineCentered(receipt, getText(STR_LAST_SETTLEMENT), LARGE_FONT);
                break;
            default:
                // unhandled report type
                Timber.i("%s report type is not handle yet", reportType.toString());
        }
    }

    private void addFooter(TransRec trans, PrintReceipt receipt) {
        String responseCode = "";
        if (!Util.isNullOrEmpty(trans.getProtocol().getServerResponseCode())) {
            responseCode = trans.getProtocol().getServerResponseCode();
        }
        if (reportType == IMessages.ReportType.LastReconciliationReport || reportType == IMessages.ReportType.XReport) {
            // Pre-settlement and Last settlement
            if (responseCode.equals("00")) {
                addLineCentered(receipt, getText(STR_APPROVED) + " " + responseCode, LARGE_FONT);
                addBoxedText(receipt, new String[]{getText(STR_TOTALS_IN_BALANCE), getText(STR_TOTALS_NOT_RESET)});
                // override display text
                trans.getProtocol().setAdditionalResponseText(getText(STR_APPROVED));
                trans.getProtocol().setPosResponseText(getText(STR_APPROVED));
            } else {
                // print error
                addLineCentered(receipt, getText(STR_DECLINED).toUpperCase() + " " + responseCode, LARGE_FONT);
                addBoxedText(receipt, new String[]{getText(STR_ENQUIRY_FAILED), getText(STR_TOTALS_NOT_RESET)});
                trans.getProtocol().setAdditionalResponseText(getText(STR_DECLINED));
                trans.getProtocol().setPosResponseText(getText(STR_DECLINED));
            }
        } else if (reportType == IMessages.ReportType.ZReport) {
            switch (responseCode) {
                case "97": // Settled
                    // 'settlement successful' in big text
                    addLineCentered(receipt, getText(STR_SETTLED) + " " + responseCode, LARGE_FONT);
                    addBoxedText(receipt, new String[]{getText(STR_TOTALS_IN_BALANCE), getText(STR_TOTALS_RESET)});
                    // override display text
                    trans.getProtocol().setAdditionalResponseText(getText(STR_SETTLEMENT_SUCCESS));
                    trans.getProtocol().setPosResponseText(getText(STR_SETTLEMENT_SUCCESS));
                    break;

                case "93": // Already Settled
                    addLineCentered(receipt, getText(STR_DECLINED).toUpperCase() + " " + responseCode, LARGE_FONT);
                    addBoxedText(receipt, new String[]{getText(STR_TILL_RECEIPT_ALREADY_SETTLED_MESSAGE)});
                    trans.getProtocol().setAdditionalResponseText(getText(STR_DECLINED));
                    trans.getProtocol().setPosResponseText(getText(STR_DECLINED));
                    break;

                case "00": // Treat it as error
                    addLineCentered(receipt, getText(STR_DECLINED).toUpperCase() + " " + responseCode, LARGE_FONT);
                    // no specific error text
                    addBoxedText(receipt, new String[]{getText(STR_TOTALS_NOT_RESET)});
                    trans.getProtocol().setAdditionalResponseText(getText(STR_DECLINED));
                    trans.getProtocol().setPosResponseText(getText(STR_DECLINED));
                    break;

                default:
                    addLineCentered(receipt, getText(STR_DECLINED).toUpperCase() + " " + responseCode, LARGE_FONT);
                    if (!Util.isNullOrEmpty(trans.getProtocol().getCardAcceptorPrinterData())) {
                        addBoxedText(receipt, new String[]{trans.getProtocol().getCardAcceptorPrinterData()});
                    } else {
                        // no specific error text
                        addBoxedText(receipt, new String[]{getText(STR_TOTALS_NOT_RESET)});
                    }
                    break;
            }
        }
    }

    private void addSchemeSurchargeLine(PrintReceipt receipt, String schemeName, Reconciliation reconciliation) {
        // Searching in local sourced Schemes totals for surcharge amount as it is not provided from Host
        if (reconciliation != null) {
            ArrayList<Reconciliation.CardSchemeTotals> schemeTotals = reconciliation.getPreviousSchemeTotalsAsArray();
            for (Reconciliation.CardSchemeTotals total : schemeTotals) {
                if (total.name.toUpperCase().equals(schemeName)) {
                    receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SURCHARGE), "", d.getFramework().getCurrency().formatUIAmount(
                            "" + total.surchargeAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()), MEDIUM_FONT));
                    return;
                }
            }
        }
    }

}
