package com.linkly.payment.printing.receipts.common;

import static com.linkly.libui.IUIDisplay.String_id.STR_AMOUNT_CAPS;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASH;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASHBACK;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASH_REV;
import static com.linkly.libui.IUIDisplay.String_id.STR_COMPLETION_REV;
import static com.linkly.libui.IUIDisplay.String_id.STR_COUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_DAILY_BATCH_REPORT;
import static com.linkly.libui.IUIDisplay.String_id.STR_DATE;
import static com.linkly.libui.IUIDisplay.String_id.STR_FROM;
import static com.linkly.libui.IUIDisplay.String_id.STR_NO_TRANS;
import static com.linkly.libui.IUIDisplay.String_id.STR_PAN;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRE_AUTH_COMPLETION;
import static com.linkly.libui.IUIDisplay.String_id.STR_REC_NB;
import static com.linkly.libui.IUIDisplay.String_id.STR_REFUND;
import static com.linkly.libui.IUIDisplay.String_id.STR_REFUND_REV;
import static com.linkly.libui.IUIDisplay.String_id.STR_SALE;
import static com.linkly.libui.IUIDisplay.String_id.STR_SALE_REV;
import static com.linkly.libui.IUIDisplay.String_id.STR_SUB_TOTAL;
import static com.linkly.libui.IUIDisplay.String_id.STR_TIP;
import static com.linkly.libui.IUIDisplay.String_id.STR_TO;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTAL;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUICurrency;

public class DailyBatchReceipt extends Receipt {

    public DailyBatchReceipt(boolean isFull) {
        this.isFull = isFull;
    }
    private boolean isFull;

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
            case PLAINTEXT_OFFLINE_PIN:
            case ENCIPHERED_OFFLINE_PIN:
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


    @Override
    public PrintReceipt generateReceipt(Object obj) {

        Reconciliation rec = (Reconciliation) obj;
        PayCfg paycfg = d.getPayCfg();
        PrintReceipt receipt = new PrintReceipt();

        this.addHeader(receipt);
        this.addUserDetails(receipt);
        this.addPosIdAndReceiptNumber(receipt, rec.getReceiptNumber());
        this.addVersion(receipt);
        this.addMID(receipt);
        this.addSpaceLine(receipt);

        Receipt.addLineCentered(receipt, getText(STR_DAILY_BATCH_REPORT), LARGE_FONT);
        this.addSpaceLine(receipt);


        /*TODO - Add all the Transaction Details */
        if (isFull) {
            if (rec.getRecTransList() == null || rec.getRecTransList().size() == 0) {
                Receipt.addLineCentered(receipt, getText(STR_NO_TRANS), LARGE_FONT);
            } else {
                int t;
                receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_FROM)+ " "+getText(STR_REC_NB) + rec.getStartReceiptTran() + " "+getText(STR_TO).toUpperCase()+ " "+getText(STR_REC_NB) + rec.getEndReceiptTran()));
                this.addSpaceLine(receipt);

                /*Add Each of the Transactions*/
                t = rec.getRecTransList().size() - 1;
                for (; t >= 0; t--) {
                    TransRec tran = rec.getRecTransList().get(t);

                    if (tran.approvedAndIncludeInReconciliation()) {
                        //STAN // Date
                        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_REC_NB) + String.format("%06d", tran.getAudit().getReceiptNumber()), getText(STR_DATE) + tran.getAudit().getTransDateTimeAsString("ddMMyy") + " " + tran.getAudit().getTransDateTimeAsString("HHmmss")));

                        // Masked Pan
                        receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_PAN) + tran.getMaskedPan(TransRec.MaskType.REPORT_MASK, d.getPayCfg())));

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
        }

        receipt.getLines().add(new PrintReceipt.PrintFillLine('_'));


        Receipt.addLineCentered(receipt, paycfg.getCurrencyCode(), LARGE_FONT);
        this.addSpaceLine(receipt);

        /*Display Counts */
        receipt.getLines().add(new PrintReceipt.PrintTableLine("   ", getText(STR_COUNT), getText(STR_AMOUNT_CAPS)));
        if (paycfg.isSaleTransAllowed()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SALE).toUpperCase(), "(" + String.format("%04d", rec.getSale().count) + ")", d.getFramework().getCurrency().formatAmount(rec.getSale().amount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));

            if (paycfg.isReversalTransAllowed()) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SALE_REV), "(" + String.format("%04d", rec.getSale().reversalCount) + ")", d.getFramework().getCurrency().formatAmount(rec.getSale().reversalAmount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            }
        }

        if (paycfg.isRefundTransAllowed()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_REFUND), "(" + String.format("%04d", rec.getRefund().count) + ")", d.getFramework().getCurrency().formatAmount(rec.getRefund().amount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            if (paycfg.isReversalTransAllowed()) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_REFUND_REV), "(" + String.format("%04d", rec.getRefund().reversalCount) + ")", d.getFramework().getCurrency().formatAmount(rec.getRefund().reversalAmount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            }
        }

        if (paycfg.isCashTransAllowed()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_CASH).toUpperCase(), "(" + String.format("%04d", rec.getCash().count) + ")", d.getFramework().getCurrency().formatAmount(rec.getCash().amount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            if (paycfg.isReversalTransAllowed()) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_CASH_REV), "(" + String.format("%04d", rec.getCash().reversalCount) + ")", d.getFramework().getCurrency().formatAmount(rec.getCash().reversalAmount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            }
        }

        if (paycfg.isCompletionTransAllowed()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_PRE_AUTH_COMPLETION).toUpperCase(), "(" + String.format("%04d", rec.getCompletion().count) + ")", d.getFramework().getCurrency().formatAmount(rec.getCompletion().amount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            if (paycfg.isReversalTransAllowed()) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_COMPLETION_REV), "(" + String.format("%04d", rec.getCompletion().reversalCount) + ")", d.getFramework().getCurrency().formatAmount(rec.getCompletion().reversalAmount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            }
        }

        printVasTotals(d, rec, receipt);

        receipt.getLines().add(new PrintReceipt.PrintFillLine('_'));

        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SUB_TOTAL), "(" + String.format("%04d", rec.getSubTotalCount()) + ")", d.getFramework().getCurrency().formatAmount(rec.getSubTotalAmount() + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
        if (paycfg.isTipAllowed()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_TIP), "(" + String.format("%04d", rec.getTips().count) + ")", d.getFramework().getCurrency().formatAmount(rec.getTips().amount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
            this.addSpaceLine(receipt);
        }

        if (paycfg.isCashBackAllowed() || CoreOverrides.get().isEnableCashback()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_CASHBACK).toUpperCase(), "(" + String.format("%04d", rec.getCashback().count) + ")", d.getFramework().getCurrency().formatAmount(rec.getCashback().amount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
        }

        receipt.getLines().add(new PrintReceipt.PrintFillLine('_'));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_TOTAL).toUpperCase(), "(" + String.format("%04d", rec.getTotalCount()) + ")", d.getFramework().getCurrency().formatAmount(rec.getTotalAmount() + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
        receipt.getLines().add(new PrintReceipt.PrintFillLine('_'));

        this.addSpaceLine(receipt);

        /*Date and time*/
        this.addDateTimeLine(receipt);


        return receipt;

    }


    public boolean isFull() {
        return this.isFull;
    }

    public void setFull(boolean isFull) {
        this.isFull = isFull;
    }
}
