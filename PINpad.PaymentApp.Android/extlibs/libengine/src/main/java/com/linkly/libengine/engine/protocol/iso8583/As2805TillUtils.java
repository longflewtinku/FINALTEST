package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.SUBST_VAL_CVV;
import static com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.Formatters.getBinary;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.CTLS;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC_FALLBACK_SWIPED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.MANUAL;
import static com.linkly.libengine.engine.transactions.properties.TCard.CardIssuer.AMEX;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMMDD_CHIP;
import static com.linkly.libsecapp.emv.Tag.aid;
import static com.linkly.libsecapp.emv.Tag.auth_resp_code;
import static com.linkly.libsecapp.emv.Tag.eftpos_payment_account_reference;
import static com.linkly.libsecapp.emv.Tag.iss_auth_data;
import static com.linkly.libsecapp.emv.Tag.issuer_app_data;
import static com.linkly.libsecapp.emv.Tag.isuer_scrpt_templ_71;
import static com.linkly.libsecapp.emv.Tag.isuer_scrpt_templ_72;

import com.linkly.libengine.config.paycfg.PaymentSwitch;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.AdditionalAmount;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.IsoUtils;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.ProcessingCode;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.FormatException;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData;
import com.linkly.libengine.engine.protocol.svfe.SvfePack;
import com.linkly.libengine.engine.protocol.svfe.SvfeUtils;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583Svfe;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.env.BatchNumber;
import com.linkly.libengine.env.RRN;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.EmvTag;
import com.linkly.libsecapp.emv.EmvTags;
import com.linkly.libsecapp.emv.Tag;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class As2805TillUtils {

    private static final int PAYMENT_ACCOUNT_REFERENCE_LENGTH = 29;

    private As2805TillUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static ProcessingCode packProcCode(TransRec trans) throws FormatException {
        ProcessingCode procCode;
        try {
            procCode = new ProcessingCode();

            if (trans.isReconciliation()) {
                procCode.setTranType("95"); // settlement totals
            } else if (trans.isLastReconciliation()) {
                procCode.setTranType("96"); // last-settlement totals
            } else if (trans.isPreReconciliation()) {
                procCode.setTranType("97"); // Pre-settlement totals
            } else if (trans.isSale() || trans.isCompletion() || trans.isPreAuth() || trans.isReversal()) {
                procCode.setTranType("00");
            } else if (trans.isCashback()) {
                procCode.setTranType("09");
            } else if (trans.isCash()) {
                procCode.setTranType("01");
            } else if (trans.isRefund() || trans.isPreAuthCancellation()) {
                procCode.setTranType("20");
            } else {
                Timber.e("ERROR - trans type unknown, assuming 00 processing code de 3");
                procCode.setTranType("00");
            }

            String accountType = String.format("%02d", trans.getProtocol().getAccountType());

            procCode.setFromAccountType(accountType);
            procCode.setToAccountType("00");
        } catch (Exception e) {
            Timber.e(e);
            throw new FormatException("Unable to pack processing code");
        }

        return procCode;
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

    public static String packCardSeqNumber(TransRec trans) throws FormatException {
        try {
            TCard cardInfo = trans.getCard();

            if (cardInfo != null && cardInfo.getPsn() >= 0) {
                return IsoUtils.padLeft(String.valueOf(cardInfo.getPsn()), 3, '0');
            }
        } catch (Exception e) {
            Timber.e(e);
            throw new FormatException("Unable to pack card sequence number");
        }
        return null;
    }

    public static String packPosConditionCode(TransRec trans) {
        // refer till host interface message spec for details on DE25 values
        // 'auto' transactions are initiated by a POS, so return "04" ECR/integrated POS condition code
        // else return "42" for standalone or "08" for MOTO
        EngineManager.TransType transType = trans.getTransType();

        if (trans.isMoto()) // Currently Till not support Pre-auths but still adding pre-auth moto check
            return "08";
        else if (transType.autoTransaction) {
            return "04";
        } else
            return "42";
    }

    public static String packOriginalDataElements(TransRec trans) {
        StringBuilder originalDataElements = new StringBuilder();

        // Happen if someone tries to reverse a transaction that has not been sent to the host
        if (trans.getProtocol().getOriginalMessageType() == null) {
            Timber.e("Original Message Type NULL");
        }

        // positions 1-4 contain original msg type of msg being reversed
        originalDataElements.append(String.format("%04d", trans.getProtocol().getOriginalMessageType()));

        // Can happen if someone tries to reverse a transaction that has not been sent to the host
        if (trans.getProtocol().getOriginalStan() == null) {
            Timber.e("OriginalStan NULL");
        }
        // positions 5-10 contain original STAN of msg being reversed
        originalDataElements.append(String.format("%06d", trans.getProtocol().getOriginalStan()));


        // positions 11-20 original transaction date/time
        String bankDate = trans.getProtocol().getBankDate();
        String bankTime = trans.getProtocol().getBankTime();
        String originalDateTime;
        if (bankDate != null && bankTime != null && bankDate.length() == 6 && bankTime.length() == 6) {
            // correct date/time from host. Use it
            originalDateTime = bankDate.substring(4) + bankDate.substring(2,4) + bankTime; // chop off century
        } else {
            originalDateTime = trans.getAudit().getTransDateTimeAsString("ddMMHHmmss");
            if (originalDateTime == null || originalDateTime.length() != 10) {
                originalDateTime = "0000000000";   // invalid date/time
            }
        }
        originalDataElements.append(originalDateTime);

        // positions 21-31 original aiic of the msg being reversed. Not used, "Filled with zeroes"

        // fill with zeros to 42 characters in length
        int lenRemaining = 42 - originalDataElements.length();
        if (lenRemaining > 0) {
            // append lenRemaining '0' chars
            originalDataElements.append(new String(new char[lenRemaining]).replace("\0", "0"));
        } else {
            // error...!
            Timber.i("ERROR packing original data elements.");
        }

        Timber.i("Original Data Elements: %s", originalDataElements.toString());

        return originalDataElements.toString();
    }

    private static String generateOfflineAuthCode(IDependency d, TransRec trans) {
        // From JIRA task:
        // "Make use of full terminal ID and the time of day in hhmmss format, then use a hash
        // algorithm (e.g. SHA1), and take the first 3 bytes, converted to integer then modulo
        // 1,000,000 to produce a 6 digit number. This should produce good 'randomness' across
        // all 6 digits, making it mathematically very unlikely to have collisions".

        try {
            TAudit auditInfo = trans.getAudit();
            As2805TillUtils.packTransTime(trans, d.getPayCfg().getBankTimeZone());

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            String shaBuffer = auditInfo.getTerminalId().concat(As2805TillUtils.packTransTime(trans, d.getPayCfg().getBankTimeZone()));
            Timber.i("Data for SHA1 (local authCode generation) : %s", shaBuffer);

            md.update(shaBuffer.getBytes(), 0, shaBuffer.length());
            byte[] sha1 = md.digest();

            // An int is 32 bits, signed, in Java - large enough for an unsigned value from 3 bytes,
            // as long as we block sign-extension from occurring in intermediate calculations.
            int authCode = sha1[0] & 0xFF;     // Mask off the upper bits of each byte, to ensure we end up with a positive integer.
            authCode *= 0x100;
            authCode += sha1[1] & 0xFF;
            authCode *= 0x100;
            authCode += sha1[2] & 0xFF;
            authCode %= 1000000;                // Modulo operation, to restrict to 6 digits.

            return String.format("%06d", authCode);

        } catch (Exception e) {
            Timber.e(e);
        }

        return "000000";
    }

    public static String packAuthCode(IDependency d, TransRec trans) throws FormatException {
        String authCode;
        try {
            authCode = trans.getProtocol().getAuthCode();

            // If we don't have an authCode yet (i.e. for EFB / offline approved), generate one.
            if (authCode == null || (authCode.isEmpty() || authCode.equals("000000"))) {
                authCode = generateOfflineAuthCode(d, trans);
                Timber.i("Terminal-generated auth code = %s", authCode);
            }

            while (authCode.length() < 6) {
                authCode = authCode.concat("0");
            }
        } catch (Exception e) {
            Timber.e(e);
            throw new FormatException("Unable to pack Auth code");
        }
        return authCode;

    }

    public static String packAiic(PaymentSwitch paySwitchCfg) {
        if (paySwitchCfg.getAiic() == null || paySwitchCfg.getAiic().length() == 0) {
            Timber.i("ERROR - AIIC NOT SET IN CONFIG. USING DEFAULT OF ZEROS");
            return "000000";
        } else {
            return paySwitchCfg.getAiic();
        }
    }

    public static String calculateRetRefNumber() {
        // Till way of generating the RRN RRN is a 12-digit field comprised of:
        // a)	The first 6 digits are a batch sequence number
        // b)	The last 6 digits are a transaction sequence number (env variable "RRN" is used for this)
        String rrn = String.format( Locale.getDefault(), "%06d%06d", BatchNumber.getCurValue(), RRN.getNewValue());
        return rrn.length() > 12 ? rrn.substring(rrn.length()-12) : rrn;
    }

    public static void updateRetRefNumber(TransRec trans) {
        try {
            As2805TillUtils.packRetRefNumber(trans);
        } catch (Exception ex) {
            Timber.i(ex);
        }
    }

    public static String packRetRefNumber(TransRec trans) {
        String rrn = trans.getProtocol().getRRN();
        if (rrn == null) {
            rrn = calculateRetRefNumber();
            trans.getProtocol().setRRN(rrn);
        }

        return rrn;
    }

    @SuppressWarnings("java:S3776")
    public static String packAdditionalDataNational47(TransRec trans, char substValCvv, boolean advice) {
        StringBuilder fieldData = new StringBuilder();
        TCard cardinfo = trans.getCard();

        // this is a constructed field with subfields
        // each subfield consists of:
        // - 3 character code
        // - applicable data (variable len)
        // - a '\' terminator char

        // see the Till spec for more detail

        // mandatory field - TCC - terminal capability codes.
        fieldData.append("TCC07\\"); // 07 = mag stripe, contact, and contactless readers

        // mandatory field - TAM - Transaction Acceptance Method.
        fieldData.append("TAM30\\"); // 3 = secure payment terminal 0 = not specified

        if (advice && trans.isEfbAuthorisedTransaction()){
            fieldData.append("FBKE\\");
        }
        // man pan transactions have extra fields
        if (trans.getCard().getCaptureMethod() == MANUAL) {
            // CCI indicates if CCV is present or not
            P2PLib p2pInstance = P2PLib.getInstance();
            IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();
            int cvvLen = p2pEncrypt.getElementLength(CVV);

            // if faulty mag stripe card read fallback to manual entry
            // TODO: test this! assuming that CVV isn't prompted on fallback to manpan on msr read failure
            if (trans.getCard().isFaultyMsr()) {
                // fallback due to faulty msr reads
                fieldData.append("FCR\\");
                // CCT should be present if CVV was prompted for, even if no CVV was entered
                fieldData.append("CCT0\\");
            } else {
                // man pan initiated txn
                if (cvvLen > 0) {
                    // yes, CVV entered/present
                    String cvv = As2805TillUtils.packSensitiveField(CVV, substValCvv);
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
            if (trans.getCard().isOverTelephone()) {
                fieldData.append("ECM1");
            } else if (trans.getCard().isMailOrder()) {
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
        if (isEftposAid(Util.hexStringToByteArray(trans.getCard().getAid())) && (null != cardinfo)) {
            EmvTags tags = cardinfo.getTags();
            if (null != tags && tags.isTagSet(eftpos_payment_account_reference)) {
                byte[] par = tags.getTag(eftpos_payment_account_reference);
                // update ARI tag only when PAR is valid
                if (isValidPaymentAccountReference(par)) {
                    fieldData.append("ARI");
                    fieldData.append(new String(par));
                    fieldData.append("\\");
                }
            }
        }

        // if MCR was performed, append MCR flag
        if ((null != cardinfo) && cardinfo.isCtlsMcrPerformed()) {
            fieldData.append("MCR");
            fieldData.append("\\");
        }

        return fieldData.toString();
    }

    private static boolean isValidPaymentAccountReference(byte[] input){
        return (input != null) && (input.length == PAYMENT_ACCOUNT_REFERENCE_LENGTH);
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
        TAudit auditinfo = trans.getAudit();

        String posData = auditinfo.getTerminalId() + As2805TillUtils.packStan(proto.getStan()) + As2805TillUtils.packCashierId(auditinfo.getUserId());
        AdditionalData addData = new AdditionalData();
        addData.put(AdditionalData.Field.PosData, posData);


        String authProfile = String.format("%d%d", 0, 0);
        addData.put(AdditionalData.Field.AuthProfile, authProfile);


        P2PLib p2pInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();
        int cvvLen = p2pEncrypt.getElementLength(CVV);

        if (cvvLen > 0) {
            String cvv = As2805TillUtils.packSensitiveField(CVV, SUBST_VAL_CVV);
            cvv = " " + cvv;
            addData.put(AdditionalData.Field.CardVerificationData, cvv);
        }
        return addData;
    }


    public static String packPosEntryMode(TransRec trans) {

        String posEntryMode;
        switch (trans.getCard().getCaptureMethod()) {
            case NOT_CAPTURED:
                // shouldnt' really happen, log a warning here and simulate icc entry
                posEntryMode = "05";
                Timber.w("card capture method NOT_CAPTURED - unexpected");
                break;

            case ICC_FALLBACK_KEYED:
            case MANUAL:
                posEntryMode = "61";
                break;

            case ICC_FALLBACK_SWIPED:
            case SWIPED:
                posEntryMode = "62";
                break;

            case ICC:
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
                posEntryMode = "61"; // manual PAN entry
                break;
        }

        // Till spec says position 3 is used to indicate PIN entry capability
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

    public static String packAdditionalAmounts(TransRec trans) {

        String amtTip = "";
        String amtSurcharge = "";
        TAmounts amounts = trans.getAmounts();
        TProtocol proto = trans.getProtocol();
        if (amounts.getTip() > 0) {
            AdditionalAmount cashback = new AdditionalAmount();
            cashback.setAccountType(proto.getAccountType());
            cashback.setAmountType("57"); //  amount type 57 for tip amount
            cashback.setCurrencyCode(amounts.getCurrency());
            cashback.setValue(amounts.getTip()); // sets sign and amount
            amtTip = cashback.toString();
        }
        if (amounts.getSurcharge() > 0) {
            AdditionalAmount surcharge = new AdditionalAmount();
            surcharge.setAccountType(proto.getAccountType());
            surcharge.setAmountType("42"); //  amount type 42 for surcharge
            surcharge.setCurrencyCode(amounts.getCurrency());
            surcharge.setValue(amounts.getSurcharge()); // sets sign and amount
            amtSurcharge = surcharge.toString();
        }
        return amtTip + amtSurcharge;
    }

    public static String packCashAmount(TransRec trans) {
        if (trans.getAmounts().getCashbackAmount() > 0) {
            // pwcb txns, get amount from specific cashback amt field
            return IsoUtils.padLeft(String.valueOf(trans.getAmounts().getCashbackAmount()), 12, '0');
        } else if (trans.isCash()) {
            // cash amount == total amount for cashout txns
            return IsoUtils.padLeft(String.valueOf(trans.getAmounts().getTotalAmount()), 12, '0');
        }
        // else don't pack de57
        return null;
    }

    /**
     * If "Appication Expiry Date" tag is present in secure storage and it Mastercard transaction then add this tag directly from secure storage into list of tags to be sent to host
     * The "Application Expiry Date" was not read into TCard record as it contains sensitive info. Thus this additional method is needed to fetch this tag
     * @param trans
     * @param tags
     * @throws FormatException
     */
    public static void packApplicationExpiryDateTag(TransRec trans, EmvTags tags) throws FormatException {
        try {
            P2PLib p2pInstance = P2PLib.getInstance();
            IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();

            if (p2pEncrypt.getElementLength(EXPIRY_YYMMDD_CHIP) > 0) {
                // Replace with clear data if it's Mastercard; do not send it otherwise
                tags.remove(Tag.expiry_date.value());

                if (isMastercardAid(Util.hexStringToByteArray(trans.getCard().getAid()))) {
                    // Reach out secure storage for Application Expiry Date value. We can send it in clear as we do not have expiry date as part of track but rather standalone tag
                    String v = p2pEncrypt.getData(EXPIRY_YYMMDD_CHIP);
                    tags.add(Tag.expiry_date, v);
                }
            }
        } catch (Exception e) {
            Timber.e(e);
            throw new FormatException("Unable to pack EMV tag");
        }
    }

    @SuppressWarnings("java:S1066") // we might add more condition to suit Till spec
    public static void packAdditionalEmvTags(TransRec trans, EmvTags tags, As2805Till msg, boolean isAdvice) throws FormatException {
        try {
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

            if (!tags.isTagSet(Tag.appl_pan_seqnum) && (trans.getCard() != null && trans.getCard().getPsn() >= 0)) {
                String value = Util.padLeft("" + trans.getCard().getPsn(), 2, '0');
                tags.add(Tag.appl_pan_seqnum, value);
            }

            if (isAdvice) {
                // tags that should be present in 0220 advices only
                if (!tags.isTagSet(auth_resp_code) && !Util.isNullOrEmpty(trans.getCard().getArc())) {
                    tags.add(auth_resp_code, trans.getCard().getArc());
                }

                // .. add future ones here
            }
        } catch (Exception e) {
            Timber.e(e);
            throw new FormatException("Unable to pack additional EMV tags");
        }
    }


    public static String packIccData(TransRec trans, As2805Till msg, boolean isAdvice) throws FormatException {
        return packIccDataImpl(trans, msg, isAdvice);
    }

    private static byte[] eftposRid = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, (byte) 0x84};
    private static byte[] mastercardRid = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};

    public static boolean compareAid(byte[] aid, byte[] compareTo) {
        if (aid == null || compareTo == null) {
            return false;
        }

        byte[] rid = Arrays.copyOf(aid, compareTo.length);
        return Arrays.equals(rid, compareTo);
    }

    public static boolean isEftposAid(byte[] aid) {
        return compareAid(aid, eftposRid);
    }

    public static boolean isMastercardAid(byte[] aid) {
        return compareAid(aid, mastercardRid);
    }

    private static byte[] customPack(TransRec trans, EmvTags tags) {
        boolean isEftpos = isEftposAid(Util.hexStringToByteArray(trans.getCard().getAid()));

        // if not an eftpos card, delete tag 4f (aid), according to spec DE 55 notes "applicable for EPAL transactions only"
        if (!isEftpos) {
            tags.remove(aid.getValue());
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (Map.Entry<Integer, EmvTag> entry : tags.entrySet()) {
            byte[] b;
            // if this is a CTLS EFTPOS card, and tag is 95 (TVR), then send all zeros due to issue with EFTPOS host
            if (trans.getCard().getCaptureMethod() == CTLS && entry.getValue().getTag() == 0x95 && isEftpos) {
                Timber.i("Zeroing TVR because EFTPOS CTLS card");
                b = new byte[]{(byte) 0x95, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00};
            } else {
                b = entry.getValue().pack();
            }
            stream.write(b, 0, b.length);
        }
        return stream.toByteArray();

    }

    private static String packIccDataImpl(TransRec trans, As2805Till msg, boolean isAdvice) throws FormatException {
        TCard cardinfo = trans.getCard();
        EmvTags tags = (EmvTags) cardinfo.getTags().clone();

        if (tags == null) {
            return null;
        }

        packAdditionalEmvTags(trans, tags, msg, isAdvice);

        packApplicationExpiryDateTag(trans, tags);

        if (!isAdvice) {
            // send 8A in advices only
            tags.remove(auth_resp_code.value());
        }

        return Util.hex2Str(customPack(trans, tags));
    }

    @SuppressWarnings("java:S3776") //cognitive complexity(21)
    public static String packIccDataCommon(IDependency d, TransRec trans, Iso8583Svfe msg, boolean additionalEmvTags) throws Exception {
        TCard cardinfo = trans.getCard();
        EmvTags tags = cardinfo.getTags();

        if (tags == null) {
            return returnTagsFromTransRec(cardinfo, trans);
        }

        if (additionalEmvTags) {
            SvfeUtils.packAdditionalEmvTags(trans, tags, msg);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        ArrayList<Tag> list = d.getProtocol().getEmvTagList();

        for (Tag t : list) {

            EmvTag tag = tags.get(t.value());
            if (tag != null) {
                // Workaround for FIS Connex host to not populate the PAR tag, when its length is not 29.
                if (tag.getTag() == eftpos_payment_account_reference.getValue() && !isValidPaymentAccountReference(tag.getData())) {
                    Timber.e("PAR tag dropped due to length check, length = %d", tag.getData().length);
                    tags.remove(eftpos_payment_account_reference.getValue()); // remove PAR tag
                    continue;
                }
                if (!SvfePack.p2peEncryptEnabled && t.isSensitiveCardholderData()) {
                    String value = SvfePack.getNonSensitiveElement(t.getElement());
                    if (value != null) {
                        /* must use a new tag so we arent just updating the one on the transaction with the old data */
                        EmvTag tNew = new EmvTag(tag.getTag(), Util.hexStringToByteArray(value));
                        tag = tNew;
                    }
                }
                byte[] b = tag.pack();

                if (b != null && b.length > 0)
                    stream.write(b, 0, b.length);
            }

        }
        byte[] bytes = stream.toByteArray();
        return getBinary().getString(bytes);
    }

    private static String returnTagsFromTransRec(TCard cardinfo, TransRec trans) {
        if (cardinfo.isCtlsCaptured() && trans.getCtlsTagsString() != null) {
            return trans.getCtlsTagsString();
        } else if (cardinfo.isIccCaptured() && trans.getEmvTagsString() != null) {
            return trans.getEmvTagsString();
        } else {
            Timber.i("No tags to pack");
            return null;
        }
    }

    public static void unpackIccData(TransRec trans, String field55) throws FormatException {
        try {
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
                byte[] issuerAuthData;
                byte[] hostIssuerAuthData =  respTags.getTag(iss_auth_data);
                byte[] responseCode = respTags.getTag(auth_resp_code);

                // Till workaround to address FIS Connex host issue. The Issuer Authentication Data(Tag 91) is split in to Tag 8A and Tag 91 for amex scheme by Connex host.
                // So to get correct IAD, both data's in Tag 91 and Tag 8A, needs to be combined
                if(cardinfo.getCardIssuer() == AMEX && responseCode != null){
                    // Initialise Issuer Auth Data object with expected data length
                    issuerAuthData = new byte[hostIssuerAuthData.length + responseCode.length];
                    System.arraycopy(hostIssuerAuthData, 0, issuerAuthData, 0, hostIssuerAuthData.length);
                    System.arraycopy(responseCode, 0, issuerAuthData, hostIssuerAuthData.length, responseCode.length);
                }
                else {
                    issuerAuthData = hostIssuerAuthData;
                }
                trans.getCard().setIssuerAuthData(issuerAuthData);
                Timber.i("Issuer Auth Data: " + Util.byteArrayToHexString(issuerAuthData));
            }

            if (respTags.isTagSet(issuer_app_data)) {
                byte[] issuerAppData = respTags.getTag(issuer_app_data);
                trans.getCard().setIssuerAppData(issuerAppData);
                Timber.i("Issuer App Data: " + Util.byteArrayToHexString(issuerAppData));
            }

            if (respTags.isTagSet(isuer_scrpt_templ_71)) {
                byte[] script71 = respTags.getTag(isuer_scrpt_templ_71);
                trans.getCard().setScript71Data(script71);
                Timber.i("Script71: " + Util.byteArrayToHexString(script71));
            }

            if (respTags.isTagSet(isuer_scrpt_templ_72)) {
                byte[] script72 = respTags.getTag(isuer_scrpt_templ_72);
                trans.getCard().setScript72Data(script72);
                Timber.i("Script72: " + Util.byteArrayToHexString(script72));
            }
        } catch (Exception e) {
            Timber.e(e);
            throw new FormatException("Unable to unpack ICC data");
        }
    }


    public static String packSensitiveField(IP2PEncrypt.ElementType elementType, char fillChar) {
        P2PLib p2pInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();

        int elementLength = p2pEncrypt.getElementLength(elementType);
        if (elementLength <= 0)
            return "";

        return new String(new char[elementLength]).replace("\0", Character.toString(fillChar));
    }

    /**
     * Update Tags read from DB before sending to the host. Used for advices and reversals. Add Sensitive tags from secure storage.
     * As of time of writing the only new element is "Application Expiry Date"
     * @param trans
     * @param tagsString
     * @return
     * @throws FormatException
     */
    public static String repackDE55Tags(TransRec trans, String tagsString) throws FormatException {
        if (!Util.isNullOrEmpty(tagsString)) {
            try {
                EmvTags outputTags = new EmvTags();

                byte[] tagsByteArray = Util.hexStringToByteArray(tagsString);
                outputTags.unpack(tagsByteArray);

                packApplicationExpiryDateTag(trans, outputTags);

                return Util.hex2Str(outputTags.pack());
            } catch (Exception e) {
                Timber.e(e);
                throw new FormatException("Unable to unpack ICC data");
            }
        }
        return null;
    }
}
