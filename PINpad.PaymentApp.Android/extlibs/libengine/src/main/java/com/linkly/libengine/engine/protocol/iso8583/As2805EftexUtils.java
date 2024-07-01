package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.SUBST_VAL_CVV;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._055_ICC_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData.Field.CardVerificationData;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData.Field.PosData;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData.Field.StructuredData;
import static com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.Formatters.getBinary;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.CTLS;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC_FALLBACK_SWIPED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_OFFLINE_PIN;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_ONLINE_PIN;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_PIN_AND_SIG;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.PLAINTEXT_OFFLINE_PIN;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.PLAINTEXT_PIN_AND_SIG;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.SIG;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.emv.Tag.aid;
import static com.linkly.libsecapp.emv.Tag.auth_resp_code;
import static com.linkly.libsecapp.emv.Tag.icc_response;
import static com.linkly.libsecapp.emv.Tag.iss_auth_data;
import static com.linkly.libsecapp.emv.Tag.issuer_app_data;
import static com.linkly.libsecapp.emv.Tag.isuer_scrpt_templ_71;
import static com.linkly.libsecapp.emv.Tag.isuer_scrpt_templ_72;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.linkly.libengine.config.paycfg.PaymentSwitch;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.AdditionalAmount;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Eftex;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.HashtableMessage;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.PosDataCode;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.IsoUtils;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.ProcessingCode;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.UnknownFieldException;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.env.BatchNumber;
import com.linkly.libengine.env.ItemNumber;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.EmvTag;
import com.linkly.libsecapp.emv.EmvTags;
import com.linkly.libsecapp.emv.Tag;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class As2805EftexUtils {

    private static final String TAG = "As2805EftexUtils";
    private static final String FORMAT_yyMMddHHmmss = "yyMMddHHmmss";

    public static ProcessingCode packProcCode(TransRec trans) throws Exception {
        ProcessingCode procCode = new ProcessingCode();

        if (trans.isSale() || trans.isCompletion() || trans.isPreAuth() || trans.isReversal() ) {
            procCode.setTranType("00");
        } else if (trans.isCashback()) {
            procCode.setTranType("09");
        } else if (trans.isCash()) {
            procCode.setTranType("01");
        } else if (trans.isRefund()) {
            procCode.setTranType("20");
        } else {
            Timber.e( "ERROR - trans type unknown, assuming 00 processing code de 3");
            procCode.setTranType("00");
        }

        String accountType = String.format("%02d", trans.getProtocol().getAccountType());
        if (trans.isRefund()) {
            procCode.setFromAccountType("00");
            procCode.setToAccountType(accountType);
        } else {
            procCode.setFromAccountType(accountType);
            procCode.setToAccountType("00");
        }
        return procCode;

    }

    public static String getBankTimeHHMMSS(String yymmddhhmmssInput){
        // validate input
        if( yymmddhhmmssInput == null || yymmddhhmmssInput.length() != 12 ){
            Timber.e( "Error, invalid input bank date/time %s", yymmddhhmmssInput == null ? "null" : yymmddhhmmssInput );
            return null;
        }

        return yymmddhhmmssInput.substring(6);
    }

    public static String getBankDateYYMMDD(String yymmddhhmmssInput){
        // validate input
        if( yymmddhhmmssInput == null || yymmddhhmmssInput.length() != 12 ){
            Timber.e( "Error, invalid input bank date/time %s", yymmddhhmmssInput == null ? "null" : yymmddhhmmssInput );
            return null;
        }
        return yymmddhhmmssInput.substring(0,6);
    }

    public static void setBankDateAndTime(Date date, TransRec trans){
        DateFormat dateFormat = new SimpleDateFormat("yyMMdd", Locale.getDefault());
        String strDate = dateFormat.format(date);
        trans.getProtocol().setBankDate(strDate);

        DateFormat timeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
        String strTime = timeFormat.format(date);
        trans.getProtocol().setBankTime(strTime);
    }

    public static String packTrack2(String track2) {
        /* to be updated when we do p2pe */
        if (track2 != null) {
            return track2;
        }
        return null;
    }

    public static String packReconIndicator(int batchNumber) {
        return IsoUtils.padLeft(String.valueOf(batchNumber), 3, '0');
    }

    public static String packStan(long stan) {
        return IsoUtils.padLeft(String.valueOf(stan), 6, '0');
    }

    public static String packLocalDateTimeDe12(TransRec trans) {
        return trans.getAudit().getTransDateTimeAsString(FORMAT_yyMMddHHmmss);
    }

    public static String packTransTime(TransRec trans, String bankTimeZone) {
        return trans.getAudit().getTransDateTimeAsString("HHmmss", bankTimeZone);
    }

    public static String packTransDate(TransRec trans, String bankTimeZone) {
        return trans.getAudit().getTransDateTimeAsString("MMdd", bankTimeZone);
    }

    public static String packReversalDateTime(TransRec trans) {
        if (Engine.getDep().getPayCfg().isReversalCopyOriginal()) {
            return trans.getProtocol().getOrginalTransmissionDateTimeAsString(FORMAT_yyMMddHHmmss);
        } else {
            return trans.getAudit().getReversalDateTimeAsString(FORMAT_yyMMddHHmmss);
        }
    }

    public static String packPosDataCode(TransRec trans) {

        TCard cardInfo = trans.getCard();
        TCard.CvmType cvmType = cardInfo.getCvmType();

        PosDataCode pdc = new PosDataCode();

        pdc.setCardDataInputCapability(PosDataCode.CardDataInputCapability._A_CONTACTLESS_ICC);
        pdc.setCardholderAuthCapability(PosDataCode.CardholderAuthCapability._1_PIN);
        pdc.setCardCaptureCapability(PosDataCode.CardCaptureCapability._0_NONE);
        pdc.setOperatingEnvironment(PosDataCode.OperatingEnvironment._1_ATTENDED_ON_ACCEPTOR_PREMISES);

        if (trans.getCard().isCardholderPresent()) {
            pdc.setCardholderPresent(PosDataCode.CardholderPresent._0_PRESENT);
            pdc.setCardPresent(PosDataCode.CardPresent._1_PRESENT);
        }
        else {
            pdc.setCardholderPresent(PosDataCode.CardholderPresent._1_NOT_PRESENT);
            pdc.setCardPresent(PosDataCode.CardPresent._0_NOT_PRESENT);
        }

        TCard.CaptureMethod eCaptMethod = cardInfo.getCaptureMethod();
        switch (eCaptMethod) {
            case CTLS:
                pdc.setCardDataInputMode(PosDataCode.CardDataInputMode._7_CONTACTLESS_ICC);
                break;
            case CTLS_MSR:
                pdc.setCardDataInputMode(PosDataCode.CardDataInputMode._8_CONTACTLESS_MAGSTRIPE);
                break;
            case ICC:
                pdc.setCardDataInputMode(PosDataCode.CardDataInputMode._5_ICC);
                break;
            case ICC_FALLBACK_KEYED:
            case MANUAL:
                pdc.setCardDataInputMode(PosDataCode.CardDataInputMode._6_KEY_ENTRY);
                break;
            case ICC_FALLBACK_SWIPED:
            case SWIPED:
            default:
                pdc.setCardDataInputMode(PosDataCode.CardDataInputMode._2_MAGSTRIPE);
                break;
        }

        if (cvmType == ENCIPHERED_ONLINE_PIN) {
            Timber.i( "Online PIN CVM");
            pdc.setCardholderAuthMethod(PosDataCode.CardholderAuthMethod._1_PIN);
            pdc.setCardholderAuthEntity(PosDataCode.CardholderAuthEntity._3_AUTHORIZER);
        } else if( cvmType == PLAINTEXT_PIN_AND_SIG || cvmType == ENCIPHERED_PIN_AND_SIG || cvmType == PLAINTEXT_OFFLINE_PIN || cvmType == ENCIPHERED_OFFLINE_PIN ) {
            Timber.i( "Offline PIN CVM");
            pdc.setCardholderAuthMethod(PosDataCode.CardholderAuthMethod._1_PIN);
            pdc.setCardholderAuthEntity(PosDataCode.CardholderAuthEntity._1_ICC);
        } else if (cvmType == SIG) {
            Timber.i( "SIG CVM");
            pdc.setCardholderAuthMethod(PosDataCode.CardholderAuthMethod._5_MANUAL);
            pdc.setCardholderAuthEntity(PosDataCode.CardholderAuthEntity._4_MERCHANT);
        } else {
            Timber.i( "NO CVM");
            pdc.setCardholderAuthMethod(PosDataCode.CardholderAuthMethod._0_NONE);
            pdc.setCardholderAuthEntity(PosDataCode.CardholderAuthEntity._0_NOT_AUTHENTICATED);
        }

        pdc.setCardDataOutputCapability(PosDataCode.CardDataOutputCapability._1_NONE);
        pdc.setTerminalOutputCapability(PosDataCode.TerminalOutputCapability._4_PRINTING_AND_DISPLAY);
        pdc.setPinCaptureCapability(PosDataCode.PinCaptureCapability._6_SIX);
        pdc.setTerminalOperator(PosDataCode.TerminalOperator._0_CUSTOMER_OPERATED);
        pdc.setTerminalType(PosDataCode.TerminalType._01_POS);

        return pdc.toString();
    }

    public static String packMerchantId(String merchantId) {
        return IsoUtils.padRight(merchantId, 15, ' ');
    }

    public static String packCardSeqNumber(TransRec trans) throws Exception {

        TCard cardInfo = trans.getCard();

        if (cardInfo != null && cardInfo.getPsn() >= 0) {
            return IsoUtils.padLeft(String.valueOf(cardInfo.getPsn()), 3, '0');
        }

        return null;
    }

    public static String packOriginalDataElements(TransRec trans, PaymentSwitch paySwitchCfg) {
        return String.format(Locale.getDefault(),"%04d%06d%.10s%.11s",
                trans.getProtocol().getOriginalMessageType(),
                trans.getProtocol().getOriginalStan(),
                trans.getAudit().getLastTransmissionDateTimeAsString("MMddHHmmss"),
                packAiic(paySwitchCfg));
    }

    public static String packAuthCode(TransRec trans) throws Exception {
        String authCode = trans.getProtocol().getAuthCode();
        while (authCode != null && authCode.length() < 6) {
            authCode = authCode.concat("0");
        }
        return authCode;

    }

    public static String packActionCode(TransRec trans, boolean isReversal) {
        if( isReversal ) {
            // termapp spec says to use:
            // 000 if txn was authorised
            // 911 if txn timed out
            TProtocol.ReversalReason reversalReason = trans.getProtocol().getReversalReason();
            switch (reversalReason) {
                case COMMS_FAIL:
                case TIMEOUT:
                case POWER_FAIL:
                case NOT_SET:
                    // treat these all as txn timed out
                    return "911";

                case CUSTOMER_CANCELLATION:
                case OPERATOR_REVERSAL:
                case GENACFAIL:
                case SIGFAIL:
                case ACQUIRER_APPROVED:
                    return "000";

                default:
                    Timber.e("Unknown/unhandled reversal reason %s. Add to switch", reversalReason.name());
                    return "000";
            }
        } else {
            // it's an advice, they're always approved set 000
            return "000";
        }
    }

    public static String packAiic(PaymentSwitch paySwitchCfg) {
        if( paySwitchCfg.getAiic() == null || paySwitchCfg.getAiic().length() == 0 ) {
            Timber.e( "ERROR - AIIC NOT SET IN CONFIG. USING DEFAULT OF ZEROS");
            return "00000000000";
        } else {
            return Util.leftPad(paySwitchCfg.getAiic(), 11, '0');
        }
    }

    /**
     * Generate a RRN using the following.
     * TTTTTT - TID but in BASE 62
     * BBB - Batch number
     * NNN - Transaction Sequence Number
     * Note: as we are packing into base 62, we have to use the numerical value rather than the string representation.
     *
     * @param terminalId terminal id/DE41
     * @param batchNumber batch number
     * @param transNumber trans number in batch
     * @return rrn string
     */
    public static String generateRrn(String terminalId, Integer batchNumber, Integer transNumber) {
        // step 1 - sanitise input terminal id
        if(Util.isNullOrEmpty(terminalId)) {
            // use tid value of 0 if input is empty
            terminalId = "0";
        }
        // remove all non-numeric chars. Only keeps numeric digits
        terminalId = terminalId.replaceAll("[^\\d]", "");
        if(Util.isNullOrEmpty(terminalId)) {
            // if no digits were present, use tid value of 0
            terminalId = "0";
        }

        // step 2 - convert sanitised TID (integer value) to base62
        String tid = Util.encodeBase62(Long.parseLong(terminalId));

        // step 3 - pad base62 tid and pack with batch and trans numbers
        return String.format( Locale.getDefault(), "%s%03d%03d", Util.padLeft(tid, 6, '0'), batchNumber, transNumber);
    }

    public static String calculateRetRefNumber(TransRec trans) {
        // For now just echoing the RRN already present in the record, else null
        if (!TextUtils.isEmpty(trans.getProtocol().getRRN())) {
            return trans.getProtocol().getRRN();
        } else {
            // Generate a new RRN number
            return generateRrn(trans.getAudit().getTerminalId(), BatchNumber.getCurValue(), ItemNumber.getNewValue());
        }
    }

    public static void updateRetRefNumber(TransRec trans) {
        try {
            As2805EftexUtils.packRetRefNumber(trans);
        } catch (Exception ex) {
            Timber.w(ex);
        }
    }

    public static String packRetRefNumber(TransRec trans) {
        String rrn = calculateRetRefNumber(trans);
        trans.getProtocol().setRRN(rrn);
        return rrn;
    }

    public static String packCashierId(String cashierId) {
        if (cashierId == null) {
            cashierId = "";
        }
        cashierId = IsoUtils.padRight(cashierId, 5, ' ');
        cashierId = cashierId.substring(0, 5);
        return cashierId;
    }

    public static AdditionalData packAdditionalData(IDependency d, TransRec trans, String iccData) {
        TAudit transAudit = trans.getAudit();

        AdditionalData addData = new AdditionalData();

        // build DE 48-F0.1 POS Data. 19 digits chars. This is a constructed field
        // POS terminal number, np 8
        // pos txn sequence number, n 6 - unique txn id for this batch
        // pos operator ID, np 5
        @SuppressLint("DefaultLocale")
        String sb = transAudit.getTerminalId() +
                String.format("%06d", transAudit.getReceiptNumber()) + // batch txn number goes here. increments for each txn in the batch, resets when new batch opened
                "00000";// TODO: could put POS operator in here in future
        addData.put(PosData, sb);

        // if CVV is present, pack it
        P2PLib p2pInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();
        int cvvLen = p2pEncrypt.getElementLength(CVV);
        if (cvvLen > 0) {
            // left pad to 4 chars as per termapp spec if required
            String cvv = As2805EftexUtils.packSensitiveField( CVV, SUBST_VAL_CVV);
            cvv = Util.leftPad( cvv, 4, ' ' );
            addData.put(CardVerificationData, cvv);
        }

        HashtableMessage data = new HashtableMessage();

        // pack DE48.16 structured data - Amex proprietary fields go in here
        // Amex specific fields
        if( !Util.isNullOrEmpty(d.getPayCfg().getAmexSellerId()) ) {
            data.put("SELLER_ID", d.getPayCfg().getAmexSellerId());
        }
        if( !Util.isNullOrEmpty(d.getPayCfg().getAmexSellerEmail()) ) {
            data.put("SELLER_EMAIL", d.getPayCfg().getAmexSellerEmail());
        }
        if( !Util.isNullOrEmpty(d.getPayCfg().getAmexSellerTelephone()) ) {
            data.put("SELLER_TELEPHONE", d.getPayCfg().getAmexSellerTelephone());
        }
        if( !Util.isNullOrEmpty(d.getPayCfg().getAmexPaymentFacilitator()) ) {
            data.put("PAYMENT_FACILITATOR", d.getPayCfg().getAmexPaymentFacilitator());
        }
        // deferred auths must have MessageReasonCode value of 5206
        if(trans.isDeferredAuth()) {
            data.put("MessageReasonCode", "5206");
        }

        if( trans.getCard() != null ) {
            // if MCR performed, add flag here. this gets translated by bp-node into DE47 in switch to issuer message
            if (trans.getCard().isCtlsMcrPerformed()) {
                data.put("MCR", "Y");
            }

            // Faulty Card Read (FCR). This indicates the primary read interface failed with this card. The flag should be set for EMV Technical
            // Fallback, as the chip failed and also for manual entry as the swipe read failed
            if( trans.getCard().getCaptureMethod() == ICC_FALLBACK_SWIPED ) {
                data.put("FCR","Y");
            }
        }

        try {
            // if icc data is in message (DE55)
            if( iccData != null ) {
                // then pack ICC data in here also
                data.put("EFTEX:ICCData", iccData);
            }
        } catch( Exception e ) {
            Timber.w(e);
        }

        // TODO: put other structured data fields in here as/when required

        // only pack/include the field if something was added above
        if( data.size() > 0 ) {
            addData.put(StructuredData, data.toMessageString());
        }


        return addData;
    }

    public static String packAdditionalAmounts(TransRec trans) {

        String amtCashback = "";
        String amtTip = "";
        TAmounts amounts = trans.getAmounts();
        TProtocol proto = trans.getProtocol();

        AdditionalAmount baseAdditionalAmt = new AdditionalAmount();
        baseAdditionalAmt.setAccountType(proto.getAccountType());
        baseAdditionalAmt.setAmountType("53"); // 53 = approved amount
        baseAdditionalAmt.setCurrencyCode(amounts.getCurrency());
        baseAdditionalAmt.setValue( amounts.getTotalAmount() ); // total amount
        String amtBase = baseAdditionalAmt.toString();

        if( amounts.getCashbackAmount() > 0) {
            AdditionalAmount cashback = new AdditionalAmount();
            cashback.setAccountType(proto.getAccountType());
            cashback.setAmountType("40");
            cashback.setCurrencyCode(amounts.getCurrency());
            cashback.setValue( amounts.getCashbackAmount() ); // sets sign and amount
            amtCashback = cashback.toString();
        }

        // eftex haven't specified if this should be anywhere, will leave it here until we get confirmation.
        // delete this comment by june 2022 if no-ones complained!
        if (amounts.getTip() > 0) {
            AdditionalAmount tip = new AdditionalAmount();
            tip.setAccountType(proto.getAccountType());
            tip.setAmountType("57");
            tip.setCurrencyCode(amounts.getCurrency());
            tip.setValue( amounts.getTip() ); // sets sign and amount
            amtTip = tip.toString();
        }

        return amtBase + amtCashback + amtTip/* + amtSurcharge*/;
    }

    public static String packCashAmount(TransRec trans) {
        if( trans.getAmounts().getCashbackAmount() > 0 ) {
            // pwcb txns, get amount from specific cashback amt field
            return IsoUtils.padLeft(String.valueOf(trans.getAmounts().getCashbackAmount()), 12, '0');
        } else if( trans.isCash() ) {
            // cash amount == total amount for cashout txns
            return IsoUtils.padLeft(String.valueOf(trans.getAmounts().getTotalAmount()), 12, '0');
        }
        // else don't pack de57
        return null;
    }

    public static void packAdditionalEmvTags(TransRec trans, EmvTags tags, As2805Eftex msg, boolean isAdvice)
            throws Exception {
        if (!tags.isTagSet(Tag.term_county_code)) {
            tags.add(Tag.term_county_code, trans.getAudit().getCountryCode());
        }

        if (!tags.isTagSet(Tag.amt_auth_num)) {
            String amountStr = msg.get(Bit._004_TRAN_AMOUNT);
            tags.add(Tag.amt_auth_num, amountStr);
        }

        if (!tags.isTagSet(Tag.tran_type)) {
            String tranType = msg.getProcessingCode().getTranType();
            tags.add(Tag.tran_type, tranType);
        }

        if (!tags.isTagSet(Tag.tran_date)) {
            String protoDate = msg.get(Bit._012_LOCAL_TRAN_DATETIME);
            // CTLS MSR seems to break this as the tag doesn't exist
            String date = protoDate != null ? msg.get(Bit._012_LOCAL_TRAN_DATETIME).substring(0, 6) : trans.getAudit().getTransDateTimeAsString("yyMMdd");
            tags.add(Tag.tran_date, date);
        }

        if (!tags.isTagSet(Tag.trans_curcy_code)) {
            String curr = "0" + msg.get(Bit._049_TRAN_CURRENCY_CODE);
            tags.add(Tag.trans_curcy_code, curr);
        }

        if (!tags.isTagSet(Tag.appl_pan_seqnum)) {
            // if PAN sequence number isn't read from the card, send zero value
            trans.getCard().setPsn(0);
            tags.add(Tag.appl_pan_seqnum, "00");
        }

        if( isAdvice ) {
            // tags that should be present in 0220 advices only
            if (!tags.isTagSet(auth_resp_code) && !Util.isNullOrEmpty(trans.getCard().getArc()) ) {
                tags.add(auth_resp_code, trans.getCard().getArc());
            }

            // .. add future ones here
        }
    }


    public static byte[] s_aucEftposRid = new byte[] {(byte) 0xA0, 0x00, 0x00, 0x03, (byte)0x84};

    public static boolean compareAid(byte[] aid, byte[] compareTo) {
        if (aid == null || compareTo == null) {
            return false;
        }

        byte[] rid = Arrays.copyOf(aid, compareTo.length);
        if (Arrays.equals(rid, compareTo)) {
            return true;
        }
        return false;
    }

    public static boolean isEftposAid(byte[] aid) {
        return compareAid(aid, s_aucEftposRid);
    }

    private static byte[] customPack( TransRec trans, EmvTags tags ) {
        boolean isEftpos = isEftposAid( Util.hexStringToByteArray(trans.getCard().getAid()));

        // if not an eftpos card, delete tag 4f (aid), according to spec DE 55 notes "applicable for EPAL transactions only"
        if( !isEftpos ) {
            tags.remove(aid.getValue());
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (Map.Entry<Integer, EmvTag> entry : tags.entrySet()) {
            byte[] b;
            // if this is a CTLS EFTPOS card, and tag is 95 (TVR), then send all zeros due to issue with EFTPOS host
            if( trans.getCard().getCaptureMethod() == CTLS && entry.getValue().getTag() == 0x95 && isEftpos ) {
                Timber.i( "Zeroing TVR because EFTPOS CTLS card" );
                b = new byte[] { (byte)0x95, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00 };
            } else {
                b = entry.getValue().pack();
            }
            stream.write(b, 0, b.length);
        }
        return stream.toByteArray();

    }

    /**
     * data packed in DE55 must be 'wrapped' in FF20 container tag
     *
     * @param trans
     * @return String - 'expanded' hex - icc data for DE55
     */
    public static String getIccDataForAdviceDe55(TransRec trans) {
        String iccData = null;
        if (!Util.isNullOrEmpty(trans.getEmvTagsString())) {
            iccData = packIccDataInFF20Container(trans.getEmvTagsString());
        } else if (!Util.isNullOrEmpty(trans.getCtlsTagsString())) {
            iccData = packIccDataInFF20Container(trans.getCtlsTagsString());
        } else {
            Timber.i("not packing DE 55 for reversal/advice as no tag data saved on trans record");
        }
        return iccData;
    }


    /**
     * packs DE55 ICC data the 'advice way', used by deferred auths, advices and reversals currently
     *
     * @param msg - as2805 msg object to pack de55 into
     * @param trans - input trans object
     * @throws UnknownFieldException - thrown by as2805 class on error
     */
    public static void packDe55AdviceStyle(As2805Eftex msg, TransRec trans) throws UnknownFieldException {
        String iccData = getIccDataForAdviceDe55(trans);
        if( iccData != null ) {
            msg.set(_055_ICC_DATA, iccData);
        }
    }

    /**
     * packs ICC data into DE48 the 'advice way', used by deferred auths, advices and reversals currently
     *
     * @param d - dependencies object
     * @param msg - as2805 msg output to pack de48 into
     * @param trans - input trans object
     * @throws Exception - thrown by as2805 class on error
     */
    public static void packDe48AdviceStyle(IDependency d, As2805Eftex msg, TransRec trans) throws Exception {
        String de48Subfield16IccData = getIccDataForAdviceDe48Subfield16(trans);
        msg.putAdditionalData(packAdditionalData(d, trans, de48Subfield16IccData));
    }

    /**
     * data packed in DE48.16 must NOT be wrapped in FF20 container tag.
     * take it exactly as it's stored in the transaction record
     *
     * @param trans
     * @return string - icc data in expanded/ascii format
     */
    public static String getIccDataForAdviceDe48Subfield16(TransRec trans) {
        String iccData = null;
        if (!Util.isNullOrEmpty(trans.getEmvTagsString())) {
            iccData = trans.getEmvTagsString();
        } else if (!Util.isNullOrEmpty(trans.getCtlsTagsString())) {
            iccData = trans.getCtlsTagsString();
        } else {
            Timber.i("not packing DE 48.16 for reversal/advice as no tag data saved on trans record");
        }
        return iccData;
    }

    /**
     * takes serialised list of EMV tags as input.
     * pre-pends/wraps the tag data in a FF20 template tag container
     *
     * @param serialisedTags - input serialised tags
     * @return tags wrapped in FF20 container tag
     */
    private static String packIccDataInFF20Container(String serialisedTags) {
        byte[] inputTagsBinary = Util.hexStringToByteArray(serialisedTags);
        EmvTag containerTag = new EmvTag( 0xFF20, inputTagsBinary );
        return Util.hex2Str(containerTag.pack());
    }

    private static byte[] getIccData(TransRec trans, As2805Eftex msg, boolean isAdvice) throws Exception {
        TCard cardinfo = trans.getCard();
        EmvTags tags = (EmvTags) cardinfo.getTags().clone();

        packAdditionalEmvTags(trans, tags, msg, isAdvice);

        if (!isAdvice) {
            // send 8A in advices only
            tags.remove(auth_resp_code.value());
        }
        return customPack(trans, tags);
    }

    /**
     * pack 'live' tag data retrieved from tag collection into format for DE55, wrapped in FF20 container tag
     *
     * @param trans trans rec
     * @param msg msg we're packing
     * @return packed icc data in string format
     * @throws Exception
     */
    public static String packIccDataInFF20Container(TransRec trans, As2805Eftex msg)  throws Exception {
        // last arg is always false as not used for advices
        byte[] packedTagData = getIccData(trans, msg, false);
        // wrap packedTagData in ff20 tag
        EmvTag containerTag = new EmvTag( 0xFF20, packedTagData );
        return Util.hex2Str(containerTag.pack());
    }

    /**
     * pack 'live' tag data retrieved from tag collection into format for DE48.16, i.e. NOT wrapped in FF20 container tag
     *
     * @param trans trans rec
     * @param msg msg we're packing
     * @return packed icc data in string format
     * @throws Exception
     */
    public static String packIccDataForDe48(TransRec trans, As2805Eftex msg)  throws Exception {
        if (trans.getCard().getTags() == null)
            return null;

        // last arg is always false as not used for advices
        byte[] packedTagData = getIccData(trans, msg, false);
        // for DE48.16, DON'T wrap packedTagData in ff20 container tag
        return Util.hex2Str(packedTagData);
    }

    public static void unpackIccData(TransRec trans, String field55)
            throws Exception {
        TCard cardinfo = trans.getCard();
        EmvTags containerTag = new EmvTags();
        EmvTags respTags = cardinfo.getRespTags();

        if (respTags == null) {
            respTags = new EmvTags();
            cardinfo.setRespTags(respTags);
        }

        if( field55 == null ) {
            return;
        }

        // data from eftex host is in an FF21 container tag
        byte[] field55Data = Util.hexStringToByteArray(field55);
        containerTag.unpack(field55Data);

        if( !containerTag.isTagSet(icc_response) ) {
            Timber.e( "Error - container tag FF21 missing in DE55 response" );
        }

        // unpack child elements to respTags
        respTags.unpack( containerTag.getTag(icc_response) );

        if (respTags.isTagSet(iss_auth_data)) {
            byte[] issuerAuthData = respTags.getTag(iss_auth_data);
            trans.getCard().setIssuerAuthData(issuerAuthData);
            Timber.i( "Issuer Auth Data: " + Util.byteArrayToHexString(issuerAuthData));
        }

        if (respTags.isTagSet(issuer_app_data)) {
            byte[] issuerAppData = respTags.getTag(issuer_app_data);
            trans.getCard().setIssuerAppData(issuerAppData);
            Timber.i( "Issuer App Data: " + Util.byteArrayToHexString(issuerAppData));
        }

        if (respTags.isTagSet(isuer_scrpt_templ_71)) {
            byte[] script71 = respTags.getTag(isuer_scrpt_templ_71);
            trans.getCard().setScript71Data(script71);
            Timber.i( "Script71: " + Util.byteArrayToHexString(script71));
        }

        if (respTags.isTagSet(isuer_scrpt_templ_72)) {
            byte[] script72 = respTags.getTag(isuer_scrpt_templ_72);
            trans.getCard().setScript72Data(script72);
            Timber.i( "Script72: " + Util.byteArrayToHexString(script72));
        }

    }


    public static String packSensitiveField(IP2PEncrypt.ElementType elementType, char fillChar ) {
        P2PLib p2pInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();

        int elementLength = p2pEncrypt.getElementLength( elementType );
        if( elementLength <= 0 )
            return "";

        return new String(new char[elementLength]).replace("\0", Character.toString(fillChar) );
    }

    private static String returnTagsFromTransRec(TCard cardinfo, TransRec trans) {
        if (cardinfo.isCtlsCaptured() && trans.getCtlsTagsString() != null) {
            return trans.getCtlsTagsString();
        } else if (cardinfo.isIccCaptured() && trans.getEmvTagsString() != null) {
            return trans.getEmvTagsString();
        } else {
            Timber.i( "No tags to pack");
            return null;
        }
    }

    public static String packIccDataCommon(IDependency d, TransRec trans, As2805Eftex msg, boolean additionalEmvTags)
            throws Exception {
        TCard cardinfo = trans.getCard();
        EmvTags tags = cardinfo.getTags();

        if (tags == null) {
            return returnTagsFromTransRec(cardinfo, trans);
        }

        if (additionalEmvTags) {
            packAdditionalEmvTags(trans, tags, msg, true);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ArrayList<Tag> list = d.getProtocol().getEmvTagList();
        boolean isEftpos = isEftposAid( Util.hexStringToByteArray(cardinfo.getAid()));

        for (Tag t : list) {
            EmvTag tag = tags.get(t.value());
            if (tag != null) {
                byte[] b;
                // if this is a CTLS EFTPOS card, and tag is 95 (TVR), then send all zeros due to issue with EFTPOS host
                if( cardinfo.getCaptureMethod() == CTLS && tag.getTag() == 0x95 && isEftpos ) {
                    Timber.i( "Zeroing TVR because EFTPOS CTLS card" );
                    b = new byte[] { (byte)0x95, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00 };
                } else {
                    b = tag.pack();
                }
                if (b != null && b.length > 0) {
                    stream.write(b, 0, b.length);
                }
            }
        }
        byte[] bytes = stream.toByteArray();
        return getBinary().getString(bytes);
    }
}
