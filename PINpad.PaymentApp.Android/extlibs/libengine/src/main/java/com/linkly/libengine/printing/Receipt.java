package com.linkly.libengine.printing;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CHEQUE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CREDIT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_SAVINGS;
import static com.linkly.libmal.global.printing.PrintReceipt.FONT.FONT_16_24;
import static com.linkly.libmal.global.printing.PrintReceipt.FONT.FONT_16_32;
import static com.linkly.libmal.global.printing.PrintReceipt.FONT.FONT_8_16;
import static com.linkly.libmal.global.printing.PrintReceipt.FONT.FONT_FIXED_WIDTH_POS;
import static com.linkly.libsecapp.emv.Tag.cvm_results;
import static com.linkly.libui.IUIDisplay.String_id.STR_ACCOUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_AID;
import static com.linkly.libui.IUIDisplay.String_id.STR_AIP;
import static com.linkly.libui.IUIDisplay.String_id.STR_AMOUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_AMT;
import static com.linkly.libui.IUIDisplay.String_id.STR_APP_PSN;
import static com.linkly.libui.IUIDisplay.String_id.STR_APP_START_DATE_FORMAT;
import static com.linkly.libui.IUIDisplay.String_id.STR_ATC;
import static com.linkly.libui.IUIDisplay.String_id.STR_AUTHORISED;
import static com.linkly.libui.IUIDisplay.String_id.STR_AUTH_CODE;
import static com.linkly.libui.IUIDisplay.String_id.STR_CALL_ISSUER;
import static com.linkly.libui.IUIDisplay.String_id.STR_CARDHOLDER_NOT_PRESENT;
import static com.linkly.libui.IUIDisplay.String_id.STR_CARDHOLDER_SIGN;
import static com.linkly.libui.IUIDisplay.String_id.STR_CARD_BALANCE;
import static com.linkly.libui.IUIDisplay.String_id.STR_CASH;
import static com.linkly.libui.IUIDisplay.String_id.STR_CBK;
import static com.linkly.libui.IUIDisplay.String_id.STR_CDO_INDICATOR;
import static com.linkly.libui.IUIDisplay.String_id.STR_CER;
import static com.linkly.libui.IUIDisplay.String_id.STR_CHQ;
import static com.linkly.libui.IUIDisplay.String_id.STR_CONTACTLESS;
import static com.linkly.libui.IUIDisplay.String_id.STR_CRD;
import static com.linkly.libui.IUIDisplay.String_id.STR_CRYPTOGRAM;
import static com.linkly.libui.IUIDisplay.String_id.STR_CRYPTOGRAM_TYPE;
import static com.linkly.libui.IUIDisplay.String_id.STR_CTLS;
import static com.linkly.libui.IUIDisplay.String_id.STR_CVR;
import static com.linkly.libui.IUIDisplay.String_id.STR_DATE;
import static com.linkly.libui.IUIDisplay.String_id.STR_DEBIT_ACT_TEXT;
import static com.linkly.libui.IUIDisplay.String_id.STR_DEFERRED;
import static com.linkly.libui.IUIDisplay.String_id.STR_DIAGNOSTIC_CODE;
import static com.linkly.libui.IUIDisplay.String_id.STR_DIGITAL_SIG;
import static com.linkly.libui.IUIDisplay.String_id.STR_DISC;
import static com.linkly.libui.IUIDisplay.String_id.STR_DISCOUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_DUPLICATE_RECEIPT;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMV_TAGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_EXPIRY_DATE_FORMAT;
import static com.linkly.libui.IUIDisplay.String_id.STR_GRATUITY;
import static com.linkly.libui.IUIDisplay.String_id.STR_ICC;
import static com.linkly.libui.IUIDisplay.String_id.STR_KEYED;
import static com.linkly.libui.IUIDisplay.String_id.STR_KEYED_MAIL_ORDER;
import static com.linkly.libui.IUIDisplay.String_id.STR_KEYED_TEL_ORDER;
import static com.linkly.libui.IUIDisplay.String_id.STR_MCR_INDICATOR;
import static com.linkly.libui.IUIDisplay.String_id.STR_MID;
import static com.linkly.libui.IUIDisplay.String_id.STR_MSR;
import static com.linkly.libui.IUIDisplay.String_id.STR_NAME;
import static com.linkly.libui.IUIDisplay.String_id.STR_NOT_AUTHORISED;
import static com.linkly.libui.IUIDisplay.String_id.STR_NO_CARDHOLDER;
import static com.linkly.libui.IUIDisplay.String_id.STR_NO_CARDHOLDER_VERIFICATION;
import static com.linkly.libui.IUIDisplay.String_id.STR_PID;
import static com.linkly.libui.IUIDisplay.String_id.STR_PIN_VERIFIED;
import static com.linkly.libui.IUIDisplay.String_id.STR_PLS_CREDIT_MY_ACT;
import static com.linkly.libui.IUIDisplay.String_id.STR_PLS_DEBIT_MY_ACT;
import static com.linkly.libui.IUIDisplay.String_id.STR_PLS_SIGN_BELOW;
import static com.linkly.libui.IUIDisplay.String_id.STR_REASON;
import static com.linkly.libui.IUIDisplay.String_id.STR_RECEIPT_MERCHANT_COPY;
import static com.linkly.libui.IUIDisplay.String_id.STR_RECEIPT_NOT_INVOICE;
import static com.linkly.libui.IUIDisplay.String_id.STR_INVOICE_NO;
import static com.linkly.libui.IUIDisplay.String_id.STR_RECEIPT_SAF_RECORD;
import static com.linkly.libui.IUIDisplay.String_id.STR_REFERENCE;
import static com.linkly.libui.IUIDisplay.String_id.STR_RESPONSE_CODE;
import static com.linkly.libui.IUIDisplay.String_id.STR_REV;
import static com.linkly.libui.IUIDisplay.String_id.STR_RRN;
import static com.linkly.libui.IUIDisplay.String_id.STR_SAV;
import static com.linkly.libui.IUIDisplay.String_id.STR_SCG;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTLEMENT_DATE_FORMAT;
import static com.linkly.libui.IUIDisplay.String_id.STR_STAN;
import static com.linkly.libui.IUIDisplay.String_id.STR_SURCHARGE;
import static com.linkly.libui.IUIDisplay.String_id.STR_SWIPED;
import static com.linkly.libui.IUIDisplay.String_id.STR_TID;
import static com.linkly.libui.IUIDisplay.String_id.STR_TIME;
import static com.linkly.libui.IUIDisplay.String_id.STR_TIP;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOT;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTAL;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTAL_SHOWN;
import static com.linkly.libui.IUIDisplay.String_id.STR_TSI;
import static com.linkly.libui.IUIDisplay.String_id.STR_TVR;
import static com.linkly.libui.IUIDisplay.String_id.STR_UNABLE_TO_AUTHORISE;
import static com.linkly.libui.IUIDisplay.String_id.STR_UNABLE_TO_PRINT_RECEIPT;
import static com.linkly.libui.IUIDisplay.String_id.STR_USER_ID;
import static com.linkly.libui.IUIDisplay.String_id.STR_USER_NAME;
import static com.linkly.libui.IUIDisplay.String_id.STR_UTI;
import static com.linkly.libui.IUIDisplay.String_id.STR_VERIFICATION;
import static com.linkly.libui.IUIDisplay.String_id.STR_VERIFIED_BY_DEVICE;
import static com.linkly.libui.IUIDisplay.String_id.STR_VERIFIED_BY_PIN;
import static com.linkly.libui.IUIDisplay.String_id.STR_VERIFIED_BY_SIGN;
import static com.linkly.libui.IUIDisplay.String_id.STR_VERIFIED_PIN_AND_SIGN;
import static com.linkly.libui.IUIDisplay.String_id.STR_VERSION;
import static com.linkly.libui.IUIDisplay.String_id.STR_VIRTUAL_MID;
import static com.linkly.libui.IUIDisplay.String_id.STR_VIRTUAL_NAME;
import static com.linkly.libui.IUIDisplay.String_id.STR_VIRTUAL_TID;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.env.IccDiags;
import com.linkly.libengine.users.User;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.IMal;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libmal.global.printing.PrintReceipt.FONT;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.emv.EmvTag;
import com.linkly.libsecapp.emv.EmvTags;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay.String_id;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Receipt implements IReceipt {
    public static final PrintReceipt.FONT SMALL_FONT = FONT_8_16;
    public static final PrintReceipt.FONT MEDIUM_FONT = FONT_16_24;
    public static final PrintReceipt.FONT LARGE_FONT = FONT_16_32;
    private static final String[] shortFieldNames = {getText(STR_AMT), getText(STR_CBK), getText(STR_TIP), getText(STR_DISC), getText(STR_SCG), getText(STR_TOT)};
    private static final String[] longFieldNames = {getText(STR_AMOUNT).toUpperCase(), getText(STR_CASH).toUpperCase(), getText(STR_GRATUITY).toUpperCase(), getText(STR_DISCOUNT), getText(STR_SURCHARGE).toUpperCase(), getText(STR_TOTAL).toUpperCase()};
    private static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    HashMap<String, VasCounter> vasCounters = new HashMap<>();
    private boolean isDuplicate;
    private boolean isMerchantCopy;
    private boolean isCardHolderCopy;
    private boolean isSAFReversalDelete;

    protected IDependency d = null;

    protected IMal mal = null;

    public void setDependencies( IDependency d, IMal mal) {
        this.mal = mal;
        this.d = d;
    }

    public PrintReceipt generateReceipt(Object obj) {
        return null;
    }

    public PrintReceipt generateReceipt(Object obj, ArrayList<String> keys, Boolean isWhitelist, String configTitle){
        return null;
    }


    @Override
    public void setIsDuplicate(boolean b) {
        isDuplicate = b;
    }

    @Override
    public void setIsMerchantCopy(boolean b) {
        isMerchantCopy = b;
        isCardHolderCopy = !b;
    }

    @Override
    public void setIsCardHolderCopy(boolean b) {
        isCardHolderCopy = b;
        isMerchantCopy = !b;
    }

    @Override
    public void setIsSAFReversalDelete(boolean b) { isSAFReversalDelete = b; }


    public PrintReceipt generateInvalidReceipt() {
        PrintReceipt receipt = new PrintReceipt();

        this.addOptoMerchantDetails(receipt, 2);
        Receipt.addLineCentered(receipt, getText(STR_UNABLE_TO_PRINT_RECEIPT), LARGE_FONT);
        return receipt;
    }


    public static void addLineCentered(PrintReceipt receipt, String text, PrintReceipt.FONT font, boolean bold, boolean inverted) {
        // split lines by \n
        String [] lineStrings = text.split("\n");
        for( String lineString : lineStrings ) {
            PrintReceipt.PrintLine line = new PrintReceipt.PrintLine(lineString, font, bold, PrintReceipt.PrintLine.TextAlignment.CENTER);
            line.setInverted(inverted);
            receipt.getLines().add(line);
        }
    }

    public static void addLineCentered(PrintReceipt receipt, String text, PrintReceipt.FONT font, boolean bold) {
        addLineCentered(receipt, text, font, bold, false);
    }

    public static void addLineCentered(PrintReceipt receipt, String text, PrintReceipt.FONT font) {
        addLineCentered(receipt, text, font, false);
    }

    public static void addLineCentered(PrintReceipt receipt, String text) {
        addLineCentered(receipt, text, PrintReceipt.DEFAULT_FONT);
    }

    public static void addLineCenteredIfNotEmpty(PrintReceipt receipt, String value, PrintReceipt.FONT font) {
        if(!Util.isNullOrWhitespace(value)) {
            addLineCentered(receipt, value, font);
        }
    }

    public void addPosIdAndReceiptNumber(PrintReceipt receipt, Integer receiptNum) {
        PayCfg paycfg = d.getPayCfg();
        String posId = getText(String_id.STR_POS_ID) + paycfg.getStid();

        receipt.getLines().add(new PrintReceipt.PrintLine(posId, SMALL_FONT));
        if (receiptNum != null && receiptNum > 0) {
            String recNum = getText(STR_INVOICE_NO) + " " + Util.padLeft(receiptNum.toString(), 6, '0' );
            receipt.getLines().add(new PrintReceipt.PrintLine(recNum, SMALL_FONT));
        }
    }

    public void addPID(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_PID) + trans.getEftPaymentIdShort(d.getPayCfg().getStid()), font));
    }

    // Attach Mid
    private String getMIDText(TransRec trans) {
        String mid = null;

        if(trans != null) {
            mid = trans.getProtocol().getCardAcceptorNumber();
            if(Util.isNullOrEmpty(mid)) {
                String schemeId = trans.getCard().getCardsConfig(d.getPayCfg()).getSchemeId();
                if(!Util.isNullOrEmpty(schemeId)) {
                    String merchantDepartmentId = trans.getProtocol().getMerchantDepartmentId();
                    mid = d.getPayCfg().getMerchantNumber(schemeId, merchantDepartmentId);
                }
            }
        }

        if(Util.isNullOrEmpty(mid)) {
            mid = d.getPayCfg().getMid();
        }

        return mid;
    }

    private String getMIDText() {
        return getMIDText(null);
    }

    public void addMaskedMID(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        String mid = getMaskedText(getMIDText(trans), 5);

        if(!Util.isNullOrEmpty(mid)) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_MID) + " " + mid, font));
        }
    }

    public void addMID(PrintReceipt receipt) {
        addMID(receipt, LARGE_FONT, null);
    }

    public void addMID(PrintReceipt receipt, PrintReceipt.FONT font, TransRec trans) {
        String mid = getMIDText(trans);

        if (isCardHolderCopy()) {
            mid = getMaskedText(mid, 5);
        }

        if(!Util.isNullOrEmpty(mid)) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_MID) +  mid, font));
        }
    }

    public void addPSN(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        if (trans.getCard().getPsn() >= 0) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_APP_PSN), Util.leftPad("" + trans.getCard().getPsn(), 2, '0'), font));
        }
    }

    /**
     * fit both PSN and ATC on a single line to save paper
     *
     * @param receipt
     * @param trans
     * @param font
     */
    public void addPsnAndAtc(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        String psnLabelAndValue = "";
        if (trans.getCard().getPsn() >= 0) {
            String psnVal;
            psnVal = Util.leftPad("" + trans.getCard().getPsn(), 2, '0');
            psnLabelAndValue = getText(STR_APP_PSN) + " " + psnVal;
        }

        String atcLabelAndValue = "";
        String atcVal = trans.getCard().getAtc();
        if( !Util.isNullOrEmpty( atcVal ) ){
            atcLabelAndValue = getText(STR_ATC) + " " + atcVal;
        }

        receipt.getLines().add(new PrintReceipt.PrintTableLine(psnLabelAndValue, atcLabelAndValue, font));
    }

    public void addStan(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        long stanValue = trans.getProtocol().getStan();

        if( stanValue > 0 ) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_STAN) + " " + Util.leftPad( "" + stanValue, 6, '0'), font));
        }
    }

    /**
     * Adds STAN data into the receipt if valid
     * Create a receipt line with 'STAN' justified as per alignment specified
     * @param receipt {@link PrintReceipt} receipt object
     * @param trans {@link TransRec} the transactions containing STAN
     * @param font {@link PrintReceipt.FONT} font type to be used for this
     * @param font {@link PrintReceipt.PrintLine.TextAlignment} text alignment to be used for this
     * */
    public void addStanJustified(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font, PrintReceipt.PrintLine.TextAlignment textAlignment) {
        long stanValue = trans.getProtocol().getStan();

        if( stanValue > 0 ) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_STAN) + " " + Util.leftPad( "" + stanValue, 6, '0'), font, textAlignment));
        }
    }

    public void addAID(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        String aid = trans.getCard().getAid();
        if (!Util.isNullOrEmpty(aid)) {

            /* PC as requested by NMI for VpTT T5.26.04 */
            if (aid.length() > 14 && trans.getCard().isVisaCard() && trans.getCard().isCtlsCaptured())
                aid = aid.substring(0, 14);

            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_AID)+" " + aid, font));
        }
    }

    /**
     * Adds ATC tag data into the receipt if present
     * Create a receipt line in the format 'ATC data'
     * @param receipt {@link PrintReceipt} receipt object
     * @param trans {@link TransRec} the transactions containing ATC
     * @param font {@link PrintReceipt.FONT} font type to be used for this
     * */
    public void addATC( PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font ){
        if( receipt == null || trans == null || trans.getCard() == null )
            return;

        String atc = trans.getCard().getAtc();

        if( !Util.isNullOrEmpty( atc ) ){
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_ATC), atc, font));
        }
    }

    /**
     * Adds TSI tag data into the receipt if present
     *
     * @param receipt {@link PrintReceipt} receipt object
     * @param trans {@link TransRec} the transactions containing ATC
     * @param font {@link PrintReceipt.FONT} font type to be used for this
     * */
    public void addTsiAndCdoMcrFlags(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font ){
        if( receipt == null || trans == null || trans.getCard() == null )
            return;
        TCard card = trans.getCard();

        String tsiLabelAndVal = "";
        String tsiVal = trans.getCard().getTsi();
        if( !Util.isNullOrEmpty( tsiVal ) ){
            tsiLabelAndVal = getText(STR_TSI) + " " + tsiVal;
        }

        StringBuilder cardFlags = new StringBuilder();
        if( card.isCtlsMcrPerformed() ) {
            cardFlags.append(getText(STR_MCR_INDICATOR));
        }

        if( card.isEmvCdoPerformed() ) {
            if (cardFlags.length() > 0) {
                // add space separator
                cardFlags.append(" ");
            }
            cardFlags.append(getText(STR_CDO_INDICATOR));
        }

        // only print line if there's data
        if(tsiLabelAndVal.length() > 0 || cardFlags.toString().length() > 0) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(tsiLabelAndVal, cardFlags.toString(), font));
        }
    }

    public void addTags(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        String buff = "";
        String tvr = trans.getCard().getTvr();
        String tsi = trans.getCard().getTsi() ;
        String cvmResults = "";
        EmvTags tags = trans.getCard().getTags();
        if (tags != null && tags.isTagSet(cvm_results))
            cvmResults = tags.getString(cvm_results);

        if (!Util.isNullOrWhitespace(tvr))
            buff += tvr;

        buff += " ";

        if (!Util.isNullOrWhitespace(tsi))
            buff += tsi;

        buff += " ";

        if (!Util.isNullOrWhitespace(cvmResults))
            buff += cvmResults;


        if (!Util.isNullOrWhitespace(buff) && buff.length() > 2) {
            receipt.getLines().add(new PrintReceipt.PrintLine(buff, font));
        }
    }

    private String getMaskedText(String text, int unmaskedSuffixCount) {
        if (text != null && text.length() >= unmaskedSuffixCount) {
            StringBuilder strMaskedTid = new StringBuilder();
            for (int i = 0; i < text.length() - unmaskedSuffixCount; ++i) {
                strMaskedTid.append( "*" );
            }
            strMaskedTid.append( text.substring( text.length() - unmaskedSuffixCount ) );
            text = strMaskedTid.toString();
        }

        return text;
    }

    private String getTIDText() {
        return getTIDText(null);
    }

    private String getTIDText(TransRec trans) {
        String tid = null;
        if(trans != null) {
            tid = trans.getBestTerminalId(d.getPayCfg().getStid());
        }
        if(Util.isNullOrEmpty(tid)) {
            tid = UserManager.getActiveUser().getTerminalId();
        }
        if(Util.isNullOrEmpty(tid)) {
            tid = d.getPayCfg().getStid();
        }
        return tid;
    }

    public void addMaskedTID(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        String tid = getMaskedText(getTIDText(trans), 4);

        if(!Util.isNullOrEmpty(tid)) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_TID) + " " + tid, font));
        }
    }

    public void addTID(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        String tid = getTIDText(trans);

        if(isCardHolderCopy()) {
            tid = getMaskedText(tid, 4);
        }

        if(!Util.isNullOrEmpty(tid)) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_TID) + tid, font));
        }
    }

    public void addTID(PrintReceipt receipt, TransRec trans) {
        addTID(receipt, trans, LARGE_FONT);
    }

    public void addTID(PrintReceipt receipt, PrintReceipt.FONT font) {
        addTID(receipt, null, font);
    }

    public void addMIDAndTID(PrintReceipt receipt, TransRec trans) {
        String mid = getMIDText(trans);
        String tid = getTIDText(trans);

        if(!Util.isNullOrEmpty(mid)) {
            mid = getText(STR_MID) + mid;
        }
        if(!Util.isNullOrEmpty(tid)) {
            tid = getText(STR_TID) + tid;
        }


        receipt.getLines().add(new PrintReceipt.PrintLine(mid, MEDIUM_FONT));
        receipt.getLines().add(new PrintReceipt.PrintLine(tid, MEDIUM_FONT));

    }

    public void addMIDAndTID(PrintReceipt receipt) {
        addMIDAndTID(receipt, null);
    }

    public void addCardEaseReference(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        String eftPaymentId = trans.getProtocol().getEftPaymentId();

        if (!Util.isNullOrEmpty(eftPaymentId))
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_CER)+ eftPaymentId, font));
    }

    public void addVirtualDetails(PrintReceipt receipt, TransRec tran) {
        if (!Util.isNullOrEmpty(tran.getAudit().getVirtualMid())) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_VIRTUAL_MID) + tran.getAudit().getVirtualMid(), SMALL_FONT));
        }
        if (!Util.isNullOrEmpty(tran.getAudit().getVirtualTid())) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_VIRTUAL_TID) + tran.getAudit().getVirtualTid(), SMALL_FONT));
        }
        if (!Util.isNullOrEmpty(tran.getAudit().getVirtualName())) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_VIRTUAL_NAME) + tran.getAudit().getVirtualName(), SMALL_FONT));
        }
    }

    public void addReceiptNumber(PrintReceipt receipt, TransRec tran) {
        addReceiptNumber(receipt, tran, LARGE_FONT);
    }

    // Attach Receipt NB
    public void addReceiptNumber(PrintReceipt receipt, TransRec tran, PrintReceipt.FONT font) {
        if (receipt == null || tran == null) {
            return;
        }

        if (tran.getAudit().getReceiptNumber() > 0) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_INVOICE_NO) + " " + Util.padLeft(tran.getAudit().getReceiptNumber().toString(), 6, '0' ), font));
        }
    }

    public void addSettlementDate(PrintReceipt receipt, TransRec tran, PrintReceipt.FONT font) {
        if( receipt == null || tran == null ) {
            return;
        }

        if( tran.getProtocol().getBatchNumber() > 0 ) {
            // convert int to string in MMDD format
            String settleDate = Util.padLeft(tran.getProtocol().getBatchNumber().toString(), 4, '0' );

            // draw it as DD/MM
            receipt.getLines().add(new PrintReceipt.PrintLine(String.format(getText(STR_SETTLEMENT_DATE_FORMAT), settleDate.substring(2, 4), settleDate.substring(0, 2)), font));
        }
    }

    public void addRRNLeft(PrintReceipt receipt, TransRec tran, PrintReceipt.FONT font) {
        if (receipt == null || tran == null) {
            return;
        }

        if (!Util.isNullOrWhitespace(tran.getProtocol().getRRN())) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_RRN) + tran.getProtocol().getRRN(), font));
        }

        this.addSpaceLine(receipt);
    }

    public void addRRN(PrintReceipt receipt, TransRec tran, PrintReceipt.FONT font) {
        if (receipt == null || tran == null) {
            return;
        }

        if (!Util.isNullOrWhitespace(tran.getProtocol().getRRN())) {
            addLineCentered(receipt, getText(STR_RRN) + tran.getProtocol().getRRN(), font);
        }

        this.addSpaceLine(receipt);
    }

    public void addAccountType(PrintReceipt receipt, TransRec tran) {
        if (receipt == null || tran == null) {
            return;
        }

        receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_ACCOUNT) + tran.getProtocol().getAccountTypeName()));
    }

    /**
     * Writes Count & Amount to a receipt line with default {@link Receipt#MEDIUM_FONT}
     * @param receipt to be written on
     * @param leftTextId {@link String_id} to be used
     * @param count value
     * @param amount total
     * */
    public void addCountAndAmount( PrintReceipt receipt, String_id leftTextId, long count, long amount ){
        this.addCountAndAmount( receipt, getText( leftTextId ), count, amount );
    }

    /**
     * Writes Count & Amount to a receipt line with default {@link Receipt#MEDIUM_FONT}
     * @param receipt to be written on
     * @param leftText String to be used
     * @param count value
     * @param amount total
     * */
    public void addCountAndAmount( PrintReceipt receipt, String leftText, long count, long amount ){
        receipt.getLines().add( new PrintReceipt.PrintTableLine(
                leftText.length() > 0 ? leftText.toUpperCase() + ":" : "",
                "x" + count,
                d.getFramework().getCurrency().formatUIAmount(
                        "" + amount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL,
                        d.getPayCfg().getCountryCode()
                ) ) );
    }

    /**
     * Writes Count & Amount to a receipt line with default {@link Receipt#MEDIUM_FONT}
     * @param receipt to be written on
     * @param leftText String to be used
     * @param count value
     * @param amount total
     * @param negative sign to be used
     * */
    public void addCountAndAmount( PrintReceipt receipt, String leftText, long count, long amount, boolean negative ){
        receipt.getLines().add( new PrintReceipt.PrintTableLine(
                leftText.length() > 0 ? leftText.toUpperCase() + ":" : "",
                "x" + count, (negative ? "-" : "") +
                d.getFramework().getCurrency().formatUIAmount(
                        "" + amount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL,
                        d.getPayCfg().getCountryCode()
                ) ) );
    }

    /**
     * Writes Count & Amount to a receipt line with raw text input using default font{@link Receipt#MEDIUM_FONT}
     * @param receipt to be written on
     * @param leftText String to be used
     * @param count value
     * @param amount total
     * @param negative sign to be used
     * */
    public void addCountAndAmountWithRawLabel(PrintReceipt receipt, String leftText, long count, long amount, boolean negative ){
        receipt.getLines().add( new PrintReceipt.PrintTableLine(
                leftText.length() > 0 ? leftText : "",
                "x" + count, (negative ? "-" : "") +
                d.getFramework().getCurrency().formatUIAmount(
                        "" + amount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL,
                        d.getPayCfg().getCountryCode()
                ) ) );
    }

    public void addCountAndAmount( PrintReceipt receipt, String_id leftTextId, long count, long amount, boolean negative ){
        this.addCountAndAmount( receipt, getText( leftTextId ), count, amount , negative);
    }

    public void addCountAndAmount(IDependency d, PrintReceipt receipt, String text, long count, String amount, PrintReceipt.FONT font) {
        receipt.getLines().add(new PrintReceipt.PrintTableLine(text + count, d.getFramework().getCurrency().formatUIAmount(amount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode()), font));
    }

    /**
     * Writes left aligned text, currency code and Amount right aligned to a receipt line with raw text input using the font type specified
     * @param receipt to be written on
     * @param leftText String to be used which will be left aligned
     * @param amount amount to be used and it will be right aligned
     * @param font font type to be used
     * */
    public void addAmountField(PrintReceipt receipt, String leftText, long amount, PrintReceipt.FONT font) {
        receipt.getLines().add(new PrintReceipt.PrintTableLine(leftText, d.getPayCfg().getCountryCode().getAlphaCode(), d.getFramework().getCurrency().formatUIAmount("" + (amount), IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL,
                d.getPayCfg().getCountryCode()), font));

    }

    public void addAmountFields(IDependency d, PrintReceipt receipt, TransRec tran) {
        addAmountFields(d, receipt, tran, LARGE_FONT, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, true, false);
    }

    // Adds the amount Fields to a transaction
    public void addAmountFields(IDependency d, PrintReceipt receipt, TransRec tran, PrintReceipt.FONT font, IUICurrency.EAmountFormat format, boolean shortText, boolean alwaysShowTotal) {
        String[] fieldsText;
        long oldAmount = tran.getAmounts().getAmount();
        if (shortText)
            fieldsText = shortFieldNames;
        else
            fieldsText = longFieldNames;

        if (tran.getAmounts().getDiscountedAmount() > 0) {
            oldAmount += tran.getAmounts().getDiscountedAmount();
        }

       if (tran.isTopupPreAuth()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(fieldsText[FieldNames.AMOUNT.ordinal()], d.getFramework().getCurrency().formatUIAmount( tran.getAmounts().getTopupAmount() + "", format, d.getPayCfg().getCountryCode()), font));
        } else {
           if( !tran.isCash() ) {
               receipt.getLines().add( new PrintReceipt.PrintTableLine( fieldsText[FieldNames.AMOUNT.ordinal()], d.getFramework().getCurrency().formatUIAmount( oldAmount + "", format, d.getPayCfg().getCountryCode() ), font ) );
           }
        }
        //TODO Check This condition is correct
        if (tran.getAmounts().getCashbackAmount() > 0) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(fieldsText[FieldNames.CASH_BACK.ordinal()], d.getFramework().getCurrency().formatUIAmount(tran.getAmounts().getCashbackAmount() + "", format, d.getPayCfg().getCountryCode()), font));
        }

        if (tran.getAmounts().getTip() > 0) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(fieldsText[FieldNames.TIP.ordinal()], d.getFramework().getCurrency().formatUIAmount(tran.getAmounts().getTip() + "", format, d.getPayCfg().getCountryCode()), font));
        }

        if (tran.getAmounts().getSurcharge() > 0) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(fieldsText[FieldNames.SURCHARGE.ordinal()], d.getFramework().getCurrency().formatUIAmount(tran.getAmounts().getSurcharge() + "", format, d.getPayCfg().getCountryCode()), font));
        }
        if (tran.getAmounts().getDiscountedAmount() > 0) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(fieldsText[FieldNames.DISCOUNT.ordinal()], "-" + d.getFramework().getCurrency().formatUIAmount(tran.getAmounts().getDiscountedAmount() + "", format, d.getPayCfg().getCountryCode()), font));
            receipt.getLines().add(new PrintReceipt.PrintTableLine(fieldsText[FieldNames.TOTAL.ordinal()], " " + d.getFramework().getCurrency().formatUIAmount(tran.getAmounts().getAmount() + "", format, d.getPayCfg().getCountryCode()), font));
        }

        if (tran.getAmounts().getTip() > 0 || tran.getAmounts().getCashbackAmount() > 0 || alwaysShowTotal) {
            receipt.getLines().add(new PrintReceipt.PrintFillLine('_', SMALL_FONT, true));
            receipt.getLines().add(new PrintReceipt.PrintTableLine(fieldsText[FieldNames.TOTAL.ordinal()], d.getFramework().getCurrency().formatUIAmount(tran.getAmounts().getTotalAmount() + "", format, d.getPayCfg().getCountryCode()), LARGE_FONT, true));
        }

    }

    public void addTransFooter(PrintReceipt receipt, TransRec tran) {
        if (receipt == null || tran == null) {
            return;
        }

        if (tran.getAudit().getUti() != null) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_UTI) + tran.getAudit().getUti(), SMALL_FONT));
        }

        if (tran.getCard().isIccCaptured()) {
            addIccFooter(receipt, tran);
        }

        receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_RRN) + tran.getProtocol().getRRN(), SMALL_FONT));
        receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_ACCOUNT) + tran.getProtocol().getAccountTypeName(), SMALL_FONT));
    }

    private String getAccountTypeText( int accountType ) {
        switch( accountType ) {
            case ACC_TYPE_SAVINGS:
                return getText(STR_SAV);
            case ACC_TYPE_CHEQUE:
                return getText(STR_CHQ);
            case ACC_TYPE_CREDIT:
                return getText(STR_CRD);
            default:
                return "";
        }
    }

    public void addCardType(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        String accountTypeStr = getAccountTypeText(trans.getProtocol().getAccountType());

        if (trans.getCard().isCtlsCaptured()) {

            receipt.getLines().add(new PrintReceipt.PrintTableLine( accountTypeStr, getText(STR_CONTACTLESS), font));
        } else if (trans.getCard().isIccCaptured()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(accountTypeStr, getText(STR_ICC), font));
        } else if (trans.getCard().isIccFallbackCaptured()) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(accountTypeStr, getText(STR_ICC), font));
        } else if (trans.getCard().isManual()) {

            if (trans.getCard().isOverTelephone())
                receipt.getLines().add(new PrintReceipt.PrintTableLine(accountTypeStr, getText(STR_KEYED_TEL_ORDER), font));
            else if (trans.getCard().isMailOrder())
                receipt.getLines().add(new PrintReceipt.PrintTableLine(accountTypeStr, getText(STR_KEYED_MAIL_ORDER), font));
            else
                receipt.getLines().add(new PrintReceipt.PrintTableLine(accountTypeStr, getText(STR_KEYED), font));
        } else
        {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(accountTypeStr, getText(STR_SWIPED), font));
        }
    }

    public void addCardName(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        receipt.getLines().add(new PrintReceipt.PrintLine(trans.getCard().getCardName(d.getPayCfg()), font ));
    }

    public void addCardNameAndType(PrintReceipt receipt, TransRec trans) {
        if (trans.getCard().isCtlsCaptured()) {
            receipt.getLines().add(new PrintReceipt.PrintLine(trans.getCard().getCardName(d.getPayCfg()) + "("+getText(STR_CTLS) +")", LARGE_FONT));
        } else if (trans.getCard().isIccCaptured()) {
            receipt.getLines().add(new PrintReceipt.PrintLine(trans.getCard().getCardName(d.getPayCfg()) +  "("+getText(STR_ICC) +")", LARGE_FONT));
        } else if (trans.getCard().isManual()) {
            receipt.getLines().add(new PrintReceipt.PrintLine(trans.getCard().getCardName(d.getPayCfg()) +  "("+getText(STR_KEYED) +")", LARGE_FONT));
        } else
        {
            receipt.getLines().add(new PrintReceipt.PrintLine(trans.getCard().getCardName(d.getPayCfg()) +  "("+getText(STR_MSR) +")", LARGE_FONT));
        }
    }

    public void addMaskedPan(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        receipt.getLines().add(new PrintReceipt.PrintLine(trans.getMaskedPan(!isCardHolderCopy() ? TransRec.MaskType.MERCH_MASK : TransRec.MaskType.CUSTOMER_MASK, d.getPayCfg()), font));

    }

    // Attach the card data
    public void addCardDetails(PrintReceipt receipt, TransRec tran) {
        this.addSpaceLine(receipt);
        if (isMerchantCopy()) {
            receipt.getLines().add(new PrintReceipt.PrintLine(tran.getMaskedPan(TransRec.MaskType.MERCH_MASK, d.getPayCfg()), LARGE_FONT));
        } else if (isCardHolderCopy()) {
            receipt.getLines().add(new PrintReceipt.PrintLine(tran.getMaskedPan(TransRec.MaskType.CUSTOMER_MASK, d.getPayCfg()), LARGE_FONT));
        } else {
            receipt.getLines().add(new PrintReceipt.PrintLine(tran.getMaskedPan(d.getPayCfg()), LARGE_FONT));
        }

        if (tran.getCard().isIccCaptured() || tran.getCard().isCtlsCaptured()) {
            addIccHeader(receipt, tran);
        }

        if (tran.getCard().getExpiry() != null) {
            receipt.getLines().add(new PrintReceipt.PrintLine(String.format(getText(STR_EXPIRY_DATE_FORMAT), tran.getCard().getExpiry().substring(2, 4), tran.getCard().getExpiry().substring(0, 2)), SMALL_FONT));
        }
        this.addSpaceLine(receipt);

        addCardNameAndType(receipt, tran);
    }

    public void addHeader(PrintReceipt receipt) {
        /* Disclaimer */
        this.addDisclaimer( receipt );
        /*Logo*/
        this.addLogo( receipt );

        /*Header Details */
        addMerchantDetails(receipt);

        if (isDuplicate()) {
            addLineCentered(receipt, getText(STR_DUPLICATE_RECEIPT), LARGE_FONT, true);
            this.addSpaceLine(receipt);
        }
    }

    public void addIccHeader(PrintReceipt receipt, TransRec tran) {
        if (tran.getCard().getAid() != null) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_AID)+" " + tran.getCard().getAid()));
        }
        if (tran.getCard().getPsn() >= 0) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_APP_PSN) + tran.getCard().getPsn()));
        }
        if (tran.getCard().getValidFrom() != null) {
            receipt.getLines().add(new PrintReceipt.PrintLine(String.format(getText(STR_APP_START_DATE_FORMAT), tran.getCard().getValidFrom().substring(2, 4), tran.getCard().getValidFrom().substring(0, 2))));
        }
    }

    public void addIccFooter(PrintReceipt receipt, TransRec tran) {

        if (tran.getCard().getCryptogram() != null) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_CRYPTOGRAM) + Util.byteArrayToHexString(tran.getCard().getCryptogram()), SMALL_FONT));
        }

        if (tran.getCard().getCryptogramTypeCode() != null) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_CRYPTOGRAM_TYPE) + tran.getCard().getCryptogramTypeCode(), SMALL_FONT));
        }

        if (tran.getCard().getTvr() != null) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_TVR) + tran.getCard().getTvr(), SMALL_FONT));
        }

        if (tran.getCard().getTsi() != null) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_TSI) + tran.getCard().getTsi(), SMALL_FONT));
        }

        if (tran.getCard().getCvr() != null) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_CVR) + tran.getCard().getCvr(), SMALL_FONT));
        }
    }

    public void addIccDiags(PrintReceipt receipt, TransRec tran) {
        if (IccDiags.getCurValue()) {
            if (tran.getCard().isIccCaptured() || tran.getCard().isCtlsCaptured()) {
                EmvTags tags = tran.getCard().getTags();
                if (tags != null) {
                    addSpaceLine(receipt);
                    receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_EMV_TAGS), FONT_FIXED_WIDTH_POS, PrintReceipt.PrintLine.TextAlignment.CENTER));
                    addSpaceLine(receipt);

                    // AIP first
                    if (tran.getCard().getAip() != null ) {
                        receipt.getLines().add( new PrintReceipt.PrintLine(getText(STR_AIP), FONT_FIXED_WIDTH_POS) );
                        receipt.getLines().add(new PrintReceipt.PrintTableLine("", Util.byteArrayToHexString(tran.getCard().getAip()), FONT_FIXED_WIDTH_POS));
                    }

                    // then each of the emv tags in the collection
                    for (EmvTag tag : tags.values()) {
                        // first line is text description on left, hex tag on right
                        receipt.getLines().add( new PrintReceipt.PrintLine(tag.getName().toUpperCase().replace("_", " "), FONT_FIXED_WIDTH_POS) );
                        // second line is content
                        receipt.getLines().add(new PrintReceipt.PrintTableLine(Integer.toHexString(tag.getTag()).toUpperCase(), Util.byteArrayToHexString(tag.getData()), FONT_FIXED_WIDTH_POS));
                    }

                    // then CVR at the end
                    if (tran.getCard().getCvr() != null && !tran.getCard().getCvr().isEmpty()) {
                        receipt.getLines().add( new PrintReceipt.PrintLine(getText(STR_CVR), FONT_FIXED_WIDTH_POS) );
                        receipt.getLines().add(new PrintReceipt.PrintTableLine("", tran.getCard().getCvr(), FONT_FIXED_WIDTH_POS));
                    }

                }
            }
        }
    }

    protected Date getTranDate(TransRec trans) {

        // When dealing with displaying bank time for our receipt we go on the following order.
        // 1 Bank date and time adjusted for local time
        // 2 Recorded trans date and time from the Audit value
        // 3 Terminal's current recorded time
        Date date = null;

        if( trans != null ) {
            if (trans.getProtocol() != null && trans.getProtocol().getBankDate() != null && trans.getProtocol().getBankTime() != null) {
                // chop first 2 digits off yyMMdd date to get just MMdd
                date = Util.convertForTimezone( Calendar.getInstance().get( Calendar.YEAR ) + trans.getProtocol().getBankDate().substring(2) + trans.getProtocol().getBankTime(), d.getPayCfg().getBankTimeZone());
            }
            // IF converting the date for timezone fails, just assign from transdatetime value
            if( date == null ) {
                date = new Date(trans.getAudit().getTransDateTime());
            }

        } else {
            date = new Date();
        }

        return date;
    }

    public String formatDateTime(Date dateTime, String format) {
        return new SimpleDateFormat( format, Locale.getDefault() ).format(dateTime);
    }

    public void addDateTimeLine(PrintReceipt receipt) {
        addDateTimeLine(receipt, null, SMALL_FONT, true);
    }

    public void addDateTimeLine(PrintReceipt receipt, PrintReceipt.FONT font) {
        addDateTimeLine(receipt, null, font, true);
    }

    public void addDateTimeLine(PrintReceipt receipt, TransRec trans) {
        addDateTimeLine(receipt, trans, SMALL_FONT, true);
    }

    public void addDateTimeLine(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font, boolean addLabels) {
        addDateTimeLineCustomFormat(receipt, trans, font, addLabels, DEFAULT_DATE_FORMAT, "HH:mm:ss");
    }

    public void addDateTimeLineAs12HourFormat(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font, boolean addLabels) {
        addDateTimeLineCustomFormat(receipt, trans, font, addLabels, DEFAULT_DATE_FORMAT, "hh:mm aa");
    }

    public void addDateTimeLineCustomFormat(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font, boolean addLabels, String dateFormat, String timeFormat) {
        Date dateTime = getTranDate(trans);
        String date = (addLabels ? getText(STR_DATE) : "") + formatDateTime(dateTime, dateFormat);
        String time = (addLabels ? getText(STR_TIME) : "") + formatDateTime(dateTime, timeFormat);

        receipt.getLines().add(new PrintReceipt.PrintTableLine(date, time, font));
    }

    public void addDateTimeLineLarge(PrintReceipt receipt) {
        //TODO: This might not fit on one line?
        addDateTimeLine(receipt, null, LARGE_FONT, true);
    }

    private void addDateTimeLineJustified(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font, PrintReceipt.PrintLine.TextAlignment textAlignment) {
        PrintReceipt.PrintLine line = new PrintReceipt.PrintLine(formatDateTime(getTranDate(trans), "dd/MM/yyyy HH:mm:ss"), font, textAlignment);
        receipt.getLines().add(line);
    }

    public void addDateTimeLineLeft(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        addDateTimeLineJustified(receipt, trans, font, PrintReceipt.PrintLine.TextAlignment.LEFT);
    }

    public void addDateTimeLineCentre(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        addDateTimeLineJustified(receipt, trans, font, PrintReceipt.PrintLine.TextAlignment.CENTER);
    }

    public void addDateTimeLineRight(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        addDateTimeLineJustified(receipt, trans, font, PrintReceipt.PrintLine.TextAlignment.RIGHT);
    }

    public void addDateTimeLineMultiline(PrintReceipt receipt, TransRec trans, PrintReceipt.FONT font) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault() );
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault() );
        Date dateTime = getTranDate(trans);
        receipt.getLines().add(new PrintReceipt.PrintLine(timeFormat.format(dateTime), font, PrintReceipt.PrintLine.TextAlignment.CENTER));
        receipt.getLines().add(new PrintReceipt.PrintLine(dateFormat.format(dateTime), font, PrintReceipt.PrintLine.TextAlignment.CENTER));
    }

    public void addPromoLines(PrintReceipt receipt) {
        PayCfg paycfg = d.getPayCfg();

        if (paycfg.getReceipt().getPromo() != null) {
            if( !Util.isNullOrEmpty(paycfg.getReceipt().getPromo().getLine1()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getPromo().getLine1(), SMALL_FONT));
            }
            if( !Util.isNullOrEmpty(paycfg.getReceipt().getPromo().getLine2()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getPromo().getLine2(), SMALL_FONT));
            }
            if( !Util.isNullOrEmpty(paycfg.getReceipt().getPromo().getLine3()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getPromo().getLine3(), SMALL_FONT));
            }
            if( !Util.isNullOrEmpty(paycfg.getReceipt().getPromo().getLine4()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getPromo().getLine4(), SMALL_FONT));
            }
            if( !Util.isNullOrEmpty(paycfg.getReceipt().getPromo().getLine5()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getPromo().getLine5(), SMALL_FONT));
            }
            if( !Util.isNullOrEmpty(paycfg.getReceipt().getPromo().getLine6()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getPromo().getLine6(), SMALL_FONT));
            }
            if( !Util.isNullOrEmpty(paycfg.getReceipt().getPromo().getLine7()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getPromo().getLine7(), SMALL_FONT));
            }
            if( !Util.isNullOrEmpty(paycfg.getReceipt().getPromo().getLine8()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getPromo().getLine8(), SMALL_FONT));
            }
            if( !Util.isNullOrEmpty(paycfg.getReceipt().getPromo().getLine9()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getPromo().getLine9(), SMALL_FONT));
            }
            if( !Util.isNullOrEmpty(paycfg.getReceipt().getPromo().getLine10()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getPromo().getLine10(), SMALL_FONT));
            }

            this.addSpaceLine(receipt);

        }
    }

    public void addCardHolder(PrintReceipt receipt, TransRec tran) {
        if (tran.getCard().getCardsConfig(d.getPayCfg()).isPrintCardholderName()) {
            if (tran.getCard().getCardHolderName() != null && tran.getCard().getCardHolderName().length() > 0) {
                receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_NAME), SMALL_FONT));
                receipt.getLines().add(new PrintReceipt.PrintLine(tran.getCard().getCardHolderName(), SMALL_FONT));
                this.addSpaceLine(receipt);
            }
        }
    }

    public void addHelpLines(PrintReceipt receipt) {
        PayCfg paycfg = d.getPayCfg();

        if (paycfg.getReceipt().getHelp() != null) {
            if (paycfg.getReceipt().getHelp().getLine1() != null && paycfg.getReceipt().getHelp().getLine1().length() > 0) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getHelp().getLine1(), SMALL_FONT));
            }
            if (paycfg.getReceipt().getHelp().getLine2() != null && paycfg.getReceipt().getHelp().getLine2().length() > 0) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getHelp().getLine2(), SMALL_FONT));
            }
            if (paycfg.getReceipt().getHelp().getLine3() != null && paycfg.getReceipt().getHelp().getLine3().length() > 0) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getHelp().getLine3(), SMALL_FONT));
            }
            if (paycfg.getReceipt().getHelp().getLine4() != null && paycfg.getReceipt().getHelp().getLine4().length() > 0) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getHelp().getLine4(), SMALL_FONT));
            }
            if (paycfg.getReceipt().getHelp().getLine5() != null && paycfg.getReceipt().getHelp().getLine5().length() > 0) {
                receipt.getLines().add(new PrintReceipt.PrintLine(paycfg.getReceipt().getHelp().getLine5(), SMALL_FONT));
            }
        }
    }

    public void addSpaceLines(PrintReceipt receipt, int count) {
        for (int i = 0; i < count; i++) {
            addSpaceLine(receipt);
        }
    }

    public void addSpaceLine(PrintReceipt receipt) {
        if(!PrintReceipt.DEBUG_FORCE_SMALL_RECEIPT) {
            receipt.getLines().add(new PrintReceipt.PrintSpaceLine());
        }
    }

    // Adds a section line to the receipt
    public void addSectionLine(PrintReceipt receipt) {
        receipt.getLines().add(new PrintReceipt.PrintFillLine('-'));
    }

    public void addMerchantDetails(PrintReceipt receipt) {

        PayCfg paycfg = d.getPayCfg();
        if (paycfg.getReceipt() != null && paycfg.getReceipt().getMerchant() != null) {
            com.linkly.libengine.config.paycfg.Receipt.MerchantRec merchantRec = paycfg.getReceipt().getMerchant();

            // add lines, skipping empty ones
            if( !Util.isNullOrEmpty(merchantRec.getLine0()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine0(), LARGE_FONT, true, PrintReceipt.PrintLine.TextAlignment.LEFT));
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine1()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine1(), MEDIUM_FONT));
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine2()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine2(), MEDIUM_FONT));
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine3()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine3(), MEDIUM_FONT));
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine4()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine4(), MEDIUM_FONT));
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine5()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine5(), MEDIUM_FONT));
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine6()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine6(), MEDIUM_FONT));
            }
            this.addSpaceLine(receipt);
        }
    }

    public void addWorldpayCardAcceptorDetails(PayCfg paycfg, PrintReceipt receipt) {

        if (paycfg.getReceipt() != null && paycfg.getReceipt().getMerchant() != null) {
            com.linkly.libengine.config.paycfg.Receipt.MerchantRec merchantRec = paycfg.getReceipt().getMerchant();
            if( !Util.isNullOrEmpty(merchantRec.getLine0()) ) {
                addLineCenteredIfNotEmpty(receipt, merchantRec.getLine0(), MEDIUM_FONT);
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine1()) ) {
                addLineCenteredIfNotEmpty(receipt, merchantRec.getLine1(), SMALL_FONT);
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine2()) ) {
                addLineCenteredIfNotEmpty(receipt, merchantRec.getLine2(), SMALL_FONT);
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine3()) ) {
                addLineCenteredIfNotEmpty(receipt, merchantRec.getLine3(), SMALL_FONT);
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine4()) ) {
                addLineCenteredIfNotEmpty(receipt, merchantRec.getLine4(), SMALL_FONT);
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine5()) ) {
                addLineCenteredIfNotEmpty(receipt, merchantRec.getLine5(), SMALL_FONT);
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine6()) ) {
                addLineCenteredIfNotEmpty(receipt, merchantRec.getLine6(), SMALL_FONT);
            }
        }
    }

    public void addOptoMerchantDetails(PrintReceipt receipt, int spaceLines) {

        if (d.getPayCfg().getReceipt() != null && d.getPayCfg().getReceipt().getMerchant() != null) {
            com.linkly.libengine.config.paycfg.Receipt.MerchantRec merchantRec = d.getPayCfg().getReceipt().getMerchant();

            this.addLogo( receipt );

            if( !Util.isNullOrEmpty(merchantRec.getLine0()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine0(), LARGE_FONT, true, PrintReceipt.PrintLine.TextAlignment.LEFT));
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine1()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine1(), MEDIUM_FONT));
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine2()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine2(), MEDIUM_FONT));
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine3()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine3(), MEDIUM_FONT));
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine4()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine4(), MEDIUM_FONT));
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine5()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine5(), MEDIUM_FONT));
            }
            if( !Util.isNullOrEmpty(merchantRec.getLine6()) ) {
                receipt.getLines().add(new PrintReceipt.PrintLine(merchantRec.getLine6(), MEDIUM_FONT));
            }
            this.addSpaceLines(receipt, spaceLines);
        }

        if (isDuplicate()) {
            addLineCentered(receipt, getText(STR_DUPLICATE_RECEIPT), LARGE_FONT, true);
            this.addSpaceLine(receipt);
        }

    }



    public void addUserDetails(PrintReceipt receipt) {
        User user = UserManager.getActiveUser();
        if (user != null) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_USER_NAME) + user.getUserName(), SMALL_FONT));
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_USER_ID).toUpperCase()+"  : " + user.getUserId(), SMALL_FONT));
        }
    }

    public void addCustomerReference(PrintReceipt receipt, TransRec tran) {
        //Customer reference
        if (tran.getAudit().getReference() != null) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_REFERENCE)+": ", SMALL_FONT));
            receipt.getLines().add(new PrintReceipt.PrintLine(tran.getAudit().getReference(), SMALL_FONT));
            this.addSpaceLine(receipt);
        }
    }
    public void addCustomerReference2(PrintReceipt receipt, TransRec tran) {
        //Customer reference
        if (tran.getAudit().getReference() != null && !tran.getAudit().getReference().isEmpty()) {
            addLineCentered(receipt, getText(STR_REFERENCE).toUpperCase()+": " + tran.getAudit().getReference(), MEDIUM_FONT);
            this.addSpaceLine(receipt);
        }
    }

    public void addAccount(PrintReceipt receipt, TransRec tran) {
        //Todo  Get Account Name if present
        if (tran.getCard().getCardHolderName() != null && tran.getCard().getCardHolderName().length() > 0) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_NAME), SMALL_FONT));
            receipt.getLines().add(new PrintReceipt.PrintLine(tran.getCard().getCardHolderName(), SMALL_FONT));
            this.addSpaceLine(receipt);
        }
    }

    public void addBanner(PrintReceipt receipt) {
        if (isSAFReversalDelete) {
            addLineCentered(receipt, getText(STR_RECEIPT_SAF_RECORD), LARGE_FONT, true);
            addLineCentered(receipt, getText(STR_RECEIPT_NOT_INVOICE), LARGE_FONT, true);
        } else if (isMerchantCopy()) {
            addLineCentered(receipt, getText(STR_RECEIPT_MERCHANT_COPY), LARGE_FONT, true);
        } else if (isCardHolderCopy()) {
            addLineCentered(receipt, getText(String_id.STR_RECEIPT_CARD_HOLDER_COPY), LARGE_FONT, true);
        }

        if (isDuplicate()) {
            addLineCentered(receipt, getText(STR_DUPLICATE_RECEIPT), LARGE_FONT, true);
            this.addSpaceLine(receipt);
        }


    }

    public static String getText(String_id string_id){
        return Engine.getDep().getPrompt(string_id);
    }

    public void addFooter(PrintReceipt receipt) {
        PayCfg paycfg = d.getPayCfg();
        this.addSpaceLine(receipt); // Always add a space line here to give a gap between the footer and the previous area.
        if (isCardHolderCopy()) {
            if (paycfg.getReceipt().getFooter() != null) {

                // We only print the line if it has data in it not empty/null
                if (paycfg.getReceipt().getFooter().getLine1() != null && paycfg.getReceipt().getFooter().getLine1().length() > 0) {
                    Receipt.addLineCentered( receipt, paycfg.getReceipt().getFooter().getLine1(), FONT_FIXED_WIDTH_POS );
                }

                // We only print the line if it has data in it not empty/null
                if (paycfg.getReceipt().getFooter().getLine2() != null && paycfg.getReceipt().getFooter().getLine2().length() > 0) {
                    Receipt.addLineCentered( receipt, paycfg.getReceipt().getFooter().getLine2(), FONT_FIXED_WIDTH_POS );
                }
            }

        }
        addBanner(receipt);
    }

    public void addDigitalSignatureSection(PrintReceipt receipt, TransRec tran, PrintReceipt.FONT fontSize) {

        if (!EFTPlatform.printToScreen())
            return;

        if (!tran.isApprovedOrDeferred()) {
            return;
        }

        if (!isMerchantCopy())
            return;

        if (tran.isSignatureRequired() && tran.isApprovedOrDeferred()) {
            addSectionLine(receipt);
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_DIGITAL_SIG), fontSize));
        }
    }


    public void addVerificationDetails(PrintReceipt receipt, TransRec tran, PrintReceipt.FONT fontSize) {
        if (!tran.isApprovedOrDeferred()) {
            return;
        }

        boolean printSignStrip = false;
        if ( tran.isSignatureRequired() && tran.isApprovedOrDeferred()) {

            if (tran.getCard().isPinAndSigVerificationRequired()) {
                if (isMerchantCopy()) {
                    addLineCentered(receipt, getText(STR_PIN_VERIFIED), fontSize);
                    addLineCentered(receipt, getText(STR_PLS_SIGN_BELOW), fontSize);
                } else {
                    addLineCentered(receipt, getText(STR_VERIFIED_PIN_AND_SIGN), fontSize);
                }
            } else {
                if (isMerchantCopy()) {
                    receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_CARDHOLDER_SIGN), fontSize));
                } else {
                    addLineCentered(receipt, getText(STR_VERIFIED_BY_SIGN), fontSize);
                }
            }
            printSignStrip = true;
        } else if (tran.getCard().isDeviceVerified()) {
            addLineCentered(receipt, getText(STR_VERIFIED_BY_DEVICE), fontSize);
        } else if (!tran.getCard().isCardholderPresent()) {
            addLineCentered(receipt, getText(STR_CARDHOLDER_NOT_PRESENT), fontSize);
        } else if (tran.getCard().isPinVerificationRequired()) {
            // Consider instead using tran.isVerifiedByPinRequiredOnReceipt for this case.
            addLineCentered(receipt, getText(STR_VERIFIED_BY_PIN), fontSize);
        } else if (!tran.getCard().isNotVerified()) {
            if (fontSize.ordinal() >= LARGE_FONT.ordinal()) {
                addLineCentered(receipt, getText(STR_NO_CARDHOLDER), fontSize);
                addLineCentered(receipt, getText(STR_VERIFICATION), fontSize);
            } else {
                addLineCentered(receipt, getText(STR_NO_CARDHOLDER_VERIFICATION), fontSize);
            }

        }

        if (printSignStrip && isMerchantCopy() && !EFTPlatform.printToScreen()) {
            this.addSpaceLines(receipt, 5);
            receipt.getLines().add(new PrintReceipt.PrintFillLine('_'));
            this.addSpaceLine(receipt);
        }
    }

    public void addDebitOrCreditText(PrintReceipt receipt, boolean isDebit, TransRec trans) {
        if (trans.isApprovedOrDeferred()) {
            if (isDebit) {
                addLineCentered(receipt, getText(STR_PLS_DEBIT_MY_ACT), SMALL_FONT);
            } else {
                addLineCentered(receipt, getText(STR_PLS_CREDIT_MY_ACT), SMALL_FONT);
            }
            this.addSpaceLine(receipt);
        }
    }

    public void addTransName(PrintReceipt receipt, String transName1, String transName2) {
        if (receipt != null) {
            addLineCenteredIfNotEmpty(receipt, transName1, LARGE_FONT);
            addLineCenteredIfNotEmpty(receipt, transName2, LARGE_FONT);
        }
    }

    public void addAuthCode(PrintReceipt receipt, TransRec tran) {
        addAuthCode(receipt, tran, LARGE_FONT, false);
    }

    public void addAuthCode(PrintReceipt receipt, TransRec tran, PrintReceipt.FONT font, boolean centre) {

        if (receipt == null || tran == null) {
            return;
        }

        if (tran.isApproved()) {
            if (!Util.isNullOrWhitespace(tran.getProtocol().getAuthCode())) {
                PrintReceipt.PrintLine.TextAlignment alignment = centre ? PrintReceipt.PrintLine.TextAlignment.CENTER : PrintReceipt.PrintLine.TextAlignment.LEFT;
                String authText = getText(STR_AUTH_CODE) + tran.getProtocol().getAuthCode();
                receipt.getLines().add(new PrintReceipt.PrintLine(authText, font, alignment));
                this.addSpaceLine(receipt);
            }
        }
    }

    public void addOfflineBalance(IDependency d, PrintReceipt receipt, TransRec tran, PrintReceipt.FONT font) {
        PrintReceipt.PrintLine line;
        if (receipt == null || tran == null) {
            return;
        }

        if (!isCardHolderCopy())
            return;

        if (tran.getCard().getCtlsBalanceValueAOSA() != null && !tran.getCard().getCtlsBalanceValueAOSA().isEmpty()) {
            String formattedAmount = d.getFramework().getCurrency().formatUIAmount(tran.getCard().getCtlsBalanceValueAOSA() + "", IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
            line = new PrintReceipt.PrintLine(getText(STR_CARD_BALANCE) + formattedAmount, font);
            receipt.getLines().add(line);
            this.addSpaceLine(receipt);
        }
    }



    public void addVersion(PrintReceipt receipt) {

        if (receipt == null) {
            return;
        }

        //  " Dirty hack here.
        receipt.getLines().add( new PrintReceipt.PrintLine(
                getText( STR_VERSION ).toUpperCase() + " " + d.getPayCfg().getPaymentAppVersion(),
                SMALL_FONT ) );
    }

    public void addErrorText(PrintReceipt receipt, TransRec tran) {
        if (!Util.isNullOrEmpty(tran.getProtocol().getErrorCode()) &&
                !Util.isNullOrEmpty(tran.getProtocol().getAdditionalResponseText()) &&
                !Util.containsAuthCode(tran.getProtocol().getAdditionalResponseText()) &&
                !Util.containsApproved(tran.getProtocol().getAdditionalResponseText())) {
            String out = String.format("(%s, %s)", tran.getProtocol().getErrorCode(), tran.getProtocol().getAdditionalResponseText());
            addLineCentered(receipt, out, MEDIUM_FONT);
        }
    }

    public void addResponseCode(PrintReceipt receipt, TransRec tran) {
        if (!Util.isNullOrEmpty(tran.getProtocol().getServerResponseCode())) {
            addLineCentered(receipt, getText(STR_RESPONSE_CODE) + tran.getProtocol().getServerResponseCode(), MEDIUM_FONT);
        }
    }
    public void addAuthenticationResult(PrintReceipt receipt, TransRec tran) {
        this.addSpaceLine(receipt);
        if (tran.isApproved()) {
            addLineCentered(receipt, getText(STR_AUTHORISED).toUpperCase(), LARGE_FONT, true);
            /* the following added for Amex test AXP RCP 001 */
            this.addSpaceLine(receipt);
            addResponseCode(receipt, tran);
        } else if (tran.isDeferredAuth()) {
            addLineCentered(receipt, getText(STR_AUTHORISED).toUpperCase(), LARGE_FONT, true);
            addLineCentered(receipt, getText(STR_DEFERRED), LARGE_FONT, true);
            /* the following added for Amex test AXP RCP 001 */
            this.addSpaceLine(receipt);
            addResponseCode(receipt, tran);
        } else if (tran.getProtocol().getHostResult() == TProtocol.HostResult.DECLINED) {
            // use card acceptor printer data if present, else use default block
            if( !Util.isNullOrEmpty(tran.getProtocol().getCardAcceptorPrinterData())) {
                // print decline reason from as2805 table
                addLineCentered(receipt, tran.getProtocol().getCardAcceptorPrinterData(), LARGE_FONT, true, true);
                addErrorText(receipt, tran);
            } else {
                addLineCentered(receipt, getText(STR_NOT_AUTHORISED), LARGE_FONT, true, true);
                addResponseCode(receipt, tran);
            }
            this.addSpaceLine(receipt);
        } else if (tran.getProtocol().getHostResult() == TProtocol.HostResult.CONNECT_FAILED || tran.getProtocol().getHostResult() == TProtocol.HostResult.NO_RESPONSE) {
            addLineCentered(receipt, getText(STR_UNABLE_TO_AUTHORISE), LARGE_FONT, true, true);
            this.addSpaceLine(receipt);
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_REASON) + tran.getProtocol().getHostResult().displayName));
        } else if (tran.isCancelled()) {
            addLineCentered(receipt, tran.getProtocol().getPosResponseText().toUpperCase(), LARGE_FONT, true, true);
            addResponseCode( receipt, tran );
        } else {
            addLineCentered(receipt, getText(STR_NOT_AUTHORISED), LARGE_FONT, true, true);
        }

        if (tran.isReferred() && tran.getProtocol().getReferralNumber() != null && !tran.getProtocol().getReferralNumber().isEmpty()) {
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_CALL_ISSUER) + tran.getProtocol().getReferralNumber(), SMALL_FONT));
        }

    }

    private VasCounter getRightCounters(TransRec t) {
        if (vasCounters.containsKey(t.getVasName())) {
            return vasCounters.get(t.getVasName());
        }

        VasCounter newVas = new VasCounter( t.getVasName() );
        vasCounters.put(t.getVasName(), newVas);
        return newVas;
    }

    private void addToCounters(TransRec trans) {
        VasCounter v = getRightCounters(trans);
        long amount = trans.getAmounts().getAmount();
        Integer authCount = trans.getProtocol().getAuthCount();
        Integer revCount = trans.getProtocol().getReversalCount();

        v.vasAmount += (authCount * amount);
        v.vasCount += authCount;
        v.vasReversalAmount += (amount * revCount);
        v.vasReversalCount += revCount;
    }

    public void printVasTotals(IDependency d, Reconciliation rec, PrintReceipt receipt) {

        boolean containsVas = false;
        vasCounters = new HashMap<>();

        if (rec.getRecTransList() == null || rec.getRecTransList().size() == 0) {
            return;
        }

        for (TransRec t : rec.getRecTransList()) {
            if (t.isVas()) {
                containsVas = true;
                addToCounters(t);
            }
        }

        if (containsVas) {
            for (VasCounter o : vasCounters.values()) {
                if (o == null) {
                    continue;
                }
                receipt.getLines().add(new PrintReceipt.PrintTableLine(o.vasName, "(" + String.format(Locale.getDefault(), "%04d", o.vasCount) + ")", d.getFramework().getCurrency().formatUIAmount(o.vasAmount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));

                if (d.getPayCfg().isReversalTransAllowed()) {
                    receipt.getLines().add(new PrintReceipt.PrintTableLine(o.vasName + getText(STR_REV), "(" + String.format(Locale.getDefault(), "%04d", o.vasReversalCount) + ")", d.getFramework().getCurrency().formatUIAmount(o.vasReversalAmount + "", IUICurrency.EAmountFormat.FMT_AMT_MIN, d.getPayCfg().getCountryCode())));
                }

            }
        }
    }

    public void addDeclarationBlock(PrintReceipt receipt, FONT font){
        Receipt.addLineCentered(receipt, getText(STR_DEBIT_ACT_TEXT), font);
        Receipt.addLineCentered(receipt, getText(STR_TOTAL_SHOWN), font);
    }

    public void addDiagnosticCode(PrintReceipt receipt, TransRec trans){
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_DIAGNOSTIC_CODE), trans.getProtocol().getDiagnosticCode(), MEDIUM_FONT));
    }


    public void addLogo( PrintReceipt receipt ){
        receipt.getLines().add( new PrintReceipt.PrintImageLine(Engine.getDep().getPayCfg().getBrandReceiptLogoHeaderOrDefault()) );
        // Add padding at the bottom of the logo
        this.addSpaceLine(receipt);
    }

    protected void addDisclaimer( PrintReceipt receipt ) {
        if (isSAFReversalDelete) {
            addLineCentered(receipt, getText(STR_RECEIPT_SAF_RECORD), LARGE_FONT, true);
            addLineCentered(receipt, getText(STR_RECEIPT_NOT_INVOICE), LARGE_FONT, true);
        }
    }

    public boolean isDuplicate() {
        return this.isDuplicate;
    }

    public boolean isMerchantCopy() {
        return this.isMerchantCopy;
    }

    public boolean isCardHolderCopy() {
        return this.isCardHolderCopy;
    }

    public boolean isSAFReversalDelete() {
        return this.isSAFReversalDelete;
    }

    private static class VasCounter {
        public String vasName;
        public long vasCount = 0;
        public long vasAmount = 0;
        public long vasReversalCount = 0;
        public long vasReversalAmount = 0;
        public VasCounter(String vasName) {
            this.vasName = vasName;
        }
    }

    public enum FieldNames  { AMOUNT, CASH_BACK, TIP, DISCOUNT, SURCHARGE, TOTAL, COUNT}
}
