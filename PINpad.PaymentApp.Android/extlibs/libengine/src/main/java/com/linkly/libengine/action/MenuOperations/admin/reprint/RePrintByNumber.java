package com.linkly.libengine.action.MenuOperations.admin.reprint;


import static com.linkly.libengine.action.Printing.PrintFirst.doPrintWithOptionsMenu;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.ENTER_RECEIPT_NUM;
import static com.linkly.libui.UIScreenDef.RECEIPT_NOT_FOUND;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

public class RePrintByNumber extends IAction {

    @Override
    public String getName() {
        return "RePrintByNumber";
    }

    @Override
    public void run() {
        HashMap<String,Object> map = new HashMap<>();
        /*Step 1 Get the User ID */

        ui.showInputScreen(ENTER_RECEIPT_NUM, map);
        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);


        if (res == OK) {
            String receiptNo = ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1);

            TransRec latest = TransRecManager.getInstance().getTransRecDao().getByReceiptNumber( receiptNo );
            if (latest != null) {
                if (latest.getTransType() == EngineManager.TransType.RECONCILIATION) {
                    ui.showScreen(RECEIPT_NOT_FOUND);
                    return;
                }
                doPrintWithOptionsMenu(d, latest, true, false, mal);

            } else {
                ui.showScreen( RECEIPT_NOT_FOUND);
            }
        }
        ui.displayMainMenuScreen();
    }
}
