package com.linkly.libengine.action.MenuOperations.admin.reprint;

import static com.linkly.libengine.action.Printing.PrintFirst.doPrintWithOptionsMenu;
import static com.linkly.libui.UIScreenDef.RECEIPT_NOT_FOUND;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.transactions.TransRec;

public class RePrintLast extends IAction {

    @Override
    public String getName() {
        return "RePrintLast";
    }

    @Override
    public void run() {

        // find the last financial (non-admin) txn
        TransRec latest = TransRec.getLatestFinancialTxn();
        if (latest != null) {
            doPrintWithOptionsMenu(d, latest, true, false, mal);
        } else {
            ui.showScreen(RECEIPT_NOT_FOUND);
        }
        ui.displayMainMenuScreen();

    }
}
