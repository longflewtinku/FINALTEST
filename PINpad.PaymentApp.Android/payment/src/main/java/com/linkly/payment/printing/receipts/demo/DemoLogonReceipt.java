package com.linkly.payment.printing.receipts.demo;

import static com.linkly.libui.IUIDisplay.String_id.STR_LOGON;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.printing.PrintReceipt;

public class DemoLogonReceipt extends DemoBaseReceipt {

    @Override
    public PrintReceipt generateReceipt(Object obj ) {
        TransRec trans = ( TransRec ) obj;
        PrintReceipt receipt = super.generateReceipt( trans ); // generate standard header

        addDateTimeRRN( receipt, trans );

        this.addSpaceLine( receipt );
        Receipt.addLineCentered( receipt, getText( STR_LOGON ).toUpperCase(), PrintReceipt.FONT.FONT_FIXED_WIDTH_POS );

        this.addAuthenticationResult( receipt, trans );
        addDemoModeWarning(receipt);

        return receipt;
    }

}
