package com.linkly.payment.printing.receipts.common;

import static com.linkly.libui.IUIDisplay.String_id.STR_PLS_RETAIN_RECEIPTS;

import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.printing.PrintReceipt;

import java.util.ArrayList;
import java.util.HashMap;

public class GenericConfigReceipt extends Receipt {


    @SuppressWarnings("unchecked")
    @Override
    public PrintReceipt generateReceipt(Object obj, ArrayList<String> keys, Boolean isWhitelist, String configTitle) {
        ArrayList<HashMap<String, String>> maps = (ArrayList<HashMap<String, String>>) obj;
        PrintReceipt receipt = new PrintReceipt();

        this.addHeader(receipt);
        this.addUserDetails(receipt);
        this.addPosIdAndReceiptNumber(receipt, null);
        this.addVersion(receipt);
        this.addMID(receipt);
        this.addSpaceLine(receipt);

        String displayName = "----" + configTitle + "----";
        addLineCentered(receipt, displayName);

        this.addSpaceLine(receipt);

        if (EFTPlatform.isPaxTerminal()) {
            for (HashMap<String, String> map : maps) {
                for (String key : map.keySet()) {
                    if (!keys.contains(key) && !isWhitelist) {
                        if (key.equals("sectionTitle")) {
                            this.addSpaceLine(receipt);
                            addLineCentered(receipt, map.get(key));
                        } else {
                            receipt.getLines().add(new PrintReceipt.PrintTableLine(key + ": ", map.get(key)));
                        }
                    } else if (keys.contains(key) && isWhitelist) {
                        if (key.equals("sectionTitle")) {
                            this.addSpaceLine(receipt);
                            addLineCentered(receipt, map.get(key));
                        } else {
                            receipt.getLines().add(new PrintReceipt.PrintTableLine(key + ": ", map.get(key)));
                        }
                    }
                }
            }
        }

        this.addSpaceLine(receipt);

        /*Date and time*/
        addDateTimeLine(receipt);

        this.addSpaceLine(receipt);

        addLineCentered(receipt, getText(STR_PLS_RETAIN_RECEIPTS));

        return receipt;
    }

}
