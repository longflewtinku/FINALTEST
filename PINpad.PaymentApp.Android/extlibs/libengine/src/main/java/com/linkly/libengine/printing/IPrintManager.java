package com.linkly.libengine.printing;

import android.graphics.Bitmap;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.IMal;
import com.linkly.libmal.IMalPrint;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libui.IUIDisplay;

public interface IPrintManager {

    enum ReportType {
        TOTALS_REPORT,
        USER_REPORT,
        USER_PASSWORD_REPORT,
        DAILY_BATCH_REPORT,
        FULL_DAILY_BATCH_REPORT,
        USERLOG_REPORT,
        ABOUTAPP_REPORT,
        GENERIC_REPORT,
        HISTORY_REPORT,
        DEFERRED_AUTHS,
        SHIFT_TOTALS_REPORT,
        REPRINT_SHIFT_TOTALS_REPORT,
        SUB_TOTALS_REPORT,
    }

    public enum PrintPreference {
        PRINT_PREFERENCE_DEFAULT,
        PRINT_PREFERENCE_SCREEN,
        PRINT_PREFERENCE_PAPER,
        PRINT_PREFERENCE_BOTH,
    }

    IReceipt getReceiptForTrans(IDependency d, TransRec tran, IMal mal);
    IReceipt getReceiptForReport(IDependency d, ReportType reportType, IMal mal);
    IReceipt getReceiptForReport(IDependency d, ReportType reportType, IProto.TaskProtocolType type, IMal mal);

    // returns MalPrint "ps-" printing status
    IMalPrint.PrinterReturn printReceipt( IDependency d, PrintReceipt receipt, String message, boolean isShareable, IUIDisplay.String_id strPrompt, PrintPreference printPreference, IMal mal);

    Bitmap printReceiptToBitmap(IMalPrint d, PrintReceipt receipt);
}
