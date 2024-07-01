package com.linkly.libengine.action.Printing;

import androidx.preference.PreferenceManager;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.check.CheckPrinter;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.IMalPrint;

import timber.log.Timber;

public class PrintSecond extends IAction {

    @Override
    public String getName() {
        return "PrintSecond";
    }

    //todo this should be put to the transation state
    @Override
    public void run() {
        IMalPrint.PrinterReturn printingStatus;

        d.getWorkflowEngine().setNextAction(TransactionFinalizer.class);
        if (CoreOverrides.get().isAutoFillTrans() && trans.getTransType() != EngineManager.TransType.RECONCILIATION) {
            return;
        }

        if (trans.isApprovedOrDeferred() && trans.isSignatureRequired()) {
            Timber.i( "Receipt needs printing for signature CVM");
        } else if ((trans.getCard().getCaptureMethod() == TCard.CaptureMethod.CTLS || trans.getCard().getCaptureMethod() == TCard.CaptureMethod.CTLS_MSR) &&
                !PreferenceManager.getDefaultSharedPreferences(context).getBoolean("printContactlessReceipts", true)) {
            return;
        }

        printingStatus = PrintFirst.doPrintFirstOrSecond(d, trans, false, mal, context);

        if (!IMalPrint.PrinterReturn.SUCCESS.equals(printingStatus)) {
            CheckPrinter.handlePrinterCheckFailure(d, trans, printingStatus, PrintSecond.class);
        }
    }

}
