package com.linkly.libengine.action.MenuOperations.admin.reports;

import static com.linkly.libengine.engine.reporting.TotalsManager.totalsDao;
import static com.linkly.libengine.printing.IPrintManager.ReportType.TOTALS_REPORT;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.NO_DATA_AVAILABLE;
import static com.linkly.libui.UIScreenDef.STR_CLEAR_TOTALS_SCREEN;
import static com.linkly.libui.UIScreenDef.TOTALS_REPORT_SCREEN;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_DEFAULT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_BORDER;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.reporting.Totals;
import com.linkly.libengine.engine.reporting.TotalsReport;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.display.DisplayQuestion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class ReportTotals extends IAction {

    @Override
    public String getName() {
        return "ReportTotals";
    }

    @Override
    public void run() {

        runTotalsReport();
        ui.displayMainMenuScreen();
    }


    public void runTotalsReport() {
        boolean isFull = false;
        List<Totals> totalsList = totalsDao.getAll();

        if (totalsList == null || totalsList.size() == 0) {
            /*No Data */
            ui.showScreen(NO_DATA_AVAILABLE);
            return;
        }

        // get report type - full or short
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<DisplayQuestion> options = new ArrayList<>();
        options.add(new DisplayQuestion(String_id.STR_FULL, "OP1", BTN_STYLE_DEFAULT));
        options.add(new DisplayQuestion(String_id.STR_SHORT, "OP2", BTN_STYLE_DEFAULT));
        map.put(IUIDisplay.uiScreenOptionList, options);
        ui.showScreen(TOTALS_REPORT_SCREEN, map);

        IUIDisplay.UIResultCode res = ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            PayCfg cfg = d.getPayCfg();
            TotalsReport report = new TotalsReport();
            int i = 0;
            String selected = ui.getResultText(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.uiResultText1);
            if (selected.contains("OP1")) {
                isFull = true;
            }


            if (isFull) {
                // generate full report details
                for (i = 0; i < cfg.getCards().size(); i++) {
                    TotalsReport.TotalsGroup group = new TotalsReport.TotalsGroup(cfg.getCards().get(i).getName());
                    // find and accumulate totals for this card
                    for (Totals data : totalsList) {
                        if (data.getCardName().compareToIgnoreCase(group.cardName) == 0) {
                            group.getTransTotals().add(data);
                        }
                    }
                    report.getTotalsDataItems().add(group);
                }
            }

            // get totals grouped by trans type
            totalsList = Totals.getTransTypeGroupedTotals();
            TotalsReport.TotalsGroup group = new TotalsReport.TotalsGroup("Totals (" + cfg.getCurrencyCode() + ")");
            if (totalsList != null) {
                for (Totals data : totalsList) {
                    data.setGroupTotal(true);
                    group.getTransTotals().add(data);
                }
            }
            report.getTotalsDataItems().add(group);
            Timber.i( "TotalTotal Data");


            IReceipt reportRec = d.getPrintManager().getReceiptForReport(d,TOTALS_REPORT, mal);
            report.setFullReport(isFull);
            mal.getHardware().getMalPrint().printReceipt(reportRec.generateReceipt(report));

            deleteTotalsReport();

        }
    }

    public void deleteTotalsReport() {
        // ask clear totals? yes/no question
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<DisplayQuestion> options = new ArrayList<>();
        options.add(new DisplayQuestion(String_id.STR_YES, "OP1", BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
        options.add(new DisplayQuestion(String_id.STR_NO, "OP2", BTN_STYLE_PRIMARY_BORDER));
        map.put(IUIDisplay.uiScreenOptionList, options);
        ui.showScreen(STR_CLEAR_TOTALS_SCREEN, map);

        IUIDisplay.UIResultCode res = ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            // ui operation completed without error
            String selected = ui.getResultText(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.uiResultText1);
            if (selected.contains("OP1")) {
                // user selected yes - clear the totals
                Totals.clearTotals();
            }
        }
    }
}
