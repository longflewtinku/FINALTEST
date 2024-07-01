package com.linkly.payment.printing.receipts.generic;

import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libui.IUIDisplay.String_id.STR_BATTERY_LEVEL;
import static com.linkly.libui.IUIDisplay.String_id.STR_CONNECTION_TYPE;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMV_ENABLED;
import static com.linkly.libui.IUIDisplay.String_id.STR_FAILED;
import static com.linkly.libui.IUIDisplay.String_id.STR_FIRMWARE_VER;
import static com.linkly.libui.IUIDisplay.String_id.STR_LAST_UPDATE_CHECK;
import static com.linkly.libui.IUIDisplay.String_id.STR_LAST_Z_REPORT;
import static com.linkly.libui.IUIDisplay.String_id.STR_MODEL;
import static com.linkly.libui.IUIDisplay.String_id.STR_NO;
import static com.linkly.libui.IUIDisplay.String_id.STR_NO_REC_PERFORMED;
import static com.linkly.libui.IUIDisplay.String_id.STR_PTID;
import static com.linkly.libui.IUIDisplay.String_id.STR_REASON;
import static com.linkly.libui.IUIDisplay.String_id.STR_RECONCILIATION_TIME;
import static com.linkly.libui.IUIDisplay.String_id.STR_SOFTWARE_VER;
import static com.linkly.libui.IUIDisplay.String_id.STR_STORED_TRANSACTIONS;
import static com.linkly.libui.IUIDisplay.String_id.STR_TERMINAL_SN;
import static com.linkly.libui.IUIDisplay.String_id.STR_YES;
import static com.linkly.payment.positivesvc.MessageReceiver.getBatteryPercentage;

import android.content.Intent;
import android.content.IntentFilter;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.platform.Platform;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.payment.activities.AppMain;
import com.linkly.payment.utilities.PrintUtilities;

import java.util.Collections;
import java.util.List;

public class AboutReceipt extends Receipt {
    @Override
    public PrintReceipt generateReceipt(Object obj) {
        TransRec tran = (TransRec) obj;

        PayCfg paycfg = d.getPayCfg();
        PrintReceipt receipt = new PrintReceipt();

        // find latest reconciliation
        TransRec rec = TransRecManager.getInstance().getTransRecDao().getLatestByTransType( RECONCILIATION );

        receipt.getLines().add( new PrintReceipt.PrintLine(getText(String_id.STR_SYS_INFO)) );
        this.addUserDetails(receipt);
        this.addPosIdAndReceiptNumber(receipt, null);
        this.addVersion(receipt);
        this.addMaskedMID(receipt, null, SMALL_FONT);
        this.addSpaceLine(receipt);

        if (tran != null) {
            addLineCentered(receipt, tran.getTransType().getDisplayName().toUpperCase());
        } else {
            addLineCentered(receipt, "----"+getText(String_id.STR_SYS_INFO).toUpperCase()+"----", MEDIUM_FONT);
        }

        this.addSpaceLine(receipt);

        if (tran != null && tran.getTransType() == EngineManager.TransType.TESTCONNECT) {
            if (tran.getProtocol().getHostResult() == TProtocol.HostResult.AUTHORISED) {
                addLineCentered(receipt, getText(String_id.STR_SUCCESS).toUpperCase(), LARGE_FONT);
                this.addSpaceLine(receipt);

            } else {
                this.addSpaceLine(receipt);
                addLineCentered(receipt, getText(STR_FAILED).toUpperCase(), LARGE_FONT, false, true);

                receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_REASON) + tran.getProtocol().getHostResult().displayName));
                this.addSpaceLine(receipt);
            }
        }

        /*About App Content*/
        //d.getMal().getHardware().

        String serialNumber = mal.getHardware().getSerialNumber();

        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_CONNECTION_TYPE).toUpperCase()+": ", Util.GetConnectedNetworkName(mal.getMalContext()), SMALL_FONT));
        if (Platform.isPaxTerminal()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_FIRMWARE_VER)+": ", mal.getHardware().getFirmwareVersion(), SMALL_FONT));
        }
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_PTID)+": ", serialNumber, SMALL_FONT));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_TERMINAL_SN), serialNumber, SMALL_FONT));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_MODEL), Platform.getModel(), SMALL_FONT));

        int batterylevel = 0;
        for(int i=0; i < 5; i++) {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = AppMain.getApp().getApplicationContext().registerReceiver(null, ifilter);
            batterylevel = getBatteryPercentage(batteryStatus);
            if (batterylevel > 0)
                break;
        }

        // battery level
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_BATTERY_LEVEL)+" ", batterylevel + "%", SMALL_FONT));

        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_EMV_ENABLED), (paycfg.isEmvSupported() ? getText(STR_YES) : getText(STR_NO)), SMALL_FONT));
        if (rec != null) {
            String recDate = rec.getAudit().getTransDateTimeAsString("dd/MM/yyyy HH:mm");
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_RECONCILIATION_TIME), recDate));
        } else {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_RECONCILIATION_TIME), getText(STR_NO_REC_PERFORMED), SMALL_FONT));
        }
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SOFTWARE_VER), paycfg.getPaymentAppVersion(), SMALL_FONT));

        this.addSpaceLine(receipt);

        PrintUtilities.addNetworkStatus(d, receipt);
        this.addSpaceLine(receipt);

        List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findAll();
        Collections.reverse(allTrans);

        boolean latestReconFound = false;
        String latestReconDateTime = "";
        for ( TransRec trans : allTrans) {
            if (trans.getTransType() == RECONCILIATION && !latestReconFound) {
                latestReconFound = true;
                latestReconDateTime = trans.getAudit().getLastTransmissionDateTimeAsString("dd/MM/yyyy - hh:mm:ss");
            }
        }
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_STORED_TRANSACTIONS).toUpperCase()+": ", "" + TransRec.countTransInBatch(), SMALL_FONT));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_LAST_Z_REPORT), latestReconDateTime, SMALL_FONT));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_LAST_UPDATE_CHECK), d.getDownloadCfg().getLastDownload(), SMALL_FONT));

        this.addSpaceLine(receipt);

        /*Date and time*/
        addDateTimeLine(receipt);

        this.addSpaceLine(receipt);

        return receipt;
    }
}
