package com.linkly.payment.printing.receipts.common;

import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libui.IUIDisplay.String_id.STR_ABOUT_APPLICATION;
import static com.linkly.libui.IUIDisplay.String_id.STR_APN;
import static com.linkly.libui.IUIDisplay.String_id.STR_APP_NAME;
import static com.linkly.libui.IUIDisplay.String_id.STR_CONNECTION_TYPE;
import static com.linkly.libui.IUIDisplay.String_id.STR_CURRENT_SIM;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMV_ENABLED;
import static com.linkly.libui.IUIDisplay.String_id.STR_FAILED;
import static com.linkly.libui.IUIDisplay.String_id.STR_FIRMWARE_VER;
import static com.linkly.libui.IUIDisplay.String_id.STR_MODEL;
import static com.linkly.libui.IUIDisplay.String_id.STR_NO;
import static com.linkly.libui.IUIDisplay.String_id.STR_NO_REC_PERFORMED;
import static com.linkly.libui.IUIDisplay.String_id.STR_PLS_RETAIN_RECEIPTS;
import static com.linkly.libui.IUIDisplay.String_id.STR_PTID;
import static com.linkly.libui.IUIDisplay.String_id.STR_REASON;
import static com.linkly.libui.IUIDisplay.String_id.STR_RECONCILIATION_TIME;
import static com.linkly.libui.IUIDisplay.String_id.STR_SIM1;
import static com.linkly.libui.IUIDisplay.String_id.STR_SIM2;
import static com.linkly.libui.IUIDisplay.String_id.STR_SOFTWARE_VER;
import static com.linkly.libui.IUIDisplay.String_id.STR_SUCCESS;
import static com.linkly.libui.IUIDisplay.String_id.STR_TERMINAL_SN;
import static com.linkly.libui.IUIDisplay.String_id.STR_YES;

import com.linkly.libconfig.DownloadCfg;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.printing.PrintReceipt;

public class AboutAppReceipt extends Receipt {


    @Override
    public PrintReceipt generateReceipt(Object obj) {
        TransRec tran = (TransRec) obj;

        PayCfg paycfg = d.getPayCfg();
        DownloadCfg dwncfg = d.getDownloadCfg();
        PrintReceipt receipt = new PrintReceipt();

        // find latest reconciliation
        TransRec rec = TransRecManager.getInstance().getTransRecDao().getLatestByTransType( RECONCILIATION );

        this.addHeader(receipt);
        this.addUserDetails(receipt);
        this.addPosIdAndReceiptNumber(receipt, null);
        this.addVersion(receipt);
        this.addMID(receipt);
        this.addSpaceLine(receipt);

        String displayName;
        if (tran != null) {
            displayName = tran.getTransType().getDisplayName().toUpperCase();
        } else {
            displayName = "----"+getText(STR_ABOUT_APPLICATION)+"----";
        }
        addLineCentered(receipt, displayName);

        this.addSpaceLine(receipt);

        if (tran != null && tran.getTransType() == EngineManager.TransType.TESTCONNECT) {
            if (tran.getProtocol().getHostResult() == TProtocol.HostResult.AUTHORISED) {
                addLineCentered(receipt, getText(STR_SUCCESS).toUpperCase(), LARGE_FONT);
                this.addSpaceLine(receipt);

            } else {
                this.addSpaceLine(receipt);
                addLineCentered(receipt, getText(STR_FAILED).toUpperCase(), LARGE_FONT, false, true);

                receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_REASON) + tran.getProtocol().getHostResult().displayName));
                this.addSpaceLine(receipt);
            }
        }

        /*About App Content*/

        String SN = mal.getHardware().getSerialNumber();

        if (EFTPlatform.isPaxTerminal()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_CONNECTION_TYPE).toUpperCase()+": ", d.getComms().getCommsType(d)));
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_FIRMWARE_VER)+": ", mal.getHardware().getFirmwareVersion()));
        }
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_PTID)+": ", SN));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_TERMINAL_SN), SN));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_MODEL), EFTPlatform.getModel()));

        if (rec != null) {
            String recDate = rec.getAudit().getTransDateTimeAsString("dd/MM/yyyy HH:mm");
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_RECONCILIATION_TIME), recDate));
        } else {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_RECONCILIATION_TIME), getText(STR_NO_REC_PERFORMED)));
        }
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SOFTWARE_VER), paycfg.getPaymentAppVersion()));
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_EMV_ENABLED), (paycfg.isEmvSupported() ? getText(STR_YES) : getText(STR_NO))));

        if (EFTPlatform.isPaxTerminal()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_APP_NAME)+" ("+getText(STR_SIM1)+"): ", dwncfg.getGprs().getPrimary().getApnName()));
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_APN)+"      ("+getText(STR_SIM1)+"): ", dwncfg.getGprs().getPrimary().getApn()));
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_APP_NAME)+" ("+getText(STR_SIM2)+"): ", dwncfg.getGprs().getSecondary().getApnName()));
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_APN)+"      ("+getText(STR_SIM2)+"): ", dwncfg.getGprs().getSecondary().getApn()));
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_CURRENT_SIM)+"    : ", "1"));
        }

        this.addSpaceLine(receipt);

        /*Date and time*/
        addDateTimeLine(receipt);

        this.addSpaceLine(receipt);

        addLineCentered(receipt, getText(STR_PLS_RETAIN_RECEIPTS));

        return receipt;
    }
}
