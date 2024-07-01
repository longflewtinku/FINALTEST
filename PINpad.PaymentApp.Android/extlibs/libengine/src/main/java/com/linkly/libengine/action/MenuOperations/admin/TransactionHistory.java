package com.linkly.libengine.action.MenuOperations.admin; //NOSONAR

import static com.linkly.libengine.action.Printing.PrintFirst.doPrintWithOptionsMenu;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_BOTH;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_SCREEN;
import static com.linkly.libengine.users.User.Privileges.MANAGER;
import static com.linkly.libengine.users.User.Privileges.SUPERVISOR;
import static com.linkly.libmal.global.printing.PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_TRANSACTION_HISTORY;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRINTING;
import static com.linkly.libui.IUIDisplay.String_id.STR_TRANSACTION_HISTORY;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.DISPLAY_TRANSACTION_HISTORY;
import static com.linkly.libui.UIScreenDef.PERMISSIONS_ERROR;
import static com.linkly.libui.UIScreenDef.UNABLE_TO_READ_RECORD;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libengine.users.User;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.IMal;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

import timber.log.Timber;

public class TransactionHistory extends IAction {
    @Override
    public String getName() {
        return "TransactionHistory";
    }

    // Suppressing complexity issue as better to see what is going on than obscuring it.
    @SuppressWarnings("java:S3776")
    @Override
    public void run() {
        IUIDisplay.UIResultCode res;
        do {
            HashMap<String, Object> map = new HashMap<>();

            map.put(IUIDisplay.uiTitleId, STR_TRANSACTION_HISTORY);
            ui.showInputScreen(DISPLAY_TRANSACTION_HISTORY, map);

            res = ui.getResultCode(ACT_TRANSACTION_HISTORY, IUIDisplay.NO_TIMEOUT); // timeout is implemented in the fragment itself. we dont' want to timeout in the transaction thread
            if (res == OK) {
                String selectedRow = ui.getResultText(ACT_TRANSACTION_HISTORY, IUIDisplay.uiResultText1);
                int uid = 0;
                boolean rerunLoop = false;
                try {
                    uid = Integer.parseInt(selectedRow);
                } catch (NumberFormatException ex) {
                    Timber.e("format error while parsing uid");
                    ui.showScreen(UNABLE_TO_READ_RECORD);
                    ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                    rerunLoop = true;
                }

                TransRec trans = null;
                if( !rerunLoop ) {
                    trans = TransRecManager.getInstance().getTransRecDao().getByUid(uid);
                    if (trans == null) {
                        ui.showScreen(UNABLE_TO_READ_RECORD);
                        ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                        rerunLoop = true;
                    }
                }

                // stupid hack to get around sonarcloud which doesn't like more than one continue statement in a do..while loop
                if( rerunLoop ) {
                    continue;
                }

                // We need to set our current transaction to allow the print response to happen
                d.resetCurrentTransaction(trans);
                print(d, trans);

                User user = UserManager.getActiveUser();
                if ( user == null || (user.getPrivileges() != MANAGER && user.getPrivileges() != SUPERVISOR)) {
                    ui.showScreen(PERMISSIONS_ERROR);
                    ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                }
            }
        } while( res == OK );
        ui.displayMainMenuScreen();
    }

    private void reprintReconciliation(IDependency d, TransRec t, IMal mal){
        IReceipt receiptGenerator = d.getPrintManager().getReceiptForTrans(d,t, mal);
        if (receiptGenerator == null) {
            return;
        }

        receiptGenerator.setIsMerchantCopy(true);
        receiptGenerator.setIsCardHolderCopy(false);
        receiptGenerator.setIsDuplicate(true);

        PrintReceipt receiptToPrint = receiptGenerator.generateReceipt(t);

        receiptToPrint.setIconFinished(PR_SUCCESS_ICON);
        receiptToPrint.setIconWhilePrinting(PR_SUCCESS_ICON);
        IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_BOTH : PRINT_PREFERENCE_SCREEN;
        d.getPrintManager().printReceipt(d, receiptToPrint, "", true, STR_PRINTING, printPreference, mal);
    }

    private void print(IDependency d, TransRec t) {
        if( t.isReconciliation() ) {
            reprintReconciliation(d,t, mal);
        } else {
            // else regular transaction
            doPrintWithOptionsMenu(d, t, true, false, mal);
        }
    }

}
