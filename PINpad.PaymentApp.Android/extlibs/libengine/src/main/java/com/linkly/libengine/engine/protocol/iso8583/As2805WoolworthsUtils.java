package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.SUBST_VAL_CVV;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._024_NII;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.CTLS;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC_FALLBACK_SWIPED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.MANUAL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.COMMS_FAIL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.CUSTOMER_CANCELLATION;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.OPERATOR_REVERSAL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.POWER_FAIL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.TIMEOUT;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.IP2PSec.KeyGroup.DYNAMIC_GROUP;
import static com.linkly.libsecapp.emv.Tag.aid;
import static com.linkly.libsecapp.emv.Tag.auth_resp_code;
import static com.linkly.libsecapp.emv.Tag.iss_auth_data;
import static com.linkly.libsecapp.emv.Tag.issuer_app_data;
import static com.linkly.libsecapp.emv.Tag.isuer_scrpt_templ_71;
import static com.linkly.libsecapp.emv.Tag.isuer_scrpt_templ_72;

import com.linkly.libengine.config.paycfg.PaymentSwitch;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.AdditionalAmount;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.IsoUtils;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.ProcessingCode;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter.Formatters;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.engine.transactions.properties.TSec;
import com.linkly.libengine.env.LastUsedMerchantId;
import com.linkly.libengine.env.LastUsedTerminalId;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.EmvTag;
import com.linkly.libsecapp.emv.EmvTags;
import com.linkly.libsecapp.emv.Tag;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

public class As2805WoolworthsUtils {

    private static final String TAG = "As2805WoolworthsUtils";

    public static ProcessingCode packProcCode(TransRec trans) throws Exception {

        EngineManager.TransType transType = trans.getTransType();
        ProcessingCode procCode = new ProcessingCode();

        if( trans.isReconciliation()) {
            procCode.setTranType("96"); // settlement totals enquiry (no reset)
        } else if (trans.isSale() || trans.isCompletion() || trans.isPreAuth() || trans.isReversal() ) {
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

    public static String packTrack2(String track2) {
        /* to be updated when we do p2pe */
        if (track2 != null) {
            return track2;
        }
        return null;
    }

    public static String packReconIndicator(long batchNumber) {
        return IsoUtils.padLeft(String.valueOf(batchNumber), 3, '0');
    }

    public static String packStan(long stan) {
        return IsoUtils.padLeft(String.valueOf(stan), 6, '0');
    }

    public static String packLocalDateTime(TransRec trans) {
        return trans.getAudit().getTransDateTimeAsString("MMddHHmmss");
    }

    public static String packTransTime(TransRec trans, String bankTimeZone) {
        return trans.getAudit().getTransDateTimeAsString("HHmmss", bankTimeZone);
    }

    public static String packTransDate(TransRec trans, String bankTimeZone) {
        return trans.getAudit().getTransDateTimeAsString("MMdd", bankTimeZone);
    }

    public static String packReversalDateTime(TransRec trans) {
        if (Engine.getDep().getPayCfg().isReversalCopyOriginal()) {
            return trans.getProtocol().getOrginalTransmissionDateTimeAsString("yyMMddHHmmss");
        } else {
            return trans.getAudit().getReversalDateTimeAsString("yyMMddHHmmss");
        }
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

    public static String packPosConditionCode(TransRec trans) {
        // refer woolies host interface message spec for details on DE25 values
        // 'auto' transactions are initiated by a POS, so return "04" ECR/integrated POS condition code
        // else return "42" for standalone or "08" for MOTO
        EngineManager.TransType transType = trans.getTransType();

        if (transType.autoTransaction) {
            if (transType == EngineManager.TransType.SALE_MOTO_AUTO || transType == EngineManager.TransType.REFUND_MOTO_AUTO )
                return "08";
            else
                return "04";
        }
        else
            return "42";
    }

    public static String packMsgReasonCode(TProtocol.ReversalReason reversalReason) {

        if (reversalReason == CUSTOMER_CANCELLATION) {
            return "4023";
        } else if (reversalReason == COMMS_FAIL) {
            return "4025";
        } else if (reversalReason == TIMEOUT) {
            return "4021";
        } else if (reversalReason == POWER_FAIL) {
            return "4026";
        } else if (reversalReason == OPERATOR_REVERSAL) {
            return "4000";
        } else {
            return "4027";
        }
    }

    public static String packReversalReason(TProtocol.ReversalReason reversalReason) {

        if (reversalReason == CUSTOMER_CANCELLATION) {
            return "100";
        } else if (reversalReason == COMMS_FAIL) {
            return "909";
        } else if (reversalReason == TIMEOUT) {
            return "911";
        } else if (reversalReason == POWER_FAIL) {
            return "909";
        } else if (reversalReason == OPERATOR_REVERSAL) {
            return "000";
        } else {
            return "115";
        }
    }

    public static String packOriginalDataElements(IDependency d, TransRec trans) {
        PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
        StringBuilder originalDataElements = new StringBuilder();

        // Happen if someone tries to reverse a transaction that has not been sent to the host
        if(trans.getProtocol().getOriginalMessageType() == null) {
            Timber.e( "Original Message Type NULL");
        }

        // positions 1-4 contain original msg type of msg being reversed
        originalDataElements.append( String.format( "%04d", trans.getProtocol().getOriginalMessageType()));

        // Can happen if someone tries to reverse a transaction that has not been sent to the host
        if(trans.getProtocol().getOriginalStan() == null) {
            Timber.e( "OriginalStan NULL");
        }
        // positions 5-10 contain original STAN of msg being reversed
        originalDataElements.append( String.format( "%06d", trans.getProtocol().getOriginalStan()));


        // positions 11-20 original transaction date/time
        String bankDate = trans.getProtocol().getBankDate();
        String bankTime = trans.getProtocol().getBankTime();
        String originalDateTime;
        if (bankDate != null && bankTime != null && bankDate.length() == 4 && bankTime.length() == 6) {
            // correct date/time from host. Use it
            originalDateTime = bankTime + bankDate.substring(2); // chop off century
        }
        else {
            originalDateTime = trans.getAudit().getTransDateTimeAsString("HHmmyyMMdd");
            if (originalDateTime == null || originalDateTime.length() != 10 ) {
                originalDateTime = "0000000000";   // invalid date/time
            }
        }
        originalDataElements.append(originalDateTime);

        // positions 21-31 original aiic of the msg being reversed. Not used, "Filled with zeroes"

        // fill with zeros to 42 characters in length
        int lenRemaining = 42 - originalDataElements.length();
        if( lenRemaining > 0 ) {
            // append lenRemaining '0' chars
            originalDataElements.append( new String(new char[lenRemaining]).replace("\0", "0" ) );
        } else {
            // error...!
            Timber.i( "ERROR packing original data elements.");
        }

        Timber.i( "Original Data Elements: %s", originalDataElements.toString());

        return originalDataElements.toString();
    }

    public static String packAuthCode(TransRec trans) throws Exception {
        String authCode = trans.getProtocol().getAuthCode();
        while (authCode != null && authCode.length() < 6) {
            authCode = authCode.concat("0");
        }
        return authCode;

    }

    public static void packNii(PaymentSwitch paySwitchCfg, As2805Woolworths msg) throws Exception {
        if( paySwitchCfg.getNii() != null && paySwitchCfg.getNii().length() > 0 ) {
            msg.set(_024_NII, paySwitchCfg.getNii());
        }
        // else don't pack it, it's optional
    }

    public static String packAiic(PaymentSwitch paySwitchCfg) throws Exception {
        if( paySwitchCfg.getAiic() == null || paySwitchCfg.getAiic().length() == 0 ) {
            Timber.i( "ERROR - AIIC NOT SET IN CONFIG. USING DEFAULT OF ZEROS");
            return "000000";
        } else {
            return paySwitchCfg.getAiic();
        }
    }

    public static String calculateRetRefNumber(TransRec trans) throws Exception {

        // WPay way of generating the RRN, RRN is a 12-digit field
        // For AES/3DES DUKPT, DE37 shall contain the following format:
        // YDDD (position 1-4) is the year and day of year, Y=0-9, DDD=001-366
        // HH (position 5-6) is the hours value.
        // NNNNNN (position 7-12) is the value from DE11 (STAN).
        String rrn = String.format(Locale.getDefault(), "%01d%03d%02d%06d", Calendar.getInstance().get(Calendar.YEAR) % 10, Calendar.getInstance().get(Calendar.DAY_OF_YEAR),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY), trans.getProtocol().getStan());
        return rrn.length() > 12 ? rrn.substring(rrn.length() - 12) : rrn;
    }

    public static void updateRetRefNumber(TransRec trans) {
        try {
            As2805WoolworthsUtils.packRetRefNumber(trans);
        } catch (Exception ex) {
            Timber.w(ex);
        }
    }

    public static String packRetRefNumber(TransRec trans) throws Exception {
        String rrn = trans.getProtocol().getRRN();
        if (rrn == null) {
            rrn = calculateRetRefNumber(trans);
            trans.getProtocol().setRRN(rrn);
        }

        return rrn;
    }

    public static String packAdditionalDataNational47(TransRec trans, char substValCvv) throws Exception {
        StringBuilder fieldData = new StringBuilder();
        TCard cardinfo = trans.getCard();

        // this is a constructed field with subfields
        // each subfield consists of:
        // - 3 character code
        // - applicable data (variable len)
        // - a '\' terminator char

        // see the Woolworths spec for more detail

        // mandatory field - TCC - terminal capability codes.
        fieldData.append( "TCC07\\" ); // 07 = mag stripe, contact, and contactless readers

        // man pan transactions have extra fields
        if( trans.getCard().getCaptureMethod() == MANUAL) {
            // CCI indicates if CCV is present or not
            P2PLib p2pInstance = P2PLib.getInstance();
            IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();
            int cvvLen = p2pEncrypt.getElementLength(CVV);

            // if faulty mag stripe card read fallback to manual entry
            // TODO: test this! assuming that CVV isn't prompted on fallback to manpan on msr read failure
            if( trans.getCard().isFaultyMsr() ) {
                // fallback due to faulty msr reads
                fieldData.append( "FCR\\" );
                // CCT should be present if CVV was prompted for, even if no CVV was entered
                fieldData.append("CCT0\\");
            } else {
                // man pan initiated txn
                if (cvvLen > 0) {
                    // yes, CVV entered/present
                    String cvv = As2805WoolworthsUtils.packSensitiveField( CVV, substValCvv );
                    fieldData.append("CCI1\\");
                    fieldData.append("CCV").append(cvv).append("\\");
                } else {
                    // no CVV present/entered
                    fieldData.append("CCI0\\");
                }
                // CCT should be present if CVV was prompted for, even if no CVV was entered
                fieldData.append("CCT1\\");
            }

            // ECMmt\ field = E commerce and mail/telephone order indicator
            // first digit
            if( trans.getCard().isOverTelephone() ) {
                fieldData.append("ECM1");
            } else if( trans.getCard().isMailOrder()) {
                fieldData.append("ECM2");
            } else {
                fieldData.append("ECM0");
            }
            // second digit, always '1' for single transaction. we don't support recurring or instalment payments
            fieldData.append("1\\");
        } else if (trans.getCard().getCaptureMethod() == ICC_FALLBACK_SWIPED) {
            Timber.d("Fallback to swipe from ICC, confirm if ICC card before adding FCR tag");
            if (trans.getCard().isIccCardSC()) {
                Timber.d("Adding FCR tag");
                // fallback to MSR due to faulty chip read
                fieldData.append("FCR\\");
            }
        }

        // if this is an eftpos card, look for tag 9f24 (Payment Account Reference, PAR), and send in an ARI tag
        if( isEftposAid( Util.hexStringToByteArray(trans.getCard().getAid()))) {
            if( null != cardinfo ) {
                EmvTags tags = cardinfo.getTags();
                if (null != tags && tags.isTagSet(Tag.eftpos_payment_account_reference)) {
                    byte par[] = tags.getTag(Tag.eftpos_payment_account_reference);
                    fieldData.append("ARI");
                    fieldData.append(new String(par));
                    fieldData.append("\\");
                }
            }
        }

        // if MCR was performed, append MCR flag
        if( cardinfo.isCtlsMcrPerformed() ) {
            fieldData.append("MCR");
            fieldData.append("\\");
        }

        return fieldData.toString();
    }

    public static String packCashierId(String cashierId) {
        if (cashierId == null) {
            cashierId = "";
        }
        cashierId = IsoUtils.padRight(cashierId, 5, ' ');
        cashierId = cashierId.substring(0, 5);
        return cashierId;
    }

    public static AdditionalData packAdditionalData(TransRec trans) {
        TProtocol proto = trans.getProtocol();
        TSec secinfo = trans.getSecurity();
        TAudit auditinfo = trans.getAudit();
        TCard cardinfo = trans.getCard();


        String posData = auditinfo.getTerminalId() + As2805WoolworthsUtils.packStan(proto.getStan()) + As2805WoolworthsUtils.packCashierId(auditinfo.getUserId());
        AdditionalData addData = new AdditionalData();
        addData.put(AdditionalData.Field.PosData, posData);


        String authProfile = String.format("%d%d", 0, 0);
        addData.put(AdditionalData.Field.AuthProfile, authProfile);


        P2PLib p2pInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();
        int cvvLen = p2pEncrypt.getElementLength(CVV);

        if (cvvLen > 0) {
            String cvv = As2805WoolworthsUtils.packSensitiveField( CVV, SUBST_VAL_CVV);
            cvv = " " + cvv;
            addData.put(AdditionalData.Field.CardVerificationData, cvv);
        }
        return addData;
    }


    public static String packPosEntryMode(TransRec trans) {

        String posEntryMode;
        switch(trans.getCard().getCaptureMethod()) {
            case NOT_CAPTURED:
                // shouldnt' really happen, log a warning here and simulate icc entry
                posEntryMode = "05";
                Timber.w( "card capture method NOT_CAPTURED - unexpected" );
                break;
            case MANUAL:
                posEntryMode = "01";
                break;

            case ICC_FALLBACK_SWIPED:
                posEntryMode = "62";
                break;

            case SWIPED:
                posEntryMode = "02";
                break;

            case ICC:
            case ICC_FALLBACK_KEYED:
            case ICC_OFFLINE:
                posEntryMode = "05";
                break;

            case CTLS:
                posEntryMode = "07";
                break;
            case CTLS_MSR:
                posEntryMode = "91";
                break;
            default:
                posEntryMode = "01"; // manual PAN entry
                break;
        }

        // Woolworths spec says position 3 is used to indicate PIN entry capability
        //  1 = pin entry capability
        //  2 = no PIN entry capability
        // assuming because this is CAPABILITY, and not term caps USED, let's hard-code that value to '1'
        posEntryMode += "1";
        return posEntryMode;
    }

    public static String packKsn(byte[] ksn) {
        if (ksn == null) {
            return null;
        }
        return Util.byteArrayToHexString(ksn);
    }

    public static String packPinBlock(byte[] pinBlock) {

        if (pinBlock == null) {
            return null;
        }

        try {
            return Formatters.getBinary().getString(pinBlock);
        } catch (Exception e) {
            Timber.w(e);
        }

        return null;

    }

    public static String packAdditionalAmounts(TransRec trans) {

        String amtTip = "";
        String amtSurcharge = "";
        TAmounts amounts = trans.getAmounts();
        TProtocol proto = trans.getProtocol();
        if (amounts.getTip() > 0) {
            AdditionalAmount cashback = new AdditionalAmount();
            cashback.setAccountType(proto.getAccountType());
            cashback.setAmountType("57"); // woolies use amount type 57 for tip amount
            cashback.setCurrencyCode(amounts.getCurrency());
            cashback.setValue( amounts.getTip() ); // sets sign and amount
            amtTip = cashback.toString();
        }
        if (amounts.getSurcharge() > 0) {
            AdditionalAmount surcharge = new AdditionalAmount();
            surcharge.setAccountType(proto.getAccountType());
            surcharge.setAmountType("70"); // woolies use amount type 70 for surcharge
            surcharge.setCurrencyCode(amounts.getCurrency());
            surcharge.setValue( amounts.getSurcharge() ); // sets sign and amount
            amtSurcharge = surcharge.toString();
        }
        return amtTip + amtSurcharge;
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

    public static void packAdditionalEmvTags(TransRec trans, EmvTags tags, As2805Woolworths msg, boolean isAdvice)
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

        if( isAdvice ) {
            // tags that should be present in 0220 advices only
            if (!tags.isTagSet(auth_resp_code) && !Util.isNullOrEmpty(trans.getCard().getArc()) ) {
                tags.add(auth_resp_code, trans.getCard().getArc());
            }

            // .. add future ones here
        }
    }

    /**
     * checks if the TID or MID has changed since the last time we performed a registration
     * Registration is the 0800/0810 with stan 000000, which wpay connex host uses to link
     * a tid/mid with a DUKPT key.
     *
     * @return true = tid and/or mid has changed
     */
    public static boolean tidOrMidChanged(IDependency d) {
        String lastTid = LastUsedTerminalId.getCurValue();
        String lastMid = LastUsedMerchantId.getCurValue();

        String currentTid = d.getPayCfg().getStid();
        String currentMid = d.getPayCfg().getMid();

        return !Objects.equals(lastTid, currentTid) || !Objects.equals(lastMid, currentMid);
    }


    public static String packIccData(TransRec trans, As2805Woolworths msg, boolean isAdvice)
            throws Exception {
        return packIccDataImpl(trans, msg, isAdvice);
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

    private static String packIccDataImpl(TransRec trans, As2805Woolworths msg, boolean isAdvice)
            throws Exception {
        TCard cardinfo = trans.getCard();
        EmvTags tags = (EmvTags) cardinfo.getTags().clone();

        if (tags == null) {
            return null;
        }

        packAdditionalEmvTags(trans, tags, msg, isAdvice);

        if (!isAdvice) {
            // send 8A in advices only
            tags.remove(auth_resp_code.value());
        }

        return Util.hex2Str( customPack(trans, tags) );
    }

    public static void unpackIccData(TransRec trans, String field55)
            throws Exception {
        TCard cardinfo = trans.getCard();
        EmvTags respTags = cardinfo.getRespTags();

        if (respTags == null) {
            respTags = new EmvTags();
            cardinfo.setRespTags(respTags);
        }

        if (field55 != null) {
            byte[] field55Data = Util.hexStringToByteArray(field55);
            respTags.unpack(field55Data);
        }

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


    private static String getSetField(As2805Woolworths msg, int field) {
        if (msg.isFieldSet(field) && !"null".equals(msg.get(field))) {
            return msg.get(field);
        } else {
            return "";
        }
    }


    public static void setBankDateAndTime(Date date, TransRec trans){
        DateFormat dateFormat = new SimpleDateFormat("yyMMdd", Locale.getDefault());
        String strDate = dateFormat.format(date);
        trans.getProtocol().setBankDate(strDate);

        DateFormat timeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
        String strTime = timeFormat.format(date);
        trans.getProtocol().setBankTime(strTime);
    }

    public static int getCurrentDukptKeyIndex() {
        byte[] ksn = P2PLib.getInstance().getIP2PSec().getDUKPTKsn(DYNAMIC_GROUP);
        if (ksn == null || ksn.length < 10){
            Timber.e("ERROR, ksn not returned or expected length");
            return -1;
        }
        // mask low 21 bits
        byte[] keyIndexBytes = new byte[3];
        System.arraycopy(ksn, 7, keyIndexBytes, 0, 3);

        // Correcting the bitwise operations with parentheses and avoiding sign extension
        return ((keyIndexBytes[0] & 0x1F) << 16) // Mask first byte to lower 5 bits then shift
                + ((keyIndexBytes[1] & 0xFF) << 8) // Mask and shift the second byte
                + (keyIndexBytes[2] & 0xFF); // Add the third byte
    }

    static String getCurrentDukptKsn() {
        byte[] ksn = P2PLib.getInstance().getIP2PSec().getDUKPTKsn(DYNAMIC_GROUP);
        return Util.byteArrayToHexString(ksn);
    }

    public static String incrementDukptKsn() {
        byte[] oldKsn = P2PLib.getInstance().getIP2PSec().getDUKPTKsn(DYNAMIC_GROUP);
        P2PLib.getInstance().getIP2PSec().incDUKPTKsn(DYNAMIC_GROUP);
        byte[] newKsn = P2PLib.getInstance().getIP2PSec().getDUKPTKsn(DYNAMIC_GROUP);
        Timber.i("Woolworths performProtocolChecks() incrementing DUKPT ksn, old KSN = %s, new KSN = %s", Util.byteArrayToHexString(oldKsn), Util.byteArrayToHexString(newKsn));
        return Util.byteArrayToHexString(newKsn);
    }

}
