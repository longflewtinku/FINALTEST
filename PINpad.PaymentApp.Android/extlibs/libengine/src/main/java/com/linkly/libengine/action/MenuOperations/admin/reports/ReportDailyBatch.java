package com.linkly.libengine.action.MenuOperations.admin.reports;

import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_DEFAULT;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_SCREEN;
import static com.linkly.libengine.printing.IPrintManager.ReportType.DAILY_BATCH_REPORT;
import static com.linkly.libengine.printing.IPrintManager.ReportType.FULL_DAILY_BATCH_REPORT;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_SUB_TOTALS_STARTED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_FINISHED;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_QUESTION;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMPTY;
import static com.linkly.libui.IUIDisplay.String_id.STR_FULL;
import static com.linkly.libui.IUIDisplay.String_id.STR_SHORT;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.DAILY_BATCH_REPORT_SELECT_TYPE;
import static com.linkly.libui.UIScreenDef.PROCESSING_PLEASE_WAIT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_DEFAULT;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.Printing.PrintFirst;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.reporting.DailyBatch;
import com.linkly.libengine.engine.reporting.IDailyBatch;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayQuestion;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class ReportDailyBatch extends IAction {

    private final boolean auto;

    public ReportDailyBatch( boolean auto ) {
        this.auto = auto;
    }

    public ReportDailyBatch( boolean auto, PositiveTransEvent positiveTransEvent ){
        this.auto = auto;
    }

    @Override
    public String getName() {
        return "ReportDailyBatch";
    }

    @Override
    public void run() {
        IUIDisplay.UIResultCode resultCode = OK;
        boolean isFull = false;
        IDailyBatch dailyBatch = new DailyBatch();
        IProto.TaskProtocolType protocol = d.getCustomer().getProtocolType();

        if ( this.auto ) {
            d.getStatusReporter().reportStatusEvent( STATUS_SUB_TOTALS_STARTED , trans.isSuppressPosDialog());
        }

        ui.showScreen( PROCESSING_PLEASE_WAIT );
        Reconciliation dailyBatchReport = dailyBatch.generateDailyBatch( false, d );

        if ( d.getCustomer().supportFullDailyBatchReport() && !this.auto ) {
            HashMap<String, Object> map = new HashMap<>();
            map.put( IUIDisplay.uiEnableBackButton, true );
            ArrayList<DisplayQuestion> options = new ArrayList<>();
            options.add( new DisplayQuestion( STR_FULL, "OP0", BTN_STYLE_DEFAULT ) );
            options.add( new DisplayQuestion( STR_SHORT, "OP1", BTN_STYLE_DEFAULT ) );

            map.put( IUIDisplay.uiScreenOptionList, options );

            ui.showScreen( DAILY_BATCH_REPORT_SELECT_TYPE, map );

            resultCode = ui.getResultCode( ACT_QUESTION, IUIDisplay.LONG_TIMEOUT );
            if ( resultCode == OK ) {
                String result = ui.getResultText( ACT_QUESTION, IUIDisplay.uiResultText1 );
                if ( result.equals( "OP0" ) ) {
                    isFull = true;
                }
            }
        }

        if ( resultCode == OK ) {
            if ( dailyBatchReport != null ) {
                PrintReceipt receiptToPrint = PrintFirst.buildAndBroadcastReceipt( d, isFull ? FULL_DAILY_BATCH_REPORT : DAILY_BATCH_REPORT, protocol, dailyBatchReport, context, mal );
                Timber.d( "receiptToPrint is = %s", receiptToPrint );
                IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_DEFAULT : PRINT_PREFERENCE_SCREEN;
                if( this.auto && super.trans.getTransEvent().isPosPrintingSync() ){
                    if( super.trans.isPrintOnTerminal()) {
                        d.getPrintManager().printReceipt(d, receiptToPrint, null, true, STR_EMPTY, printPreference, mal);
                    }
                } else {
                    d.getPrintManager().printReceipt(d, receiptToPrint, null, true, STR_EMPTY, printPreference, mal);
                }
            }
        }

        if ( auto ) {
            ECRHelpers.ipcSendReportResponse( d, super.trans, dailyBatchReport, "XReport", context );
            d.getStatusReporter().reportStatusEvent( STATUS_TRANS_FINISHED , trans.isSuppressPosDialog() );
        } else
            ui.displayMainMenuScreen();
    }
}
