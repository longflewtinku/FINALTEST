package com.linkly.payment.printing.receipts.generic;

import static com.linkly.libui.IUIDisplay.String_id.STR_NO_TRANS;
import static com.linkly.libui.IUIDisplay.String_id.STR_PAN;
import static com.linkly.libui.IUIDisplay.String_id.STR_INVOICE_NO;

import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.payment.fragments.FragDeferredAuths.DeferredTransaction;

import java.util.ArrayList;

public class DeferredAuthsReceipt extends Receipt {
    @Override
    public PrintReceipt generateReceipt(Object obj) {
        ArrayList<?> list = (ArrayList<?>) obj;
        PrintReceipt receipt = new PrintReceipt();

        this.addOptoMerchantDetails(receipt, 0);
        Receipt.addLineCentered(receipt, getText(String_id.STR_DEFERRED_AUTHS).toUpperCase(), LARGE_FONT);
        this.addDateTimeLineCentre(receipt, null, SMALL_FONT);

        this.addSpaceLines(receipt, 2);

        if (list == null || list.isEmpty()) {
            Receipt.addLineCentered(receipt, getText(STR_NO_TRANS), LARGE_FONT);
        } else {
            for ( Object trans : list){
                // Display a table.
                DeferredTransaction deferTrans = ( DeferredTransaction )trans;

                receipt.getLines().add(new PrintReceipt.PrintTableLine(deferTrans.getTransType(), d.getFramework().getCurrency().formatAmount(deferTrans.getAmount() + "", IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode())
                        , MEDIUM_FONT));
                receipt.getLines().add(new PrintReceipt.PrintTableLine(deferTrans.getStatus(), getText(STR_INVOICE_NO)+" " + deferTrans.getReceiptNo()));
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_PAN) + deferTrans.getMaskedpan(), ""));

                this.addSpaceLine(receipt);
            }
        }
        return receipt;
    }
}
