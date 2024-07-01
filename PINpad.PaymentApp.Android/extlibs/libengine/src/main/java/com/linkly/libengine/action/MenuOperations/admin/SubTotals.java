package com.linkly.libengine.action.MenuOperations.admin;

import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_DEFAULT;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_SCREEN;
import static com.linkly.libengine.printing.IPrintManager.ReportType.SUB_TOTALS_REPORT;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMPTY;
import static com.linkly.libui.UIScreenDef.PROCESSING_PLEASE_WAIT_SHORT;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libmal.MalFactory;

public class SubTotals extends IAction {

    @Override
    public String getName() {
        return "SubTotals";
    }

    @Override
    public void run() {
        ui.showScreen(PROCESSING_PLEASE_WAIT_SHORT);
        IReceipt receipt = d.getPrintManager().getReceiptForReport(d, SUB_TOTALS_REPORT, mal);
        IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_DEFAULT : PRINT_PREFERENCE_SCREEN;
        d.getPrintManager().printReceipt(d, receipt.generateReceipt(null), null, false, STR_EMPTY, printPreference, mal);
        ui.displayMainMenuScreen();
    }
}