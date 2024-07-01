package com.linkly.payment.printing;

import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_BOTH;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_PAPER;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_SCREEN;
import static com.linkly.libui.IUIDisplay.SCREEN_ICON.ERROR_ICON;
import static com.linkly.libui.IUIDisplay.SCREEN_ICON.PRINTING_ICON;
import static com.linkly.libui.IUIDisplay.SCREEN_ICON.PROCESSING_ICON;
import static com.linkly.libui.IUIDisplay.SCREEN_ICON.SUCCESS_ICON;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMPTY;
import static com.linkly.libui.UIScreenDef.BATTERY_LOW_PLEASE_CHARGE;
import static com.linkly.libui.UIScreenDef.PAPER_OUT_PLEASE_INSERT;
import static com.linkly.libui.UIScreenDef.PRINTER_ERROR_VAR_BR;
import static com.linkly.libui.UIScreenDef.VIEW_ANIMATED_RECEIPT;
import static com.linkly.libui.UIScreenDef.VIEW_RECEIPT;

import android.graphics.Bitmap;
import android.os.SystemClock;

import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libmal.IMal;
import com.linkly.libmal.IMalPrint;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UI;
import com.linkly.libui.display.DisplayQuestion;
import com.linkly.payment.printing.receipts.common.AboutAppReceipt;
import com.linkly.payment.printing.receipts.common.DailyBatchReceipt;
import com.linkly.payment.printing.receipts.common.GenericConfigReceipt;
import com.linkly.payment.printing.receipts.common.HistoryReceipt;
import com.linkly.payment.printing.receipts.common.ShiftTotalsReceipt;
import com.linkly.payment.printing.receipts.common.TotalsReportReceipt;
import com.linkly.payment.printing.receipts.common.UserPasswordReportReceipt;
import com.linkly.payment.printing.receipts.common.UserReportReceipt;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class PrintManager implements IPrintManager {

    private static PrintManager instance = null;
    private static final String TAG = "PrintManager";

    private PrintManager() {
        super();
    }

    public static PrintManager getInstance() {
        if (instance == null) {
            instance = new PrintManager();
        }
        return instance;
    }

    public Bitmap printReceiptToBitmap(IMalPrint print, PrintReceipt receipt) {
        return print.printReceiptToBitmap(receipt);
    }

    public static Bitmap lastReceiptPrinted = null;
    public static boolean lastReceiptApproved = false;

    private IMalPrint.PrinterReturn printReceiptToScreen( IDependency d, PrintReceipt receipt, String message, boolean shareable, IUIDisplay.String_id strPrompt, PrintPreference printPreference, IMalPrint malPrint) {
        Timber.i("printReceiptToScreen");
        lastReceiptPrinted = printReceiptToBitmap(malPrint, receipt);

        HashMap<String, Object> map = new HashMap<>();
        if (strPrompt != null && strPrompt != STR_EMPTY)
            map.put(IUIDisplay.uiScreenPrompt,  UI.getInstance().getPrompt(strPrompt));

        switch( receipt.getIconWhilePrinting() ) {
            case PR_ERROR_ICON:
                // declined
                map.put(IUIDisplay.uiScreenIcon, ERROR_ICON );
                break;

            case PR_PROCESSING_ICON:
                map.put(IUIDisplay.uiScreenIcon, PROCESSING_ICON );
                break;

            case PR_PRINTING_ICON:
                map.put(IUIDisplay.uiScreenIcon, PRINTING_ICON );
                break;

            case PR_SUCCESS_ICON:
            default:
                // treat all others as approved
                map.put(IUIDisplay.uiScreenIcon, SUCCESS_ICON );
                break;
        }

        map.put(IUIDisplay.uiUserSharable, shareable);

        if (EFTPlatform.printToScreen() || printPreference == PRINT_PREFERENCE_SCREEN) {
            // use static view receipt screen with DONE button, and very long timeout
            d.getUI().showScreen(VIEW_RECEIPT, map);
            d.getUI().getResultCode(IUIDisplay.ACTIVITY_ID.ACT_SCREEN_PRINT, IUIDisplay.LONG_TIMEOUT);
        } else {
            // preference will be to print and view ('both'), so display animated receipt that scrolls up and off screen
            if (malPrint.getPrinterHardwareStatus() == IMalPrint.PrinterReturn.SUCCESS) {
                // display animated receipt only if printer is OK and paper printing is expected to be successful
                d.getUI().showScreen(VIEW_ANIMATED_RECEIPT, map);
                d.getUI().getResultCode(IUIDisplay.ACTIVITY_ID.ACT_ANIMATED_PRINT, IUIDisplay.IMMEDIATE_TIMEOUT);
            }
        }

        return IMalPrint.PrinterReturn.SUCCESS;
    }

    // Suppressing if statement warning as merging it will break logic for PrintFist
    @SuppressWarnings("java:S1066")
    @Override
    public IMalPrint.PrinterReturn printReceipt( IDependency d, PrintReceipt receipt, String message, boolean shareable, IUIDisplay.String_id strPrompt, PrintPreference printPreference, IMal mal) {

        if(EFTPlatform.hasPrinter() && printPreference == PRINT_PREFERENCE_BOTH) {
            Timber.i("Print to both screen and paper");
            printReceiptToScreen(d,receipt, message, shareable, strPrompt, printPreference, mal.getPrint());
        } else if (EFTPlatform.hasPrinter() && printPreference == PRINT_PREFERENCE_PAPER) {
            Timber.i("Print to paper");
        } else if (EFTPlatform.printToScreen() || printPreference == PRINT_PREFERENCE_SCREEN )
            return printReceiptToScreen(d,receipt, message, shareable, strPrompt, printPreference, mal.getPrint());

        IMalPrint.PrinterReturn ret;
        IUIDisplay ui = d.getUI();

        if (message == null) {
            message = "";
        }

        if (CoreOverrides.get().isAutoFillTrans() && d.getCurrentTransaction().getTransType() != EngineManager.TransType.RECONCILIATION){
           return IMalPrint.PrinterReturn.SUCCESS;
        }

        ret = mal.getPrint().getPrinterStatus();
        if (ret == IMalPrint.PrinterReturn.SUCCESS) {
            ret = mal.getPrint().printReceipt(receipt);
        } else {
            switch (ret) {
                case VOLTAGE_LOW:
                    ui.showScreen(BATTERY_LOW_PLEASE_CHARGE);
                    break;
                case OUT_OF_PAPER:
                    int timeoutMs;
                    if (d.getCurrentTransaction() == null) {
                        timeoutMs = d.getPayCfg().getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.PAPER_OUT_TIMEOUT, false); //MD : for non-transaction such as User Manager stuff and configs, there is no access mode configured
                    } else {
                        timeoutMs = d.getPayCfg().getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.PAPER_OUT_TIMEOUT, d.getCurrentTransaction().getAudit().isAccessMode());
                    }
                    HashMap<String, Object> map = new HashMap<>();
                    ArrayList<DisplayQuestion> options = new ArrayList<>();
                    options.add(new DisplayQuestion(d.getPrompt(IUIDisplay.String_id.STR_OK), "OP0", DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
                    map.put(IUIDisplay.uiScreenOptionList, options);
                    long startTimeMs = SystemClock.elapsedRealtime();//fetch starting time
                    while ((SystemClock.elapsedRealtime() - startTimeMs) < timeoutMs) {
                        ui.showScreen(PAPER_OUT_PLEASE_INSERT, map);
                        IUIDisplay.UIResultCode resultCode = ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, Math.toIntExact(timeoutMs - (SystemClock.elapsedRealtime() - startTimeMs)));
                        Timber.d("Result code :%s", resultCode);
                        if (resultCode == IUIDisplay.UIResultCode.OK) {
                            if (mal.getPrint().getPrinterStatus() == IMalPrint.PrinterReturn.SUCCESS) {
                                ret = mal.getPrint().printReceipt(receipt);
                                break;
                            }
                        }
                    }
                    break;

                default:
                    ui.showScreen(PRINTER_ERROR_VAR_BR);
                    break;
            }
        }

        return ret;
    }

    @Override
    public IReceipt getReceiptForTrans(IDependency d, TransRec tran, IMal mal) {
        IReceipt receipt = null;

        receipt = d.getCustomer().getReceiptForTrans(tran);
        if( receipt != null ) {
            receipt.setDependencies(d, mal);
        }

        return receipt;
    }

    @Override
    public IReceipt getReceiptForReport(IDependency d, ReportType reportType, IMal mal) {
        IReceipt receipt = getReceiptForReport(d, reportType, d.getCustomer().getProtocolType(), mal);
        if( receipt != null ) {
            receipt.setDependencies(d, mal);
        }

        return receipt;
    }

    @Override
    public IReceipt getReceiptForReport(IDependency d, ReportType reportType, IProto.TaskProtocolType type, IMal mal) {
        IReceipt receipt = null;

        switch (reportType) {
            case TOTALS_REPORT:
                receipt = new TotalsReportReceipt();
                break;
            case HISTORY_REPORT:
                 receipt = new HistoryReceipt();
                break;
            case USER_REPORT:
                receipt = new UserReportReceipt();
                break;
            case USER_PASSWORD_REPORT:
                receipt = new UserPasswordReportReceipt();
                break;
            case DAILY_BATCH_REPORT:
                 receipt = new DailyBatchReceipt(false);
                break;
            case FULL_DAILY_BATCH_REPORT:
                receipt = new DailyBatchReceipt(true);
                break;
            case ABOUTAPP_REPORT:
                receipt = new AboutAppReceipt();
                break;
            case GENERIC_REPORT:
                receipt = new GenericConfigReceipt();
                break;
            case SHIFT_TOTALS_REPORT:
                receipt = new ShiftTotalsReceipt(IMessages.ReportType.ShiftTotalsReport);
                break;
            case REPRINT_SHIFT_TOTALS_REPORT:
                receipt = new ShiftTotalsReceipt(IMessages.ReportType.ReprintShiftTotalsReport);
                break;
            case SUB_TOTALS_REPORT:
                receipt = new ShiftTotalsReceipt(IMessages.ReportType.SubShiftTotalsReport);
                break;
        }

        if( receipt != null ) {
            receipt.setDependencies(d, mal);
        }

        return receipt;
    }
}
