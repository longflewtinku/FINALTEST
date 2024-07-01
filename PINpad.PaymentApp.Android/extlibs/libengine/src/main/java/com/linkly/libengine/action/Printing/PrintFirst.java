package com.linkly.libengine.action.Printing;

import static com.linkly.libengine.action.MenuOperations.admin.reprint.RePrint.updateScreenIcons;
import static com.linkly.libengine.engine.transactions.TransRec.printReceiptToDebug;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.TIMEOUT;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_SCREEN;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_CUSTOMER_RECEIPT;
import static com.linkly.libpositive.wrappers.PositiveReceiptResponse.ReceiptType.CUSTOMER;
import static com.linkly.libpositive.wrappers.PositiveReceiptResponse.ReceiptType.DUPLICATE;
import static com.linkly.libpositive.wrappers.PositiveReceiptResponse.ReceiptType.LOGON;
import static com.linkly.libpositive.wrappers.PositiveReceiptResponse.ReceiptType.MERCHANT;
import static com.linkly.libpositive.wrappers.PositiveReceiptResponse.ReceiptType.SETTLEMENT;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMPTY;
import static com.linkly.libui.IUIDisplay.String_id.STR_REMOVE_CARDHOLDER_COPY;
import static com.linkly.libui.IUIDisplay.String_id.STR_REMOVE_MERCHANT_COPY;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.CUSTOMER_EMAIL_QUEUED;
import static com.linkly.libui.UIScreenDef.ENTER_EMAIL;
import static com.linkly.libui.UIScreenDef.MERCHANT_EMAIL_QUEUED;
import static com.linkly.libui.UIScreenDef.PRINT_CUSTOMER_RECEIPT;
import static com.linkly.libui.UIScreenDef.REMOVE_CARDHOLDER_COPY;
import static com.linkly.libui.UIScreenDef.REMOVE_MERCHANT_COPY;
import static com.linkly.libui.UIScreenDef.SELECT_RECEIPT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_BORDER_DOUBLE;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE;

import android.content.Context;
import android.os.SystemClock;

import androidx.preference.PreferenceManager;

import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.check.CheckPrinter;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libmal.IMal;
import com.linkly.libmal.IMalPrint;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.wrappers.PositiveReceiptResponse;
import com.linkly.libpositive.wrappers.PositiveTransResult;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;
import com.linkly.libui.UIScreenDef;
import com.linkly.libui.display.DisplayQuestion;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class PrintFirst extends IAction {

    //todo this should be put to the translation state
    private static IMalPrint.PrinterReturn firstPrintingStatus = IMalPrint.PrinterReturn.SUCCESS;
    private static String_id promptText = STR_EMPTY;

    private static final String PRINT_CUSTOMER_RECEIPT_ALWAYS = "ALWAYS";
    private static final String PRINT_CUSTOMER_RECEIPT_ASK = "ASK";
    private static final String PRINT_CUSTOMER_RECEIPT_NEVER = "NEVER";

    @Override
    public String getName() {
        return "PrintFirst";
    }

    @Override
    public void run() {
        firstPrintingStatus = IMalPrint.PrinterReturn.SUCCESS;
        if ( ( CoreOverrides.get().isAutoFillTrans() &&
                trans.getTransType() != EngineManager.TransType.RECONCILIATION ) ) {
            return;
        }

        if (trans.isApprovedOrDeferred() && trans.isSignatureRequired()) {
            Timber.i( "Receipt needs printing for signature CVM");
            if( trans.getAudit().isSignatureChecked()) {
                d.getWorkflowEngine().setNextAction(PrintSecond.class);
                return;
            }
        }
        else if ((trans.getCard().getCaptureMethod() == TCard.CaptureMethod.CTLS || trans.getCard().getCaptureMethod() == TCard.CaptureMethod.CTLS_MSR) &&
                !PreferenceManager.getDefaultSharedPreferences(context).getBoolean("printContactlessReceipts", true)) {
            return;
        }

        if (EFTPlatform.printToScreen()) {
            doPrintWithOptionsMenu(d, trans, false, true, mal);
        }

        emailMerchantReceipt(d, trans);

        firstPrintingStatus = doPrintFirstOrSecond(d, trans, true, mal, context);

        if (!IMalPrint.PrinterReturn.SUCCESS.equals(firstPrintingStatus)) {
            if (trans.isApprovedOrDeferred() && trans.isSignatureRequired() ) {
				// Note - currently, out-of-paper condition on signature-required receipts cancels the transaction.
				// If we specify PrintFirst.class here instead, it doesn't quite work - we skip the sig-ok query processing. That still needs fixing.
                CheckPrinter.handlePrinterCheckFailure(d, trans, firstPrintingStatus, TransactionCanceller.class);
            } else {
                CheckPrinter.handlePrinterCheckFailure(d, trans, firstPrintingStatus, PrintFirst.class);
            }
        }
    }

    /**
     * Prints Merchant &/or Customer receipt first or second
     * Whether to print merchant copy is controlled by Payment App's DefaultSharedPreferences
     * Whether to print merchant copy is depended on arg first + App's DefaultSharedPreferences
     * @param d {@link IDependency} object
     * @param trans {@link TransRec} trans object
     * @param first tells if it is the first receipt being printed in the transaction flow
     * */
    @SuppressWarnings("java:S6541")
    public static IMalPrint.PrinterReturn doPrintFirstOrSecond(IDependency d, TransRec trans, boolean first, IMal mal, Context context) {
        ReceiptType receipt = ReceiptType.NONE;
        String printCustomerReceipt = d.getPayCfg().getPrintCustomerReceipt();
        boolean alreadyAsked = false;
        boolean printCustomerCopy = true; // Default to true unless customer copy not required
        boolean printMerchantReceipt = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("printMerchantReceipt", true);
        boolean printMerchantFirst = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("merchantReceiptFirst", true);

        // Determine which receipt needs to be printed
        if ( !first && EFTPlatform.printToScreen() && mal.getHardware().getMalPrint().getPrinterHardwareStatus() == IMalPrint.PrinterReturn.OUT_OF_PAPER) {
            Timber.d( "Do not print second receipt: Print to Screen is enabled and printer is out of paper" );
            // keep "receipt = ReceiptType.NONE"
        } else if( trans.isApprovedOrDeferred() && trans.isSignatureRequired() ){
            // If signature is required, then we want to print the merchant receipt regardless of whether the merchant copy is being printed or not
            printMerchantReceipt = true;

            // If Signature receipt, it will always be merchant first & then customer
            receipt = first ? ReceiptType.MERCHANT : ReceiptType.CUSTOMER;
            promptText = STR_EMPTY;
        } else {
            /*
            * Merchant Receipt      Merchant First          Result
            *   0                       x                   Customer
            *   0                       x                   Customer
            *   1                       0                   Customer + Merchant
            *   1                       1                   Merchant + Customer
            * */
            if( first ) {
                if ( printMerchantFirst ) {
                    promptText = STR_REMOVE_MERCHANT_COPY;
                    receipt = ReceiptType.MERCHANT;
                } else {
                    promptText = STR_REMOVE_CARDHOLDER_COPY;
                    receipt = ReceiptType.CUSTOMER;
                }
            } else {
                receipt = printMerchantFirst ? ReceiptType.CUSTOMER : ReceiptType.MERCHANT;

                // if applicable, display a 'remove [merchant/cardholder] copy' dismissible screen before proceeding
                if ( EFTPlatform.hasPrinter() && // and we have a printer
                        ( promptText != STR_EMPTY ) && // and a prompt (remove merchant/cardholder receipt) is set
                        printMerchantReceipt &&
                        (firstPrintingStatus == IMalPrint.PrinterReturn.SUCCESS) &&
                        ( ( trans.isPrintOnTerminal() && d.getPayCfg().isShowReceiptPromptForAuto() ) ||
                                !( trans.getTransType().autoTransaction && trans.getTransEvent() != null &&
                                        trans.getTransEvent().isPosPrintingSync() ) // NOT using (pos syncing + auto trans) display this screen
                        )
                ) {
                    // then display screen to prompt user to remove receipt and press OK
                    if ( promptText.equals( STR_REMOVE_MERCHANT_COPY ) ) {
                        // do not display "remove receipt" if "printCustomerReceipt" prompt is following or no customer display will be printed
                        if (printCustomerReceipt.equals(PRINT_CUSTOMER_RECEIPT_ALWAYS)) {
                            removeReceiptPrompt(d, REMOVE_MERCHANT_COPY, trans);
                        }
                    } else {
                        removeReceiptPrompt( d, REMOVE_CARDHOLDER_COPY ,trans);
                    }
                }
            }
        }

        if (receipt.equals(ReceiptType.CUSTOMER)) {
            // Check if customer receipt printing is disabled or it's required to display Prompt to print Customer receipt
            if (printCustomerReceipt.equals(PRINT_CUSTOMER_RECEIPT_NEVER)) {
                Timber.i("Skip Customer Receipt Printing: parameter set to NEVER");
                promptText = STR_EMPTY;
                printCustomerCopy = false;
                alreadyAsked = true;
            } else if (printCustomerReceipt.equals(PRINT_CUSTOMER_RECEIPT_ASK)) {
                Timber.d("Print Customer Ask");
                if (!printCustomerReceiptPrompt( d, trans )) {
                    Timber.i("Skip Customer Receipt Printing: used declined");
                    promptText = STR_EMPTY;
                    printCustomerCopy = false;
                }
                alreadyAsked = true;
            }
        }

        // After ReceiptType is determined, print
        IMalPrint.PrinterReturn result = IMalPrint.PrinterReturn.SUCCESS;
        String firstPrintFail = "Second receipt not printed as first printing failed !";
        switch( receipt ){
            case MERCHANT: {
                if (!first && firstPrintingStatus != IMalPrint.PrinterReturn.SUCCESS) {
                    Timber.w(firstPrintFail);
                } else {
                    result = printMerchantCopy(d, trans, printMerchantReceipt, context, mal);
                }
                break;
            }
            case CUSTOMER: {
                if (!first && firstPrintingStatus != IMalPrint.PrinterReturn.SUCCESS) {
                    Timber.w(firstPrintFail);
                } else {
                    result = printCardholderCopy(d, trans, alreadyAsked, printCustomerCopy, context, mal);
                }
                break;
            }
            case NONE:
                Timber.d( "Not printing anything" );
                break;
            default:
                break;
        }
        if ( firstPrintingStatus != IMalPrint.PrinterReturn.SUCCESS ) {
            Timber.w(firstPrintFail);
        }

        return result;
    }

    private static IMalPrint.PrinterReturn printCardholderCopy(IDependency d, TransRec trans, boolean alreadyAsked, boolean printReceipt, Context context, IMal mal) {
        if (!EFTPlatform.isAppPrinting()) {
            return IMalPrint.PrinterReturn.SUCCESS;
        }
        Timber.d("printCardholderCopy");
        if (!trans.getTransType().autoTransaction &&
            !trans.getCard().isCardholderPresent() &&
            !alreadyAsked &&
            !printCustomerReceiptPrompt( d, trans )) {
                return IMalPrint.PrinterReturn.SUCCESS;
        }

        return print(d, trans, false, false, printReceipt, context, mal);
    }

    private static IMalPrint.PrinterReturn printMerchantCopy(IDependency d, TransRec trans, boolean printReceipt, Context context, IMal mal) {
        return print(d, trans, true, false, printReceipt, context, mal);
    }

    public static IMalPrint.PrinterReturn print( IDependency d, TransRec trans, boolean merchant, boolean isDuplicate, Context context, IMal mal) {
        return print(d, trans, merchant, isDuplicate, true, context, mal);
    }

    public static IMalPrint.PrinterReturn print( IDependency d, TransRec trans, boolean merchant, boolean isDuplicate, boolean printReceipt, Context context, IMal mal) {

        IMalPrint.PrinterReturn result = IMalPrint.PrinterReturn.SUCCESS;

        // Special case: print signature receipt on terminal if requested
        if (merchant && trans.isApprovedOrDeferred() && trans.isSignatureRequired() &&
                trans.getTransType().autoTransaction && trans.getTransEvent() != null && trans.getTransEvent().isUseTerminalPrinterForSignatureCheck()) {
            // also logs receipt to logcat

            buildAndBroadcastReceipt(d, trans, ReceiptType.MERCHANT, isDuplicate, printReceipt, context, mal);
            Timber.i( "Signature check. Printing on terminal as requested in event" );
            if (printReceipt) {
                result = trans.print(d, merchant, isDuplicate, false, mal);

            }
        }
        else if (trans.getTransType().autoTransaction && trans.getTransEvent() != null && trans.getTransEvent().isPosPrintingSync()) {
            Timber.i("sending %s receipt to POS", merchant ? "MERCHANT" : "CUSTOMER");
            // also logs receipt to logcat
            buildAndBroadcastReceipt(d, trans, merchant ? ReceiptType.MERCHANT : ReceiptType.CUSTOMER, isDuplicate, printReceipt, context, mal);
            if (trans.isPrintOnTerminal() && printReceipt) {
                Timber.i("auto transaction but isPrintOnTerminal is true");
                result = trans.print(d, merchant, isDuplicate, false, mal);
            }
        } else if (printReceipt) { // Print our normal way
            Timber.i("NOT auto transaction so printing on terminal");
            result = trans.print(d, merchant, isDuplicate, true, mal);
        }
        return result;
    }

    @SuppressWarnings("java:S6541")
    public static void doPrintWithOptionsMenu( IDependency d, TransRec trans, boolean isDuplicate, boolean runningTransaction, IMal mal) {
        IUIDisplay ui = d.getUI();

        Timber.i( "Print To Screen");

        while (true) {
            HashMap<String, Object> map = new HashMap<>();
            ArrayList<DisplayQuestion> options = new ArrayList<>();

            if (runningTransaction) {

                options.add(new DisplayQuestion(String_id.STR_VIEW_CARDHOLDER_COPY, "ViewCardHolder", DisplayQuestion.EButtonStyle.BTN_STYLE_GREY_LEFT));
                options.add(new DisplayQuestion(String_id.STR_VIEW_MERCHANT_COPY, "ViewMerchant", DisplayQuestion.EButtonStyle.BTN_STYLE_GREY_RIGHT));
                if (d.getPayCfg().isMailEnabled()) {
                    options.add(new DisplayQuestion(String_id.STR_MAIL_CARDHOLDER_COPY, "MailCardHolder", DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
                }
                options.add(new DisplayQuestion(String_id.STR_DONE, "Done", DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));

            } else {
                options.add(new DisplayQuestion(String_id.STR_VIEW_CARDHOLDER_COPY, "ViewCardHolder", DisplayQuestion.EButtonStyle.BTN_STYLE_GREY_LEFT));
                options.add(new DisplayQuestion(String_id.STR_VIEW_MERCHANT_COPY, "ViewMerchant", DisplayQuestion.EButtonStyle.BTN_STYLE_GREY_RIGHT));

                if (d.getPayCfg().isMailEnabled()) {
                    options.add(new DisplayQuestion(String_id.STR_MAIL_CARDHOLDER_COPY, "MailCardHolder", DisplayQuestion.EButtonStyle.BTN_STYLE_GREY_LEFT));
                    options.add(new DisplayQuestion(String_id.STR_MAIL_MERCHANT_COPY, "MailMerchant", DisplayQuestion.EButtonStyle.BTN_STYLE_GREY_RIGHT));
                }

                if (EFTPlatform.hasPrinter()) {
                    options.add(new DisplayQuestion(String_id.STR_PRINT_CUSTOMER_RECEIPT, "PrintCustomer", DisplayQuestion.EButtonStyle.BTN_STYLE_GREY_LEFT));
                    options.add(new DisplayQuestion(String_id.STR_PRINT_MERCHANT_COPY, "PrintMerchant", DisplayQuestion.EButtonStyle.BTN_STYLE_GREY_RIGHT));
                    options.add(new DisplayQuestion(String_id.STR_PRINT_BOTH, "Both", DisplayQuestion.EButtonStyle.BTN_STYLE_GREY_LEFT));
                    options.add(new DisplayQuestion(String_id.STR_DONE, "Done", DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_RIGHT));
                } else {
                    options.add(new DisplayQuestion(String_id.STR_DONE, "Done", DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT));
                }
            }


            map.put(IUIDisplay.uiScreenFragType, IUIDisplay.FRAG_TYPE.FRAG_GRID);
            map.put(IUIDisplay.uiScreenOptionList, options);

            if (trans.isApproved())
                map.put(IUIDisplay.uiScreenTitle, UI.getInstance().getPrompt(String_id.STR_APPROVED));
            else if (trans.isCancelled())
                map.put(IUIDisplay.uiScreenTitle, UI.getInstance().getPrompt(String_id.STR_CANCELLED));
            else
                map.put(IUIDisplay.uiScreenTitle, UI.getInstance().getPrompt(String_id.STR_DECLINED));

            d.getUI().showScreen(SELECT_RECEIPT, map);

            IUIDisplay.UIResultCode resultCode = ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.LONG_TIMEOUT);
            if (resultCode == IUIDisplay.UIResultCode.OK) {
                String result = ui.getResultText(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.uiResultText1);

                switch ( result ) {
                    case "ViewCardHolder":
                        Timber.i( "View The Cardholder Receipt" );
                        printReceipt( d, trans, false, PRINT_PREFERENCE_SCREEN, isDuplicate, mal );

                        break;
                    case "ViewMerchant":
                        Timber.i( "View The Merchant Receipt" );
                        printReceipt( d, trans, true, PRINT_PREFERENCE_SCREEN, isDuplicate, mal );

                        break;
                    case "MailCardHolder":
                        Timber.i( "Mail The CardholderReceipt" );
                        emailCardholderReceipt( d, trans );

                        break;
                    case "MailMerchant":
                        Timber.i( "Mail The MerchantReceipt" );
                        emailMerchantReceipt( d, trans );

                        break;
                    case "PrintCustomer":
                        Timber.i( "Print The CustomerReceipt" );
                        trans.print( d, false, isDuplicate, mal );

                        break;
                    case "PrintMerchant":
                        Timber.i( "Print The MerchantReceipt" );
                        trans.print( d, true, isDuplicate, mal );

                        break;
                    case "Both":
                        Timber.i( "Print The MerchantReceipt" );
                        if ( trans.print( d, true, isDuplicate, mal ) == IMalPrint.PrinterReturn.SUCCESS ) {
                            // else go on to print customer copy. prompt user to remove merchant copy first
                            removeReceiptPrompt( d, REMOVE_MERCHANT_COPY,trans );
                            trans.print( d, false, isDuplicate, mal );
                        }
                        break;
                    case "Done":
                        return;
                    default:
                        break;
                }
            } else {
                break;
            }
        }
    }

    private static void emailMerchantReceipt(IDependency d, TransRec trans) {

        if (d.getPayCfg().isMailEnabled()) {
            trans.getProtocol().setMerchantEmailToUpload(true);
            trans.save();

            d.getUI().showScreen(MERCHANT_EMAIL_QUEUED);
        }
    }

    private static void emailCardholderReceipt(IDependency d, TransRec trans) {

        if (!d.getPayCfg().isMailEnabled()) {
            return;
        }

        HashMap<String, Object> map = new HashMap<>();

        map.put(IUIDisplay.uiScreenMinLen, 1);

        d.getUI().showInputScreen(ENTER_EMAIL, map );
        IUIDisplay.UIResultCode res = d.getUI().getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {

            String result = d.getUI().getResultText(ACT_INPUT, IUIDisplay.uiResultText1);
            if (!Util.isNullOrEmpty(result)) {
                trans.getProtocol().setCustomerEmailToUpload(true);
                trans.getProtocol().setMailCustomerAddress(result);
                trans.save();
                Timber.i( "Send email to %s", result);

                d.getUI().showScreen(CUSTOMER_EMAIL_QUEUED);
            }

        }
    }

    // todo: may want to shift this into the or its own action maybe? not sure as we will have to wait for a response/blocking call
    public static void broadcastReceipt(IDependency d, PositiveReceiptResponse receiptData, final boolean isDuplicate, final TransRec trans, Context context) {
        // log receipt
        printReceiptToDebug(receiptData);

        // Send data to pos
        d.getMessages().sendReceipt(context, receiptData);

        try {
            // Based on testing spec the timeout should be > 60 seconds
            long startTimeInMillis = SystemClock.elapsedRealtime();
            int printResponseTimeoutInMillis = 60 * 1000;
            long timeElapsedSinceStart = 0;
            while (trans != null && !trans.isContinuePrint()) {
                // This logic should stop the terminal from freezing
                if (timeElapsedSinceStart < printResponseTimeoutInMillis) {
                    Thread.sleep(5);
                    timeElapsedSinceStart = SystemClock.elapsedRealtime() - startTimeInMillis;
                } else {
                    // transaction is to be cancelled if the receipt for signature check cannot be sent to POS when printing at POS
                    if (trans.isApprovedOrDeferred() && trans.isSignatureRequired() && !trans.getAudit().isSignatureChecked()
                            && !(trans.getTransEvent().isUseTerminalPrinterForSignatureCheck() || trans.getTransEvent().isUseTerminalPrinter())
                            && !isDuplicate) {
                        Timber.e("Signature receipt couldn't be printed on POS, cancelling..");
                        trans.setToReverse(TIMEOUT);
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    }
                    trans.setContinuePrint(true);
                }
            }
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    public enum ReceiptType {
        MERCHANT,
        CUSTOMER,
        SETTLEMENT,
        LOGON,
        NONE,
    }

    private static final byte USE_INTERNAL_PRINTER_MASK = 0x01;

    /**
     * Calculates the number of copies value for receipts. This value reflects the number of copies
     * THAT THE SHOULD BE PRINTED ON A EXTERNAL PRINTER
     * @param deviceCode the linkly device code that is passed through.
     * @param printReceipt configuration if the receipt is expected to be printed
     * @return number of copies that should be printed on the external printer based off the device code and terminal print configuration
     */
    public static int calculateNumberOfCopies(String deviceCode, boolean printReceipt) {
        // Treat default as 0 as default in case any issues happen.
        int numberOfCopies = 0;

        // For mPOS device code may not exist.
        if(deviceCode != null) {
            // the device code is only a single byte however passed in as string.
            byte[] data = deviceCode.getBytes(StandardCharsets.UTF_8);

            // Should be > 0 but bugs....
            if (data.length >= 1) {
                // the 0x01 (less significant bit) shows if we should use our "internal printer" (aka the terminal printer).
                // NOTE: We don't care if the terminal has a printer or not.
                // If the POS expects to be printed on the terminal and we don't have a printer we just don't print anything.
                // Look at the linkly terminal development spec. Appendix B for masking values.
                boolean useInternalPrinter = (data[0] & USE_INTERNAL_PRINTER_MASK) != 0;
                // We send number of copies as 1 IF we are NOT using our internal printer
                // AND we are expecting to print a receipt. Otherwise keep as 0.
                if (!useInternalPrinter && printReceipt) {
                    numberOfCopies = 1;
                }
            }
        }

        return numberOfCopies;
    }

    public static PositiveReceiptResponse buildReceiptForBroadcast(PrintReceipt receiptToPrint, TransRec trans, ReceiptType type, boolean isDuplicate, boolean printReceipt) {

        PositiveReceiptResponse receiptData = new PositiveReceiptResponse();

        // Build receipt metadata
        if(trans != null) {
            receiptData.setBankApproved(trans.isApproved());
            try {
                receiptData.setTxnRefNumber(trans.getAudit().getReference());
            } catch (Exception e) {
                receiptData.setTxnRefNumber("");
            }
            receiptData.setStan(trans.getProtocol().getStan());

            receiptData.setCopies(calculateNumberOfCopies(trans.getTransEvent().getDeviceCode(), printReceipt));
            receiptData.setDeviceCode(trans.getTransEvent().getDeviceCode());
            receiptData.setCutReceipt(trans.getTransEvent().isCutReceipt());
            receiptData.setAutoPrint(trans.getTransEvent().isAutoPrint());
        }

        if(receiptToPrint !=null) {
            int longestLine = 0;
            int numLines = 0;

            PrintReceipt convertedFormat = receiptToPrint.convertForFixedWidth();
            String[] lines = new String[convertedFormat.getLines().size()];

            ArrayList<PrintReceipt.PrintLine> receiptLines = convertedFormat.getLines();

            for (PrintReceipt.PrintLine line : receiptLines) {
                if (line.getLineText().length() > longestLine) {
                    longestLine = line.getLineText().length();
                }

                lines[numLines++] = line.getLineText();
            }

            // Build receipt data for pos
            receiptData.setReceiptFound(true);
            receiptData.setReceiptData(lines);
            receiptData.setNumberOfLines(numLines);
            receiptData.setLongestLine(longestLine);
        }

        if (isDuplicate) {
            receiptData.setType(DUPLICATE);
        } else {
            switch (type) {
                case SETTLEMENT:
                    receiptData.setType(SETTLEMENT);
                    break;
                case CUSTOMER:
                    receiptData.setType(CUSTOMER);
                    break;
                case MERCHANT:
                    receiptData.setType(MERCHANT);
                    break;
                case LOGON:
                    receiptData.setType(LOGON);
                    break;
                default:
                    Timber.e("Unsupported type");
                    break;
            }
        }

        return receiptData;
    }

    public static PrintReceipt buildAndBroadcastReceipt( IDependency d, IPrintManager.ReportType reportType, IProto.TaskProtocolType protocol, Reconciliation reconciliation, Context context, IMal mal ) {
        IReceipt reportReceipt = d.getPrintManager().getReceiptForReport( d, reportType, protocol, mal);

        reportReceipt.setIsDuplicate( false );
        reportReceipt.setIsMerchantCopy( true );
        reportReceipt.setIsCardHolderCopy( false );

        PrintReceipt printReceipt = reportReceipt.generateReceipt( reconciliation );

        PositiveReceiptResponse receiptResponse = buildReceiptForBroadcast(printReceipt, null, ReceiptType.SETTLEMENT, false, true);

        broadcastReceipt( d, receiptResponse, false, null, context );

        return printReceipt;
    }

    /**
     * converts PrintReceipt object to PositiveTransResult.Receipt object and saves to receipts list in trans record
     * @param trans trans record to update
     * @param isMerchant true if merchant receipt
     * @param receiptToSave receipt data to save
     */
    private static void saveReceiptToTransRec( TransRec trans, boolean isMerchant, PrintReceipt receiptToSave ) {
        // convert PrintReceipt object to PositiveTransResult.Receipt object
        ArrayList<String> outputLines = new ArrayList<>();
        for( PrintReceipt.PrintLine line : receiptToSave.getLines() ) {
            outputLines.add(line.getLineText());
        }

        // add to receipts list
        trans.getReceipts().add( new PositiveTransResult.Receipt(isMerchant?"M":"C", outputLines) );
    }


    public static void buildAndBroadcastReceipt(IDependency d, TransRec trans, ReceiptType type, boolean isDuplicate, Context context, IMal mal) {
        buildAndBroadcastReceipt(d, trans, type, isDuplicate, true, context, mal);
    }

    public static PositiveTransResult.Receipt buildReceipt(IDependency d, TransRec trans, IMal mal, boolean isMerchant) {
        IReceipt receiptGenerator = d.getPrintManager().getReceiptForTrans(d,trans, mal);
        if(receiptGenerator != null) {
            PrintReceipt receiptToPrint = receiptGenerator.generateReceipt(trans);
            PrintReceipt convertedFormat = receiptToPrint.convertForFixedWidth();
            List<String> lines = new ArrayList<>();

            for (PrintReceipt.PrintLine line : convertedFormat.getLines()) {
                lines.add(line.getLineText());
            }

            return new PositiveTransResult.Receipt(isMerchant ? "M" : "C", lines);
        }

        return null;
    }

    public static void buildAndBroadcastReceipt(IDependency d, TransRec trans, ReceiptType type, boolean isDuplicate, boolean printReceipt, Context context, IMal mal) {
        IReceipt receiptGenerator = d.getPrintManager().getReceiptForTrans(d,trans, mal);

        if (receiptGenerator != null) {

            receiptGenerator.setIsDuplicate(isDuplicate);
            boolean merchant = type == ReceiptType.SETTLEMENT || type == ReceiptType.MERCHANT;
            receiptGenerator.setIsMerchantCopy(merchant);
            receiptGenerator.setIsCardHolderCopy(!merchant);

            PrintReceipt receiptToPrint = receiptGenerator.generateReceipt(trans);

            PositiveReceiptResponse receiptData = buildReceiptForBroadcast(receiptToPrint, trans, type, isDuplicate, printReceipt);
            broadcastReceipt(d, receiptData, isDuplicate, trans, context);

            // save receipt copy to transaction record
            saveReceiptToTransRec( trans, merchant, receiptToPrint );
        }
    }

    public static IMalPrint.PrinterReturn printReceipt( IDependency d, TransRec trans, boolean merchant, IPrintManager.PrintPreference printPreference, boolean isDuplicate, IMal mal) {
        IReceipt receiptGenerator = d.getPrintManager().getReceiptForTrans(d,trans, mal);

        if (receiptGenerator != null) {
            receiptGenerator.setIsDuplicate(isDuplicate);
            receiptGenerator.setIsMerchantCopy(merchant);
            receiptGenerator.setIsCardHolderCopy(!merchant);

            PrintReceipt receiptToPrint = receiptGenerator.generateReceipt(trans);
            updateScreenIcons(trans, receiptToPrint);
            return d.getPrintManager().printReceipt(d, receiptToPrint, null, true, STR_EMPTY, printPreference, mal);
        }
        return IMalPrint.PrinterReturn.UNKNOWN_FAILURE;
    }

    public static void removeReceiptPrompt(IDependency d, UIScreenDef screenDef,TransRec trans) {
        IUIDisplay ui = d.getUI();
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<DisplayQuestion> options = new ArrayList<>();

        options.add(new DisplayQuestion(d.getPrompt(String_id.STR_OK), "OP0", DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
        map.put(IUIDisplay.uiScreenOptionList, options);
        Timber.d("Remove Receipt Prompt call");
        ui.showScreen(screenDef, map);
        if (ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_QUESTION,
                d.getPayCfg().getUiConfigTimeouts().
                        getTimeoutMilliSecs(ConfigTimeouts.REMOVE_RECEIPT_TIMEOUT,
                                trans.getAudit().isAccessMode())) == IUIDisplay.UIResultCode.OK) {
            // The Result is not important in this situation
            ui.getResultText(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.uiResultText1);
        }
    }

    private static boolean printCustomerReceiptPrompt(IDependency d,TransRec trans) {
        IUIDisplay ui = d.getUI();
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<DisplayQuestion> options = new ArrayList<>();

        Timber.d("Print Customer Receipt Prompt");

        options.add(new DisplayQuestion(String_id.STR_YES, "OP0", BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
        options.add(new DisplayQuestion(String_id.STR_NO, "OP1", BTN_STYLE_PRIMARY_BORDER_DOUBLE));
        map.put(IUIDisplay.uiScreenOptionList, options);

        d.getStatusReporter().reportStatusEvent(STATUS_CUSTOMER_RECEIPT, trans.isSuppressPosDialog());

        ui.showScreen(PRINT_CUSTOMER_RECEIPT, map);
        IUIDisplay.UIResultCode uiResultCode = ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_QUESTION,
                d.getPayCfg().getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.CUSTOMER_PRINT_RECEIPT_TIMEOUT,
                        trans.getAudit().isAccessMode()));
        if (uiResultCode == IUIDisplay.UIResultCode.POS_YES) {
            return true;
        } else if (uiResultCode == IUIDisplay.UIResultCode.OK) {
            // yes selected if "OP0"
            return ui.getResultText(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.uiResultText1).equals("OP0");
        }
        return false;
    }
}
