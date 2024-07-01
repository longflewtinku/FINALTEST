package com.linkly.libengine.action.MenuOperations.admin.reprint;


import static com.linkly.libengine.action.MenuOperations.admin.reprint.RePrint.updateScreenIcons;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_DEFAULT;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_SCREEN;
import static com.linkly.libui.IUIDisplay.String_id.STR_REPRINT;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.reporting.ReconciliationManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.printing.PrintReceipt;

public class RePrintLastReconciliation extends IAction {

    @Override
    public String getName() {
        return "RePrintLastReconciliation";
    }

    @Override
    public void run() {
        ReconciliationManager.getInstance(); // create instance if not already done

        // find newest reconciliation
        TransRec latest = TransRecManager.getInstance().getTransRecDao().getLatestByTransType(RECONCILIATION);

        if (latest != null) {
            latest.setReconciliation(reconciliationDao.findByTransId(latest.getUid()));

            if (latest.getReconciliation() != null) {
                IReceipt receiptGenerator = d.getPrintManager().getReceiptForTrans(d, latest, mal);

                if (receiptGenerator != null) {
                    receiptGenerator.setIsDuplicate(true);
                    PrintReceipt receiptToPrint = receiptGenerator.generateReceipt(latest);

                    // TODO: perhaps display error here
                    if (receiptToPrint == null)
                        return;

                    updateScreenIcons(latest, receiptToPrint);
                    IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_DEFAULT : PRINT_PREFERENCE_SCREEN;
                    d.getPrintManager().printReceipt(d, receiptToPrint, null, true, STR_REPRINT, printPreference, mal);
                }
            }
            ui.displayMainMenuScreen();
        }
    }
}
