package com.linkly.payment.printing.receipts.suncorp;

import static com.linkly.libui.IUIDisplay.String_id.STR_CARD_SCHEME_NET_TOTALS;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASH;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASHBACK_SHORT;
import static com.linkly.libui.IUIDisplay.String_id.STR_GRATUITY;
import static com.linkly.libui.IUIDisplay.String_id.STR_HOST_AUTO_SETTLED;
import static com.linkly.libui.IUIDisplay.String_id.STR_INCLUSIVE_OF;
import static com.linkly.libui.IUIDisplay.String_id.STR_NET_TOTAL;
import static com.linkly.libui.IUIDisplay.String_id.STR_REFUND;
import static com.linkly.libui.IUIDisplay.String_id.STR_SALE;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTLEMENT_FAILED;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTLEMENT_FAILURE;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTLEMENT_REPORT;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTLEMENT_SUCCESS;
import static com.linkly.libui.IUIDisplay.String_id.STR_SYSTEM_ERROR;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_IN_BALANCE;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_NOT_RESET;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_OUT_OF_BALANCE;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_RESET;
import static com.linkly.libui.IUIDisplay.String_id.STR_X_REPORT_RECEIPT;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUICurrency;

import java.util.ArrayList;


public class SuncorpReportReceipt extends SuncorpBaseReceipt {
    private boolean ZReport = false;
    private boolean isFull = false;

    public SuncorpReportReceipt(boolean zReport, boolean full) {
        ZReport = zReport;
        isFull = full;
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

    @SuppressWarnings("static")
    public PrintReceipt generateReceipt(Object obj, TransRec trans) {
        Reconciliation rec = (Reconciliation) obj;
        PrintReceipt receipt = super.generateReceipt(trans); // generate standard header

        // settlement date/batch number
        this.addSettlementDate(receipt,trans,MEDIUM_FONT);

        this.addSpaceLines(receipt, 2);

        if (ZReport) {
            this.addLineCentered(receipt, getText(STR_SETTLEMENT_REPORT), LARGE_FONT);
        } else {
            this.addLineCentered(receipt, getText(STR_X_REPORT_RECEIPT), LARGE_FONT);
        }

        this.addSpaceLines(receipt, 2);

        long saleCount =  rec.getSale().count - rec.getSale().reversalCount;
        long saleAmount = rec.getSale().amount - rec.getSale().reversalAmount;

        long cashCount = rec.getCash().count - rec.getCash().reversalCount;
        long cashAmount = rec.getCash().amount - rec.getCash().reversalAmount;

        long refundCount = rec.getRefund().count - rec.getRefund().reversalCount;
        long refundAmount = rec.getRefund().amount - rec.getRefund().reversalAmount;

        this.addCountAndAmount( receipt, STR_SALE, saleCount, saleAmount );
        this.addCountAndAmount( receipt, STR_CASH, cashCount, cashAmount );
        this.addCountAndAmount( receipt, STR_REFUND, refundCount, refundAmount );
        this.addCountAndAmount( receipt, STR_NET_TOTAL, saleCount + cashCount + refundCount, ( saleAmount + cashAmount - refundAmount ) );
        this.addSpaceLines( receipt, 2 );

        if( d.getPayCfg() != null && d.getCustomer() != null ) {
            boolean cashBackAllowed = d.getPayCfg().isCashBackAllowed();
            boolean tipsAllowed = ( d.getCustomer().supportTipsOnReports() && d.getPayCfg().isTipAllowed() );

            if( cashBackAllowed || tipsAllowed ) {
                receipt.getLines().add( new PrintReceipt.PrintLine( getText( STR_INCLUSIVE_OF ), MEDIUM_FONT ) );
            }

            if ( cashBackAllowed ) {
                this.addCountAndAmount( receipt, STR_CASHBACK_SHORT, rec.getCashback().count - rec.getCashback().reversalCount, ( rec.getCashback().amount - rec.getCashback().reversalAmount ) );
            }
            if( tipsAllowed ) {
                this.addCountAndAmount( receipt, STR_GRATUITY, rec.getTips().count - rec.getTips().reversalCount, ( rec.getTips().amount - rec.getTips().reversalAmount ) );
            }
        }

        this.addSpaceLines(receipt, 2);

        getCardSchemeDataLines(d, rec.getPreviousSchemeTotalsAsArray(), receipt);
        this.addSpaceLines(receipt, 2);

        TProtocol.HostResult hostResult = trans.getProtocol().getHostResult();

        switch( hostResult ) {
            case RECONCILED_IN_BALANCE:
                // 'settlement successful' in big text
                addLineCentered(receipt, getText(STR_SETTLEMENT_SUCCESS), LARGE_FONT);
                addBoxedText( receipt, new String[] { getText(STR_TOTALS_IN_BALANCE), getText(STR_TOTALS_RESET) } );
                // override display text
                trans.getProtocol().setAdditionalResponseText(getText(STR_SETTLEMENT_SUCCESS));
                trans.getProtocol().setPosResponseText(getText(STR_SETTLEMENT_SUCCESS));
                break;

            case RECONCILED_OUT_OF_BALANCE:
                // 'settlement failure' in big text
                addLineCentered(receipt, getText(STR_SETTLEMENT_FAILURE), LARGE_FONT);
                addBoxedText( receipt, new String[] { getText(STR_TOTALS_OUT_OF_BALANCE), getText(STR_TOTALS_RESET) } );
                // override display text
                trans.getProtocol().setAdditionalResponseText(getText(STR_SETTLEMENT_FAILURE));
                trans.getProtocol().setPosResponseText(getText(STR_SETTLEMENT_FAILURE));
                break;

            case RECONCILED_OFFLINE_HOST_CUTOVER:
                // auto cutover by host occurred
                addLineCentered(receipt, getText(STR_SETTLEMENT_SUCCESS), LARGE_FONT);
                addBoxedText( receipt, new String[] { getText(STR_HOST_AUTO_SETTLED), getText(STR_TOTALS_RESET) } );
                break;

            case RECONCILE_FAILED_TERMINAL_ALREADY_SETTLED:
            case RECONCILE_FAILED_OUTSIDE_WINDOW:
                // for these expected response codes, display text from response code table
                // print text from response code table if we have it
                if( !Util.isNullOrEmpty(trans.getProtocol().getCardAcceptorPrinterData())) {
                    addBoxedText( receipt, new String[] { trans.getProtocol().getCardAcceptorPrinterData(), getText(STR_TOTALS_NOT_RESET) } );
                } else {
                    addBoxedText( receipt, new String[] { getText(STR_SETTLEMENT_FAILED), getText(STR_SYSTEM_ERROR), getText(STR_TOTALS_NOT_RESET) } );
                }
                break;

            default:
                // print system error
                addBoxedText( receipt, new String[] { getText(STR_SETTLEMENT_FAILED), getText(STR_SYSTEM_ERROR), getText(STR_TOTALS_NOT_RESET) } );
                break;
        }
        return receipt;
    }

    private void getCardSchemeDataLines(IDependency d, ArrayList<Reconciliation.CardSchemeTotals> prevTotals, PrintReceipt receipt) {

        final int NAME_MAX_LENGTH = 12;
        final int LINE_MAX_LENGTH = 26;
        if (receipt == null || (prevTotals == null || prevTotals.size() == 0))
            return;

        receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_CARD_SCHEME_NET_TOTALS) , MEDIUM_FONT));

        ArrayList<Reconciliation.CardSchemeTotals> schemeTotals = new ArrayList<>(prevTotals);

        for (Reconciliation.CardSchemeTotals total : schemeTotals) {

            String startLine = total.name + ":";
            while (startLine.length() < NAME_MAX_LENGTH)
                startLine = startLine + " ";
            startLine = startLine + "x";

            String amount = d.getFramework().getCurrency().formatAmount("" + total.totalAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());

            if (startLine.length() + amount.length() > LINE_MAX_LENGTH) {
                receipt.getLines().add(new PrintReceipt.PrintLine(total.name + ":" , MEDIUM_FONT));
                this.addCountAndAmount(d, receipt, "           x", total.totalCount, "" + total.totalAmount, MEDIUM_FONT);
            } else {
                this.addCountAndAmount(d, receipt, startLine, total.totalCount, "" + total.totalAmount, MEDIUM_FONT);
            }
        }

    }

    public boolean isFull() {
        return this.isFull;
    }

    public void setFull(boolean isFull) {
        this.isFull = isFull;
    }
}
