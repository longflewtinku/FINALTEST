package com.linkly.payment.printing.receipts.suncorp;

import static com.linkly.libui.IUIDisplay.String_id.STR_FALLBACK;
import static com.linkly.libui.IUIDisplay.String_id.STR_PLS_RETAIN_RECEIPTS;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRE_AUTH;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRE_AUTH_COMPLETION;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOPUP_COMPLETION;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOPUP_PRE_AUTH;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUICurrency;

public class SuncorpPreauthCompletionReceipt extends Receipt {
    @SuppressWarnings("static")
    @Override
    public PrintReceipt generateReceipt(Object obj) {
        TransRec trans = (TransRec) obj;
        PrintReceipt receipt = new PrintReceipt();

        this.addOptoMerchantDetails(receipt, 2);
        this.addDateTimeLineLeft(receipt, trans, MEDIUM_FONT);

        this.addMaskedMID(receipt, trans, MEDIUM_FONT);
        this.addMaskedTID(receipt, trans, MEDIUM_FONT);
        this.addCardEaseReference(receipt, trans, MEDIUM_FONT);

        this.addAID(receipt, trans, MEDIUM_FONT);
        this.addReceiptNumber(receipt, trans, MEDIUM_FONT);
        this.addStan(receipt,trans,MEDIUM_FONT);
        this.addPSN(receipt, trans, MEDIUM_FONT);

        this.addCardType(receipt, trans, MEDIUM_FONT);
        this.addCardName(receipt, trans, MEDIUM_FONT);
        this.addMaskedPan(receipt, trans, MEDIUM_FONT);

        switch( trans.getTransType() ) {
            case COMPLETION:
                this.addLineCentered( receipt, getText(STR_PRE_AUTH_COMPLETION).toUpperCase(), LARGE_FONT);
                break;
            case TOPUPCOMPLETION:
                this.addLineCentered( receipt, getText(STR_TOPUP_COMPLETION).toUpperCase(), LARGE_FONT);
                break;
            case PREAUTH:
                this.addLineCentered( receipt, getText(STR_PRE_AUTH).toUpperCase(), LARGE_FONT);
                break;
            case TOPUPPREAUTH:
                this.addLineCentered( receipt, getText(STR_TOPUP_PRE_AUTH).toUpperCase(), LARGE_FONT);
                break;
            default:
                break;
        }
        if(trans.getCard().isIccFallback()){
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_FALLBACK), MEDIUM_FONT));
        }
        this.addAmountFields(d, receipt, trans, MEDIUM_FONT, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, false, true);
        this.addSpaceLine(receipt);

        this.addAuthenticationResult(receipt, trans);
        this.addVerificationDetails(receipt, trans, MEDIUM_FONT);

        if (trans.isApprovedOrDeferred() && !isCardHolderCopy()) {
            this.addDeclarationBlock(receipt,MEDIUM_FONT);
        }

        this.addAuthCode(receipt, trans, MEDIUM_FONT, true);

        this.addRRN(receipt, trans, LARGE_FONT);

        this.addOfflineBalance(d, receipt, trans, MEDIUM_FONT);
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
