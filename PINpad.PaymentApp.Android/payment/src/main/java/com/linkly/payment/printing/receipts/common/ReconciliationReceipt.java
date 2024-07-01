package com.linkly.payment.printing.receipts.common;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUICurrency;

public class ReconciliationReceipt extends Receipt {


    private String getSignType(TransRec tran) {
        String out = "?";

        switch (tran.getCard().getCvmType()) {
            case NO_CVM_SET:
            case NO_CVM:
                out = "N";
                break;
            case SIG:
                out = "Z";
                break;
            case ENCIPHERED_OFFLINE_PIN:
            case PLAINTEXT_OFFLINE_PIN:
            case ENCIPHERED_ONLINE_PIN:
                out = "K";
                break;
            case PLAINTEXT_PIN_AND_SIG:
            case ENCIPHERED_PIN_AND_SIG:
                out = "B";
                break;
        }


        return out;
    }

    private String getCaptureType(TransRec tran) {
        String out = "?";
        //TODO Make sure all methods are covered
        switch (tran.getCard().getCaptureMethod()) {
            case RRN_ENTERED:
            case MANUAL:
                out = "M";
                break;
            case CTLS:
                out = "R";
                break;
            case ICC:
                out = "I";
                break;
            case SWIPED:
                out = "C";
                break;
            default:
                out = "A";
        }


        return out;
    }

    @SuppressWarnings("static")
    @Override
    public PrintReceipt generateReceipt(Object obj) {

        TransRec reconcTrans = (TransRec) obj;
        Reconciliation rec = reconcTrans.getReconciliation();
        int recTransCount = 0;
        PayCfg paycfg = d.getPayCfg();
        PrintReceipt receipt = new PrintReceipt();

        this.addHeader(receipt);
        this.addUserDetails(receipt);
        this.addPosIdAndReceiptNumber(receipt, rec.getReceiptNumber());
        this.addVersion(receipt);
        this.addMID(receipt);
        this.addSpaceLine(receipt);

        this.addTransName(receipt, "RECONCILIATION", null);
        this.addSpaceLine(receipt);

        /*Get Count of Transactions that can be included in Rec*/
        if (rec.getRecTransList() != null && rec.getRecTransList().size() > 0) {
            int t;
            for (t = 0; t < rec.getRecTransList().size(); t++) {
                TransRec tran = rec.getRecTransList().get(t);
                if (tran.approvedAndIncludeInReconciliation()) {
                    recTransCount++;
                }
            }
        }


        /*TODO - Add all the Transaction Details */
        if (rec.getRecTransList() == null || rec.getRecTransList().size() == 0 || recTransCount == 0) {
            this.addLineCentered(receipt, "NO TRANSACTIONS", LARGE_FONT);
        } else {
            int t;
            receipt.getLines().add(new PrintReceipt.PrintLine("FROM REC NB: " + rec.getStartReceiptTran() + " TO REC NB: " + rec.getEndReceiptTran()));
            this.addSpaceLine(receipt);

            /*Add Each of the Transactions*/
            for (t = 0; t < rec.getRecTransList().size(); t++) {
                TransRec tran = rec.getRecTransList().get(t);

                if (tran.approvedAndIncludeInReconciliation()) {
                    //STAN // Date
                    receipt.getLines().add(new PrintReceipt.PrintTableLine("REC NB: " + String.format("%06d", tran.getAudit().getReceiptNumber()), "DATE: " + tran.getAudit().getTransDateTimeAsString("ddMMyy") + " " + tran.getAudit().getTransDateTimeAsString("HHmmss")));

                    // Masked Pan
                    receipt.getLines().add(new PrintReceipt.PrintLine("PAN: " + tran.getMaskedPan(TransRec.MaskType.REPORT_MASK, d.getPayCfg())));


                    String signType = getSignType(tran);
                    String signUse = getCaptureType(tran);

                    String transName = tran.getTransType().getDisplayName();
                    if (tran.isVas()) {
                        transName = tran.getVasName();
                    }

                    // Display a table. (Note: in this we limit the display name to a max of 8 characters to fit on the line)
                    int maxLength = transName.length() < 8 ? transName.length() : 8;

                    receipt.getLines().add(new PrintReceipt.PrintTableLine(transName.substring(0, maxLength), d.getFramework().getCurrency().formatAmount(tran.getAmounts().getTotalAmount() + "", IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()), signType + signUse + " " + tran.getProtocol().getAuthCode()));

                    this.addSpaceLine(receipt);
                }
            }
        }

        receipt.getLines().add(new PrintReceipt.PrintFillLine('_'));

        this.addLineCentered(receipt, paycfg.getCurrencyCode(), LARGE_FONT);
        this.addSpaceLine(receipt);

        /*Display Counts */
        receipt.getLines().add(new PrintReceipt.PrintTableLine("   ", "COUNT", "AMOUNT"));
        if (paycfg.isSaleTransAllowed()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine("SALE", "(" + String.format("%04d", rec.getSale().count) + ")", d.getFramework().getCurrency().formatAmount(rec.getSale().amount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));

            if (paycfg.isReversalTransAllowed()) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine("SALE REV", "(" + String.format("%04d", rec.getSale().reversalCount) + ")", d.getFramework().getCurrency().formatAmount(rec.getSale().reversalAmount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            }
        }

        if (paycfg.isRefundTransAllowed()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine("REFUND", "(" + String.format("%04d", rec.getRefund().count) + ")", d.getFramework().getCurrency().formatAmount(rec.getRefund().amount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            if (paycfg.isReversalTransAllowed()) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine("REFUND REV", "(" + String.format("%04d", rec.getRefund().reversalCount) + ")", d.getFramework().getCurrency().formatAmount(rec.getRefund().reversalAmount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            }
        }

        if (paycfg.isCashTransAllowed()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine("CASH", "(" + String.format("%04d", rec.getCash().count) + ")", d.getFramework().getCurrency().formatAmount(rec.getCash().amount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            if (paycfg.isReversalTransAllowed()) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine("CASH REV", "(" + String.format("%04d", rec.getCash().reversalCount) + ")", d.getFramework().getCurrency().formatAmount(rec.getCash().reversalAmount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            }
        }


        if (paycfg.isCompletionTransAllowed()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine("COMPLETION", "(" + String.format("%04d", rec.getCompletion().count) + ")", d.getFramework().getCurrency().formatAmount(rec.getCompletion().amount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            if (paycfg.isReversalTransAllowed()) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine("COMPLETION REV", "(" + String.format("%04d", rec.getCompletion().reversalCount) + ")", d.getFramework().getCurrency().formatAmount(rec.getCompletion().reversalAmount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            }
        }

        printVasTotals(d, rec, receipt);

        receipt.getLines().add(new PrintReceipt.PrintFillLine('_'));

        receipt.getLines().add(new PrintReceipt.PrintTableLine("SUB TOTAL", "(" + String.format("%04d", rec.getSubTotalCount()) + ")", d.getFramework().getCurrency().formatAmount(rec.getSubTotalAmount() + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
        if (paycfg.isTipAllowed()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine("TIPS", "(" + String.format("%04d", rec.getTips().count) + ")", d.getFramework().getCurrency().formatAmount(rec.getTips().amount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            this.addSpaceLine(receipt);
        }

        if (paycfg.isCashBackAllowed() || CoreOverrides.get().isEnableCashback()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine("CASHBACK", "(" + String.format("%04d", rec.getCashback().count) + ")", d.getFramework().getCurrency().formatAmount(rec.getCashback().amount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
        }

        receipt.getLines().add(new PrintReceipt.PrintFillLine('_'));
        receipt.getLines().add(new PrintReceipt.PrintTableLine("TOTAL", "(" + String.format("%04d", rec.getTotalCount()) + ")", d.getFramework().getCurrency().formatAmount(rec.getTotalAmount() + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
        receipt.getLines().add(new PrintReceipt.PrintFillLine('_'));

        this.addSpaceLine(receipt);

        /*Date and time*/
        this.addDateTimeLine(receipt, reconcTrans);


        receipt.getLines().add(new PrintReceipt.PrintFillLine('*'));

        TProtocol.HostResult hostResult = reconcTrans.getProtocol().getHostResult();
        if (hostResult == TProtocol.HostResult.RECONCILED_IN_BALANCE) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine("*", "RECONCILIATION BALANCED", "*"));
        } else if ((hostResult == TProtocol.HostResult.RECONCILED_OUT_OF_BALANCE) || (hostResult == TProtocol.HostResult.RECONCILED_NO_TOTALS)) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine("*", "RECONCILIATION NOT BALANCED", "*"));
        } else {
            receipt.getLines().add(new PrintReceipt.PrintTableLine("*", "RECONCILIATION UNCONFIRMED", "*"));
        }

        receipt.getLines().add(new PrintReceipt.PrintFillLine('*'));

        return receipt;

    }

}
