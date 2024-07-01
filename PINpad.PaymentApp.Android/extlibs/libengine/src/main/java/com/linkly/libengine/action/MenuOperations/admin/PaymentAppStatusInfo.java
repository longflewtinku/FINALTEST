package com.linkly.libengine.action.MenuOperations.admin;

import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_FINISHED;

import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.os.StatFs;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.libmal.IMalPrint;
import com.linkly.libpositive.wrappers.PositivePaymentAppInfoResult;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class PaymentAppStatusInfo extends IAction {


    // Need to mirror the merchant number, pass in merchant number for the constructor
    private String merchantNumber = "";



    public PaymentAppStatusInfo(String merchantNumber) {
        this.merchantNumber = merchantNumber;
    }

    // Map to our linkly status info
    // TODO: do we want thing in the payment app as this is linkly stuff
    private enum Device {

        VERIFONE_CARBON_10("v070"),
        VERIFONE_CARBON_8("v071"),
        VERIFONE_CARBON_5("v072"),
        CBA_ALBERT("Albt"),
        LINKLY_VPP("p010"),
        PAX_A920("x920"),
        PAX_A920_PRO("x92P"),
        PAX_A77("x770"),
        PAX_A30("x300"),
        PAX_A35("x350"),
        PAX_A80("x800"),
        PAX_A910S("x91S"),
        SUNMI_P2_PRO("sP2P"),
        SUNMI_P2_LITE("sP2L"),
        INGENICO_A8("iA80");

        String id;

        public String getId() {
            return id;
        }

        Device(String id) {
            this.id = id;
        }
    }

    @Override
    public String getName() {
        return "PaymentAppStatusInfo";
    }

    @Override
    public void run() {

        PositivePaymentAppInfoResult status = new PositivePaymentAppInfoResult();

        status.setMerchantNumber(merchantNumber); // Not wanting to support right now
        status.setTID(d.getPayCfg().getStid()); //Terminal ID 8    Terminal ID as set by the initialisation request
        status.setMID(d.getPayCfg().getMid()); //Merchant ID 15    Merchant ID as set by the initialisation request
        status.setSoftwareVersionNumber(d.getPayCfg().getPaymentAppVersion()); //Software Version No. 16    PIN pad Software version
        status.setNII(d.getPayCfg().getPaymentSwitch().getNii()); //    NII In Protocol.
        status.setAIIC(d.getPayCfg().getPaymentSwitch().getAiic()); //    Acquirer Institution Identification Code Protocol.
        status.setTxnTimeout(Integer.toString(d.getPayCfg().getPaymentSwitch().getReceiveTimeout())); // TODO FIX // Protocol configuration bank ip and host. If we cant find one we will have to add.
        status.setAcquirer(d.getCustomer() != null ? d.getCustomer().getAcquirerCode():" "); // get from customer class
        status.setBankDescription(d.getPayCfg().getBankDescription()); //Bank Description add to param override.xml
        status.setKvc(""); // Not used
        status.setSafCount(Integer.toString(TransRec.countTransInBatch()));

        status.setNetworkType("1"); //Network Type 1        ‘1’ = Leased, ‘2’= Dial-up
        status.setRetailerName2085(d.getPayCfg().getRetailerName()); // payparamcfg.java may have to extend when parsing XMLS


        status.setTipping(d.getPayCfg().isTipAllowed()); //Tipping pay cfg.
        status.setPreauth(d.getPayCfg().isPreAuthTransAllowed()); //Pre-Auth
        status.setCompletions(d.getPayCfg().isCompletionTransAllowed()); //Completions
        status.setCashout(d.getPayCfg().isCashTransAllowed()); //Cash Out
        status.setRefund(d.getPayCfg().isRefundTransAllowed()); //Refund
        status.setBalanceEnquiries(d.getPayCfg().isBalanceTransAllowed()); //Balance Enquiries
        status.setDeposits(d.getPayCfg().isDepositTransAllowed()); //Deposits
        status.setVoucherEntry(false); // Not supporting right now
        status.setMoto(d.getPayCfg().isManualAllowed()); //MOTO Transactions
        status.setAutoCompletions(false); // Not supporting right now
        status.setEfb(d.getPayCfg().isEfbSupported()); // Dont know to find out
        status.setEmv(d.getPayCfg().isEmvSupported()); // grab from config.
        status.setTrainingMode(false); // Not supporting
        // These are used for in-bank terminals. Not used
        status.setWithdrawal(false);
        status.setFundsTranafer(false);
        status.setStartCash(false);
        status.setPinRequest(false);

        setTerminalPeripherals(status);
        status.setModem(PositivePaymentAppInfoResult.TERMINAL_MODEM.COMMS3G_4G); // Keep at 3g for now
        setCommsHW(status);
        status.setLinklyCloud(true); //Terminal Supports Linkly cloud
        status.setSurcharge(d.getPayCfg().isSurchargeSupported());
        status.setMultimerchant(false); // Not supporting right now
        status.setTouchScreen(true); //Terminal Has a Touch screen

        status.setStandInCreditCardLimit(convertCentsTo9Digit(d.getPayCfg().getSaleLimitCents()));
        status.setStandInDebitCardLimit(convertCentsTo9Digit(d.getPayCfg().getSaleLimitCents()));
        status.setStandInMaxNumberStandInTrans(d.getPayCfg().getMaxEfbTrans());

        status.setScheme(PositivePaymentAppInfoResult.KEY_HANDLING_SCHEME.TRIPLE_DES); // Forced to 3des

        status.setMaxCashoutLimit( convertCentsTo9Digit(d.getPayCfg().getCashoutLimitCents())); // CardProducts.xml Might be global ones. If not payconfig.

        // for some reason maxRefundLimit is type "String" in Cents, convert to Double "$$$$$$$.cc"
        status.setMaxRefundLimit(convertCentsTo9Digit(Integer.parseInt(d.getPayCfg().getMaxRefundLimit())));
        status.setCpatVersion(Integer.toString(d.getPayCfg().getCardProductVersion())); // Add version as root element in cardproduct.json (Still not working)
        status.setNameTableVersion(("")); // Dont need to support
        status.setCommsType("0"); // Forced comms type is always 0. Legacy stuff
        status.setCardMisreadCount("0"); // Dont track this for now
        status.setTotalMemeory(getTotalInternalMemorySize()); //Total # memory pages (will be GB).
        status.setFreeMemory(getAvailableInternalMemorySize()); //# free memory pages (will be GB).
        populateHardwareSpecificFields(status);
        status.setNumApplicationOnTerminal(getNumberOfApps()); //# applications in terminal
        status.setNumLinesOnDisplay("10"); // Hardcoded as 10
        status.setHwInceptionDate(mal.getHardware().getHardwareInceptionDate());
        status.setResponseCode("T0");
        status.setResponseText("APPROVED");

        d.getMessages().sendPaymentAppInfoResponse(context, status);

        posPowerFail();

        // Send any config debug to the pos
        sendConfigDebugToPos();
    }

    /**
     * convert double (dollars) value, e.g. $10,000.00 has double value of 10000.0
     * this needs to be converted to $$$$$$$cc value, without decimal, i.e. 001000000
     *
     * @param value input double value to convert
     * @return 9 digit $$$$$$$cc formatted value
     */
    private static String convertCentsTo9Digit(int value) {
        return value > 999999999 ? "999999999" : String.format( Locale.getDefault(), "%09d", value);
    }

    //
    private void sendConfigDebugToPos() {
        if(d.getDebugReporter() != null) {
            List<Exception> errors = d.getConfig().getConfigErrors();
            if(errors != null) {
                for (Exception e : d.getConfig().getConfigErrors()) {
                    d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.CONFIG_FAILURE, e.getMessage());
                }
            }
        }
    }

    private void posPowerFail() {

        boolean suppressPosDialog = false;
        // ignore this response if we are running a transaction
        if(WorkflowScheduler.isTransactionRunning()) {
            return;
        }

        TransRec last = TransRec.getLatestFinancialTxn();

        // If we didn't report the result to the pos, (Power fail situation) and the last transaction is an Automated Txn
        // Just report it to the POS.
        if(last != null && !last.isReportedToPOS() && last.getTransType().autoTransaction) {

            last.debug();

            Timber.i("Reporting Transaction to POS");

            // Send back transaction message
            ECRHelpers.ipcSendTransResponse( d, last, context );

            // for PAT mode 2 POS dialogs needs to be suppress
            if(last.isPatMode("2")) {
                suppressPosDialog = true;
            }
            // Advise the pos to close window in case the terminal window is still showing
            d.getStatusReporter().reportStatusEvent(STATUS_TRANS_FINISHED, suppressPosDialog);

            // update the record as we have now reported the result to the pos
            last.setReportedToPOS(true);
            last.save();
        }
    }

    private void populateHardwareSpecificFields(PositivePaymentAppInfoResult status) {

        if( status == null) {
            Timber.e( "ERROR " + PositivePaymentAppInfoResult.class.getSimpleName() + " is null");
            return;
        }

        status.setPinPadSN(mal.getHardware().getSerialNumber()); //PIN pad Serial Number 16    PIN pad Serial Number in ASCII
        status.setHardwareSerial(mal.getHardware().getSerialNumber()); //Hardware Serial #            16    Actual serial number that is unique to the h/w

        String model;

        Timber.i( "Terminal model %s", mal.getHardware().getModel());
        switch(mal.getHardware().getModel()) {
            case "A920":
                model = Device.PAX_A920.getId();
                break;

            case "A77":
                model = Device.PAX_A77.getId();
                break;

            case "A30":
                model = Device.PAX_A30.getId();
                break;

            case "A35":
                model = Device.PAX_A35.getId();
                break;

            case "A920PRO":
                model = Device.PAX_A920_PRO.getId();
                break;

            case "A80":
                model = Device.PAX_A80.getId();
                break;

            case "A910S":
                model = Device.PAX_A910S.getId();
                break;

            // TODO: What string will non PAX terminals return here?
            default:
                Timber.e( "Unknown terminal model %s", mal.getHardware().getModel());
                model = "NA";
                break;
        }

        status.setTerminalType(model);
    }

    private void setCommsHW(PositivePaymentAppInfoResult status) {

        if (status == null) {
            return;
        }

        PositivePaymentAppInfoResult.TERMINAL_COMMS_HW comms_hw = PositivePaymentAppInfoResult.TERMINAL_COMMS_HW.NONE;

        if (mal.getHardware().hasEthernet() && mal.getHardware().hasWifi()) {
            comms_hw = PositivePaymentAppInfoResult.TERMINAL_COMMS_HW.BOTH;
        } else if (mal.getHardware().hasEthernet()) {
            comms_hw = PositivePaymentAppInfoResult.TERMINAL_COMMS_HW.ETHERNET;
        } else if (mal.getHardware().hasWifi()) {
            comms_hw = PositivePaymentAppInfoResult.TERMINAL_COMMS_HW.WIFI;
        } else {
            Timber.i( "Warning no ethernet or wifi" );
        }

        Timber.i( "Result: %s", comms_hw.name());


        status.setComms_hw(comms_hw);
    }

    /**
     * Checks if terminal has Barcode & Printer peripherals.
     * For printer, it will check if the terminal has a physical printer + printer status is ready/Ok
     * @param status {@link PositivePaymentAppInfoResult} object
     * */
    private void setTerminalPeripherals( PositivePaymentAppInfoResult status ) {

        if ( status == null ) {
            return;
        }

        PositivePaymentAppInfoResult.TERMINAL_PERIPHERALS peripherals = PositivePaymentAppInfoResult.TERMINAL_PERIPHERALS.NONE;
        boolean hasPrinter = ( mal.getHardware().hasPrinter() &&
                ( IMalPrint.PrinterReturn.SUCCESS == mal.getHardware().getMalPrint().getPrinterStatus() ) );

        if ( mal.getHardware().hasBarcodeReader() && hasPrinter ) {
            peripherals = PositivePaymentAppInfoResult.TERMINAL_PERIPHERALS.BOTH;
        } else if ( mal.getHardware().hasBarcodeReader() ) {
            peripherals = PositivePaymentAppInfoResult.TERMINAL_PERIPHERALS.BARCODE_READER;
        } else if ( hasPrinter ) {
            peripherals = PositivePaymentAppInfoResult.TERMINAL_PERIPHERALS.PRINTER;
        } else {
            Timber.i( "Warning no barcode and printer" );
        }

        Timber.i( "Result: %s", peripherals.name() );

        status.setPeripherals( peripherals );// Check terminal features in MAL layer
    }

    // returns number of non system apps on the terminal
    @SuppressWarnings("deprecation")
    private String getNumberOfApps() {

        int numberOfNonSystemApps = 0;

        List<ApplicationInfo> appList = context.getPackageManager().getInstalledApplications(0);
        for(ApplicationInfo info : appList) {
            // Ignore all system apps
            if((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                numberOfNonSystemApps++;
            }
        }

        Timber.i( "Number of Apps:%s", numberOfNonSystemApps);

        return Integer.toString(numberOfNonSystemApps);
    }

    public static String formatSize(double sizeBytes) {

        sizeBytes /= 1024; // Convert to KB
        sizeBytes /= 1024; // Convert to MB
        sizeBytes /= 1024;  // Convert to GB

        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(sizeBytes);
    }

    public static String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();

        Timber.i( "Available Size: %s", (availableBlocks * blockSize) );

        return formatSize(availableBlocks * blockSize);
    }

    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        Timber.i( "Total Size: %s", (totalBlocks * blockSize) );
        return formatSize(totalBlocks * blockSize);
    }
}
