package com.linkly.libengine.action.check;

import static com.linkly.libui.UIScreenDef.BATTERY_TOO_LOW;
import static com.linkly.libui.UIScreenDef.PAPER_OUT;
import static com.linkly.libui.UIScreenDef.PRINTER_ERROR_VAR_CHECK_PRINTER;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.status.IStatus;
import com.linkly.libmal.IMalPrint;
import com.linkly.libmal.global.platform.EFTPlatform;

import timber.log.Timber;

/**
 * {@link IAction} class which checks printer status
 * Will throw up a screen if either battery is too low or if Printer is out of paper or if there is something wrong with it
 * Will skip printer status check if:
 * - We are spoofing comms
 * - If we are printing to the screen
 * - If Current Transaction {@link com.linkly.libengine.engine.EngineManager.TransType#autoTransaction} flag is set
 */
public class CheckPrinter extends IAction {
    @Override
    public String getName() {
        return "CheckPrinter";
    }

    @Override
    public void run() {
        // Currently using terminal printer for signature check regardless of the flag (auto print) from POS
        // So only checking the useTerminalPrinter flag. might need to include UseTerminalPrinterForSignatureCheck flag in future
        boolean printOnTerminal = true;
        if (trans.getTransType().autoTransaction && trans.getTransEvent() != null) {
            printOnTerminal = trans.getTransEvent().isUseTerminalPrinter();
        }

        // If there is no need to print then we can skip the paper check
        if (CoreOverrides.get().isSpoofComms() || EFTPlatform.printToScreen() || !printOnTerminal) {
            return;
        }
        IMalPrint.PrinterReturn status;
        if (mal == null) {
            Timber.e("Mal is null");
            status = IMalPrint.PrinterReturn.UNKNOWN_FAILURE;
        } else {
            status = mal.getHardware().getMalPrint().getPrinterStatus();
        }

        if (status != IMalPrint.PrinterReturn.SUCCESS) {
            handlePrinterCheckFailure(d, trans, status, TransactionCanceller.class);
        }
    }

    public static void handlePrinterCheckFailure(IDependency d, TransRec trans, IMalPrint.PrinterReturn status, Class<? extends IAction> jumpTo) {
        if (status == IMalPrint.PrinterReturn.VOLTAGE_LOW) {
            d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_PRINTER_GENERAL_ERROR, trans.isSuppressPosDialog());
            d.getFramework().getUI().showScreen(BATTERY_TOO_LOW);

        } else if (status == IMalPrint.PrinterReturn.OUT_OF_PAPER) {
            d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_PRINTER_OUT_OF_PAPER , trans.isSuppressPosDialog());
            d.getFramework().getUI().showScreen(PAPER_OUT);
        } else {
            d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_PRINTER_GENERAL_ERROR ,trans.isSuppressPosDialog());
            d.getFramework().getUI().showScreen(PRINTER_ERROR_VAR_CHECK_PRINTER, status.toString());
        }

        d.getWorkflowEngine().setNextAction(jumpTo);
    }
}
