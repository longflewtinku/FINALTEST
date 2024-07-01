package com.linkly.libengine.action.MenuOperations.admin.reports;

import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_DEFAULT;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_SCREEN;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMPTY;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.reporting.History;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.printing.PrintReceipt;

public class ReportHistory extends IAction {

    private boolean auto = false;

    public ReportHistory(boolean auto) {
        this.auto = auto;
    }

    @Override
    public String getName() {
        return "ReportHistory";
    }

    @Override
    public void run() {

        History history = new History();
        Reconciliation recDetails = history.generateHistory();
        if (recDetails != null) {
            IReceipt dailyReport = null;
            dailyReport = d.getPrintManager().getReceiptForReport(d, IPrintManager.ReportType.HISTORY_REPORT, d.getCustomer().getProtocolType(), mal);
            PrintReceipt receiptToPrint = dailyReport.generateReceipt(recDetails);
            IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_DEFAULT : PRINT_PREFERENCE_SCREEN;
            d.getPrintManager().printReceipt(d, receiptToPrint, null, true, STR_EMPTY, printPreference, mal);
        }

        if (auto)
            ECRHelpers.ipcSendReportResponse(d, trans, recDetails,"historyReport", context);
        else
            ui.displayMainMenuScreen();
        
    }
}
