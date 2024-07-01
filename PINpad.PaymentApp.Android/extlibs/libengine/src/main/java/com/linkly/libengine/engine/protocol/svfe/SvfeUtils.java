package com.linkly.libengine.engine.protocol.svfe;

import static com.linkly.libengine.engine.EngineManager.TransType.BALANCE;
import static com.linkly.libengine.engine.EngineManager.TransType.CASH;
import static com.linkly.libengine.engine.EngineManager.TransType.COMPLETION;
import static com.linkly.libengine.engine.EngineManager.TransType.OFFLINECASH;
import static com.linkly.libengine.engine.EngineManager.TransType.OFFLINESALE;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.EngineManager.TransType.REFUND;
import static com.linkly.libengine.engine.EngineManager.TransType.TESTCONNECT;
import static com.linkly.libengine.engine.EngineManager.TransType.TOPUPCOMPLETION;
import static com.linkly.libengine.engine.EngineManager.TransType.TOPUPPREAUTH;
import static com.linkly.libengine.engine.protocol.svfe.SvfePack.substValCvv;
import static com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.Formatters.getBinary;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_DEFAULT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.COMMS_FAIL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.CUSTOMER_CANCELLATION;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.TIMEOUT;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PIN_BLOCK;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PIN_BLOCK_KSN;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptAlgorithm.DUKPT_ANSI_2009;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptAlgorithm.NONE;
import static com.linkly.libsecapp.IP2PEncrypt.PackFormat.ASCII;
import static com.linkly.libsecapp.emv.Tag.iss_auth_data;
import static com.linkly.libsecapp.emv.Tag.issuer_app_data;
import static com.linkly.libsecapp.emv.Tag.isuer_scrpt_templ_71;
import static com.linkly.libsecapp.emv.Tag.isuer_scrpt_templ_72;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.svfe.openisoj.AdditionalAmount;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583Rev93.Bit;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583Svfe;
import com.linkly.libengine.engine.protocol.svfe.openisoj.IsoUtils;
import com.linkly.libengine.engine.protocol.svfe.openisoj.ProcessingCode;
import com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.Formatters;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.CardholderDataElement;
import com.linkly.libsecapp.EncryptResult;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.EmvTag;
import com.linkly.libsecapp.emv.EmvTags;
import com.linkly.libsecapp.emv.Tag;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import timber.log.Timber;

public class SvfeUtils {

    private static final String TAG = "SvfeUtils";

    public static ProcessingCode packProcCode(TransRec trans) throws Exception {

        long cashbackAmount = 0;
        boolean isRefund = false;
        boolean isBalance = false;
        EngineManager.TransType transType = trans.getTransType();
        ProcessingCode procCode = new ProcessingCode();

        if (trans.isSale() || transType == OFFLINESALE) {
            procCode.setTranType("00");
        } else if (trans.isCashback()) {
            procCode.setTranType("09");
        } else if (transType == CASH || transType == OFFLINECASH) {
            procCode.setTranType("01");
        } else if (trans.isCashback()) {
            procCode.setTranType("09");
        } else if (transType == REFUND) {
            procCode.setTranType("20");
        } else if (transType == BALANCE) {
            procCode.setTranType("31");
        } else if (transType == PREAUTH || transType == TOPUPPREAUTH) {
            procCode.setTranType("93");
        } else if (transType == COMPLETION || transType == TOPUPCOMPLETION) {
            procCode.setTranType("94");
        } else if (transType == RECONCILIATION) {
            procCode.setTranType("91");
        } else if (transType == TESTCONNECT) {
            procCode.setTranType("99");

        } else if (trans.isDeposit()) {
            procCode.setTranType("21");
        } else {
            procCode.setTranType("00");
        }


        String accountType = String.format("%02d", trans.getProtocol().getAccountType());
        procCode.setFromAccountType(accountType);
        procCode.setToAccountType("00");
        return procCode;

    }

    public static String packSensitiveField( IP2PEncrypt.ElementType elementType, char fillChar ) {
        P2PLib p2pInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();

        int elementLength = p2pEncrypt.getElementLength( elementType );
        if( elementLength <= 0 )
            return "";

        return new String(new char[elementLength]).replace("\0", Character.toString(fillChar) );
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
                posEntryMode = "08";
                break;
            case CTLS:
                posEntryMode = "07";
                break;
            case CTLS_MSR:
                posEntryMode = "91";
                break;
            default:
                posEntryMode = "01"; //??
                break;
        }

        switch(trans.getCard().getCvmType()) {
            case NO_CVM_SET:
            case NO_CVM:
                posEntryMode += "0";
                break;
            case SIG:
                posEntryMode += "2";
                break;
            case CDCVM:

            case PLAINTEXT_PIN_AND_SIG:   // ?? also sig
            case ENCIPHERED_PIN_AND_SIG: //  ?? also SIG
            case PLAINTEXT_OFFLINE_PIN:
            case ENCIPHERED_OFFLINE_PIN:
                posEntryMode += "9";
                break;

            case ENCIPHERED_ONLINE_PIN:
                posEntryMode += "1";
                break;
            default:
                break;
        }
        return posEntryMode;
    }

    public static String packReconciliationDate(TransRec trans) {
        return trans.getAudit().getTransDateTimeAsString("yyMMdd");
    }

    public static String packReversalDateTime(IDependency d, TransRec trans) {
        if (d.getPayCfg().isReversalCopyOriginal()) {
            return trans.getAudit().getTransDateTimeAsString("yyMMddHHmmss");
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


    public static String packMsgReasonCode(TProtocol.ReversalReason reversalReason) {

        if (reversalReason == CUSTOMER_CANCELLATION) {
            return "08";
        } else if (reversalReason == COMMS_FAIL) {
            return "01";
        } else if (reversalReason == TIMEOUT) {
            return "01";
        } else
        {
            return "00";
        }
    }

    public static void updateRetRefNumber(TransRec trans) {
        try {
            SvfeUtils.packRetRefNumber(trans);
        } catch (Exception ex) {
            Timber.w(ex);
        }
    }

    public static String packRetRefNumber(TransRec trans) throws Exception {
        if (trans.getProtocol().getRRN() != null && !trans.getProtocol().getRRN().isEmpty())
            return trans.getProtocol().getRRN();
        return null;
    }



    public static String packAdditionVal(int tag, String value) {
        if (value == null || value.isEmpty())
            return "";

        return String.format("%03d%03d%s", tag, value.length(), value);
    }

    public static String packAdditionalData(TransRec trans, boolean reversal) {
        String data = "";
        String cvv = packSensitiveField(CVV, substValCvv);
        if (cvv != null && !cvv.isEmpty()) {
            data = packAdditionVal(13, "10" + cvv ); /* cvv value */
        }
        data += packAdditionVal(16, trans.getCard().isCardholderPresent() ? "1" : "0");

        if (reversal) {
            data += packAdditionVal(30, trans.getProtocol().getRRN());
            data += packAdditionVal(31, packMsgReasonCode(trans.getProtocol().getReversalReason()));
        }
        return data;
    }

    public static String packKsn(boolean cleartextMode) {

        P2PLib p2pInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();

        int pinBlockKsnLen = p2pEncrypt.getElementLength(PIN_BLOCK_KSN);
        if (pinBlockKsnLen > 0) {

            String pinBlockKsnTemplate = new String(new char[pinBlockKsnLen]).replace("\0", "X");
            CardholderDataElement[] pinBlockKsnElements = new CardholderDataElement[1];
            pinBlockKsnElements[0] = new CardholderDataElement( PIN_BLOCK_KSN, false, 1, pinBlockKsnLen, ASCII );

            IP2PEncrypt.EncryptParameters encryptParameters = null;
            String pinBlockKsn = null;

            if( cleartextMode ) {
                EncryptResult encryptPinBlockKsnResult = p2pEncrypt.encrypt( pinBlockKsnTemplate.getBytes(), new IP2PEncrypt.EncryptParameters( IP2PEncrypt.PaddingAlgorithm.NONE, NONE, 1 ), pinBlockKsnElements );
                if( encryptPinBlockKsnResult != null )
                    pinBlockKsn = new String(encryptPinBlockKsnResult.getEncryptedMessage());


            } else {
                EncryptResult encryptPinBlockKsnResult = p2pEncrypt.encrypt(pinBlockKsnTemplate.getBytes(), new IP2PEncrypt.EncryptParameters(IP2PEncrypt.PaddingAlgorithm.NONE, DUKPT_ANSI_2009, 1), pinBlockKsnElements);
                if (encryptPinBlockKsnResult != null)
                    pinBlockKsn = Util.byteArrayToHexString(encryptPinBlockKsnResult.getEncryptedMessage());
            }
            return pinBlockKsn;
        }
        return null;
    }


    public static String packPinBlock(boolean cleartextMode) {

        P2PLib p2pInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();

        int pinBlockLen = p2pEncrypt.getElementLength(PIN_BLOCK);
        if (pinBlockLen > 0) {

            String pinBlockTemplate = new String(new char[pinBlockLen]).replace("\0", "X");

            // only 1 element to substitute/encrypt for track 2
            CardholderDataElement[] pinBlockElements = new CardholderDataElement[1];
            pinBlockElements[0] = new CardholderDataElement( PIN_BLOCK, false, 1, pinBlockLen, ASCII );
            IP2PEncrypt.EncryptParameters encryptParameters = null;
            String pinBlock = null;
            if( cleartextMode ) {
                EncryptResult encryptPinBlockResult = p2pEncrypt.encrypt( pinBlockTemplate.getBytes(), new IP2PEncrypt.EncryptParameters( IP2PEncrypt.PaddingAlgorithm.NONE, NONE, 1 ), pinBlockElements );
                if( encryptPinBlockResult != null )
                    pinBlock = new String(encryptPinBlockResult.getEncryptedMessage());


            } else {
                // encrypted path
                EncryptResult encryptPinBlockResult = p2pEncrypt.encrypt(pinBlockTemplate.getBytes(), new IP2PEncrypt.EncryptParameters(IP2PEncrypt.PaddingAlgorithm.NONE, DUKPT_ANSI_2009, 1), pinBlockElements);
                if (encryptPinBlockResult != null)
                    pinBlock = Util.byteArrayToHexString(encryptPinBlockResult.getEncryptedMessage());
            }
            return pinBlock;

        }
        return null;
    }


    public static String packAdditionalAmounts(TransRec trans) {

        String result = "";
        TAmounts amounts = trans.getAmounts();
        if (amounts.getCashbackAmount() > 0) {
            AdditionalAmount cashback = new AdditionalAmount();
            cashback.setAccountType(ACC_TYPE_DEFAULT);
            cashback.setAmount(IsoUtils.padLeft("" + amounts.getCashbackAmount(), 12, '0'));
            cashback.setAmountType("40");
            cashback.setCurrencyCode(amounts.getCurrency());
            cashback.setSign("D");
            result = cashback.getAmount();
        }


        return result;
    }

    public static void packAdditionalEmvTags(TransRec trans, EmvTags tags, Iso8583Svfe msg)
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

        if (!tags.isTagSet(Tag.trans_curcy_code)) {
            String curr = "0" + msg.get(Bit._049_TRAN_CURRENCY_CODE);
            tags.add(Tag.trans_curcy_code, curr);
        }
    }


    public static String packIccData(IDependency d, TransRec trans, Iso8583Svfe msg)
            throws Exception {
        return packIccData_common(d, trans, msg, true);
    }

    public static String packIccData_common(IDependency d, TransRec trans, Iso8583Svfe msg, boolean additionalEmvTags)
            throws Exception {
        TCard cardinfo = trans.getCard();
        EmvTags tags = cardinfo.getTags();

        if (tags == null) {

            if (trans.getCard().isCtlsCaptured() && trans.getCtlsTagsString() != null)
                return trans.getCtlsTagsString();

            else if (trans.getCard().isIccCaptured() && trans.getEmvTagsString() != null)
                return trans.getEmvTagsString();
            else {
                Timber.i( "No tags to pack");
                return null;
            }
        }

        if (additionalEmvTags) {
            packAdditionalEmvTags(trans, tags, msg);
        }


        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        ArrayList<Tag> list = d.getProtocol().getEmvTagList();

        for (Tag t : list) {

            EmvTag tag = tags.get(t.value());
            if (tag != null) {
                if (!SvfePack.p2peEncryptEnabled && t.isSensitiveCardholderData()) {
                    String value = SvfePack.getNonSensitiveElement(t.getElement());
                    if( value != null ) {
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
        String emvData = getBinary().getString(bytes);
        return emvData;

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

            for (EmvTag tag : respTags.values()) {
                Timber.i( "Unpacked: " + tag.getHexTag() + ":" + Util.byteArrayToHexString(tag.getData()));
            }
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


    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String packMac(Iso8583Svfe msg) throws Exception {
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
        byte[] macBytes = P2PLib.getInstance().getIP2PSec().getDUKPTMac(newMsgData, IP2PSec.KeyGroup.TRANS_GROUP);

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

    private static String getSetField(Iso8583Svfe msg, int field) {
        if (msg.isFieldSet(field) && !"null".equals(msg.get(field))) {
            return msg.get(field);
        } else {
            return "";
        }
    }

    public static void unpackTimeData(TransRec trans, String field72) {
        if (field72 != null && field72.contains(":")) {
            //yyyymmddhhmmss
            String dateString = field72.substring(field72.indexOf(":") + 1);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            try {
                Date date = sdf.parse(dateString);
                trans.getProtocol().setServerDateTime(date);
            } catch (Exception ex) {
                Timber.w(ex);
            }

        }
    }


}
