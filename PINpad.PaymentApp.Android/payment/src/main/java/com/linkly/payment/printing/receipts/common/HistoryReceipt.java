package com.linkly.payment.printing.receipts.common;

import static com.linkly.libui.IUIDisplay.String_id.STR_APPROVED;
import static com.linkly.libui.IUIDisplay.String_id.STR_CANCELLED;
import static com.linkly.libui.IUIDisplay.String_id.STR_CTLS;
import static com.linkly.libui.IUIDisplay.String_id.STR_DATE;
import static com.linkly.libui.IUIDisplay.String_id.STR_DECLINED;
import static com.linkly.libui.IUIDisplay.String_id.STR_HISTORY_REPORT;
import static com.linkly.libui.IUIDisplay.String_id.STR_ICC;
import static com.linkly.libui.IUIDisplay.String_id.STR_MAN;
import static com.linkly.libui.IUIDisplay.String_id.STR_NA;
import static com.linkly.libui.IUIDisplay.String_id.STR_NO_TRANS;
import static com.linkly.libui.IUIDisplay.String_id.STR_PAN;
import static com.linkly.libui.IUIDisplay.String_id.STR_PIN;
import static com.linkly.libui.IUIDisplay.String_id.STR_PIN_SIG;
import static com.linkly.libui.IUIDisplay.String_id.STR_INVOICE_NO;
import static com.linkly.libui.IUIDisplay.String_id.STR_SIG;
import static com.linkly.libui.IUIDisplay.String_id.STR_SN;
import static com.linkly.libui.IUIDisplay.String_id.STR_SWIPE;
import static com.linkly.libui.IUIDisplay.String_id.STR_TID;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay.String_id;

public class HistoryReceipt extends Receipt {

    public HistoryReceipt() {
    }
    private boolean isFull;

    private String getSignType(TransRec tran) {
        String out = "?";

        switch (tran.getCard().getCvmType()) {
            case NO_CVM_SET:
            case NO_CVM:
                out = getText(String_id.STR_NO_CVM);
                break;
            case SIG:
                out = getText(STR_SIG);
                break;
            case PLAINTEXT_OFFLINE_PIN:
            case ENCIPHERED_OFFLINE_PIN:
            case ENCIPHERED_ONLINE_PIN:
                out = getText(STR_PIN);
                break;
            case PLAINTEXT_PIN_AND_SIG:
            case ENCIPHERED_PIN_AND_SIG:
                out = getText(STR_PIN_SIG);
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
                out = getText(STR_MAN);
                break;
            case CTLS:
            case CTLS_MSR:
                out = getText(STR_CTLS);
                break;
            case ICC:
            case ICC_FALLBACK_KEYED:
            case ICC_FALLBACK_SWIPED:
            case ICC_OFFLINE:
                out = getText(STR_ICC);
                break;
            case SWIPED:
                out = getText(STR_SWIPE);
                break;
            case NOT_CAPTURED:
            default:
                out = getText(STR_NA);
        }


        return out;
    }


    @SuppressWarnings("static")
    @Override
    public PrintReceipt generateReceipt(Object obj) {

        Reconciliation rec = (Reconciliation) obj;
        PayCfg paycfg = d.getPayCfg();
        PrintReceipt receipt = new PrintReceipt();

        receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getMerchant().getLine1(), LARGE_FONT));
        receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getMerchant().getLine2(), MEDIUM_FONT));
        receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getMerchant().getLine3(), MEDIUM_FONT));
        receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getMerchant().getLine4(), MEDIUM_FONT));
        receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getMerchant().getLine5(), MEDIUM_FONT));

        this.addLineCentered(receipt, getText(STR_HISTORY_REPORT).toUpperCase(), LARGE_FONT);

        this.addLineCentered(receipt, getText(STR_SN)  + mal.getHardware().getSerialNumber(), SMALL_FONT);
        if (UserManager.getActiveUser() != null && UserManager.getActiveUser().getTerminalId() != null && !UserManager.getActiveUser().getTerminalId().isEmpty())
            this.addLineCentered(receipt, getText(STR_TID) + UserManager.getActiveUser().getTerminalId(), SMALL_FONT);

        this.addDateTimeLineCentre(receipt, null, SMALL_FONT);

        this.addSpaceLines(receipt, 2);


        /*TODO - Add all the Transaction Details */
        if (rec.getRecTransList() == null || rec.getRecTransList().size() == 0) {
            this.addLineCentered(receipt, getText(STR_NO_TRANS), LARGE_FONT);
        } else {
            int t;
            /*Add Each of the Transactions*/
            t = rec.getRecTransList().size() - 1;
            for (; t >= 0; t--) {
                TransRec tran = rec.getRecTransList().get(t);

                String result = getText(STR_DECLINED).toUpperCase();
                if (tran.isApproved()) {
                    result = getText(STR_APPROVED).toUpperCase();
                } else if (tran.isCancelled()) {
                    result = getText(STR_CANCELLED).toUpperCase();
                }

                String signType = getSignType(tran);
                String signUse = getCaptureType(tran);

                // (Note: in this we limit the display name to a max of 8 characters to fit on the line)
                String transName = tran.getTransType().getDisplayName();
                while (transName.length() < 8) {
                    transName += " ";
                }
                transName = transName.substring(0, 7);

                // Display a table.
                receipt.getLines().add(new PrintReceipt.PrintTableLine(transName + " " + d.getFramework().getCurrency().formatAmount(tran.getAmounts().getTotalAmount() + "", IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()),
                        " " + signType + "\\" + signUse, MEDIUM_FONT));
                receipt.getLines().add(new PrintReceipt.PrintTableLine(result, getText(STR_DATE) + tran.getAudit().getTransDateTimeAsString("dd\\MM\\yy") + " " + tran.getAudit().getTransDateTimeAsString("HH:mm:ss")));
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_PAN) + tran.getMaskedPan(TransRec.MaskType.REPORT_MASK, d.getPayCfg()), /*"PID:" + tran.getEftPaymentIdShort()*/ ""));
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_INVOICE_NO), "" +tran.getAudit().getReceiptNumber(), MEDIUM_FONT));


                this.addSpaceLine(receipt);
            }
        }

        this.addSpaceLine(receipt);

        return receipt;

    }


    public boolean isFull() {
        return this.isFull;
    }

    public void setFull(boolean isFull) {
        this.isFull = isFull;
    }
}
