package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.protocol.iso8583.As2805SuncorpPack.substValCvv;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._024_NII;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.CTLS;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.CTLS_MSR;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.MANUAL;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_OFFLINE_PIN;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_ONLINE_PIN;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_PIN_AND_SIG;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.PLAINTEXT_OFFLINE_PIN;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.PLAINTEXT_PIN_AND_SIG;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.SIG;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.COMMS_FAIL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.CUSTOMER_CANCELLATION;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.OPERATOR_REVERSAL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.POWER_FAIL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.TIMEOUT;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.emv.Tag.iss_auth_data;
import static com.linkly.libsecapp.emv.Tag.issuer_app_data;
import static com.linkly.libsecapp.emv.Tag.isuer_scrpt_templ_71;
import static com.linkly.libsecapp.emv.Tag.isuer_scrpt_templ_72;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.config.paycfg.PaymentSwitch;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.AdditionalAmount;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.PosDataCode;
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
import com.linkly.libengine.env.BatchNumber;
import com.linkly.libengine.env.Stan;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.EmvTags;
import com.linkly.libsecapp.emv.Tag;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import timber.log.Timber;

public class As2805SuncorpUtils {

    private static final String TAG = "As2805SuncorpUtils";

    public static ProcessingCode packProcCode(TransRec trans) throws Exception {

        EngineManager.TransType transType = trans.getTransType();
        ProcessingCode procCode = new ProcessingCode();

        if (trans.isSale() ) {
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
        procCode.setFromAccountType(accountType);
        procCode.setToAccountType("00");
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
        return trans.getAudit().getTransDateTimeAsString("yyMMddHHmmss");
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

    public static String packExpiryDate(TransRec trans) {
        return trans.getCard().getExpiry();
    }


    public static PosDataCode packPosDataCode(TransRec trans) {

        TCard cardInfo = trans.getCard();
        TCard.CvmType cvmType = cardInfo.getCvmType();

        PosDataCode pdc = new PosDataCode();

        if (cardInfo.getCaptureMethod() == CTLS_MSR) {
            pdc.setCardDataInputCapability(PosDataCode.CardDataInputCapability._B_CONTACTLESS_MAGSTRIPE);
        } else if (cardInfo.getCaptureMethod() == CTLS) {
            pdc.setCardDataInputCapability(PosDataCode.CardDataInputCapability._A_CONTACTLESS_ICC);
        } else {
            pdc.setCardDataInputCapability(PosDataCode.CardDataInputCapability._5_MAGSTRIPE_KEY_ENTRY_ICC);
        }

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

           case ICC_OFFLINE:
                /* not implemented yet
                if ( !psPaymCfg->bDisableField55Advice ) {
                    pdc.setCardDataInputMode(PosDataCode.CardDataInputMode._5_ICC);
                    break;
                }
                */
                /* intentional fall through */
            case ICC_FALLBACK_KEYED:
            case MANUAL:
                pdc.setCardDataInputMode(PosDataCode.CardDataInputMode._6_KEY_ENTRY);
                break;

            case ICC_FALLBACK_SWIPED:
                pdc.setCardDataInputMode(PosDataCode.CardDataInputMode._9_ICC_FALLBACK_TO_MAGSTRIPE);
                break;
            case SWIPED:
            default:
                pdc.setCardDataInputMode(PosDataCode.CardDataInputMode._2_MAGSTRIPE);
                break;
        }

        if (cvmType == ENCIPHERED_ONLINE_PIN || cvmType == PLAINTEXT_PIN_AND_SIG || cvmType == ENCIPHERED_PIN_AND_SIG || cvmType == PLAINTEXT_OFFLINE_PIN || cvmType == ENCIPHERED_OFFLINE_PIN) {
            Timber.i( "ENCIPHERED_ONLINE_PIN CVM");
            pdc.setCardholderAuthMethod(PosDataCode.CardholderAuthMethod._1_PIN);
            pdc.setCardholderAuthEntity(PosDataCode.CardholderAuthEntity._0_NOT_AUTHENTICATED);
        } else if (cvmType == SIG) {
            Timber.i( "SIG CVM");
            pdc.setCardholderAuthMethod(PosDataCode.CardholderAuthMethod._2_SIGNATURE);
            pdc.setCardholderAuthEntity(PosDataCode.CardholderAuthEntity._4_MERCHANT);
        } else {
            Timber.i( "NO CVM");
            pdc.setCardholderAuthMethod(PosDataCode.CardholderAuthMethod._0_NONE);
            pdc.setCardholderAuthEntity(PosDataCode.CardholderAuthEntity._4_MERCHANT);
        }

        pdc.setCardDataOutputCapability(PosDataCode.CardDataOutputCapability._1_NONE);
        pdc.setTerminalOutputCapability(PosDataCode.TerminalOutputCapability._4_PRINTING_AND_DISPLAY);
        pdc.setPinCaptureCapability(PosDataCode.PinCaptureCapability._C_TWELVE);
        pdc.setTerminalOperator(PosDataCode.TerminalOperator._1_CARD_ACCEPTOR_OPERATED);
        pdc.setTerminalType(Engine.getDep().getCustomer().getTerminalType(trans));
        return pdc;
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
        } else
        {
            return "115";
        }
    }

    public static String packOriginalDataElements(IDependency d, TransRec trans) {
        PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
        StringBuilder originalDataElements = new StringBuilder();

        // positions 1-4 contain original msg type of msg being reversed
        originalDataElements.append( String.format( "%04d", trans.getProtocol().getOriginalMessageType()));

        // positions 5-10 contain original STAN of msg being reversed
        originalDataElements.append( String.format( "%06d", trans.getProtocol().getOriginalStan()));

        // positions 11-20 zero fill
        originalDataElements.append( "0000000000" );

        // positions 21-31 original aiic of the msg being reversed. First 2 bytes used to indicate the length of the data to follow
        String aiicVal = paySwitchCfg.getAiic();
        // pack length
        originalDataElements.append( String.format("%02d", aiicVal.length() ) );
        // pack data
        originalDataElements.append( aiicVal );

        // fill with zeros to 42 characters in length
        int lenRemaining = 42 - originalDataElements.length();
        if( lenRemaining > 0 ) {
            // append lenRemaining '0' chars
            originalDataElements.append( new String(new char[lenRemaining]).replace("\0", "0" ) );
        } else {
            // error...!
            Timber.i( "ERROR packing original data elements. AIIC must be too long!");
        }

        return originalDataElements.toString();
    }

    public static String packAuthCode(TransRec trans) throws Exception {
        String authCode = trans.getProtocol().getAuthCode();
        while (authCode != null && authCode.length() < 6) {
            authCode = authCode.concat("0");
        }
        return authCode;

    }

    public static void packNii(PaymentSwitch paySwitchCfg, As2805Suncorp msg) throws Exception {
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

    public static String calculateRetRefNumber(TransRec trans, PayCfg config) throws Exception {
        TProtocol proto = trans.getProtocol();
        TAudit auditinfo = trans.getAudit();
        String rrn = trans.getProtocol().getRRN();
        int stan = Stan.getCurValue() + 1; /* as calculated ahead of time for most messages */

        if (rrn != null && rrn.length() > 0) {
            return rrn;
        }

        if (auditinfo.getTerminalId() == null) {
            return null;
        }
        if (auditinfo.getTerminalId().length() <= 0) {
            return null;
        }

        if (proto.getBatchNumber() == -1) {
            proto.setBatchNumber(BatchNumber.getCurValue());
        }

        rrn = trans.calculateRetRefNumber();

        if (rrn == null) {
            /* one set of rules for mastercard */
            if (trans.getCard().getCardsConfig(config) == null) {
                Timber.i( "No cards config set yet");
            } else if (trans.getCard().getCardsConfig(config).getPsi() == null) {
                Timber.i( "No cards config PSI set yet");
            } else if (trans.getCard().getCardsConfig(config).getPsi().compareTo("M") == 0) {

                Date resultdate = new Date(trans.getAudit().getTransDateTime());
                Calendar cal = new GregorianCalendar();
                cal.setTime(resultdate); // Give your own date

                String year = trans.getAudit().getTransDateTimeAsString("yyyy");

                rrn = String.format("%s%03d%s%06d", year.substring(3),
                        cal.get(Calendar.DAY_OF_YEAR),
                        trans.getAudit().getTransDateTimeAsString("HH"),
                        stan);

            }

            if (rrn == null || rrn.length() <= 0) { /* different rules for everyone else */
                rrn = String.format("%s000%05d", trans.getAudit().getTransDateTimeAsString("MMdd"), stan);
            }
        }

        return rrn;
    }

    public static void updateRetRefNumber(TransRec trans, PayCfg cfg) {
        try {
            As2805SuncorpUtils.packRetRefNumber(trans, cfg);
        } catch (Exception ex) {
            Timber.w(ex);
        }
    }

    public static String packRetRefNumber(TransRec trans, PayCfg cfg) throws Exception {
        String rrn = calculateRetRefNumber(trans, cfg);
        trans.getProtocol().setRRN(rrn);
        return rrn;
    }

    public static String packAdditionalDataNational47(TransRec trans) throws Exception {
        StringBuilder fieldData = new StringBuilder();

        // this is a constructed field with subfields
        // each subfield consists of:
        // - 3 character code
        // - applicable data (variable len)
        // - a '\' terminator char

        // see the suncorp spec for more detail

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
            } else {
                // man pan initiated txn
                if (cvvLen > 0) {
                    // yes, CVV entered/present
                    String cvv = As2805SuncorpUtils.packSensitiveField(CVV, substValCvv);
                    fieldData.append("CCI1\\");
                    fieldData.append("CCV").append(cvv).append("\\");
                } else {
                    // no CVV present/entered
                    fieldData.append("CCI0\\");
                }
            }

            // CCT should be present if CVV was prompted for, even if no CVV was entered
            fieldData.append("CCT0\\");

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


        String posData = auditinfo.getTerminalId() + As2805SuncorpUtils.packStan(proto.getStan()) + As2805SuncorpUtils.packCashierId(auditinfo.getUserId());
        AdditionalData addData = new AdditionalData();
        addData.put(AdditionalData.Field.PosData, posData);


        String authProfile = String.format("%d%d", 0, 0);
        addData.put(AdditionalData.Field.AuthProfile, authProfile);


        P2PLib p2pInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();
        int cvvLen = p2pEncrypt.getElementLength(CVV);

        if (cvvLen > 0) {
            String cvv = As2805SuncorpUtils.packSensitiveField( CVV, substValCvv );
            cvv = " " + cvv;
            addData.put(AdditionalData.Field.CardVerificationData, cvv);
        }
        return addData;
    }


    public static String packPosEntryMode(TransRec trans) {

        String posEntryMode;
        switch(trans.getCard().getCaptureMethod()) {
            case NOT_CAPTURED:
                return "";
            case MANUAL:
                posEntryMode = "01";
                break;
            case SWIPED:
                posEntryMode = "02";
                break;

            case ICC:
            case ICC_FALLBACK_KEYED:
            case ICC_OFFLINE:
                posEntryMode = "05";
                break;

            case ICC_FALLBACK_SWIPED:
                posEntryMode = "62";
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

        // suncorp spec says position 3 is used to indicate PIN entry capability
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

        String result = "";
        TAmounts amounts = trans.getAmounts();
        TProtocol proto = trans.getProtocol();
        if (amounts.getTip() > 0) {
            AdditionalAmount cashback = new AdditionalAmount();
            cashback.setAccountType(proto.getAccountType());
            cashback.setAmountType("91");
            cashback.setCurrencyCode(amounts.getCurrency());
            cashback.setValue( amounts.getTip() ); // sets sign and amount
            result = cashback.toString();
        }
        return result;
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

    public static void packAdditionalEmvTags(TransRec trans, EmvTags tags, As2805Suncorp msg)
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
            String date = msg.get(Bit._012_LOCAL_TRAN_DATETIME).substring(0, 6);
            tags.add(Tag.tran_date, date);
        }

        /*
        if (!tags.isTagSet(Tag.tran_time)) {
            String time = msg.get(Bit._012_LOCAL_TRAN_DATETIME).substring(6);
            tags.add(Tag.tran_time, time);
        }
        */

        if (!tags.isTagSet(Tag.trans_curcy_code)) {
            String curr = "0" + msg.get(Bit._049_TRAN_CURRENCY_CODE);
            tags.add(Tag.trans_curcy_code, curr);
        }

        if (!tags.isTagSet(Tag.appl_pan_seqnum) && (trans.getCard() != null && trans.getCard().getPsn() >= 0)) {
            String value = Util.padLeft("" + trans.getCard().getPsn(), 2, '0');
            tags.add(Tag.appl_pan_seqnum, value);
        }
    }


    public static String packIccData(TransRec trans, As2805Suncorp msg)
            throws Exception {
        return packIccDataImpl(trans, msg, true);
    }

    private static String packIccDataImpl(TransRec trans, As2805Suncorp msg, boolean additionalEmvTags)
            throws Exception {
        TCard cardinfo = trans.getCard();
        EmvTags tags = cardinfo.getTags();

        if (tags == null) {
            return null;
        }

        if (additionalEmvTags) {
            packAdditionalEmvTags(trans, tags, msg);
        }

        return Util.hex2Str( tags.pack() );
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

    public static String packMac(As2805Suncorp msg) throws Exception {
        StringBuilder messageData = new StringBuilder();
        messageData.append(getSetField(msg, 2));
        messageData.append(getSetField(msg, 4));
        messageData.append(getSetField(msg, 11));
        messageData.append(getSetField(msg, 14));
        messageData.append(getSetField(msg, 35));
        messageData.append(getSetField(msg, 38));
        messageData.append(getSetField(msg, 39));
        messageData.append(getSetField(msg, 41));

        byte[] msgData = Formatters.getAscii().getBytes(messageData.toString());
        int numPadBytes = (8 - msgData.length % 8) % 8;

        byte[] newMsgData = new byte[msgData.length + numPadBytes];
        System.arraycopy(msgData, 0, newMsgData, 0, msgData.length);

        byte[] macBytes = P2PLib.getInstance().getIP2PSec().getDUKPTMac(newMsgData, IP2PSec.KeyGroup.TERM_GROUP);

        if (macBytes == null) {
            throw new Exception();
        }

        String s = Util.byteArrayToHexString(macBytes);


        s = s.toUpperCase();
        if (s.length() >= 8) {
            s = s.substring(0, 8);
        }
        return s;
    }

    private static String getSetField(As2805Suncorp msg, int field) {
        if (msg.isFieldSet(field) && !"null".equals(msg.get(field))) {
            return msg.get(field);
        } else {
            return "";
        }
    }
}
