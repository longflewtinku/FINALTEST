package com.linkly.libengine.action.MenuOperations.admin.reprint;


import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.RECONCILED_IN_BALANCE;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_DEFAULT;
import static com.linkly.libmal.global.printing.PrintReceipt.SCREEN_ICON.PR_ERROR_ICON;
import static com.linkly.libmal.global.printing.PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON;
import static com.linkly.libui.IUIDisplay.String_id.STR_REPRINT;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libmal.global.printing.PrintReceipt;

import timber.log.Timber;

public class RePrint extends IAction {

    @Override
    public String getName() {
        return "RePrint";
    }

    @Override
    public void run() {
        // set statics above so static methods below can use the current data
        TransRec latest = TransRecManager.getInstance().getTransRecDao().getLatest();

        if (latest != null) {
            IReceipt receiptGenerator = d.getPrintManager().getReceiptForTrans(d,latest, mal);

            if (receiptGenerator != null) {
                PrintReceipt receiptToPrint = receiptGenerator.generateReceipt(latest);

                if (latest.isApprovedOrDeferred() || (latest.getTransType() == RECONCILIATION && latest.getProtocol().getHostResult() == RECONCILED_IN_BALANCE)) {
                    receiptToPrint.setIconFinished(PR_SUCCESS_ICON);
                    receiptToPrint.setIconWhilePrinting(PR_SUCCESS_ICON);
                } else {
                    receiptToPrint.setIconFinished(PR_ERROR_ICON);
                    receiptToPrint.setIconWhilePrinting(PR_ERROR_ICON);
                }
                d.getPrintManager().printReceipt(d, receiptToPrint, null, true, STR_REPRINT, PRINT_PREFERENCE_DEFAULT, mal);
            } else {
                Timber.i( "Receipt Not Implemented for transaction Type");
            }
        } else {
            Timber.i( "Nothing to reprint");
        }
    }

    public static void updateScreenIcons(TransRec latest, PrintReceipt receiptToPrint) {
        if (latest.isReconciliation()) {
            if (latest.getProtocol().getHostResult() == RECONCILED_IN_BALANCE || latest.getProtocol().getHostResult() == AUTHORISED) {
                receiptToPrint.setIconFinished(PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON);
                receiptToPrint.setIconWhilePrinting(PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON);
            } else {
                receiptToPrint.setIconFinished(PrintReceipt.SCREEN_ICON.PR_ERROR_ICON);
                receiptToPrint.setIconWhilePrinting(PrintReceipt.SCREEN_ICON.PR_ERROR_ICON);
            }
        } else {
            receiptToPrint.setIconFinished(PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON);
            receiptToPrint.setIconWhilePrinting(PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON);
        }
    }
}
