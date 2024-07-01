package com.linkly.payment.printing.receipts.demo;

import static com.linkly.libui.IUIDisplay.String_id.STR_CARD_SCHEME_NET_TOTALS;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASH;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASHBACK_ONLY;
import static com.linkly.libui.IUIDisplay.String_id.STR_COMPLETION;
import static com.linkly.libui.IUIDisplay.String_id.STR_GRATUITY;
import static com.linkly.libui.IUIDisplay.String_id.STR_HOST_AUTO_SETTLED;
import static com.linkly.libui.IUIDisplay.String_id.STR_INCLUSIVE_OF;
import static com.linkly.libui.IUIDisplay.String_id.STR_LAST_SETTLEMENT_REPORT;
import static com.linkly.libui.IUIDisplay.String_id.STR_NET_TOTAL;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRE_AUTH;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRE_SETTLEMENT_REPORT;
import static com.linkly.libui.IUIDisplay.String_id.STR_REFUND;
import static com.linkly.libui.IUIDisplay.String_id.STR_SALE;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTLEMENT_FAILED;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTLEMENT_REPORT;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTLEMENT_SUCCESS;
import static com.linkly.libui.IUIDisplay.String_id.STR_SURCHARGE;
import static com.linkly.libui.IUIDisplay.String_id.STR_SYSTEM_ERROR;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_IN_BALANCE;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_NOT_RESET;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_OUT_OF_BALANCE;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_RESET;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libui.IUICurrency;

import java.util.ArrayList;

import timber.log.Timber;


public class DemoReportReceipt extends DemoBaseReceipt {
    public final IMessages.ReportType reportType;

    public DemoReportReceipt(IMessages.ReportType reportType) {
        this.reportType = reportType;
    }

    private void addBoxedText( PrintReceipt receipt, String[] prompts ) {
        receipt.getLines().add(new PrintReceipt.PrintFillLine('*'));

        for( String prompt : prompts ) {
            String[] lines = prompt.split("\n" );
            for( String line : lines ) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine("*", line, "*"));
            }
        }

        receipt.getLines().add(new PrintReceipt.PrintFillLine('*'));
    }

    @SuppressWarnings("java:S3776") // Cognitive complexity
    public PrintReceipt generateReceipt(Object obj, TransRec trans) {
        Reconciliation rec = (Reconciliation) obj;
        PrintReceipt receipt = super.generateReceipt(trans); // generate standard header

        // settlement date/batch number
        this.addSettlementDate(receipt,trans,MEDIUM_FONT);

        this.addSpaceLines(receipt, 2);

        switch (reportType) {
            case ZReport:
                Receipt.addLineCentered(receipt, getText(STR_SETTLEMENT_REPORT), LARGE_FONT);
                break;
            case XReport:
                Receipt.addLineCentered(receipt, getText(STR_PRE_SETTLEMENT_REPORT), LARGE_FONT);
                break;
            case LastReconciliationReport:
                Receipt.addLineCentered(receipt, getText(STR_LAST_SETTLEMENT_REPORT), LARGE_FONT);
                break;
            default:
                // unhandled report type
                Timber.i("%s report type is not handle yet", reportType.toString());
        }

        this.addSpaceLines(receipt, 2);

        long saleCount = rec.getSale().count - rec.getSale().reversalCount;
        long saleAmount = rec.getSale().amount - rec.getSale().reversalAmount;

        long cashCount = rec.getCash().count - rec.getCash().reversalCount;
        long cashAmount = rec.getCash().amount - rec.getCash().reversalAmount;

        long refundCount = rec.getRefund().count - rec.getRefund().reversalCount;
        long refundAmount = rec.getRefund().amount - rec.getRefund().reversalAmount;

        long preAuthCompleteCount = rec.getCompletion().count - rec.getCompletion().reversalCount;
        long preAuthCompleteAmount = rec.getCompletion().amount - rec.getCompletion().reversalAmount;

        this.addCountAndAmount(receipt, STR_SALE, saleCount, saleAmount);
        this.addCountAndAmount(receipt, STR_CASH, cashCount, cashAmount);
        this.addCountAndAmount(receipt, STR_REFUND, refundCount, refundAmount);
        // MD: To adjust in one line the length of string needs to be below 15 as based on length it will insert blank spaces in PrintLine method
        //     so introduced two different strings for Pre-Auth Completion as it exceeds length of 15 characters so the logic of printing other receipts not change.
        receipt.getLines().add( new PrintReceipt.PrintLine( getText( STR_PRE_AUTH ).toUpperCase(), MEDIUM_FONT ) );
        this.addCountAndAmount(receipt, STR_COMPLETION, preAuthCompleteCount, preAuthCompleteAmount);
        this.addCountAndAmount(receipt, STR_NET_TOTAL, saleCount + cashCount + refundCount + preAuthCompleteCount, (saleAmount + cashAmount + preAuthCompleteAmount - refundAmount));
        this.addSpaceLines(receipt, 2);


        if( d.getPayCfg() != null && d.getCustomer() != null ) {
            boolean cashBackAllowed = d.getPayCfg().isCashBackAllowed();
            boolean tipsAllowed = ( d.getCustomer().supportTipsOnReports() && d.getPayCfg().isTipAllowed() );
            boolean surchargeAllowed = d.getPayCfg().isSurchargeSupported();

            if( cashBackAllowed || tipsAllowed || surchargeAllowed ) {
                receipt.getLines().add( new PrintReceipt.PrintLine( getText( STR_INCLUSIVE_OF ), MEDIUM_FONT ) );
            }

            if ( cashBackAllowed ) {
                this.addCountAndAmount( receipt, STR_CASHBACK_ONLY, rec.getCashback().count - rec.getCashback().reversalCount, ( rec.getCashback().amount - rec.getCashback().reversalAmount ) );
            }
            if( tipsAllowed ) {
                this.addCountAndAmount( receipt, STR_GRATUITY, rec.getTips().count - rec.getTips().reversalCount, ( rec.getTips().amount - rec.getTips().reversalAmount ) );
            }
            if( surchargeAllowed ) {
                this.addCountAndAmount( receipt, STR_SURCHARGE, rec.getSurcharge().count - rec.getSurcharge().reversalCount, rec.getSurcharge().amount - rec.getSurcharge().reversalAmount );
            }
        }

        this.addSpaceLines(receipt, 2);

        getCardSchemeDataLines(d, rec.getPreviousSchemeTotalsAsArray(), receipt);
        this.addSpaceLines(receipt, 2);

        if(reportType == IMessages.ReportType.ZReport || reportType == IMessages.ReportType.LastReconciliationReport) {
            TProtocol.HostResult hostResult = trans.getProtocol().getHostResult();

            switch (hostResult) {
                case RECONCILED_IN_BALANCE:
                    // 'settlement successful' in big text
                    addLineCentered(receipt, getText(STR_SETTLEMENT_SUCCESS), LARGE_FONT);
                    addBoxedText(receipt, new String[]{getText(STR_TOTALS_IN_BALANCE), getText(STR_TOTALS_RESET)});
                    // override display text
                    trans.getProtocol().setAdditionalResponseText(getText(STR_SETTLEMENT_SUCCESS));
                    trans.getProtocol().setPosResponseText(getText(STR_SETTLEMENT_SUCCESS));
                    break;

                case RECONCILED_OUT_OF_BALANCE:
                    if (!Util.isNullOrEmpty(trans.getProtocol().getCardAcceptorPrinterData())) {
                        addLineCentered(receipt, trans.getProtocol().getCardAcceptorPrinterData(), LARGE_FONT);
                    }
                    addBoxedText(receipt, new String[]{getText(STR_TOTALS_OUT_OF_BALANCE), getText(STR_TOTALS_RESET)});
                    break;

                case RECONCILED_OFFLINE_HOST_CUTOVER:
                    // auto cutover by host occurred
                    addLineCentered(receipt, getText(STR_SETTLEMENT_SUCCESS), LARGE_FONT);
                    addBoxedText(receipt, new String[]{getText(STR_HOST_AUTO_SETTLED), getText(STR_TOTALS_RESET)});
                    break;

                case RECONCILE_FAILED_TERMINAL_ALREADY_SETTLED:
                case RECONCILE_FAILED_OUTSIDE_WINDOW:
                    // for these expected response codes, display text from response code table
                    // print text from response code table if we have it
                    if (!Util.isNullOrEmpty(trans.getProtocol().getCardAcceptorPrinterData())) {
                        addBoxedText(receipt, new String[]{trans.getProtocol().getCardAcceptorPrinterData(), getText(STR_TOTALS_NOT_RESET)});
                    } else {
                        addBoxedText(receipt, new String[]{getText(STR_SETTLEMENT_FAILED), getText(STR_SYSTEM_ERROR), getText(STR_TOTALS_NOT_RESET)});
                    }
                    break;

                default:
                    // print system error
                    addBoxedText(receipt, new String[]{getText(STR_SETTLEMENT_FAILED), getText(STR_SYSTEM_ERROR), getText(STR_TOTALS_NOT_RESET)});
                    break;
            }
        }
        addDemoModeWarning(receipt);
        return receipt;
    }

    private void getCardSchemeDataLines(IDependency d, ArrayList<Reconciliation.CardSchemeTotals> prevTotals, PrintReceipt receipt) {

        final int NAME_MAX_LENGTH = 12;
        final int LINE_MAX_LENGTH = 26;
        if (receipt == null || (prevTotals == null || prevTotals.isEmpty()))
            return;

        receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_CARD_SCHEME_NET_TOTALS), MEDIUM_FONT));

        ArrayList<Reconciliation.CardSchemeTotals> schemeTotals = new ArrayList<>(prevTotals);

        for (Reconciliation.CardSchemeTotals total : schemeTotals) {

            String amount = d.getFramework().getCurrency().formatAmount("" + total.totalAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());

            if (total.name.length() > NAME_MAX_LENGTH || total.name.length() + amount.length() > LINE_MAX_LENGTH) {
                receipt.getLines().add(new PrintReceipt.PrintLine(total.name.toUpperCase() + ":", MEDIUM_FONT));
                this.addCountAndAmount(receipt, "", total.totalCount, total.totalAmount);
            } else {
                this.addCountAndAmount(receipt, total.name, total.totalCount, total.totalAmount);
            }
        }

    }
}
