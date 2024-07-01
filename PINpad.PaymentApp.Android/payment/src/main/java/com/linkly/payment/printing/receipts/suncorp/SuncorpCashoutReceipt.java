package com.linkly.payment.printing.receipts.suncorp;

import static com.linkly.libui.IUIDisplay.String_id.STR_CASH;
import static com.linkly.libui.IUIDisplay.String_id.STR_FALLBACK;
import static com.linkly.libui.IUIDisplay.String_id.STR_PLS_RETAIN_RECEIPTS;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUICurrency;

public class SuncorpCashoutReceipt extends Receipt {
    @SuppressWarnings("static")
    @Override
    public PrintReceipt generateReceipt(Object obj) {
        TransRec trans = (TransRec) obj;
        PrintReceipt receipt = new PrintReceipt();

        this.addOptoMerchantDetails(receipt, 0);

        this.addDateTimeLineLeft(receipt, trans, MEDIUM_FONT);

        this.addMaskedMID(receipt, trans, MEDIUM_FONT);
        this.addMaskedTID(receipt, trans, MEDIUM_FONT);
        this.addAID(receipt, trans, MEDIUM_FONT);
        this.addReceiptNumber(receipt, trans, MEDIUM_FONT);
        this.addStan(receipt,trans,MEDIUM_FONT);
        this.addPSN(receipt, trans, MEDIUM_FONT);

        this.addCardType(receipt, trans, MEDIUM_FONT);
        this.addCardName(receipt, trans, MEDIUM_FONT);
        this.addMaskedPan(receipt, trans, MEDIUM_FONT);

        receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_CASH).toUpperCase(), MEDIUM_FONT));
        if(trans.getCard().isIccFallback()){
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_FALLBACK), MEDIUM_FONT));
        }
        this.addAmountFields(d, receipt, trans, MEDIUM_FONT, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, false, true);
        this.addSpaceLines(receipt, 2);

        this.addAuthenticationResult(receipt, trans);
        this.addVerificationDetails(receipt, trans, MEDIUM_FONT);

        if (trans.isApproved() && !isCardHolderCopy()) {
            this.addDeclarationBlock(receipt,MEDIUM_FONT);
        }

        this.addAuthCode(receipt, trans, MEDIUM_FONT, true);
        this.addCustomerReference2(receipt, trans);
        this.addBanner(receipt);
        if (isCardHolderCopy()) {
            this.addLineCentered(receipt, getText(STR_PLS_RETAIN_RECEIPTS), SMALL_FONT);
        }

        if (isMerchantCopy()) {
            addIccDiags(receipt, trans);
        }

        this.addDigitalSignatureSection(receipt, trans, MEDIUM_FONT);
        return receipt;
    }
}
