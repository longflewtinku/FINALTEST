package com.linkly.payment.printing.receipts.generic;


import static com.linkly.libui.IUIDisplay.String_id.STR_LOGON;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;

/**
 * Logon Receipt Creator, baseclass of {@link Receipt}
 */
public class LogonReceipt extends GenericBaseReceipt {
    /**
     * Generates Logon Receipt
     * Will create a {@link PrintReceipt} object in with the following format:
     * Header
     * Date & Time
     * TID
     * MID
     * STAN
     *
     * LOGON
     * Result Text
     * Response Code
     * @param obj {@link Object} object which is cast into TransRec object
     */
    @Override
    public PrintReceipt generateReceipt( Object obj ) {
        TransRec trans = ( TransRec ) obj;
        PrintReceipt receipt = super.generateReceipt( trans ); // generate standard header

        addDateTimeRRN( receipt, trans );

        this.addSpaceLine( receipt );
        Receipt.addLineCentered( receipt, getText( STR_LOGON ).toUpperCase(), PrintReceipt.FONT.FONT_FIXED_WIDTH_POS );

        this.addAuthenticationResult( receipt, trans );

        return receipt;
    }
}
