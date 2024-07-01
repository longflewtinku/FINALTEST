package com.linkly.payment.printing.receipts.suncorp;


import static com.linkly.libui.IUIDisplay.String_id.STR_LOGON;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;

/**
 * Logon Receipt Creator, baseclass of {@link Receipt}
 */
public class SuncorpLogonReceipt extends SuncorpBaseReceipt {
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
    public PrintReceipt generateReceipt( Object obj ) {
        TransRec trans = ( TransRec ) obj;
        PrintReceipt receipt = super.generateReceipt( trans ); // generate standard header


        this.addSpaceLine( receipt );
        Receipt.addLineCentered( receipt, getText( STR_LOGON ).toUpperCase(), MEDIUM_FONT );

        this.addAuthenticationResult( receipt, trans );

        return receipt;
    }
}
